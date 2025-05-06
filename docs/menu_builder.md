# Creating Menus
Creating Menus is super easy!

```java
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import menu.api.gg.lode.bookshelfapi.Menu;
import build.menu.api.gg.lode.bookshelfapi.MenuBuilder;
import build.menu.api.gg.lode.bookshelfapi.TopMenuBuilder;
import util.api.gg.lode.bookshelfapi.MiniMessageUtil;

public class TestMenu extends Menu {

    private final Player player;

    public TestMenu(Player player) {
        this.player = player;
    }

    @Override
    @NotNull
    protected TopMenuBuilder getTopMenuBuilder(TopMenuBuilder builder) {
        return builder
                .setTitle("%s's Inventory", player.getName()) // Supports String.format w/ Kyori MiniMessage.
                .setRows(6) // 6 Rows or 53 Slots.
                .addClickAction(event -> event.setCancelled(true)) // Disable any clicks.
                .outline(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)) // Outline with Black Panes.
                .insertInRow(1, 1, new ItemStack(Material.PLAYER_HEAD), event -> {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage(MiniMessageUtil.deserialize("<green>I have been clicked!"));
                });
    }

    @Override
    @Nullable
    protected MenuBuilder getBottomMenuBuilder(MenuBuilder builder) {
        return null; // We do not need any contents at the bottom of our menu.
    }

} 
```

```java
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TestListener implements Listener {

    private final JavaPlugin plugin;
    public TestListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Opens the menu whenever the player joins!
        // Remember to hook into the api! Read the docs on how to do so.
        plugin.bookshelf().api().getMenuManager().registerAndOpen(player, new TestMenu(player));
    }
}
```