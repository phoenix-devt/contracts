package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.utils.ConfigFile;
import fr.phoenix.contracts.contract.review.ContractReview;

import java.util.HashMap;
import java.util.UUID;

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
            reviews.put(UUID.fromString(key), new ContractReview(config.getConfig().getConfigurationSection(key)));
        }
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
