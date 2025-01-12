package to.lodestone.bookshelfapi.api.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class ChatHeadUtil {
    private static final HashMap<UUID, String> cachedSkinUrls = new HashMap<>();

    public static String saturateColor(String primaryHex, String saturaterHex, double saturationFactor) {
        Color primaryColor = Color.decode(primaryHex);
        Color saturaterColor = Color.decode(saturaterHex);
        int red = (int) ((double) primaryColor.getRed() * (1.0 - saturationFactor) + (double) saturaterColor.getRed() * saturationFactor);
        int green = (int) ((double) primaryColor.getGreen() * (1.0 - saturationFactor) + (double) saturaterColor.getGreen() * saturationFactor);
        int blue = (int) ((double) primaryColor.getBlue() * (1.0 - saturationFactor) + (double) saturaterColor.getBlue() * saturationFactor);
        Color saturatedColor = new Color(red, green, blue);
        return String.format("#%02x%02x%02x", saturatedColor.getRed(), saturatedColor.getGreen(), saturatedColor.getBlue());
    }

    public static String _getHead(Player player) {
        return _getHead(player.getUniqueId(), null);
    }

    public static Component getHead(Player player) {
        return getHead(player.getUniqueId(), null);
    }

    public static Component getHead(Player player, String saturateWith) {
        return getHead(player.getUniqueId(), saturateWith);
    }

    public static Component getHead(UUID uuid) {
        return getHead(uuid, null);
    }

    public static String _getHead(UUID uuid, @Nullable String saturateWith) {
        String[] hexColors = getPixelColors(getPlayerSkinURL(uuid));
        if (hexColors.length < 64) {
            throw new IllegalArgumentException("Hex colors array must have at least 64 elements.");
        } else {
            String[][] components = new String[8][8];

            int col;
            int unicodeChar;
            for (int i = 0; i < 64; ++i) {
                int row = i / 8;
                col = i % 8;
                unicodeChar = (char) ('\uf000' + i % 8 + 1);
                String component = "<font:player_head>";
                if (saturateWith == null) {
                    component = component + String.format("<%s>", hexColors[i]);
                } else {
                    component = component + String.format("<%s>", saturateColor(hexColors[i], saturateWith, 0.8));
                }

                if (i != 7 && i != 15 && i != 23 && i != 31 && i != 39 && i != 47 && i != 55) {
                    if (i == 63) {
                        component = component + String.format("%s", (char) unicodeChar);
                    } else {
                        component = component + String.format("%s%s", (char) unicodeChar, "\uf102");
                    }
                } else {
                    component = component + String.format("%s%s", (char) unicodeChar, "\uf101");
                }

                components[row][col] = component;
            }

            StringBuilder componentToReturn = new StringBuilder();
            col = components.length;

            for (unicodeChar = 0; unicodeChar < col; ++unicodeChar) {
                String[] c = components[unicodeChar];
                int var10 = c.length;

                for (String cc : c) {
                    componentToReturn.append(cc);
                }
            }

            return componentToReturn.toString();
        }
    }

    public static Component getHead(UUID uuid, @Nullable String saturateWith) {
        String[] hexColors = getPixelColors(getPlayerSkinURL(uuid));
        if (hexColors.length < 64) {
            throw new IllegalArgumentException("Hex colors array must have at least 64 elements.");
        } else {
            Component[][] components = new Component[8][8];

            int col;
            int unicodeChar;
            for (int i = 0; i < 64; ++i) {
                int row = i / 8;
                col = i % 8;
                unicodeChar = (char) ('\uf000' + i % 8 + 1);
                Component component = Component.empty();
                if (i != 7 && i != 15 && i != 23 && i != 31 && i != 39 && i != 47 && i != 55) {
                    if (i == 63) {
                        component = component.append(MiniMessageUtil.deserialize("%s", (char) unicodeChar));
                    } else {
                        component = component.append(MiniMessageUtil.deserialize("%s%s", (char) unicodeChar, "\uf102"));
                    }
                } else {
                    component = component.append(MiniMessageUtil.deserialize("%s%s", (char) unicodeChar, "\uf101"));
                }

                if (saturateWith == null) {
                    component = component.color(TextColor.fromHexString(hexColors[i]));
                } else {
                    component = component.color(TextColor.fromHexString(saturateColor(hexColors[i], saturateWith, 0.8)));
                }

                component = component.font(Key.key("player_head"));
                components[row][col] = component;
            }

            Component componentToReturn = net.kyori.adventure.text.Component.empty();
            col = components.length;

            for (unicodeChar = 0; unicodeChar < col; ++unicodeChar) {
                Component[] c = components[unicodeChar];
                int var10 = c.length;

                for (Component cc : c) {
                    componentToReturn = componentToReturn.append(cc);
                }
            }

            return componentToReturn;
        }
    }

    private static String[] getPixelColors(String playerSkinUrl) {
        String[] colors = new String[64];

        try {
            BufferedImage skinImage = ImageIO.read(new URL(playerSkinUrl));
            int faceStartX = 8;
            int faceStartY = 8;
            int faceWidth = 8;
            int faceHeight = 8;
            BufferedImage faceImage = skinImage.getSubimage(faceStartX, faceStartY, faceWidth, faceHeight);
            int index = 0;

            for (int x = 0; x < faceHeight; ++x) {
                for (int y = 0; y < faceWidth; ++y) {
                    int rgb = faceImage.getRGB(x, y);
                    String hexColor = String.format("#%06X", rgb & 16777215);
                    colors[index++] = hexColor;
                }
            }
        } catch (IOException var13) {
            var13.printStackTrace();
        }

        return colors;
    }

    private static String getPlayerSkinURL(UUID uuid) {
        try {
            if (cachedSkinUrls.containsKey(uuid)) {
                return cachedSkinUrls.get(uuid);
            }

            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            String jsonResponse = response.toString();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray propertiesArray = jsonObject.getJSONArray("properties");

            for (int i = 0; i < propertiesArray.length(); ++i) {
                JSONObject property = propertiesArray.getJSONObject(i);
                if (property.getString("name").equals("textures")) {
                    String value = property.getString("value");
                    byte[] decodedBytes = Base64.getDecoder().decode(value);
                    String decodedValue = new String(decodedBytes);
                    JSONObject textureJson = new JSONObject(decodedValue);
                    String skinURL = textureJson.getJSONObject("textures").getJSONObject("SKIN").getString("url");
                    cachedSkinUrls.put(uuid, skinURL);
                    return skinURL;
                }
            }
        } catch (Exception var16) {
            var16.printStackTrace();
        }

        return "Unable to retrieve player skin URL.";
    }
}