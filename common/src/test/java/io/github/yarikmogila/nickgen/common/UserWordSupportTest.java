package io.github.yarikmogila.nickgen.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class UserWordSupportTest {

    @Test
    void shouldReplaceFirstUnderscoredPart() {
        String actual = UserWordSupport.applyUserWord(
                "Стас_Роамер",
                "Айлин",
                UserWordSupport.UserWordPosition.START,
                new Random(1)
        );
        assertEquals("Айлин_Роамер", actual);
    }

    @Test
    void shouldReplaceFirstCamelCasePartAndKeepNumberSuffix() {
        String actual = UserWordSupport.applyUserWord(
                "VikinPhantom806",
                "Айлин",
                UserWordSupport.UserWordPosition.START,
                new Random(1)
        );
        assertEquals("Айлин_Phantom806", actual);
    }

    @Test
    void shouldPrefixWhenSingleToken() {
        String actual = UserWordSupport.applyUserWord(
                "EmoInvoker78",
                "Айлин",
                UserWordSupport.UserWordPosition.START,
                new Random(1)
        );
        assertEquals("Айлин_Invoker78", actual);
    }

    @Test
    void shouldKeepCandidateIfUserWordAlreadyIncluded() {
        String actual = UserWordSupport.applyUserWord("АйлинInvoker78", "Айлин", new Random(1));
        assertEquals("АйлинInvoker78", actual);
    }

    @Test
    void shouldAllowEndPosition() {
        String actual = UserWordSupport.applyUserWord(
                "VikinPhantom806",
                "Айлин",
                UserWordSupport.UserWordPosition.END,
                new Random(1)
        );
        assertEquals("Vikin_Айлин806", actual);
    }

    @Test
    void shouldParseUserWordPositionOption() {
        UserWordSupport.UserWordPosition position = UserWordSupport.resolveUserWordPosition(
                java.util.Map.of(GenerationOptionKeys.USER_WORD_POSITION, "end")
        );
        assertEquals(UserWordSupport.UserWordPosition.END, position);
    }

    @Test
    void shouldStyleWordToMatchReferenceWhenRequested() {
        String actual = UserWordSupport.applyUserWord(
                "D4K3R_Token",
                "Антон",
                UserWordSupport.UserWordPosition.START,
                UserWordSupport.UserWordStyle.MATCH,
                new Random(2)
        );

        assertTrue(actual.endsWith("_Token"));
        String styled = actual.substring(0, actual.indexOf('_'));
        assertFalse(styled.equals("Антон"));
        assertTrue(styled.matches(".*[\\u0400-\\u04FF].*"));
    }

    @Test
    void shouldParseUserWordStyleOption() {
        UserWordSupport.UserWordStyle style = UserWordSupport.resolveUserWordStyle(
                java.util.Map.of(GenerationOptionKeys.USER_WORD_STYLE, "match")
        );
        assertEquals(UserWordSupport.UserWordStyle.MATCH, style);
    }
}
