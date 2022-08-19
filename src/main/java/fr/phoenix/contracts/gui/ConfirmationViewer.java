package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.checkerframework.checker.units.qual.C;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * GUI to accept one decision. Receives some data through T.
 */
public class ConfirmationViewer<T> extends EditableInventory {
    private final BiConsumer<ConfirmationInventory, T> consumer;

    public ConfirmationViewer(BiConsumer<ConfirmationInventory, T> consumer) {
        super("confirmation");
        this.consumer = consumer;
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("yes"))
            return new YesItem(config);
        if (function.equals("back")) {
            return new BackItem(config);
        }
        return null;
    }

    /**
     * Tells the Confirmation GUI what is has to do when the player clicks on Yes.
     */
    public void accept(ConfirmationInventory inv, T t) {
        consumer.accept(inv, t);
    }

    public ConfirmationInventory generate(GeneratedInventory previous, T t) {
        return new ConfirmationInventory<T>(previous.getPlayerData(), this, previous, t);
    }

    public class BackItem extends InventoryItem<ConfirmationInventory> {

        public BackItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(ConfirmationInventory inv, int n) {
            return new Placeholders();
        }

    }


    public class YesItem extends InventoryItem<ConfirmationInventory> {

        public YesItem(ConfigurationSection config) {
            super(config);
        }
        @Override
        public Placeholders getPlaceholders(ConfirmationInventory inv, int n) {
            return new Placeholders();
        }
    }


    public class ConfirmationInventory<T> extends GeneratedInventory {
        private final GeneratedInventory previous;
        private final T t;

        public ConfirmationInventory(PlayerData playerData, EditableInventory editable, GeneratedInventory previous, T t) {
            super(playerData, editable);
            this.previous = previous;
            this.t=t;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return Contracts.plugin.placeholderParser.parse(player, str);
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("back")) {
                previous.open();
            }
            if (item.getFunction().equals("yes")) {
                ((ConfirmationViewer) getEditable()).accept(this, t);
                player.getOpenInventory().close();
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }


}
