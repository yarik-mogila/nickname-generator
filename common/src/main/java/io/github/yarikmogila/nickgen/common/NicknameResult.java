package io.github.yarikmogila.nickgen.common;

import java.util.Objects;

public record NicknameResult(
        String value,
        NicknameLocale locale,
        NicknameTemplate template,
        String generatorId
) {
    public NicknameResult {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        Objects.requireNonNull(template, "template must not be null");
        Objects.requireNonNull(generatorId, "generatorId must not be null");
    }

    public NicknameResult(String value, NicknameLocale locale, NicknameTemplate template) {
        this(value, locale, template, StandardNicknameGenerators.DICTIONARY);
    }
}
