package fr.phoenix.contracts.contract;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.api.event.ContractStateChangeEvent;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ChatInput;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

public abstract class Contract {
    protected final UUID contractId;
    protected final ContractType type;
    protected String name;
    // The employer creates the contract and the employee tries employer fulfill it
    protected final UUID employer;
    // Not final
    protected UUID employee;
    protected UUID middleman;
    protected ContractParties middlemanDisputeCaller;
    protected ContractParties adminDisputeCaller;


    protected double amount;
    /**
     * The amount of money you can lose/gain when a dispute is resolved.
     */
    protected double guarantee;

    protected ContractState state;
    protected List<String> description = new ArrayList<>();

    protected List<Proposal> proposals = new ArrayList<>();

    protected double lastOffer;
    protected ContractParties lastOfferProvider;
    /**
     * The deadline for the project in days.
     */
    protected int deadLine;
    private Map<ContractState, Long> stateEnteringTime = new HashMap<>();
    private long lastStateChange;

    /**
     * Maps the parameter to their ids
     */
    private final Map<String, Parameter> parameters = new LinkedHashMap<>();


    public Contract(ContractType type, UUID employer) {
        contractId = UUID.randomUUID();
        this.type = type;
        this.employer = employer;
        state = ContractState.OPEN;
        stateEnteringTime.put(ContractState.WAITING_ACCEPTANCE, System.currentTimeMillis());
        lastStateChange = System.currentTimeMillis();
        addParameter(new Parameter("name", "The name of your contract",
                () -> Arrays.asList(name), (p, str) -> {
            name = str;
        }, () -> name == null));

        addParameter(new Parameter("description", "The description of your contract",
                () -> description, (p, str) -> {
            description.add(str);
        }, () -> false));

        addParameter(new Parameter("deadline", "The number of days the contract to be fulfilled before.", () -> Arrays.asList("" + deadLine), (p, str) -> {
            try {
                deadLine = Integer.parseInt(str);
            } catch (Exception e) {
                Message.NOT_VALID_INTEGER.format("input", str).send(p);
            }
        }, () -> deadLine <= 0));

        addParameter(new Parameter("guarantee", "The amount of money you will pay if you terminate the contract.", () -> Arrays.asList("" + guarantee), (p, str) -> {
            try {
                guarantee = Double.parseDouble(str);
            } catch (Exception e) {
                Message.NOT_VALID_DOUBLE.format("input", str).send(p);
            }
        }, () -> guarantee < 0));

        addParameter(new Parameter("payment-amount", "The amount of money you will pay the employee if he fulfills the contract.", () -> Arrays.asList("" + amount), (p, str) -> {
            try {
                amount = Double.parseDouble(str);
            } catch (Exception e) {
                Message.NOT_VALID_DOUBLE.format("input", str).send(p);
            }
        }, () -> amount <= 0));
    }

    public void setupParameters() {

    }


    public Contract(ContractType type, ConfigurationSection section) {
        this.type = type;
        name = section.getString("name");
        contractId = UUID.fromString(section.getName());
        employee = section.getString("employee") == null ? null : UUID.fromString(section.getString("employee"));
        employer = UUID.fromString(section.getString("employer"));
        amount = section.getDouble("amount");
        state = ContractState.valueOf(ContractsUtils.enumName(section.getString("contract-state")));
        deadLine = section.getInt("deadline");
        description = section.getStringList("description");
        if (section.contains("entering-time")) {
            for (String key : section.getConfigurationSection("entering-time").getKeys(false)) {
                stateEnteringTime.put(ContractState.valueOf(ContractsUtils.enumName(key)), section.getLong("entering-time." + key));
            }
        }
        if (section.contains("proposals")) {
            for (String key : section.getConfigurationSection("proposals").getKeys(false)) {
                addProposal(new Proposal(section.getConfigurationSection("proposals." + key)));
            }
        }

    }


    public void addProposal(Proposal proposal) {
        proposals.add(proposal);
    }

    public List<Proposal> getProposals() {
        return proposals;
    }

    /**
     * This method is very important, it is used employer have an
     * ordered list representing the parameters for the gui.
     */
    protected void addParameter(Parameter parameter) {
        parameters.put(parameter.getId(), parameter);
    }

    public double getLastOffer() {
        return lastOffer;
    }

    @Nullable
    /**
     * @return returns null if there hasn't been any offers yet.
     */
    public ContractParties getLastOfferProvider() {
        return lastOfferProvider;
    }

