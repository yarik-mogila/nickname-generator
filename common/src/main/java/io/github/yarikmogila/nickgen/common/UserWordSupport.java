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

        if (random.nextBoolean()) {
            return userWord + candidate;
        }
        return candidate + userWord;
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
}
