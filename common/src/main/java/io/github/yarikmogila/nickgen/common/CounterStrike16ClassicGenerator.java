package io.github.yarikmogila.nickgen.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

final class CounterStrike16ClassicGenerator implements NicknameProfileGenerator {

    private static final CounterStrike16Config CONFIG = CounterStrike16Config.loadDefault();

    private final CounterStrike16Config config;

    CounterStrike16ClassicGenerator() {
        this(CONFIG);
    }

    private CounterStrike16ClassicGenerator(CounterStrike16Config config) {
        this.config = config;
    }

    @Override
    public String id() {
        return StandardNicknameGenerators.COUNTER_STRIKE_16_CLASSIC;
    }

    @Override
    public String displayName() {
        return config.displayName();
    }

    @Override
    public String description() {
        return config.description();
    }

    @Override
    public String generateCandidate(NicknameRequestContext context, Random random) {
        int tokenCount = randomBetween(random, config.tokenMin(), config.tokenMax());
        String separator = pick(config.separators(), random);
        if (isAggressiveSeparator(separator)) {
            tokenCount = Math.min(tokenCount, 2);
        }

        List<String> tokens = new ArrayList<>(tokenCount);

        for (int index = 0; index < tokenCount; index++) {
            String source = pickSourceToken(random);
            String transformed = transformToken(source, random);
            if (!transformed.isBlank()) {
                tokens.add(transformed);
            }
        }

        if (tokens.isEmpty()) {
            tokens.add(transformToken(pick(config.coreWords(), random), random));
        }

        if (tokens.size() > 1 && containsHardSeparator(tokens.get(0))) {
            tokens = List.of(tokens.get(0), normalizeSecondaryToken(tokens.get(1)));
            separator = "_";
        }

        if (tokens.size() > 1 && isAggressiveSeparator(separator) && containsHardSeparator(tokens.get(1))) {
            separator = "_";
        }

        String nickname = String.join(separator, tokens);

        nickname = maybeInjectMathSymbol(nickname, separator, random);
        nickname = maybeWrapDecorations(nickname, random);
        nickname = maybeAppendNumber(nickname, random);

        return nickname;
    }

    private String pickSourceToken(Random random) {
        int roll = random.nextInt(100);
        if (roll < config.coreChancePercent()) {
            return pick(config.coreWords(), random);
        }
        roll -= config.coreChancePercent();
        if (roll < config.gameChancePercent()) {
            return pick(config.gameWords(), random);
        }
        roll -= config.gameChancePercent();
        if (roll < config.memeChancePercent()) {
            return pick(config.memeWords(), random);
        }
        return pick(config.randomWords(), random);
    }

    private String transformToken(String token, Random random) {
        String transformed = token;
        transformed = maybeMixScripts(transformed, random);
        transformed = maybeLeetReplace(transformed, random);
        transformed = applyCasePattern(transformed, random);
        return transformed;
    }

    private String maybeMixScripts(String token, Random random) {
        StringBuilder builder = new StringBuilder(token.length());
        for (int index = 0; index < token.length(); index++) {
            char symbol = token.charAt(index);
            if (random.nextInt(100) >= config.scriptMixChancePercent()) {
                builder.append(symbol);
                continue;
            }

            Character latinToCyr = config.latinToCyrMap().get(symbol);
            if (latinToCyr != null) {
                builder.append(latinToCyr);
                continue;
            }

            Character cyrToLatin = config.cyrToLatinMap().get(symbol);
            if (cyrToLatin != null) {
                builder.append(cyrToLatin);
                continue;
            }

            builder.append(symbol);
        }
        return builder.toString();
    }

    private String maybeLeetReplace(String token, Random random) {
        StringBuilder builder = new StringBuilder(token.length());
        for (int index = 0; index < token.length(); index++) {
            char symbol = token.charAt(index);
            char lower = Character.toLowerCase(symbol);
            Character replacement = config.leetMap().get(lower);
            if (replacement != null && random.nextInt(100) < config.leetChancePercent()) {
                builder.append(replacement);
            } else {
                builder.append(symbol);
            }
        }
        return builder.toString();
    }

