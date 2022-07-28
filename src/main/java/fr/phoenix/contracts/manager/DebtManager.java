package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.contract.debt.DebtInfo;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DebtManager {

    private final HashMap<UUID, List<DebtInfo>> debts = new HashMap<>();
    //This runnable checks for all the debts and pay back if a player is in debt
    private final BukkitRunnable runnable = new BukkitRunnable() {
        @Override
        public void run() {
            for (UUID uuid : debts.keySet()) {
                if (isInDept(uuid)) {
                    DebtInfo debtInfo = debts.get(uuid).get(0);
                    OfflinePlayer inDebt = Bukkit.getOfflinePlayer(uuid);
                    double amount = Contracts.plugin.economy.getBalance(inDebt);
                    if (amount > 0) {
                        OfflinePlayer toPay = Bukkit.getOfflinePlayer(debtInfo.getToPay());
                        if (amount > debtInfo.getAmount()) {
                            amount = debtInfo.getAmount();
                        }
                        if (inDebt.isOnline())
                            Message.PAYED_DEBT.format("to", toPay.getName(), "amount", amount)
                                    .send(inDebt.getPlayer());
                        if (toPay.isOnline())
                            Message.RECEIVED_DEPT.format("from", inDebt.getName(), "amount", amount)
                                    .send(toPay.getPlayer());
                        Contracts.plugin.economy.withdrawPlayer(inDebt, amount);
                        Contracts.plugin.economy.depositPlayer(toPay, amount);
                        debtInfo.reduceDebt(amount);

                        if (debtInfo.shouldRemove())
                            debts.get(inDebt.getUniqueId()).remove(0);

                    }
                }
            }
        }
    };


    public DebtManager() {
        runnable.runTaskTimer(Contracts.plugin, 0L, 5 * 20L);
    }


    /**
     * Used when a player to reimburse the debt he has by paying a certain amount.
     */


    public boolean isInDept(UUID uuid) {
        return debts.containsKey(uuid) && debts.get(uuid).size() > 0;
    }


    public void addDebt(UUID inDebt, UUID toPay, double amount) {
        //We first check if the player to pay was in debt of the inDept player before so we reduce the debt.
        int index = getDebtWith(toPay, inDebt);
        if (index != -1) {
            DebtInfo debtInfo = debts.get(toPay).get(index);
            double withdraw = Math.min(amount, debtInfo.getAmount());
            //We reimburse and update the debt
            debtInfo.reduceDebt(withdraw);
            if (debtInfo.shouldRemove())
                debts.get(toPay).remove(index);
            amount -= withdraw;
        }

        if (amount <= 0)
            return;
        //Once the debt was reduced we created a new debtInfo in the otherSide or update the current debt.

        if (!debts.containsKey(inDebt))
            debts.put(inDebt, new ArrayList<>());

        if (getDebtWith(inDebt, toPay) != -1) {
            debts.get(inDebt).get(index).addDebt(amount);
        } else {
            debts.get(inDebt).add(new DebtInfo(toPay, amount));
        }

    }


    /**
     * Checks if the first player is in dept with the second one and the return the index of the DeptInfo.
     * If not return -1
     */
    private int getDebtWith(UUID inDept, UUID toPay) {
        if (!debts.containsKey(inDept))
            return -1;
        for (int i = 0; i < debts.get(inDept).size(); i++) {
            if (debts.get(inDept).get(i).getToPay().equals(toPay))
                return i;
        }
        return -1;
    }
}
