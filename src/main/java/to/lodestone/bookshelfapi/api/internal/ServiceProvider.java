package to.lodestone.bookshelfapi.api.internal;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

/**
 * Service Provider that hooks into Bukkit's Plugin Service.
 *
 * @param <T> {@link Service}
 * @author John Aquino
 */
public class ServiceProvider<T extends Service> {
    protected T service = null;

    public ServiceProvider(JavaPlugin plugin, Class<T> type, String name) {
        T provider = plugin.getServer().getServicesManager().load(type);
        if (provider == null) {
            plugin.getLogger().severe("Could not hook into " + name + ". This may cause major errors in the plugin.");
        } else {
            this.service = provider;
            plugin.getLogger().info("Successfully hooked into " + name + "!");
        }
    }

    @Nullable
    public T service() {
        return this.service;
    }
}
