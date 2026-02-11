package io.github.yarikmogila.nickgen.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class DictionaryNicknameGeneratorTest {

    @Test
    void shouldGenerateRequestedAmountOfUniqueNicknames() {
        DictionaryNicknameGenerator generator = new DictionaryNicknameGenerator();

        List<NicknameResult> results = generator.generate(new GenerationRequest(
                20,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN,
                null
        ));

        assertEquals(20, results.size());
        Set<String> unique = results.stream().map(NicknameResult::value).collect(Collectors.toSet());
        assertEquals(20, unique.size());
        results.forEach(result -> assertEquals(StandardNicknameGenerators.DICTIONARY, result.generatorId()));
    }

    @Test
    void shouldRespectTemplatePatterns() {
        DictionaryNicknameGenerator generator = new DictionaryNicknameGenerator();

        List<NicknameResult> adjectiveNoun = generator.generate(new GenerationRequest(
                5,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN,
                123L
        ));
        List<NicknameResult> nounVerb = generator.generate(new GenerationRequest(
                5,
                NicknameLocale.EN,
                NicknameTemplate.NOUN_VERB,
                456L
        ));
        List<NicknameResult> adjectiveNounNumber = generator.generate(new GenerationRequest(
                5,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN_NUMBER,
                789L
        ));

        adjectiveNoun.forEach(result -> assertTrue(result.value().matches("^[A-Z][a-z]+[A-Z][a-z]+$")));
        nounVerb.forEach(result -> assertTrue(result.value().matches("^[A-Z][a-z]+[A-Z][a-z]+$")));
        adjectiveNounNumber.forEach(result -> assertTrue(result.value().matches("^[A-Z][a-z]+[A-Z][a-z]+\\d{2,4}$")));
    }

    @Test
    void shouldLoadDictionariesForBothLocales() {
        DictionaryNicknameGenerator generator = new DictionaryNicknameGenerator();

        for (NicknameLocale locale : NicknameLocale.values()) {
            for (NicknameTemplate template : NicknameTemplate.values()) {
                List<NicknameResult> results = generator.generate(new GenerationRequest(2, locale, template, null));
                assertEquals(2, results.size());
                assertFalse(results.get(0).value().isBlank());
                assertFalse(results.get(1).value().isBlank());
            }
        }
    }

    @Test
    void shouldGenerateDeterministicallyWithSeed() {
        DictionaryNicknameGenerator first = new DictionaryNicknameGenerator();
        DictionaryNicknameGenerator second = new DictionaryNicknameGenerator();

        GenerationRequest request = new GenerationRequest(10, NicknameLocale.EN, NicknameTemplate.ADJ_NOUN, 42L);

        List<String> firstResult = first.generate(request).stream().map(NicknameResult::value).toList();
        List<String> secondResult = second.generate(request).stream().map(NicknameResult::value).toList();

        assertEquals(firstResult, secondResult);
    }

    @Test
    void shouldFailWhenCannotGenerateEnoughUniqueNicknames() {
        EnumMap<NicknameLocale, DictionaryNicknameGenerator.LocaleWordBank> tinyBanks =
                new EnumMap<>(NicknameLocale.class);

        tinyBanks.put(
                NicknameLocale.EN,
                new DictionaryNicknameGenerator.LocaleWordBank(
                        Map.of("nature", List.of("silent")),
                        Map.of("nature", List.of("forest")),
                        Map.of("nature", List.of("flows"))
                )
        );

        DictionaryNicknameGenerator generator = new DictionaryNicknameGenerator(tinyBanks, 2, 1);

        assertThrows(
                NotEnoughUniqueNicknamesException.class,
                () -> generator.generate(new GenerationRequest(
                        2,
                        NicknameLocale.EN,
                        NicknameTemplate.ADJ_NOUN,
                        77L
                ))
        );
    }

    @Test
    void shouldIncludeUserWordForDirectDictionaryGeneration() {
        DictionaryNicknameGenerator generator = new DictionaryNicknameGenerator();

        List<NicknameResult> results = generator.generate(new GenerationRequest(
                8,
                NicknameLocale.EN,
                NicknameTemplate.ADJ_NOUN,
                12L,
                StandardNicknameGenerators.DICTIONARY,
                Map.of(GenerationOptionKeys.USER_WORD, "Dragon")
        ));

        assertEquals(8, results.size());
        results.forEach(result -> assertTrue(result.value().toLowerCase().contains("dragon")));
    }
}
