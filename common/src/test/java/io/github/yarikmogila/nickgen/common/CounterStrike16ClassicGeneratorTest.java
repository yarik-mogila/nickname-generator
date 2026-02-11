package io.github.yarikmogila.nickgen.common;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class CounterStrike16ClassicGeneratorTest {

    @Test
    void shouldGenerateReadableTokensWithoutMixedScripts() {
        CounterStrike16ClassicGenerator generator = new CounterStrike16ClassicGenerator();
        NicknameRequestContext context = new NicknameRequestContext(
                NicknameLocale.RU,
                NicknameTemplate.ADJ_NOUN,
                Map.of()
        );

        Random random = new Random(2026);
        for (int index = 0; index < 300; index++) {
            String candidate = generator.generateCandidate(context, random);
            String[] tokens = candidate.split("[^\\p{L}\\p{N}]+");
            for (String token : tokens) {
                if (!token.isBlank()) {
                    assertFalse(
                            containsBothScripts(token),
                            () -> "Mixed script token detected: '" + token + "' in nickname '" + candidate + "'"
                    );
                }
            }
        }
    }

    private boolean containsBothScripts(String token) {
        boolean hasLatin = false;
        boolean hasCyrillic = false;

        for (int index = 0; index < token.length(); index++) {
            char symbol = token.charAt(index);
            if (!Character.isLetter(symbol)) {
                continue;
            }

            Character.UnicodeScript script = Character.UnicodeScript.of(symbol);
            if (script == Character.UnicodeScript.LATIN) {
                hasLatin = true;
            } else if (script == Character.UnicodeScript.CYRILLIC) {
                hasCyrillic = true;
            }

            if (hasLatin && hasCyrillic) {
                return true;
            }
        }

        return false;
    }
}
