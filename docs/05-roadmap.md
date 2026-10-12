# Roadmap

The desktop drill is playable for the full N5–N3 set. The items below are not in this build. Nothing in the current loop is a fake screen standing in for a missing feature, except the N2 chip, which is an intentional disabled control.

## N2 and N1

The selector already has a disabled N2. Turning it on means:

- Add exercises with `"jlpt": "N2"` (and later N1, which would be a new `JlptLevel`)
- Mark the level `playable`
- Extend the allowed level set in `ProgressCodec`
- Keep the same tile, answer, and distractor shape so the reducer does not grow a special case

Likely N2 patterns, not yet written: ばかり, わけではない, ようにする, ことにする, passive/causative combinations, and tighter は/が contrasts. They should be authored the same way as `docs/04-exercise-format.md`, not as a new game mode.

## More sentences at N5–N3

34 items loop quickly once they are familiar. The next content pass is more items in the same JSON file: honorific/humble pairs only if the tiles stay unambiguous, and more minimal pairs (に vs で, は vs が) at N5 before adding longer N3 clauses.

## Audio

No audio in this build. A later pass could play the canonical sentence after a correct check or a reveal (one clip per answer line, or on-device TTS). Autoplay should be off by default. Clips would live under resources and stay offline. This does not change scoring.

## Input

Click and drag-to-append match the HTML game. Not built yet:

- Dropping a tile into an arbitrary gap in the bar (today a drag onto the bar appends, and a drag inside the bar moves that tile to the end)
- Keyboard focus that moves from chip to chip

## Authoring

There is no in-app editor. Editing `exercises.json` and running the tests is the workflow. A future checker could render every item headlessly and fail if a kanji tile lacks a reading; the unit test already does the reading check.

## Mobile

The shared UI already runs in the browser through the Kotlin/Wasm target (`wasmJs`). Android and iOS are not targets yet. They can reuse `domain`, the exercise loader, and the `ProgressStore` interface; each would add its own `actual` store. The sentence bar and the pool already wrap. Hit targets and the reset dialog should be rechecked on a narrow screen before calling a phone build done. Keep this as one Gradle module.

## Sync and accounts

Out of scope. Progress is one local JSON file. If sync ever exists, that file's schema (`jlpt`, `furigana`, `distractors`, `score`, `streak`, `best`) is the document to upload, not the whole `GameState`.

## Known deliberate differences from the HTML file

- Duplicate identical tiles match by surface, so two `を` or two `に` do not fail a correct-looking sentence.
- Enter during the short wrong-answer flash does not skip to the next item.
- Progress on the desktop is a JSON file under the config directory. The web build stores that same JSON in `localStorage`.
