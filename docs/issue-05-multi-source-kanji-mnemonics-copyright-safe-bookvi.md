# Issue #5: Multi-source kanji mnemonics & copyright-safe book/video reference hooks

| Field | Value |
| --- | --- |
| **Number** | #5 |
| **State** | Open |
| **Labels** | `enhancement`, `radicals-kanji`, `media-references` |
| **GitHub URL** | [https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/5](https://github.com/ToddSielandPeterson/JapaneseLanguagGame/issues/5) |
| **Created** | 2026-11-06T16:15:54Z |

---

Parent epic: #3

## Summary

Schema + UI for **story/visual mnemonics** and **copyright-respecting external pointers** (not inlined book text or ripped video). Support first-party short keywords we author, learner personal notes, and deep links / citation indexes into the reference stack the curriculum is built around.

## Why / Pedagogy

Mnemonics stick when learners can jump to the story they own (book on the shelf, playlist they follow) without us redistributing protected prose or video. Multiple sources disagree on primitive names; the app should cite, not flatten, those sources.

## Reference stack (hooks, not redistributed content)

| Source | What we store |
| --- | --- |
| Lindsay Jimenez *Japanese Kanji Mnemonics* (N5: 103, N4: 181, N3: 361, N2: 415) | Series/volume, entry or kanji index, optional page |
| *JLPT Kanji Mnemonics* YouTube (N5–N1, radicals, recaps, quizzes) | Playlist id + video id / timestamp deep link |
| Heisig *Remembering the Kanji* (RTK) | Frame number + primitive name string |
| Conning *Kodansha Kanji Learner’s Course* (KKLC) | Entry number + grapheme id refs |
| Learner | Free-text personal mnemonic (local only) |

## Concrete scope & data/UI ideas

**Data (on kanji / radical records):**
```json
"references": [
  { "source": "jimenez_n5", "entry": "…", "url": "https://…" },
  { "source": "rtk", "frame": 123, "primitive": "…” },
  { "source": "kklc", "entry": 45 },
  { "source": "youtube", "videoId": "…", "t": 90 }
]
```
Optional short `keyword` / `storyBeat` fields we write ourselves (original, not copied).

**UI:**
- Detail panel on radical/kanji cards: “Open reference” buttons (external browser)
- Editable personal note field persisted with progress (see mastery/`ProgressStore` issue)
- Clear UI copy: *You need your own book/video access; we only deep-link.*

**Repo touchpoints:** new JSON beside `exercises.json`; Compose resources; no change required to Sentence Builder check logic initially. Document policy in `docs/` (extend roadmap / new `docs/0x-media-references.md`).

## Open design questions

1. Hard-fail missing Jimenez/RTK/KKLC ids vs. soft optional fields during early graph authoring?
2. Do YouTube links open in-app WebView later, or always external (safer for Wasm desktop parity)?
3. Shared `source` enum in Kotlin vs. free strings with a documented vocabulary?
