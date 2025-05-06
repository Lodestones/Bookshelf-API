package gg.lode.bookshelfapi.api.manager;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface IVanishManager {

    boolean isVanished(Player player);
    boolean isVanished(UUID uniqueId);

    void setVanished(Player player, boolean vanished);
    void setVanished(UUID uniqueId, boolean vanished);

    void vanishPlayer(Player player);
    void unvanishPlayer(Player player);
}
