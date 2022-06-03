package fr.lezoo.contracts.manager;

import fr.lezoo.contracts.player.PlayerData;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {
    private final HashMap<UUID, PlayerData> players= new HashMap<>();



    public PlayerData get(OfflinePlayer player) {
        return players.get(player.getUniqueId());
    }

}
