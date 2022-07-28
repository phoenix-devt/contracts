package fr.phoenix.contracts.contract.classic;

import fr.lezoo.contracts.contract.ContractState;
import fr.lezoo.contracts.utils.ContractsUtils;
import fr.lezoo.contracts.utils.message.Message;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.calledmethods.qual.EnsuresCalledMethods;

import java.util.UUID;

public class ExchangeContract extends ClassicContract {
    private Material material;
    private double materialAmount;
    private double materialGiven = 0;

    public ExchangeContract(ConfigurationSection section) {
        super(section);
        addParameter("material", (player, str) -> {
            Material material = Material.valueOf(ContractsUtils.enumName(str));
            if (material == null) {
                Message.NOT_VALID_MATERIAL.format("input", ContractsUtils.enumName(str)).send(player);
                return;
            }
            this.material = material;
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
    public void onGetItem(EntityPickupItemEvent e) {
        if (e.getItem().getThrower().equals(employee)) {
            ItemStack itemStack = e.getItem().getItemStack();
            if (itemStack.getType().equals(material)) {
                materialGiven += itemStack.getAmount();
                if (materialGiven >= materialAmount) {
                    changeContractState(ContractState.FULFILLED);
                }
            }
        }
    }

    public ExchangeContract(UUID employer) {
        super(employer);
    }


}
