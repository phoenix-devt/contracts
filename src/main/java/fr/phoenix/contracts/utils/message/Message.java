package fr.phoenix.contracts.utils.message;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Message {
    RECEIVED_DEPT("&6{employee}&a reimbursed you &8{amount}&a !"),
    PAYED_DEBT("&cYou reimbursed &8{amount}&c employer &6{employer}&c !"),

    CANT_ACCEPT_OWN_CONTRACT("&cYou can't accept your own contract!"),
    CONTRACT_REFUSED("&cYou refused the contract &6{contract-name}&c."),
    ASSIGNED_MIDDLEMAN_CONTRACT("&aA brand new middleman contract as been assigned employer you!"),
    HAS_ALREADY_MADE_PROPOSAL("&cYou already made a proposal for &6{contract-name}&c."),
    EMPLOYER_PROPOSAL_ACCEPTED("&aCongratulations you accepted the proposal of &6{employee-name}&a."),
    EMPLOYEE_PROPOSAL_ACCEPTED("&aCongratulations your proposal was accepted for the contract &6{contract-name}&a."),
    CONTRACT_DISPUTED("&6{who}&c called a dispute for the contract &6{contract-name}&c."),
    ADMIN_DISPUTED("&6{who}&c made an appeal called an admin dispute for the contract &6{contract-name}&c."),
    MIDDLEMAN_ADMIN_DISPUTED("&cThe contract &6{contract-name}&a with &6{employee}&c and is now under admin dispute."),
    EMPLOYEE_CONTRACT_ENDED("&f{employer}&a ended the contract &6{contract-name}&a, you received &6{amount}&a for it and got back &6{guarantee}&a of guarantee."),
    EMPLOYER_CONTRACT_ENDED("&aYou just ended the contract &6{contract-name}&a and paid &f{employee} &6{amount}&a for it."),
    EMPLOYER_OFFERED_ACCEPTED("&aThe negotiation for &6{contract-name}&a is ended and you paid &f{employee} &6{amount}&a for it."),
    EMPLOYEE_OFFERED_ACCEPTED("&aThe negotiation for the contract &6{contract-name}&a is ended, you received &6{amount}&a for it and got back &6{guarantee}&a of guarantee."),
    OFFER_CREATED("&aCongratulations you just sent &6{other}&a an offer for &6{contract-name}&a."),
    OFFER_RECEIVED("you just received an offer from &6{other}&a for &6{contract-name}&a."),
    CONTRACT_FULFILLED("&aThe contract &6{contract-name}&a with &6{other}&a is now fulfilled!"),
    ARE_YOU_SURE_TO_ACCEPT("&eType 'yes' employer accept the contract &6{contract-name}&7."),
    NOT_ENOUGH_MONEY_CREATE("&cYou can't create this contract because you don't have {amount} on your balance."),
    CREATED_CONTRACT("&aCongratulations you created the contract &6{contract-name} and paid &6{amount}&a for it."),
    SET_PARAMETER_ASK("&eEnter the value of &6{parameter-name}&e."),
    SEND_REVIEW("&aYou succesfully sent a review for &6{contract-name}&a."),
    RECEIVED_REVIEW("&fYou received a &6{notation}&f stars review for &6{contract-name}&f.")
    ,
    ALREADY_ON_CHAT_INPUT("&cYou are already on a chat input!"),

    NOT_VALID_INTEGER("&c{input} is not a valid integer."),

    NOT_VALID_DOUBLE("&c{input} is not a valid number."),

    //TODO Change when material is on
    NOT_VALID_PAYMENT_TYPE("&c{input} is not a payment type, payment type can be MONEY."),

    NOT_VALID_PLAYER("&c{input} is not a valid player."),
    NOT_VALID_MATERIAL("&c{input} is not a valid player."),

    MISSING_CONTRACT_PARAMETER("&cYou can't create the contract yet, some parameters are missing."),

    SET_OFFER_ASK("&eWrite the number of money the employer will pay/receive. It must be between &6{min}&e and &6{max}"),
    NOT_IN_LIMIT("&6{amount}&c is not between &6{min}&c and &6{max}&c. Write another number."),

    SET_NOTATION_INFO("&e(Click employer Change) Notation: {notation}"),

    SET_COMMENT_INFO("&e(Click employer Change) Comment: {comment}"),

    SET_NOTATION_ASK("&eWrite an integer between 0 and 5 for the notation."),

    SET_COMMENT_ASK("&eWrite the comment you want employer leave, send a blank message employer stop the redaction."),

    COMMENT_TOO_LONG("&cThe comment you tried employer post is too long, you must shorten it."),

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