package io.github.yarikmogila.nickgen.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class ExtensibleNicknameGenerator implements NicknameGenerator {

    private static final EngineConfig ENGINE_CONFIG = EngineConfig.loadDefault();

    private final NicknameGeneratorRegistry registry;
    private final Set<String> generatedNicknames;
    private final int minAttempts;
    private final int attemptsPerNickname;

    public ExtensibleNicknameGenerator() {
        this(StandardNicknameGenerators.defaultRegistry(), ENGINE_CONFIG.minAttempts(), ENGINE_CONFIG.attemptsPerNickname());
    }

    public ExtensibleNicknameGenerator(NicknameGeneratorRegistry registry) {
        this(registry, ENGINE_CONFIG.minAttempts(), ENGINE_CONFIG.attemptsPerNickname());
    }

    public ExtensibleNicknameGenerator(
            NicknameGeneratorRegistry registry,
            int minAttempts,
            int attemptsPerNickname
    ) {
        if (minAttempts < 1) {
            throw new IllegalArgumentException("minAttempts must be positive");
        }
        if (attemptsPerNickname < 1) {
            throw new IllegalArgumentException("attemptsPerNickname must be positive");
        }

        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.generatedNicknames = new HashSet<>();
        this.minAttempts = minAttempts;
        this.attemptsPerNickname = attemptsPerNickname;
    }

    @Override
    public List<NicknameGeneratorDescriptor> availableGenerators() {
        return registry.descriptors();
    }

    @Override
    public synchronized List<NicknameResult> generate(GenerationRequest request) {
        validateRequest(request);

        String generatorId = normalizeGeneratorId(request.generatorId());
        NicknameProfileGenerator profileGenerator = registry.getById(generatorId);

        Random random = request.seed() != null
                ? new Random(request.seed())
                : ThreadLocalRandom.current();

        NicknameRequestContext context = new NicknameRequestContext(request.locale(), request.template(), request.options());
        String userWord = UserWordSupport.resolveUserWord(request.options());
        UserWordSupport.UserWordPosition userWordPosition = UserWordSupport.resolveUserWordPosition(request.options());

        int maxAttempts = resolveMaxAttempts(request.count());
        int attempts = 0;
        List<NicknameResult> results = new ArrayList<>(request.count());

        while (results.size() < request.count()) {
            if (attempts++ >= maxAttempts) {
                throw new NotEnoughUniqueNicknamesException(
                        "Could not generate " + request.count() + " unique nicknames in "
                                + maxAttempts + " attempts"
                );
            }

            String candidate = profileGenerator.generateCandidate(context, random);
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            candidate = UserWordSupport.applyUserWord(candidate, userWord, userWordPosition, random);

            if (generatedNicknames.add(candidate)) {
                results.add(new NicknameResult(candidate, request.locale(), request.template(), generatorId));
            }
        }

        return List.copyOf(results);
    }

    private int resolveMaxAttempts(int count) {
        long attemptsByCount = (long) count * attemptsPerNickname;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(minAttempts, attemptsByCount));
    }

    private void validateRequest(GenerationRequest request) {
        if (request == null) {
            throw new InvalidGenerationRequestException("request must not be null");
        }
        if (request.count() < 1) {
            throw new InvalidGenerationRequestException("count must be >= 1");
        }
        if (request.locale() == null) {
            throw new InvalidGenerationRequestException("locale must not be null");
        }
        if (request.template() == null) {
            throw new InvalidGenerationRequestException("template must not be null");
        }
    }

    private String normalizeGeneratorId(String generatorId) {
        if (generatorId == null || generatorId.isBlank()) {
            return GenerationRequest.DEFAULT_GENERATOR_ID;
        }
        return generatorId.trim();
    }

    private record EngineConfig(int minAttempts, int attemptsPerNickname) {
        private static final String RESOURCE_PATH = "/generators/engine.properties";

        private static EngineConfig loadDefault() {
            var properties = ConfigResourceSupport.loadProperties(RESOURCE_PATH);
            int minAttempts = ConfigResourceSupport.requiredInt(properties, "minAttempts", 1, Integer.MAX_VALUE);
            int attemptsPerNickname = ConfigResourceSupport.requiredInt(
                    properties,
                    "attemptsPerNickname",
                    1,
                    Integer.MAX_VALUE
            );
            return new EngineConfig(minAttempts, attemptsPerNickname);
        }
    }
}
