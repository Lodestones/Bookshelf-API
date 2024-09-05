package to.lodestone.bookshelfapi.api;

import org.bukkit.event.Listener;

import java.net.HttpURLConnection;
import java.net.URL;

public class PremiumManager implements Listener {
    private final static String URL = "https://lodestone.to/api/premium/verify";

    private final boolean isPremiumServer;

    public PremiumManager(String ip) {
        this.isPremiumServer = isPremiumServer(URL + String.format("?ip=%s", ip));
    }

    public boolean isPremiumServer() {
        return isPremiumServer;
    }

    @SuppressWarnings("deprecation")
    private boolean isPremiumServer(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
