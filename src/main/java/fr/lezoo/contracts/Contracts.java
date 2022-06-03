package fr.lezoo.contracts;

import fr.lezoo.contracts.compat.placeholder.DefaultPlaceholderParser;
import fr.lezoo.contracts.compat.placeholder.PlaceholderParser;
import fr.lezoo.contracts.manager.PlayerManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class Contracts extends JavaPlugin {
    public static Contracts plugin;

    public Economy economy;
    public PlayerManager playerManager = new PlayerManager();
    public PlaceholderParser placeholderParser = new DefaultPlaceholderParser();

    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("ENABLED");


        //Register eco
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null)
            economy = provider.getProvider();
        else {
            getLogger().log(Level.SEVERE, "Could not hook onto Vault, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        //Save the commands
        Field field = null;
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            // We then register the command indivually


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onDisable() {

    }
}
