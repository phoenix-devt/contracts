package fr.phoenix.contracts.contract.review;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.UUID;

/**
 *
 */
public class ContractReview {
    private final long reviewDate;
    private final UUID reviewId, contractId, reviewed, reviewer;
    private final List<String> comment;

    private int notation;

    public ContractReview(ConfigurationSection section) {
        reviewDate = section.getLong("date");
        reviewId = UUID.fromString(section.getName());
        contractId = UUID.fromString(section.getString("contract-id"));
        reviewed = UUID.fromString(section.getString("reviewed"));
        reviewer = UUID.fromString(section.getString("reviewer"));
        notation = section.getInt("notation");
        comment = section.getStringList("comment");
    }

    public ContractReview(UUID reviewed, UUID reviewer, Contract contract, int notation, List<String> comment) {
        reviewDate = System.currentTimeMillis();
        reviewId = UUID.randomUUID();
        this.reviewed = reviewed;
        this.reviewer = reviewer;
        this.contractId = contract.getId();
        this.notation = notation;
        this.comment = comment;
    }

    public void saveInConfig(FileConfiguration config) {
        config.set(reviewId + ".date", reviewDate);
        config.set(reviewId + ".contract-id", contractId.toString());
        config.set(reviewId + ".reviewed", reviewed.toString());
        config.set(reviewId + ".reviewer", reviewer.toString());
        config.set(reviewId + ".notation", notation);
        config.set(reviewId + ".comment", comment);
    }

    public void removeComment() {
        comment.clear();
    }

    public void setNotation(int notation) {
        this.notation = notation;
    }


    public void addComment(String line) {
        comment.add(line);
    }

    public long getReviewDate() {
        return reviewDate;
    }

    public UUID getUuid() {
        return reviewId;
    }

    public UUID getReviewed() {
        return reviewed;
    }

    public UUID getReviewer() {
        return reviewer;
    }

    public Contract getContract() {
        return Contracts.plugin.contractManager.get(contractId);
    }

    public int getNotation() {
        return notation;
    }

    public List<String> getComment() {
        return comment;
    }
}
