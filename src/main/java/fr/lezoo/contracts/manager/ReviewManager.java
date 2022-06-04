package fr.lezoo.contracts.manager;

import fr.lezoo.contracts.api.ConfigFile;
import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.review.ContractReview;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;
import java.util.UUID;

public class ReviewManager implements FileManager {
    private final HashMap<UUID, ContractReview> reviews = new HashMap<>();
    private final ConfigFile config = new ConfigFile("review");



    public ContractReview get(UUID reviewId) {
        return reviews.get(reviewId);
    }


    @Override
    public void load() {
        for (String key : config.getConfig().getKeys(false)) {
            reviews.put(UUID.fromString(key), new ContractReview(config.getConfig().getConfigurationSection(key)));
        }
    }

    @Override
    public void save(boolean clearBefore) {
        if (clearBefore) {
            for (String key : config.getConfig().getKeys(true))
                config.getConfig().set(key, null);
        }


        config.save();


    }


}
