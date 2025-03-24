package to.lodestone.bookshelfapi.api.util;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.*?)\\}");
    private static final Map<Character, Integer> romanValues = new HashMap<>();

    static {
        romanValues.put('I', 1);
        romanValues.put('V', 5);
        romanValues.put('X', 10);
        romanValues.put('L', 50);
        romanValues.put('C', 100);
        romanValues.put('D', 500);
        romanValues.put('M', 1000);
    }

    /**
     * Encodes a list of strings into a Base64 string.
     *
     * @param list The list of strings to encode.
     * @return Base64 encoded string.
     */
    public static String encodeListToBase64(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        String joined = String.join("\n", list);
        return Base64.getEncoder().encodeToString(joined.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Formats a string with the given arguments.
     *
     * @param str  The string to format.
     * @param args The arguments to replace the placeholders with.
     * @return The formatted string.
     */
    public static String format(String str, Object... args) {
        if (str == null || args == null || args.length == 0) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        int length = str.length();

        for (int i = 0; i < length; i++) {
            if (i < length - 2 && str.charAt(i) == '{' && Character.isDigit(str.charAt(i + 1)) && str.charAt(i + 2) == '}') {
                int index = str.charAt(i + 1) - '0'; // Convert '0' - '9' to int index
                if (index >= 0 && index < args.length) {
                    result.append(args[index]);
                    i += 2; // Skip past `{X}`
                    continue;
                }
            }
            result.append(str.charAt(i));
        }

        return result.toString();
    }

    /**
     * Formats a string with the given values.
     *
     * @param str    The string to format.
     * @param values The values to replace the placeholders with.
     * @return The formatted string.
     */
    public static String format(String str, Map<String, Object> values) {
        if (str == null || values == null || values.isEmpty()) {
            return str;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1); // Extract key inside `{ }`
            Object value = values.get(key);
            matcher.appendReplacement(result, value != null ? value.toString() : matcher.group(0));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Decodes a Base64 string back into a list of strings.
     *
     * @param base64 The Base64 encoded string.
     * @return The decoded list of strings.
     */
    public static List<String> decodeBase64ToList(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return List.of();
        }
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        return Arrays.asList(decodedString.split("\n"));
    }

    public static String titleCase(String sentence, boolean removeUnderscore) {
        StringBuilder titleCaseSentence = new StringBuilder();
        String[] words = sentence.toLowerCase().replaceAll("_", removeUnderscore ? " " : "_").split(" ");

        for (String word : words) {
            if (!word.isEmpty()) {
                String firstLetter = word.substring(0, 1).toUpperCase();
                String restOfWord = word.substring(1);
                titleCaseSentence.append(firstLetter).append(restOfWord).append(" ");
            }
        }

        return titleCaseSentence.toString().trim();
    }

    public static int romanToInt(String s) {
        int result = 0;
        int prevValue = 0;

        for (int i = s.length() - 1; i >= 0; i--) {
            int value = romanValues.get(s.charAt(i));
            if (value < prevValue) {
                result -= value;
            } else {
                result += value;
            }
            prevValue = value;
        }

        return result;
    }

    public static String intToRoman(int num) {
        if (num < 1 || num > 3999)
            return String.valueOf(num);

        String[] romanSymbols = {"I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M"};
        int[] values = {1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000};

        StringBuilder result = new StringBuilder();

        int i = 12; // Start with the highest symbol
        while (num > 0) {
            int quotient = num / values[i];
            num %= values[i];
            while (quotient > 0) {
                result.append(romanSymbols[i]);
                quotient--;
            }
            i--; // Move to the next smaller symbol
        }

        return result.toString();
    }

    private static boolean startsWithVowel(String word) {
        return Pattern.compile("^[aeiou]", Pattern.CASE_INSENSITIVE).matcher(word).find();
    }

    public static double calculateSimilarity(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }

        int distance = dp[str1.length()][str2.length()];
        int maxLength = Math.max(str1.length(), str2.length());
        double similarity = 1 - (double) distance / maxLength;
        return similarity * 100;
    }

    public static String getTimeDuration(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String formattedTime;

        if (hours > 0) {
            formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            formattedTime = String.format("%02d:%02d", minutes, seconds);
        }

        return formattedTime;
    }

    public static String getTimeString(long milliseconds) {
        if (milliseconds < 0) {
            return "Invalid time";
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            long remainingMinutes = minutes % 60;
            return hours + " hour" + (hours > 1 ? "s" : "") +
                    (remainingMinutes > 0 ? " and " + remainingMinutes + " minute" + (remainingMinutes > 1 ? "s" : "") : "");
        }

        if (minutes > 0) {
            long remainingSeconds = seconds % 60;
            return minutes + " minute" + (minutes > 1 ? "s" : "") +
                    (remainingSeconds > 0 ? " and " + remainingSeconds + " second" + (remainingSeconds > 1 ? "s" : "") : "");
        }

        return seconds + " second" + (seconds != 1 ? "s" : "");
    }

}
