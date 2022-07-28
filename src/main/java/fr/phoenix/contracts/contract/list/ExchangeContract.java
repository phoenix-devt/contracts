package fr.phoenix.contracts.contract.list;

import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ExchangeContract extends Contract implements Listener {
    private Material material;
    private double materialAmount;
    private double materialGiven = 0;

    public ExchangeContract(UUID employer) {
        super(ContractType.EXCHANGE, employer);
    }

    public ExchangeContract(ConfigurationSection section) {
        super(ContractType.EXCHANGE, section);

        addParameter("material", (player, str) -> {
            try {
                this.material = Material.valueOf(ContractsUtils.enumName(str));
            } catch (IllegalArgumentException exception) {
                Message.NOT_VALID_MATERIAL.format("input", ContractsUtils.enumName(str)).send(player);
            }
        });

        addParameter("material-amount", (player, str) -> {
            try {
                materialAmount = Double.parseDouble(str);
            } catch (Exception e) {
                Message.NOT_VALID_DOUBLE.format("input", str).send(player);
            }
        });
    }

    @EventHandler
    public void onGetItem(EntityPickupItemEvent event) {
        if (event.getItem().getThrower().equals(employee)) {
            ItemStack itemStack = event.getItem().getItemStack();
            if (itemStack.getType().equals(material)) {
                materialGiven += itemStack.getAmount();
                if (materialGiven >= materialAmount) {
                    // TODO
                }
            }
        }
    }


}
