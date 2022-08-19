package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ContractsUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ContractAdminViewer extends EditableInventory {

    public ContractAdminViewer(String id) {
        super(id);
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("next-page"))
            return new NextPageItem(config);
        if (function.equals("previous-page"))
            return new PreviousPageItem(config);
        if (function.equals("contract"))
            return new ContractItem(config);

        return null;
    }

    public class ContractItem extends InventoryItem<ContractAdminInventory> {

        public ContractItem(ConfigurationSection config) {
            super(config);
        }


        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractAdminInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            ItemStack item = super.getDisplayedItem(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getId().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractAdminInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            return ContractsUtils.getContractPlaceholder(contract);
        }
    }

    public class PreviousPageItem extends SimpleItem<ContractAdminInventory> {

        public PreviousPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ContractAdminInventory inv) {
            return inv.page > 0;
        }

    }

    public class NextPageItem extends SimpleItem<ContractAdminInventory> {

        public NextPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ContractAdminInventory inv) {
            return inv.page < inv.maxPage;
        }
    }


    public class ContractAdminInventory extends GeneratedInventory {
        private int page;
        private final int maxPage;
        private final List<Contract> displayedContracts=Contracts.plugin.contractManager.getContractsOfState(ContractState.ADMIN_DISPUTED);
        public ContractAdminInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
            maxPage=Math.max(0,(displayedContracts.size()-1)/ editable.getSlots());
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return Contracts.plugin.placeholderParser.parse(player,str);
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            if (item instanceof NextPageItem) {
                page++;
                open();
            }
            if (item instanceof PreviousPageItem) {
                page--;
                open();
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }
}
