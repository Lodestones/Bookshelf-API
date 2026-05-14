package gg.lode.bookshelfapi.api.mojang;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MojangProfile {
    private final UUID uuid;
    private final String username;

    public MojangProfile(String uuid, String username) {
        this.uuid = formatUUID(uuid);
        this.username = username;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return username;
    }

    private UUID formatUUID(String rawUUID) {
        StringBuilder formattedUUID = new StringBuilder(rawUUID);
        formattedUUID.insert(8, '-');
        formattedUUID.insert(13, '-');
        formattedUUID.insert(18, '-');
        formattedUUID.insert(23, '-');
        return UUID.fromString(formattedUUID.toString());
    }

    public static MojangProfile getMojangProfile(String username) throws IOException {
        return fetch("https://api.mojang.com/users/profiles/minecraft/" + username);
    }

    public static MojangProfile getMojangProfileFromUUID(String uuid) throws IOException {
        return fetch("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-", ""));
    }

    private static MojangProfile fetch(String apiEndpoint) throws IOException {
        URL url = new URL(apiEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
        }
        try {
            JSONObject object = new JSONObject(response.toString());
            return new MojangProfile(object.getString("id"), object.getString("name"));
        } catch (JSONException e) {
            throw new IOException("Malformed Mojang response: " + e.getMessage(), e);
        }
    }
}
