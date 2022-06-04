package fr.lezoo.contracts.utils.message;

import org.apache.commons.lang.Validate;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Message {
    SET_NOTATION_INFO("&e(Click to Change) Notation: {notation}"),
    SET_COMMENT_INFO("&e(Click to Change) Comment: {comment}"),
    SET_NOTATION_ASK("&eWrite an integer between 1 and 5 for the notation."),
    SET_COMMENT_ASK("&eWrite the comment you want to leave, send a blank message to stop the redaction."),
    COMMENT_TOO_LONG("&cThe comment you tried to post is too long, you must shorten it."),
    NOT_VALID_NOTATION("&c{input} is not a valid notation, it must be an integer."),
    NOT_ENOUGH_PERMISSIONS("&cYou don't have enough permissions."),
    ;

    private List<String> message;
    private SoundReader sound;

    private Message(String... message) {
        this(null, message);
    }

    private Message(SoundReader sound, String... message) {
        this.message = Arrays.asList(message);
        this.sound = sound;
    }

    public String getPath() {
        return name().toLowerCase().replace("_", "-");
    }

    /**
     * Deep Copy !!
     *
     * @return Message updated based on what's in the config files
     */
    public List<String> getCached() {
        return new ArrayList<>(message);
    }

    public SoundReader getSound() {
        return sound;
    }

    public boolean hasSound() {
        return sound != null;
    }

    public PlayerMessage format(Object... placeholders) {
        return new PlayerMessage(this).format(placeholders);
    }

    public void update(ConfigurationSection config) {
        List<String> format = config.getStringList("format");
        Validate.notNull(this.message = format, "Could not read message format");
        sound = config.contains("sound") ? new SoundReader(config.getConfigurationSection("sound")) : null;
    }
}