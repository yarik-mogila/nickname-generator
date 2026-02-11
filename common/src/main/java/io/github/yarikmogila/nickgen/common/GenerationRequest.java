package io.github.yarikmogila.nickgen.common;

import java.util.Map;

public record GenerationRequest(
        int count,
        NicknameLocale locale,
        NicknameTemplate template,
        Long seed,
        String generatorId,
        Map<String, String> options
) {
    public static final String DEFAULT_GENERATOR_ID = StandardNicknameGenerators.DICTIONARY;

    public GenerationRequest {
        options = options == null ? Map.of() : Map.copyOf(options);
    }

    public GenerationRequest(int count, NicknameLocale locale, NicknameTemplate template, Long seed) {
        this(count, locale, template, seed, DEFAULT_GENERATOR_ID, Map.of());
    }

    public GenerationRequest(
            int count,
            NicknameLocale locale,
            NicknameTemplate template,
            Long seed,
            String generatorId
    ) {
        this(count, locale, template, seed, generatorId, Map.of());
    }
}
