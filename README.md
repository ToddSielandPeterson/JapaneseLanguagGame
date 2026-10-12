# JapaneseLanguageGame

Sentence Builder (文を作ろう) is a desktop drill for Japanese word order and particles. An English prompt is shown with scrambled tiles — content words and particles — and you arrange them into a grammatical sentence. It is a Kotlin port of the self-contained HTML game, with the same 34 N5 / N4 / N3 exercises and the same distractors.

JLPT N5, N4, and N3 are playable. N2 is on the selector and disabled. Furigana defaults to on. Distractors default to off; turn them on when you want extra wrong tiles that must not be used. Score, streak, best streak, level, and the two toggles are stored locally. Nothing phones home.

Design notes live in [docs/](docs/README.md).

## Open in IntelliJ

1. Install a JDK 21 (the project toolchain is 21).
2. **File → Open** and choose this repository root (the folder that contains `settings.gradle.kts`).
3. Trust the Gradle project. IntelliJ imports it as **JapaneseLanguageGame**.
4. Let Gradle download Kotlin and Compose the first time.

Run from the Gradle tool window: **Tasks → compose desktop → run**.

Or the gutter on `main` in `src/main/kotlin/com/cognitivecreations/japaneselanguagegame/Main.kt`.

## Run from a terminal

```bash
./gradlew run
```

Tests:

```bash
./gradlew test
```

The first run downloads Gradle (the wrapper), Kotlin 2.4.20, and Compose Multiplatform 1.12.1. After that the game is offline. Japanese text uses a bundled subset of [Noto Sans JP](https://fonts.google.com/noto/specimen/Noto+Sans+JP) (SIL Open Font License, see `src/main/resources/fonts/OFL.txt`).

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

Progress is saved to `~/.config/JapaneseLanguageGame/progress.json` (or `$XDG_CONFIG_HOME/JapaneseLanguageGame/progress.json`). Set `JAPANESE_LANGUAGE_GAME_HOME` to put that file somewhere else.

## Layout

| Path | Role |
| --- | --- |
| `src/main/kotlin/.../domain` | Rules and models, no UI |
| `src/main/kotlin/.../data` | `exercises.json` loader and the progress file |
| `src/main/kotlin/.../ui` | The sentence desk |
| `src/main/resources/exercises.json` | All 34 exercises |
| `docs/` | Design notes |

Package: `com.cognitivecreations.japaneselanguagegame`.
