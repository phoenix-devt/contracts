package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.utils.ConfigFile;
import fr.phoenix.contracts.contract.review.ContractReview;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class ReviewManager implements FileManager {
    private final HashMap<UUID, ContractReview> reviews = new HashMap<>();
    private ConfigFile config;



    public ContractReview get(UUID reviewId) {
        return reviews.get(reviewId);
    }


    @Override
    public void load() {
        config= new ConfigFile("review");
        for (String key : config.getConfig().getKeys(false)) {
            Contracts.log(Level.SEVERE,key+".....................................");
            reviews.put(UUID.fromString(key), new ContractReview(config.getConfig().getConfigurationSection(key)));
        }
    }

    public void register(ContractReview review) {
        reviews.put(review.getUuid(),review);
    }

    @Override
    public void save(boolean clearBefore) {
        config= new ConfigFile("review");
        if (clearBefore) {
            for (String key : config.getConfig().getKeys(true))
                config.getConfig().set(key, null);
        }
        for(ContractReview review:reviews.values())
            review.saveInConfig(config.getConfig());

        config.save();


    }


}
