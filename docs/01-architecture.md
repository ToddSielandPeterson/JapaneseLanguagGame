# Architecture

One Gradle module, Kotlin Multiplatform, Compose Multiplatform. Targets: JVM (desktop) and wasmJs (browser). IntelliJ opens the repository root; `settings.gradle.kts` names the project **JapaneseLanguageGame**.

```
JapaneseLanguageGame
├── build.gradle.kts
├── settings.gradle.kts
├── amplify.yml                         Amplify installs a JDK and runs the wasm distribution
├── customHttp.yml                      Content-Type: application/wasm
├── src/commonMain/kotlin/.../
│   ├── JapaneseLanguageGameApp.kt      theme, keyboard, controller, wrong-flash timer
│   ├── domain/
│   │   ├── Models.kt                   Tile, Exercise, GameState, events
│   │   └── GameReducer.kt              rules, catalog type, tile pool
│   ├── data/
│   │   ├── ExerciseRepository.kt       Compose resource JSON → domain
│   │   └── ProgressStore.kt            store interface, codec, key/value store
│   ├── theme/GameTheme.kt              Material 3 dark palette, Noto Sans JP
│   └── ui/
│       ├── GameScreen.kt               the one screen
│       ├── WordTile.kt                 chip, click, drag
│       └── FuriganaText.kt             reading stacked above kanji
├── src/commonMain/composeResources/
│   ├── files/exercises.json            the 120 items
│   ├── files/OFL.txt
│   └── font/                           OFL Noto Sans JP subset
├── src/jvmMain/kotlin/.../
│   ├── Main.kt                         desktop window
│   └── data/FileProgressStore.kt       ~/.config JSON
├── src/wasmJsMain/
│   ├── kotlin/.../Main.kt              ComposeViewport, font preload
│   ├── kotlin/.../BrowserProgressStore.kt
│   └── resources/                      index.html, styles.css
└── src/jvmTest/kotlin/                 reducer, catalog, file store, key/value store
```

Package: `com.cognitivecreations.japaneselanguagegame`.

## Layers

**Domain** knows sentences, tiles, and score. It does not know Compose, files, or JSON. `GameReducer` is a pure function from `(GameState, GameEvent)` to the next `GameState`. Tests drive it with a shuffler that does not randomize, so a given answer sequence is stable.

**Data** turns `files/exercises.json` into an `ExerciseCatalog`. `ProgressStore` is the save/load interface. `ProgressCodec` is the shared JSON and the sanitize step (unknown level becomes N5, negative numbers become 0). `FileProgressStore` is the JVM file. `KeyValueProgressStore` is the browser shape; wasm passes `localStorage` through `BrowserLocalStorage`. A corrupt payload falls back to defaults so a bad write cannot trap the app on the next launch.

**UI** renders `GameState` and sends `GameEvent`s. The 900 ms "not quite" flash is a `LaunchedEffect` in the app composable; when it ends it dispatches `DismissWrongFlash`. Drag ghosts live in the screen, not in the domain: a drop either places a tile, removes one, or does nothing.

`GameController` holds the `MutableState` and writes progress only when the persisted slice changes (level, furigana, distractors, score, streak, best). Moving tiles does not touch storage.

## Desktop and web

`src/jvmMain/.../Main.kt` is the `application { Window { ... } }` entry. The run task is `./gradlew run`, main class `com.cognitivecreations.japaneselanguagegame.MainKt`.

`src/wasmJsMain/.../Main.kt` calls `ComposeViewport`. The production task is `./gradlew wasmJsBrowserDistribution`. Output: `build/dist/wasmJs/productionExecutable`.

Material 3 supplies buttons, the alert dialog, and typography. Color, tile chrome, and the dashed sentence bar are drawn explicitly so the desk matches the HTML game's dark palette rather than a default Material seed.

Japanese text uses a subset of Noto Sans JP (OFL) from Compose resources, regular and semibold. The web entry preloads those fonts before the first tiles are drawn. Furigana is not HTML `<ruby>`; it is a `Column` of reading over surface, with the bundled font set on both lines. See `FuriganaText.kt`.

## What is random

The queue for a level is shuffled when the level loads and again when the learner finishes the last item (the deck loops). The tile pool is shuffled per question, with up to eight retries if that shuffle happens to already be a full accepted answer. Tests replace `DefaultShuffler` so they do not depend on `Random`.

## Tests

`./gradlew test` (the JVM tests) covers:

- 120 exercises, 40 per playable level, readings on kanji, distractors present, answers reassemble
- に / へ alternate, interchangeable duplicate を and に
- streak bonus, reveal-as-miss, wrong-answer flash, N2 ignored, queue wrap
- progress file round trip and corrupt-file fallback
- the key/value store used by the browser: round trip, corrupt payload, sanitize, overwrite
