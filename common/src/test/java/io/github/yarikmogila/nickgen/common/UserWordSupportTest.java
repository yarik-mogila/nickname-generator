package io.github.yarikmogila.nickgen.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import org.junit.jupiter.api.Test;

class UserWordSupportTest {

    @Test
    void shouldReplaceFirstUnderscoredPart() {
        String actual = UserWordSupport.applyUserWord("Стас_Роамер", "Айлин", new Random(1));
        assertEquals("Айлин_Роамер", actual);
    }

    @Test
    void shouldReplaceFirstCamelCasePartAndKeepNumberSuffix() {
        String actual = UserWordSupport.applyUserWord("VikinPhantom806", "Айлин", new Random(1));
        assertEquals("Айлин_Phantom806", actual);
    }

    @Test
    void shouldPrefixWhenSingleToken() {
        String actual = UserWordSupport.applyUserWord("EmoInvoker78", "Айлин", new Random(1));
        assertEquals("Айлин_Invoker78", actual);
    }

    @Test
    void shouldKeepCandidateIfUserWordAlreadyIncluded() {
        String actual = UserWordSupport.applyUserWord("АйлинInvoker78", "Айлин", new Random(1));
        assertEquals("АйлинInvoker78", actual);
    }
}
