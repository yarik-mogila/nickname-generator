package io.github.yarikmogila.nickgen.common;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

final class UserWordSupport {

    private UserWordSupport() {
    }

    static String resolveUserWord(Map<String, String> options) {
        if (options == null) {
            return null;
        }

        String raw = options.get(GenerationOptionKeys.USER_WORD);
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = normalizeWord(raw);
        return normalized.isBlank() ? null : normalized;
    }

    static String applyUserWord(String candidate, String userWord, Random random) {
        if (candidate == null || candidate.isBlank()) {
            return candidate;
        }
        if (userWord == null || userWord.isBlank()) {
            return candidate;
        }

        String normalizedCandidate = candidate.toLowerCase(Locale.ROOT);
        String normalizedUserWord = userWord.toLowerCase(Locale.ROOT);
        if (normalizedCandidate.contains(normalizedUserWord)) {
            return candidate;
        }

        String[] parts = splitNumberSuffix(candidate);
        String base = parts[0];
        String numericSuffix = parts[1];

        if (base.isBlank()) {
            return userWord + numericSuffix;
        }

        if (base.contains("_")) {
            String[] tokens = base.split("_", -1);
            tokens[0] = userWord;
            return String.join("_", tokens) + numericSuffix;
        }

        int secondTokenIndex = findSecondTokenStart(base);
        if (secondTokenIndex > 0) {
            String tail = base.substring(secondTokenIndex);
            String separator = needsSeparator(userWord, tail) ? "_" : "";
            return userWord + separator + tail + numericSuffix;
        }

        String separator = needsSeparator(userWord, base) ? "_" : "";
        return userWord + separator + base + numericSuffix;
    }

    private static String normalizeWord(String rawWord) {
        String normalized = rawWord.trim().replace('_', ' ').replace('-', ' ');
        String[] parts = normalized.split("\\s+");

        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            result.append(capitalize(part));
        }
        return result.toString();
    }

    private static String capitalize(String value) {
        if (value.length() == 1) {
            return value.toUpperCase(Locale.ROOT);
        }
        String first = value.substring(0, 1).toUpperCase(Locale.ROOT);
        String tail = value.substring(1).toLowerCase(Locale.ROOT);
        return first + tail;
    }

    private static String[] splitNumberSuffix(String candidate) {
        int endOfBase = candidate.length();
        while (endOfBase > 0 && Character.isDigit(candidate.charAt(endOfBase - 1))) {
            endOfBase--;
        }
        return new String[]{candidate.substring(0, endOfBase), candidate.substring(endOfBase)};
    }

    private static int findSecondTokenStart(String base) {
        for (int index = 1; index < base.length(); index++) {
            char previous = base.charAt(index - 1);
            char current = base.charAt(index);
            if (Character.isLowerCase(previous) && Character.isUpperCase(current)) {
                return index;
            }
            if (Character.isDigit(previous) && Character.isLetter(current)) {
                return index;
            }
        }
        return -1;
    }

    private static boolean needsSeparator(String left, String right) {
        Character.UnicodeScript leftScript = firstLetterScript(left);
        Character.UnicodeScript rightScript = firstLetterScript(right);
        return leftScript != null && rightScript != null && leftScript != rightScript;
    }

    private static Character.UnicodeScript firstLetterScript(String value) {
        for (int index = 0; index < value.length(); index++) {
            char symbol = value.charAt(index);
            if (Character.isLetter(symbol)) {
                return Character.UnicodeScript.of(symbol);
            }
        }
        return null;
    }
}
