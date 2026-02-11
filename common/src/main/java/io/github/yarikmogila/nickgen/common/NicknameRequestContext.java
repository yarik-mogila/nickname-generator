package io.github.yarikmogila.nickgen.common;

import java.util.Map;

public record NicknameRequestContext(
        NicknameLocale locale,
        NicknameTemplate template,
        Map<String, String> options
) {
    public NicknameRequestContext {
        options = options == null ? Map.of() : Map.copyOf(options);
    }
}
