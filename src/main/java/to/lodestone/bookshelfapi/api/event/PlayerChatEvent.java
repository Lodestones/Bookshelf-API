package to.lodestone.bookshelfapi.api.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerChatEvent extends BaseEvent implements Cancellable {

    private final Player player;
    private Component prefix;
    private Component suffix;
    private Component message;
    private boolean isCancelled;
    private String permission;
    private String playerColor;
    private String messageColor;
    private List<UUID> viewers;

    public PlayerChatEvent(Player player, Component prefix, Component suffix, Component message, String permission) {
        this.player = player;
        this.prefix = prefix;
        this.suffix = suffix;
        this.message = message;
        this.permission = permission;
        this.playerColor = NamedTextColor.GRAY.asHexString();
        this.messageColor = NamedTextColor.GRAY.asHexString();
        this.viewers = new ArrayList<>();
    }

    public PlayerChatEvent(Player player, Component prefix, Component suffix, Component message) {
        this.player = player;
        this.prefix = prefix;
        this.suffix = suffix;
        this.message = message;
        this.permission = null;
        this.playerColor = NamedTextColor.GRAY.asHexString();
        this.messageColor = NamedTextColor.GRAY.asHexString();
        this.viewers = new ArrayList<>();
    }

    public PlayerChatEvent(Player player, Component prefix, Component message) {
        this.player = player;
        this.prefix = prefix;
        this.suffix = Component.empty();
        this.message = message;
        this.permission = null;
        this.playerColor = NamedTextColor.GRAY.asHexString();
        this.messageColor = NamedTextColor.GRAY.asHexString();
        this.viewers = new ArrayList<>();
    }

    public PlayerChatEvent(Player player, Component message) {
        this.player = player;
        this.prefix = Component.empty();
        this.suffix = Component.empty();
        this.message = message;
        this.permission = null;
        this.playerColor = NamedTextColor.GRAY.asHexString();
        this.messageColor = NamedTextColor.GRAY.asHexString();
        this.viewers = new ArrayList<>();
    }

    public void messageColor(String messageColor) {
        this.messageColor = messageColor;
    }

    public String messageColor() {
        return this.messageColor;
    }

    public void playerColor(String playerColor) {
        this.playerColor = playerColor;
    }

    public String playerColor() {
        return this.playerColor;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    public Player getPlayer() {
        return player;
    }

    public Component prefix() {
        return prefix;
    }

    public Component suffix() {
        return suffix;
    }

    public Component message() {
        return message;
    }

    public void prefix(Component component) {
        this.prefix = component;
    }

    public void suffix(Component component) {
        this.suffix = component;
    }

    public void message(Component component) {
        this.message = component;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    public List<UUID> getViewers() {
        return viewers;
    }

    public void setViewers(List<UUID> viewers) {
        this.viewers = viewers;
    }
}
