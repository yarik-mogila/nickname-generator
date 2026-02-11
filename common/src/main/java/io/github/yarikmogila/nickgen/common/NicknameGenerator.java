package io.github.yarikmogila.nickgen.common;

import java.util.List;

public interface NicknameGenerator {
    List<NicknameResult> generate(GenerationRequest request);
}
