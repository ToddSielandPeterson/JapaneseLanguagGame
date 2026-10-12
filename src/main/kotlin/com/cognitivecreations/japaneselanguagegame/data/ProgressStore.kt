package com.cognitivecreations.japaneselanguagegame.data

import com.cognitivecreations.japaneselanguagegame.domain.PersistedProgress
import kotlinx.serialization.json.Json
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * JSON file under the user config directory. Offline, no account, no network.
 *
 * Default location: `$XDG_CONFIG_HOME/JapaneseLanguageGame/progress.json`,
 * or `~/.config/JapaneseLanguageGame/progress.json`.
 */
class ProgressStore(
    private val file: Path = defaultProgressFile(),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    fun load(): PersistedProgress {
        if (!file.exists()) return PersistedProgress()
        return try {
            sanitize(json.decodeFromString(file.readText()))
        } catch (_: Exception) {
            PersistedProgress()
        }
    }

    fun save(progress: PersistedProgress) {
        val directory = file.parent
        if (directory != null) Files.createDirectories(directory)
        val temporary = file.resolveSibling("${file.fileName}.tmp")
        Files.writeString(temporary, json.encodeToString(sanitize(progress)))
        try {
            Files.move(
                temporary,
                file,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun sanitize(progress: PersistedProgress): PersistedProgress {
        val jlpt = if (progress.jlpt in PLAYABLE) progress.jlpt else "N5"
        return progress.copy(
            jlpt = jlpt,
            score = progress.score.coerceAtLeast(0),
            streak = progress.streak.coerceAtLeast(0),
            best = progress.best.coerceAtLeast(0),
        )
    }

    companion object {
        private val PLAYABLE = setOf("N5", "N4", "N3")

        fun defaultProgressFile(): Path {
            val override = System.getenv("JAPANESE_LANGUAGE_GAME_HOME")
            val directory = if (!override.isNullOrBlank()) {
                Path(override)
            } else {
                val xdg = System.getenv("XDG_CONFIG_HOME")
                if (!xdg.isNullOrBlank()) {
                    Path(xdg).resolve("JapaneseLanguageGame")
                } else {
                    Path(System.getProperty("user.home"), ".config", "JapaneseLanguageGame")
                }
            }
            return directory.resolve("progress.json")
        }
    }
}
