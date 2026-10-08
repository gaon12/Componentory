package xyz.gaon.componentory.survivor

import android.util.AtomicFile
import java.io.File

/** Every writer reads the latest document while holding the same per-file lock. */
internal class GameStore(directory: File) {
    private val file = AtomicFile(File(directory, "save.json"))
    private val lock = locks.getOrPut(file.baseFile.absolutePath) { Any() }

    fun read(): GameSave = synchronized(lock) { readLocked() }

    private fun readLocked(): GameSave {
        if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists())
            return GameSave()
        return GameJson.save(file.readFully().toString(Charsets.UTF_8))
    }

    private fun update(change: (GameSave) -> GameSave): GameSave =
        synchronized(lock) {
            val before = readLocked()
            val after = change(before)
            if (after == before) return@synchronized before
            require(file.baseFile.parentFile?.let { it.isDirectory || it.mkdirs() } == true)
            val output = file.startWrite()
            try {
                output.write(GameJson.save(after).toByteArray(Charsets.UTF_8))
                file.finishWrite(output)
            } catch (error: Exception) {
                file.failWrite(output)
                throw error
            }
            after
        }

    fun start(json: String): GameSave {
        val s = GameJson.session(json)
        require(s.outcome == RunOutcome.ACTIVE)
        return update { before ->
            require(before.activeId == null && before.records.none { it.id == s.id })
            before.copy(activeId = s.id, activeJson = json)
        }
    }

    fun checkpoint(json: String): GameSave {
        val s = GameJson.session(json)
        return update { before ->
            val previous = before.activeJson?.let(GameJson::session)
            if (
                before.activeId != s.id ||
                    before.records.any { it.id == s.id } ||
                    previous == null ||
                    s.tick < previous.tick ||
                    s.level < previous.level
            )
                before
            else before.copy(activeJson = json)
        }
    }

    fun finish(json: String): GameSave {
        val s = GameJson.session(json)
        return update { GameLedger.finish(it, s) }
    }

    fun buy(upgrade: PermanentUpgrade): GameSave = update { before ->
        before.progress.buy(upgrade)?.let { before.copy(progress = it) } ?: before
    }

    fun unlock(support: SupportId): GameSave = update { before ->
        before.progress.unlock(support)?.let { before.copy(progress = it) } ?: before
    }

    fun updateSubmission(runId: String, change: (GameSubmission) -> GameSubmission): GameSave =
        update { before ->
            before.copy(
                submissions = before.submissions.map { if (it.runId == runId) change(it) else it }
            )
        }

    companion object {
        private val locks = java.util.concurrent.ConcurrentHashMap<String, Any>()
    }
}
