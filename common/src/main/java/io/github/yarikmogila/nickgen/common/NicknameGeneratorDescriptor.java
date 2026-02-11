package io.github.yarikmogila.nickgen.common;

import java.util.Objects;

public record NicknameGeneratorDescriptor(
        String id,
        String displayName,
        String description
) {
    public NicknameGeneratorDescriptor {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(description, "description must not be null");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
