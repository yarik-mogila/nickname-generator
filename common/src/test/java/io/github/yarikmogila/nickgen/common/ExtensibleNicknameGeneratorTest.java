package io.github.yarikmogila.nickgen.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExtensibleNicknameGeneratorTest {

    @Test
    void shouldExposeBuiltInGeneratorDescriptors() {
        ExtensibleNicknameGenerator generator = new ExtensibleNicknameGenerator();

        List<String> ids = generator.availableGenerators().stream()
                .map(NicknameGeneratorDescriptor::id)
                .toList();

        assertTrue(ids.contains(StandardNicknameGenerators.DICTIONARY));
        assertTrue(ids.contains(StandardNicknameGenerators.MINECRAFT_YOUTUBER));
        assertTrue(ids.contains(StandardNicknameGenerators.COUNTER_STRIKE_PRO));
        assertTrue(ids.contains(StandardNicknameGenerators.DOTA_PRO));
    }

    @Test
    void shouldGenerateWithStandardProfiles() {
        ExtensibleNicknameGenerator generator = new ExtensibleNicknameGenerator();

        List<NicknameResult> minecraft = generator.generate(new GenerationRequest(
                10,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN,
                11L,
                StandardNicknameGenerators.MINECRAFT_YOUTUBER
        ));
        List<NicknameResult> counterStrike = generator.generate(new GenerationRequest(
                10,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN,
                22L,
                StandardNicknameGenerators.COUNTER_STRIKE_PRO
        ));
        List<NicknameResult> dota = generator.generate(new GenerationRequest(
                10,
                NicknameLocale.RU,
                NicknameTemplate.ADJ_NOUN,
                33L,
                StandardNicknameGenerators.DOTA_PRO
        ));

        assertEquals(10, minecraft.size());
        assertEquals(10, counterStrike.size());
        assertEquals(10, dota.size());

        minecraft.forEach(result -> {
            assertFalse(result.value().isBlank());
            assertEquals(StandardNicknameGenerators.MINECRAFT_YOUTUBER, result.generatorId());
        });
        counterStrike.forEach(result -> {
            assertFalse(result.value().isBlank());
            assertEquals(StandardNicknameGenerators.COUNTER_STRIKE_PRO, result.generatorId());
        });
        dota.forEach(result -> {
            assertFalse(result.value().isBlank());
            assertEquals(StandardNicknameGenerators.DOTA_PRO, result.generatorId());
        });
    }

    @Test
    void shouldGenerateDeterministicallyWithSeedForProfile() {
        ExtensibleNicknameGenerator first = new ExtensibleNicknameGenerator();
        ExtensibleNicknameGenerator second = new ExtensibleNicknameGenerator();

        GenerationRequest request = new GenerationRequest(
                12,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN,
                99L,
                StandardNicknameGenerators.COUNTER_STRIKE_PRO
        );

        List<String> firstValues = first.generate(request).stream().map(NicknameResult::value).toList();
        List<String> secondValues = second.generate(request).stream().map(NicknameResult::value).toList();

        assertEquals(firstValues, secondValues);
    }

    @Test
    void shouldFailOnUnknownGeneratorId() {
        ExtensibleNicknameGenerator generator = new ExtensibleNicknameGenerator();

        assertThrows(
                InvalidGenerationRequestException.class,
                () -> generator.generate(new GenerationRequest(
                        1,
                        NicknameLocale.EN,
                        NicknameTemplate.ADJ_NOUN,
                        null,
                        "unknown-id"
                ))
        );
    }

    @Test
    void shouldSupportThirdPartyGeneratorViaRegistry() {
        NicknameGeneratorRegistry registry = new NicknameGeneratorRegistry()
                .register(new FixedSuffixGenerator("third-party", "Third Party"));
        ExtensibleNicknameGenerator generator = new ExtensibleNicknameGenerator(registry, 10, 10);

        List<NicknameResult> results = generator.generate(new GenerationRequest(
                3,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN,
                7L,
                "third-party"
        ));

        assertEquals(3, results.size());
        assertTrue(results.get(0).value().startsWith("Custom"));
        assertEquals("third-party", results.get(0).generatorId());
    }

    @Test
    void shouldIncludeUserWordInGeneratedNicknames() {
        ExtensibleNicknameGenerator generator = new ExtensibleNicknameGenerator();

        List<NicknameResult> results = generator.generate(new GenerationRequest(
                12,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN,
                123L,
                StandardNicknameGenerators.COUNTER_STRIKE_PRO,
                Map.of(GenerationOptionKeys.USER_WORD, "Sniper")
        ));

        assertEquals(12, results.size());
        results.forEach(result -> assertTrue(result.value().toLowerCase().contains("sniper")));
    }

    private static final class FixedSuffixGenerator implements NicknameProfileGenerator {
        private final String id;
        private final String title;

        private FixedSuffixGenerator(String id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String displayName() {
            return title;
        }

        @Override
        public String description() {
            return "Custom test generator";
        }

        @Override
        public String generateCandidate(NicknameRequestContext context, java.util.Random random) {
            return "Custom" + random.nextInt(10_000);
        }
    }
}
