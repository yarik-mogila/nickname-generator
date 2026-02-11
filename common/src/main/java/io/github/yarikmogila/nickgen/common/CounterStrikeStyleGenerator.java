package io.github.yarikmogila.nickgen.common;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

final class CounterStrikeStyleGenerator implements NicknameProfileGenerator {

    private static final CounterStrikeConfig CONFIG = CounterStrikeConfig.loadDefault();

    private final List<String> stems;
    private final List<String> suffixes;
    private final Map<Character, Character> leetMap;
    private final int leetChancePercent;
    private final int bareNumberChancePercent;
    private final int numberMin;
    private final int numberMax;
    private final int caseWeightLower;
    private final int caseWeightUpper;
    private final int caseWeightCapitalized;
    private final int caseWeightTailUpper;

    CounterStrikeStyleGenerator() {
        this(
                CONFIG.stems(),
                CONFIG.suffixes(),
                CONFIG.leetMap(),
                CONFIG.leetChancePercent(),
                CONFIG.bareNumberChancePercent(),
                CONFIG.numberMin(),
                CONFIG.numberMax(),
                CONFIG.caseWeightLower(),
                CONFIG.caseWeightUpper(),
                CONFIG.caseWeightCapitalized(),
                CONFIG.caseWeightTailUpper()
        );
    }

    private CounterStrikeStyleGenerator(
            List<String> stems,
            List<String> suffixes,
            Map<Character, Character> leetMap,
            int leetChancePercent,
            int bareNumberChancePercent,
            int numberMin,
            int numberMax,
            int caseWeightLower,
            int caseWeightUpper,
            int caseWeightCapitalized,
            int caseWeightTailUpper
    ) {
        this.stems = List.copyOf(stems);
        this.suffixes = List.copyOf(suffixes);
        this.leetMap = Map.copyOf(leetMap);
        this.leetChancePercent = leetChancePercent;
        this.bareNumberChancePercent = bareNumberChancePercent;
        this.numberMin = numberMin;
        this.numberMax = numberMax;
        this.caseWeightLower = caseWeightLower;
        this.caseWeightUpper = caseWeightUpper;
        this.caseWeightCapitalized = caseWeightCapitalized;
        this.caseWeightTailUpper = caseWeightTailUpper;
    }

    @Override
    public String id() {
        return StandardNicknameGenerators.COUNTER_STRIKE_PRO;
    }

    @Override
    public String displayName() {
        return CONFIG.displayName();
    }

    @Override
    public String description() {
        return CONFIG.description();
    }

    @Override
    public String generateCandidate(NicknameRequestContext context, Random random) {
        String stem = pick(stems, random);
        String withLeet = applyLeet(stem, random);
        String withCase = applyCase(withLeet, random);
        String suffix = pick(suffixes, random);

        if (suffix.isEmpty() && random.nextInt(100) < bareNumberChancePercent) {
            return withCase + randomBetween(random, numberMin, numberMax);
        }
        return withCase + suffix;
    }

