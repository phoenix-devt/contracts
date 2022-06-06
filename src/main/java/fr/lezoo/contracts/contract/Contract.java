package fr.lezoo.contracts.contract;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.event.ContractStateChangeEvent;
import fr.lezoo.contracts.gui.objects.GeneratedInventory;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.utils.ChatInput;
import fr.lezoo.contracts.utils.ContractsUtils;
import fr.lezoo.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class Contract {
    protected final UUID contractId;
    protected String name;
    //The employer creates the contract and the employee tries to fulfill it
    protected final UUID employer;
    //Not final
    protected UUID employee;
    protected PaymentInfo paymentInfo = new PaymentInfo();
    protected ContractState state;
    private long creationTime, acceptanceTime, endTime;

    //Hashmap to store all the parameters that need to be setup and to check if it has been setup
    private final HashMap<String, BiConsumer<Player, String>> parameters = new HashMap<>();
    protected List<String> parametersList = new ArrayList<>();

    //Map all the filledParameters with their value
    protected final HashMap<String, String> filledParameters = new HashMap<>();

    public Contract(UUID employer) {
        contractId = UUID.randomUUID();
        this.employer = employer;
        state = ContractState.OPEN;
        creationTime = System.currentTimeMillis();
        addParameter("name", (p, str) -> {
                    name = str;
                    filledParameters.put("name", str);
                }
        );
        addParameter("payment-amount", (p, str) -> {
            try {
                paymentInfo.setAmount(Double.parseDouble(str));
                filledParameters.put("payment-amount", str);
            } catch (Exception e) {
                Message.NOT_VALID_DOUBLE.format("input", str).send(p);
            }
        });
        addParameter("payment-type", (p, str) -> {
            try {
                paymentInfo.setType(PaymentType.valueOf(ContractsUtils.enumName(str)));
                filledParameters.put("payment-type", str);
            } catch (Exception e) {
                Message.NOT_VALID_PAYMENT_TYPE.format("input", str).send(p);
            }
        });

    }

    public Contract(ConfigurationSection section) {
        name = section.getString("name");
        contractId = UUID.fromString(section.getName());
        employee = section.getString("employee")==null?null:UUID.fromString(section.getString("employee"));
        employer = UUID.fromString(section.getString("employer"));
        paymentInfo = new PaymentInfo(Objects.requireNonNull(section.getConfigurationSection("payment-info")));
        state = ContractState.valueOf(ContractsUtils.enumName(section.getString("contract-state")));
        creationTime = section.getLong("creation-time");
        acceptanceTime = section.getLong("acceptance-time");
        endTime = section.contains("end-time") ? section.getLong("end-time") : 0;
    }


    public List<String> getParametersList() {
        return parametersList;
    }

    /**
     * This method is very important, it is used to have an ordered list representing the parameters for the gui.
     */
    protected void addParameter(String str, BiConsumer<Player, String> consumer) {
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
            parameters.get(str).accept(p.getPlayer(), val);
            return true;
        }
        );
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
        Contracts.plugin.contractManager.addContract(this);
        PlayerData.get(employer).addContract(this);
    }

    ;


    public void setName(String name) {
        this.name = name;
    }

    public void setEmployee(UUID employee) {
        this.employee = employee;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public UUID getUuid() {
        return contractId;
    }

    public ContractState getState() {
        return state;
    }

    public boolean isEnded() {
        return endTime > 0;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getAcceptanceTime() {
        return acceptanceTime;
    }

    public long getEndTime() {
        return endTime;
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
     * Method called when a player approves and accept a contract and becomes employed for it.
     *
     * @param employeeId
     */
    public void whenAccepted(UUID employeeId) {
        acceptanceTime = System.currentTimeMillis();
        employee = employeeId;
        PlayerData.get(employeeId).addContract(this);
        Message.CONTRACT_ACCEPTED.format("contract-name",getName()).send(PlayerData.get(employeeId).getPlayer());
    }

    public UUID getEmployee() {
        return employee;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void save(FileConfiguration config) {
        String str = contractId.toString();
        config.set(str + ".name", name);
        //The employee can be null at first
        if (employee != null)
            config.set(str + ".employee", employee.toString());
        config.set(str + ".employer", employer.toString());
        config.set(str + ".payment-info.type", ContractsUtils.ymlName(paymentInfo.getType().toString()));
        config.set(str + ".payment-info.amount", "" + paymentInfo.getAmount());
        config.set(str + ".contract-state", ContractsUtils.ymlName(state.toString()));
        config.set(str + ".creation-time", creationTime);
        config.set(str + ".acceptance-time", acceptanceTime);
        config.set(str + ".end-time", endTime);

    }
    //TODO

    /**
     * Calls a middle man because there is a dispute with the contract.
     */
    public void callDispute() {

    }


    public void changeContractState(ContractState newState) {
        ContractStateChangeEvent event = new ContractStateChangeEvent(this, newState);
        Bukkit.getPluginManager().callEvent(event);
        //We change the state of the contract and add it to contractsToReview
        state = newState;


    }
}
