package fr.lezoo.contracts.contract.permanent;

import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.contract.PaymentInfo;
import fr.lezoo.contracts.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class PermanentContract extends Contract {


    public PermanentContract(ConfigurationSection section) {
        super(section);
    }

    public PermanentContract(UUID employer) {
        super(employer);
    }

}
