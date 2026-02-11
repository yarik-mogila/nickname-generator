package io.github.yarikmogila.nickgen.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class NicknameGeneratorRegistry {

    private final Map<String, NicknameProfileGenerator> generatorsById = new LinkedHashMap<>();

    public NicknameGeneratorRegistry register(NicknameProfileGenerator generator) {
        Objects.requireNonNull(generator, "generator must not be null");

        String id = normalizeId(generator.id());
        generatorsById.put(id, generator);
        return this;
    }

    public NicknameGeneratorRegistry registerAll(Iterable<? extends NicknameProfileGenerator> generators) {
        Objects.requireNonNull(generators, "generators must not be null");
        for (NicknameProfileGenerator generator : generators) {
            register(generator);
        }
        return this;
    }

    public NicknameProfileGenerator getById(String id) {
        NicknameProfileGenerator generator = generatorsById.get(normalizeId(id));
        if (generator == null) {
            throw new InvalidGenerationRequestException("Unknown generatorId: " + id);
        }
        return generator;
    }

    public boolean contains(String id) {
        return generatorsById.containsKey(normalizeId(id));
    }

    public List<NicknameGeneratorDescriptor> descriptors() {
        List<NicknameGeneratorDescriptor> descriptors = new ArrayList<>(generatorsById.size());
        for (NicknameProfileGenerator generator : generatorsById.values()) {
            descriptors.add(new NicknameGeneratorDescriptor(
                    generator.id(),
                    generator.displayName(),
                    generator.description()
            ));
        }
        return List.copyOf(descriptors);
    }

    private String normalizeId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            throw new InvalidGenerationRequestException("generatorId must not be blank");
        }
        return rawId.trim();
    }
}
