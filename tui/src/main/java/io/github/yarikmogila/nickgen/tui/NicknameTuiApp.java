package io.github.yarikmogila.nickgen.tui;

import io.github.yarikmogila.nickgen.common.ExtensibleNicknameGenerator;
import io.github.yarikmogila.nickgen.common.GenerationRequest;
import io.github.yarikmogila.nickgen.common.InvalidGenerationRequestException;
import io.github.yarikmogila.nickgen.common.NicknameGenerator;
import io.github.yarikmogila.nickgen.common.NicknameLocale;
import io.github.yarikmogila.nickgen.common.NicknameResult;
import io.github.yarikmogila.nickgen.common.NicknameTemplate;
import io.github.yarikmogila.nickgen.common.NotEnoughUniqueNicknamesException;
import io.github.yarikmogila.nickgen.common.StandardNicknameGenerators;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(
        name = "nickgen",
        mixinStandardHelpOptions = true,
        description = "Generates unique and meaningful nicknames"
)
public final class NicknameTuiApp implements Callable<Integer> {

    @Option(names = {"-c", "--count"}, defaultValue = "1", description = "Number of nicknames to generate")
    int count;

    @Option(names = {"-l", "--locale"}, defaultValue = "EN", description = "Locale: ${COMPLETION-CANDIDATES}")
    NicknameLocale locale;

    @Option(names = {"-t", "--template"}, defaultValue = "ADJ_NOUN", description = "Template: ${COMPLETION-CANDIDATES}")
    NicknameTemplate template;

    @Option(names = "--seed", description = "Optional random seed for deterministic generation")
    Long seed;

    @Option(
            names = {"-g", "--generator"},
            defaultValue = StandardNicknameGenerators.DICTIONARY,
            description = "Generator ID: dictionary, minecraft-youtuber, cs-pro, dota-pro"
    )
    String generatorId;

    @Spec
    private CommandSpec spec;

    private final NicknameGenerator generator;

    public NicknameTuiApp() {
        this(new ExtensibleNicknameGenerator());
    }

    NicknameTuiApp(NicknameGenerator generator) {
        this.generator = generator;
    }

    @Override
    public Integer call() {
        try {
            GenerationRequest request = new GenerationRequest(count, locale, template, seed, generatorId);
            List<NicknameResult> results = generator.generate(request);
            results.forEach(result -> spec.commandLine().getOut().println(result.value()));
            return 0;
        } catch (InvalidGenerationRequestException | NotEnoughUniqueNicknamesException exception) {
            spec.commandLine().getErr().println("Error: " + exception.getMessage());
            return 2;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new NicknameTuiApp()).execute(args);
        System.exit(exitCode);
    }
}
