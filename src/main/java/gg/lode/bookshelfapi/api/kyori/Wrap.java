package gg.lode.bookshelfapi.api.kyori;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that wraps a string to a list of string to compensate the desired length per line.
 * If you wish, you can even use "\n" to force break a line.
 *
 * @author John Aquino.
 */
public class Wrap {

    private static final Pattern MATCH_PATTERN = Pattern.compile("<([^<]+)>|([^ <]+ ?)");

    private final String startingString;
    private final int maxLengthPerLine;

    private int currentLength = 0;
    private String currentTags = "";

    private Wrap(String str, int maxLengthPerLine) {
        this.startingString = str;
        this.maxLengthPerLine = maxLengthPerLine;
    }

    public static Wrap of(String str, int maxLengthPerLine) {
        return new Wrap(str, maxLengthPerLine);
    }

    public List<String> get() {
        if (this.startingString.isEmpty()) return List.of("");
        String[] lines = this.process(this.startingString).split("\\{NEWLINE\\}");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].replaceAll("\\s+$", "");
        }
        return List.of(lines);
    }

    private String process(String word) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = MATCH_PATTERN.matcher(word);
        while (matcher.find()) {
            String replacement;
            if (matcher.group(1) != null) {
                replacement = this.processTag(matcher);
            } else {
                replacement = this.processWord(matcher);
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String processWord(Matcher matcher) {
        StringBuilder result = new StringBuilder();
        String toProcess = matcher.group();
        String[] newLineSplit = toProcess.split("\\R"); // Splits on newlines
        for (int i = 0; i < newLineSplit.length; i++) {
            if (i > 0) {
                result.append("{NEWLINE}");
                this.currentLength = 0;
                this.currentTags = "";
            }

            int newWordLength = newLineSplit[i].length();
            if (newWordLength > this.maxLengthPerLine && this.currentLength == 0) {
                result.append(newLineSplit[i]).append("{NEWLINE}").append(this.currentTags);
            } else if (this.currentLength + newWordLength > this.maxLengthPerLine) {
                result.append("{NEWLINE}").append(this.currentTags).append(newLineSplit[i]);
                this.currentLength = newWordLength;
            } else {
                result.append(newLineSplit[i]);
                this.currentLength += newWordLength;
            }
        }

        if (toProcess.endsWith("\n")) {
            this.currentLength = 0;
            this.currentTags = "";
            while (toProcess.endsWith("\n")) {
                result.append("{NEWLINE}");
                toProcess = toProcess.substring(0, toProcess.length() - 1);
            }
        }

        return result.toString();
    }

    private String processTag(Matcher matcher) {
        String embeddedKey = matcher.group(1);
        switch (embeddedKey.toLowerCase(Locale.ROOT)) {
            case "bold", "b", "!bold", "!b", // ignores any kyori adventure formatting
                    "italic", "em", "i", "!italic", "!em", "!i",
                    "underlined", "u", "!underlined", "!u",
                    "strikethrough", "st", "!strikethrough", "!st",
                    "obfuscated", "obf", "!obfuscated", "!obf" -> {
                this.currentTags = this.currentTags.concat(matcher.group());
            }
            default -> this.currentTags = matcher.group();
        }
        return matcher.group();
    }
}
