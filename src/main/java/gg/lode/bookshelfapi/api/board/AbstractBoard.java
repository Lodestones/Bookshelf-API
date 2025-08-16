package gg.lode.bookshelfapi.api.board;

import gg.lode.bookshelfapi.api.util.MiniMessageHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractBoard {

    public static final String ID = "SIDEBAR_SLOT_";
    private static final String DEFAULT_SCOREBOARD_NAME = "Default Scoreboard Title";
    private static final int MAX_LINES = 15;
    protected final Scoreboard scoreboard;
    protected final Objective sidebar;

    protected final Player player;
    protected final TabList tabList;

    public AbstractBoard(Player player, Component title) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.scoreboard = player.getScoreboard();
        Objective sidebar = scoreboard.getObjective("sidebar");
        if (sidebar == null) sidebar = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, title);
        this.sidebar = sidebar;
        this.sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.tabList = new TabList(null, null);

        for (int i = 1; i <= MAX_LINES; i++) {
            getOrCreateTeam(i).addEntry(generateEntry(i));
        }
    }

    private Team getOrCreateTeam(int slot) {
        String teamName = ID + player.hashCode() + "_" + slot;
        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);
        return team;
    }

    /**
     * Sets a line in the scoreboard at the given slot with the provided text.
     *
     * @param slot the line number (1-based)
     * @param text the text to display
     */
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

    /**
     * Sets a line in the scoreboard at the given slot with the provided component.
     *
     * @param slot the line number (1-based)
     * @param text the component to display
     */
    private void setLine(int slot, Component text) {
        if (slot <= MAX_LINES) {
            Team team = getOrCreateTeam(slot);
            String entry = generateEntry(slot);
            if (!scoreboard.getEntries().contains(entry))
                this.sidebar.getScore(entry).setScore(slot);
            if (!team.prefix().equals(text))
                team.prefix(text);
        }
    }

    /**
     * Sets scoreboard lines from a list of strings.
     *
     * @param list the list of lines
     */
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

    /**
     * Sets scoreboard lines from a list of components.
     *
     * @param list the list of components
     */
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

    /**
     * Removes a line from the scoreboard at the given slot.
     *
     * @param slot the line number (1-based)
     */
    private void removeLine(int slot) {
        String entry = generateEntry(slot);
        if (scoreboard.getEntries().contains(entry))
            scoreboard.resetScores(entry);
    }

    /**
     * Returns the tab list associated with this board.
     *
     * @return the TabList
     */
    public TabList getTabList() {
        return tabList;
    }

    /**
     * Generates a unique entry string for a given slot, using ChatColor values.
     *
     * @param slot the line number (1-based)
     * @return the entry string
     */
    @SuppressWarnings("deprecation")
    private String generateEntry(int slot) {
        ChatColor[] colors = ChatColor.values();
        if (slot < 0 || slot >= colors.length) {
            throw new IllegalArgumentException("Slot out of bounds for ChatColor values: " + slot);
        }
        return colors[slot].toString();
    }

    /**
     * Updates the board. Implement this in subclasses to define board behavior.
     */
    public abstract void update();

    /**
     * Represents the tab list (top and bottom) for the board.
     */
    public static class TabList {
        private @Nullable Component topTabList;
        private @Nullable Component bottomTabList;

        /**
         * Creates a new TabList.
         *
         * @param topTabList    the top tab list component
         * @param bottomTabList the bottom tab list component
         */
        public TabList(@Nullable Component topTabList, @Nullable Component bottomTabList) {
            this.topTabList = topTabList;
            this.bottomTabList = bottomTabList;
        }

        /**
         * Gets the bottom tab list component.
         *
         * @return the bottom tab list
         */
        public @Nullable Component getBottomTabList() {
            return bottomTabList;
        }

        /**
         * Sets the bottom tab list component.
         *
         * @param bottomTabList the component to set
         */
        public void setBottomTabList(@Nullable Component bottomTabList) {
            this.bottomTabList = bottomTabList;
        }

        /**
         * Gets the top tab list component.
         *
         * @return the top tab list
         */
        public @Nullable Component getTopTabList() {
            return topTabList;
        }

        /**
         * Sets the top tab list component.
         *
         * @param topTabList the component to set
         */
        public void setTopTabList(@Nullable Component topTabList) {
            this.topTabList = topTabList;
        }
    }
}