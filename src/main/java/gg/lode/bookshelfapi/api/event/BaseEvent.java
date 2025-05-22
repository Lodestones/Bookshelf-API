package gg.lode.bookshelfapi.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public abstract class BaseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public BaseEvent() {
        super();
    }

    public BaseEvent(boolean isAsync) {
        super(isAsync);
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
} 