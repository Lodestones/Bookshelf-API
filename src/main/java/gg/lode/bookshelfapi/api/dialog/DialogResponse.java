package gg.lode.bookshelfapi.api.dialog;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * The result of a player clicking a {@link DialogModel.CustomAction} button.
 * Carries the action id and the submitted payload — the button's additions
 * merged with every dialog input value, keyed by input key.
 */
public final class DialogResponse {

    private final Player player;
    private final String actionId;
    private final Map<String, String> values;

    public DialogResponse(Player player, String actionId, Map<String, String> values) {
        this.player = player;
        this.actionId = actionId;
        this.values = Collections.unmodifiableMap(values);
    }

    public Player player() {
        return player;
    }

    /** The custom action id (the {@code id} of the clicked button's action). */
    public String actionId() {
        return actionId;
    }

    /** All submitted values: input keys + button additions. */
    public Map<String, String> values() {
        return values;
    }

    public @Nullable String getText(String key) {
        return values.get(key);
    }

    public @Nullable Boolean getBoolean(String key) {
        String v = values.get(key);
        return v == null ? null : v.equalsIgnoreCase("true");
    }

    public @Nullable Float getFloat(String key) {
        String v = values.get(key);
        if (v == null) return null;
        try {
            return Float.parseFloat(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