    public ContractParties getContractParty(PlayerData playerData) {
        if (isEmployee(playerData))
            return ContractParties.EMPLOYEE;
        if (isEmployer(playerData))
            return ContractParties.EMPLOYER;
        if (isMiddleman(playerData))
            return ContractParties.MIDDLEMAN;
        return null;
    }


    public void openChatInput(String str, PlayerData playerData, GeneratedInventory inv) {
        //If the player is already on chat input we block the access employer a new chat input.
        if (playerData.isOnChatInput()) {
            Message.ALREADY_ON_CHAT_INPUT.format().send(playerData.getPlayer());
            return;
        }
        Message.SET_PARAMETER_ASK.format("parameter-name", ContractsUtils.chatName(str)).send(playerData.getPlayer());
        new ChatInput(playerData, inv, (p, val) -> {
            parameters.get(str).set(p.getPlayer(), val);
            return true;
        });
    }


    /**
     * Used employer verify the contract has all is parameters setup.
     */
    public boolean allParameterFilled() {
        for (Parameter param : parameters.values())
            if (param.needsToBeFilled())
                return false;
        return true;
    }

    /**
     * Used employer fully create the initialized contract and put it in the contract market.
     */
    public void createContract() {
        state = ContractState.WAITING_ACCEPTANCE;
        Message.CREATED_CONTRACT.format("contract-name", name, "amount", amount).send(Bukkit.getPlayer(employer));
        Contracts.plugin.contractManager.registerContract(this);
        PlayerData.getOrLoad(employer).addContract(this);
        //We remove the amount of the contract to the player
        Contracts.plugin.economy.withdrawPlayer(Bukkit.getOfflinePlayer(getEmployer()), amount);

    }

