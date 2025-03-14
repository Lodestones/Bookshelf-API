package to.lodestone.bookshelfapi.api;

import org.bukkit.event.Listener;

import java.net.HttpURLConnection;
import java.net.URL;

public class PremiumManager implements Listener {
    private final static String URL = "https://lodestone.to/api/premium/verify";
    private final static String LICENSE_URL = "https://lodestone.to/api/license/verify";

    private final boolean isPremiumServer;
    private boolean isLicensedServer;

    @SuppressWarnings("deprecation")
    public PremiumManager() {
        boolean isPremiumServer;
        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            String ip = new String(conn.getInputStream().readAllBytes());
            isPremiumServer = checkStatus(URL + String.format("?ip=%s", ip));
        } catch (Exception ignored) {
            isPremiumServer = false;
        }
        this.isPremiumServer = isPremiumServer;
        this.isLicensedServer = false;
    }

    @SuppressWarnings("deprecation")
    public PremiumManager(String licenseKey) {
        this();
        boolean isLicensedServer;
        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            String ip = new String(conn.getInputStream().readAllBytes());
            isLicensedServer = checkStatus(LICENSE_URL + String.format("?ip=%s&key=%s", ip, licenseKey));
        } catch (Exception ignored) {
            isLicensedServer = false;
        }

        this.isLicensedServer = isLicensedServer;
    }

    public boolean isLicensedServer() {
        return isLicensedServer;
    }

    public boolean isPremiumServer() {
        return isPremiumServer;
    }

    @SuppressWarnings("deprecation")
    private boolean checkStatus(String urlString) {
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
