package fr.lezoo.contracts.review;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 *
 */
public class ContractReview {

    private final UUID reviewId, contractId, reviewed;
    private int notation;
    private List<String> comment;


    public ContractReview(ConfigurationSection section) {
        reviewId=UUID.fromString(section.getName());
        contractId = UUID.fromString(section.getString("contract-id"));
        reviewed = UUID.fromString(section.getString("reviewed"));
        notation=section.getInt("notation");
        comment=section.getStringList("comment");

    }

    public ContractReview(UUID reviewed, Contract contract, int notation, List<String> comment) {
        reviewId=UUID.randomUUID();
        this.reviewed = reviewed;
        this.contractId = contract.getUuid();
        this.notation = notation;
        this.comment = comment;
    }


    public void removeComment() {
        comment=new ArrayList<>();
    }

    public void setNotation(int notation) {
        this.notation = notation;
    }

    public void addComment(String line) {
        comment.add(line);
    }

    public UUID getUuid() {
        return reviewId;
    }

    public OfflinePlayer getReviewed() {
        return Bukkit.getOfflinePlayer(reviewed);
    }

    public Contract getContract() {
        return Contracts.plugin.contractManager.get(contractId);
    }

    public double getNotation() {
        return notation;
    }

    public List<String> getComment() {
        return comment;
    }
}
