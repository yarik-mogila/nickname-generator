package io.github.yarikmogila.nickgen.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class NicknamePanelTest {

    @Test
    void shouldJoinMultipleNicknamesWithNewLineDelimiter() {
        String joined = NicknamePanel.joinLines(List.of("One", "Two", "Three"));
        assertEquals("One\nTwo\nThree", joined);
    }

    @Test
    void shouldReturnSingleNicknameWithoutExtraDelimiter() {
        String joined = NicknamePanel.joinLines(List.of("Solo"));
        assertEquals("Solo", joined);
    }
}
