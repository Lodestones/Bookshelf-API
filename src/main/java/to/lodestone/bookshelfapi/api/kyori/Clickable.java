package to.lodestone.bookshelfapi.api.kyori;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that wraps all the contents for a clickable components into one.
 *
 * @author John Aquino
 */
public class Clickable {

    private final List<TextComponent> components;

    public Clickable(String message, @Nullable HoverEvent<?> hover, @Nullable ClickEvent click) {
        this(Component.text(message), hover, click);
    }

    public Clickable(TextComponent message) {
        this(message, null, null);
    }

    public Clickable(TextComponent message, @Nullable HoverEvent<?> hover, @Nullable ClickEvent click) {
        this.components = new ArrayList<>();
        this.add(message, hover, click);
    }

    public Clickable(Component message) {
        this((TextComponent) message);
    }

    public Clickable(Component message, @Nullable HoverEvent<?> hover, @Nullable ClickEvent click) {
        this((TextComponent) message, hover, click);
    }

    public TextComponent add(TextComponent component, @Nullable HoverEvent<?> hover, @Nullable ClickEvent click) {
        if (hover != null) component = component.hoverEvent(hover);
        if (click != null) component = component.clickEvent(click);
        this.components.add(component);
        return component;
    }

    public void add(TextComponent component) {
        this.components.add(component);
    }

    public void sendToPlayer(Player player) {
        this.sendToPlayers(player);
    }

    public void sendToPlayers(Player... players) {
        Component mergedComponent = this.asComponents();
        for (Player player : players) {
            player.sendMessage(mergedComponent);
        }
    }

    public Component asComponents() {
        return Component.text().append(this.components).build();
    }
}
