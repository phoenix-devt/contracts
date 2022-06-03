package fr.lezoo.contracts.manager;

import fr.lezoo.contracts.Contracts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigManager {




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