    public ContractType getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return The list of all the parameters in the order of insertion.
     */
    public List<Parameter> getParametersList() {
        List<Parameter> result = new ArrayList<>();
        parameters.keySet().forEach(str -> result.add(parameters.get(str)));
        return result;
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

    public Parameter getParameter(String name) {
        return parameters.get(name);
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

    public String getMiddlemanName() {
        return Bukkit.getOfflinePlayer(middleman) != null ? Bukkit.getOfflinePlayer(middleman).getName() : "NO_PLAYER";
    }

    public String getEmployerName() {
        return Bukkit.getOfflinePlayer(employer) != null ? Bukkit.getOfflinePlayer(employer).getName() : "NO_PLAYER";
    }

    public UUID getEmployer() {
        return employer;
    }

    public boolean canBeReviewed() {
        if(hasBeenIn(ContractState.FULFILLED)) {
            return  (getEnteringTime(ContractState.FULFILLED) - System.currentTimeMillis()) / 1000 * 3600 * 24 < Contracts.plugin.configManager.reviewPeriod;
        }
        else
        {
            return state==ContractState.RESOLVED&&(getEnteringTime(ContractState.RESOLVED) - System.currentTimeMillis()) / 1000 * 3600 * 24 < Contracts.plugin.configManager.reviewPeriod;
        }
    }


    /**
     * Gets and load the data of the other person with you in the contract.
     *
     * @return
     */
    @Nullable
    public PlayerData getOther(PlayerData playerData) {
        UUID otherUUID = playerData.getUuid().equals(getEmployee()) ? getEmployer() : getEmployee();
        if (otherUUID == null)
            return null;
        return PlayerData.getOrLoad(otherUUID);
    }

    public UUID getEmployee() {
        return employee;
    }

    public double getAmount() {
        return amount;
    }

    public List<String> getDescription() {
        return description;
    }

    public int getDeadLine() {
        return deadLine;
    }

    @Nullable
    public ContractParties getMiddlemanDisputeCaller() {
        return middlemanDisputeCaller;
    }

    @Nullable
    public ContractParties getAdminDisputeCaller() {
        return adminDisputeCaller;
    }

    public boolean isEmployee(PlayerData playerData) {
        return playerData.getUuid().equals(employee);
    }

    public boolean isMiddleman(PlayerData playerData) {
        return playerData.getUuid().equals(middleman);

    }

    public boolean isEmployer(PlayerData playerData) {
        return playerData.getUuid().equals(employer);
    }

    public boolean isEmployee(UUID uuid) {
        return uuid.equals(employee);
    }

    public boolean isMiddleman(UUID uuid) {
        return uuid.equals(middleman);

    }

    public boolean isEmployer(UUID uuid) {
        return uuid.equals(employer);
    }

    public void save(FileConfiguration config) {
        String str = contractId.toString();
        config.set(str + ".name", name);
        //The employee Ve be null at first
        if (employee != null)
            config.set(str + ".employee", employee.toString());
        config.set(str + ".employer", employer.toString());
        config.set(str + ".amount", "" + getAmount());
        config.set(str + ".contract-state", ContractsUtils.ymlName(state.toString()));
        config.set(str + ".deadline", deadLine);
        config.set(str + ".description", description);
        for (ContractState state : stateEnteringTime.keySet()) {
            config.set(str + ".entering-time." + ContractsUtils.ymlName(state.toString()), stateEnteringTime.get(state));
        }
        ConfigurationSection section = config.createSection(str + ".proposals");
        for (Proposal proposal : getProposals()) {
            proposal.save(section);
        }
    }

    /**
     * Calls a middle man because there is a dispute with the contract.
     */
    public void callDispute(PlayerData playerData) {
        adminDisputeCaller = isEmployee(playerData) ? ContractParties.EMPLOYEE : ContractParties.EMPLOYER;
        changeContractState(ContractState.MIDDLEMAN_DISPUTED);
        Player employeePlayer = Bukkit.getPlayer(employee);
        Player employerPlayer = Bukkit.getPlayer(employer);
        if (employeePlayer != null)
            Message.CONTRACT_DISPUTED.format("contract-name", name, "who", employee.equals(middlemanDisputeCaller) ? "You" : employerPlayer.getName())
                    .send(employeePlayer.getPlayer());
        if (employerPlayer != null)
            Message.CONTRACT_DISPUTED.format("contract-name", name, "who", employer.equals(middlemanDisputeCaller) ? "You" : employeePlayer.getName())
                    .send(employerPlayer.getPlayer());
        Contracts.plugin.middlemenManager.assignToRandomMiddleman(this);
    }

    /**
     * Calls a middle man because there is a dispute with the contract.
     */
    public void callAdminDispute(PlayerData playerData) {
        adminDisputeCaller = isEmployee(playerData) ? ContractParties.EMPLOYEE : ContractParties.EMPLOYER;
        changeContractState(ContractState.ADMIN_DISPUTED);
        Player employeePlayer = Bukkit.getPlayer(employee);
        Player employerPlayer = Bukkit.getPlayer(employer);
        Player middlemanPlayer = Bukkit.getPlayer(middleman);
        if (employeePlayer != null)
            Message.ADMIN_DISPUTED.format("contract-name", name, "who", employee.equals(adminDisputeCaller) ? "You" : employerPlayer.getName())
                    .send(employeePlayer.getPlayer());
        if (employerPlayer != null)
            Message.ADMIN_DISPUTED.format("contract-name", name, "who", employer.equals(adminDisputeCaller) ? "You" : employeePlayer.getName())
                    .send(employerPlayer.getPlayer());
        if (middlemanPlayer != null) {
            Message.ADMIN_DISPUTED.format("contract-name", name, "who", playerData.getPlayerName())
                    .send(middlemanPlayer.getPlayer());
        }
        Contracts.plugin.middlemenManager.assignToRandomMiddleman(this);
    }

    /**
     * When the employer judges that the job is done and pays the employee for it.
     */
    public void endContract() {
        changeContractState(ContractState.RESOLVED);
        PlayerData employerData = PlayerData.getOrLoad(employer);
        Player employeePlayer = Bukkit.getPlayer(employee);
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employee), guarantee + amount);
        if (employeePlayer != null)
            Message.EMPLOYEE_CONTRACT_ENDED.format("employer", employerData.getPlayerName(), "contract-name", getName(), "amount", amount, "guarantee", guarantee).send(employeePlayer);

        Message.EMPLOYER_CONTRACT_ENDED.format("employee", Bukkit.getOfflinePlayer(employee).getName(), "contract-name", getName(), "amount", amount).send(employerData.getPlayer());
    }


