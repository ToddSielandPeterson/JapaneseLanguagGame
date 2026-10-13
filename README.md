# JapaneseLanguageGame

Sentence Builder (文を作ろう) is a drill for Japanese word order and particles. An English prompt is shown with scrambled tiles — content words and particles — and you arrange them into a grammatical sentence. It is a Kotlin Compose Multiplatform port of the self-contained HTML game, with 120 N5 / N4 / N3 exercises (40 each), including the original 34, and the same distractor style. The same screen runs on the desktop and in the browser (Kotlin/Wasm).

JLPT N5, N4, and N3 are playable. N2 is on the selector and disabled. Furigana defaults to on. Distractors default to off; turn them on when you want extra wrong tiles that must not be used. Score, streak, best streak, level, and the two toggles are stored locally. Nothing phones home.

Design notes live in [docs/](docs/README.md).

## Open in IntelliJ

1. Install a JDK 21 (the project toolchain is 21).
2. **File → Open** and choose this repository root (the folder that contains `settings.gradle.kts`).
3. Trust the Gradle project. IntelliJ imports it as **JapaneseLanguageGame**.
4. Let Gradle download Kotlin and Compose the first time.

Run from the Gradle tool window: **Tasks → compose desktop → run**.

Or the gutter on `main` in `src/jvmMain/kotlin/com/cognitivecreations/japaneselanguagegame/Main.kt`.

## Run from a terminal

Desktop:

```bash
./gradlew run
```

Browser, with a dev server:

```bash
./gradlew wasmJsBrowserDevelopmentRun
```

Production files for static hosting (what Amplify publishes):

```bash
./gradlew wasmJsBrowserDistribution
```

The site is written to `build/dist/wasmJs/productionExecutable`. Hosting that folder is described in [docs/deploy-amplify.md](docs/deploy-amplify.md).

Tests:

```bash
./gradlew test
```

The first run downloads Gradle (the wrapper), Kotlin 2.4.20, and Compose Multiplatform 1.12.1. After that the desktop game is offline. The web build is a static site. Japanese text uses a bundled subset of [Noto Sans JP](https://fonts.google.com/noto/specimen/Noto+Sans+JP) (SIL Open Font License, see `src/commonMain/composeResources/files/OFL.txt`).

On a Linux session without working OpenGL, Skiko logs `Cannot create Linux GL context` and falls back to another renderer. The window should still open. If it does not:

```bash
SKIKO_RENDER_API=SOFTWARE ./gradlew run
```

## How to play

1. Pick **N5**, **N4**, or **N3**.
2. Read the English line. Click tiles into the sentence bar, or drag them. Click a placed tile to put it back.
3. **Check** when you are ready. **Hint** shows a structure tip. **Reveal** fills the answer and counts as a miss. **Next** loads another shuffled sentence. The deck loops.
4. **ふりがな** shows or hides readings on kanji. **Distractors** adds the curated wrong tiles for the current item and restarts that sentence.

Keyboard: Enter checks, or advances after a result. H hint. N next. Esc clears.

A correct sentence scores `1 + min(streak, 5)`, then the streak goes up by one. A miss or a reveal clears the streak. **Reset** asks before it zeros score, streak, and best.

On the desktop, progress is saved to `~/.config/JapaneseLanguageGame/progress.json` (or `$XDG_CONFIG_HOME/JapaneseLanguageGame/progress.json`). Set `JAPANESE_LANGUAGE_GAME_HOME` to put that file somewhere else. In the browser, the same JSON is stored in `localStorage` under the key `JapaneseLanguageGame.progress`.

## Layout

| Path | Role |
| --- | --- |
| `src/commonMain/kotlin/.../domain` | Rules and models, no UI |
| `src/commonMain/kotlin/.../data` | Exercise loader and the shared progress codec |
| `src/commonMain/kotlin/.../ui` | The sentence desk, including furigana |
| `src/commonMain/composeResources/` | `exercises.json` and Noto Sans JP |
| `src/jvmMain/` | Desktop window and the progress file |
| `src/wasmJsMain/` | Browser entry, localStorage, page shell |
| `amplify.yml` | Amplify Hosting build |
| `docs/` | Design notes and [Amplify deploy steps](docs/deploy-amplify.md) |

Package: `com.cognitivecreations.japaneselanguagegame`.
