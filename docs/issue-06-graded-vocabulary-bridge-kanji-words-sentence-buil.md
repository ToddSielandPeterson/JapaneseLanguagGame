# Issue #6: Graded vocabulary bridge: kanji → words → Sentence Builder tiles

| Field | Value |
| --- | --- |
| **Number** | #6 |
| **State** | Open |
| **Labels** | `enhancement`, `curriculum`, `vocabulary` |
| **GitHub URL** | [https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/6](https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/6) |
| **Created** | 2026-11-06T16:16:07Z |

---

Parent epic: #3

## Summary

After component kanji are learned, unlock **high-utility vocabulary** (kun’yomi, on’yomi, jukugo) that feeds the surfaces already used as tiles in Sentence Builder. Bridge isolated kanji study into real reading in the existing 120-item catalog (~335 unique correct surfaces).

## Why / Pedagogy

KKLC and WaniKani both treat vocab as the payoff for kanji. Our payoff is sharper: unlocked words should become (or light up) tiles in `exercises.json`, so morphology and structure practice reuse the same lexicon.

## Concrete scope & data/UI ideas

**Data — `vocab.json` (sketch):**
- `id`, `surface`, `reading`, `sense` (short English), `jlpt` (soft)
- `kanjiComponents[]` — kanji ids that must be known (blurred-JLPT OK)
- `readingType` — kun / on / mixed
- `exerciseTileSurfaces[]` or links to exercise tile ids where this word appears

**Domain / gating:**
- Word unlocks when its kanji prerequisites are mastered (see SRS issue)
- Optional: dim or lock Sentence Builder items whose required vocab tiles are not yet unlocked (design choice — default propose **soft gate**: items playable, but a “Study these words first” strip appears)

**UI:**
- Vocab review desk (meaning ↔ reading ↔ kanji breakdown)
- From an exercise in `GameScreen`, optional “Words in this sentence” drawer listing linked vocab + study status

**Authoring priority:**
1. Extract unique content-word surfaces from `exercises.json` (skip pure particles)
2. Attach kanji prerequisites for kanji-bearing words
3. Fill gaps with graded N5→N3 lists only where they unlock upcoming sentences

## Open design questions

1. Are particles/grammar tiles ever “vocab items,” or keep them only in Sentence Builder / Cure Dolly layers?
2. One reading per vocab card vs. multiple readings as separate cards (WaniKani-style)?
3. How to handle words like `食べます` that are already conjugated tiles — lemma `食べる` plus stem mode (sibling issue)?
