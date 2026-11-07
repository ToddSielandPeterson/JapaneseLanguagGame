Summary
Add a radical/primitive layer and an interactive Build the Kanji desk that mirrors Sentence Builder’s ordered tile-selection DNA: given an English keyword/meaning prompt, learners assemble the target kanji by selecting its radicals/primitives in proper Japanese stroke order from a bank of radicals unlocked via SRS (#9).

Core mechanic & worked example (通 — "pass through")
Prompt: Show the English meaning/keyword (e.g., "pass through" for 通), optionally paired later with context or mnemonic hints (Multi-source kanji mnemonics & copyright-safe book/video reference hooks #5).
Ordered selection (stroke-order sequence): Rather than an unordered set check, components must be picked in the exact order a writer starts each component:
Example: 通 ("pass through") breaks down into 3 primitives: マ → 用 → 辶.
Because 辶 (shinnyō / road enclosure) is written last in Japanese stroke order, the learner must select マ, then 用, then 辶. This teaches real structural stroke habits (e.g., shinnyō last vs. left/top radicals first) without requiring freehand drawing hardware.
SRS-gated radical bank (Per-item mastery, lightweight SRS & content gating (ProgressStore evolution) #9):
Only radicals/primitives the learner has already introduced/learned in SRS appear in the selectable bank (no unlearned spoilers, and early N5 learners see a small, approachable tray).
Once the learned pool grows large (e.g., 100+ primitives), the desk can either let learners filter/browse their learned bank or deal a tray of the required primitives plus plausible distractors sampled strictly from their SRS-learned pool.
Why / Pedagogy
Goes beyond WaniKani-style passive recognition flashcards while staying native to the app's click/tap tile flow (GameScreen / WordTile).
Reinforces stroke-order intuition at the component level (left-to-right, top-to-bottom, inside-before-辶) instead of stroke-by-stroke pixel tracing.
Distinguishes Kangxi radicals (dictionary indexing) from functional graphemes / primitives used in stories (RTK primitives, KKLC graphemes, Jimenez visual elements — see Multi-source kanji mnemonics & copyright-safe book/video reference hooks #5).
Enforces the dependency DAG naturally: a kanji cannot enter the Build queue until its component radicals are learned in SRS (Epic: Radical → Kanji → Vocab → Sentence Builder progression (blurred JLPT ladder) #3, Per-item mastery, lightweight SRS & content gating (ProgressStore evolution) #9).
Concrete scope & data/UI linkages
Data (future Compose resources alongside exercises.json):
radicals.json — id, glyph, display name, aliases (RTK primitive, Kangxi number if applicable), stroke count, meaning keyword(s).
kanji.json — character, English prompt keyword(s), soft JLPT band, components[] ordered by first-stroke order, plus mnemonic/book/video reference hooks (Multi-source kanji mnemonics & copyright-safe book/video reference hooks #5).
Catalog validation tests (parallel to ExerciseRepository tests) ensuring no dependency cycles and every components[] id exists in radicals.json or earlier kanji.json.
Domain & UI:
Sequence validation directly parallels GameReducer checking ordered Tile sequences against answers[][] (allowing multiple valid sequences only if we ever support alternate decomposition granularities).
Visual build slot could progressively reveal/highlight each component's position inside the kanji frame as each radical is selected in order.
Cross-links:
Epic: Radical → Kanji → Vocab → Sentence Builder progression (blurred JLPT ladder) #3 (Umbrella ladder): Component prerequisites drive when a kanji unlocks across blurred JLPT lines.
Multi-source kanji mnemonics & copyright-safe book/video reference hooks #5 (Mnemonics & books/videos): Reveal / study card links the ordered primitives to the mnemonic story.
Graded vocabulary bridge: kanji → words → Sentence Builder tiles #6 (Vocab bridge): Mastering built kanji unlocks vocabulary words and Sentence Builder tiles (~234 kanji-bearing tiles in exercises.json).
Per-item mastery, lightweight SRS & content gating (ProgressStore evolution) #9 (SRS & ProgressStore): Filters the radical bank to learning+ / unlocked radicals and tracks per-kanji build mastery.
Open questions for the future design doc
Split enclosures (e.g., 囗 in 国 / 困, or 門 / 行): In handwriting, the outer box 囗 starts on stroke 1, the inner part (玉) goes next, and the bottom closing stroke of 囗 comes last. Rule of thumb: order components by which component's first stroke is written first (囗 → 玉), or model wrap-around enclosures specially?
Repeated components: For kanji like 林 (木 + 木), 森 (木 × 3), or 品 (口 × 3), can a radical tile in the bank be tapped multiple times rather than consumed on first click?
Component granularity: When a sub-kanji is itself learned (e.g., 甬 vs. マ + 用 inside 通, or 青 inside 晴), when do we chunk as a sub-kanji vs. atomic radicals?
Scaling the SRS radical bank: At 15 learned radicals, showing the whole learned bank works directly; at 150+ learned radicals, do we deal a curated subset of learned radicals (like Sentence Builder's tile pool + distractors) or provide stroke-count / shape tabs over the full learned bank?