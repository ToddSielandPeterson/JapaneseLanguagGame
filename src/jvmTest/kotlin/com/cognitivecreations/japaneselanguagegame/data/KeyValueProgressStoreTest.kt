package com.cognitivecreations.japaneselanguagegame.data

import com.cognitivecreations.japaneselanguagegame.domain.PersistedProgress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeyValueProgressStoreTest {
    @Test
    fun roundTripUsesTheBrowserKey() {
        val memory = MemoryStorage()
        val store = KeyValueProgressStore(memory)
        val saved = PersistedProgress(
            jlpt = "N3",
            furigana = false,
            distractors = true,
            score = 4,
            streak = 2,
            best = 9,
        )
        store.save(saved)
        assertEquals(saved, store.load())
        assertEquals(KeyValueProgressStore.STORAGE_KEY, "JapaneseLanguageGame.progress")
        val raw = memory.getItem(KeyValueProgressStore.STORAGE_KEY)
        assertTrue(raw!!.contains("\"jlpt\": \"N3\""))
        assertTrue(raw.contains("\"distractors\": true"))
    }

    @Test
    fun missingAndCorruptPayloadsFallBack() {
        val empty = KeyValueProgressStore(MemoryStorage())
        assertEquals(PersistedProgress(), empty.load())

        val blank = MemoryStorage()
        blank.setItem(KeyValueProgressStore.STORAGE_KEY, "   ")
        assertEquals(PersistedProgress(), KeyValueProgressStore(blank).load())

        val corrupt = MemoryStorage()
        corrupt.setItem(KeyValueProgressStore.STORAGE_KEY, "{not json")
        assertEquals(PersistedProgress(), KeyValueProgressStore(corrupt).load())
    }

    @Test
    fun unknownLevelAndNegativeScoresAreSanitized() {
        val memory = MemoryStorage()
        memory.setItem(
            KeyValueProgressStore.STORAGE_KEY,
            """{"jlpt":"N2","furigana":true,"distractors":false,"score":-3,"streak":-1,"best":4}""",
        )
        val loaded = KeyValueProgressStore(memory).load()
        assertEquals("N5", loaded.jlpt)
        assertEquals(0, loaded.score)
        assertEquals(0, loaded.streak)
        assertEquals(4, loaded.best)
        assertFalse(loaded.distractors)
        assertTrue(loaded.furigana)
    }

    @Test
    fun saveOverwritesAndMatchesTheFileCodec() {
        val memory = MemoryStorage()
        val store = KeyValueProgressStore(memory)
        store.save(PersistedProgress(jlpt = "N5", score = 1))
        store.save(PersistedProgress(jlpt = "N4", score = 8, best = 3))
        val raw = memory.getItem(KeyValueProgressStore.STORAGE_KEY)!!
        assertEquals(ProgressCodec.decode(raw), store.load())
        assertEquals(
            ProgressCodec.encode(PersistedProgress(jlpt = "N4", score = 8, best = 3)),
            raw,
        )
        assertNull(memory.getItem("other"))
    }

    private class MemoryStorage : KeyValueStorage {
        private val values = mutableMapOf<String, String>()
        override fun getItem(key: String): String? = values[key]
        override fun setItem(key: String, value: String) {
            values[key] = value
        }
    }
}
