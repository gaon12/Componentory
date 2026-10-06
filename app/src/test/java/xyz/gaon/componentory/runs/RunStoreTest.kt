package xyz.gaon.componentory.runs

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
