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

Open the project from the repository root and run the desktop app with `./gradlew run`. See the root [README](../README.md).
