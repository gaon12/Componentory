package xyz.gaon.componentory.catalog

import java.io.File
import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ComponentInventoryParserTest {
    @Test
    fun readsTheAuditedFileWithAllProvidersAndSupportingCatalogIds() {
        val entries =
            File("../docs/component-inventory.csv").reader().use { ComponentInventory.parse(it) }
        assertEquals(248, entries.size)
        assertEquals(
            mapOf(
                InventoryFamily.PLATFORM to 74,
                InventoryFamily.MATERIAL2 to 52,
                InventoryFamily.MATERIAL3 to 122,
            ),
            entries.groupingBy { it.family }.eachCount(),
        )
        val group = entries.single { it.source == "android.widget.RadioGroup" }
        assertEquals(InventoryStatus.IMPLEMENTED, group.status)
        assertEquals(listOf("RADIO"), group.catalogIds)
        assertEquals("RadioButton sample uses a real RadioGroup.", group.notes)
        val segmented = entries.single { it.source == "androidx.compose.material3.SegmentedButton" }
        assertEquals(listOf("SINGLE_SEGMENTED", "MULTI_SEGMENTED"), segmented.catalogIds)
        assertNull(segmented.apiIntroduced)
    }

    @Test
    fun preservesEscapedQuotesCommasAndTheDeclaredStatus() {
        val entries =
            parse(
                row(
                    source = "android.widget.RadioGroup",
                    status = "Implemented",
                    ids = "RADIO",
                    notes = "Uses a \"real\", grouped control.",
                ),
                row(
                    family = "MATERIAL3",
                    source = "androidx.compose.material3.SegmentedButton",
                    api = "",
                    status = "Implemented",
                    ids = "SINGLE_SEGMENTED;MULTI_SEGMENTED",
                ),
                row(notes = "Awaiting a sample."),
            )
        assertEquals("Uses a \"real\", grouped control.", entries[0].notes)
        assertEquals(1, entries[0].apiIntroduced)
        assertEquals(listOf("SINGLE_SEGMENTED", "MULTI_SEGMENTED"), entries[1].catalogIds)
        assertEquals(InventoryStatus.PENDING, entries[2].status)
        assertEquals(emptyList<String>(), entries[2].catalogIds)
    }

    @Test
    fun pendingSearchCombinesSourceNameWithAnOptionalProvider() {
        val entries =
            parse(
                row(notes = "Select a date."),
                row("MATERIAL2", "androidx.compose.material.Card", "", notes = "Group content."),
                row("MATERIAL3", "androidx.compose.material3.DatePicker", ""),
                row("MATERIAL3", "androidx.compose.material3.Button", "", "Implemented", "BUTTON"),
            )
        assertEquals(3, ComponentInventory.pending(entries, "  ").size)
        assertEquals(
            listOf(entries[0], entries[2]),
            ComponentInventory.pending(entries, " DATEpicker "),
        )
        assertEquals(
            listOf(entries[0]),
            ComponentInventory.pending(entries, "date", InventoryFamily.PLATFORM),
        )
        assertEquals(
            emptyList<InventoryEntry>(),
            ComponentInventory.pending(entries, "GROUP CONTENT"),
        )
        assertEquals(
            listOf(entries[2]),
            ComponentInventory.pending(entries, family = InventoryFamily.MATERIAL3),
        )
        assertEquals(emptyList<InventoryEntry>(), ComponentInventory.pending(entries, "Button"))
    }

    @Test
    fun rejectsWrongHeadersAndMalformedSingleLineCsv() {
        val valid = row()
        listOf("", HEADER, "$HEADER\n\n", "wrong header\n$valid").forEach { csv ->
            assertThrows(IllegalArgumentException::class.java) {
                ComponentInventory.parse(StringReader(csv))
            }
        }
        listOf(
                valid.removePrefix("\""),
                valid.dropLast(1),
                "$valid,",
                "$valid,\"extra\"",
                "\"PLATFORM\",\"android.widget.DatePicker\",\"1\",\"Pending\",\"\"",
                "$valid\n\n$valid",
                row(notes = "A multiline\nnote"),
            )
            .forEach { malformed -> rejects(malformed) }
    }

    @Test
    fun rejectsUnknownFamiliesStatusesAndWrongApiOrSourceMetadata() {
        listOf(
                row(family = "UNKNOWN"),
                row(status = "Unsupported"),
                row(source = "androidx.compose.material.Button"),
                row(source = "android."),
                row(source = "android.widget.Date Picker"),
                row("MATERIAL2", "android.widget.Button", ""),
                row("MATERIAL3", "androidx.compose.material3.DatePicker", "21"),
            )
            .forEach { invalid -> rejects(invalid) }
        listOf("", "0", "-1", "unknown", "2147483648").forEach { api -> rejects(row(api = api)) }
    }

    @Test
    fun rejectsDuplicateSourcesAndInvalidCatalogReferences() {
        rejects("${row()}\n${row()}")
        listOf(
                row(ids = "DIALOG"),
                row(status = "Implemented"),
                row(status = "Implemented", ids = "UNKNOWN_SAMPLE"),
                row(status = "Implemented", ids = "RADIO;RADIO"),
                row(status = "Implemented", ids = "RADIO;"),
                row(status = "Implemented", ids = " RADIO "),
            )
            .forEach { invalid -> rejects(invalid) }
    }

    @Test
    fun futureApiNumbersStayPendingAndCompleteInventoriesNeedNoPendingRows() {
        val future = parse(row(source = "android.widget.FutureControl", api = "99"))
        assertEquals(99, future.single().apiIntroduced)
        assertEquals(future, ComponentInventory.pending(future))
        val complete =
            parse(
                row("MATERIAL3", "androidx.compose.material3.Button", "", "Implemented", "BUTTON")
            )
        assertEquals(emptyList<InventoryEntry>(), ComponentInventory.pending(complete))
    }

    private fun parse(vararg rows: String): List<InventoryEntry> =
        ComponentInventory.parse(StringReader("$HEADER\n${rows.joinToString("\n")}\n"))

    private fun rejects(row: String) {
        assertThrows(IllegalArgumentException::class.java) { parse(row) }
    }

    private fun row(
        family: String = "PLATFORM",
        source: String = "android.widget.DatePicker",
        api: String = "1",
        status: String = "Pending",
        ids: String = "",
        notes: String = "",
    ): String =
        listOf(family, source, api, status, ids, notes).joinToString(",") {
            "\"${it.replace("\"", "\"\"")}\""
        }

    companion object {
        private const val HEADER = "family,source,apiIntroduced,status,catalogId,notes"
    }
}
