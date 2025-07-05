package gg.lode.bookshelfapi.api.board;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public interface IBoardManager {
    void addPlayer(Player paramPlayer, AbstractBoard paramAbstractBoard);

    void addPlayer(UUID paramUUID, AbstractBoard paramAbstractBoard);

    void removePlayer(Player paramPlayer);

    void removePlayer(UUID paramUUID);

    boolean hasScoreboard(Player paramPlayer);

    boolean hasScoreboard(UUID paramUUID);

    Map<UUID, AbstractBoard> getAllScoreboards();

    void startScoreboard(JavaPlugin paramJavaPlugin, int paramInt);

    void endScoreboard();
}