    private int randomBetween(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private String applyLeet(String stem, Random random) {
        StringBuilder builder = new StringBuilder(stem.length());
        for (char symbol : stem.toCharArray()) {
            char lower = Character.toLowerCase(symbol);
            if (random.nextInt(100) < leetChancePercent && leetMap.containsKey(lower)) {
                builder.append(leetMap.get(lower));
            } else {
                builder.append(symbol);
            }
        }
        return builder.toString();
    }

    private String applyCase(String stem, Random random) {
        int totalWeight = caseWeightLower + caseWeightUpper + caseWeightCapitalized + caseWeightTailUpper;
        int roll = random.nextInt(totalWeight);

        if (roll < caseWeightLower) {
            return stem.toLowerCase(Locale.ROOT);
        }
        roll -= caseWeightLower;

        if (roll < caseWeightUpper) {
            return stem.toUpperCase(Locale.ROOT);
        }
        roll -= caseWeightUpper;

        if (roll < caseWeightCapitalized) {
            return Character.toUpperCase(stem.charAt(0)) + stem.substring(1).toLowerCase(Locale.ROOT);
        }

        if (stem.length() < 2) {
            return stem;
        }

        String head = stem.substring(0, stem.length() - 2).toLowerCase(Locale.ROOT);
        String tail = stem.substring(stem.length() - 2).toUpperCase(Locale.ROOT);
        return head + tail;
    }

    private String pick(List<String> words, Random random) {
        String value = words.get(random.nextInt(words.size()));
        return "<empty>".equals(value) ? "" : value;
    }

    private record CounterStrikeConfig(
            String displayName,
            String description,
            List<String> stems,
            List<String> suffixes,
            Map<Character, Character> leetMap,
            int leetChancePercent,
            int bareNumberChancePercent,
            int numberMin,
            int numberMax,
            int caseWeightLower,
            int caseWeightUpper,
            int caseWeightCapitalized,
            int caseWeightTailUpper
    ) {
        private static final String RESOURCE_PATH = "/generators/cs-pro.properties";

        private static CounterStrikeConfig loadDefault() {
            Properties properties = ConfigResourceSupport.loadProperties(RESOURCE_PATH);

            String displayName = ConfigResourceSupport.requiredString(properties, "displayName");
            String description = ConfigResourceSupport.requiredString(properties, "description");

            List<String> stems = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "stemsFile")
            );
            List<String> suffixes = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "suffixesFile")
            );

            Map<Character, Character> leetMap = parseLeetMap(
                    ConfigResourceSupport.requiredString(properties, "leetMap")
            );

            int leetChancePercent = ConfigResourceSupport.requiredPercent(properties, "leetChancePercent");
            int bareNumberChancePercent = ConfigResourceSupport.requiredPercent(
                    properties,
                    "bareNumberChancePercent"
            );
            int numberMin = ConfigResourceSupport.requiredInt(properties, "numberMin", 0, Integer.MAX_VALUE);
            int numberMax = ConfigResourceSupport.requiredInt(properties, "numberMax", numberMin, Integer.MAX_VALUE);

            int caseWeightLower = ConfigResourceSupport.requiredInt(properties, "caseWeightLower", 1, 1000);
            int caseWeightUpper = ConfigResourceSupport.requiredInt(properties, "caseWeightUpper", 0, 1000);
            int caseWeightCapitalized = ConfigResourceSupport.requiredInt(properties, "caseWeightCapitalized", 0, 1000);
            int caseWeightTailUpper = ConfigResourceSupport.requiredInt(properties, "caseWeightTailUpper", 0, 1000);

            if (caseWeightLower + caseWeightUpper + caseWeightCapitalized + caseWeightTailUpper < 1) {
                throw new IllegalStateException("At least one case weight must be > 0");
            }

            return new CounterStrikeConfig(
                    displayName,
                    description,
                    stems,
                    suffixes,
                    leetMap,
                    leetChancePercent,
                    bareNumberChancePercent,
                    numberMin,
                    numberMax,
                    caseWeightLower,
                    caseWeightUpper,
                    caseWeightCapitalized,
                    caseWeightTailUpper
            );
        }

        private static Map<Character, Character> parseLeetMap(String raw) {
            String[] pairs = raw.split(",");
            Map<Character, Character> mapping = new HashMap<>();
            for (String pair : pairs) {
                String trimmed = pair.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] parts = trimmed.split(":");
                if (parts.length != 2 || parts[0].length() != 1 || parts[1].length() != 1) {
                    throw new IllegalStateException("Invalid leet mapping entry: " + trimmed);
                }
                mapping.put(parts[0].charAt(0), parts[1].charAt(0));
            }

            if (mapping.isEmpty()) {
                throw new IllegalStateException("leetMap must contain at least one entry");
            }

            return Map.copyOf(mapping);
        }
    }
}
