package fr.lezoo.contracts.utils;

import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.listener.temp.TemporaryListener;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.review.ContractReview;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens to chat input without any inventory
 */
public class SimpleChatInput extends TemporaryListener {
    private final PlayerData playerData;
    private final ContractReview contract;
    private final TriFunction<PlayerData, String, ContractReview, Boolean> inputHandler;

    public SimpleChatInput(PlayerData playerData, ContractReview contract, TriFunction<PlayerData, String, ContractReview, Boolean> inputHandler) {
        super(AsyncPlayerChatEvent.getHandlerList(), PlayerMoveEvent.getHandlerList());

        this.playerData = playerData;
        this.contract = contract;
        this.inputHandler = inputHandler;

        playerData.setOnChatInput(true);
    }

    /**
     * Close only if the input handler accepts the message.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().equals(playerData.getPlayer())) {
            event.setCancelled(true);
            if (inputHandler.apply(playerData, event.getMessage(), contract))
                close();
        }
    }

    @EventHandler
    public void cancel(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ())
            close();
    }

    @Override
    public void whenClosed() {
        playerData.setOnChatInput(false);
    }
}
