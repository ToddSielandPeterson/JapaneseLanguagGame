package com.cognitivecreations.japaneselanguagegame.domain

/**
 * Pure game rules ported from the HTML client.
 *
 * Scoring on a correct check (applied before the streak increments):
 * `score += 1 + min(streak, 5)`, then `streak += 1`, and best is raised if needed.
 * A wrong check or a reveal sets the streak back to 0 and does not change the score.
 * A wrong check unlocks again after a short flash so the player can edit and retry.
 */
object GameReducer {
    fun start(
        catalog: ExerciseCatalog,
        saved: PersistedProgress = PersistedProgress(),
        shuffler: Shuffler = DefaultShuffler,
    ): GameState {
        val level = JlptLevel.entries.firstOrNull { it.name == saved.jlpt && it.playable }
            ?: JlptLevel.N5
        val base = GameState(
            jlpt = level,
            furigana = saved.furigana,
            distractorsEnabled = saved.distractors,
            score = saved.score.coerceAtLeast(0),
            streak = saved.streak.coerceAtLeast(0),
            best = saved.best.coerceAtLeast(0),
            queue = emptyList(),
            index = 0,
            tileOrder = emptyList(),
            placed = emptyList(),
            phase = RoundPhase.BUILDING,
            hintShown = false,
            feedbackToken = 0,
        )
        val queue = shuffler.shuffle(catalog.forLevel(level).map { it.id })
        return beginRound(base, catalog, shuffler, index = 0, queue = queue)
    }

    fun reduce(
        state: GameState,
        event: GameEvent,
        catalog: ExerciseCatalog,
        shuffler: Shuffler = DefaultShuffler,
    ): GameState = when (event) {
        is GameEvent.SelectLevel -> selectLevel(state, event.level, catalog, shuffler)
        GameEvent.ToggleFurigana -> state.copy(furigana = !state.furigana)
        GameEvent.ToggleDistractors -> beginRound(
            state.copy(distractorsEnabled = !state.distractorsEnabled),
            catalog,
            shuffler,
            index = state.index,
            queue = state.queue,
        )
        GameEvent.ResetStats -> state.copy(score = 0, streak = 0, best = 0)
        is GameEvent.Place -> place(state, event.tileId)
        is GameEvent.RemoveAt -> removeAt(state, event.index)
        GameEvent.Clear -> if (state.phase == RoundPhase.BUILDING) state.copy(placed = emptyList()) else state
        GameEvent.Check -> check(state, catalog)
        GameEvent.Hint -> if (state.phase == RoundPhase.BUILDING) state.copy(hintShown = true) else state
        GameEvent.Reveal -> reveal(state, catalog)
        GameEvent.Next -> next(state, catalog, shuffler)
        GameEvent.DismissWrongFlash ->
            if (state.phase == RoundPhase.WRONG_FLASH) state.copy(phase = RoundPhase.BUILDING) else state
    }

    fun activeTiles(exercise: Exercise, distractorsEnabled: Boolean): List<Tile> {
        if (!distractorsEnabled) return exercise.tiles
        val used = exercise.tiles.map { it.surface }.toMutableSet()
        val extra = mutableListOf<Tile>()
        for (distractor in exercise.distractors) {
            if (distractor.surface in used) continue
            used += distractor.surface
            extra += distractor
            if (extra.size >= 3) break
        }
        if (extra.isEmpty()) {
            for (particle in FallbackParticles.tiles) {
                if (particle.surface in used) continue
                extra += particle.copy(id = "d_fb_${particle.surface}")
                if (extra.size >= 2) break
            }
        }
        return exercise.tiles + extra
    }

    /**
     * Fisher-Yates (or the injected shuffler) with up to eight retries so the pool
     * is not handed to the player already in a fully correct order.
     */
    fun shuffledTileOrder(
        ids: List<String>,
        answers: List<List<String>>,
        shuffler: Shuffler,
    ): List<String> {
        if (ids.isEmpty()) return emptyList()
        val canons = answers.map { it.joinToString(",") }.toSet()
        var order = shuffler.shuffle(ids)
        var guard = 0
        while (order.joinToString(",") in canons && guard++ < 8) {
            order = shuffler.shuffle(ids)
        }
        return order
    }

