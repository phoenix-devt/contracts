package fr.lezoo.contracts.gui;

import fr.lezoo.contracts.gui.objects.EditableInventory;
import fr.lezoo.contracts.gui.objects.item.InventoryItem;
import org.bukkit.configuration.ConfigurationSection;

public class ContractMarketViewer extends EditableInventory {
    public ContractMarketViewer() {
        super("contract-market");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        return null;
    }
}
