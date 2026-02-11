package io.github.yarikmogila.nickgen.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.yarikmogila.nickgen.common.GenerationRequest;
import io.github.yarikmogila.nickgen.common.GenerationOptionKeys;
import io.github.yarikmogila.nickgen.common.InvalidGenerationRequestException;
import io.github.yarikmogila.nickgen.common.NicknameGenerator;
import io.github.yarikmogila.nickgen.common.StandardNicknameGenerators;
import io.github.yarikmogila.nickgen.common.NicknameLocale;
import io.github.yarikmogila.nickgen.common.NicknameResult;
import io.github.yarikmogila.nickgen.common.NicknameTemplate;
import java.awt.GraphicsEnvironment;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class NicknameGenerationFacadeTest {

    @Test
    void shouldPassArgumentsToGenerator() {
        CapturingGenerator generator = new CapturingGenerator();
        NicknameGenerationFacade facade = new NicknameGenerationFacade(generator);

        List<NicknameResult> results = facade.generate(
                3,
                NicknameLocale.RU,
                NicknameTemplate.NOUN_VERB,
                StandardNicknameGenerators.COUNTER_STRIKE_PRO,
                "pirate",
                "123"
        );

        assertEquals(3, generator.lastRequest.count());
        assertEquals(NicknameLocale.RU, generator.lastRequest.locale());
        assertEquals(NicknameTemplate.NOUN_VERB, generator.lastRequest.template());
        assertEquals(StandardNicknameGenerators.COUNTER_STRIKE_PRO, generator.lastRequest.generatorId());
        assertEquals("pirate", generator.lastRequest.options().get(GenerationOptionKeys.USER_WORD));
        assertEquals(123L, generator.lastRequest.seed());
        assertEquals(1, results.size());
    }

    @Test
    void shouldUseNullSeedWhenBlank() {
        CapturingGenerator generator = new CapturingGenerator();
        NicknameGenerationFacade facade = new NicknameGenerationFacade(generator);

        facade.generate(
                1,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN,
                StandardNicknameGenerators.DICTIONARY,
                null,
                "   "
        );

        assertEquals(null, generator.lastRequest.seed());
    }

    @Test
    void shouldFailForInvalidSeed() {
        NicknameGenerationFacade facade = new NicknameGenerationFacade(new CapturingGenerator());

        assertThrows(
                InvalidGenerationRequestException.class,
                () -> facade.generate(
                        1,
                        NicknameLocale.EN,
                        NicknameTemplate.ADJ_NOUN,
                        StandardNicknameGenerators.DICTIONARY,
                        null,
                        "abc"
                )
        );
    }

    @Test
    void shouldCreatePanelInHeadlessSafeWay() {
        NicknamePanel panel = new NicknamePanel(new NicknameGenerationFacade(new CapturingGenerator()));
        assertNotNull(panel);
    }

    @Test
    void shouldExposeAvailableGeneratorsForUi() {
        NicknameGenerationFacade facade = new NicknameGenerationFacade(new CapturingGenerator());
        assertFalse(facade.availableGenerators().isEmpty());
    }

    @Test
    void shouldCreateFrameWhenGraphicsEnvironmentAvailable() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

        NicknameFrame frame = new NicknameFrame(new NicknameGenerationFacade(new CapturingGenerator()));
        assertNotNull(frame.getContentPane());
        frame.dispose();
    }

    private static final class CapturingGenerator implements NicknameGenerator {
        private GenerationRequest lastRequest;

        @Override
        public List<NicknameResult> generate(GenerationRequest request) {
            this.lastRequest = request;
            return List.of(new NicknameResult("UiNick", request.locale(), request.template()));
        }
    }
}
