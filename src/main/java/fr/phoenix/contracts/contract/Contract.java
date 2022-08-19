package fr.phoenix.contracts.contract;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.api.event.ContractStateChangeEvent;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ChatInput;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public abstract class Contract {
    protected final UUID contractId;
    protected final ContractType type;
    protected String name;
    // The employer creates the contract and the employee tries to fulfill it
    protected final UUID employer;
    // Not final
    protected UUID employee;
    protected double amount;
    /**
     * The amount of money you can lose/gain when a dispute is resolved.
     */
    protected double guarantee;

    protected ContractState state;
    private Map<ContractState, Long> stateEnteringTime = new HashMap<>();
    private long lastStateChange;

    // Hashmap to store all the parameters that need to be setup and to check if it has been setup
    protected final List<String> parametersList = new ArrayList<>();
    /**
     * Maps the parameter names to the function that will be applied when a player tries to set them.
     * If the function return true, The parameter will be effectively modified.
     */
    private final HashMap<String, BiFunction<Player, String, Boolean>> parameters = new HashMap<>();

    /**
     * Maps all the filledParameters with their value.
     */
    protected final HashMap<String, String> filledParameters = new HashMap<>();

    public Contract(ContractType type, UUID employer) {
        contractId = UUID.randomUUID();
        this.type = type;
        this.employer = employer;
        state = ContractState.OPEN;
        stateEnteringTime.put(ContractState.WAITING_ACCEPTANCE, System.currentTimeMillis());
        lastStateChange = System.currentTimeMillis();
        addParameter("name", (p, str) -> {
                    name = str;
                    return true;
                }
        );
        addParameter("guarantee", (p, str) -> {
                    try {
                        amount = Double.parseDouble(str);
                    } catch (Exception e) {
                        Message.NOT_VALID_DOUBLE.format("input", str).send(p);
                        return false;
                    }
                    return true;
                }
        );
        addParameter("payment-amount", (p, str) -> {
            try {
                setAmount(Double.parseDouble(str));
            } catch (Exception e) {
                Message.NOT_VALID_DOUBLE.format("input", str).send(p);
                return false;
            }
            return true;
        });

    }

    public Contract(ContractType type, ConfigurationSection section) {
        this.type = type;
        name = section.getString("name");
        contractId = UUID.fromString(section.getName());
        employee = section.getString("employee") == null ? null : UUID.fromString(section.getString("employee"));
        employer = UUID.fromString(section.getString("employer"));
        amount = section.getDouble("amount");
        state = ContractState.valueOf(ContractsUtils.enumName(section.getString("contract-state")));
        if (section.contains("entering-time")) {
            for (String key : section.getConfigurationSection("entering-time").getKeys(false)) {
                stateEnteringTime.put(ContractState.valueOf(ContractsUtils.enumName(key)), section.getLong("entering-time." + key));
            }
        }
    }

    public List<String> getParametersList() {
        return parametersList;
    }

    /**
     * This method is very important, it is used to have an
     * ordered list representing the parameters for the gui.
     */
    protected void addParameter(String str, BiFunction<Player, String, Boolean> consumer) {
        parameters.put(str, consumer);
        parametersList.add(str);
    }

    public void openChatInput(String str, PlayerData playerData, GeneratedInventory inv) {
        //If the player is already on chat input we block the access to a new chat input.
        if (playerData.isOnChatInput()) {
            Message.ALREADY_ON_CHAT_INPUT.format().send(playerData.getPlayer());
            return;
        }
        Message.SET_PARAMETER_ASK.format("parameter-name", ContractsUtils.chatName(str)).send(playerData.getPlayer());
        new ChatInput(playerData, inv, (p, val) -> {
            if (parameters.get(str).apply(p.getPlayer(), val))
                filledParameters.put(str, val);
            return true;
        });
    }

    public String getFilledParameter(String str) {
        return filledParameters.get(str);
    }

    public boolean hasParameter(String str) {
        return filledParameters.keySet().contains(str);
    }

    /**
     * Used to verify the contract has all is parameters setup.
     */
    public boolean hasAllParameters() {
        for (String param : parameters.keySet())
            if (!filledParameters.keySet().contains(param))
                return false;
        return true;
    }

    /**
     * Used to fully create the initialized contract and put it in the contract market.
     */
    public void createContract() {
        state = ContractState.WAITING_ACCEPTANCE;
        Message.CREATED_CONTRACT.format("contract-name", name).send(Bukkit.getPlayer(employer));
        Contracts.plugin.contractManager.registerContract(this);
        PlayerData.get(employer).addContract(this);
    }

    public ContractType getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmployee(UUID employee) {
        this.employee = employee;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getGuarantee() {
        return guarantee;
    }

    public void setGuarantee(double guarantee) {
        this.guarantee = guarantee;
    }

    public UUID getId() {
        return contractId;
    }

    public ContractState getState() {
        return state;
    }

    /**
     * If the entering time is set then the contract has been in the state.
     */
    public boolean hasBeenIn(ContractState state) {
        return stateEnteringTime.containsKey(state);
    }

    public long getEnteringTime(ContractState state) {
        return stateEnteringTime.get(state);
    }

    public long getLastStateChange() {
        return lastStateChange;
    }

    public String getName() {
        return name;
    }

    public String getEmployeeName() {
        return Bukkit.getOfflinePlayer(employee) != null ? Bukkit.getOfflinePlayer(employee).getName() : "NO_PLAYER";
    }

    public String getEmployerName() {
        return Bukkit.getOfflinePlayer(employer) != null ? Bukkit.getOfflinePlayer(employer).getName() : "NO_PLAYER";
    }

    public UUID getEmployer() {
        return employer;
    }

    /**
     * Method called when a player approves and accepts a contract.
     */
    public void whenAccepted(PlayerData playerData) {
        Validate.isTrue(state == ContractState.WAITING_ACCEPTANCE, "Contract is not waiting acceptance.");

        employee = playerData.getUuid();
        changeContractState(ContractState.OPEN);
        playerData.addContract(this);
        Message.EMPLOYEE_CONTRACT_ACCEPTED.format("contract-name", getName()).send(playerData.getPlayer());
    }

    public UUID getEmployee() {
        return employee;
    }

    public double getAmount() {
        return amount;
    }

    public void save(FileConfiguration config) {
        String str = contractId.toString();
        config.set(str + ".name", name);
        //The employee can be null at first
        if (employee != null)
            config.set(str + ".employee", employee.toString());
        config.set(str + ".employer", employer.toString());
        config.set(str + "amount", "" + getAmount());
        config.set(str + ".contract-state", ContractsUtils.ymlName(state.toString()));
        for (ContractState state : stateEnteringTime.keySet()) {
            config.set(str + ".entering-time." + ContractsUtils.ymlName(state.toString()), stateEnteringTime.get(state));
        }
    }

    /**
     * Calls a middle man because there is a dispute with the contract.
     */
    public void callDispute() {
        changeContractState(ContractState.MIDDLEMAN_DISPUTED);
        Player employeePlayer = Bukkit.getPlayer(employee);
        Player employerPlayer = Bukkit.getPlayer(employer);
        if (employeePlayer != null)
            Message.CONTRACT_DISPUTED.format("contract-name", name, "other", employerPlayer.getName())
                    .send(employeePlayer.getPlayer());
        if (employerPlayer != null)
            Message.CONTRACT_DISPUTED.format("contract-name", name, "other", employeePlayer.getName())
                    .send(employerPlayer.getPlayer());
        Contracts.plugin.middlemenManager.assignToRandomMiddleman(this);
    }

    /**
     * Requires employee to be online
     */
    public void completeContract(Player employee) {
        Validate.isTrue(state == ContractState.OPEN, "Contract is not open");
        changeContractState(ContractState.FULFILLED);

        Player employer = Bukkit.getPlayer(this.employer);
        if (employer != null)
            Message.CONTRACT_FULFILLED.format("contract-name", name, "other", employee.getName()).send(employer);
        Message.CONTRACT_FULFILLED.format("contract-name", name, "other", employer.getName()).send(employee);

        // Transactions
        Contracts.plugin.economy.withdrawPlayer(Bukkit.getOfflinePlayer(this.employer), amount);
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(this.employee), amount);
    }

    public void changeContractState(ContractState newState) {
        Bukkit.getPluginManager().callEvent(new ContractStateChangeEvent(this, newState));
        state = newState;
        lastStateChange = System.currentTimeMillis();
        stateEnteringTime.put(newState, System.currentTimeMillis());
    }

}
