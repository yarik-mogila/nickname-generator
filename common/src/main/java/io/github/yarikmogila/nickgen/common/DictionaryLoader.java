package io.github.yarikmogila.nickgen.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

final class DictionaryLoader {

    private static final String DICTIONARY_CONFIG_PATH = "/generators/dictionary.properties";

    private DictionaryLoader() {
    }

    static EnumMap<NicknameLocale, DictionaryNicknameGenerator.LocaleWordBank> loadDefaultBanks() {
        Properties properties = ConfigResourceSupport.loadProperties(DICTIONARY_CONFIG_PATH);

        EnumMap<NicknameLocale, DictionaryNicknameGenerator.LocaleWordBank> banks =
                new EnumMap<>(NicknameLocale.class);
        banks.put(NicknameLocale.EN, loadLocaleWordBank(properties, "en"));
        banks.put(NicknameLocale.RU, loadLocaleWordBank(properties, "ru"));
        return banks;
    }

    private static DictionaryNicknameGenerator.LocaleWordBank loadLocaleWordBank(
            Properties properties,
            String localeCode
    ) {
        String adjectivesPath = ConfigResourceSupport.requiredString(properties, localeCode + ".adjectivesFile");
        String nounsPath = ConfigResourceSupport.requiredString(properties, localeCode + ".nounsFile");
        String verbsPath = ConfigResourceSupport.requiredString(properties, localeCode + ".verbsFile");

        Map<String, List<String>> adjectives = readGroupedWords(adjectivesPath);
        Map<String, List<String>> nouns = readGroupedWords(nounsPath);
        Map<String, List<String>> verbs = readGroupedWords(verbsPath);

        return new DictionaryNicknameGenerator.LocaleWordBank(adjectives, nouns, verbs);
    }

    private static Map<String, List<String>> readGroupedWords(String resourcePath) {
        InputStream inputStream = DictionaryLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException("Dictionary resource is missing: " + resourcePath);
        }

        Map<String, List<String>> groupedWords = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int delimiterIndex = trimmed.indexOf('|');
                if (delimiterIndex <= 0 || delimiterIndex == trimmed.length() - 1) {
                    throw new IllegalStateException("Invalid dictionary line at " + resourcePath + ":" + lineNumber);
                }

                String group = trimmed.substring(0, delimiterIndex).trim();
                String word = trimmed.substring(delimiterIndex + 1).trim();
                if (group.isEmpty() || word.isEmpty()) {
                    throw new IllegalStateException("Invalid dictionary line at " + resourcePath + ":" + lineNumber);
                }

                groupedWords.computeIfAbsent(group, ignored -> new ArrayList<>()).add(word);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read dictionary resource: " + resourcePath, exception);
        }

        if (groupedWords.isEmpty()) {
            throw new IllegalStateException("Dictionary resource is empty: " + resourcePath);
        }

        Map<String, List<String>> immutable = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : groupedWords.entrySet()) {
            immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(immutable);
    }
}
