package fr.lezoo.contracts.contract.classic;

import fr.lezoo.contracts.contract.Contract;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.UUID;

public abstract class ClassicContract extends Contract implements Listener {


    public ClassicContract(ConfigurationSection section){
        super(section);
    }

    public ClassicContract(UUID employer) {
        super(employer);
    }





}
