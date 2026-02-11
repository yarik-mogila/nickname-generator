package io.github.yarikmogila.nickgen.common;

import java.util.List;

public interface NicknameGenerator {
    List<NicknameResult> generate(GenerationRequest request);

    default List<NicknameGeneratorDescriptor> availableGenerators() {
        return List.of(new NicknameGeneratorDescriptor(
                GenerationRequest.DEFAULT_GENERATOR_ID,
                "Default",
                "Default nickname generation strategy"
        ));
    }
}
