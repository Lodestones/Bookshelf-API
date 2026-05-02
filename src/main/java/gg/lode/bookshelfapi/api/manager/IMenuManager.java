package gg.lode.bookshelfapi.api.manager;

import gg.lode.bookshelfapi.api.menu.Menu;
import gg.lode.bookshelfapi.api.menu.PacketMenuHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IMenuManager {

    void register(Player player, Menu menu);

    void register(UUID uniqueId, Menu menu);

    void registerAndOpen(Player player, Menu menu);

    void registerAndOpen(UUID uniqueId, Menu menu);

    Menu getActiveMenu(Player player);

    Menu getActiveMenu(UUID uniqueId);

    void setPacketMenuHandler(@Nullable PacketMenuHandler handler);

    @Nullable PacketMenuHandler getPacketMenuHandler();

    void dispatchClick(Player player, InventoryClickEvent event);
}
