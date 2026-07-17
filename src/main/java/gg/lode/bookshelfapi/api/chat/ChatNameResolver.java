package gg.lode.bookshelfapi.api.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves the name a chat sender is shown under, per recipient.
 *
 * <p>Chat is otherwise rendered once and sent to everyone, so the sender's name is necessarily the
 * same for every recipient. That makes per-viewer identity impossible to express: a disguised player
 * cannot appear under their disguise to most of the server while staff still see who they really are.
 * A resolver is consulted once per recipient, so the same message can carry a different name to each.
 *
 * <p>Register one with {@link gg.lode.bookshelfapi.api.manager.IChatManager#setChatNameResolver}.
 * With none registered chat resolves names as it always has, from
 * {@link Player#displayName()}.
 *
 * <p>Implementations are called on the main thread, once per recipient per message, so they should be
 * a cheap lookup rather than any kind of I/O.
 */
@FunctionalInterface
public interface ChatNameResolver {

    /**
     * The name {@code sender} should appear under to {@code viewer}.
     *
     * @param sender the player who sent the message
     * @param viewer the player who will see it, or null when the recipient is not a player — the
     *               console, or a message being forwarded to another server, where no single
     *               viewer's perspective applies. Return the true name for a null viewer; a
     *               disguise that hides a player from the console hides them from moderation too.
     * @return the name to render; never null
     */
    @NotNull
    Component resolve(@NotNull Player sender, @Nullable Player viewer);
}
