package fr.phoenix.contracts.contract.list;

import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class SalaryContract extends Contract {

    public SalaryContract(ConfigurationSection section) {
        super(ContractType.SALARY, section);
    }

    public SalaryContract(UUID employer) {
        super(ContractType.SALARY, employer);
    }

    @Override
    public void createContract() {

    }
}
