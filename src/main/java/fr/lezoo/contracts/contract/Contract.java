package fr.lezoo.contracts.contract;

import fr.lezoo.contracts.event.ContractStateChangeEvent;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.utils.ContractsUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;
import java.util.UUID;

public abstract class Contract {
    protected final UUID contractId;
    //The employer creates the contract and the employee tries to fulfill it
    protected final UUID employer, employee;
    protected final PaymentInfo paymentInfo;
    protected ContractState state;
    private long startTime, endTime;

    public Contract(UUID employer, UUID employee, PaymentInfo paymentInfo) {
        contractId = UUID.randomUUID();
        this.employer = employer;
        this.employee = employee;
        this.paymentInfo = paymentInfo;
        state = ContractState.OPEN;
        startTime = System.currentTimeMillis();
    }

    public Contract(ConfigurationSection section) {
        contractId = UUID.fromString(section.getName());
        employee = UUID.fromString(section.getString("employee"));
        employer = UUID.fromString(section.getString("employer"));
        paymentInfo = new PaymentInfo(Objects.requireNonNull(section.getConfigurationSection("payment-info")));
        state = ContractState.valueOf(ContractsUtils.enumName(section.getString("contract-state")));
        startTime = section.getLong("start-time");
        endTime = section.contains("end-time") ? section.getLong("end-time") : 0;
    }


    public UUID getUuid() {
        return contractId;
    }

    public ContractState getState() {
        return state;
    }

    public boolean isEnded() {
        return endTime>0;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public OfflinePlayer getEmployer() {
        return Bukkit.getOfflinePlayer(employer);
    }

    public OfflinePlayer getEmployee() {
        return Bukkit.getOfflinePlayer(employee);
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void save(FileConfiguration config) {
        String str = contractId.toString();
        config.set(str + ".employee", employee.toString());
        config.set(str + ".employer", employee.toString());
        config.set(str + ".payment-info.type", ContractsUtils.ymlName(paymentInfo.getType().toString()));
        config.set(str + ".payment-info.amount", "" + paymentInfo.getAmount());
        config.set(str + ".contract-state", ContractsUtils.ymlName(state.toString()));
        config.set(str + ".start-time", startTime);
        config.set(str + ".end-time", endTime);

    }
    //TODO

    /**
     * Calls a middle man because there is a dispute with the contract.
     */
    public void callDispute() {

    }


    public void changeContratState(ContractState newState) {
        ContractStateChangeEvent event= new ContractStateChangeEvent(this,newState);
        Bukkit.getPluginManager().callEvent(event);
        //We change the state of the contract and add it to contractsToReview
        state = newState;

        //If the employer or employee is their we change the values of their open... maps
        if(PlayerData.has(employee))
            PlayerData.get(employee).changeState(contractId);
        if(PlayerData.has(employer))
            PlayerData.get(employer).changeState(contractId);


    }
}
