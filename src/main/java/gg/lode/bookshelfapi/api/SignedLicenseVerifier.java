package gg.lode.bookshelfapi.api;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Verifies an Ed25519-signed license response from {@code lode.gg/api/license/verify}.
 * <p>
 * Server signs `pluginId|ip|port|nonce|expiresAt|valid` (UTF-8) with its private key
 * and returns {@code { success, payload, signature }}. Plugin embeds the matching
 * raw 32-byte Ed25519 public key (base64) and verifies before trusting the result.
 * <p>
 * Generate a keypair with {@code LodestoneV3/scripts/generate-license-keypair.js}.
 * Set the private key as the {@code LICENSE_SIGN_PRIVATE_KEY_PEM} env var on the
 * server, paste the raw public key into each plugin.
 * <p>
 * If {@code publicKeyB64} is empty, falls back to legacy behaviour (HTTP 200 +
 * {@code success:true}) so plugins can ship before the signing key is deployed.
 */
public final class SignedLicenseVerifier {

    private static final byte[] ED25519_SPKI_PREFIX = {
            0x30, 0x2A, 0x30, 0x05, 0x06, 0x03, 0x2B, 0x65, 0x70, 0x03, 0x21, 0x00
    };

    private SignedLicenseVerifier() {}

    @SuppressWarnings("deprecation")
    public static boolean verify(String pluginId, int port, String publicKeyB64) {
        HttpURLConnection ipConn = null;
        HttpURLConnection verifyConn = null;
        try {
            URL ipUrl = new URL("https://api.ipify.org");
            ipConn = (HttpURLConnection) ipUrl.openConnection();
            ipConn.setRequestMethod("GET");
            ipConn.setRequestProperty("User-Agent", "Mozilla/5.0");
            ipConn.setConnectTimeout(5000);
            ipConn.setReadTimeout(5000);
            String ip;
            try (InputStream is = ipConn.getInputStream()) {
                ip = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            }

            byte[] nonceBytes = new byte[16];
            new SecureRandom().nextBytes(nonceBytes);
            String nonce = HexFormat.of().formatHex(nonceBytes);

            URL verifyUrl = new URL(String.format(
                    "https://lode.gg/api/license/verify?ip=%s&id=%s&port=%s&nonce=%s",
                    ip, pluginId, port, nonce));
            verifyConn = (HttpURLConnection) verifyUrl.openConnection();
            verifyConn.setRequestMethod("GET");
            verifyConn.setConnectTimeout(5000);
            verifyConn.setReadTimeout(5000);
            int code = verifyConn.getResponseCode();
            String body;
            try (InputStream is = code == HttpURLConnection.HTTP_OK
                    ? verifyConn.getInputStream() : verifyConn.getErrorStream()) {
                body = is == null ? "" : new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            if (publicKeyB64 == null || publicKeyB64.isEmpty()) {
                return code == HttpURLConnection.HTTP_OK
                        && new JSONObject(body).optBoolean("success", false);
            }

            JSONObject json = new JSONObject(body);
            JSONObject payload = json.optJSONObject("payload");
            String signature = json.optString("signature", "");
            if (payload == null || signature.isEmpty()) return false;

            if (!pluginId.equals(payload.optString("pluginId"))) return false;
            if (!ip.equals(payload.optString("ip"))) return false;
            if (!String.valueOf(port).equals(payload.optString("port"))) return false;
            if (!nonce.equals(payload.optString("nonce"))) return false;
            long expiresAt = payload.optLong("expiresAt", 0L);
            if (expiresAt <= System.currentTimeMillis()) return false;
            boolean valid = payload.optBoolean("valid", false);
            if (!valid) return false;

            String canonical = pluginId + "|" + ip + "|" + port + "|" + nonce + "|"
                    + expiresAt + "|" + valid;
            return verifyEd25519(canonical, signature, publicKeyB64);
        } catch (Exception e) {
            return false;
        } finally {
            if (ipConn != null) ipConn.disconnect();
            if (verifyConn != null) verifyConn.disconnect();
        }
    }

    private static boolean verifyEd25519(String canonicalMsg, String sigB64, String publicKeyB64) {
        try {
            byte[] raw = Base64.getDecoder().decode(publicKeyB64);
            if (raw.length != 32) return false;
            byte[] spki = new byte[ED25519_SPKI_PREFIX.length + 32];
            System.arraycopy(ED25519_SPKI_PREFIX, 0, spki, 0, ED25519_SPKI_PREFIX.length);
            System.arraycopy(raw, 0, spki, ED25519_SPKI_PREFIX.length, 32);
            PublicKey pub = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(spki));
            Signature sig = Signature.getInstance("Ed25519");
            sig.initVerify(pub);
            sig.update(canonicalMsg.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(sigB64));
        } catch (Exception e) {
            return false;
        }
    }
}
