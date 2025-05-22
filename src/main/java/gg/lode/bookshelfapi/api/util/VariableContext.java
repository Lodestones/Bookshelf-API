package gg.lode.bookshelfapi.api.util;

import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableContext {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<([^<>]+?)(?::([a-zA-Z]))?>");
    private final Map<String, String> variables = new HashMap<>();

    // Define or update a variable
    public void set(String key, Object value) {
        variables.put(key, String.valueOf(value));
    }

    // Get a variable value
    public String get(String key) {
        return variables.get(key);
    }

    // Replace <var[:formatter]> recursively
    public String replace(String input) {
        String result = input;
        boolean replaced;

        do {
            replaced = false;
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(result);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String key = matcher.group(1);
                String format = matcher.group(2);

                // Recursively resolve inner variables in the key itself
                String resolvedKey = replace(key);
                String value = variables.get(resolvedKey);

                if (value != null) {
                    value = applyFormatter(value, format);
                    matcher.appendReplacement(buffer, Matcher.quoteReplacement(value));
                    replaced = true;
                }
            }

            matcher.appendTail(buffer);
            result = buffer.toString();
        } while (replaced);

        return result;
    }

    private String applyFormatter(String value, String format) {
        if (format == null) return value;

        return switch (format.toLowerCase()) {
            case "u" -> value.toUpperCase();
            case "l" -> value.toLowerCase();
            // Add more formatters here as needed
            default -> value;
        };
    }

    public Component replaceAsComponent(String input) {
        String result = replace(input);
        return MiniMessageUtil.deserialize(result);
    }
}
