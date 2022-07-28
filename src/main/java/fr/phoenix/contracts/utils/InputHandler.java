package fr.phoenix.contracts.utils;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.gui.ContractPortfolioViewer;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.contract.review.ContractReview;
import fr.phoenix.contracts.utils.message.Message;

public class InputHandler {

    public static final TriFunction<PlayerData, String, ContractReview, Boolean> SET_NOTATION = (playerData, str, review) -> {
        try {
            int value=Integer.parseInt(str);
            review.setNotation(value);
        }
        catch (Exception e) {
            Message.NOT_VALID_NOTATION.format("input",str).send(playerData.getPlayer());
        }
        ContractPortfolioViewer.displayChoices(playerData,review);
        return true;
    };

    public static final TriFunction<PlayerData, String, ContractReview, Boolean> SET_COMMENT = (playerData, str, review) -> {
        if(str.equals("")) {
            ContractPortfolioViewer.displayChoices(playerData,review);
            return true;
        }
        int lines=(str.length()/ Contracts.plugin.configManager.maxCommentCharPerLine)+1;
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
