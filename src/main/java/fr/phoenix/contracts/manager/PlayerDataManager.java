package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.api.ConfigFile;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager implements FileManager {
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final Map<String, UUID> playerNames = new HashMap<>();


    public PlayerData get(OfflinePlayer player) {
        return players.get(player.getUniqueId());
    }

    public PlayerData get(UUID uuid) {
        return players.get(uuid);
    }

    public UUID get(String str) {
        return playerNames.get(str);
    }


    public boolean has(String name) {
        return playerNames.containsKey(name);
    }

    public boolean has(UUID uuid) {
        return players.containsKey(uuid);
    }


    @Override
    public void load() {
        // Load player data of online players
        Bukkit.getOnlinePlayers().forEach(player -> players.put(player.getUniqueId(), new PlayerData(player)));
        Bukkit.getOnlinePlayers().forEach(player -> playerNames.put(player.getName(), player.getUniqueId()));
    }


    /**
     * Used to load a player that is offline if needed
     *
     * @param uuid
     */
    public void setup(UUID uuid) {
        if (!players.containsKey(uuid))
            players.put(uuid, new PlayerData(uuid));
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if (!playerNames.containsKey(name))
            playerNames.put(name, uuid);

    }

    /**
     * Called when a player logs on the server
     */
    public void setup(Player player) {

        if (!playerNames.containsKey(player.getName()))
            playerNames.put(player.getName(), player.getUniqueId());

        if (players.containsKey(player.getUniqueId()))
            players.get(player.getUniqueId()).updatePlayer(player);
        else
            players.put(player.getUniqueId(), new PlayerData(player));
    }

    @Override
    public void save(boolean clearBefore) {

        // Save player data
        for (PlayerData player : players.values()) {
            ConfigFile config = new ConfigFile("/userdata", player.getUuid().toString());
            if (clearBefore) {
                for (String key : config.getConfig().getKeys(true))
                    config.getConfig().set(key, null);
            }

            player.saveInConfig(config.getConfig());
            config.save();
        }
    }
}
