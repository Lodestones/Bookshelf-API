package to.lodestone.bookshelfapi.api.manager;

import java.util.UUID;

public interface IChatManager {

    void setChatMuted(boolean value);
    boolean isChatMuted();
    boolean canPlayerSpeak(UUID uniqueId);

}
