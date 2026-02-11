package io.github.yarikmogila.nickgen.tui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.yarikmogila.nickgen.common.GenerationRequest;
import io.github.yarikmogila.nickgen.common.GenerationOptionKeys;
import io.github.yarikmogila.nickgen.common.InvalidGenerationRequestException;
import io.github.yarikmogila.nickgen.common.NicknameGenerator;
import io.github.yarikmogila.nickgen.common.NicknameLocale;
import io.github.yarikmogila.nickgen.common.NicknameResult;
import io.github.yarikmogila.nickgen.common.NicknameTemplate;
import io.github.yarikmogila.nickgen.common.StandardNicknameGenerators;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;
import picocli.CommandLine;
import org.junit.jupiter.api.Test;

class NicknameTuiAppTest {

    @Test
    void shouldParseArgumentsAndGenerateNicknames() {
        CapturingGenerator generator = new CapturingGenerator();
        NicknameTuiApp app = new NicknameTuiApp(generator);
        CommandLine commandLine = new CommandLine(app);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        commandLine.setOut(new PrintWriter(output, true));

        int exitCode = commandLine.execute(
                "--count", "2",
                "--locale", "RU",
                "--template", "NOUN_VERB",
                "--generator", "cs-pro",
                "--word", "beast",
                "--seed", "42"
        );

        assertEquals(0, exitCode);
        assertEquals(2, generator.lastRequest.count());
        assertEquals(NicknameLocale.RU, generator.lastRequest.locale());
        assertEquals(NicknameTemplate.NOUN_VERB, generator.lastRequest.template());
        assertEquals(StandardNicknameGenerators.COUNTER_STRIKE_PRO, generator.lastRequest.generatorId());
        assertEquals("beast", generator.lastRequest.options().get(GenerationOptionKeys.USER_WORD));
        assertEquals(42L, generator.lastRequest.seed());

        String outputText = output.toString();
        assertTrue(outputText.contains("TestNickOne"));
        assertTrue(outputText.contains("TestNickTwo"));
    }

    @Test
    void shouldReturnValidationErrorForInvalidCount() {
        NicknameTuiApp app = new NicknameTuiApp(new ValidatingGenerator());
        CommandLine commandLine = new CommandLine(app);

        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        commandLine.setErr(new PrintWriter(errorOutput, true));

        int exitCode = commandLine.execute("--count", "0");

        assertEquals(2, exitCode);
        assertTrue(errorOutput.toString().contains("count must be >= 1"));
    }

    @Test
    void shouldReturnErrorForUnknownGenerator() {
        NicknameTuiApp app = new NicknameTuiApp();
        CommandLine commandLine = new CommandLine(app);

        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        commandLine.setErr(new PrintWriter(errorOutput, true));

        int exitCode = commandLine.execute("--generator", "unknown-generator");

        assertEquals(2, exitCode);
        assertTrue(errorOutput.toString().contains("Unknown generatorId"));
    }

    private static final class CapturingGenerator implements NicknameGenerator {
        private GenerationRequest lastRequest;

        @Override
        public List<NicknameResult> generate(GenerationRequest request) {
            this.lastRequest = request;
            return List.of(
                    new NicknameResult("TestNickOne", request.locale(), request.template()),
                    new NicknameResult("TestNickTwo", request.locale(), request.template())
            );
        }
    }

    private static final class ValidatingGenerator implements NicknameGenerator {
        @Override
        public List<NicknameResult> generate(GenerationRequest request) {
            if (request.count() < 1) {
                throw new InvalidGenerationRequestException("count must be >= 1");
            }
            return List.of(new NicknameResult("FallbackNick", request.locale(), request.template()));
        }
    }
}
