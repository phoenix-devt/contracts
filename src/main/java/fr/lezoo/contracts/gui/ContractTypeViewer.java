package fr.lezoo.contracts.gui;

import fr.lezoo.contracts.contract.ContractType;
import fr.lezoo.contracts.gui.objects.EditableInventory;
import fr.lezoo.contracts.gui.objects.GeneratedInventory;
import fr.lezoo.contracts.gui.objects.item.InventoryItem;
import fr.lezoo.contracts.gui.objects.item.Placeholders;
import fr.lezoo.contracts.gui.objects.item.SimpleItem;
import fr.lezoo.contracts.manager.InventoryManager;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.utils.ContractsUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.function.BiConsumer;

public class ContractTypeViewer extends EditableInventory {


    public ContractTypeViewer() {
        super("contract-type");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("lending"))
            return new LendingContractItem(config);
        if (function.equals("salary"))
            return new SalaryContractItem(config);
        if (function.equals("kill"))
            return new KillContractItem(config);
        if (function.equals("exchange"))
            return new ExchangeContractItem(config);
        return null;
    }

    public ContractTypeInventory newInventory(PlayerData playerData, InventoryToOpenType inventoryToOpen) {
        return new ContractTypeInventory(playerData, this, inventoryToOpen);
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


    public class ContractTypeInventory extends GeneratedInventory {
        private final InventoryToOpenType inventoryToOpen;

        public ContractTypeInventory(PlayerData playerData, EditableInventory editable, InventoryToOpenType inventoryToOpen) {
            super(playerData, editable);
            this.inventoryToOpen = inventoryToOpen;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return ContractsUtils.applyColorCode(
                    str.replace("{type}", inventoryToOpen == InventoryToOpenType.CREATION_VIEWER ? "Contract Creation" : "Contract Market"));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            //Opens the right inventory for the players. (market or create)
            event.setCancelled(true);
            if (item instanceof LendingContractItem) {
                inventoryToOpen.open(playerData, ContractType.LENDING);
            }
            if (item instanceof SalaryContractItem) {
                inventoryToOpen.open(playerData, ContractType.SALARY);
            }
            if (item instanceof KillContractItem) {
                inventoryToOpen.open(playerData, ContractType.KILL);
            }
            if (item instanceof ExchangeContractItem) {
                inventoryToOpen.open(playerData, ContractType.EXCHANGE);
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }

    public enum InventoryToOpenType {
        MARKET_VIEWER(((playerData, contractType) -> InventoryManager.CONTRACT_MARKET.newInventory(playerData, contractType).open())),
        CREATION_VIEWER(((playerData, contractType) -> InventoryManager.CONTRACT_CREATION.newInventory(playerData, contractType).open()));

        private final BiConsumer<PlayerData, ContractType> openInv;

        InventoryToOpenType(BiConsumer<PlayerData, ContractType> openInv) {
            this.openInv = openInv;
        }

        public void open(PlayerData playerData, ContractType contractType) {
            openInv.accept(playerData, contractType);
        }
    }
}
