package com.cognitivecreations.japaneselanguagegame.data

import com.cognitivecreations.japaneselanguagegame.domain.PersistedProgress
import kotlinx.serialization.json.Json

/**
 * Score and settings that survive a restart. Desktop writes a JSON file.
 * The browser writes the same JSON to localStorage.
 */
interface ProgressStore {
    fun load(): PersistedProgress
    fun save(progress: PersistedProgress)
}

/** Platform store: a file on the JVM, localStorage on the web. */
expect fun createProgressStore(): ProgressStore

/**
 * String map used by the browser store. Tests use an in-memory implementation
 * so the localStorage path does not need a browser.
 */
interface KeyValueStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
}

/**
 * Shared encode, decode, and sanitize step for every [ProgressStore].
 * A corrupt payload falls back to defaults so a bad write cannot trap the next launch.
 */
object ProgressCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    fun encode(progress: PersistedProgress): String = json.encodeToString(sanitize(progress))

    fun decode(text: String): PersistedProgress = try {
        sanitize(json.decodeFromString(text))
    } catch (_: Exception) {
        PersistedProgress()
    }

    fun sanitize(progress: PersistedProgress): PersistedProgress {
        val jlpt = if (progress.jlpt in PLAYABLE) progress.jlpt else "N5"
        return progress.copy(
            jlpt = jlpt,
            score = progress.score.coerceAtLeast(0),
            streak = progress.streak.coerceAtLeast(0),
            best = progress.best.coerceAtLeast(0),
        )
    }

    private val PLAYABLE = setOf("N5", "N4", "N3")
}

/**
 * [ProgressStore] backed by [KeyValueStorage]. The web target passes browser localStorage.
 */
class KeyValueProgressStore(
    private val storage: KeyValueStorage,
    private val key: String = STORAGE_KEY,
) : ProgressStore {
    override fun load(): PersistedProgress {
        val raw = storage.getItem(key)
        if (raw.isNullOrBlank()) return PersistedProgress()
        return ProgressCodec.decode(raw)
    }

    override fun save(progress: PersistedProgress) {
        storage.setItem(key, ProgressCodec.encode(progress))
    }

    companion object {
        const val STORAGE_KEY: String = "JapaneseLanguageGame.progress"
    }
}
