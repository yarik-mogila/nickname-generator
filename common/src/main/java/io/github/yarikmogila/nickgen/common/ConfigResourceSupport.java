package io.github.yarikmogila.nickgen.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

final class ConfigResourceSupport {

    private ConfigResourceSupport() {
    }

    static Properties loadProperties(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");

        InputStream inputStream = ConfigResourceSupport.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException("Config resource is missing: " + resourcePath);
        }

        Properties properties = new Properties();
        try (InputStream stream = inputStream) {
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read config resource: " + resourcePath, exception);
        }
        return properties;
    }

    static List<String> loadWordList(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");

        InputStream inputStream = ConfigResourceSupport.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException("Word list resource is missing: " + resourcePath);
        }

        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                words.add(trimmed);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read word list resource: " + resourcePath, exception);
        }

        if (words.isEmpty()) {
            throw new IllegalStateException("Word list resource is empty: " + resourcePath);
        }
        return List.copyOf(words);
    }

    static String requiredString(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required config key: " + key);
        }
        return value.trim();
    }

    static int requiredInt(Properties properties, String key, int minInclusive, int maxInclusive) {
        String raw = requiredString(properties, key);
        try {
            int value = Integer.parseInt(raw);
            if (value < minInclusive || value > maxInclusive) {
                throw new IllegalStateException(
                        "Config key out of range: " + key + "=" + value
                                + ", expected " + minInclusive + ".." + maxInclusive
                );
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid integer config key: " + key + "=" + raw, exception);
        }
    }

    static int requiredPercent(Properties properties, String key) {
        return requiredInt(properties, key, 0, 100);
    }
}
