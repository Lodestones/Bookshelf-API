package gg.lode.bookshelfapi.api.manager;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface ICooldownManager {

    void setCooldown(Player player, String id, long milliseconds);

    void setCooldown(String id, long milliseconds);

    void setCooldown(Player player, String id, long milliseconds, Consumer<Player> callback);

    void setCooldown(String id, long milliseconds, Consumer<Player> callback);

    boolean hasCooldown(Player player, String id);

    boolean hasCooldown(String id);

    boolean notifyPlayerWithCooldown(Player player, String id, Component component);

    boolean notifyPlayerWithCooldown(Player player, String id, String message);

    boolean notifyPlayerWithCooldown(Player player, String id, Component component, long milliseconds);

    boolean notifyPlayerWithCooldown(Player player, String id, String message, long milliseconds);

    long getCooldown(Player player, String id);

}
