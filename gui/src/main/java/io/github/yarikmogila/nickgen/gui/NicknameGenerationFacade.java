package io.github.yarikmogila.nickgen.gui;

import io.github.yarikmogila.nickgen.common.ExtensibleNicknameGenerator;
import io.github.yarikmogila.nickgen.common.GenerationRequest;
import io.github.yarikmogila.nickgen.common.InvalidGenerationRequestException;
import io.github.yarikmogila.nickgen.common.NicknameGenerator;
import io.github.yarikmogila.nickgen.common.NicknameGeneratorDescriptor;
import io.github.yarikmogila.nickgen.common.NicknameLocale;
import io.github.yarikmogila.nickgen.common.NicknameResult;
import io.github.yarikmogila.nickgen.common.NicknameTemplate;
import java.util.List;
import java.util.Objects;

final class NicknameGenerationFacade {

    private final NicknameGenerator generator;

    NicknameGenerationFacade() {
        this(new ExtensibleNicknameGenerator());
    }

    NicknameGenerationFacade(NicknameGenerator generator) {
        this.generator = Objects.requireNonNull(generator, "generator must not be null");
    }

    List<NicknameGeneratorDescriptor> availableGenerators() {
        return generator.availableGenerators();
    }

    List<NicknameResult> generate(
            int count,
            NicknameLocale locale,
            NicknameTemplate template,
            String generatorId,
            String seedText
    ) {
        Long seed = parseSeed(seedText);
        return generator.generate(new GenerationRequest(count, locale, template, seed, generatorId));
    }

    private Long parseSeed(String seedText) {
        if (seedText == null || seedText.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(seedText.trim());
        } catch (NumberFormatException exception) {
            throw new InvalidGenerationRequestException("seed must be a valid long number");
        }
    }
}
