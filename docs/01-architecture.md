# Architecture

Single Gradle module, Kotlin JVM, Compose Multiplatform Desktop. IntelliJ opens the repository root; `settings.gradle.kts` names the project **JapaneseLanguageGame**.

```
JapaneseLanguageGame
├── build.gradle.kts
├── settings.gradle.kts
├── src/main/kotlin/com/cognitivecreations/japaneselanguagegame/
│   ├── Main.kt                          window, minimum size
│   ├── JapaneseLanguageGameApp.kt       theme, keyboard, controller, wrong-flash timer
│   ├── domain/
│   │   ├── Models.kt                    Tile, Exercise, GameState, events
│   │   └── GameReducer.kt               rules, catalog type, tile pool
│   ├── data/
│   │   ├── ExerciseRepository.kt        JSON → domain, validation
│   │   └── ProgressStore.kt             ~/.config JSON
│   ├── theme/GameTheme.kt               Material 3 dark palette, Noto Sans JP
│   └── ui/
│       ├── GameScreen.kt                the one screen
│       ├── WordTile.kt                  chip, click, drag
│       └── FuriganaText.kt              reading stacked above kanji
├── src/main/resources/
│   ├── exercises.json                   the 34 items
│   └── fonts/                           OFL Noto Sans JP subset
└── src/test/kotlin/                     reducer, catalog, progress file
```

Package: `com.cognitivecreations.japaneselanguagegame`.

## Layers

**Domain** knows sentences, tiles, and score. It does not know Compose, files, or JSON. `GameReducer` is a pure function from `(GameState, GameEvent)` to the next `GameState`. Tests drive it with a shuffler that does not randomize, so a given answer sequence is stable.

**Data** turns the classpath JSON into an `ExerciseCatalog`, and reads/writes `PersistedProgress`. A corrupt progress file falls back to defaults so a bad write cannot trap the app on the next launch.

**UI** renders `GameState` and sends `GameEvent`s. The 900 ms "not quite" flash is a `LaunchedEffect` in the app composable; when it ends it dispatches `DismissWrongFlash`. Drag ghosts live in the screen, not in the domain: a drop either places a tile, removes one, or does nothing.

`GameController` holds the `MutableState` and writes the progress file only when the persisted slice changes (level, furigana, distractors, score, streak, best). Moving tiles does not touch disk.

## Compose Desktop

`Main.kt` is the `application { Window { ... } }` entry. The run task is `./gradlew run`, main class `com.cognitivecreations.japaneselanguagegame.MainKt`.

Material 3 supplies buttons, the alert dialog, and typography. Color, tile chrome, and the dashed sentence bar are drawn explicitly so the desk matches the HTML game's dark palette rather than a default Material seed.

Japanese text uses a subset of Noto Sans JP (OFL) bundled in resources, regular and semibold. Furigana is not HTML `<ruby>`; it is a `Column` of reading over surface. See `FuriganaText.kt`.

## What is random

The queue for a level is shuffled when the level loads and again when the learner finishes the last item (the deck loops). The tile pool is shuffled per question, with up to eight retries if that shuffle happens to already be a full accepted answer. Tests replace `DefaultShuffler` so they do not depend on `Random`.

## Tests

`./gradlew test` covers:

- 34 exercises, level counts, readings on kanji, distractors present
- に / へ alternate, interchangeable duplicate を and に
- streak bonus, reveal-as-miss, wrong-answer flash, N2 ignored, queue wrap
- progress file round trip and corrupt-file fallback