    private String applyCasePattern(String token, Random random) {
        int totalWeight = config.caseWeightUpper() + config.caseWeightLower()
                + config.caseWeightAlternating() + config.caseWeightContrast();
        int roll = random.nextInt(totalWeight);

        if (roll < config.caseWeightUpper()) {
            return token.toUpperCase(Locale.ROOT);
        }
        roll -= config.caseWeightUpper();

        if (roll < config.caseWeightLower()) {
            return token.toLowerCase(Locale.ROOT);
        }
        roll -= config.caseWeightLower();

        if (roll < config.caseWeightAlternating()) {
            return alternatingCase(token);
        }

        return contrastCase(token);
    }

    private String alternatingCase(String token) {
        StringBuilder builder = new StringBuilder(token.length());
        boolean upper = true;
        for (int index = 0; index < token.length(); index++) {
            char symbol = token.charAt(index);
            if (!Character.isLetter(symbol)) {
                builder.append(symbol);
                continue;
            }
            builder.append(upper ? Character.toUpperCase(symbol) : Character.toLowerCase(symbol));
            upper = !upper;
        }
        return builder.toString();
    }

    private String contrastCase(String token) {
        if (token.length() < 2) {
            return token.toUpperCase(Locale.ROOT);
        }

        int split = token.length() / 2;
        String head = token.substring(0, split).toLowerCase(Locale.ROOT);
        String tail = token.substring(split).toUpperCase(Locale.ROOT);
        return head + tail;
    }

    private String maybeInjectMathSymbol(String nickname, String separator, Random random) {
        if (separator.isEmpty()) {
            return nickname;
        }
        if (random.nextInt(100) >= config.mathSymbolChancePercent()) {
            return nickname;
        }

        int separatorIndex = nickname.indexOf(separator);
        if (separatorIndex < 0) {
            return nickname;
        }

        String symbol = pick(config.mathSymbols(), random);
        return nickname.substring(0, separatorIndex) + symbol + nickname.substring(separatorIndex + separator.length());
    }

    private String maybeWrapDecorations(String nickname, Random random) {
        if (random.nextInt(100) >= config.decorationChancePercent()) {
            return nickname;
        }

        String decoration = pick(config.decorations(), random);
        return decoration + nickname + decoration;
    }

    private String maybeAppendNumber(String nickname, Random random) {
        if (random.nextInt(100) >= config.numberChancePercent()) {
            return nickname;
        }

        int number = randomBetween(random, config.numberMin(), config.numberMax());
        return nickname + number;
    }

