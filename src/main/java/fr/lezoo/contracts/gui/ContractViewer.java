package fr.lezoo.contracts.gui;

import fr.lezoo.contracts.command.ReviewCommand;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.review.ContractReview;
import fr.lezoo.contracts.utils.message.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ContractViewer {


    /**
     * Sends a clickable message to the player corresponding to the review he wants to post.
     */
    public static void displayChoices(PlayerData playerData, ContractReview review) {
        TextComponent textComponent = new TextComponent(Message.SET_NOTATION_INFO.format("notation",""+review.getNotation()).getAsString());
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/review "+ ReviewCommand.NOTATION_ASK+" "+review.getUuid().toString()));
        playerData.getPlayer().spigot().sendMessage(textComponent);


        StringBuilder comment= new StringBuilder();
        for(String str: review.getComment()) {
            comment.append("\n");
            comment.append(str);
        }
        textComponent = new TextComponent(Message.SET_NOTATION_INFO.format("comment",""+comment.toString()).getAsString());
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/review "+ ReviewCommand.COMMENT_ASK+" "+review.getUuid().toString()));
        playerData.getPlayer().spigot().sendMessage(textComponent);

    }
}
