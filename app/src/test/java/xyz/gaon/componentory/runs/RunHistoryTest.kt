package xyz.gaon.componentory.runs

import java.io.File
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RunHistoryTest {
    @get:Rule val folder = TemporaryFolder()

    private fun record(id: String, time: Long) =
        RunRecord(
            id,
            time,
            "BUTTON",
            "MATERIAL2",
            "MATERIAL3",
            true,
            emptyMap(),
            emptyMap(),
            emptyMap(),
        )

    @Test
    fun failedSaveKeepsTheVerifiedSnapshotAndSuccessfulRetryClearsTheFailure() = runBlocking {
        val history = RunHistory(RunStore(folder.root))
        assertTrue(history.load())
        assertFalse(history.loading)
        assertTrue(history.save(record("original", 1)))
        val staging = File(folder.root, "${RunStore.FILE_NAME}.tmp")
        assertTrue(staging.mkdir())
        assertFalse(history.save(record("failed", 2)))
        assertEquals(RunOperation.SAVE, history.failure)
        assertEquals(listOf("original"), history.records.map { it.id })
        assertFalse(history.busy)
        assertTrue(staging.delete())
        assertTrue(history.save(record("retry", 3)))
        assertEquals(null, history.failure)
        assertEquals(listOf("retry", "original"), history.records.map { it.id })
    }

    @Test
    fun concurrentOperationsPublishTheirCommittedRecordsAndDeleteUpdatesTheSnapshot() =
        runBlocking {
            val history = RunHistory(RunStore(folder.root))
            history.load()
            (1..10)
                .map { value -> async { history.save(record("run-$value", value.toLong())) } }
                .awaitAll()
                .forEach { assertTrue(it) }
            assertEquals(10, history.records.size)
            assertTrue(history.delete("run-10"))
            assertEquals(9, history.records.size)
            assertFalse(history.records.any { it.id == "run-10" })
            val exported = requireNotNull(history.export())
            assertTrue(exported.indexOf("\"run-1\"") < exported.indexOf("\"run-9\""))
            assertFalse(history.busy)
        }

    @Test
    fun unreadableHistoryReportsLoadingFailureWithoutShowingSuccessfulEmptyData() = runBlocking {
        assertTrue(File(folder.root, RunStore.FILE_NAME).mkdir())
        val history = RunHistory(RunStore(folder.root))
        assertFalse(history.load())
        assertEquals(RunOperation.LOAD, history.failure)
        assertFalse(history.loading)
        assertFalse(history.busy)
    }
}
