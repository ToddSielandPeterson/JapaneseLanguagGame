package com.cognitivecreations.japaneselanguagegame.domain

import kotlinx.serialization.Serializable

/**
 * JLPT band. N2 is visible in the selector and intentionally not playable yet.
 */
enum class JlptLevel(val playable: Boolean) {
    N5(true),
    N4(true),
    N3(true),
    N2(false),
}

enum class Difficulty(val wire: String) {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced"),
    ;

    companion object {
        fun fromWire(raw: String): Difficulty =
            entries.firstOrNull { it.wire == raw }
                ?: error("Unknown difficulty '$raw'")
    }
}

/**
 * Grammatical role of a tile. The wire value matches the HTML exercise data.
 * Particle-styled tiles (particles and conjunctions) use the blue chip treatment.
 */
enum class TileRole(val wire: String) {
    TOPIC("topic"),
    SUBJECT("subject"),
    OBJECT("object"),
    PARTICLE("particle"),
    VERB("verb"),
    NOUN("noun"),
    SUFFIX("suffix"),
    PLACE("place"),
    POSITION("position"),
    TIME("time"),
    MEANS("means"),
    ADJ("adj"),
    COPULA("copula"),
    COND("cond"),
    TE("te"),
    REL("rel"),
    DET("det"),
    MUST("must"),
    POT("pot"),
    EXPR("expr"),
    ADV("adv"),
    VOL("vol"),
    AGENT("agent"),
    PASSIVE("passive"),
    CAUSER("causer"),
    CAUSEE("causee"),
    CAUS("caus"),
    CONJ("conj"),
    MODAL("modal"),
    ;

    val particleStyled: Boolean get() = this == PARTICLE || this == CONJ

    companion object {
        fun fromWire(raw: String): TileRole =
            entries.firstOrNull { it.wire == raw }
                ?: error("Unknown tile role '$raw'")
    }
}

data class Tile(
    val id: String,
    val surface: String,
    val reading: String? = null,
    val role: TileRole,
) {
    /** Furigana is shown only when the surface contains kanji and a reading is authored. */
    val showsReading: Boolean
        get() = !reading.isNullOrBlank() && surface.any { it in KANJI }

    companion object {
        /** Same CJK unified range the HTML game uses for ruby. */
        val KANJI: CharRange = '\u4E00'..'\u9FAF'
    }
}

data class Exercise(
    val id: String,
    val jlpt: JlptLevel,
    val difficulty: Difficulty,
    val english: String,
    val tiles: List<Tile>,
    val answers: List<List<String>>,
    val hint: String,
    val notes: String,
    val distractors: List<Tile>,
) {
    fun tile(id: String): Tile? =
        tiles.firstOrNull { it.id == id }
            ?: distractors.firstOrNull { it.id == id }
            ?: FallbackParticles.byId(id)

    fun answerTiles(index: Int = 0): List<Tile> {
        val ids = answers.getOrElse(index) { answers.first() }
        return ids.map { tileId ->
            tile(tileId) ?: error("Exercise $id references missing tile '$tileId' in an answer")
        }
    }
}

/**
 * Settings and score that survive restarts. Mirrors the HTML localStorage payload.
 */
@Serializable
data class PersistedProgress(
    val jlpt: String = "N5",
    val furigana: Boolean = true,
    val distractors: Boolean = false,
    val score: Int = 0,
    val streak: Int = 0,
    val best: Int = 0,
)

enum class RoundPhase {
    /** Player can place, remove, check, hint, reveal, and clear. */
    BUILDING,

    /** Incorrect check. Buttons lock briefly, then the round returns to [BUILDING]. */
    WRONG_FLASH,

    /** Accepted answer. Score already applied. Next is available. */
    SOLVED,

    /** Answer was revealed. Counts as a miss. Next is available. */
    REVEALED,
}

data class GameState(
    val jlpt: JlptLevel,
    val furigana: Boolean,
    val distractorsEnabled: Boolean,
    val score: Int,
    val streak: Int,
    val best: Int,
    val queue: List<String>,
    val index: Int,
    val tileOrder: List<String>,
    val placed: List<String>,
    val phase: RoundPhase,
    val hintShown: Boolean,
    val feedbackToken: Int,
) {
    val exerciseId: String? get() = queue.getOrNull(index)

    val inputLocked: Boolean get() = phase != RoundPhase.BUILDING

    fun toPersisted(): PersistedProgress = PersistedProgress(
        jlpt = jlpt.name,
        furigana = furigana,
        distractors = distractorsEnabled,
        score = score,
        streak = streak,
        best = best,
    )
}

sealed interface GameEvent {
    data class SelectLevel(val level: JlptLevel) : GameEvent
    data object ToggleFurigana : GameEvent
    data object ToggleDistractors : GameEvent
    data object ResetStats : GameEvent
    data class Place(val tileId: String) : GameEvent
    data class RemoveAt(val index: Int) : GameEvent
    data object Clear : GameEvent
    data object Check : GameEvent
    data object Hint : GameEvent
    data object Reveal : GameEvent
    data object Next : GameEvent
    data object DismissWrongFlash : GameEvent
}

/**
 * Shuffles a list. Production uses [DefaultShuffler]; tests inject a stable order.
 */
interface Shuffler {
    fun <T> shuffle(items: List<T>): List<T>
}

object DefaultShuffler : Shuffler {
    override fun <T> shuffle(items: List<T>): List<T> = items.toMutableList().apply { shuffle() }
}

/**
 * Particles used only when an exercise has no remaining curated distractor
 * whose surface is not already in the correct tile set.
 */
object FallbackParticles {
    val tiles: List<Tile> = listOf(
        "は", "が", "を", "に", "で", "へ", "と", "も", "の", "から", "まで",
    ).mapIndexed { index, surface ->
        Tile(id = "fb${index + 1}", surface = surface, role = TileRole.PARTICLE)
    }

    fun byId(id: String): Tile? {
        tiles.firstOrNull { it.id == id }?.let { return it }
        val prefix = "d_fb_"
        if (!id.startsWith(prefix)) return null
        val surface = id.removePrefix(prefix)
        return tiles.firstOrNull { it.surface == surface }?.copy(id = id)
    }
}
