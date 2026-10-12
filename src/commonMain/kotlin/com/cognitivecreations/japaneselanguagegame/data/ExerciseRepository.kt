package com.cognitivecreations.japaneselanguagegame.data

import com.cognitivecreations.japaneselanguagegame.domain.Difficulty
import com.cognitivecreations.japaneselanguagegame.domain.Exercise
import com.cognitivecreations.japaneselanguagegame.domain.ExerciseCatalog
import com.cognitivecreations.japaneselanguagegame.domain.JlptLevel
import com.cognitivecreations.japaneselanguagegame.domain.Tile
import com.cognitivecreations.japaneselanguagegame.domain.TileRole
import com.cognitivecreations.japaneselanguagegame.generated.resources.Res
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Loads the ported exercise catalog from Compose resources (`files/exercises.json`).
 * The file is the full N5/N4/N3 set from the HTML game, including curated distractors
 * and the n5-03 に/へ alternate. Desktop and web both read this same resource.
 */
object ExerciseRepository {
    const val RESOURCE_PATH: String = "files/exercises.json"

    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun load(): ExerciseCatalog {
        val text = Res.readBytes(RESOURCE_PATH).decodeToString()
        return parse(text)
    }

    fun parse(text: String): ExerciseCatalog {
        val dtos = json.decodeFromString<List<ExerciseDto>>(text)
        val exercises = dtos.map { it.toDomain() }
        val ids = exercises.map { it.id }
        require(ids.size == ids.toSet().size) { "Duplicate exercise ids in catalog" }
        require(exercises.isNotEmpty()) { "Exercise catalog is empty" }
        return ExerciseCatalog(exercises)
    }
}

@Serializable
private data class ExerciseDto(
    val id: String,
    val jlpt: String,
    val level: String,
    val english: String,
    val tiles: List<TileDto>,
    val answers: List<List<String>>,
    val hint: String = "",
    val notes: String = "",
    val distractors: List<TileDto> = emptyList(),
) {
    fun toDomain(): Exercise {
        val jlptLevel = JlptLevel.entries.firstOrNull { it.name == jlpt }
            ?: error("Exercise $id has unknown JLPT level '$jlpt'")
        require(tiles.isNotEmpty()) { "Exercise $id has no tiles" }
        require(answers.isNotEmpty()) { "Exercise $id has no answers" }
        val tileIds = tiles.map { it.id }.toSet()
        require(tileIds.size == tiles.size) { "Exercise $id has duplicate tile ids" }
        answers.forEach { answer ->
            require(answer.isNotEmpty()) { "Exercise $id has an empty answer" }
            answer.forEach { tileId ->
                require(tileId in tileIds) { "Exercise $id answer references missing tile '$tileId'" }
            }
        }
        val surfaces = tiles.map { it.surface }.toSet()
        distractors.forEach { distractor ->
            require(distractor.surface !in surfaces) {
                "Exercise $id distractor '${distractor.surface}' duplicates a correct tile"
            }
        }
        return Exercise(
            id = id,
            jlpt = jlptLevel,
            difficulty = Difficulty.fromWire(level),
            english = english,
            tiles = tiles.map { it.toDomain() },
            answers = answers,
            hint = hint,
            notes = notes,
            distractors = distractors.map { it.toDomain() },
        )
    }
}

@Serializable
private data class TileDto(
    val id: String,
    val surface: String,
    val reading: String? = null,
    val role: String,
) {
    fun toDomain(): Tile = Tile(
        id = id,
        surface = surface,
        reading = reading,
        role = TileRole.fromWire(role),
    )
}
