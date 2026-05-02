package gg.lode.bookshelfapi.api.manager;

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
}
