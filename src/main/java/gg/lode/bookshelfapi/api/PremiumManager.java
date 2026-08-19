package gg.lode.bookshelfapi.api;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PremiumManager implements Listener {
    private final static String LICENSE_URL = "https://lode.gg/api/license/verify";
    private final boolean isLicensedServer;

    @Deprecated(forRemoval = true, since = "1.1.63")
    public PremiumManager(@Nullable String ignored, String id, int port) {
        this(id, port);
    }

    /**
     * Identify this server by the per-buyer licence key its loader stamped in.
     *
     * <p>The public-address lookup this replaced needed a third-party service
     * to discover the address and then matched on it, so every buyer whose host
     * reassigns addresses between sessions looked like an unregistered server.
     * Worse, a failure to reach that service was itself read as "unlicensed".
     *
     * <p>Only a definitive answer counts against a server now. No key to
     * present, lode.gg unreachable, or a server error are all "cannot say",
     * which reports licensed — entitlement is settled before a premium plugin's
     * code reaches the server at all.
     */
    public PremiumManager(String id, int port) {
        String key = stampedLicenseKey();
        boolean licensed = true;
        if (key != null) {
            HttpURLConnection conn = null;
            try {
                @SuppressWarnings("deprecation")
                URL url = new URL(LICENSE_URL + "?id=" + id);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-License-Key", key);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int code = conn.getResponseCode();
                if (code >= 400 && code < 500) licensed = false;
            } catch (Exception cannotSay) {
                licensed = true;
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        this.isLicensedServer = licensed;
    }

    /** The licence key the loader stamped into this jar, or null. */
    private static String stampedLicenseKey() {
        try (InputStream in = PremiumManager.class.getResourceAsStream("/cloud/license.key")) {
            if (in == null) return null;
            String key = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            return key.isEmpty() ? null : key;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isLicensedServer() {
        return isLicensedServer;
    }
}