    private int randomBetween(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private boolean isAggressiveSeparator(String separator) {
        return "/".equals(separator) || "|".equals(separator);
    }

    private boolean containsHardSeparator(String token) {
        return token.contains("/") || token.contains("|") || token.contains("::") || token.contains("__");
    }

    private String normalizeSecondaryToken(String token) {
        if (token == null || token.isBlank()) {
            return "X";
        }
        return token.replace('/', '_').replace('|', '_').replace(':', '_');
    }

    private String pick(List<String> values, Random random) {
        return values.get(random.nextInt(values.size()));
    }

    private record CounterStrike16Config(
            String displayName,
            String description,
            List<String> coreWords,
            List<String> memeWords,
            List<String> gameWords,
            List<String> randomWords,
            List<String> separators,
            List<String> decorations,
            List<String> mathSymbols,
            Map<Character, Character> leetMap,
            Map<Character, Character> latinToCyrMap,
            Map<Character, Character> cyrToLatinMap,
            int tokenMin,
            int tokenMax,
            int coreChancePercent,
            int memeChancePercent,
            int gameChancePercent,
            int scriptMixChancePercent,
            int leetChancePercent,
            int decorationChancePercent,
            int mathSymbolChancePercent,
            int numberChancePercent,
            int numberMin,
            int numberMax,
            int caseWeightUpper,
            int caseWeightLower,
            int caseWeightAlternating,
            int caseWeightContrast
    ) {
        private static final String RESOURCE_PATH = "/generators/cs16-classic.properties";

        private static CounterStrike16Config loadDefault() {
            Properties properties = ConfigResourceSupport.loadProperties(RESOURCE_PATH);

            String displayName = ConfigResourceSupport.requiredString(properties, "displayName");
            String description = ConfigResourceSupport.requiredString(properties, "description");

            List<String> coreWords = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "coreWordsFile")
            );
            List<String> memeWords = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "memeWordsFile")
            );
            List<String> gameWords = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "gameWordsFile")
            );
            List<String> randomWords = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "randomWordsFile")
            );
            List<String> separators = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "separatorsFile")
            );
            List<String> decorations = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "decorationsFile")
            );
            List<String> mathSymbols = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "mathSymbolsFile")
            );

            Map<Character, Character> leetMap = parseCharMap(
                    ConfigResourceSupport.requiredString(properties, "leetMap")
            );
            Map<Character, Character> latinToCyrMap = parseCharMap(
                    ConfigResourceSupport.requiredString(properties, "latinToCyrMap")
            );
            Map<Character, Character> cyrToLatinMap = parseCharMap(
                    ConfigResourceSupport.requiredString(properties, "cyrToLatinMap")
            );

            int tokenMin = ConfigResourceSupport.requiredInt(properties, "tokenMin", 1, 10);
            int tokenMax = ConfigResourceSupport.requiredInt(properties, "tokenMax", tokenMin, 10);

            int coreChancePercent = ConfigResourceSupport.requiredPercent(properties, "coreChancePercent");
            int memeChancePercent = ConfigResourceSupport.requiredPercent(properties, "memeChancePercent");
            int gameChancePercent = ConfigResourceSupport.requiredPercent(properties, "gameChancePercent");
            if (coreChancePercent + memeChancePercent + gameChancePercent > 100) {
                throw new IllegalStateException("core/meme/game chances must be <= 100 in total");
            }

            int scriptMixChancePercent = ConfigResourceSupport.requiredPercent(properties, "scriptMixChancePercent");
            int leetChancePercent = ConfigResourceSupport.requiredPercent(properties, "leetChancePercent");
            int decorationChancePercent = ConfigResourceSupport.requiredPercent(properties, "decorationChancePercent");
            int mathSymbolChancePercent = ConfigResourceSupport.requiredPercent(properties, "mathSymbolChancePercent");
            int numberChancePercent = ConfigResourceSupport.requiredPercent(properties, "numberChancePercent");

            int numberMin = ConfigResourceSupport.requiredInt(properties, "numberMin", 0, Integer.MAX_VALUE);
            int numberMax = ConfigResourceSupport.requiredInt(properties, "numberMax", numberMin, Integer.MAX_VALUE);

            int caseWeightUpper = ConfigResourceSupport.requiredInt(properties, "caseWeightUpper", 0, 1000);
            int caseWeightLower = ConfigResourceSupport.requiredInt(properties, "caseWeightLower", 0, 1000);
            int caseWeightAlternating = ConfigResourceSupport.requiredInt(properties, "caseWeightAlternating", 0, 1000);
            int caseWeightContrast = ConfigResourceSupport.requiredInt(properties, "caseWeightContrast", 0, 1000);
            if (caseWeightUpper + caseWeightLower + caseWeightAlternating + caseWeightContrast < 1) {
                throw new IllegalStateException("At least one case weight must be > 0");
            }

            return new CounterStrike16Config(
                    displayName,
                    description,
                    coreWords,
                    memeWords,
                    gameWords,
                    randomWords,
                    separators,
                    decorations,
                    mathSymbols,
                    leetMap,
                    latinToCyrMap,
                    cyrToLatinMap,
                    tokenMin,
                    tokenMax,
                    coreChancePercent,
                    memeChancePercent,
                    gameChancePercent,
                    scriptMixChancePercent,
                    leetChancePercent,
                    decorationChancePercent,
                    mathSymbolChancePercent,
                    numberChancePercent,
                    numberMin,
                    numberMax,
                    caseWeightUpper,
                    caseWeightLower,
                    caseWeightAlternating,
                    caseWeightContrast
            );
        }

        private static Map<Character, Character> parseCharMap(String raw) {
            String[] pairs = raw.split(",");
            Map<Character, Character> mapping = new HashMap<>();
            for (String pair : pairs) {
                String trimmed = pair.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] parts = trimmed.split(":");
                if (parts.length != 2 || parts[0].length() != 1 || parts[1].length() != 1) {
                    throw new IllegalStateException("Invalid mapping entry: " + trimmed);
                }
                mapping.put(parts[0].charAt(0), parts[1].charAt(0));
            }

            if (mapping.isEmpty()) {
                throw new IllegalStateException("Mapping must contain at least one entry");
            }

            return Map.copyOf(mapping);
        }
    }
}
