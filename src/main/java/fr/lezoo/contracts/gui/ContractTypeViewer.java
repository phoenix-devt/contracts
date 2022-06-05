package fr.lezoo.contracts.gui;

import fr.lezoo.contracts.gui.objects.EditableInventory;
import fr.lezoo.contracts.gui.objects.GeneratedInventory;
import fr.lezoo.contracts.gui.objects.item.InventoryItem;
import fr.lezoo.contracts.gui.objects.item.Placeholders;
import fr.lezoo.contracts.gui.objects.item.SimpleItem;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.utils.ContractsUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ContractTypeViewer extends EditableInventory {


    public ContractTypeViewer() {
        super("contract-type");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if(function.equals("lending"))
            return new LendingContractItem(config);
        if(function.equals("salary"))
            return new SalaryContractItem(config);
        if(function.equals("kill"))
            return new KillContractItem(config);
        if(function.equals("exchange"))
            return new ExchangeContractItem(config);
        return null;
    }


    public class LendingContractItem extends SimpleItem<ContractTypeInventory> {
        public LendingContractItem(ConfigurationSection config) {
            super(config);
        }
    }

    public class SalaryContractItem extends SimpleItem<ContractTypeInventory> {
        public SalaryContractItem(ConfigurationSection config) {
            super(config);
        }
    }

    public class KillContractItem extends SimpleItem<ContractTypeInventory> {
        public KillContractItem(ConfigurationSection config) {
            super(config);
        }
    }

    public class ExchangeContractItem extends SimpleItem<ContractTypeInventory> {
        public ExchangeContractItem(ConfigurationSection config) {
            super(config);
        }
    }



    public  class ContractTypeInventory extends GeneratedInventory {
     private final EditableInventory inventoryToOpen;

        public ContractTypeInventory(PlayerData playerData, EditableInventory editable,EditableInventory inventoryToOpen) {
            super(playerData, editable);
            this.inventoryToOpen=inventoryToOpen;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return ContractsUtils.applyColorCode(
                    str.replace("{type}",inventoryToOpen instanceof ContractCreationViewer?"Contract Creation":"Contract Market"));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }
}