    /**
     * Accepts an exact tile-id sequence, or the same surfaces in the same order.
     * Identical tiles (two を, two に) are interchangeable. Any id that is not one
     * of the exercise's real tiles — a distractor — fails the check.
     */
    fun isCorrect(exercise: Exercise, placed: List<String>): Boolean {
        if (placed.isEmpty()) return false
        val legit = exercise.tiles.map { it.id }.toSet()
        if (placed.any { it !in legit }) return false
        if (exercise.answers.any { it == placed }) return true
        val surfaceOf = exercise.tiles.associate { it.id to it.surface }
        val placedSurfaces = placed.map { surfaceOf[it] ?: return false }
        return exercise.answers.any { answer ->
            answer.map { surfaceOf.getValue(it) } == placedSurfaces
        }
    }

    private fun selectLevel(
        state: GameState,
        level: JlptLevel,
        catalog: ExerciseCatalog,
        shuffler: Shuffler,
    ): GameState {
        if (!level.playable) return state
        val queue = shuffler.shuffle(catalog.forLevel(level).map { it.id })
        return beginRound(state.copy(jlpt = level), catalog, shuffler, index = 0, queue = queue)
    }

    private fun beginRound(
        state: GameState,
        catalog: ExerciseCatalog,
        shuffler: Shuffler,
        index: Int,
        queue: List<String>,
    ): GameState {
        val exercise = queue.getOrNull(index)?.let(catalog::byId)
        val order = if (exercise == null) {
            emptyList()
        } else {
            shuffledTileOrder(
                activeTiles(exercise, state.distractorsEnabled).map { it.id },
                exercise.answers,
                shuffler,
            )
        }
        return state.copy(
            queue = queue,
            index = index,
            tileOrder = order,
            placed = emptyList(),
            phase = RoundPhase.BUILDING,
            hintShown = false,
        )
    }

    private fun place(state: GameState, tileId: String): GameState {
        if (state.phase != RoundPhase.BUILDING) return state
        if (tileId !in state.tileOrder) return state
        if (tileId in state.placed) return state
        return state.copy(placed = state.placed + tileId)
    }

    private fun removeAt(state: GameState, index: Int): GameState {
        if (state.phase != RoundPhase.BUILDING) return state
        if (index !in state.placed.indices) return state
        return state.copy(placed = state.placed.filterIndexed { i, _ -> i != index })
    }

    private fun check(state: GameState, catalog: ExerciseCatalog): GameState {
        if (state.phase != RoundPhase.BUILDING) return state
        val exercise = state.exerciseId?.let(catalog::byId) ?: return state
        if (state.placed.isEmpty()) return state
        if (isCorrect(exercise, state.placed)) {
            val gained = 1 + minOf(state.streak, 5)
            val streak = state.streak + 1
            return state.copy(
                phase = RoundPhase.SOLVED,
                score = state.score + gained,
                streak = streak,
                best = maxOf(state.best, streak),
            )
        }
        return state.copy(
            phase = RoundPhase.WRONG_FLASH,
            streak = 0,
            feedbackToken = state.feedbackToken + 1,
        )
    }

    private fun reveal(state: GameState, catalog: ExerciseCatalog): GameState {
        if (state.phase != RoundPhase.BUILDING) return state
        val exercise = state.exerciseId?.let(catalog::byId) ?: return state
        val canonical = exercise.answers.firstOrNull() ?: return state
        return state.copy(
            phase = RoundPhase.REVEALED,
            placed = canonical,
            streak = 0,
        )
    }

    private fun next(state: GameState, catalog: ExerciseCatalog, shuffler: Shuffler): GameState {
        if (state.phase != RoundPhase.SOLVED && state.phase != RoundPhase.REVEALED) return state
        if (state.queue.isEmpty()) return state
        if (state.index >= state.queue.lastIndex) {
            val queue = shuffler.shuffle(catalog.forLevel(state.jlpt).map { it.id })
            return beginRound(state, catalog, shuffler, index = 0, queue = queue)
        }
        return beginRound(state, catalog, shuffler, index = state.index + 1, queue = state.queue)
    }
}

class ExerciseCatalog(exercises: List<Exercise>) {
    val exercises: List<Exercise> = exercises

    private val byIdMap: Map<String, Exercise> = exercises.associateBy { it.id }

    fun byId(id: String): Exercise? = byIdMap[id]

    fun forLevel(level: JlptLevel): List<Exercise> = exercises.filter { it.jlpt == level }

    companion object {
        const val EXPECTED_COUNT: Int = 34
    }
}
