package fr.phoenix.contracts.contract;

import fr.phoenix.contracts.contract.list.ExchangeContract;
import fr.phoenix.contracts.contract.list.KillContract;
import fr.phoenix.contracts.contract.list.LendingContract;
import fr.phoenix.contracts.contract.list.SalaryContract;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;
import java.util.function.Function;

public enum ContractType {
    KILL(KillContract::new, KillContract::new),
    EXCHANGE(ExchangeContract::new, ExchangeContract::new),
    SALARY(SalaryContract::new, SalaryContract::new),
    LENDING(LendingContract::new, LendingContract::new);

    private final Function<UUID, Contract> initializer;
    private final Function<ConfigurationSection, Contract> loader;

    ContractType(Function<UUID, Contract> initializer, Function<ConfigurationSection, Contract> loader) {
        this.initializer = initializer;
        this.loader = loader;
    }

    public Contract instanciate(UUID uuid) {
        return initializer.apply(uuid);
    }

    public Contract loadFromConfig(ConfigurationSection config) {
        return loader.apply(config);
    }
}
