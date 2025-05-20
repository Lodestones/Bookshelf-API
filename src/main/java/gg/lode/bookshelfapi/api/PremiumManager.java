package gg.lode.bookshelfapi.api;

import org.bukkit.event.Listener;

import java.net.HttpURLConnection;
import java.net.URL;

public class PremiumManager implements Listener {
    private final static String LICENSE_URL = "https://lode.gg/api/license/verify";
    private final boolean isLicensedServer;

    public PremiumManager(String licenseKey, String id, int port) {
        boolean isLicensedServer;

        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            String ip = new String(conn.getInputStream().readAllBytes());
            try {
                isLicensedServer = checkStatus(LICENSE_URL + String.format("?ip=%s&key=%s&id=%s&port=%s", ip, licenseKey, id, port));
            } catch (Exception e) {
                isLicensedServer = true;
            }
        } catch (Exception ignored) {
            isLicensedServer = false;
        }

        this.isLicensedServer = isLicensedServer;
    }

    public boolean isLicensedServer() {
        return isLicensedServer;
    }

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
