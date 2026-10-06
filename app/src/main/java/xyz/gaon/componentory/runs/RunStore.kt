package xyz.gaon.componentory.runs

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Keep JSONL evidence readable even when an interrupted or future-version line is unknown.
class RunStore(private val directory: File) {
    private val file: File
        get() = File(directory, FILE_NAME)

    fun append(record: RunRecord): List<RunRecord> =
        synchronized(lock) {
            val existing = readText()
            val separator = if (existing.isNotEmpty() && !existing.endsWith('\n')) "\n" else ""
            val updated = existing + separator + record.toJson() + "\n"
            replace(updated)
            records(updated)
        }

    fun list(): List<RunRecord> = synchronized(lock) { records(readText()) }

    fun delete(id: String): List<RunRecord> =
        synchronized(lock) {
            val existing = readText()
            // Retain bytes of unrelated and unrecognized lines, including their line endings.
            val updated =
                existing
                    .splitToSequence('\n')
                    .filter { line -> RunRecord.fromJson(line)?.id != id }
                    .joinToString("\n")
            if (updated != existing) replace(updated)
            records(updated)
        }

    fun exportText(): String =
        list()
            .sortedBy { it.createdAtEpochMillis }
            .joinToString(",\n", prefix = "[\n", postfix = "\n]\n") { it.toJson() }

    private fun readText(): String = if (file.exists()) file.readText(Charsets.UTF_8) else ""

    private fun records(text: String): List<RunRecord> =
        text
            .lineSequence()
            .mapNotNull(RunRecord::fromJson)
            .sortedByDescending { it.createdAtEpochMillis }
            .toList()

    private fun replace(text: String) {
        if (!directory.isDirectory && !directory.mkdirs())
            throw IOException("Run directory could not be created.")
        val staging = File(directory, "$FILE_NAME.tmp")
        try {
            FileOutputStream(staging).use { stream ->
                stream.write(text.toByteArray(Charsets.UTF_8))
                stream.fd.sync()
            }
            // The original stays intact until a complete same-directory replacement succeeds.
            if (!staging.renameTo(file)) throw IOException("Run history replacement failed.")
        } finally {
            if (staging.isFile) staging.delete()
        }
    }

    companion object {
        const val FILE_NAME = "runs.jsonl"
        private val lock = Any()
    }
}
