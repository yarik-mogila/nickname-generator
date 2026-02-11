package io.github.yarikmogila.nickgen.common;

import java.util.List;
import java.util.Random;

final class MinecraftYoutuberStyleGenerator implements NicknameProfileGenerator {

    private static final List<String> PREFIXES = List.of(
            "Tommy", "Awesome", "Dream", "Techno", "Pixel", "Block", "Craft", "Ender", "Nether", "Mine"
    );

    private static final List<String> SUFFIXES = List.of(
            "Innit", "Dude", "Blade", "Craft", "Playz", "Builder", "Hunter", "Master", "Rider", "Knight"
    );

    private static final List<String> SINGLE_WORDS = List.of(
            "Dream", "Technoblade", "BlockRunner", "EnderKnight", "MineRider"
    );

    @Override
    public String id() {
        return StandardNicknameGenerators.MINECRAFT_YOUTUBER;
    }

    @Override
    public String displayName() {
        return "Minecraft YouTuber Style";
    }

    @Override
    public String description() {
        return "Nicknames similar to Minecraft content creator naming style.";
    }

    @Override
    public String generateCandidate(NicknameRequestContext context, Random random) {
        if (random.nextInt(100) < 25) {
            return SINGLE_WORDS.get(random.nextInt(SINGLE_WORDS.size()));
        }

        String prefix = PREFIXES.get(random.nextInt(PREFIXES.size()));
        String suffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));

        if (random.nextInt(100) < 30) {
            return prefix + suffix + (10 + random.nextInt(90));
        }
        return prefix + suffix;
    }
}
