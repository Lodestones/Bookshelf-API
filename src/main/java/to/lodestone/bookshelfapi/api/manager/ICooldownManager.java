package to.lodestone.bookshelfapi.api.manager;

import org.bukkit.entity.Player;

public interface ICooldownManager {

    void setCooldown(Player player, String id, long milliseconds);
    void setCooldown(String id, long milliseconds);

    boolean hasCooldown(Player player, String id);
    boolean hasCooldown(String id);

}
