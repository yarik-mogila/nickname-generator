package io.github.yarikmogila.nickgen.common;

import java.util.Objects;

public record NicknameResult(
        String value,
        NicknameLocale locale,
        NicknameTemplate template
) {
    public NicknameResult {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        Objects.requireNonNull(template, "template must not be null");
    }
}
