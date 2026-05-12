package gg.lode.bookshelfapi.bootstrap;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper lifecycle contract that the cloud-loaded Bookshelf impl
 * fulfils. The public Bookshelf-Loader jar is a JavaPlugin shim that
 * downloads the impl, instantiates the entry class, and forwards
 * Bukkit lifecycle calls here. Implementations MUST have a public
 * no-arg constructor.
 */
public interface BookshelfBootstrap {
    void onLoad(JavaPlugin host);
    void onEnable(JavaPlugin host);
    void onDisable(JavaPlugin host);
}
