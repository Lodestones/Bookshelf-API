# Creating Events
Creating events with Bookshelf is relatively easy!<br>
Make each class extend [BaseEvent](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/event/BaseEvent.java) and that's practically it.<br>
Ensure that your event class is structured with getters and setters.

# Example on a Readable Event
You can emit the event by running `PlayerEliminatedEvent#callEvent();`
```java
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import to.lodestone.bookshelfapi.api.event.BaseEvent;

public class PlayerEliminatedEvent extends BaseEvent {

    private final Player player;
    @Nullable
    private final LivingEntity killer;

    public PlayerEliminatedEvent(Player player, @Nullable LivingEntity killer) {
        this.player = player;
        this.killer = killer;
    }

    @Nullable
    public LivingEntity getKiller() {
        return killer;
    }

    public Player getPlayer() {
        return player;
    }
}
```
You can emit the event by running `PlayerConsumeEvent#callEvent();`<br>
You can cancel the event by setting `PlayerConsumeEvent#setCancelled(boolean);`
# Example on a Cancellable Event

```java
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import to.lodestone.bookshelfapi.api.event.BaseEvent;

public class PlayerConsumeEvent extends BaseEvent implements Cancellable {

    private final Player player;
    private final ItemStack consumedItem;

    public PlayerConsumeEvent(Player player, ItemStack consumedItem) {
        this.player = player;
        this.consumedItem = consumedItem;
    }

    public ItemStack getConsumedItem() {
        return consumedItem;
    }

    public Player getPlayer() {
        return player;
    }
}
```

