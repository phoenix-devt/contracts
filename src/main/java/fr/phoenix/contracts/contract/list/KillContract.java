package fr.phoenix.contracts.contract.list;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class KillContract extends Contract implements Listener {
    private UUID playerToKill;

    public KillContract(ConfigurationSection section) {
        super(ContractType.KILL, section);

        playerToKill = UUID.fromString(section.getString("player-to-kill"));
    }

    public KillContract(UUID employer) {
        super(ContractType.KILL, employer);

        //We register the new parameter to set
        addParameter("player-to-kill", (p, str) -> {
                    if (Contracts.plugin.playerManager.has(str)) {
                        playerToKill = Contracts.plugin.playerManager.get(str);
                        filledParameters.put("player-to-kill", str);
                    } else
                        Message.NOT_VALID_PLAYER.format("input", str).send(p);
                }
        );
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
        String str = contractId.toString();
        //Very important to set the type in the yml
        config.set(str + ".type", ContractType.KILL.toString());
        config.set(str + ".player-to-kill", playerToKill.toString());
    }
}
