package fr.phoenix.contracts;

import fr.phoenix.contracts.command.ContractTreeRoot;
import fr.phoenix.contracts.compat.Metrics;
import fr.phoenix.contracts.compat.placeholder.DefaultPlaceholderParser;
import fr.phoenix.contracts.compat.placeholder.PlaceholderParser;
import fr.phoenix.contracts.listener.PlayerListener;
import fr.phoenix.contracts.manager.*;
import fr.phoenix.contracts.utils.ConfigFile;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class Contracts extends JavaPlugin {
    public static Contracts plugin;

    public Economy economy;

    public final ConfigManager configManager = new ConfigManager();
    public final PlayerDataManager playerManager = new PlayerDataManager();
    public final ContractManager contractManager = new ContractManager();
    public final ReviewManager reviewManager = new ReviewManager();
    public final PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
    public final MiddlemenManager middlemenManager = new MiddlemenManager();

    @Override
    public void onEnable() {

        // Metrics data
        new Metrics(this, 15383);

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Register eco
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null)
            economy = provider.getProvider();
        else {
            getLogger().log(Level.SEVERE, "Could not hook onto Vault, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // Register the root command
        getCommand("contract").setExecutor(new ContractTreeRoot("contract", ""));

        // Load managers (the order is important: player must be loaded at the end)
        configManager.load();
        contractManager.load();
        reviewManager.load();
        InventoryManager.load();
        playerManager.load();

    }

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onDisable() {

        // Save the managers
        contractManager.save(true);
        playerManager.save(true);
        reviewManager.save(true);
    }

    public static void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    public static void log(String message) {
        plugin.getLogger().log(Level.WARNING, message);
    }

}
