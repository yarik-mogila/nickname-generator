package io.github.yarikmogila.nickgen.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

final class UserWordSupport {

    private static final Map<Character, Character> LEET_MAP = Map.ofEntries(
            Map.entry('a', '4'), Map.entry('e', '3'), Map.entry('i', '1'), Map.entry('o', '0'),
            Map.entry('s', '5'), Map.entry('t', '7'), Map.entry('b', '8'), Map.entry('z', '2'),
            Map.entry('а', '4'), Map.entry('е', '3'), Map.entry('о', '0'), Map.entry('с', '5'),
            Map.entry('т', '7'), Map.entry('в', '8'), Map.entry('з', '2')
    );

    enum UserWordPosition {
        SMART,
        START,
        END
    }

    enum UserWordStyle {
        PLAIN,
        MATCH
    }

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
        return applyUserWord(candidate, userWord, UserWordPosition.SMART, UserWordStyle.PLAIN, random);
    }

    static String applyUserWord(
            String candidate,
            String userWord,
            UserWordPosition position,
            Random random
    ) {
        return applyUserWord(candidate, userWord, position, UserWordStyle.PLAIN, random);
    }

    static String applyUserWord(
            String candidate,
            String userWord,
            UserWordPosition position,
            UserWordStyle style,
            Random random
    ) {
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

        UserWordPosition effectivePosition = position == null ? UserWordPosition.SMART : position;
        UserWordStyle effectiveStyle = style == null ? UserWordStyle.PLAIN : style;

        String[] parts = splitNumberSuffix(candidate);
        String base = parts[0];
        String numericSuffix = parts[1];

        if (base.isBlank()) {
            return stylizeUserWord(userWord, candidate, effectiveStyle, random) + numericSuffix;
        }

        if (base.contains("_")) {
            String[] tokens = base.split("_", -1);
            int index = resolveTokenIndex(effectivePosition, tokens.length, random);
            String reference = tokens[index].isBlank() ? base : tokens[index];
            tokens[index] = stylizeUserWord(userWord, reference, effectiveStyle, random);
            return String.join("_", tokens) + numericSuffix;
        }

        int secondTokenIndex = findSecondTokenStart(base);
        if (secondTokenIndex > 0) {
            String first = base.substring(0, secondTokenIndex);
            String tail = base.substring(secondTokenIndex);
            if (effectivePosition == UserWordPosition.SMART && random.nextBoolean()) {
                effectivePosition = UserWordPosition.END;
            }

            if (effectivePosition == UserWordPosition.END) {
                String styled = stylizeUserWord(userWord, first, effectiveStyle, random);
                String separator = needsSeparator(first, styled) ? "_" : "";
                return first + separator + styled + numericSuffix;
            }

            String styled = stylizeUserWord(userWord, first, effectiveStyle, random);
            String separator = needsSeparator(styled, tail) ? "_" : "";
            return styled + separator + tail + numericSuffix;
        }

        if (effectivePosition == UserWordPosition.END || (effectivePosition == UserWordPosition.SMART && random.nextBoolean())) {
            String styled = stylizeUserWord(userWord, base, effectiveStyle, random);
            String separator = needsSeparator(base, styled) ? "_" : "";
            return base + separator + styled + numericSuffix;
        }

        String styled = stylizeUserWord(userWord, base, effectiveStyle, random);
        String separator = needsSeparator(styled, base) ? "_" : "";
        return styled + separator + base + numericSuffix;
    }

    static UserWordPosition resolveUserWordPosition(Map<String, String> options) {
        if (options == null) {
            return UserWordPosition.SMART;
        }

        String raw = options.get(GenerationOptionKeys.USER_WORD_POSITION);
        if (raw == null || raw.isBlank()) {
            return UserWordPosition.SMART;
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "start", "prefix", "first" -> UserWordPosition.START;
            case "end", "suffix", "last" -> UserWordPosition.END;
            case "smart", "auto", "random" -> UserWordPosition.SMART;
            default -> throw new InvalidGenerationRequestException(
                    "Unsupported userWordPosition: " + raw + ". Allowed: start, end, smart"
            );
        };
    }

    static UserWordStyle resolveUserWordStyle(Map<String, String> options) {
        if (options == null) {
            return UserWordStyle.PLAIN;
        }

        String raw = options.get(GenerationOptionKeys.USER_WORD_STYLE);
        if (raw == null || raw.isBlank()) {
            return UserWordStyle.PLAIN;
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "plain", "raw" -> UserWordStyle.PLAIN;
            case "match", "styled", "style" -> UserWordStyle.MATCH;
            default -> throw new InvalidGenerationRequestException(
                    "Unsupported userWordStyle: " + raw + ". Allowed: plain, match"
            );
        };
    }

    private static String stylizeUserWord(
            String userWord,
            String reference,
            UserWordStyle style,
            Random random
    ) {
        if (style == UserWordStyle.PLAIN || reference == null || reference.isBlank()) {
            return userWord;
        }

        String leetAligned = alignLeet(userWord, reference, random);
        return alignCase(leetAligned, reference);
    }

    private static String alignLeet(String word, String reference, Random random) {
        int digitCount = 0;
        for (int index = 0; index < reference.length(); index++) {
            if (Character.isDigit(reference.charAt(index))) {
                digitCount++;
            }
        }

        if (digitCount == 0) {
            return word;
        }

        int chance = Math.min(85, 25 + digitCount * 15);
        StringBuilder builder = new StringBuilder(word.length());
        for (int index = 0; index < word.length(); index++) {
            char symbol = word.charAt(index);
            char lower = Character.toLowerCase(symbol);
            Character mapped = LEET_MAP.get(lower);
            if (mapped != null && random.nextInt(100) < chance) {
                builder.append(mapped);
            } else {
                builder.append(symbol);
            }
        }

        return builder.toString();
    }

    private static String alignCase(String word, String reference) {
        List<Boolean> referenceCasePattern = extractReferenceCasePattern(reference);
        if (referenceCasePattern.isEmpty()) {
            return word;
        }

        boolean allUpper = referenceCasePattern.stream().allMatch(Boolean::booleanValue);
        if (allUpper) {
            return word.toUpperCase(Locale.ROOT);
        }

        boolean allLower = referenceCasePattern.stream().noneMatch(Boolean::booleanValue);
        if (allLower) {
            return word.toLowerCase(Locale.ROOT);
        }

        if (isAlternating(referenceCasePattern)) {
            return applyAlternatingCase(word, referenceCasePattern.get(0));
        }

        return applyPatternCase(word, referenceCasePattern);
    }

    private static List<Boolean> extractReferenceCasePattern(String reference) {
        List<Boolean> pattern = new ArrayList<>();
        for (int index = 0; index < reference.length(); index++) {
            char symbol = reference.charAt(index);
            if (!Character.isLetter(symbol)) {
                continue;
            }
            pattern.add(Character.isUpperCase(symbol));
        }
        return pattern;
    }

    private static boolean isAlternating(List<Boolean> pattern) {
        if (pattern.size() < 3) {
            return false;
        }

        for (int index = 1; index < pattern.size(); index++) {
            if (pattern.get(index).equals(pattern.get(index - 1))) {
                return false;
            }
        }
        return true;
    }

    private static String applyAlternatingCase(String word, boolean startUpper) {
        StringBuilder builder = new StringBuilder(word.length());
        boolean upper = startUpper;
        for (int index = 0; index < word.length(); index++) {
            char symbol = word.charAt(index);
            if (!Character.isLetter(symbol)) {
                builder.append(symbol);
                continue;
            }
            builder.append(upper ? Character.toUpperCase(symbol) : Character.toLowerCase(symbol));
            upper = !upper;
        }
        return builder.toString();
    }

    private static String applyPatternCase(String word, List<Boolean> pattern) {
        StringBuilder builder = new StringBuilder(word.length());
        int letterIndex = 0;

        for (int index = 0; index < word.length(); index++) {
            char symbol = word.charAt(index);
            if (!Character.isLetter(symbol)) {
                builder.append(symbol);
                continue;
            }

            boolean makeUpper = pattern.get(letterIndex % pattern.size());
            builder.append(makeUpper ? Character.toUpperCase(symbol) : Character.toLowerCase(symbol));
            letterIndex++;
        }

        return builder.toString();
    }

    private static int resolveTokenIndex(UserWordPosition position, int tokenCount, Random random) {
        if (tokenCount < 2) {
            return 0;
        }
        if (position == UserWordPosition.START) {
            return 0;
        }
        if (position == UserWordPosition.END) {
            return tokenCount - 1;
        }
        return random.nextBoolean() ? 0 : tokenCount - 1;
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
