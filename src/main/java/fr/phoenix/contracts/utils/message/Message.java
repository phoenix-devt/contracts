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

    EMPLOYEE_PROPOSAL_ACCEPTED("&aCongratulations your proposal was accepted for the contract &c{contract-name}&a."),

    PROPOSAL_RECEIVED("&aYou just received a proposal from &6{other}&a for the contract &c{contract-name}&a."),

    PROPOSAL_CREATED("&aYou just created a proposal for &c{contract-name}&a and paid &6{guarantee}&a in guarantee."),

    CONTRACT_DISPUTED("&6{who}&c called a dispute for the contract &6{contract-name}&c."),

    ADMIN_DISPUTED("&6{who}&c made an appeal called an admin dispute for the contract &6{contract-name}&c."),

    MIDDLEMAN_ADMIN_DISPUTED("&cThe contract &6{contract-name}&a with &6{employee}&c and is now under admin dispute."),

    EMPLOYEE_CONTRACT_ENDED("&f{employer}&a ended the contract &c{contract-name}&a, you received &6{amount}&a for it and your guarantee of &6{guarantee}&a has been refunded."),

    EMPLOYER_CONTRACT_ENDED("&aYou just ended the contract &c{contract-name}&a and paid &f{employee} &6{amount}&a for it."),

    EMPLOYER_CONTRACT_RESOLVED("&aThe contract &c{contract-name}&a is now resolved, you paid &f{employee} &6{amount}&a for it."),

    EMPLOYEE_CONTRACT_RESOLVED("&fThe contract &c{contract-name}&a is now resolved, you received &6{amount}&a for it and got back &6{guarantee}&a of guarantee."),

    MIDDLEMAN_RESOLVED("&fThe contract &c{contract-name}&a is now resolved, you received &6{commission}&a from commissions for it."),

    EMPLOYER_OFFERED_ACCEPTED("&aThe negotiation for &c{contract-name}&a is ended and you paid &f{employee} &6{amount}&a for it."),

    EMPLOYEE_OFFERED_ACCEPTED("&aThe negotiation for the contract &c{contract-name}&a is ended, you received &6{amount}&a for it and got back &6{guarantee}&a of guarantee."),

    OFFER_CREATED("&aCongratulations you just sent an offer to &6{other}&a for &c{contract-name}&a."),

    OFFER_RECEIVED("&aYou just received an offer from &6{other}&a for &c{contract-name}&a."),

    CONTRACT_FULFILLED("&aThe contract &c{contract-name}&a with &6{other}&a is now fulfilled!"),

    ARE_YOU_SURE_TO_ACCEPT("&eType 'yes' employer accept the contract &c{contract-name}&7."),

    NOT_ENOUGH_MONEY_CREATE("&cYou can't create this contract because you don't have {amount} on your balance."),

    GUARANTEE_REFUND("&aAnother proposal has been accepted for {contract-name}&a your guarantee of &6{guarantee}&a has been refunded."),

    NOT_ENOUGH_MONEY_PROPOSAL("&cYou can't make a proposal because you can't pay {guarantee} for the guarantee."),

    CREATED_CONTRACT("&aCongratulations you created the contract &c{contract-name}&a and paid &6{amount}&a for it."),

    SET_PARAMETER_ASK("&eEnter the value of &6{parameter-name}&e."),

    SEND_REVIEW("&aYou succesfully sent a review for &c{contract-name}&a."),

    RECEIVED_REVIEW("&aYou received a &6{notation}&a stars review for &c{contract-name}&a."),

    ALREADY_ON_CHAT_INPUT("&cYou are already on a chat input!"),

    NOT_VALID_INTEGER("&c{input} is not a valid integer."),

    NOT_VALID_DOUBLE("&c{input} is not a valid number."),

    NOT_VALID_PLAYER("&c{input} is not a valid player."),

    NOT_VALID_MATERIAL("&c{input} is not a valid player."),

    MISSING_CONTRACT_PARAMETER("&cYou can't create the contract yet, some parameters are missing."),

    SET_OFFER_ASK("&eWrite the amount of money &f{employer}&e will pay. It must be between &6{min}&e and &6{max}"),

    RESOLVE_DISPUTE_ASK("&eWrite the amount of money the employer will have to pay to the employee. It must be between &6{min}&e and &6{max}"),

    NOT_IN_LIMIT("&6{amount}&c is not between &6{min}&c and &6{max}&c. Write another number."),

    SET_NOTATION_INFO("&e(Click employer Change) Notation: {notation}"),

    SET_COMMENT_INFO("&e(Click employer Change) Comment: {comment}"),

    SET_NOTATION_ASK("&eWrite an integer between 0 and 5 for the notation."),

    SET_COMMENT_ASK("&eWrite the comment you want employer leave, send a blank message employer stop the redaction."),

    COMMENT_TOO_LONG("&cThe comment you tried employer post is too long, you must shorten it."),

    COMMENT_REQUIRED("&cYou need to provide a comment to send the review!"),

    NOT_VALID_NOTATION("&c{input} is not a valid notation, it must be an integer."),

    NOT_ENOUGH_MONEY_FOR_COMMISSION("&cYou don't have enough money to pay for commissions."),

    PAID_COMMISSION("&eYou paid &6{commission} in commission for the middleman."),

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