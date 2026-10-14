# Data model

## JlptLevel

`N5`, `N4`, `N3` are playable. `N2` exists so the selector can show it, and `playable` is false. Selecting N2 is a no-op. There is no exercise with `jlpt: "N2"`.

## Difficulty

Authored as `beginner`, `intermediate`, or `advanced` on each exercise (N5 / N4 / N3 respectively). It is stored and not shown. The badge on screen is the JLPT level.

## Tile

| Field | Meaning |
| --- | --- |
| `id` | Stable id inside one exercise (`t1`, `t7`, `d1`). Not unique across exercises. |
| `surface` | What the learner reads: `私`, `は`, `食べます`. |
| `reading` | Hiragana for furigana. Omitted for kana-only tiles. |
| `role` | Grammatical tag from the HTML (`particle`, `verb`, `topic`, …). |

`showsReading` is true only when `reading` is present **and** the surface contains a character in U+4E00–U+9FAF, the same kanji test as the HTML `<ruby>` rule. `コーヒー` never grows a reading line.

`role` is `particle` or `conj` when the chip should use the blue particle style. Other roles share the neutral chip. The role is not printed on the tile.

## Exercise

| Field | Meaning |
| --- | --- |
| `id` | `n5-01` … `n3-09` |
| `jlpt` | `N5`, `N4`, or `N3` |
| `english` | The prompt |
| `tiles` | Tiles that may appear in an accepted answer |
| `answers` | One or more id sequences. Order is the sentence. |
| `hint` | Shown after Hint |
| `notes` | Shown after a correct check or a reveal |
| `distractors` | Extra tiles used only while Distractors is on |

An answer may be shorter than `tiles`. `n5-03` includes both `に` (`t5`) and `へ` (`t7`). Either sequence is accepted; the unused particle stays in the pool. Putting both in the sentence matches neither sequence, so it fails.

## Checking an answer

`GameReducer.isCorrect`:

1. Empty placement is not checkable (the Check button is also disabled).
2. Any placed id that is not one of the exercise's real tiles is a distractor and fails.
3. An exact id sequence match succeeds.
4. Otherwise the surfaces are compared in order. Two tiles with the same surface (the two `を` in `n4-11`, the two `に` in `n3-04`) are interchangeable. The HTML compared ids only, so swapping those identical chips failed a visually correct sentence. This port accepts the surface sequence on purpose.

## Round phase

| Phase | Learner can |
| --- | --- |
| `BUILDING` | Place, remove, clear, hint, check, reveal |
| `WRONG_FLASH` | Nothing for ~900 ms, then back to `BUILDING` with the tiles still placed and the streak already cleared |
| `SOLVED` | Next only. Score already applied. |
| `REVEALED` | Next only. Streak cleared. Sentence replaced by `answers[0]`. |

## Scoring

On a correct check, before the streak increments:

```
gained = 1 + min(streak, 5)
score += gained
streak += 1
best = max(best, streak)
```

A wrong check sets `streak = 0` and leaves `score` and `best` alone. Reveal does the same and does not award the points that the now-filled sentence would have earned.

Worked example from a fresh desk: correct, correct, wrong, correct → scores 1, 3, 3, 4 and streaks 1, 2, 0, 1, with best staying at 2.

Reset sets score, streak, and best to 0. It does not change level or toggles and does not redraw the current sentence.

Changing level reshuffles that level's deck and keeps the score. Turning distractors on or off rebuilds the current pool and clears the sentence in progress.

## PersistedProgress

Written by `ProgressStore` as JSON:

```json
{
  "jlpt": "N5",
  "furigana": true,
  "distractors": false,
  "score": 0,
  "streak": 0,
  "best": 0
}
```

Desktop path: `$JAPANESE_LANGUAGE_GAME_HOME/progress.json` if that environment variable is set, otherwise `$XDG_CONFIG_HOME/JapaneseLanguageGame/progress.json`, otherwise `~/.config/JapaneseLanguageGame/progress.json`. The web app stores the same document in `localStorage` at `JapaneseLanguageGame.progress`. Both paths use `ProgressCodec`, so a bad payload falls back the same way.

The in-progress sentence, hint, and deck order are not saved. Relaunching starts a fresh shuffle at the saved level with the saved score. Unknown `jlpt` values (including `N2`) load as N5. Negative numbers load as 0.

## Tile pool

`GameReducer.activeTiles`:

- Distractors off: `exercise.tiles` only.
- Distractors on: those tiles plus up to three distractors whose surface is not already used.
- If every curated distractor collided with a real surface, up to two fallback particles are added from は が を に で へ と も の から まで, with ids `d_fb_<surface>`. The shipped 120 items all have usable curated distractors, so the fallback is a safety net.

The pool order is that list, shuffled, unless the shuffle equals an entire accepted answer (same ids, same length). In that case it retries up to eight times.
