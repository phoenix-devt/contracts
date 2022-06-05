package fr.lezoo.contracts.contract;

import fr.lezoo.contracts.event.ContractStateChangeEvent;
import fr.lezoo.contracts.utils.ContractsUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;
import java.util.UUID;

public abstract class Contract {
    protected final UUID contractId;
    protected final String name;
    //The employer creates the contract and the employee tries to fulfill it
    protected final UUID employer;
    //Not final
    protected UUID employee;
    protected final PaymentInfo paymentInfo;
    protected ContractState state;
    private long creationTime, approvalTime, endTime;

    public Contract(UUID employer, PaymentInfo paymentInfo, String name) {
        contractId = UUID.randomUUID();
        this.name = name;
        this.employer = employer;
        this.paymentInfo = paymentInfo;
        state = ContractState.OPEN;
        creationTime = System.currentTimeMillis();
    }

    public Contract(ConfigurationSection section) {
        name = section.getString("name");
        contractId = UUID.fromString(section.getName());
        employee = UUID.fromString(section.getString("employee"));
        employer = UUID.fromString(section.getString("employer"));
        paymentInfo = new PaymentInfo(Objects.requireNonNull(section.getConfigurationSection("payment-info")));
        state = ContractState.valueOf(ContractsUtils.enumName(section.getString("contract-state")));
        creationTime = section.getLong("creation-time");
        approvalTime=section.getLong("approval-time");
        endTime = section.contains("end-time") ? section.getLong("end-time") : 0;
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

    public long getApprovalTime() {
        return approvalTime;
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
     * @param employeeId
     */
    public void approvedBy(UUID employeeId) {
        approvalTime = System.currentTimeMillis();
        employee = employeeId;
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
        config.set(str + ".employee", employee.toString());
        config.set(str + ".employer", employee.toString());
        config.set(str + ".payment-info.type", ContractsUtils.ymlName(paymentInfo.getType().toString()));
        config.set(str + ".payment-info.amount", "" + paymentInfo.getAmount());
        config.set(str + ".contract-state", ContractsUtils.ymlName(state.toString()));
        config.set(str + ".creation-time", creationTime);
        config.set(str+".approval-time",approvalTime);
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
