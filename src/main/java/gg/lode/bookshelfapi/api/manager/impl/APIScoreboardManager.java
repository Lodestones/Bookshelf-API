package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.board.AbstractBoard;
import gg.lode.bookshelfapi.api.manager.IScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class APIScoreboardManager extends BukkitRunnable implements IScoreboardManager, Listener {

    private final static String DEFAULT_TEAM_NAME = "default_team";
    protected final JavaPlugin plugin;
    protected final HashMap<UUID, AbstractBoard> playerBoards = new HashMap<>();

    public APIScoreboardManager(JavaPlugin plugin) {
        this.plugin = plugin;

        startScoreboard(plugin, 1);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void changeColor(Player player, Player other, NamedTextColor color, @Nullable String teamName) {
        if (teamName == null) teamName = DEFAULT_TEAM_NAME;
        Team scoreboardTeam = player.getScoreboard().getTeam(teamName);
        if (scoreboardTeam == null)
            try {
                scoreboardTeam = player.getScoreboard().registerNewTeam(teamName);
                if (color != null)
                    scoreboardTeam.color(color);
                if (!teamName.equalsIgnoreCase(DEFAULT_TEAM_NAME))
                    scoreboardTeam.prefix(Component.empty());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        assert scoreboardTeam != null;
        if (scoreboardTeam.hasEntry(other.getName()))
            return;
        removeFromTeams(player, other);
        scoreboardTeam.addEntry(other.getName());
    }

    @Override
    public void changeColor(Player player, Player other, NamedTextColor color) {
        changeColor(player, other, color, null);
    }

    @Override
    public void removeFromTeams(Player player, Player other) {
        if (player == null || other == null)
            return;
        for (Team team : player.getScoreboard().getTeams()) {
            if (team.hasEntry(other.getName()))
                team.removeEntry(other.getName());
        }
    }

    @Override
    public void addPlayer(Player player, AbstractBoard board) {
        addPlayer(player.getUniqueId(), board);
    }

    @Override
    public void addPlayer(UUID uniqueId, AbstractBoard board) {
        playerBoards.put(uniqueId, board);
    }

    @Override
    public void removePlayer(Player player) {
        removePlayer(player.getUniqueId());
    }

    @Override
    public void removePlayer(UUID uniqueId) {
        playerBoards.remove(uniqueId);
    }

    @Override
    public boolean hasScoreboard(Player player) {
        return hasScoreboard(player.getUniqueId());
    }

    @Override
    public boolean hasScoreboard(UUID uniqueId) {
        return playerBoards.containsKey(uniqueId);
    }

    @Override
    public Map<UUID, AbstractBoard> getAllScoreboards() {
        return playerBoards;
    }

    @Override
    public void startScoreboard(JavaPlugin plugin, int tick) {
        this.runTaskTimerAsynchronously(plugin, tick, tick);
    }

    @Override
    public void endScoreboard() {
        playerBoards.clear();
        this.cancel();
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, AbstractBoard> entry : playerBoards.entrySet()) {
            UUID uniqueId = entry.getKey();
            AbstractBoard board = entry.getValue();
            if (board != null) {
                Player player = plugin.getServer().getPlayer(uniqueId);
                if (player != null && player.isOnline()) {
                    board.update();

                    AbstractBoard.TabList tabList = board.getTabList();

                    if (tabList.getTopTabList() != null)
                        player.sendPlayerListHeader(tabList.getTopTabList());

                    if (tabList.getBottomTabList() != null)
                        player.sendPlayerListFooter(tabList.getBottomTabList());
                }
            }
        }
    }
}