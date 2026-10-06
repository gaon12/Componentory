package xyz.gaon.componentory.runs

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class RunOperation {
    LOAD,
    SAVE,
    DELETE,
    EXPORT,
}

@Stable
class RunHistory(private val store: RunStore) {
    var records by mutableStateOf(emptyList<RunRecord>())
        private set

    var loading by mutableStateOf(true)
        private set

    var busy by mutableStateOf(false)
        private set

    var failure by mutableStateOf<RunOperation?>(null)
        private set

    private val operations = Mutex()

    suspend fun load(): Boolean = update(RunOperation.LOAD, store::list)

    suspend fun save(record: RunRecord): Boolean =
        update(RunOperation.SAVE) { store.append(record) }

    suspend fun delete(id: String): Boolean = update(RunOperation.DELETE) { store.delete(id) }

    suspend fun export(): String? =
        perform(RunOperation.EXPORT) { withContext(Dispatchers.IO) { store.exportText() } }

    private suspend fun update(action: RunOperation, change: () -> List<RunRecord>): Boolean =
        perform(action) {
            records = withContext(Dispatchers.IO) { change() }
            true
        } ?: false

    private suspend fun <T> perform(action: RunOperation, work: suspend () -> T): T? =
        operations.withLock {
            busy = true
            failure = null
            try {
                work()
            } catch (_: IOException) {
                // Keep the last verified snapshot when storage cannot complete the action.
                failure = action
                null
            } finally {
                if (action == RunOperation.LOAD) loading = false
                busy = false
            }
        }
}
