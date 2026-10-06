package xyz.gaon.componentory.history

import android.content.Context
import java.io.Reader
import java.security.MessageDigest
import org.json.JSONObject

enum class HistoryFilter {
    ALL,
    ADDED,
    DEPRECATED,
    REMOVED,
}

data class HistoricalComponent(
    val api: Int,
    val name: String,
    val parent: String?,
    val category: String,
    val abstract: Boolean,
    val deprecated: Boolean,
)

class AndroidHistory
private constructor(
    private val entries: List<HistoricalComponent>,
    val sourceCommit: String,
    val identicalSnapshots: Map<Int, Int> = emptyMap(),
) {
    val versions = entries.map { it.api }.distinct().sorted()
    private val byApi = entries.groupBy { it.api }
    private val firstApi =
        entries.groupBy { it.name }.mapValues { (_, rows) -> rows.minOf { it.api } }

    fun introduced(name: String): Int = requireNotNull(firstApi[name])

    fun select(
        api: Int,
        query: String = "",
        filter: HistoryFilter = HistoryFilter.ALL,
    ): List<HistoricalComponent> {
        require(api in versions) { "This API has no audited snapshot." }
        val current = byApi.getValue(api)
        val previous = byApi[versions.getOrNull(versions.indexOf(api) - 1)].orEmpty()
        val previousByName = previous.associateBy { it.name }
        val currentNames = current.map { it.name }.toSet()
        val candidates =
            when (filter) {
                HistoryFilter.ALL -> current
                HistoryFilter.ADDED -> current.filter { it.name !in previousByName }
                HistoryFilter.DEPRECATED ->
                    current.filter { it.deprecated && previousByName[it.name]?.deprecated == false }
                HistoryFilter.REMOVED -> previous.filter { it.name !in currentNames }
            }
        return candidates
            .filter { it.name.contains(query.trim(), ignoreCase = true) }
            .sortedBy { it.name.substringAfterLast('.').lowercase(java.util.Locale.ROOT) }
    }

    companion object {
        fun read(context: Context): AndroidHistory {
            val bytes = context.assets.open("history/public-ui.csv").use { it.readBytes() }
            val provenance =
                JSONObject(
                    context.assets.open("history/provenance.json").bufferedReader().use {
                        it.readText()
                    }
                )
            val hash =
                MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") {
                    "%02x".format(it)
                }
            require(hash == provenance.getString("tableSha256")) {
                "Historical source table hash mismatch."
            }
            val snapshots = provenance.getJSONArray("versions")
            val hashes = mutableMapOf<String, Int>()
            val identical = mutableMapOf<Int, Int>()
            for (index in 0 until snapshots.length()) {
                val snapshot = snapshots.getJSONObject(index)
                val api = snapshot.getInt("api")
                val signatureHash = snapshot.getString("sha256")
                hashes[signatureHash]?.let { identical[api] = it }
                hashes.putIfAbsent(signatureHash, api)
            }
            return parse(
                bytes.toString(Charsets.UTF_8).reader(),
                provenance.getString("commit"),
                identical,
            )
        }

        fun parse(
            reader: Reader,
            sourceCommit: String,
            identicalSnapshots: Map<Int, Int> = emptyMap(),
        ): AndroidHistory {
            require(sourceCommit.matches(Regex("[0-9a-f]{40}"))) {
                "An immutable source commit is required."
            }
            val lines = reader.buffered().readLines().filter { it.isNotBlank() }
            require(lines.firstOrNull() == "api,class,superClass,category,abstract,deprecated") {
                "Unexpected historical table schema."
            }
            val entries =
                lines.drop(1).map { line ->
                    // Audited fields are class identifiers and flags, with no quoted CSV values.
                    val cells = line.split(',')
                    require(cells.size == 6) { "Invalid historical class row." }
                    val api = cells[0].toInt()
                    require(
                        api > 0 &&
                            cells[1].startsWith("android.") &&
                            cells[1].matches(Regex("[\\w.]+"))
                    )
                    require(
                        cells[3] in setOf("VIEW", "DIALOG", "POPUP", "PREFERENCE", "CONTROLLER")
                    )
                    HistoricalComponent(
                        api,
                        cells[1],
                        cells[2].ifBlank { null },
                        cells[3],
                        cells[4].toBooleanStrict(),
                        cells[5].toBooleanStrict(),
                    )
                }
            require(entries.isNotEmpty()) { "The historical table is empty." }
            require(entries.distinctBy { it.api to it.name }.size == entries.size) {
                "Duplicate historical class row."
            }
            return AndroidHistory(entries, sourceCommit, identicalSnapshots)
        }
    }
}

// Labels describe API revisions; they do not identify an original device run.
internal fun androidVersionLabel(api: Int): String {
    val releases =
        listOf(
            "1.0",
            "1.1",
            "1.5",
            "1.6",
            "2.0",
            "2.0.1",
            "2.1",
            "2.2",
            "2.3",
            "2.3.3",
            "3.0",
            "3.1",
            "3.2",
            "4.0",
            "4.0.3",
            "4.1",
            "4.2",
            "4.3",
            "4.4",
            "4.4W",
            "5.0",
            "5.1",
            "6.0",
            "7.0",
            "7.1",
            "8.0",
            "8.1",
            "9",
            "10",
            "11",
            "12",
            "12L",
            "13",
            "14",
            "15",
            "16",
        )
    return releases.getOrNull(api - 1)?.let { "Android $it · API $api" } ?: "API $api"
}
