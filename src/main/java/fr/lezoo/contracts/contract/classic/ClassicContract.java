package fr.lezoo.contracts.contract.classic;

import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.contract.PaymentInfo;
import fr.lezoo.contracts.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public abstract class ClassicContract extends Contract implements Listener {


    public ClassicContract(ConfigurationSection section){
        super(section);
    }

    public ClassicContract(UUID employer, UUID employee, PaymentInfo paiementInfo) {
        super(employer, employee, paiementInfo);
    }





}
