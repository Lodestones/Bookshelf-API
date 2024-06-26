package to.lodestone.bookshelfapi.api;

import org.bukkit.plugin.java.JavaPlugin;
import to.lodestone.bookshelfapi.IBookshelfAPI;
import to.lodestone.bookshelfapi.api.internal.ServiceProvider;

public class BookshelfProvider extends ServiceProvider<IBookshelfAPI> {

    public BookshelfProvider(JavaPlugin plugin) {
        super(plugin, IBookshelfAPI.class, "Bookshelf");
    }

}
