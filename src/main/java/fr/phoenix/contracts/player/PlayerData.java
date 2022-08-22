package fr.phoenix.contracts.player;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.utils.ConfigFile;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.review.ContractReview;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {
    private final UUID uuid;
    private final String playerName;
    private Player player;
    private boolean onChatInput;

    /**
     * Keep the order in which contracts where inserted
     */
    private final Map<UUID, Contract> contracts = new LinkedHashMap<>();


    private boolean isMiddleman;

    /**
     * Used only for the middleman, not for the others
     */
    private final Map<UUID,Contract> middlemanContracts= new HashMap<>();

    private final Map<UUID, Double> debts = new HashMap<>();

    /**
     * All the reviews about the player
     */
    private final Map<UUID, ContractReview> contractReviews = new LinkedHashMap<>();

    private int numberReviews;
    private double meanNotation;


    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        playerName = Bukkit.getOfflinePlayer(uuid).getName();
        loadFromConfig();
    }

    public PlayerData(Player player) {
        playerName = player.getName();
        this.player = player;
        this.uuid = player.getUniqueId();
        loadFromConfig();
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isOnChatInput() {
        return onChatInput;
    }

    public void setOnChatInput(boolean onChatInput) {
        this.onChatInput = onChatInput;
    }

    public boolean canLeaveReview(Contract contract) {
        return (contract.hasBeenIn(ContractState.RESOLVED) && (System.currentTimeMillis() - contract.getEnteringTime(ContractState.RESOLVED)) < Contracts.plugin.configManager.reviewPeriod);
    }

    public void addContract(Contract contract) {
        contracts.put(contract.getId(), contract);
    }

    public void addReview(ContractReview review) {
        int totalNotation = (int) meanNotation * numberReviews;
        contractReviews.put(review.getUuid(), review);
        numberReviews++;
        meanNotation = ((double) (totalNotation + review.getNotation())) / ((double) numberReviews);
    }

    public int getNumberReviews() {
        return numberReviews;
    }

    public double getMeanNotation() {
        return meanNotation;
    }

    /**
     * When a middleman is assigned a contract.
     */
    public void assignMiddlemanContract(Contract contract) {
        Message.ASSIGNED_MIDDLEMAN_CONTRACT.format().send(player);
        middlemanContracts.put(contract.getId(),contract);
    }


    public void loadFromConfig() {
        FileConfiguration config = new ConfigFile("/userdata", uuid.toString()).getConfig();
        isMiddleman=config.getBoolean("is-middlemen");

        //We load the contracts
        for (String key : config.getStringList("contracts")) {
            Contract contract = Contracts.plugin.contractManager.get(UUID.fromString(key));
            contracts.put(contract.getId(), contract);
        }


        //We load the middleman contracts
        for (String key : config.getStringList("middleman-contracts")) {
            Contract contract = Contracts.plugin.contractManager.get(UUID.fromString(key));
            middlemanContracts.put(contract.getId(), contract);
        }


        //We load the contracts reviews
        for (String key : config.getStringList("reviews")) {
            ContractReview review = Contracts.plugin.reviewManager.get(UUID.fromString(key));
            contractReviews.put(review.getUuid(), review);
        }
    }

    /**
     * @return The number of middle man contracts that are not resolved.
     */
    public int getNumberOpenedMiddlemanContracts() {
        return middlemanContracts.values().stream().filter(contract -> contract.getState()!=ContractState.RESOLVED).toList().size();
    }

    public boolean isMiddleman() {
        return isMiddleman;
    }

    public void setMiddleman(boolean isMiddleman) {
        this.isMiddleman=isMiddleman;
    }

    public Map<UUID, Contract> getMiddlemanContracts() {
        return middlemanContracts;
    }


    /**
     * Gets all the middleman contracts with the state matching the contractStates given in argument.
     */
    public List<Contract> getMiddlemanContracts(ContractState... states) {
        return middlemanContracts.values()
                .stream()
                .filter(contract -> Arrays.asList(states).contains(contract.getState()))
                .sorted((contract1, contract2) -> (int)(contract1.getLastStateChange()-contract2.getLastStateChange()))
                .collect(Collectors.toList());
    }


    public List<ContractReview> getReviews() {
        return contractReviews.values().stream().sorted((review1, review2) -> (int) (review1.getReviewDate() - review2.getReviewDate())).collect(Collectors.toList());
    }

    /**
     * Gets all the contracts with the state matching the contractStates given in argument.
     */
    public List<Contract> getContracts(ContractState... states) {
        return contracts.values()
                .stream()
                .filter(contract -> Arrays.asList(states).contains(contract.getState()))
                .sorted((contract1, contract2) -> (int)(contract1.getLastStateChange()-contract2.getLastStateChange()))
                .collect(Collectors.toList());
    }

    public static boolean has(UUID uuid) {
        return Contracts.plugin.playerManager.has(uuid);
    }



    public static PlayerData getOrLoad(Player player) {
        return Contracts.plugin.playerManager.getOrLoad(player);
    }

    public static PlayerData getOrLoad(UUID uuid) {
        return Contracts.plugin.playerManager.getOrLoad(uuid);
    }

    public static PlayerData getOrLoad(String name) {
        return Contracts.plugin.playerManager.getOrLoad(Bukkit.getOfflinePlayer(name));
    }

    public void updatePlayer(Player player) {
        this.player = player;
    }

    public void saveInConfig(FileConfiguration config) {
        //Set the reviews
        List<String> reviews = contractReviews.keySet().stream().map(UUID::toString).collect(Collectors.toList());
        config.set("reviews", reviews);

        //Set the contracts
        List<String> list = contracts.keySet().stream().map(UUID::toString).collect(Collectors.toList());
        config.set("contracts", list);

        //Set the middleman contracts
        List<String> middlemanList = contracts.keySet().stream().map(UUID::toString).collect(Collectors.toList());
        config.set("contracts", middlemanList);

    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }
}
