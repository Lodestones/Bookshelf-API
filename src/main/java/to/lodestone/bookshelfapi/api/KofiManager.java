package to.lodestone.bookshelfapi.api;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.net.HttpURLConnection;
import java.net.URL;

public class KofiManager implements Listener {
    private final static String URL = "https://lodestone.to/api/ko-fi/verify";

    private final boolean isKofiDonor;

    public KofiManager(@Nullable String email) {
        this.isKofiDonor = email != null && isKofiDonor(URL + String.format("?email=%s", email));
    }

    public boolean isKofiDonor() {
        return isKofiDonor;
    }

    @SuppressWarnings("deprecation")
    private boolean isKofiDonor(String urlString) {
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