    public void whenOfferCreated(PlayerData playerData, double value) {
        lastOfferProvider = getContractParty(playerData);
        lastOffer = value;
        boolean isEmployee = isEmployee(playerData);
        OfflinePlayer employeePlayer = Bukkit.getOfflinePlayer(employee);
        OfflinePlayer employerPlayer = Bukkit.getOfflinePlayer(employer);
        if (employeePlayer != null)
            Message.OFFER_CREATED.format("other", isEmployee ? employerPlayer.getName() : employeePlayer.getName(), "contract-name", getName()).send(employeePlayer.getPlayer());
        if (employerPlayer != null)
            Message.OFFER_RECEIVED.format("other", isEmployee ? employerPlayer.getName() : employeePlayer.getName(), "contract-name", getName()).send(employerPlayer.getPlayer());
    }

    public void whenOfferAccepted() {
        changeContractState(ContractState.RESOLVED);
        Player employeePlayer = Bukkit.getPlayer(employee);
        Player employerPlayer = Bukkit.getPlayer(employer);
        //Deposit the right amount of money to the 2 parts.
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employee), lastOffer+guarantee);
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employer),amount-lastOffer);
        if (employeePlayer != null)
            Message.EMPLOYEE_CONTRACT_ENDED.format("employer", Bukkit.getOfflinePlayer(employer).getName(), "contract-name", getName(), "amount", amount, "guarantee", guarantee).send(employeePlayer);
        if (employerPlayer != null)
            Message.EMPLOYER_CONTRACT_ENDED.format("employee", Bukkit.getOfflinePlayer(employee).getName(), "contract-name", getName(), "amount", amount).send(employerPlayer);

    }


    /**
     * When the employee wants to cancel the contract he has to pay the entire guarantee.
     */
    public void whenCancelledByEmployee() {
        //If cancelled by the employee
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employer), guarantee);

    }


    /**
     * Requires employee employer be online
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

    public boolean hasAlreadyProposed(PlayerData proposingPlayer) {
        return proposals.stream().map(proposal -> proposal.getEmployee()).toList().contains(proposingPlayer);
    }

    public void setMiddleman(UUID middleman) {
        this.middleman = middleman;
    }

    public boolean hasMiddleman() {
        return middleman != null;
    }

    public UUID getMiddleman() {
        return middleman;
    }


    public void makeProposal(PlayerData proposingPlayer) {
        //If the player already made a proposal
        if (hasAlreadyProposed(proposingPlayer)) {
            Message.HAS_ALREADY_MADE_PROPOSAL.format("contract-name", getName()).send(proposingPlayer.getPlayer());
            return;
        }
        proposals.add(new Proposal(this, proposingPlayer, PlayerData.getOrLoad(employer), System.currentTimeMillis()));
    }


    public Placeholders getContractPlaceholder(PlayerData playerData) {
        Placeholders holders = new Placeholders();
        holders.register("name", getName());
        String result = "";
        for (String str : getDescription())
            result += "\n" + str;

        if (result != "")
            result = result.substring(1);
        holders.register("description", result);
        holders.register("state", getState().toString());
        holders.register("type", ContractsUtils.chatName(getType().toString()));
        holders.register("employee", getEmployee() != null ? getEmployeeName() : "");
        holders.register("employer", getEmployerName());
        if (hasBeenIn(ContractState.MIDDLEMAN_DISPUTED)) {
            holders.register("middleman", getMiddleman() != null ? getMiddlemanName() : "");
            holders.register("middleman-dispute-caller", middlemanDisputeCaller.getOfflinePlayer(this).getName());
        }
        if (state == ContractState.OPEN)
            holders.register("received-offer", lastOfferProvider != null && getContractParty(playerData) != lastOfferProvider);
        if (state == ContractState.WAITING_ACCEPTANCE)
            holders.register("proposals", getProposals().size());

        PlayerData employer = PlayerData.getOrLoad(getEmployer());
        holders.register("employer-reputation", ContractsUtils.formatNotation(employer.getMeanNotation()));
        holders.register("employer-total-reviews", employer.getNumberReviews());
        holders.register("payment", "" + getAmount());
        holders.register("guarantee", "" + getGuarantee());
        PlayerData other = getOther(playerData);
        if (other != null) {
            holders.register("has-made-review",other.hasReceivedReviewFor(this));
            holders.register("other", other.getPlayerName());
            holders.register("other-reputation", ContractsUtils.formatNotation(employer.getMeanNotation()));
            holders.register("other-total-reviews", other.getNumberReviews());
        }
        for (ContractState state : ContractState.values()) {
            holders.register(ContractsUtils.ymlName(state.toString()) + "-since",
                    hasBeenIn(state) ? ContractsUtils.formatTime(getEnteringTime(state)) : "Not been employer this state");
        }
        return holders;
    }

}
