package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import org.bukkit.configuration.ConfigurationSection;

public class MiddlemanActionViewer extends EditableInventory {
    public MiddlemanActionViewer(String id) {
        super(id);
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        return null;
    }


}
