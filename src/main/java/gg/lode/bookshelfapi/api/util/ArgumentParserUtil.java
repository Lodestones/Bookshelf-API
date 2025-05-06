package gg.lode.bookshelfapi.api.util;

import java.util.*;

public class ArgumentParserUtil {
    
    public record ParsedArguments(String cleanedArgs, Map<String, String> flags) {
        public boolean hasFlag(String flag) {
            return flags.containsKey(flag);
        }

        public boolean hasFlag(String ...flags) {
            for (String flag : flags) {
                if (hasFlag(flag)) {
                    return true;
                }
            }
            return false;
        }

        public String getFlag(String flag) {
            return flags.get(flag);
        }
    }

    public static ParsedArguments parseArguments(String input) {
        return parseArguments(input, Collections.emptySet());
    }

    public static ParsedArguments parseArguments(String input, Set<String> validFlags) {
        List<String> cleanedArgs = new ArrayList<>();
        Map<String, String> flagMap = new HashMap<>();
        String[] args = input.split(" ");

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (validFlags.contains(arg)) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    flagMap.put(arg, args[i + 1]);
                    i++; // Skip the next argument since it's consumed as a value
                } else {
                    flagMap.put(arg, ""); // Flag exists but no parameter
                }
            } else {
                cleanedArgs.add(arg);
            }
        }

        return new ParsedArguments(String.join(" ", cleanedArgs), flagMap);
    }
}