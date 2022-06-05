package fr.lezoo.contracts.contract.classic;

import fr.lezoo.contracts.contract.ContractState;
import fr.lezoo.contracts.contract.PaymentInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class KillContract extends ClassicContract {
    private final UUID playerToKill;


    public KillContract(ConfigurationSection section) {
        super(section);
        playerToKill = UUID.fromString(section.getString("player-to-kill"));
    }


    public KillContract(UUID employer, UUID playerToKill, PaymentInfo paymentInfo,String name) {
        super(employer, paymentInfo,name);
        this.playerToKill = playerToKill;
    }


    @EventHandler
    public void onKill(PlayerDeathEvent e) {
        LivingEntity killer = e.getEntity().getKiller();
        if (killer instanceof Player) {
            Player killingPlayer = (Player) killer;
            if (killingPlayer.equals(Bukkit.getPlayer(employee)) && e.getEntity().equals(Bukkit.getPlayer(playerToKill))) {
                changeContractState(ContractState.FULFILLED);
            }
        }
    }


    @Override
    public void save(FileConfiguration config) {
        super.save(config);
        String str = playerToKill.toString();
        config.set(str + ".player-to-kill", playerToKill);
    }
}
