# UX flows

There is one screen. The window title is `Sentence Builder — 文を作ろう`. Minimum size is 780×640; the layout centers a column at most 920 dp wide and scrolls, with a scrollbar, when the window is shorter than the content.

## Header

Title, one-line description, and three pills: Score, Streak, Best. Numbers use the warm accent.

## Controls

A segmented control: **N5**, **N4**, **N3**, **N2**. N2 is dimmed and does not switch the deck. Its accessible name says "coming soon".

Toggles:

| Control | Default | Effect |
| --- | --- | --- |
| Distractors | OFF | ON mixes in the extra tiles and **restarts the current sentence** (placement cleared, hint hidden, pool reshuffled). |
| ふりがな | ON | Shows or hides readings immediately, without resetting the sentence. |
| Reset | — | Asks "Reset score, streak, and best?" Cancel leaves everything. Confirm zeros the three numbers only. |

Switching N5/N4/N3 loads a newly shuffled deck of that level at item 1 and keeps the score. The badge and the `3 / 14` style counter follow the deck.

## A round

1. Read the English prompt.
2. The sentence bar starts empty: "Tap tiles below to build the sentence". The border is dashed.
3. The pool shows every tile for this round, shuffled. Particles and conjunctions are blue; other words are neutral. A placed tile stays in the pool at low opacity and cannot be taken twice.
4. Click a pool tile to append it. Click a bar tile to return it. Drag from the pool onto the bar to append. Drag from the bar back to the pool to return it. Dragging within the bar moves that tile to the end (the same as the HTML drop, which always appended).
5. **Clear** (or Esc) empties the bar.
6. **Hint** (or H) reveals the structure hint under the prompt. It stays up for the rest of the round.
7. **Check** (or Enter) when the bar is not empty.
8. **Reveal** fills the bar with the first accepted answer, marks a miss, and shows that answer again under the feedback with furigana if the toggle is on.
9. **Next** (or N, or Enter once the round is locked) advances. After the last item the deck reshuffles and starts again.

Check, Hint, Reveal, and Clear disable while the round is locked or flashing. Next enables only after a correct check or a reveal.

## Feedback

| State | Bar | Message |
| --- | --- | --- |
| Building | Dashed, slot color | Empty |
| Correct | Solid green | "Correct." plus the notes |
| Wrong, ~900 ms | Solid red | "Not quite…" plus a short particle/order reminder. Then the bar unlocks and the message clears. The tiles stay so the learner can edit. |
| Revealed | Solid red | "Answer revealed. This counts as a miss." plus the answer and the notes |

Enter during the red flash does nothing. In the HTML client, Enter during that brief lock called Next; this port waits until the result is actually settled (correct or revealed).

## Keyboard

Ignored while the reset dialog is open, and ignored with Ctrl, Alt, or Meta so shortcuts do not fight the OS.

| Key | When | Action |
| --- | --- | --- |
| Enter | Building, bar not empty | Check |
| Enter | Solved or revealed | Next |
| H | Building | Hint |
| N | Solved or revealed | Next |
| Esc | Building | Clear |

## Empty and failure states

- A level with no exercises shows "No exercises for this level." N2 never gets that far because it cannot be selected.
- If `exercises.json` is missing or fails validation, startup throws. That is a build error, not a runtime mode.
- If the progress file is missing, unreadable, or not JSON, the desk starts at N5 with furigana on, distractors off, and zeros. The game still opens.
- If saving the progress file throws an `IOException`, the in-memory score stays and a line is written to stderr. Play continues.

## Desktop and narrow windows

Controls and the action row wrap. Tiles wrap inside the bar and the pool. Furigana makes chips taller; the bar's minimum height fits one furigana chip. The bundled Noto Sans JP subset covers the exercise text, kana, and the UI's Japanese.
