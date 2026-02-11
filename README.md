# Nickname Generator

Multi-module Java 17 project for generating unique nicknames via pluggable generator profiles.

## Modules

- `common`: public API + extensible generation engine.
- `tui`: terminal app built with Picocli.
- `gui`: desktop app built with Java Swing.

## Requirements

- JDK 17+

## Build and test

```bash
./gradlew clean test build
```

## Standard generator profiles

- `dictionary`: meaningful dictionary-based EN/RU nicknames.
- `minecraft-youtuber`: style inspired by names like `TommyInnit`, `Awesamedude`, `Dream`, `Technoblade`.
- `cs-pro`: style inspired by names like `s1mple`, `d0nk`, `Dosia`, `sh1ro`, `ZywOo`, `apEX`.
- `dota-pro`: style inspired by names like `RAMZESSS666`, `Серега Пират`, `VovaPain`.

## Run TUI

Dictionary generation:

```bash
./gradlew :tui:run --args="--count 5 --locale EN --template ADJ_NOUN --generator dictionary"
```

Counter-Strike style generation:

```bash
./gradlew :tui:run --args="--count 5 --generator cs-pro --seed 42"
```

### TUI parameters

- `-c`, `--count` (default: `1`)
- `-l`, `--locale` (`EN`, `RU`; default: `EN`)
- `-t`, `--template` (`ADJ_NOUN`, `NOUN_VERB`, `ADJ_NOUN_NUMBER`; default: `ADJ_NOUN`)
- `-g`, `--generator` (`dictionary`, `minecraft-youtuber`, `cs-pro`, `dota-pro`; default: `dictionary`)
- `--seed` optional `long`

## Run GUI

```bash
./gradlew :gui:run
```

GUI includes:

- locale selector
- template selector
- generator selector
- nickname count spinner
- optional seed
- generated nickname list
- copy selected nickname button

## Public API for third-party developers (`common`)

### Core contracts

- `NicknameProfileGenerator`: implement your own profile strategy.
- `NicknameGeneratorRegistry`: register built-in and custom profile generators.
- `ExtensibleNicknameGenerator`: engine that resolves profile by `generatorId` and guarantees uniqueness.
- `GenerationRequest`: request model including `generatorId` and optional `options` map.

### Minimal custom generator example

```java
NicknameProfileGenerator custom = new NicknameProfileGenerator() {
    @Override
    public String id() { return "my-profile"; }

    @Override
    public String displayName() { return "My Profile"; }

    @Override
    public String description() { return "Custom naming style"; }

    @Override
    public String generateCandidate(NicknameRequestContext context, Random random) {
        return "Custom" + random.nextInt(10_000);
    }
};

NicknameGeneratorRegistry registry = StandardNicknameGenerators.defaultRegistry()
        .register(custom);

NicknameGenerator generator = new ExtensibleNicknameGenerator(registry);
List<NicknameResult> result = generator.generate(new GenerationRequest(
        5,
        NicknameLocale.EN,
        NicknameTemplate.ADJ_NOUN,
        123L,
        "my-profile"
));
```

## Uniqueness and behavior

- Nicknames are unique within a single generator instance (in-memory).
- If `seed` is provided, generation is deterministic for a new generator instance with the same input.
- Dictionary profile combines only compatible word groups (for example, `nature`, `tech`, `mystic`).

## Project coordinates

- Group: `io.github.yarikmogila`
- Version: `1.0.0-SNAPSHOT`

## License

MIT
