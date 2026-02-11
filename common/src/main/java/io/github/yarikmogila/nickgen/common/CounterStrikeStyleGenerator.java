package io.github.yarikmogila.nickgen.common;

import java.util.List;
import java.util.Locale;
import java.util.Random;

final class CounterStrikeStyleGenerator implements NicknameProfileGenerator {

    private static final List<String> STEMS = List.of(
            "simple", "donk", "dosia", "shiro", "zywoo", "apex", "clutch", "spray", "frag", "flick"
    );

    private static final List<String> SUFFIXES = List.of("", "x", "pro", "gg", "tv");

    @Override
    public String id() {
        return StandardNicknameGenerators.COUNTER_STRIKE_PRO;
    }

    @Override
    public String displayName() {
        return "Counter-Strike Pro Style";
    }

    @Override
    public String description() {
        return "Compact tags inspired by CS pro-scene naming patterns.";
    }

    @Override
    public String generateCandidate(NicknameRequestContext context, Random random) {
        String stem = STEMS.get(random.nextInt(STEMS.size()));
        String withLeet = applyLeet(stem, random);
        String withCase = applyCase(withLeet, random);
        String suffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));

        if (suffix.isEmpty() && random.nextInt(100) < 35) {
            return withCase + random.nextInt(10);
        }
        return withCase + suffix;
    }

    private String applyLeet(String stem, Random random) {
        StringBuilder builder = new StringBuilder(stem.length());
        for (char symbol : stem.toCharArray()) {
            char mapped = mapLeet(symbol, random);
            builder.append(mapped);
        }
        return builder.toString();
    }

    private char mapLeet(char symbol, Random random) {
        if (random.nextInt(100) > 40) {
            return symbol;
        }

        return switch (symbol) {
            case 'a' -> '4';
            case 'e' -> '3';
            case 'i' -> '1';
            case 'o' -> '0';
            case 's' -> '5';
            default -> symbol;
        };
    }

    private String applyCase(String stem, Random random) {
        int pattern = random.nextInt(4);
        return switch (pattern) {
            case 0 -> stem;
            case 1 -> stem.toUpperCase(Locale.ROOT);
            case 2 -> Character.toUpperCase(stem.charAt(0)) + stem.substring(1).toLowerCase(Locale.ROOT);
            default -> {
                if (stem.length() < 2) {
                    yield stem;
                }
                String head = stem.substring(0, stem.length() - 2).toLowerCase(Locale.ROOT);
                String tail = stem.substring(stem.length() - 2).toUpperCase(Locale.ROOT);
                yield head + tail;
            }
        };
    }
}
