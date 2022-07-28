package fr.lezoo.contracts;

import fr.lezoo.contracts.api.ConfigFile;
import fr.lezoo.contracts.command.ContractTreeRoot;
import fr.lezoo.contracts.command.ReputationViewerCommand;
import fr.lezoo.contracts.compat.Metrics;
import fr.lezoo.contracts.compat.placeholder.DefaultPlaceholderParser;
import fr.lezoo.contracts.compat.placeholder.PlaceholderParser;
import fr.lezoo.contracts.listener.PlayerListener;
import fr.lezoo.contracts.manager.*;
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

    public final DebtManager debtManager = new DebtManager();

    @Override
    public void onEnable() {
        // Metrics data
        new Metrics(this, 15383);

        //Save default config if it doesn't exist
        saveDefaultConfig();
        //Register eco
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null)
            economy = provider.getProvider();
        else {
            getLogger().log(Level.SEVERE, "Could not hook onto Vault, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        //Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);


        //We register the root command
        getCommand("contract").setExecutor(new ContractTreeRoot("contract", ""));

        //Save the commands
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            // We then register the command indivually
            FileConfiguration config = new ConfigFile("command").getConfig();

            commandMap.register("reputation-viewer", new ReputationViewerCommand(config.getConfigurationSection("reputation-viewer")));

        } catch (Exception e) {
            e.printStackTrace();
        }


        //Load manager (the order is important: player must be loaded at the end)
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


        //Save the managers
        contractManager.save(true);
        playerManager.save(true);
        reviewManager.save(true);
    }


    public static void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

}
