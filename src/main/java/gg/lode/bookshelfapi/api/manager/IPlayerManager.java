package gg.lode.bookshelfapi.api.manager;

import org.bukkit.entity.Player;

public interface IPlayerManager {

    boolean hasGodMode(Player player);
    void setGodMode(Player player, boolean value);
    String getCurrentChatChannel(Player player);

}
