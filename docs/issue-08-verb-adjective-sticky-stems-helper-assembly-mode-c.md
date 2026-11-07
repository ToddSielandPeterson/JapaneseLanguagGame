# Issue #8: Verb & adjective “sticky stems” + helper assembly mode (Cure Dolly morphology)

| Field | Value |
| --- | --- |
| **Number** | #8 |
| **State** | Open |
| **Labels** | `enhancement`, `vocabulary`, `sentence-structure` |
| **GitHub URL** | [https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/8](https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/8) |
| **Created** | 2026-11-06T16:16:33Z |

---

Parent epic: #3

## Summary

Add a morphology desk that builds verb/adjective forms from **sticky stems** (Cure Dolly’s あ/い/う/え/お-row godan + ichidan model) plus helper tiles, instead of memorizing conjugation tables. Connect those assembled forms to Sentence Builder tiles that already encode finished morphology (ます, たい, passive, causative, modal, etc. in `exercises.json` / `TileRole`).

## Why / Pedagogy

Learners meeting `書かせました` or `待たされました` (e.g. N3 items in the catalog) as opaque tiles miss the stem+helper story. Sticky stems make helpers attach to a visible stem (`かき` + `ます`, `か` + `せる`, with the godan `あ`-stem **わ** exception: `買わない`).

## Concrete scope & data/UI ideas

**Data — `stems.json` / verb lexicon:**
- Lemma, type (`godan` / `ichidan` / irregular する・くる), dictionary form
- Five sticky stems (or generation rules + exception list)
- Helper inventory: `ます`, `たい`, `ない`, `た`, `て`, `れる`/`られる`, `せる`/`させる`, `ば`, volitional, etc.
- Mapping from assembled form → surfaces used in `exercises.json`

**Mechanic:**
- Same place/check/reveal DNA as Sentence Builder and Build-the-Kanji
- Prompt: English or “make polite non-past of 書く” → assemble stem tile + helper tile(s)
- Explicit teaching beat for `わ` on godan `あ`-stem negatives (`買わない` not `かあない`)

**Bridge:**
- From Sentence Builder, long-press a verb tile → “Break into stem + helpers” when mapping exists
- Roles already present for passive/causative/modal (`TileRole` entries in `Models.kt`) should gain authoring docs that point at stem lessons

## Open design questions

1. Generate stems algorithmically with exception tables, or author every stem string for MVP?
2. Are い-adjectives in v1, or verbs-only until the desk feels solid?
3. Do we ever replace conjugated tiles in exercises with stem+helper sequences (bigger grader change), or keep assembled forms as study-only?
