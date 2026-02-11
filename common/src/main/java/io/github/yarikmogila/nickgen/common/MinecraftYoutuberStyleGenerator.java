package io.github.yarikmogila.nickgen.common;

import java.util.List;
import java.util.Properties;
import java.util.Random;

final class MinecraftYoutuberStyleGenerator implements NicknameProfileGenerator {

    private static final MinecraftConfig CONFIG = MinecraftConfig.loadDefault();

    private final List<String> prefixes;
    private final List<String> suffixes;
    private final List<String> singleWords;
    private final int singleWordChancePercent;
    private final int numberSuffixChancePercent;
    private final int numberMin;
    private final int numberMax;

    MinecraftYoutuberStyleGenerator() {
        this(
                CONFIG.prefixes(),
                CONFIG.suffixes(),
                CONFIG.singleWords(),
                CONFIG.singleWordChancePercent(),
                CONFIG.numberSuffixChancePercent(),
                CONFIG.numberMin(),
                CONFIG.numberMax()
        );
    }

    private MinecraftYoutuberStyleGenerator(
            List<String> prefixes,
            List<String> suffixes,
            List<String> singleWords,
            int singleWordChancePercent,
            int numberSuffixChancePercent,
            int numberMin,
            int numberMax
    ) {
        this.prefixes = List.copyOf(prefixes);
        this.suffixes = List.copyOf(suffixes);
        this.singleWords = List.copyOf(singleWords);
        this.singleWordChancePercent = singleWordChancePercent;
        this.numberSuffixChancePercent = numberSuffixChancePercent;
        this.numberMin = numberMin;
        this.numberMax = numberMax;
    }

    @Override
    public String id() {
        return StandardNicknameGenerators.MINECRAFT_YOUTUBER;
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
        if (random.nextInt(100) < singleWordChancePercent) {
            return pick(singleWords, random);
        }

        String prefix = pick(prefixes, random);
        String suffix = pick(suffixes, random);
        String base = prefix + suffix;

        if (random.nextInt(100) < numberSuffixChancePercent) {
            int number = numberMin + random.nextInt(numberMax - numberMin + 1);
            return base + number;
        }

        return base;
    }

    private String pick(List<String> words, Random random) {
        return words.get(random.nextInt(words.size()));
    }

    private record MinecraftConfig(
            String displayName,
            String description,
            List<String> prefixes,
            List<String> suffixes,
            List<String> singleWords,
            int singleWordChancePercent,
            int numberSuffixChancePercent,
            int numberMin,
            int numberMax
    ) {
        private static final String RESOURCE_PATH = "/generators/minecraft-youtuber.properties";

        private static MinecraftConfig loadDefault() {
            Properties properties = ConfigResourceSupport.loadProperties(RESOURCE_PATH);

            String displayName = ConfigResourceSupport.requiredString(properties, "displayName");
            String description = ConfigResourceSupport.requiredString(properties, "description");

            List<String> prefixes = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "prefixesFile")
            );
            List<String> suffixes = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "suffixesFile")
            );
            List<String> singleWords = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "singleWordsFile")
            );

            int singleWordChancePercent = ConfigResourceSupport.requiredPercent(properties, "singleWordChancePercent");
            int numberSuffixChancePercent = ConfigResourceSupport.requiredPercent(
                    properties,
                    "numberSuffixChancePercent"
            );
            int numberMin = ConfigResourceSupport.requiredInt(properties, "numberMin", 0, Integer.MAX_VALUE);
            int numberMax = ConfigResourceSupport.requiredInt(properties, "numberMax", numberMin, Integer.MAX_VALUE);

            return new MinecraftConfig(
                    displayName,
                    description,
                    prefixes,
                    suffixes,
                    singleWords,
                    singleWordChancePercent,
                    numberSuffixChancePercent,
                    numberMin,
                    numberMax
            );
        }
    }
}
