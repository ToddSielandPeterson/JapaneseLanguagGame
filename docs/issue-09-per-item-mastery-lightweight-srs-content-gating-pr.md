# Issue #9: Per-item mastery, lightweight SRS & content gating (ProgressStore evolution)

| Field | Value |
| --- | --- |
| **Number** | #9 |
| **State** | Open |
| **Labels** | `enhancement`, `srs-progression` |
| **GitHub URL** | [https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/9](https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/9) |
| **Created** | 2026-11-06T16:16:45Z |

---

Parent epic: #3

> *First-pass design idea & cross-area linkages — to be expanded into a formal design doc under `docs/` later.*

## Summary

Evolve local persistence from global session stats to **per-item mastery** across Radicals, Kanji, Vocab, and Sentences — still offline-first, still no accounts. Add optional JSON **export/import** so Wasm `localStorage` and desktop `progress.json` are not one-browser traps.

## Why / Pedagogy

Today `PersistedProgress` (`Models.kt`) is only:

```json
{ "jlpt", "furigana", "distractors", "score", "streak", "best" }
```

Deck order and item history are not saved (`docs/02-data-model.md`). That fits a casual drill; it cannot gate a radical→kanji→vocab→sentence ladder, filter candidate banks to learned items, or schedule reviews.

## Concrete scope & data/UI ideas

**Schema (backward compatible):**
- Keep existing fields for Sentence Builder score UX
- Add `items: { "<kind>:<id>": { stage, due, streak, last } }` (or parallel maps per kind)
- `kind` ∈ `radical | kanji | vocab | sentence`
- Stages: e.g. `new → learning → reviewing → mastered` (exact names TBD; SM-2-lite or fixed interval ladder — prefer simple intervals over full Anki parity for MVP)

**Stores:**
- `ProgressCodec` sanitize unknown keys / old files (already falls back on corrupt JSON)
- `FileProgressStore` (JVM) + `KeyValueProgressStore` / `BrowserProgressStore` (wasm) keep the same document
- Export / Import buttons on the desk (download/upload JSON); document in README

**Gating & bank-filtering hooks:**
- **SRS-filtered radical bank in Kanji Builder (#4):** Limit the selectable bank of radicals (and any distractor pool) in "Build the Kanji" to radicals the user has already learned through SRS, and unlock a kanji for building once its component radicals reach `learning+`.
- **Vocab unlocking (#6):** Unlock vocabulary items when component kanji prerequisites are met.
- **Sentence Builder gating:** Soft- or hard-gate sentence ids in the queue builder inside `GameReducer` level load.

**Tests:** extend `ProgressStoreTest` / `KeyValueProgressStoreTest`; migration test from v1 payload → v2

## Open design questions

1. Soft gate (recommend) vs hard gate (block) for the existing free N5/N4/N3 selector?
2. Does Reveal in Sentence Builder or Kanji Builder reset mastery the same as a wrong Check?
3. Single shared SRS clock for all kinds, or separate queues per desk?
