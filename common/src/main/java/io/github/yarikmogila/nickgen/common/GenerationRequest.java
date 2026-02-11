package io.github.yarikmogila.nickgen.common;

public record GenerationRequest(
        int count,
        NicknameLocale locale,
        NicknameTemplate template,
        Long seed
) {
}
