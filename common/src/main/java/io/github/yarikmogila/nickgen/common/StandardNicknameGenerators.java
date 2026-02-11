package io.github.yarikmogila.nickgen.common;

import java.util.List;

public final class StandardNicknameGenerators {

    public static final String DICTIONARY = "dictionary";
    public static final String MINECRAFT_YOUTUBER = "minecraft-youtuber";
    public static final String COUNTER_STRIKE_PRO = "cs-pro";
    public static final String DOTA_PRO = "dota-pro";

    private StandardNicknameGenerators() {
    }

    public static List<NicknameProfileGenerator> defaults() {
        return List.of(
                new DictionaryNicknameGenerator(),
                new MinecraftYoutuberStyleGenerator(),
                new CounterStrikeStyleGenerator(),
                new DotaStyleGenerator()
        );
    }

    public static NicknameGeneratorRegistry defaultRegistry() {
        NicknameGeneratorRegistry registry = new NicknameGeneratorRegistry();
        registry.registerAll(defaults());
        return registry;
    }
}
