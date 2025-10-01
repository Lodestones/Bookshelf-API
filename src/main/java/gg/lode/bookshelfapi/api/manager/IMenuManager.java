package gg.lode.bookshelfapi.api.manager;

import gg.lode.bookshelfapi.api.menu.Menu;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface IMenuManager {

    void register(Player player, Menu menu);

    void register(UUID uniqueId, Menu menu);

    void registerAndOpen(Player player, Menu menu);

    void registerAndOpen(UUID uniqueId, Menu menu);

    Menu getActiveMenu(Player player);

    Menu getActiveMenu(UUID uniqueId);

}
