package gg.lode.bookshelfapi.api.menu;

/**
 * Server-implementation hook for packet-based menus.
 * <p>
 * When a {@link Menu} is marked packet-based via
 * {@link gg.lode.bookshelfapi.api.menu.build.TopMenuBuilder#setPacketBased(boolean)},
 * the API delegates open/update/close to this handler so the platform module
 * (e.g. Bookshelf-Paper) can intercept inbound click packets and prevent any
 * server-side inventory mutation regardless of TPS lag.
 */
public interface PacketMenuHandler {

    void onOpen(Menu menu);

    void onUpdate(Menu menu);

    void onClose(Menu menu);
}
