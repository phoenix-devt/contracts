package fr.phoenix.contracts.contract;

import fr.phoenix.contracts.utils.ContractsUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class PaymentInfo {
    private PaymentType type;
    private double amount;


    public PaymentInfo() {

    }

    public PaymentInfo(PaymentType type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    public PaymentInfo(ConfigurationSection section) {
        type= Objects.requireNonNull(PaymentType.valueOf(ContractsUtils.enumName(section.getString("type"))));
        amount=section.getInt("amount");
    }

    public PaymentType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}