package to.lodestone.bookshelfapi.api.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import to.lodestone.bookshelfapi.api.kyori.FontInfo;
import to.lodestone.bookshelfapi.api.kyori.Wrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MiniMessageUtil {
    private final static int CENTER_PX = 154; // The size of the chat.
    private static final int MAX = 1280; // How big your screen is.
    private static final Style STYLE = Style.style().font(Key.key("space")).build();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * MiniMessage formatting utility, with String.format support built in.
     * <a href="https://docs.advntr.dev/minimessage/format.html">Read the MiniMessage Docs</a>
     *
     * @param str
     * @param args
     * @return
     */
    public static Component deserialize(String str, Object ...args) {
        return MINI_MESSAGE.deserialize(String.format(str, args));
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
                .map(MiniMessageUtil::getCenteredMessage)
                .map(MINI_MESSAGE::deserialize)
                .collect(Collectors.toList());
    }

    private static String getCenteredMessage(String str) {
        if (str == null || str.equals("")) return "";
        Component component = MINI_MESSAGE.deserialize(str);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold;
        List<Component> individualComponents = new ArrayList<>();
        individualComponents.add(component);
        for (List<Component> nested = component.children(); !nested.isEmpty(); nested = nested.get(0).children()) {
            individualComponents.addAll(nested);
        }
        for (Component child : individualComponents) {
            String message = ((TextComponent) child).content();
            isBold = child.hasDecoration(TextDecoration.BOLD);
            for (char c : message.toCharArray()) {
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

    /**
     * A method that allows the capability to create multiple components without it bleeding into incoming components.
     *
     * @param components A {@link List} of {@link Component}
     * @return A single {@link Component}.
     */
    public static Component persistStyle(Component ...components) {
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
        String str = MINI_MESSAGE.serialize(paramComponent);
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
