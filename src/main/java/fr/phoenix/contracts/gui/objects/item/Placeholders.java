package fr.phoenix.contracts.gui.objects.item;

import fr.phoenix.contracts.Contracts;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Util class to register all placeholders which must
 * be applied to an item lore, in a custom GUI.
 *
 * @author jules
 */
public class Placeholders {
    private final Map<String, String> placeholders = new HashMap<>();

    public void register(String path, Object obj) {
        placeholders.put(path, obj.toString());
    }

    /**
     * @param player Player to parse placeholders from
     * @param str    String input
     * @return String with parsed placeholders and color codes
     */
    public String apply(Player player, String str) {

        /*
         * Remove potential conditions, apply color
         * codes and external placeholders if needed.
         */
        str = Contracts.plugin.placeholderParser.parse(player, removeCondition(str));

        // Apply internal placeholders
        while (str.contains("{") && str.substring(str.indexOf("{")).contains("}")) {
            String holder = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
            str = str.replace("{" + holder + "}", placeholders.getOrDefault(holder, "PHE"));
        }
        return str;
    }

    /**

     * @param str    String input
     * @return String with parsed placeholders only for internal placeholders
     */
    public String apply(String str) {


        str=ChatColor.translateAlternateColorCodes(Contracts.plugin.configManager.colorCodeChar,str);
        // Apply internal placeholders
        while (str.contains("{") && str.substring(str.indexOf("{")).contains("}")) {
            String holder = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
            str = str.replace("{" + holder + "}", placeholders.getOrDefault(holder, "PHE"));
        }
        return str;
    }

    private String removeCondition(String str) {
        return str.startsWith("{") && str.contains("}") ? str.substring(str.indexOf("}") + 1) : str;
    }
}
