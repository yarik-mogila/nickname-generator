package io.github.yarikmogila.nickgen.common;

import java.util.Random;

public interface NicknameProfileGenerator {
    String id();

    String displayName();

    String description();

    String generateCandidate(NicknameRequestContext context, Random random);
}
