# JapaneseLanguageGame docs

Design notes for Sentence Builder (文を作ろう). The running app is the source of truth for behavior; these pages explain why it is shaped this way and how to extend it. Hosting the web build is [deploy-amplify.md](deploy-amplify.md).

| Doc | What it covers |
| --- | --- |
| [00-overview.md](00-overview.md) | Product goal, pedagogy, what this version deliberately does not do |
| [01-architecture.md](01-architecture.md) | Gradle module, packages, UI / domain / data split |
| [02-data-model.md](02-data-model.md) | Exercise, Tile, JLPT level, settings, scoring |
| [03-ux-flows.md](03-ux-flows.md) | Screen, toggles, check / hint / reveal / next, feedback |
| [04-exercise-format.md](04-exercise-format.md) | How a sentence is authored in JSON, distractors, alternate answers |
| [05-roadmap.md](05-roadmap.md) | N2/N1, audio, more sentences, mobile |
| [deploy-amplify.md](deploy-amplify.md) | Publish the web build with AWS Amplify Hosting |
| [KanjiBuild.md](KanjiBuild.md) | Design notes for interactive Build the Kanji desk |

## GitHub Issues

Tracked feature issues and architecture proposals imported from GitHub:

| Issue | Title | Labels |
| --- | --- | --- |
| [#3](issue-03-epic-radical-kanji-vocab-sentence-builder-progress.md) | Epic: Radical → Kanji → Vocab → Sentence Builder progression (blurred JLPT ladder) | `enhancement`, `curriculum`, `srs-progression` |
| [#4](issue-04-radical-primitive-graph-interactive-build-the-kanj.md) | Radical & primitive graph + interactive “Build the Kanji” mechanic | `enhancement`, `curriculum`, `radicals-kanji`, `srs-progression` |
| [#5](issue-05-multi-source-kanji-mnemonics-copyright-safe-bookvi.md) | Multi-source kanji mnemonics & copyright-safe book/video reference hooks | `enhancement`, `radicals-kanji`, `media-references` |
| [#6](issue-06-graded-vocabulary-bridge-kanji-words-sentence-buil.md) | Graded vocabulary bridge: kanji → words → Sentence Builder tiles | `enhancement`, `curriculum`, `vocabulary` |
| [#7](issue-07-cure-dolly-structural-grammar-layer-inside-sentenc.md) | Cure Dolly structural grammar layer inside Sentence Builder (文を作ろう) | `enhancement`, `curriculum`, `sentence-structure`, `media-references` |
| [#8](issue-08-verb-adjective-sticky-stems-helper-assembly-mode-c.md) | Verb & adjective “sticky stems” + helper assembly mode (Cure Dolly morphology) | `enhancement`, `vocabulary`, `sentence-structure` |
| [#9](issue-09-per-item-mastery-lightweight-srs-content-gating-pr.md) | Per-item mastery, lightweight SRS & content gating (ProgressStore evolution) | `enhancement`, `srs-progression` |

Open the project from the repository root and run the desktop app with `./gradlew run`. See the root [README](../README.md).
