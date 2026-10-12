package com.cognitivecreations.japaneselanguagegame.data

import com.cognitivecreations.japaneselanguagegame.domain.PersistedProgress
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProgressStoreTest {
    @Test
    fun roundTrip() {
        val file = Files.createTempFile("progress", ".json")
        val store = FileProgressStore(file)
        val saved = PersistedProgress(
            jlpt = "N4",
            furigana = false,
            distractors = true,
            score = 4,
            streak = 2,
            best = 9,
        )
        store.save(saved)
        assertEquals(saved, store.load())
        assertTrue(Files.readString(file).contains("\"jlpt\": \"N4\""))
    }

    @Test
    fun missingAndCorruptFilesFallBack() {
        val missing = Files.createTempDirectory("progress-dir").resolve("absent.json")
        assertEquals(PersistedProgress(), FileProgressStore(missing).load())

        val corrupt = Files.createTempFile("progress", ".json")
        Files.writeString(corrupt, "{not json")
        assertEquals(PersistedProgress(), FileProgressStore(corrupt).load())
    }

    @Test
    fun unknownLevelAndNegativeScoresAreSanitized() {
        val file = Files.createTempFile("progress", ".json")
        Files.writeString(
            file,
            """{"jlpt":"N2","furigana":true,"distractors":false,"score":-3,"streak":-1,"best":4}""",
        )
        val loaded = FileProgressStore(file).load()
        assertEquals("N5", loaded.jlpt)
        assertEquals(0, loaded.score)
        assertEquals(0, loaded.streak)
        assertEquals(4, loaded.best)
        assertFalse(loaded.distractors)
    }
}
