package fr.lezoo.contracts.manager;

import fr.lezoo.contracts.api.ConfigFile;
import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.contract.ContractState;
import fr.lezoo.contracts.contract.ContractType;
import fr.lezoo.contracts.contract.classic.ExchangeContract;
import fr.lezoo.contracts.contract.classic.KillContract;
import fr.lezoo.contracts.contract.permanent.LendingContract;
import fr.lezoo.contracts.contract.permanent.SalaryContract;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ContractManager implements FileManager {
    private final HashMap<UUID, Contract> contracts = new HashMap<>();
    private ConfigFile config;

    public List<Contract> getContractOfType(ContractType type) {

        return contracts.values().stream().filter(contract -> type.filter(contract)).collect(Collectors.toList());
    }

    public List<Contract> getContractOfState(ContractState state) {

        return contracts.values().stream().filter(contract ->contract.getState()==state).collect(Collectors.toList());
    }

    public void addContract(Contract contract) {
        contracts.put(contract.getUuid(), contract);
    }

    public Contract get(UUID contractId) {
        return contracts.get(contractId);
    }

    public void loadContract(String key) {
        ConfigurationSection section = config.getConfig().getConfigurationSection(key);
        ContractType type = ContractType.valueOf(section.getString("type"));
        Contract contract = null;
        switch (type) {
            case KILL:
                contract = new KillContract(section);
                break;
            case EXCHANGE:
                contract = new ExchangeContract(section);
                break;
            case LENDING:
                contract = new LendingContract(section);
                break;
            case SALARY:
                contract = new SalaryContract(section);
                break;
        }
        contracts.put(contract.getUuid(), contract);
    }

    @Override
    public void load() {
        config = new ConfigFile("contracts");
        for (String key : config.getConfig().getKeys(false)) {
            loadContract(key);
        }
    }

    @Override
    public void save(boolean clearBefore) {
        config = new ConfigFile("contracts");

        if (config == null)
            Bukkit.getLogger().log(Level.WARNING, "Config is null");
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
