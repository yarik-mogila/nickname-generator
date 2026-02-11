package io.github.yarikmogila.nickgen.common;

import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

final class DotaStyleGenerator implements NicknameProfileGenerator {

    private static final DotaConfig CONFIG = DotaConfig.loadDefault();

    private final List<String> latinBases;
    private final List<String> cyrillicBases;
    private final List<String> cyrillicSuffixes;
    private final List<String> latinSuffixes;
    private final int patternUppercaseDigitsChancePercent;
    private final int patternCyrillicChancePercent;
    private final String uppercaseRepeatChar;
    private final int uppercaseRepeatMin;
    private final int uppercaseRepeatMax;
    private final int uppercaseNumberMin;
    private final int uppercaseNumberMax;
    private final int cyrillicUnderscoreChancePercent;
    private final int latinNumberChancePercent;
    private final int latinNumberMin;
    private final int latinNumberMax;

    DotaStyleGenerator() {
        this(
                CONFIG.latinBases(),
                CONFIG.cyrillicBases(),
                CONFIG.cyrillicSuffixes(),
                CONFIG.latinSuffixes(),
                CONFIG.patternUppercaseDigitsChancePercent(),
                CONFIG.patternCyrillicChancePercent(),
                CONFIG.uppercaseRepeatChar(),
                CONFIG.uppercaseRepeatMin(),
                CONFIG.uppercaseRepeatMax(),
                CONFIG.uppercaseNumberMin(),
                CONFIG.uppercaseNumberMax(),
                CONFIG.cyrillicUnderscoreChancePercent(),
                CONFIG.latinNumberChancePercent(),
                CONFIG.latinNumberMin(),
                CONFIG.latinNumberMax()
        );
    }

    private DotaStyleGenerator(
            List<String> latinBases,
            List<String> cyrillicBases,
            List<String> cyrillicSuffixes,
            List<String> latinSuffixes,
            int patternUppercaseDigitsChancePercent,
            int patternCyrillicChancePercent,
            String uppercaseRepeatChar,
            int uppercaseRepeatMin,
            int uppercaseRepeatMax,
            int uppercaseNumberMin,
            int uppercaseNumberMax,
            int cyrillicUnderscoreChancePercent,
            int latinNumberChancePercent,
            int latinNumberMin,
            int latinNumberMax
    ) {
        this.latinBases = List.copyOf(latinBases);
        this.cyrillicBases = List.copyOf(cyrillicBases);
        this.cyrillicSuffixes = List.copyOf(cyrillicSuffixes);
        this.latinSuffixes = List.copyOf(latinSuffixes);
        this.patternUppercaseDigitsChancePercent = patternUppercaseDigitsChancePercent;
        this.patternCyrillicChancePercent = patternCyrillicChancePercent;
        this.uppercaseRepeatChar = uppercaseRepeatChar;
        this.uppercaseRepeatMin = uppercaseRepeatMin;
        this.uppercaseRepeatMax = uppercaseRepeatMax;
        this.uppercaseNumberMin = uppercaseNumberMin;
        this.uppercaseNumberMax = uppercaseNumberMax;
        this.cyrillicUnderscoreChancePercent = cyrillicUnderscoreChancePercent;
        this.latinNumberChancePercent = latinNumberChancePercent;
        this.latinNumberMin = latinNumberMin;
        this.latinNumberMax = latinNumberMax;
    }

    @Override
    public String id() {
        return StandardNicknameGenerators.DOTA_PRO;
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
        int roll = random.nextInt(100);
        if (roll < patternUppercaseDigitsChancePercent) {
            return uppercaseWithDigits(random);
        }
        roll -= patternUppercaseDigitsChancePercent;
        if (roll < patternCyrillicChancePercent) {
            return cyrillicStyle(random);
        }
        return latinStyle(random);
    }

    private String uppercaseWithDigits(Random random) {
        String base = pick(latinBases, random).toUpperCase(Locale.ROOT);
        int repeatCount = uppercaseRepeatMin + random.nextInt(uppercaseRepeatMax - uppercaseRepeatMin + 1);
        String repeated = uppercaseRepeatChar.repeat(repeatCount);
        int suffixNumber = uppercaseNumberMin + random.nextInt(uppercaseNumberMax - uppercaseNumberMin + 1);
        return base + repeated + suffixNumber;
    }

    private String cyrillicStyle(Random random) {
        String base = pick(cyrillicBases, random);
        String suffix = pick(cyrillicSuffixes, random);
        if (random.nextInt(100) < cyrillicUnderscoreChancePercent) {
            return base + "_" + suffix;
        }
        return base + suffix;
    }

