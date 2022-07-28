package fr.phoenix.contracts.contract.classic;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class ExchangeContract extends ClassicContract{


    public ExchangeContract(ConfigurationSection section) {
        super(section);
    }

    public ExchangeContract(UUID employer) {
        super(employer);
    }

    @Override
    public void createContract() {

    }
}
