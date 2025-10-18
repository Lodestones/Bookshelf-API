package gg.lode.bookshelfapi.api.util;

import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableContext {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<<([a-zA-Z0-9_]+?)(?::([a-zA-Z]))?>>|<([a-zA-Z0-9_]+?)(?::([a-zA-Z]))?>");
    private final Map<String, String> variables;

    // Define or update a variable
    public void set(String key, Object value) {
        variables.put(key, String.valueOf(value));
    }

    // Get a variable value
    public String get(String key) {
        return variables.get(key);
    }

    public VariableContext() {
        this.variables = new HashMap<>();
    }

    public VariableContext(Map<String, String> variables) {
        this.variables = variables;
    }

    public static void main(String[] args) {
        VariableContext context = VariableContext.of();
        context.set("test", "Hello, World!");
        System.out.println(context.replace("<<test>>"));
    }

    public static VariableContext of() {
        return new VariableContext();
    }

    public static VariableContext of(String key, String value) {
        VariableContext ctx = new VariableContext();
        ctx.set(key, value);
        return ctx;
    }

    public VariableContext fromVariables(Map<String, String> values) {
        return new VariableContext(values);
    }

    public Map<String, String> getVariables() {
        return this.variables;
    }

    public VariableContext with(String key, String value) {
        set(key, value);
        return this;
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
        return MiniMessageHelper.deserialize(result);
    }

    // Replace <var[:formatter]> recursively
    public String replace(String input) {
        if (input == null) return "";

        String result = input;
        boolean replaced;
        int maxIterations = 10; // Prevent infinite loops
        int iterations = 0;

        do {
            replaced = false;
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(result);
            StringBuilder buffer = new StringBuilder();

            while (matcher.find()) {
                String key;
                String format;
                boolean isDoubleBracketed = matcher.group(1) != null;

                if (isDoubleBracketed) {
                    key = matcher.group(1);
                    format = matcher.group(2);
                } else {
                    key = matcher.group(3);
                    format = matcher.group(4);
                }

                String value = variables.get(key);
                if (value != null) {
                    value = applyFormatter(value, format);
                    if (isDoubleBracketed) {
                        value = "<" + value + ">";
                    }
                    matcher.appendReplacement(buffer, Matcher.quoteReplacement(value));
                    replaced = true;
                }
            }

            matcher.appendTail(buffer);
            result = buffer.toString();
            iterations++;
        } while (replaced && iterations < maxIterations);

        return result;
    }
}
