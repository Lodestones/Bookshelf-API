package gg.lode.bookshelfapi.api.dialog;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Version-agnostic data model for Minecraft dialogs (MC 1.21.6+).
 * <p>
 * Ported from FancyInnovations' FancySitula dialog model
 * (<a href="https://github.com/FancyInnovations/FancyPlugins">FancyPlugins</a>,
 * MIT License, Copyright (c) 2025 Oliver Schlüter). Reshaped into Bookshelf
 * conventions as immutable records. These types carry no NMS or Paper-API
 * references — {@link DialogService} renders them onto the running server via
 * reflection, so they compile and behave the same on any build.
 * <p>
 * All text fields are MiniMessage strings.
 */
public final class DialogModel {

    private DialogModel() {
    }

    /** What the client does after a button action resolves. */
    public enum AfterAction {
        CLOSE,
        NONE,
        WAIT_FOR_RESPONSE
    }

    /** Common header data shared by every dialog type. */
    public record Data(String title,
                       @Nullable String externalTitle,
                       boolean canCloseWithEscape,
                       boolean pause,
                       AfterAction afterAction,
                       List<Body> body,
                       List<Input> inputs) {
    }

    // ---- body -------------------------------------------------------------

    public sealed interface Body permits TextBody, ComponentBody, ItemBody {
    }

    /** A line/paragraph of text. {@code width} in GUI pixels (vanilla default 200). */
    public record TextBody(String text, int width) implements Body {
        public TextBody(String text) {
            this(text, 200);
        }
    }

    /**
     * Pre-built Adventure {@link net.kyori.adventure.text.Component} body —
     * use when content can't roundtrip through MiniMessage (custom-font glyphs,
     * inline player heads via the ChatHeadFont resource pack, etc.).
     */
    public record ComponentBody(net.kyori.adventure.text.Component component, int width) implements Body {
        public ComponentBody(net.kyori.adventure.text.Component component) {
            this(component, 200);
        }
    }

    /**
     * An item rendered inline in the dialog body, with optional descriptive
     * text shown next to it (vanilla puts the item on the left and the
     * description on the right).
     *
     * @param item            the Bukkit ItemStack to render
     * @param description     MiniMessage text shown beside the item, or {@code null}
     * @param showDecorations whether to show stack count/durability bar
     * @param showTooltip     whether hovering shows the item tooltip
     * @param width           rendered width in GUI pixels (vanilla default 200)
     * @param height          rendered height in GUI pixels (vanilla default 16)
     */
    public record ItemBody(org.bukkit.inventory.ItemStack item,
                           @Nullable String description,
                           boolean showDecorations,
                           boolean showTooltip,
                           int width,
                           int height) implements Body {
        public ItemBody(org.bukkit.inventory.ItemStack item, @Nullable String description) {
            this(item, description, true, true, 200, 16);
        }
    }

    // ---- buttons & actions ------------------------------------------------

    /** Visual data for a clickable button. {@code tooltip} nullable. */
    public record ButtonData(String label, @Nullable String tooltip, int width) {
        public ButtonData(String label) {
            this(label, null, 150);
        }
    }

    /** What a button does when clicked. */
    public sealed interface ButtonAction permits CustomAction, CopyToClipboardAction, RunCommandAction {
    }

    /**
     * Sends a custom payload back to the server, surfaced via the dialog
     * response listener. {@code id} is the action identifier; {@code payload}
     * carries arbitrary string entries plus any submitted input values.
     */
    public record CustomAction(String id, Map<String, String> payload) implements ButtonAction {
    }

    /** Copies a string to the player's clipboard (client-side). */
    public record CopyToClipboardAction(String value) implements ButtonAction {
    }

    /** Runs a command as the player (client-side click event). */
    public record RunCommandAction(String command) implements ButtonAction {
    }

    /** A button: visual data plus an optional action ({@code null} = inert). */
    public record ActionButton(ButtonData data, @Nullable ButtonAction action) {
    }

    // ---- inputs -----------------------------------------------------------

    /** A keyed input field. The {@code key} is echoed back in responses. */
    public record Input(String key, InputControl control) {
    }

    public sealed interface InputControl
            permits TextInput, BooleanInput, SingleOptionInput, NumberRangeInput {
    }

    /** Free-text field. {@code maxLines} null = single line. */
    public record TextInput(int width,
                            String label,
                            boolean labelVisible,
                            @Nullable String initial,
                            int maxLength,
                            @Nullable Integer maxLines) implements InputControl {
    }

    /** Checkbox. {@code onTrue}/{@code onFalse} are the submitted string values. */
    public record BooleanInput(String label,
                               boolean initial,
                               String onTrue,
                               String onFalse) implements InputControl {
    }

    /** Dropdown of options. */
    public record SingleOptionInput(int width,
                                    List<Option> entries,
                                    String label,
                                    boolean labelVisible) implements InputControl {
        /** One dropdown entry. {@code display} nullable (falls back to id). */
        public record Option(String id, @Nullable String display, boolean initial) {
        }
    }

    /** Slider over a numeric range. {@code initial}/{@code step} nullable. */
    public record NumberRangeInput(int width,
                                   String label,
                                   String labelFormat,
                                   float start,
                                   float end,
                                   @Nullable Float initial,
                                   @Nullable Float step) implements InputControl {
    }
}
