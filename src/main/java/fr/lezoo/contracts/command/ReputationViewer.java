package fr.lezoo.contracts.command;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.review.ContractReview;
import fr.lezoo.contracts.utils.InputHandler;
import fr.lezoo.contracts.utils.SimpleChatInput;
import fr.lezoo.contracts.utils.message.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;


public class ReputationViewer extends BukkitCommand {

    public static String NOTATION_ASK = "notationask";
    public static String COMMENT_ASK = "commentask";

    protected ReputationViewer(String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }


    @Override
    public boolean execute(CommandSender sender, String str, String[] args) {

        //Secret command for clickable message
        if (args.length != 2)
            return false;
        if (!(sender instanceof Player))
            return false;

        ContractReview review = null;
        try {
            //throws NullPointerException if null)
            review = Objects.requireNonNull(Contracts.plugin.reviewManager.get(UUID.fromString(args[1])));
        } catch (NullPointerException e) {
            Contracts.plugin.getLogger().log(Level.WARNING, "Can't find the review in review command.");
            return false;
        }

        Player player = (Player) sender;
        if (args[0].equals(NOTATION_ASK)) {
            Message.SET_NOTATION_ASK.format().send(player);
            new SimpleChatInput(PlayerData.get(player), review, InputHandler.SET_NOTATION);
            return true;
        }
        if (args[0].equals(COMMENT_ASK)) {
            //We remove the old comment and we replace it
            review.removeComment();
            Message.SET_COMMENT_ASK.format().send(player);
            new SimpleChatInput(PlayerData.get(player), review, InputHandler.SET_COMMENT);
            return true;
        }
        return false;

    }
}
