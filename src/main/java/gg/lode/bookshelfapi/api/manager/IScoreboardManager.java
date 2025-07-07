package gg.lode.bookshelfapi.api.manager;

import gg.lode.bookshelfapi.api.board.AbstractBoard;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public interface IScoreboardManager {
    void addPlayer(Player player, AbstractBoard board);

    void addPlayer(UUID uniqueId, AbstractBoard board);

    void removePlayer(Player player);

    void removePlayer(UUID uniqueId);

    boolean hasScoreboard(Player player);

    boolean hasScoreboard(UUID uniqueId);

    Map<UUID, AbstractBoard> getAllScoreboards();

    void startScoreboard(JavaPlugin plugin, int tick);

    void endScoreboard();

    void changeColor(Player player, Player other, NamedTextColor color, @Nullable String teamName);

    void changeColor(Player player, Player other, NamedTextColor color);

    void removeFromTeams(Player player, Player other);
}