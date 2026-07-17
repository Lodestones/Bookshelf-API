package gg.lode.bookshelfapi.api.manager;

import gg.lode.bookshelfapi.api.chat.ChatNameResolver;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface IChatManager {

    void setChatMuted(boolean value);
    boolean isChatMuted();

    boolean canPlayerBypassChat(UUID uniqueId);

    List<String> getChannels();

    /**
     * Broadcast a one-off chat message into the given channel as
     * {@code senderName}. Skips moderation, mutes, and cooldowns — callers
     * are responsible for any pre-checks. Used for console chat and
     * one-off `/chat <channel> <msg>` sends.
     */
    void broadcastChannelMessage(String senderName, String channel, String message);

    /**
     * Installs the resolver deciding what name a chat sender appears under, per recipient. Pass null
     * to go back to the default, {@link org.bukkit.entity.Player#displayName()} for everyone.
     *
     * <p>There is one resolver for the server; setting a second replaces the first.
     *
     * @see ChatNameResolver
     */
    void setChatNameResolver(@Nullable ChatNameResolver resolver);

    /** The installed {@link ChatNameResolver}, or null if names resolve the default way. */
    @Nullable
    ChatNameResolver getChatNameResolver();
}
