package to.lodestone.bookshelfapi.api;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import to.lodestone.bookshelfapi.api.util.MiniMessageUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A utility class that can retrieve a {@link YamlConfiguration} file.
 * If provided, it can also generate the {@link YamlConfiguration} compiled inside the current jar.
 *
 * @author John Aquino
 */
public class Configuration {
    protected final JavaPlugin plugin;
    protected YamlConfiguration config;

    protected final String filePath;

    public Configuration(JavaPlugin plugin, String filePath) {
        this.plugin = plugin;
        this.filePath = filePath;
    }

    /**
     * If you do not have the necessary utility methods that {@link Configuration} provides,
     * you can use the {@link YamlConfiguration} to retrieve all the methods it provides.
     *
     * @return the {@link YamlConfiguration} file.
     */
    public YamlConfiguration get() {
        return config;
    }

    public @Nullable ConfigurationSection getSection(@NotNull UUID path) {
        return getConfigurationSection(path.toString());
    }

    public @Nullable ConfigurationSection getSection(@NotNull String path) {
        return getConfigurationSection(path);
    }

    public @NotNull List<Float> getFloatList(@NotNull String path) {
        return config.getFloatList(path);
    }

    public @NotNull List<Double> getDoubleList(@NotNull String path) {
        return config.getDoubleList(path);
    }

    public @Nullable ConfigurationSection getConfigurationSection(@NotNull String path) {
        return config.getConfigurationSection(path);
    }

    public void set(@NotNull UUID path, @Nullable Object value) {
        set(path.toString(), value);
    }

    public void save() {
        try {
            File configFile = new File(plugin.getDataFolder() + File.separator + filePath);
            this.config.save(configFile);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void set(@NotNull String path, @Nullable Object value) {
        config.set(path, value);
    }

    public @Nullable List<?> getList(@NotNull String path) {
        return config.getList(path);
    }

    public @Nullable Object get(@NotNull String path) {
        return get(path, null);
    }

    @Contract("_, !null -> !null")
    public @Nullable Object get(@NotNull String path, @Nullable Object def) {
        return config.get(path, def);
    }

    public boolean getBoolean(@NotNull String path) {
        return getBoolean(path, false);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public int getInt(@NotNull String path) {
        return getInt(path, 0);
    }

    public int getInt(@NotNull String path, int def) {
        return config.getInt(path, def);
    }

    public @NotNull List<Map<?, ?>> getMapList(@NotNull String path) {
        return config.getMapList(path);
    }

    public @NotNull List<String> getStringList(@NotNull String path) {
        return config.getStringList(path);
    }

    public @Nullable String getString(@NotNull String path) {
        return getString(path, null);
    }

    public @Nullable String getString(@NotNull String path, @Nullable String def) {
        return config.getString(path, def);
    }

    /**
     * Attempt to read or create the file and plugin folder.
     * Use this method instead of onEnable() for more control over this process.
     *
     * @return true if the configuration was read/created successfully, false otherwise.
     */
    public boolean initialize() {
        return initialize(true);
    }

    /**
     * Safely attempts to read or create the file. (catches and prints exceptions)
     * Use this method instead of onEnable() for more control over this process.
     *
     * @param tryPluginFolder Whether to attempt to create the plugin folder or not
     * @return true if the configuration was read/created successfully, false otherwise.
     */
    public boolean initialize(boolean tryPluginFolder) {
        try {
            return unsafeInitialize(tryPluginFolder);
        } catch (Exception var1) {
            var1.printStackTrace();
            return false;
        }
    }

    public boolean unsafeInitialize(boolean tryPluginFolder) throws IOException, InvalidConfigurationException {
        config = new YamlConfiguration();

        if (tryPluginFolder) {
            if (!generateFolder(plugin.getDataFolder())) {
                return false;
            }
        }

        File configFile = new File(plugin.getDataFolder() + File.separator + filePath);

        if (!configFile.exists()) {
            plugin.saveResource(filePath, false);
        }

        config.load(configFile);
        return true;
    }

    public boolean generateFolder(File file) {
        if (!file.exists()) {
            return file.mkdir();
        }

        return true;
    }

}
