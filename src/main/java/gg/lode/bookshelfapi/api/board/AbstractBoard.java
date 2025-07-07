package gg.lode.bookshelfapi.api.board;

import gg.lode.bookshelfapi.api.util.MiniMessageHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractBoard {

    public static final String ID = "SIDEBAR_SLOT_";
    private static final String DEFAULT_SCOREBOARD_NAME = "Default Scoreboard Title";
    private static final int MAX_LINES = 15;
    protected final Objective sidebar;

    protected final Player player;
    protected final TabList tabList;

    public AbstractBoard(Player player, Component title) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective sidebar = scoreboard.getObjective("sidebar");
        if (sidebar == null) sidebar = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, title);
        this.sidebar = sidebar;
        this.sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.player = player;
        this.tabList = new TabList(null, null);

        for (int i = 1; i <= 15; i++) {
            Team team = scoreboard.getTeam(ID + player.hashCode() + "_" + i);
            if (team == null) team = scoreboard.registerNewTeam(ID + player.hashCode() + "_" + i);
            team.addEntry(generateEntry(i));
        }
    }

    private void setLine(int slot, String text) {
        setLine(slot, MiniMessageHelper.deserialize(text));
    }

    public AbstractBoard(Player player) {
        this(player, Component.text(DEFAULT_SCOREBOARD_NAME));
    }

    public Scoreboard getScoreboard() {
        return player.getScoreboard();
    }

    public void setTitle(String title) {
        setTitle(MiniMessageHelper.deserialize(title));
    }

    public void setTitle(Component title) {
        assert title != null;
        assert this.sidebar != null;
        this.sidebar.displayName(title);
    }

    private void setLine(int slot, Component text) {
        Scoreboard scoreboard = player.getScoreboard();
        if (slot <= 15) {
            Team team = scoreboard.getTeam(ID + player.hashCode() + "_" + slot);
            assert team != null;
            String entry = generateEntry(slot);
            if (!scoreboard.getEntries().contains(entry))
                this.sidebar.getScore(entry).setScore(slot);
            if (!team.prefix().equals(text))
                team.prefix(text);
        }
    }

    protected void setLineFromList(List<String> list) {
        AtomicInteger slot = new AtomicInteger(list.size());
        if (slot.get() < MAX_LINES)
            for (int i = slot.get() + 1; i <= MAX_LINES; i++)
                removeLine(i);
        list.forEach(line -> {
            setLine(slot.get(), line);
            slot.getAndDecrement();
        });
    }

    protected void setLineFromComponentsList(List<Component> list) {
        AtomicInteger slot = new AtomicInteger(list.size());
        if (slot.get() < MAX_LINES)
            for (int i = slot.get() + 1; i <= MAX_LINES; i++)
                removeLine(i);
        list.forEach(line -> {
            setLine(slot.get(), line);
            slot.getAndDecrement();
        });
    }

    private void removeLine(int slot) {
        Scoreboard scoreboard = player.getScoreboard();
        String entry = generateEntry(slot);
        if (scoreboard.getEntries().contains(entry))
            scoreboard.resetScores(entry);
    }

    public TabList getTabList() {
        return tabList;
    }

    @SuppressWarnings("deprecation")
    private String generateEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }

    public abstract void update();

    public static class TabList {
        private @Nullable Component topTabList;
        private @Nullable Component bottomTabList;

        public TabList(@Nullable Component topTabList, @Nullable Component bottomTabList) {
            this.topTabList = topTabList;
            this.bottomTabList = bottomTabList;
        }

        public @Nullable Component getBottomTabList() {
            return bottomTabList;
        }

        public void setBottomTabList(@Nullable Component bottomTabList) {
            this.bottomTabList = bottomTabList;
        }

        public @Nullable Component getTopTabList() {
            return topTabList;
        }

        public void setTopTabList(@Nullable Component topTabList) {
            this.topTabList = topTabList;
        }
    }
}