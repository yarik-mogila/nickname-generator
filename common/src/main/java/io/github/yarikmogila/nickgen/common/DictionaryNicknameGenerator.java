package io.github.yarikmogila.nickgen.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class DictionaryNicknameGenerator implements NicknameGenerator, NicknameProfileGenerator {

    private static final DictionaryConfig DEFAULT_CONFIG = DictionaryConfig.loadDefault();

    private final EnumMap<NicknameLocale, LocaleWordBank> wordBanks;
    private final Set<String> generatedNicknames;
    private final int minAttempts;
    private final int attemptsPerNickname;
    private final int minNumber;
    private final int maxNumber;
    private final String displayName;
    private final String description;

    public DictionaryNicknameGenerator() {
        this(DictionaryLoader.loadDefaultBanks(), DEFAULT_CONFIG);
    }

    @Override
    public String id() {
        return StandardNicknameGenerators.DICTIONARY;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public List<NicknameGeneratorDescriptor> availableGenerators() {
        return List.of(new NicknameGeneratorDescriptor(id(), displayName(), description()));
    }

    DictionaryNicknameGenerator(
            EnumMap<NicknameLocale, LocaleWordBank> wordBanks,
            int minAttempts,
            int attemptsPerNickname
    ) {
        this(
                wordBanks,
                new DictionaryConfig(
                        DEFAULT_CONFIG.displayName(),
                        DEFAULT_CONFIG.description(),
                        minAttempts,
                        attemptsPerNickname,
                        DEFAULT_CONFIG.minNumber(),
                        DEFAULT_CONFIG.maxNumber()
                )
        );
    }

    private DictionaryNicknameGenerator(EnumMap<NicknameLocale, LocaleWordBank> wordBanks, DictionaryConfig config) {
        if (config.minAttempts() < 1) {
            throw new IllegalArgumentException("minAttempts must be positive");
        }
        if (config.attemptsPerNickname() < 1) {
            throw new IllegalArgumentException("attemptsPerNickname must be positive");
        }
        if (config.minNumber() > config.maxNumber()) {
            throw new IllegalArgumentException("numberMin must be <= numberMax");
        }

        this.wordBanks = copyBanks(wordBanks);
        this.generatedNicknames = new HashSet<>();
        this.minAttempts = config.minAttempts();
        this.attemptsPerNickname = config.attemptsPerNickname();
        this.minNumber = config.minNumber();
        this.maxNumber = config.maxNumber();
        this.displayName = config.displayName();
        this.description = config.description();
    }

    @Override
    public synchronized List<NicknameResult> generate(GenerationRequest request) {
        validateRequest(request);

        LocaleWordBank localeWordBank = wordBanks.get(request.locale());
        if (localeWordBank == null) {
            throw new InvalidGenerationRequestException("No dictionary found for locale: " + request.locale());
        }

        Random random = request.seed() != null
                ? new Random(request.seed())
                : ThreadLocalRandom.current();

        int maxAttempts = resolveMaxAttempts(request.count());
        List<NicknameResult> results = new ArrayList<>(request.count());

        int attempts = 0;
        while (results.size() < request.count()) {
            if (attempts++ >= maxAttempts) {
                throw new NotEnoughUniqueNicknamesException(
                        "Could not generate " + request.count() + " unique nicknames in "
                                + maxAttempts + " attempts"
                );
            }

            String candidate = buildCandidate(localeWordBank, request.template(), random);
            if (generatedNicknames.add(candidate)) {
                results.add(new NicknameResult(candidate, request.locale(), request.template(), id()));
            }
        }

        return List.copyOf(results);
    }

    @Override
    public String generateCandidate(NicknameRequestContext context, Random random) {
        if (context.locale() == null) {
            throw new InvalidGenerationRequestException("locale must not be null");
        }
        if (context.template() == null) {
            throw new InvalidGenerationRequestException("template must not be null");
        }

        LocaleWordBank localeWordBank = wordBanks.get(context.locale());
        if (localeWordBank == null) {
            throw new InvalidGenerationRequestException("No dictionary found for locale: " + context.locale());
        }

        return buildCandidate(localeWordBank, context.template(), random);
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
        if (request.generatorId() != null
                && !request.generatorId().isBlank()
                && !id().equals(request.generatorId().trim())) {
            throw new InvalidGenerationRequestException(
                    "DictionaryNicknameGenerator only supports generatorId='" + id() + "'"
            );
        }
    }

    private String buildCandidate(LocaleWordBank localeWordBank, NicknameTemplate template, Random random) {
        return switch (template) {
            case ADJ_NOUN -> {
                String group = localeWordBank.pickGroupForAdjNoun(random);
                String adjective = localeWordBank.pickAdjective(group, random);
                String noun = localeWordBank.pickNoun(group, random);
                yield compose(adjective, noun);
            }
            case NOUN_VERB -> {
                String group = localeWordBank.pickGroupForNounVerb(random);
                String noun = localeWordBank.pickNoun(group, random);
                String verb = localeWordBank.pickVerb(group, random);
                yield compose(noun, verb);
            }
            case ADJ_NOUN_NUMBER -> {
                String group = localeWordBank.pickGroupForAdjNoun(random);
                String adjective = localeWordBank.pickAdjective(group, random);
                String noun = localeWordBank.pickNoun(group, random);
                int number = minNumber + random.nextInt(maxNumber - minNumber + 1);
                yield compose(adjective, noun) + number;
            }
        };
    }

    private String compose(String first, String second) {
        return normalizeWord(first) + normalizeWord(second);
    }

    private String normalizeWord(String rawWord) {
        if (rawWord == null || rawWord.isBlank()) {
            return "";
        }

        String normalized = rawWord.trim().replace('_', ' ').replace('-', ' ');
        String[] parts = normalized.split("\\s+");

        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            result.append(capitalize(part));
        }
        return result.toString();
    }

    private String capitalize(String value) {
        if (value.length() == 1) {
            return value.toUpperCase();
        }
        String first = value.substring(0, 1).toUpperCase();
        String tail = value.substring(1).toLowerCase();
        return first + tail;
    }

    private EnumMap<NicknameLocale, LocaleWordBank> copyBanks(EnumMap<NicknameLocale, LocaleWordBank> source) {
        Objects.requireNonNull(source, "wordBanks must not be null");

        EnumMap<NicknameLocale, LocaleWordBank> copy = new EnumMap<>(NicknameLocale.class);
        for (Map.Entry<NicknameLocale, LocaleWordBank> entry : source.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }

        if (copy.isEmpty()) {
            throw new IllegalArgumentException("wordBanks must not be empty");
        }
        return copy;
    }

    private record DictionaryConfig(
            String displayName,
            String description,
            int minAttempts,
            int attemptsPerNickname,
            int minNumber,
            int maxNumber
    ) {
        private static final String RESOURCE_PATH = "/generators/dictionary.properties";

        private static DictionaryConfig loadDefault() {
            var properties = ConfigResourceSupport.loadProperties(RESOURCE_PATH);
            String displayName = ConfigResourceSupport.requiredString(properties, "displayName");
            String description = ConfigResourceSupport.requiredString(properties, "description");
            int minAttempts = ConfigResourceSupport.requiredInt(properties, "minAttempts", 1, Integer.MAX_VALUE);
            int attemptsPerNickname = ConfigResourceSupport.requiredInt(
                    properties,
                    "attemptsPerNickname",
                    1,
                    Integer.MAX_VALUE
            );
            int minNumber = ConfigResourceSupport.requiredInt(properties, "numberMin", 0, Integer.MAX_VALUE);
            int maxNumber = ConfigResourceSupport.requiredInt(properties, "numberMax", minNumber, Integer.MAX_VALUE);
            return new DictionaryConfig(displayName, description, minAttempts, attemptsPerNickname, minNumber, maxNumber);
        }
    }

    static final class LocaleWordBank {
        private final Map<String, List<String>> adjectivesByGroup;
        private final Map<String, List<String>> nounsByGroup;
        private final Map<String, List<String>> verbsByGroup;

        LocaleWordBank(
                Map<String, List<String>> adjectivesByGroup,
                Map<String, List<String>> nounsByGroup,
                Map<String, List<String>> verbsByGroup
        ) {
            this.adjectivesByGroup = copyMap(adjectivesByGroup, "adjectivesByGroup");
            this.nounsByGroup = copyMap(nounsByGroup, "nounsByGroup");
            this.verbsByGroup = copyMap(verbsByGroup, "verbsByGroup");
        }

        String pickGroupForAdjNoun(Random random) {
            List<String> groups = intersectGroups(adjectivesByGroup, nounsByGroup);
            return pickRandom(groups, random, "No compatible groups for ADJ_NOUN");
        }

        String pickGroupForNounVerb(Random random) {
            List<String> groups = intersectGroups(nounsByGroup, verbsByGroup);
            return pickRandom(groups, random, "No compatible groups for NOUN_VERB");
        }

        String pickAdjective(String group, Random random) {
            return pickRandom(adjectivesByGroup.get(group), random, "Missing adjective group: " + group);
        }

        String pickNoun(String group, Random random) {
            return pickRandom(nounsByGroup.get(group), random, "Missing noun group: " + group);
        }

        String pickVerb(String group, Random random) {
            return pickRandom(verbsByGroup.get(group), random, "Missing verb group: " + group);
        }

        private List<String> intersectGroups(Map<String, List<String>> first, Map<String, List<String>> second) {
            List<String> groups = new ArrayList<>();
            for (String group : first.keySet()) {
                if (second.containsKey(group)) {
                    groups.add(group);
                }
            }
            Collections.sort(groups);
            return groups;
        }

        private static Map<String, List<String>> copyMap(Map<String, List<String>> source, String fieldName) {
            Objects.requireNonNull(source, fieldName + " must not be null");
            if (source.isEmpty()) {
                throw new IllegalArgumentException(fieldName + " must not be empty");
            }

            Map<String, List<String>> copy = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : source.entrySet()) {
                String group = entry.getKey();
                List<String> words = entry.getValue();
                if (group == null || group.isBlank()) {
                    throw new IllegalArgumentException(fieldName + " contains blank group");
                }
                if (words == null || words.isEmpty()) {
                    throw new IllegalArgumentException(fieldName + " contains empty group list");
                }
                copy.put(group, List.copyOf(words));
            }

            return Collections.unmodifiableMap(copy);
        }

        private static String pickRandom(List<String> words, Random random, String errorMessage) {
            if (words == null || words.isEmpty()) {
                throw new IllegalStateException(errorMessage);
            }
            return words.get(random.nextInt(words.size()));
        }
    }
}
