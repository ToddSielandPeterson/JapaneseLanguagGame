# Overview

## Product goal

Sentence Builder (文を作ろう) is a short drill for Japanese **word order** and **particles**. The learner sees an English prompt and a shuffled set of Japanese tiles. They arrange the tiles into one grammatical sentence, then check it. The same screen runs as a desktop window and as a web page.

The audience is someone around JLPT N5–N3 who can read kana and is learning which particle attaches to which noun, and that the verb (or copula) comes last. It is a practice desk, not a textbook and not a dictionary.

## Pedagogy

Each item isolates one pattern:

- Topic は, subject が, object を
- Destination に / へ versus place-of-action で
- Existence (に + が + います), means of transport (で), time spans (から / まで)
- N4 clause glue: たら, て-form, から, てもいい, なければならない, ようになる, ながら, より〜の方が, volitional + と思う
- N3 clause glue: ので, passive によって, causative させる, と for a natural result, のに, によって "depending on", てもらう, だけでなく, のせいで + ことになる

The English sentence is the meaning. The tiles are the only Japanese the learner may use. Particles are their own tiles so the choice of は vs が vs を is visible, not hidden inside a word.

Furigana sits above kanji so a learner can attempt the sentence without already knowing every reading. Turning furigana off is the harder pass over the same items.

Distractors are optional. Off (the default) the pool contains only tiles that belong in some acceptable answer, including the spare へ on the Tanaka/school item. On, one to three curated wrong particles or words are mixed in. Using any of them fails the check. That is the "don't just use every tile" mode.

A correct answer scores `1 + min(streak, 5)` and then increments the streak. A miss or a reveal clears the streak and does not add points. Reveal fills the sentence with the canonical answer and says it counted as a miss, so guessing via Reveal is not free.

## What shipped

- **120** exercises: 40 N5, 40 N4, 40 N3, including the original 34 from the HTML game, each with curated distractors
- JLPT selector for N5, N4, and N3. N2 is shown and disabled
- Furigana on by default, distractors off by default
- Click or drag tiles into the sentence bar; click or drag them back
- Check, Hint, Reveal, Clear, Next
- Score, streak, and best streak
- Settings and stats saved locally: a JSON file on the desktop, the same JSON in `localStorage` on the web
- No account. The desktop app makes no network calls. The web app loads its own page, fonts, and exercises from the host that serves it

## Non-goals

- Teaching grammar from zero. Hints and notes assume the learner wants a nudge, not a full lesson.
- Free typing. The point is arranging given tiles, not producing Japanese from a blank line.
- Speech, pitch accent, or listening.
- N2, N1, or N0 content. The selector reserves N2 so the control does not have to be redesigned later.
- Accounts, sync, or a server.
- Grading partial credit. The sentence matches an accepted order, or it does not.
- A content-management UI. New items are data, edited in `exercises.json`.
