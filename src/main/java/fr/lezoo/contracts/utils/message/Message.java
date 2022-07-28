package fr.lezoo.contracts.utils.message;

import org.apache.commons.lang.Validate;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Message {
    RECEIVED_DEPT("&6{from}&a reimbursed you &8{amount}&a !"),
    PAYED_DEBT("&cYou reimbursed &8{amount}&c to &6{to}&c !"),

    CANT_ACCEPT_OWN_CONTRACT("&cYou can't accept your own contract!"),
    CONTRACT_REFUSED("&cYou refused the contract &6{contract-name}&c."),

    EMPLOYER_CONTRACT_ACCEPTED("&aCongratulations your contract &6{contract-name} was just accepted by &6{player-name}&a."),

    EMPLOYEE_CONTRACT_ACCEPTED("&aCongratulations you accepted the contract &6{contract-name}&a."),
    CONTRACT_DISPUTED("&cThe contract &6{contract-name}&a with &6{other}&c is now under dispute."),
    CONTRACT_FULFILLED("&aThe contract &6{contract-name}&a with &6{other}&a is now fulfilled!"),
    ARE_YOU_SURE_TO_ACCEPT("&eType 'yes' to accept the contract &6{contract-name}&7."),

    CREATED_CONTRACT("&aCongratulations you created the contract &6{contract-name}&a."),

    SET_PARAMETER_ASK("&eEnter the value of &6{parameter-name}&e."),

    ALREADY_ON_CHAT_INPUT("&cYou are already on a chat input!"),

    NOT_VALID_INTEGER("&c{input} is not a valid integer."),

    NOT_VALID_DOUBLE("&c{input} is not a valid number."),

    //TODO Change when material is on
    NOT_VALID_PAYMENT_TYPE("&c{input} is not a payment type, payment type can be MONEY."),

    NOT_VALID_PLAYER("&c{input} is not a valid player."),
    NOT_VALID_MATERIAL("&c{input} is not a valid player."),

    MISSING_CONTRACT_PARAMETER("&cYou can't create the contract yet, some parameters are missing."),


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