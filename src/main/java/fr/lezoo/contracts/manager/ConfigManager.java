package fr.lezoo.contracts.manager;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.review.ContractReview;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigManager implements FileManager {
    public long reviewPeriod;
    public int maxCommentCharPerLine, maxCommentLines,defaultNotation;



    @Override
    public void load() {
        reviewPeriod = Contracts.plugin.getConfig().getLong("review-period") * 3600 * 1000;
        maxCommentLines = Contracts.plugin.getConfig().getInt("max-comment-lines");
        maxCommentCharPerLine = Contracts.plugin.getConfig().getInt("max-comment-char-per-line");
        defaultNotation=Contracts.plugin.getConfig().getInt("default-notation");
    }

    @Override
    public void save(boolean clearBefore) {
        //nothing
    }


    public void loadDefaultFile(String name) {
        loadDefaultFile("", name);
    }

    public void loadDefaultFile(String path, String name) {
        String newPath = path.isEmpty() ? "" : "/" + path;
        File folder = new File(Contracts.plugin.getDataFolder() + (newPath));
        if (!folder.exists()) folder.mkdir();

        File file = new File(Contracts.plugin.getDataFolder() + (newPath), name);
        if (!file.exists()) try {
            Files.copy(Contracts.plugin.getResource("default/" + (path.isEmpty() ? "" : path + "/") + name), file.getAbsoluteFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
