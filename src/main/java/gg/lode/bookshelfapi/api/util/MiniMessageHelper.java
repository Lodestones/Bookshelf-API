package gg.lode.bookshelfapi.api.util;

import gg.lode.bookshelfapi.api.kyori.FontInfo;
import gg.lode.bookshelfapi.api.kyori.Wrap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.*;
import java.util.stream.Collectors;

public class MiniMessageHelper {
    private final static int CENTER_PX = 154; // The size of the chat.
    private static final int MAX = 1280; // How big your screen is.
    private static final Style STYLE = Style.style().font(Key.key("space")).build();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();

    /**
     * MiniMessage formatting utility, with String.format support built in.
     * <a href="https://docs.advntr.dev/minimessage/format.html">Read the MiniMessage Docs</a>
     *
     * @param str  The {@link String} to deserialize.
     * @param args The {@link Object} arguments to format the string with.
     * @return A converted {@link Component}.
     */
    public static Component deserialize(String str, Object... args) {
        return MINI_MESSAGE.deserialize(String.format(str, args));
    }

    public static Component deserialize(Object str, Object... args) {
        return MINI_MESSAGE.deserialize(String.format(String.valueOf(str), args));
    }

    /**
     * MiniMessage formatting utility, but splits any "\n" into a different list.
     * <a href="https://docs.advntr.dev/minimessage/format.html">Read the MiniMessage Docs</a>
     *
     * @param str The {@link String} to deserialize.
     * @return A converted {@link List} containing {@link Component}s.
     */
    public static List<Component> deserializeIntoList(String str) {
        List<Component> components = new ArrayList<>();
        for (String line : str.split("\n"))
            components.add(deserialize(line));
        return components;
    }

    /**
     * MiniMessage formatting utility, with centering capabilities with String.format support.
     * <a href="https://docs.advntr.dev/minimessage/format.html">Read the MiniMessage Docs</a>
     *
     * @param str The {@link String} to deserialize.
     * @return A converted {@link List} containing {@link Component}s.
     */
    public static List<Component> center(String str, Object ...args) {
        return Wrap.of(String.format(str, args), 50)
                .get()
                .stream()
                .map(MiniMessageHelper::getCenteredMessage)
                .map(MINI_MESSAGE::deserialize)
                .collect(Collectors.toList());
    }

    private static String getCenteredMessage(String str) {
        if (str == null || str.isEmpty()) return "";

        Component component = MINI_MESSAGE.deserialize(str);
        int messagePxSize = 0;

        Queue<Component> queue = new LinkedList<>();
        queue.add(component);

        while (!queue.isEmpty()) {
            Component current = queue.poll();
            queue.addAll(current.children());

            // Get the visible text content (for TextComponent, TranslatableComponent, etc.)
            String content = getComponentContent(current);
            if (content == null || content.isEmpty()) continue;

            boolean isBold = current.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE;

            boolean previousCode = false;
            for (char c : content.toCharArray()) {
                if (c == '§') {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    isBold = c == 'l' || c == 'L';
                } else {
                    FontInfo dFI = FontInfo.getDefaultFontInfo(c);
                    messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                    messagePxSize++;
                }
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = FontInfo.SPACE.getLength() + 1;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb + str;
    }

    private static String getComponentContent(Component component) {
        if (component instanceof TextComponent text) {
            return text.content();
        }
        if (component instanceof TranslatableComponent translatable) {
            // Simplified fallback: use the translation key
            return translatable.key();
        }
        if (component instanceof KeybindComponent keybind) {
            return keybind.keybind();
        }
        if (component instanceof ScoreComponent score) {
            return score.value();
        }
        if (component instanceof SelectorComponent selector) {
            return selector.pattern();
        }

        // Fallback to plain string representation (optional)
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * A method that allows the capability to create multiple components without it bleeding into other components.
     *
     * @param components A {@link List} of {@link Component}
     * @return A single {@link Component}.
     */
    public static Component persistStyle(Component... components) {
        Component component = Component.empty();
        for (Component comp : components) {
            component = component.append(comp).style(comp.style());
        }
        return component;
    }

    /**
     * Converts a hex color code or default named color into a {@link TextColor}.
     *
     * @param paramString A hex color code or default named color.
     * @return A {@link TextColor}.
     */
    public static TextColor color(String paramString) {
        if (paramString != null) {
            paramString = paramString.toLowerCase(Locale.ROOT).replace(" ", "_").replace("-", "_");
            if (TextColor.fromHexString(paramString) != null)
                return TextColor.fromHexString(paramString);
            if (NamedTextColor.NAMES.keys().contains(paramString))
                return NamedTextColor.NAMES.value(paramString);
        }

        throw new IllegalArgumentException("Color does not exist: " + ((paramString != null) ? paramString : "null"));
    }

    /**
     * Strips any font in a {@link TextComponent}.
     *
     * @param paramComponent The {@link TextComponent} to strip from.
     * @return A serialized {@link TextComponent}.
     */
    public static TextComponent stripFont(Component paramComponent) {
        TextComponent textComponent = toTextComponent(paramComponent);
        return textComponent.toBuilder().applyDeep(paramComponentBuilder -> paramComponentBuilder.font(null)).build();
    }

    /**
     * Strips any italic in a {@link TextComponent}.
     *
     * @param paramComponent The {@link TextComponent} to strip from.
     * @return A serialized {@link TextComponent}.
     */
    public static TextComponent stripItalic(Component paramComponent) {
        TextComponent textComponent = toTextComponent(paramComponent);
        return textComponent.toBuilder().applyDeep(paramComponentBuilder -> paramComponentBuilder.decoration(TextDecoration.ITALIC, TextDecoration.State.NOT_SET)).build();
    }

    /**
     * Converts a {@link Component} into a {@link String}.
     *
     * @param paramComponent The {@link Component} to serialize.
     * @return A serialized {@link String}.
     */
    public static String serialize(Component paramComponent) {
        String str = PLAIN_TEXT_SERIALIZER.serialize(paramComponent);
        return str.replace("\\<", "<");
    }

    /**
     * Converts a {@link Component} to a {@link TextComponent}
     *
     * @param paramComponent The {@link Component} to convert.
     * @return A {@link TextComponent}
     */
    private static TextComponent toTextComponent(Component paramComponent) {
        return Component.text().append(paramComponent).build();
    }

    /**
     * Generates a spaced text based on pixels.
     * This value can either be negative or positive.
     * <p>
     * Only use this if you have a custom resource pack using <a href="https://github.com/AmberWat/NegativeSpaceFont">NegativeSpaceFont</a>
     *
     * @param var1 how far left or right to push the pixel.
     * @return the generated component.
     */
    public static Component space(int var1) {
        return Component.translatable("space." + Math.min(Math.max(-MAX, var1), MAX)).style(STYLE);
    }

    /**
     * Generates a new layer for the text to be on.
     * This is so some text has a priority layer over another.
     * WARNING: Mass usage of this can cause FPS lag to the client.
     * <p>
     * Only use this if you have a custom resource pack using <a href="https://github.com/AmberWat/NegativeSpaceFont">NegativeSpaceFont</a>
     *
     * @return the generated component.
     */
    public static Component newLayer() {
        return Component.translatable("newlayer").style(STYLE);
    }

}
