# Issue #7: Cure Dolly structural grammar layer inside Sentence Builder (文を作ろう)

| Field | Value |
| --- | --- |
| **Number** | #7 |
| **State** | Open |
| **Labels** | `enhancement`, `curriculum`, `sentence-structure`, `media-references` |
| **GitHub URL** | [https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/7](https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/7) |
| **Created** | 2026-11-06T16:16:19Z |

---

Parent epic: #3

## Summary

Upgrade Sentence Builder’s tile roles, hints, notes, and visual layout using Cure Dolly’s model from *Unlocking Japanese* and *Organic Japanese* (93-video course), without throwing away the working check/hint/reveal loop in `GameReducer` / `GameScreen`.

## Why / Pedagogy

Current hints already nudge particles and SOV (`docs/00-overview.md`, per-item `hint`/`notes` in `exercises.json`). Cure Dolly gives a single coherent engine picture so learners stop treating `は` as “the subject” and stop losing invisible subjects.

Core ideas to encode:
- **Engine:** `Aが B` — logical subject `が` + predicate (verb / い-adjective / noun+`だ`)
- **Optional `∅が`** (“zero pronoun” / invisible Alice) made **explicit in the UI** when the Japanese omits the subject
- **Train carriages:** particle+noun pairs (`は` topic flag, `を`, `に`, `で`, `へ`, `から`, `まで`, …) docking onto the predicate engine
- Clause-modifying carriages for N4/N3 items already in the catalog (たら, ので, causative/passive, etc.)
- Deep links to specific Cure Dolly lessons/chapters (copyright-safe: links only)

## Concrete scope & data/UI ideas

**Data extensions on exercises (non-breaking):**
- `structure`: e.g. `{ "engine": ["∅が", "食べます"], "carriages": [["私","は"], ["寿司","を"]] }`
- `cureDollyRefs`: video/chapter pointers
- Keep existing `tiles` / `answers` / `distractors` as the grader of record

**UI (`GameScreen.kt`, `WordTile.kt`):**
- Optional structure overlay: engine slot + carriage docks (toggle next to ふりがな / Distractors)
- When subject is omitted, show a ghost `∅が` carriage so topic `は` is visually distinct from subject `が`
- Role styling today: `particle`/`conj` = blue chips (`TileRole` in `Models.kt`). Extend roles or add a parallel `carriageKind` without breaking chip colors

**Content:**
- Re-author hints/notes for the original + expanded N5–N3 set to speak engine/carriage language
- Prioritize minimal pairs already called out in docs (は vs が, に vs で, existence に+が+います)

## Open design questions

1. Is `∅が` a placeable tile (advanced mode) or a non-interactive overlay only?
2. Must structure overlay be solvable independently, or is it always explanatory scaffolding under the existing answer check?
3. Naming in UI: “train / carriages” vs. quieter “slots / attachments” for learners who never watched Cure Dolly?
