package fr.phoenix.contracts.utils;

import fr.phoenix.contracts.Contracts;
import org.bukkit.ChatColor;

import java.util.Arrays;

public class ContractsUtils {
    public static String applyColorCode(String str) {
        return ChatColor.translateAlternateColorCodes(Contracts.plugin.configManager.colorCodeChar,str);
    }

    /**
     *Takes a time in millis and returns the hours that passed since that event
     */
    public static int timeSinceInHours(long time) {
        return (int)(System.currentTimeMillis()-time)/(1000*3600);
    }

    public static String ymlName(String str) {
        return str.toLowerCase().replace("_", "-").replace(" ", "-");
    }

    public static String enumName(String str) {
        return str.toUpperCase().replace("-", "_");
    }

    public static String chatName(String str) {
        StringBuilder result = new StringBuilder();
        Arrays.stream(str.split("_")).forEach(intermediary -> Arrays.stream(intermediary.split("-")).forEach(word->result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase())));
        return result.toString();
    }
}
