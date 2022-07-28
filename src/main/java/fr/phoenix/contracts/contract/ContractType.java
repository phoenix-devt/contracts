package fr.phoenix.contracts.contract;

import fr.phoenix.contracts.contract.classic.ExchangeContract;
import fr.phoenix.contracts.contract.classic.KillContract;
import fr.phoenix.contracts.contract.permanent.LendingContract;
import fr.phoenix.contracts.contract.permanent.SalaryContract;

import java.util.UUID;
import java.util.function.Function;

public enum ContractType {
    KILL((uuid)->new KillContract(uuid),(c)->c instanceof KillContract),
    EXCHANGE((uuid)->new ExchangeContract(uuid),(c)->c instanceof ExchangeContract),
    SALARY((uuid)->new SalaryContract(uuid),(c)->c instanceof SalaryContract),
    LENDING((uuid)->new LendingContract(uuid),(c)->c instanceof LendingContract);

    private final Function<UUID,? extends Contract> provider;
    private final Function<Contract,Boolean> filter;

    ContractType(Function<UUID,Contract> provider,Function<Contract,Boolean> filter) {
        this.provider = provider;
        this.filter=filter;
    }

    public Contract provide(UUID uuid) {
        return provider.apply(uuid);
    }

    public boolean filter(Contract contract) {
        return filter.apply(contract);
    }

}
