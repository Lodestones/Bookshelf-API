package gg.lode.bookshelfapi.api.manager;

import java.util.List;
import java.util.UUID;

public interface IChatManager {

    void setChatMuted(boolean value);
    boolean isChatMuted();

    boolean canPlayerBypassChat(UUID uniqueId);

    List<String> getChannels();

}
