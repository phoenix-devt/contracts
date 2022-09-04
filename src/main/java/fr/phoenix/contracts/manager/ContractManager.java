package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.utils.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ContractManager implements FileManager {
    private final Map<UUID, Contract> contracts = new HashMap<>();

    public ContractManager() {
    }


    public List<Contract> getContractsOfType(ContractType type) {
        return contracts.values().stream().filter(contract -> type == contract.getType()).collect(Collectors.toList());
    }

    public List<Contract> getContractsOfState(ContractState state) {
        return contracts.values().stream().filter(contract -> contract.getState() == state).collect(Collectors.toList());
    }


    public void registerContract(Contract contract) {
        contracts.put(contract.getId(), contract);
    }

    public Contract get(UUID contractId) {
        return contracts.get(contractId);
    }

    @Override
    public void load() {
        FileConfiguration config = new ConfigFile("contracts").getConfig();
        for (String key : config.getKeys(false))
            try {
                final ContractType type = ContractType.valueOf(config.getString(key + ".type"));
                registerContract(type.loadFromConfig(config.getConfigurationSection(key)));
            } catch (RuntimeException exception) {
                Contracts.plugin.getLogger().log(Level.WARNING, "Could not load contract '" + key + "': " + exception.getMessage());
            }

        new BukkitRunnable() {
            @Override
            public void run() {
                contracts.values().stream()
                        .filter(contract -> contract.getState() == ContractState.FULFILLED || contract.getState() == ContractState.MIDDLEMAN_RESOLVED)
                        .filter(contract -> (contract.getEnteringTime(contract.getState()) - System.currentTimeMillis()) < 1000 * 3600 * 24)
                        .forEach(contract -> {
                            if (contract.getState() == ContractState.FULFILLED)
                                contract.whenResolved();
                            else
                                contract.whenResolvedFromDispute();
                        });
            }
        }.runTaskTimer(Contracts.plugin, 0L, Contracts.plugin.configManager.checkIfResolvedPeriod * 1000 * 3600);
    }

    @Override
    public void save(boolean clearBefore) {
        ConfigFile config = new ConfigFile("contracts");

        // We remove the values before
        if (clearBefore)
            for (String key : config.getConfig().getKeys(true))
                config.getConfig().set(key, null);

        // Save newest contracts
        for (Contract contract : contracts.values())
            contract.save(config.getConfig());

        config.save();
    }
}
