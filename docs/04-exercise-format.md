# Exercise format

The catalog is `src/commonMain/composeResources/files/exercises.json`. `ExerciseRepository` parses it at startup and refuses to boot if an id is duplicated, an answer points at an unknown tile, or a distractor repeats a correct surface.

The file is the port of the HTML `EXERCISES` array plus `EXERCISE_DISTRACTORS`, after the HTML's own `n5-03` fix (へ is a real tile, and both destination particles are accepted), plus later N5, N4, and N3 items. Do not hand-edit a thinner subset. Add items; do not drop the original 34.

## One item

```json
{
  "id": "n5-01",
  "jlpt": "N5",
  "level": "beginner",
  "english": "I eat sushi.",
  "tiles": [
    { "id": "t1", "surface": "私", "reading": "わたし", "role": "topic" },
    { "id": "t2", "surface": "は", "role": "particle" },
    { "id": "t3", "surface": "寿司", "reading": "すし", "role": "object" },
    { "id": "t4", "surface": "を", "role": "particle" },
    { "id": "t5", "surface": "食べます", "reading": "たべます", "role": "verb" }
  ],
  "answers": [["t1", "t2", "t3", "t4", "t5"]],
  "hint": "Topic は → object を → verb (ます) last.",
  "notes": "SOV: 私は (topic) 寿司を (object) 食べます.",
  "distractors": [
    { "id": "d1", "surface": "が", "role": "particle" },
    { "id": "d2", "surface": "に", "role": "particle" },
    { "id": "d3", "surface": "飲みます", "reading": "のみます", "role": "verb" }
  ]
}
```

`reading` is omitted when the surface is already kana. If the surface contains kanji, a reading is required by the catalog test.

## Fields

| JSON | Kotlin |
| --- | --- |
| `id` | `Exercise.id` |
| `jlpt` | `JlptLevel` (`N5`, `N4`, `N3`) |
| `level` | `Difficulty` (`beginner`, `intermediate`, `advanced`) |
| `english` | prompt |
| `tiles` | correct-side tiles, in canonical composition order (not the shuffled order) |
| `answers` | list of id lists. The HTML sometimes had a single `answer` array; the port always uses `answers`. |
| `hint`, `notes` | strings, may be empty |
| `distractors` | usually three, ids `d1`–`d3` |

Tile `role` must be one of the wires in `TileRole`: `topic`, `subject`, `object`, `particle`, `verb`, `noun`, `suffix`, `place`, `position`, `time`, `means`, `adj`, `copula`, `cond`, `te`, `rel`, `det`, `must`, `pot`, `expr`, `adv`, `vol`, `agent`, `passive`, `causer`, `causee`, `caus`, `conj`, `modal`.

`particle` and `conj` are drawn as particle chips.

## Alternate answers

`n5-03` (Tanaka goes to school):

```json
"answers": [
  ["t1", "t2", "t3", "t4", "t5", "t6"],
  ["t1", "t2", "t3", "t4", "t7", "t6"]
]
```

`t5` is に, `t7` is へ. Both are in `tiles`, so both appear even when distractors are off. Reveal shows the first sequence (に). The notes say either destination particle is fine and the unused one stays in the pool.

Do not put へ in `distractors` for this item. A distractor fails the check; へ must not.

## Distractors

They are wrong on purpose: the other particle (は vs が, に vs で), a near-miss verb, or a noun that would change the sentence. Surfaces must not duplicate a correct tile, or the repository rejects the file. At most three are shown. Including one anywhere in the bar fails Check, even if the other tiles look right.

The HTML also had a fallback list of particles for an exercise with no curated distractors. `FallbackParticles` in `Models.kt` keeps that list. None of the 120 items need it.

## Identical tiles

`n4-11` has two `を` tiles (`t2` and `t5`). `n3-04` has two `に` tiles (`t2` and `t7`). The checker treats a placement as correct when the surfaces line up with an accepted answer, so the learner is not punished for which identical chip they picked. Ids still matter for distractors: a distractor id is never a real tile id, so it cannot pass the surface check.

## Adding an exercise

1. Append an object to `exercises.json`. Use the next free id (`n5-41`, `n4-41`, …). Set `jlpt` to `N5`, `N4`, or `N3` so the existing selector shows it. Set `level` to the difficulty already used for that band: `beginner`, `intermediate`, or `advanced`.
2. Give every kanji surface a reading in kana.
3. Give at least one answer whose ids all exist in `tiles`.
4. Add distractors that are plausible mistakes and do not repeat a surface already in `tiles`.
5. Run `./gradlew test`. `GameReducerTest.catalogHasTheFullPortedSet` currently expects 120 items and 40 per playable level. Update those counts in the same change if you add items. Do not lower them to "make the test pass" by deleting sentences.

Kotlin objects are not a second catalog. JSON is the only authoring format so the sentences stay in one file a reviewer can diff.

## Deck order

Within a level, items are shuffled at runtime. The order in the JSON file is the authoring order (n5-01 through n3-40) and the order a stable shuffler uses in tests. Do not rely on JSON order during play. The existing JLPT selector filters by `jlpt`; there is no separate level filter.
