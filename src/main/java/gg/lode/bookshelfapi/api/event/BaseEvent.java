package gg.lode.bookshelfapi.api.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public abstract class BaseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public BaseEvent() {
        this(false);
    }

    public BaseEvent(boolean async) {
        super(async);
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public boolean call() {
        Bukkit.getPluginManager().callEvent(this);
        return this instanceof Cancellable && ((Cancellable) this).isCancelled();
    }
}
