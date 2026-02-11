package io.github.yarikmogila.nickgen.common;

public final class NotEnoughUniqueNicknamesException extends RuntimeException {
    public NotEnoughUniqueNicknamesException(String message) {
        super(message);
    }
}
