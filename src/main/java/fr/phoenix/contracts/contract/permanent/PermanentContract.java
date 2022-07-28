package fr.phoenix.contracts.contract.permanent;

import fr.phoenix.contracts.contract.Contract;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public abstract class PermanentContract extends Contract {


    public PermanentContract(ConfigurationSection section) {
        super(section);
    }

    public PermanentContract(UUID employer) {
        super(employer);
    }

}
