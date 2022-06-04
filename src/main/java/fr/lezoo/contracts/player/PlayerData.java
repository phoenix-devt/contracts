package fr.lezoo.contracts.player;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.api.ConfigFile;
import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.contract.ContractState;
import fr.lezoo.contracts.review.ContractReview;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {
    UUID uuid;
    Player player;
    boolean onChatInput;
    //We want to keep the order in which contracts where inserted
    private final Map<UUID, Contract> openContracts = new LinkedHashMap<>();
    private final Map<UUID, Contract> disputedContracts = new LinkedHashMap<>();
    //Closed contracts correspond to the resolved contracts and the fulfilled contracts
    private final Map<UUID, Contract> endedContracts = new LinkedHashMap();

    //All the reviews about this player
    private final Map<UUID, ContractReview> contractReviews = new LinkedHashMap<>();
    private int numberReviews, meanNotation;


    public PlayerData(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        loadFromConfig();
    }


    public void changeState(UUID contractId) {
        //Remove the old values of the contract and add the new ones
        if (openContracts.containsKey(contractId))
            openContracts.remove(contractId);
        if (disputedContracts.containsKey(contractId))
            disputedContracts.remove(contractId);
        if (endedContracts.containsKey(contractId))
            endedContracts.remove(contractId);

        Contract contract = Contracts.plugin.contractManager.get(contractId);
        if (contract.getState() == ContractState.OPEN)
            openContracts.put(uuid, contract);
        else if (contract.getState() == ContractState.DISPUTE)
            disputedContracts.put(uuid, contract);
        else
            endedContracts.put(uuid, contract);
    }

    public boolean isOnChatInput() {
        return onChatInput;
    }

    public void setOnChatInput(boolean onChatInput) {
        this.onChatInput = onChatInput;
    }

    public boolean canLeaveReview(Contract contract) {
        return (contract.isEnded() && (System.currentTimeMillis() - contract.getEndTime()) < Contracts.plugin.configManager.reviewPeriod);
    }


    public void loadFromConfig() {
        FileConfiguration config = new ConfigFile("userdata", uuid.toString()).getConfig();

        //We load the contracts
        for (String key : config.getStringList("contracts")) {
            Contract contract = Contracts.plugin.contractManager.get(UUID.fromString(key));
            switch (contract.getState()) {
                case OPEN: {
                    openContracts.put(UUID.fromString(key), contract);
                    break;
                }
                case DISPUTE: {
                    disputedContracts.put(UUID.fromString(key), contract);
                    break;
                }

                default: {
                    endedContracts.put(UUID.fromString(key), contract);
                    break;
                }

            }
        }


        //We load the contracts reviews
        for (String key : config.getStringList("reviews")) {
            ContractReview review = Contracts.plugin.reviewManager.get(UUID.fromString(key));
            contractReviews.put(review.getUuid(), review);
        }
    }

    public static boolean has(UUID uuid) {
        return Contracts.plugin.playerManager.has(uuid);
    }

    public static PlayerData get(Player player) {
        return Contracts.plugin.playerManager.get(player);
    }

    public static PlayerData get(UUID uuid) {
        return Contracts.plugin.playerManager.get(uuid);
    }

    public void updatePlayer(Player player) {
        this.player = player;
    }

    public void saveInConfig(FileConfiguration config) {
        //Set the reviews
        List<String> reviews = contractReviews.keySet().stream().map(UUID::toString).toList();
        config.set("reviews", reviews);


        //Set the contracts
        List<String> contracts = new ArrayList<>();
        for (UUID uuid : openContracts.keySet()) {
            contracts.add(uuid.toString());
        }

        for (UUID uuid : disputedContracts.keySet()) {
            contracts.add(uuid.toString());
        }

        for (UUID uuid : endedContracts.keySet()) {
            contracts.add(uuid.toString());
        }

        config.set("contracts", contracts);

    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }
}
