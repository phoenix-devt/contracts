package fr.phoenix.contracts.utils;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;

public class ContractsUtils {
    public static String applyColorCode(String str) {
        return ChatColor.translateAlternateColorCodes(Contracts.plugin.configManager.colorCodeChar, str);
    }

    /**
     * Takes a time in millis and returns the hours that passed since that event
     */
    public static int timeSinceInHours(long time) {
        return (int) (System.currentTimeMillis() - time) / (1000 * 3600);
    }

    public static String formatTime(long time) {
        if ((System.currentTimeMillis() - time) / (1000 * 3600 * 24) > 1) {
            //{day} day, {hour} hours format.
            int day = (int) ((System.currentTimeMillis() - time) / (1000 * 3600 * 24));
            int hour = (int) ((System.currentTimeMillis() - time) / (1000 * 3600) - 24 * day);
            return day + "d, " + hour + "h";
        } else {
            //{hours} hours, {minutes} minutes format
            //{day} day, {hour} hours format.
            int hour = (int) ((System.currentTimeMillis() - time) / (1000 * 3600));
            int minute = (int) ((System.currentTimeMillis() - time) / (1000 * 60) - 60 * hour);
            return hour + "h, " + minute + "min";
        }
    }


    public static String ymlName(String str) {
        return str.toLowerCase().replace("_", "-").replace(" ", "-");
    }

    public static String enumName(String str) {
        return str.toUpperCase().replace("-", "_");
    }

    public static Placeholders getContractPlaceholder(Contract contract) {
        Placeholders holders = new Placeholders();
        holders.register("name", contract.getName());
        holders.register("state", chatName(contract.getState().toString()));
        holders.register("type", chatName(contract.getType().toString()));
        holders.register("employee", contract.getEmployee() != null ? contract.getEmployeeName() : "");
        holders.register("employer", contract.getEmployerName());
        holders.register("payment", contract.getAmount());
        holders.register("guarantee", contract.getGuarantee());
        for (ContractState state : ContractState.values()) {
            holders.register(ContractsUtils.ymlName(state.toString()) + "-for",
                    contract.hasBeenIn(state) ? ContractsUtils.formatTime(contract.getEnteringTime(state)) : "Not been to this state");
        }
        return holders;
    }

    public static String chatName(String str) {
        StringBuilder result = new StringBuilder();
        Arrays.stream(str.split("_")).forEach(intermediary -> Arrays.stream(intermediary.split("-")).forEach(word -> result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase())));
        return result.toString();
    }
}
