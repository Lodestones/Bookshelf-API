# Creating Configurations
Creating configs with Bookshelf has been made easy!<br>
All of the hardwork of checking, creating, or saving a config file has been handled.

## Creating a Custom Config File

```java
import org.bukkit.plugin.java.JavaPlugin;
import api.gg.lode.bookshelfapi.Configuration;

public class MainConfiguration extends Configuration {

    public MainConfiguration(JavaPlugin plugin) {
        super(plugin, "config.yml"); // That's it!
    }

    // Add extra methods here!
    public void selfDestruct() {
        // EXPLODE!!!
    }

}
```

Alternatively, you can just create a variable of a default Configuration file.

```java
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import api.gg.lode.bookshelfapi.Configuration;
import util.api.gg.lode.bookshelfapi.MiniMessageUtil;

public class MyFirstPlugin extends JavaPlugin {

    private Configuration config;

    @Override
    public void onEnable() {
        this.config = new Configuration(this, "config.yml");

        final boolean isPVPEnabled = this.config().getBoolean("is_pvp_enabled");
        Bukkit.broadcast(MiniMessageUtil.deserialize(isPVPEnabled));

        // It's that simple!
    }

    public Configuration config() {
        return config;
    }
}
```