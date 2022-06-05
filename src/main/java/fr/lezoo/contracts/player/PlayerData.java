package fr.lezoo.contracts.player;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.api.ConfigFile;
import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.contract.ContractState;
import fr.lezoo.contracts.review.ContractReview;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {
    UUID uuid;
    Player player;
    boolean onChatInput;
    //We want to keep the order in which contracts where inserted
    private final Map<UUID, Contract> contracts = new HashMap<>();

    //All the reviews about this player
    private final Map<UUID, ContractReview> contractReviews = new LinkedHashMap<>();
    private int numberReviews;
    private double meanNotation;


    public PlayerData(UUID uuid) {
        this.uuid=uuid;
        loadFromConfig();
    }

    public PlayerData(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        loadFromConfig();
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

    public void addReview(ContractReview review) {
        int totalNotation=(int)meanNotation*numberReviews;
        contractReviews.put(review.getUuid(),review);
        numberReviews++;
        meanNotation=((double)(totalNotation+review.getNotation()))/((double)numberReviews);
    }


    public void loadFromConfig() {
        FileConfiguration config = new ConfigFile("userdata", uuid.toString()).getConfig();

        //We load the contracts
        for (String key : config.getStringList("contracts")) {
            Contract contract = Contracts.plugin.contractManager.get(UUID.fromString(key));
            contracts.put(contract.getUuid(), contract);
        }


        //We load the contracts reviews
        for (String key : config.getStringList("reviews")) {
            ContractReview review = Contracts.plugin.reviewManager.get(UUID.fromString(key));
            contractReviews.put(review.getUuid(), review);
        }
    }


    public List<ContractReview> getReviews() {
        return contractReviews.values().stream().sorted((review1, review2) -> (int) (review1.getReviewDate() - review2.getReviewDate())).collect(Collectors.toList());
    }

    /**
     * Gets all the contracts with the state matching the contractStates given in argument.
     */
    public List<Contract> getContracts(ContractState... states) {
        return contracts.values().stream().filter(contract -> Arrays.asList(states).contains(contract.getState())).sorted((contract1, contract2) -> (int) (contract1.isEnded() ? contract1.getEndTime() - contract2.getEndTime()
                : contract1.getCreationTime() - contract2.getCreationTime())).collect(Collectors.toList());
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
        List<String> reviews = contractReviews.keySet().stream().map(UUID::toString).collect(Collectors.toList());
        config.set("reviews", reviews);

        //Set the contracts
        config.set("contracts", contracts.keySet().stream().collect(Collectors.toList()));

    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }
}