    private String latinStyle(Random random) {
        String base = pick(latinBases, random);
        String suffix = pick(latinSuffixes, random);
        if (random.nextInt(100) < latinNumberChancePercent) {
            return base + suffix + (latinNumberMin + random.nextInt(latinNumberMax - latinNumberMin + 1));
        }
        return base + suffix;
    }

    private String pick(List<String> words, Random random) {
        return words.get(random.nextInt(words.size()));
    }

    private record DotaConfig(
            String displayName,
            String description,
            List<String> latinBases,
            List<String> cyrillicBases,
            List<String> cyrillicSuffixes,
            List<String> latinSuffixes,
            int patternUppercaseDigitsChancePercent,
            int patternCyrillicChancePercent,
            String uppercaseRepeatChar,
            int uppercaseRepeatMin,
            int uppercaseRepeatMax,
            int uppercaseNumberMin,
            int uppercaseNumberMax,
            int cyrillicUnderscoreChancePercent,
            int latinNumberChancePercent,
            int latinNumberMin,
            int latinNumberMax
    ) {
        private static final String RESOURCE_PATH = "/generators/dota-pro.properties";

        private static DotaConfig loadDefault() {
            Properties properties = ConfigResourceSupport.loadProperties(RESOURCE_PATH);

            String displayName = ConfigResourceSupport.requiredString(properties, "displayName");
            String description = ConfigResourceSupport.requiredString(properties, "description");

            List<String> latinBases = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "latinBasesFile")
            );
            List<String> cyrillicBases = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "cyrillicBasesFile")
            );
            List<String> cyrillicSuffixes = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "cyrillicSuffixesFile")
            );
            List<String> latinSuffixes = ConfigResourceSupport.loadWordList(
                    ConfigResourceSupport.requiredString(properties, "latinSuffixesFile")
            );

            int patternUppercaseDigitsChancePercent = ConfigResourceSupport.requiredPercent(
                    properties,
                    "patternUppercaseDigitsChancePercent"
            );
            int patternCyrillicChancePercent = ConfigResourceSupport.requiredPercent(
                    properties,
                    "patternCyrillicChancePercent"
            );
            if (patternUppercaseDigitsChancePercent + patternCyrillicChancePercent > 100) {
                throw new IllegalStateException("Pattern percentages must not exceed 100");
            }

            String uppercaseRepeatChar = ConfigResourceSupport.requiredString(properties, "uppercaseRepeatChar");
            int uppercaseRepeatMin = ConfigResourceSupport.requiredInt(properties, "uppercaseRepeatMin", 1, 20);
            int uppercaseRepeatMax = ConfigResourceSupport.requiredInt(
                    properties,
                    "uppercaseRepeatMax",
                    uppercaseRepeatMin,
                    50
            );

            int uppercaseNumberMin = ConfigResourceSupport.requiredInt(properties, "uppercaseNumberMin", 0, Integer.MAX_VALUE);
            int uppercaseNumberMax = ConfigResourceSupport.requiredInt(
                    properties,
                    "uppercaseNumberMax",
                    uppercaseNumberMin,
                    Integer.MAX_VALUE
            );

            int cyrillicUnderscoreChancePercent = ConfigResourceSupport.requiredPercent(
                    properties,
                    "cyrillicUnderscoreChancePercent"
            );
            int latinNumberChancePercent = ConfigResourceSupport.requiredPercent(properties, "latinNumberChancePercent");

            int latinNumberMin = ConfigResourceSupport.requiredInt(properties, "latinNumberMin", 0, Integer.MAX_VALUE);
            int latinNumberMax = ConfigResourceSupport.requiredInt(
                    properties,
                    "latinNumberMax",
                    latinNumberMin,
                    Integer.MAX_VALUE
            );

            return new DotaConfig(
                    displayName,
                    description,
                    latinBases,
                    cyrillicBases,
                    cyrillicSuffixes,
                    latinSuffixes,
                    patternUppercaseDigitsChancePercent,
                    patternCyrillicChancePercent,
                    uppercaseRepeatChar,
                    uppercaseRepeatMin,
                    uppercaseRepeatMax,
                    uppercaseNumberMin,
                    uppercaseNumberMax,
                    cyrillicUnderscoreChancePercent,
                    latinNumberChancePercent,
                    latinNumberMin,
                    latinNumberMax
            );
        }
    }
}
