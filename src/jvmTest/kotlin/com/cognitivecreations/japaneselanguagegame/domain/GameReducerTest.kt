package com.cognitivecreations.japaneselanguagegame.domain

import com.cognitivecreations.japaneselanguagegame.data.ExerciseRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameReducerTest {
    private val catalog = runBlocking { ExerciseRepository.load() }

    private object StableShuffler : Shuffler {
        override fun <T> shuffle(items: List<T>): List<T> = items.toList()
    }

    @Test
    fun catalogHasTheFullPortedSet() {
        assertEquals(ExerciseCatalog.EXPECTED_COUNT, catalog.exercises.size)
        assertEquals(120, catalog.exercises.size)
        assertEquals(40, catalog.forLevel(JlptLevel.N5).size)
        assertEquals(40, catalog.forLevel(JlptLevel.N4).size)
        assertEquals(40, catalog.forLevel(JlptLevel.N3).size)
        assertEquals(0, catalog.forLevel(JlptLevel.N2).size)
        assertEquals((1..40).map { "n5-%02d".format(it) }, catalog.forLevel(JlptLevel.N5).map { it.id })
        assertEquals((1..40).map { "n4-%02d".format(it) }, catalog.forLevel(JlptLevel.N4).map { it.id })
        assertEquals((1..40).map { "n3-%02d".format(it) }, catalog.forLevel(JlptLevel.N3).map { it.id })
        catalog.exercises.forEach { exercise ->
            val difficulty = when (exercise.jlpt) {
                JlptLevel.N5 -> Difficulty.BEGINNER
                JlptLevel.N4 -> Difficulty.INTERMEDIATE
                JlptLevel.N3 -> Difficulty.ADVANCED
                JlptLevel.N2 -> error("N2 is not in the catalog")
            }
            assertEquals(difficulty, exercise.difficulty, exercise.id)
        }
        assertEquals("I eat sushi.", catalog.byId("n5-01")!!.english)
        assertEquals("I ended up staying until late because of work.", catalog.byId("n3-09")!!.english)
    }

    @Test
    fun everyKanjiTileHasAReadingAndAnswersResolve() {
        catalog.exercises.forEach { exercise ->
            (exercise.tiles + exercise.distractors).forEach { tile ->
                if (tile.surface.any { it in Tile.KANJI }) {
                    val reading = tile.reading
                    assertFalse(reading.isNullOrBlank(), "${exercise.id} ${tile.surface} missing furigana")
                    assertTrue(
                        reading.all { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' },
                        "${exercise.id} ${tile.surface} reading is not kana: $reading",
                    )
                }
            }
            exercise.answers.forEach { answer ->
                answer.forEach { id ->
                    assertTrue(exercise.tiles.any { it.id == id }, "${exercise.id} missing $id")
                }
            }
            assertTrue(exercise.distractors.isNotEmpty(), "${exercise.id} has no distractors")
        }
    }

    @Test
    fun everyAnswerReassemblesFromItsTiles() {
        catalog.exercises.forEach { exercise ->
            assertTrue(exercise.answers.isNotEmpty(), exercise.id)
            exercise.answers.forEach { answer ->
                val sentence = answer.joinToString("") { id ->
                    exercise.tiles.first { it.id == id }.surface
                }
                assertTrue(sentence.isNotEmpty(), exercise.id)
                assertTrue(GameReducer.isCorrect(exercise, answer), "${exercise.id} $sentence")
            }
        }
    }

    @Test
    fun n5DestinationAcceptsNiOrHe() {
        val school = catalog.byId("n5-03")!!
        assertTrue(GameReducer.isCorrect(school, listOf("t1", "t2", "t3", "t4", "t5", "t6")))
        assertTrue(GameReducer.isCorrect(school, listOf("t1", "t2", "t3", "t4", "t7", "t6")))
        assertFalse(GameReducer.isCorrect(school, listOf("t1", "t2", "t3", "t4", "t5", "t7", "t6")))
    }

    @Test
    fun duplicateSurfacesAreInterchangeable() {
        val nagara = catalog.byId("n4-11")!!
        assertTrue(GameReducer.isCorrect(nagara, listOf("t1", "t5", "t3", "t4", "t2", "t6")))
        val bath = catalog.byId("n3-04")!!
        assertTrue(GameReducer.isCorrect(bath, listOf("t1", "t7", "t3", "t4", "t5", "t6", "t2", "t8")))
    }

    @Test
    fun distractorFailsEvenWhenTheRestIsRight() {
        val sushi = catalog.byId("n5-01")!!
        assertFalse(GameReducer.isCorrect(sushi, listOf("t1", "d1", "t3", "t4", "t5")))
        val active = GameReducer.activeTiles(sushi, distractorsEnabled = true)
        assertEquals(8, active.size)
        assertEquals(5, GameReducer.activeTiles(sushi, distractorsEnabled = false).size)
    }

    @Test
    fun scoringMatchesTheHtmlStreakBonus() {
        var state = GameReducer.start(catalog, shuffler = StableShuffler)
        val exercise = catalog.byId(state.exerciseId!!)!!
        state = placeAnswer(state, exercise.answers.first())
        state = GameReducer.reduce(state, GameEvent.Check, catalog, StableShuffler)
        assertEquals(RoundPhase.SOLVED, state.phase)
        assertEquals(1, state.score)
        assertEquals(1, state.streak)
        assertEquals(1, state.best)

        state = GameReducer.reduce(state, GameEvent.Next, catalog, StableShuffler)
        val second = catalog.byId(state.exerciseId!!)!!
        state = placeAnswer(state, second.answers.first())
        state = GameReducer.reduce(state, GameEvent.Check, catalog, StableShuffler)
        assertEquals(3, state.score)
        assertEquals(2, state.streak)
        assertEquals(2, state.best)

        state = GameReducer.reduce(state, GameEvent.Next, catalog, StableShuffler)
        state = GameReducer.reduce(state, GameEvent.Place("t1"), catalog, StableShuffler)
        state = GameReducer.reduce(state, GameEvent.Check, catalog, StableShuffler)
        assertEquals(RoundPhase.WRONG_FLASH, state.phase)
        assertEquals(3, state.score)
        assertEquals(0, state.streak)
        assertEquals(2, state.best)

        state = GameReducer.reduce(state, GameEvent.DismissWrongFlash, catalog, StableShuffler)
        assertEquals(RoundPhase.BUILDING, state.phase)
        state = GameReducer.reduce(state, GameEvent.Clear, catalog, StableShuffler)
        val third = catalog.byId(state.exerciseId!!)!!
        state = placeAnswer(state, third.answers.first())
        state = GameReducer.reduce(state, GameEvent.Check, catalog, StableShuffler)
        assertEquals(4, state.score)
        assertEquals(1, state.streak)
        assertEquals(2, state.best)
    }

    @Test
    fun revealIsAMissAndDoesNotAddScore() {
        var state = GameReducer.start(
            catalog,
            PersistedProgress(score = 4, streak = 3, best = 6),
            StableShuffler,
        )
        state = GameReducer.reduce(state, GameEvent.Reveal, catalog, StableShuffler)
        assertEquals(RoundPhase.REVEALED, state.phase)
        assertEquals(4, state.score)
        assertEquals(0, state.streak)
        assertEquals(6, state.best)
        assertEquals(catalog.byId("n5-01")!!.answers.first(), state.placed)
        state = GameReducer.reduce(state, GameEvent.Check, catalog, StableShuffler)
        assertEquals(RoundPhase.REVEALED, state.phase)
    }

    @Test
    fun levelChangeKeepsScoreAndN2DoesNothing() {
        var state = GameReducer.start(catalog, PersistedProgress(score = 9, streak = 2, best = 4), StableShuffler)
        state = GameReducer.reduce(state, GameEvent.SelectLevel(JlptLevel.N2), catalog, StableShuffler)
        assertEquals(JlptLevel.N5, state.jlpt)
        state = GameReducer.reduce(state, GameEvent.SelectLevel(JlptLevel.N4), catalog, StableShuffler)
        assertEquals(JlptLevel.N4, state.jlpt)
        assertEquals("n4-01", state.exerciseId)
        assertEquals(9, state.score)
        assertEquals(2, state.streak)
        assertTrue(state.placed.isEmpty())
    }

    @Test
    fun togglesAndReset() {
        var state = GameReducer.start(catalog, shuffler = StableShuffler)
        assertTrue(state.furigana)
        assertFalse(state.distractorsEnabled)
        state = GameReducer.reduce(state, GameEvent.Place("t1"), catalog, StableShuffler)
        state = GameReducer.reduce(state, GameEvent.ToggleDistractors, catalog, StableShuffler)
        assertTrue(state.distractorsEnabled)
        assertTrue(state.placed.isEmpty())
        assertTrue(state.tileOrder.any { it.startsWith("d") })
        state = GameReducer.reduce(state, GameEvent.ToggleFurigana, catalog, StableShuffler)
        assertFalse(state.furigana)
        state = state.copy(score = 3, streak = 2, best = 5)
        state = GameReducer.reduce(state, GameEvent.ResetStats, catalog, StableShuffler)
        assertEquals(0, state.score)
        assertEquals(0, state.streak)
        assertEquals(0, state.best)
        assertFalse(state.furigana)
    }

    @Test
    fun nextWrapsTheQueue() {
        var state = GameReducer.start(catalog, PersistedProgress(jlpt = "N3"), StableShuffler)
        val n3 = catalog.forLevel(JlptLevel.N3)
        repeat(n3.size - 1) {
            state = GameReducer.reduce(state, GameEvent.Reveal, catalog, StableShuffler)
            state = GameReducer.reduce(state, GameEvent.Next, catalog, StableShuffler)
        }
        assertEquals(n3.last().id, state.exerciseId)
        state = GameReducer.reduce(state, GameEvent.Reveal, catalog, StableShuffler)
        state = GameReducer.reduce(state, GameEvent.Next, catalog, StableShuffler)
        assertEquals(n3.first().id, state.exerciseId)
        assertEquals(0, state.index)
    }

    @Test
    fun poolIsReshuffledWhenItWouldAlreadyBeTheAnswer() {
        var calls = 0
        val shuffler = object : Shuffler {
            override fun <T> shuffle(items: List<T>): List<T> {
                calls += 1
                return if (calls == 1) items.toList() else items.reversed()
            }
        }
        val ids = listOf("t1", "t2", "t3")
        val order = GameReducer.shuffledTileOrder(ids, listOf(ids), shuffler)
        assertEquals(listOf("t3", "t2", "t1"), order)
        assertEquals(2, calls)
    }

    @Test
    fun fallbackParticlesFillWhenCuratedOnesCollide() {
        val exercise = Exercise(
            id = "synthetic",
            jlpt = JlptLevel.N5,
            difficulty = Difficulty.BEGINNER,
            english = "synthetic",
            tiles = listOf(Tile("t1", "猫", "ねこ", TileRole.NOUN)),
            answers = listOf(listOf("t1")),
            hint = "",
            notes = "",
            distractors = listOf(Tile("d1", "猫", "ねこ", TileRole.NOUN)),
        )
        val active = GameReducer.activeTiles(exercise, distractorsEnabled = true)
        assertEquals(3, active.size)
        assertTrue(active.drop(1).all { it.id.startsWith("d_fb_") })
    }

    private fun placeAnswer(state: GameState, answer: List<String>): GameState {
        var current = state
        answer.forEach { id ->
            current = GameReducer.reduce(current, GameEvent.Place(id), catalog, StableShuffler)
        }
        return current
    }
}
