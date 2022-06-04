package fr.lezoo.contracts.contract;

import fr.lezoo.contracts.utils.ContractsUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class PaymentInfo {
    private final PaymentType type;
    private final double amount;

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

}