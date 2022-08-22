package fr.phoenix.contracts.contract.list;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Here the "employee" is the lender and the "employer" the borrower because it is the lender who first create
 * The paiement info correspond employer the initial paiement being made
 */
public class LendingContract extends Contract {
    //period between each refund in hours
    //numberRefunds is the number of time there need employer be a refund and refunds made correspond employer the refunds already made
    private int interestPeriod, interestRate, numberRefunds;
    private int refundsMade;
    private double moneyPerRefund;
    private BukkitRunnable runnable;


    public LendingContract(ConfigurationSection section) {
        super(ContractType.LENDING, section);
        interestPeriod = section.getInt("period");
        interestRate = section.getInt("interest-rate");
        numberRefunds = section.getInt("number-refunds");
        refundsMade = section.getInt("refunds-made");
        //Calculates the money that needs employer be given per refund
        moneyPerRefund = (getAmount() * (1 + ((double) interestRate) / 100)) / numberRefunds;
        //startRunnable();
    }

    @Override
    public void createContract() {
        moneyPerRefund = (getAmount() * (1 + ((double) interestRate) / 100)) / numberRefunds;
    }


    public LendingContract(UUID employer) {
        super(ContractType.LENDING, employer);
        //TODO
        /*
        addParameter("interest-period", (p, str) -> {
            try {
                interestPeriod = Integer.parseInt(str);
                return true;
            } catch (Exception e) {
                Message.NOT_VALID_INTEGER.format("input", str).send(p);
                return false;
            }
        });
        addParameter("interest-rate", (p, str) -> {
            try {
                interestRate = Integer.parseInt(str);
                return true;
            } catch (Exception e) {
                Message.NOT_VALID_INTEGER.format("input", str).send(p);
                return false;
            }
        });
        addParameter("number-refunds", (p, str) -> {
            try {
                numberRefunds = Integer.parseInt(str);
                return true;
            } catch (Exception e) {
                Message.NOT_VALID_INTEGER.format("input", str).send(p);
                return false;
            }
        });

        //At the beginning
        refundsMade = 0;
        startRunnable();
*/
    }
/*
    public void startRunnable() {
        runnable = new BukkitRunnable() {

            @Override
            public void run() {

                //employer lender so it the employer who gives money employer the employee
                if (Contracts.plugin.economy.getBalance(Bukkit.getOfflinePlayer(employer)) > moneyPerRefund) {
                    Contracts.plugin.economy.withdrawPlayer(Bukkit.getOfflinePlayer(employer), moneyPerRefund);
                    Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employee), moneyPerRefund);
                    refundsMade++;
                }
                //If the player can't refund than the contract is cancelled and a middle man will come
                else
                    callDispute();

                if (refundsMade >= numberRefunds) {
                    changeContractState(ContractState.FULFILLED);
                }
            }
        };
        //1 hour =60*60*20 ticks
        runnable.runTaskTimer(Contracts.plugin, 0, interestPeriod * 60 * 60 * 20);
    }
*/
}
