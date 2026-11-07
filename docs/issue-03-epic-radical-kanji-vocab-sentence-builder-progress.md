# Issue #3: Epic: Radical → Kanji → Vocab → Sentence Builder progression (blurred JLPT ladder)

| Field | Value |
| --- | --- |
| **Number** | #3 |
| **State** | Open |
| **Labels** | `enhancement`, `curriculum`, `srs-progression` |
| **GitHub URL** | [https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/3](https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/3) |
| **Created** | 2026-11-06T16:15:20Z |

---

## Summary

Evolve Sentence Builder (`文を作ろう`) from a standalone particle/word-order drill into a full learning program with a WaniKani-inspired ladder:

**Radicals / primitives → Kanji → Vocabulary → Sentence Builder**

Anchor the ladder to JLPT tiers (N5 → N4 → N3 → N2 → N1) while **deliberately blurring JLPT lines** whenever a higher-JLPT or non-JLPT component must be taught first so a target kanji or word can be built honestly (RTK + KKLC component dependencies; Lindsay Jimenez JLPT Kanji Mnemonics tier lists).

Today the repo only ships the last rung: 120 N5–N3 sentence exercises in `exercises.json`, one `GameScreen`, and global score/streak in `ProgressStore`. Kanji appear only as tile surfaces + optional furigana (~234 kanji-bearing surfaces among ~335 unique correct tiles). There is no radical graph, vocab unlock path, mnemonic hooks, morphology mode, or per-item mastery.

## Why / Pedagogy

- WaniKani shows that gated radicals → kanji → vocab works; we want the same spine **plus interactive kanji assembly** (not only recognition).
- JLPT bands are useful marketing/anchors, but real component order often crosses bands. Prefer “needed before X” over “strict N5-only then N4-only.”
- Sentence Builder already trains structure; upstream modes should unlock the tiles that appear in `exercises.json` rather than living as a disconnected flashcard app.

## Grounding in the current codebase

| Existing | Path / note |
| --- | --- |
| Exercise catalog | `src/commonMain/composeResources/files/exercises.json` (40 N5 / 40 N4 / 40 N3) |
| Domain models | `…/domain/Models.kt` — `JlptLevel` (N2 reserved, not playable), `Tile`, `Exercise`, `PersistedProgress` |
| Rules | `…/domain/GameReducer.kt` |
| UI | `…/ui/GameScreen.kt`, `WordTile.kt`, `FuriganaText.kt` |
| Progress | `ProgressStore` / `ProgressCodec` — `{ jlpt, furigana, distractors, score, streak, best }` |
| Docs / roadmap | `docs/00-overview.md` … `docs/05-roadmap.md` (N2/N1 content, audio, mobile; no radical/vocab ladder yet) |

## Pillar checklist (child issues)

- [ ] #4 — Radical & primitive graph + interactive “Build the Kanji” mechanic
- [ ] #5 — Multi-source kanji mnemonics & copyright-safe book/video reference hooks
- [ ] #6 — Graded vocabulary bridge: kanji → words → Sentence Builder tiles
- [ ] #7 — Cure Dolly structural grammar layer inside Sentence Builder
- [ ] #8 — Verb & adjective “sticky stems” + helper assembly mode
- [ ] #9 — Per-item mastery, lightweight SRS & content gating (`ProgressStore`)

## Out of scope for this epic (for now)

- Accounts / cloud sync (keep offline-first; export/import OK)
- Replacing Sentence Builder with free typing
- Shipping copyrighted book prose or video audio inside the app

## Open design questions

1. One Gradle module with multiple “desk” modes vs. separate feature modules?
2. How hard do we gate Sentence Builder items on upstream mastery vs. keep today’s free N5/N4/N3 selector?
3. Canonical ordering source of truth when RTK, KKLC, and JLPT Kanji Mnemonics disagree?
