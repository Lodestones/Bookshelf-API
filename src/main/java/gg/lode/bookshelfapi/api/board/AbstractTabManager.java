package gg.lode.bookshelfapi.api.board;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

public abstract class AbstractTabManager extends BukkitRunnable {
    protected final JavaPlugin plugin;

    public AbstractTabManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void handleColor(Player player, Player other, String name, NamedTextColor color) {
        if (name == null)
            name = "default";
        Team scoreboardTeam = player.getScoreboard().getTeam(name);
        if (scoreboardTeam == null)
            try {
                scoreboardTeam = player.getScoreboard().registerNewTeam(name);
                if (color != null)
                    scoreboardTeam.color(color);
                if (!name.equalsIgnoreCase("default"))
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

    public void removeFromTeams(Player player, Player other) {
        if (player == null || other == null)
            return;
        for (Team team : player.getScoreboard().getTeams()) {
            if (team.hasEntry(other.getName()))
                team.removeEntry(other.getName());
        }
    }
}