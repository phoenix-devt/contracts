package fr.lezoo.contracts.gui;

import fr.lezoo.contracts.gui.objects.EditableInventory;
import fr.lezoo.contracts.gui.objects.item.InventoryItem;
import org.bukkit.configuration.ConfigurationSection;

public class ContractCreationViewer extends EditableInventory {
    public ContractCreationViewer() {
        super("contract-creation");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        return null;
    }
}
