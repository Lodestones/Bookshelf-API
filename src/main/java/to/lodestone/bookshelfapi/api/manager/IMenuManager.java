package to.lodestone.bookshelfapi.api.manager;

import org.bukkit.entity.Player;
import to.lodestone.bookshelfapi.api.menu.Menu;

import java.util.UUID;

public interface IMenuManager {

    void register(Player player, Menu menu);
    void register(UUID uniqueId, Menu menu);
    void registerAndOpen(Player player, Menu menu);
    void registerAndOpen(UUID uniqueId, Menu menu);

}
