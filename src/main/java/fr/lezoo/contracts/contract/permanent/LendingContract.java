package fr.lezoo.contracts.contract.permanent;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.contract.PaymentInfo;
import fr.lezoo.contracts.contract.PaymentType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Here the employer is the lender and the "employee" the borrower.
 * The paiement info correspond to the initial paiement being made
 */
public class LendingContract extends PermanentContract {
    //period between each refund in hours
    //numberRefunds is the number of time there need to be a refund and refunds made correspond to the refunds already made
    private final int period, interestRate, numberRefunds;
    private int refundsMade;
    private final double moneyPerRefund;
    private BukkitRunnable runnable;


    public LendingContract(ConfigurationSection section) {
        super(section);
        period = section.getInt("period");
        interestRate = section.getInt("interest-rate");
        numberRefunds = section.getInt("number-refunds");
        refundsMade = section.getInt("refunds-made");
        //Calculates the money that needs to be given per refund
        moneyPerRefund = (paymentInfo.getAmount() * (1 + ((double) interestRate) / 100)) / numberRefunds;
        startRunnable();
    }

    public LendingContract(UUID employer, UUID employee, PaymentInfo paiementInfo, int period, int numberRefunds, int interestRate) {
        super(employer, employee, paiementInfo);
        this.period = period;
        this.interestRate = interestRate;
        this.numberRefunds = numberRefunds;
        moneyPerRefund = (paymentInfo.getAmount() * (1 + ((double) interestRate) / 100)) / numberRefunds;
        //At the beginning
        refundsMade = 0;
        startRunnable();

    }

    public void startRunnable() {
        runnable = new BukkitRunnable() {

            @Override
            public void run() {

                if (paymentInfo.getType() == PaymentType.MONEY) {
                    if (Contracts.plugin.economy.getBalance(getEmployee()) > moneyPerRefund)  {
                        Contracts.plugin.economy.withdrawPlayer(getEmployee(), moneyPerRefund);
                        Contracts.plugin.economy.depositPlayer(getEmployee(), moneyPerRefund);
                        refundsMade++;
                    }
                    //If the player can't refund than the contract is cancelled and a middle man will come
                    else
                        callDispute();
                }
                if(refundsMade>=numberRefunds) {
                    endContract(ContractEndReason.FULFILLED);
                }
            }
        };
        //1 hour =60*60*20 ticks
        runnable.runTaskTimer(Contracts.plugin, 0, period * 60 * 60 * 20);
    }

}
