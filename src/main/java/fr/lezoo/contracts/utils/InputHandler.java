package fr.lezoo.contracts.utils;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.gui.ContractViewer;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.review.ContractReview;
import fr.lezoo.contracts.utils.message.Message;

public class InputHandler {

    public static final TriFunction<PlayerData, String, ContractReview, Boolean> SET_NOTATION = (playerData, str, review) -> {
        try {
            int value=Integer.parseInt(str);
            review.setNotation(value);
        }
        catch (Exception e) {
            Message.NOT_VALID_NOTATION.format("input",str).send(playerData.getPlayer());
        }
        ContractViewer.displayChoices(playerData,review);
        return true;
    };

    public static final TriFunction<PlayerData, String, ContractReview, Boolean> SET_COMMENT = (playerData, str, review) -> {
        if(str.equals("")) {
            ContractViewer.displayChoices(playerData,review);
            return true;
        }
        int lines=(str.length()/Contracts.plugin.configManager.maxCommentCharPerLine)+1;
        if(review.getComment().size()+lines>=Contracts.plugin.configManager.maxCommentLines) {
            Message.COMMENT_TOO_LONG.format().send(playerData.getPlayer());
            return false;
        }

        //The comment size fits with the config parameters
        for(int i=0;i<lines-1;i++) {
            review.addComment(str.substring(i*lines,(i+1)*lines));
        }
        return false;
    };


}
