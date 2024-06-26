package to.lodestone.bookshelfapi.api.mojang;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    public static MojangProfile getMojangProfile(String username) throws IOException, ParseException {
        String apiEndpoint = "https://api.mojang.com/users/profiles/minecraft/" + username;
        URL url = new URL(apiEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        JSONParser parser = new JSONParser();

        MojangProfile mojangProfile = null;
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String jsonResponse = response.toString();
            JSONObject object = (JSONObject) parser.parse(jsonResponse);
            mojangProfile = new MojangProfile(object.get("id").toString(), object.get("name").toString());
            reader.close();
        }

        return mojangProfile;
    }
}