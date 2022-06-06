package fr.lezoo.contracts.utils;

import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.gui.objects.GeneratedInventory;
import fr.lezoo.contracts.listener.temp.TemporaryListener;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.review.ContractReview;
import fr.lezoo.contracts.utils.message.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.function.BiFunction;

/**
 * Listens to chat input if an inventory is precised, it will be opened at the end of the chat input.
 */
public class ChatInput extends TemporaryListener {
    private final PlayerData playerData;
    private final BiFunction<PlayerData, String, Boolean> inputHandler;
    private GeneratedInventory inv;

    public ChatInput(PlayerData playerData, GeneratedInventory inv, BiFunction<PlayerData, String, Boolean> inputHandler) {
        super(AsyncPlayerChatEvent.getHandlerList(), PlayerMoveEvent.getHandlerList());

        this.playerData = playerData;
        this.inputHandler = inputHandler;
        this.inv = inv;
        //We close the inventory
        if(inv!=null)
            playerData.getPlayer().getOpenInventory().close();
        playerData.setOnChatInput(true);
    }


    public ChatInput(PlayerData playerData, BiFunction<PlayerData, String, Boolean> inputHandler) {
        this(playerData,null,inputHandler);
    }

    /**
     * Close only if the input handler accepts the message.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().equals(playerData.getPlayer())) {
            event.setCancelled(true);
            if (inputHandler.apply(playerData, event.getMessage()))
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
        if(inv!=null) {
            inv.open();
        }
    }
}
