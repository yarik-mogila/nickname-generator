package io.github.yarikmogila.nickgen.common;

import java.util.List;
import java.util.Locale;
import java.util.Random;

final class DotaStyleGenerator implements NicknameProfileGenerator {

    private static final List<String> LATIN_BASES = List.of(
            "Ramzes", "Vova", "Solo", "Noone", "Nightfall", "Yatoro", "Collapse"
    );

    private static final List<String> CYRILLIC_BASES = List.of(
            "Серега", "Вова", "Паша", "Костя", "Артем", "Денчик"
    );

    private static final List<String> CYRILLIC_SUFFIXES = List.of(
            "Пират", "Керри", "Мидер", "Саппорт", "Титан", "Гладиатор"
    );

    private static final List<String> LATIN_SUFFIXES = List.of("Pain", "Spirit", "Blade", "GG", "Mode");

    @Override
    public String id() {
        return StandardNicknameGenerators.DOTA_PRO;
    }

    @Override
    public String displayName() {
        return "Dota Style";
    }

    @Override
    public String description() {
        return "Nicknames inspired by Dota pro and streamer naming conventions.";
    }

    @Override
    public String generateCandidate(NicknameRequestContext context, Random random) {
        int pattern = random.nextInt(3);
        return switch (pattern) {
            case 0 -> uppercaseWithDigits(random);
            case 1 -> cyrillicStyle(random);
            default -> latinStyle(random);
        };
    }

    private String uppercaseWithDigits(Random random) {
        String base = LATIN_BASES.get(random.nextInt(LATIN_BASES.size())).toUpperCase(Locale.ROOT);
        int repeatCount = 2 + random.nextInt(3);
        String repeated = "S".repeat(repeatCount);
        int suffixNumber = 100 + random.nextInt(900);
        return base + repeated + suffixNumber;
    }

    private String cyrillicStyle(Random random) {
        String base = CYRILLIC_BASES.get(random.nextInt(CYRILLIC_BASES.size()));
        String suffix = CYRILLIC_SUFFIXES.get(random.nextInt(CYRILLIC_SUFFIXES.size()));
        if (random.nextBoolean()) {
            return base + suffix;
        }
        return base + "_" + suffix;
    }

    private String latinStyle(Random random) {
        String base = LATIN_BASES.get(random.nextInt(LATIN_BASES.size()));
        String suffix = LATIN_SUFFIXES.get(random.nextInt(LATIN_SUFFIXES.size()));
        if (random.nextInt(100) < 45) {
            return base + suffix + (1 + random.nextInt(999));
        }
        return base + suffix;
    }
}
