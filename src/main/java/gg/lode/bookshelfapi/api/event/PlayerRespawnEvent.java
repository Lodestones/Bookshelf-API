package gg.lode.bookshelfapi.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PlayerRespawnEvent extends BaseEvent {

    private final Player player;
    @Nullable
    private Location respawnLocation;

    public PlayerRespawnEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public @Nullable Location getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(@Nullable Location respawnLocation) {
        this.respawnLocation = respawnLocation;
    }
}
