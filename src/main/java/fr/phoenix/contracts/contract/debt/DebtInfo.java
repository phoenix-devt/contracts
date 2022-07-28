package fr.lezoo.contracts.contract.debt;

import fr.lezoo.contracts.player.PlayerData;

import java.util.UUID;

public class DebtInfo {
    private final UUID toPay;
    private double amount;


    public DebtInfo(UUID toPay, double amount) {
        this.toPay = toPay;
        this.amount = amount;
    }

    public void reduceDebt(double amount) {
        this.amount-=amount;
    }

    public void addDebt(double amount) {
        this.amount+=amount;
    }
    public boolean shouldRemove() {
        return amount<=0;
    }


    public UUID getToPay() {
        return toPay;
    }

    public double getAmount() {
        return amount;
    }
}
