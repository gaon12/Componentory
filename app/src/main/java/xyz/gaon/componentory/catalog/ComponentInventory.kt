package xyz.gaon.componentory.catalog

import android.content.Context
import java.io.Reader
import xyz.gaon.componentory.lab.LabComponent

enum class InventoryFamily(val sourcePrefix: String) {
    PLATFORM("android."),
    MATERIAL2("androidx.compose.material."),
    MATERIAL3("androidx.compose.material3."),
}

enum class InventoryStatus(val csvName: String) {
    IMPLEMENTED("Implemented"),
    RECREATED("Recreated"),
    PENDING("Pending"),
}

data class InventoryEntry(
    val family: InventoryFamily,
    val source: String,
    val apiIntroduced: Int?,
    val status: InventoryStatus,
    val catalogIds: List<String>,
    val notes: String,
)

object ComponentInventory {
    const val ASSET_NAME = "component-inventory.csv"
    private const val HEADER = "family,source,apiIntroduced,status,catalogId,notes"
    private val field = Regex("\"((?:[^\"]|\"\")*)\"(?:,|$)")

    fun read(context: Context): List<InventoryEntry> =
        context.assets.open(ASSET_NAME).bufferedReader().use { parse(it) }

    fun parse(reader: Reader): List<InventoryEntry> {
        val lines = reader.readText().lineSequence().toList().dropLastWhile { it.isEmpty() }
        require(lines.firstOrNull() == HEADER) { "Expected inventory header: $HEADER" }
        require(lines.size > 1) { "The inventory has no API sources" }
        val identities = mutableSetOf<Pair<InventoryFamily, String>>()
        return lines.drop(1).mapIndexed { index, line ->
            val lineNumber = index + 2
            val entry = parseEntry(line, lineNumber)
            require(identities.add(entry.family to entry.source)) {
                "Duplicate inventory source at line $lineNumber: ${entry.family} ${entry.source}"
            }
            entry
        }
    }

    fun pending(
        entries: List<InventoryEntry>,
        query: String = "",
        family: InventoryFamily? = null,
    ): List<InventoryEntry> {
        val term = query.trim()
        return entries.filter {
            it.status == InventoryStatus.PENDING &&
                (family == null || it.family == family) &&
                it.source.contains(term, ignoreCase = true)
        }
    }

    private fun parseEntry(line: String, lineNumber: Int): InventoryEntry {
        // Each audited row has six quoted fields on one line. Quotes inside a field are doubled.
        val matches = field.findAll(line).toList()
        require(
            line.endsWith('"') && matches.size == 6 && matches.joinToString("") { it.value } == line
        ) {
            "Expected six quoted inventory fields at line $lineNumber"
        }
        val values = matches.map { it.groupValues[1].replace("\"\"", "\"") }
        val family =
            requireNotNull(InventoryFamily.entries.firstOrNull { it.name == values[0] }) {
                "Unknown inventory family at line $lineNumber: ${values[0]}"
            }
        val source = values[1]
        require(
            source.startsWith(family.sourcePrefix) &&
                source.length > family.sourcePrefix.length &&
                source.none { it.isWhitespace() }
        ) {
            "Wrong inventory source for $family at line $lineNumber: $source"
        }
        val api = values[2].toIntOrNull()
        if (family == InventoryFamily.PLATFORM) {
            require(api != null && api > 0) {
                "Expected a positive platform API at line $lineNumber"
            }
        } else {
            require(values[2].isEmpty()) {
                "Library entries have no platform API at line $lineNumber"
            }
        }
        val status =
            requireNotNull(InventoryStatus.entries.firstOrNull { it.csvName == values[3] }) {
                "Unknown inventory status at line $lineNumber: ${values[3]}"
            }
        val catalogIds = if (values[4].isEmpty()) emptyList() else values[4].split(';')
        require(
            catalogIds.size == catalogIds.toSet().size &&
                catalogIds.all { id -> LabComponent.entries.any { it.name == id } }
        ) {
            "Invalid inventory catalog IDs at line $lineNumber: ${values[4]}"
        }
        require(
            if (status == InventoryStatus.PENDING) catalogIds.isEmpty() else catalogIds.isNotEmpty()
        ) {
            "Inventory status and catalog IDs disagree at line $lineNumber"
        }
        return InventoryEntry(family, source, api, status, catalogIds, values[5])
    }
}
