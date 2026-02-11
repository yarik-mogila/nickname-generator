# Nickname Generator

Multi-module Java 17 project for generating meaningful and unique nicknames from grouped EN/RU dictionaries.

## Modules

- `common`: core generator API and dictionary-based implementation.
- `tui`: terminal app built with Picocli.
- `gui`: desktop app built with Java Swing.

## Requirements

- JDK 17+

## Build and test

```bash
./gradlew clean test build
```

## Run TUI

Generate 5 English nicknames using template `ADJ_NOUN`:

```bash
./gradlew :tui:run --args="--count 5 --locale EN --template ADJ_NOUN"
```

Generate Russian nicknames with a deterministic seed:

```bash
./gradlew :tui:run --args="--count 5 --locale RU --template NOUN_VERB --seed 42"
```

### TUI parameters

- `-c`, `--count` (default: `1`)
- `-l`, `--locale` (`EN`, `RU`; default: `EN`)
- `-t`, `--template` (`ADJ_NOUN`, `NOUN_VERB`, `ADJ_NOUN_NUMBER`; default: `ADJ_NOUN`)
- `--seed` optional `long`

## Run GUI

```bash
./gradlew :gui:run
```

GUI includes:

- locale selector
- template selector
- nickname count spinner
- optional seed
- generated nickname list
- copy selected nickname button

## Uniqueness and behavior

- Nicknames are unique within a single generator instance (in-memory).
- If `seed` is provided, generation is deterministic for a new generator instance with the same input.
- Dictionaries are grouped by compatibility tags (for example, `nature`, `tech`, `mystic`) to keep combinations meaningful.

## Project coordinates

- Group: `io.github.yarikmogila`
- Version: `1.0.0-SNAPSHOT`

## License

MIT
