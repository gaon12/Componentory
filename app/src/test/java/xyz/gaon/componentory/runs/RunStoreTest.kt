package xyz.gaon.componentory.runs

import java.io.File
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RunStoreTest {
    @get:Rule val folder = TemporaryFolder()

    private fun record(
        id: String,
        created: Long,
        inputs: Map<String, String> = mapOf("value" to "3"),
        enabled: Boolean = true,
    ) =
        RunRecord(
            id = id,
            createdAtEpochMillis = created,
            component = "BUTTON",
            leftFamily = "MATERIAL2",
            rightFamily = "MATERIAL3",
            enabled = enabled,
            leftInputs = inputs,
            rightInputs = mapOf("text" to "done \"ok\""),
            environment = mapOf("sdk" to "36", "model" to "SM-X800"),
        )

    @Test
    fun appendAndListRoundTripsNewestFirst() {
        val store = RunStore(folder.root)
        assertTrue(store.list().isEmpty())
        store.append(record("a", 100))
        store.append(record("b", 200))
        assertEquals(listOf("b", "a"), store.list().map { it.id })
        val restored = store.list().first()
        assertEquals("BUTTON", restored.component)
        assertEquals("3", restored.leftInputs["value"])
        assertEquals("done \"ok\"", restored.rightInputs["text"])
        assertEquals("SM-X800", restored.environment["model"])
    }

    @Test
    fun escapingSurvivesNewlinesQuotesAndSlashes() {
        val store = RunStore(folder.root)
        store.append(record("c", 300, mapOf("text" to "line\nwith \"quotes\" and \\ slash")))
        assertEquals("line\nwith \"quotes\" and \\ slash", store.list().single().leftInputs["text"])
    }

    @Test
    fun malformedLinesAreSkippedNotFatal() {
        val store = RunStore(folder.root)
        store.append(record("good", 10))
        File(folder.root, RunStore.FILE_NAME).appendText("not json\n{\"id\":\"x\"}\n")
        assertEquals(listOf("good"), store.list().map { it.id })
    }

    @Test
    fun deleteKeepsAppendOrderOfTheRest() {
        val store = RunStore(folder.root)
        store.append(record("a", 1))
        store.append(record("b", 2))
        store.append(record("c", 3))
        store.delete("b")
        assertEquals(listOf("c", "a"), store.list().map { it.id })
        val raw = File(folder.root, RunStore.FILE_NAME).readLines()
        assertEquals(2, raw.size)
    }

    @Test
    fun exportTextIsAJsonArrayInChronologicalOrder() {
        val store = RunStore(folder.root)
        store.append(record("a", 10))
        store.append(record("b", 5))
        val exported = store.exportText()
        assertTrue(exported.startsWith("["))
        assertTrue(exported.indexOf("\"b\"") < exported.indexOf("\"a\""))
    }

    @Test
    fun missingRequiredFieldsRejectTheRecord() {
        assertNull(RunRecord.fromJson("{\"id\":\"x\",\"createdAt\":\"1\"}"))
        assertNull(RunRecord.fromJson(""))
        assertNull(RunRecord.fromJson("[]"))
    }

    @Test
    fun interruptedLineDoesNotConsumeTheNextSavedRecord() {
        val file = File(folder.root, RunStore.FILE_NAME)
        file.writeText("partial unrecognized line")
        val store = RunStore(folder.root)
        assertEquals(listOf("saved"), store.append(record("saved", 1)).map { it.id })
        assertTrue(file.readText().startsWith("partial unrecognized line\n"))
        assertEquals(listOf("saved"), store.list().map { it.id })
    }

    @Test
    fun stagingFailurePreservesExistingEvidenceAndAllowsRetry() {
        val store = RunStore(folder.root)
        store.append(record("original", 1))
        val file = File(folder.root, RunStore.FILE_NAME)
        val original = file.readBytes()
        val staging = File(folder.root, "${RunStore.FILE_NAME}.tmp")
        assertTrue(staging.mkdir())
        assertThrows(IOException::class.java) { store.append(record("new", 2)) }
        assertThrows(IOException::class.java) { store.delete("original") }
        assertTrue(original.contentEquals(file.readBytes()))
        assertTrue(staging.delete())
        assertEquals(listOf("new", "original"), store.append(record("new", 2)).map { it.id })
    }

    @Test
    fun deletionPreservesUnknownLinesAndDeletingAnAbsentIdDoesNotWrite() {
        val store = RunStore(folder.root)
        store.append(record("original", 1))
        val file = File(folder.root, RunStore.FILE_NAME)
        file.appendText("future record\r\npartial")
        assertEquals(emptyList<RunRecord>(), store.delete("original"))
        assertEquals("future record\r\npartial", file.readText())
        val staging = File(folder.root, "${RunStore.FILE_NAME}.tmp")
        assertTrue(staging.mkdir())
        store.delete("absent")
        assertEquals("future record\r\npartial", file.readText())
    }

    @Test
    fun independentStoreInstancesDoNotLoseConcurrentRecords() {
        val first = RunStore(folder.root)
        val second = RunStore(folder.root)
        val executor = Executors.newFixedThreadPool(4)
        try {
            executor
                .invokeAll(
                    (1..40).map { value ->
                        Callable {
                            (if (value % 2 == 0) first else second).append(
                                record("run-$value", value.toLong())
                            )
                        }
                    }
                )
                .forEach { it.get() }
        } finally {
            executor.shutdown()
        }
        assertEquals((1..40).map { "run-$it" }.toSet(), first.list().map { it.id }.toSet())
    }
}
