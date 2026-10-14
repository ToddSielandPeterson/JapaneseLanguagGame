package com.cognitivecreations.japaneselanguagegame.data

import com.cognitivecreations.japaneselanguagegame.domain.PersistedProgress
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
class FileProgressStore(
    private val file: Path = defaultProgressFile(),
) : ProgressStore {
    override fun load(): PersistedProgress {
        if (!file.exists()) return PersistedProgress()
        return ProgressCodec.decode(file.readText())
    }

    override fun save(progress: PersistedProgress) {
        val directory = file.parent
        if (directory != null) Files.createDirectories(directory)
        val temporary = file.resolveSibling("${file.fileName}.tmp")
        Files.writeString(temporary, ProgressCodec.encode(progress))
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

    companion object {
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

actual fun createProgressStore(): ProgressStore = FileProgressStore()
