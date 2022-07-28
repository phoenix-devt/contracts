package fr.phoenix.contracts.contract.permanent;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class SalaryContract extends PermanentContract {

    public SalaryContract(ConfigurationSection section) {
        super(section);
    }

    public SalaryContract(UUID employer) {
        super(employer);
    }

    @Override
    public void createContract() {

    }
}
