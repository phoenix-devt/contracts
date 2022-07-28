package fr.phoenix.contracts.listener;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.gui.objects.PluginInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;

public class PlayerListener implements Listener {

    /**
     * Load player data of players when logging in
     */
    @EventHandler
    public void a(PlayerJoinEvent event) {
        Contracts.plugin.playerManager.setup(event.getPlayer());
    }

    /**
     * Registers clicks in custom GUIs
     */
    @EventHandler
    public void b(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder != null && holder instanceof PluginInventory)
            ((PluginInventory) holder).whenClicked(event);
    }
}
