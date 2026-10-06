package xyz.gaon.componentory.runs

import java.io.File

// Append-only JSONL storage; one line per saved run keeps every record real
// and replayable without a database dependency.
class RunStore(private val directory: File) {
    private val file: File
        get() = File(directory, FILE_NAME)

    fun append(record: RunRecord) {
        directory.mkdirs()
        file.appendText(record.toJson() + "\n", Charsets.UTF_8)
    }

    fun list(): List<RunRecord> {
        if (!file.exists()) return emptyList()
        return file.readLines(Charsets.UTF_8).mapNotNull(RunRecord::fromJson).sortedByDescending {
            it.createdAtEpochMillis
        }
    }

    fun delete(id: String) {
        if (!file.exists()) return
        val kept = file.readLines(Charsets.UTF_8).filter { RunRecord.fromJson(it)?.id != id }
        file.writeText(kept.joinToString("\n", postfix = "\n"), Charsets.UTF_8)
    }

    fun exportText(): String =
        list()
            .sortedBy { it.createdAtEpochMillis }
            .joinToString(",\n", prefix = "[\n", postfix = "\n]\n") { it.toJson() }

    companion object {
        const val FILE_NAME = "runs.jsonl"
    }
}
