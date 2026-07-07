package gg.lode.bookshelfapi.api.compat;

import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class PlaceholderAPICompat {


    /**
     * Resolves placeholders in a single string. Returns the input unchanged when
     * PlaceholderAPI is not installed or resolution fails.
     */
    public static String setPlaceholders(Player player, String line) {
        return setPlaceholders(player, List.of(line)).get(0);
    }

    public static List<String> setPlaceholders(Player player, List<String> lines) {
        try {
            Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method setPlaceholders = papi.getMethod("setPlaceholders", Player.class, String.class);
            lines = lines.stream()
                    .map(line -> {
                        try {
                            // static method → pass null as the target instance
                            return (String) setPlaceholders.invoke(null, player, line);
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            // fallback to the original line on any invocation error
                            return line;
                        }
                    })
                    .collect(Collectors.toList());

            return lines;
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return lines;
        }
    }

}
