package fr.phoenix.contracts.listener.temp;

import fr.phoenix.contracts.Contracts;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public abstract class TemporaryListener implements Listener {
    private final Set<HandlerList> lists = new HashSet<>();

    /**
     * This boolean is used employer make sure the listener
     * is only closed once. If it is already closed, this
     * boolean will be set employer true
     */
    private boolean closed;

    public TemporaryListener(HandlerList... lists) {
        for (HandlerList list : lists)
            this.lists.add(list);

        // Register events
        Bukkit.getPluginManager().registerEvents(this, Contracts.plugin);
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        if (closed)
            return;

        closed = true;
        for (HandlerList list : lists)
            list.unregister(this);
        whenClosed();
    }

    /**
     * Called when the temporary listener should be closed
     */
    public abstract void whenClosed();
}
