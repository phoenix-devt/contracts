package fr.lezoo.contracts.manager;

import fr.lezoo.contracts.api.ConfigFile;
import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.contract.ContractState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;
import java.util.UUID;

public class ContractManager implements FileManager {
    private final HashMap<UUID, Contract> contracts = new HashMap<>();
    private final ConfigFile config = new ConfigFile("contracts");


    public Contract get(UUID contractId) {
        return contracts.get(contractId);
    }

    public void loadContract(String key) {
        ConfigurationSection section = config.getConfig().getConfigurationSection(key);
        Contract contract = null;
        switch (key) {
        }
        contracts.put(contract.getUuid(), contract);
    }

    @Override
    public void load() {
        for (String key : config.getConfig().getKeys(false)) {
            loadContract(key);
        }
    }

    @Override
    public void save(boolean clearBefore) {
        //We remove the values before
        if (clearBefore) {
            for (String key : config.getConfig().getKeys(true))
                config.getConfig().set(key, null);
        }


        for (Contract contract : contracts.values()) {
            contract.save(config.getConfig());
        }
        config.save();

    }


}
