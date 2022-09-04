package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;

public class ConfigManager implements FileManager {

    public int reviewPeriod, callDisputePeriod, maxCommentCharPerLine, maxCommentLines,
            defaultNotation, maxContractsPerMiddleman, checkIfResolvedPeriod, middlemanCommission;
    public char colorCodeChar;
    public DecimalFormat decimalFormat;

    @Override
    public void load() {
        reviewPeriod = Contracts.plugin.getConfig().getInt("review-period");
        callDisputePeriod = Contracts.plugin.getConfig().getInt("call-dispute-period");
        checkIfResolvedPeriod = Contracts.plugin.getConfig().getInt("check-if-resolved-period");
        maxCommentLines = Contracts.plugin.getConfig().getInt("max-comment-lines");
        maxCommentCharPerLine = Contracts.plugin.getConfig().getInt("max-comment-char-per-line");
        defaultNotation = Contracts.plugin.getConfig().getInt("default-notation");
        colorCodeChar = Contracts.plugin.getConfig().getString("color-code-char").charAt(0);
        maxContractsPerMiddleman = Contracts.plugin.getConfig().getInt("max-contract-per-middleman");
        middlemanCommission = Contracts.plugin.getConfig().getInt("contract-commission");
        decimalFormat=new DecimalFormat(Contracts.plugin.getConfig().getString("decimal-format"));
        // Load default files
        loadDefaultFile("commands.yml");
    }

    @Override
    public void save(boolean clearBefore) {

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
