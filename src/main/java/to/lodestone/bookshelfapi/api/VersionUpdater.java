package to.lodestone.bookshelfapi.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import to.lodestone.bookshelfapi.api.util.MiniMessageUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionUpdater implements Listener {
    private final JavaPlugin plugin;

    private final String name;
    private final String latestVersion;
    private final String currentVersion;
    private final String baseUrl;

    public VersionUpdater(JavaPlugin plugin, String name, String baseUrl, String updateUrl, String currentVersion) {
        this.plugin = plugin;
        this.baseUrl = baseUrl;
        this.currentVersion = currentVersion;
        this.name = name;
        this.latestVersion = getLatestVersion(updateUrl);

        if (this.latestVersion != null) {
            if (isNewerVersion(this.latestVersion, currentVersion)) {
                plugin.getLogger().severe("=================================");
                plugin.getLogger().severe(String.format("A newer version of %s is available for download in Modrinth!", name));
                plugin.getLogger().severe(String.format("Newest Version: %s | Current Version: %s", this.latestVersion, currentVersion));
                plugin.getLogger().severe(String.format("%s/version/%s", this.baseUrl, this.latestVersion));
                plugin.getLogger().severe("=================================");
            }
        }
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOp()) {
                if (this.latestVersion != null) {
                    if (isNewerVersion(this.latestVersion, currentVersion)) {
                        player.sendMessage(Component.empty());
                        player.sendMessage(MiniMessage.miniMessage().deserialize(String.format("  A newer version of <yellow>%s <reset>is available for download.", name)));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(String.format("  Newest Version: <green>%s <reset>| <reset>Current Version: <yellow>%s", latestVersion, currentVersion)));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(String.format("  <white>Update %s at <hover:show_text:'<green>Update %s at Modrinth!'><click:open_url:%s/version/%s><underlined><green>Modrinth!", name, name, baseUrl, latestVersion)));
                        player.sendMessage(Component.empty());
                    }
                }
            }
        }, 30L);
    }

    private String getLatestVersion(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                return ((JSONObject) jsonArray.get(0)).getString("version_number");
            } else {
                plugin.getLogger().severe("=================================");
                plugin.getLogger().severe(String.format("Failed to retrieve latest version of %s from Modrinth!", name));
                plugin.getLogger().severe("\"version_number\" is missing or status didn't return OK");
                plugin.getLogger().severe("=================================");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("=================================");
            plugin.getLogger().severe(String.format("Failed to retrieve latest version of %s from Modrinth!", name));
            e.printStackTrace();
            plugin.getLogger().severe("=================================");
        }
        return null;
    }

    private boolean isNewerVersion(String latestVersion, String currentVersion) {

        // Unfortunately, we would have to support old versions.
        // Will be deprecated before v1.1.0
        latestVersion = latestVersion
                .replaceAll("beta", "")
                .replaceAll("alpha", "")
                .replaceAll("private", "")
                .replaceAll("-", "")
                .replaceAll("v", "");

        currentVersion = currentVersion
                .replaceAll("beta", "")
                .replaceAll("alpha", "")
                .replaceAll("private", "")
                .replaceAll("-", "")
                .replaceAll("v", "");

        String[] latestParts = latestVersion.split("\\+")[0].split("-");
        String[] currentParts = currentVersion.split("\\+")[0].split("-");

        String[] latestVersionParts = latestParts[0].split("\\.");
        String[] currentVersionParts = currentParts[0].split("\\.");

        int maxLength = Math.max(latestVersionParts.length, currentVersionParts.length);

        for (int i = 0; i < maxLength; i++) {
            int latestPart = i < latestVersionParts.length ? Integer.parseInt(latestVersionParts[i]) : 0;
            int currentPart = i < currentVersionParts.length ? Integer.parseInt(currentVersionParts[i]) : 0;

            if (latestPart > currentPart) {
                return true;
            }

            if (latestPart < currentPart) {
                return false;
            }
        }

        // Handle pre-release versions (e.g., 1.0.0-alpha)
        if (latestParts.length > 1 && currentParts.length == 1) {
            // A pre-release version is always less than a final version
            return false;
        }
        if (currentParts.length > 1 && latestParts.length == 1) {
            // A final version is always greater than a pre-release version
            return true;
        }
        if (latestParts.length > 1 && currentParts.length > 1) {
            // Compare pre-release versions lexicographically
            return latestParts[1].compareTo(currentParts[1]) > 0;
        }

        return false;
    }

}
