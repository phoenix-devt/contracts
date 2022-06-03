package fr.lezoo.contracts.player;

import fr.lezoo.contracts.Contracts;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData {
    UUID uuid;
    Player player;


    public static PlayerData get(Player player) {
        return Contracts.plugin.playerManager.get(player);
    }


    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }
}
