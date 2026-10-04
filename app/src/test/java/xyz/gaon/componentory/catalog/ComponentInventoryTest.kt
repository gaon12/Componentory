package xyz.gaon.componentory.catalog

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

class ComponentInventoryTest {
    @Test
    fun textCoverageAddsCanonicalRowsWithoutCountingThemesAsSeparateSources() {
        val rows = inventory()
        assertEquals(239, rows.size)
        assertEquals(69, LabComponent.entries.size)
        assertEquals(
            169,
            LabComponent.entries.sumOf { component ->
                DesignFamily.entries.count { family ->
                    family.unsupportedReason(component, 24) == null
                }
            },
        )
        assertEquals(
            mapOf("Implemented" to 116, "Pending" to 123),
            rows.groupingBy { it.status }.eachCount(),
        )
        assertEquals(
            mapOf("PLATFORM" to 26, "MATERIAL2" to 31, "MATERIAL3" to 59),
            rows.filter { it.status == "Implemented" }.groupingBy { it.provider }.eachCount(),
        )
        assertEquals(
            mapOf("PLATFORM" to 48, "MATERIAL2" to 21, "MATERIAL3" to 54),
            rows.filter { it.status == "Pending" }.groupingBy { it.provider }.eachCount(),
        )
        listOf(
                "android.widget.TextView" to "TEXT",
                "android.widget.CheckedTextView" to "CHECKED_TEXT_VIEW",
                "androidx.compose.material.Text" to "TEXT",
                "androidx.compose.material3.Text" to "TEXT",
            )
            .forEach { (source, sampleId) ->
                val row = rows.single { it.source == source }
                assertEquals("Implemented", row.status)
                assertEquals(listOf(sampleId), row.catalogIds)
                assertEquals(if (row.provider == "PLATFORM") "1" else "", row.apiIntroduced)
                assertTrue(row.notes.isNotBlank())
            }
    }

    @Test
    fun auditedBaselineKeepsProviderCountsAndUniqueSources() {
        val rows = inventory()
        assertEquals(
            mapOf("PLATFORM" to 74, "MATERIAL2" to 52, "MATERIAL3" to 113),
            rows.groupingBy { it.provider }.eachCount(),
        )
        assertEquals(rows.size, rows.map { it.provider to it.source }.toSet().size)
        rows.forEach { row ->
            assertTrue(
                "Unknown status for ${row.source}",
                row.status in setOf("Implemented", "Pending"),
            )
            val prefix =
                when (row.provider) {
                    "PLATFORM" -> "android."
                    "MATERIAL2" -> "androidx.compose.material."
                    else -> "androidx.compose.material3."
                }
            assertTrue("Wrong provider for ${row.source}", row.source.startsWith(prefix))
            if (row.provider == "PLATFORM") {
                assertNotNull(
                    "Missing API introduction for ${row.source}",
                    row.apiIntroduced.toIntOrNull(),
                )
                assertTrue(
                    "Invalid API introduction for ${row.source}",
                    row.apiIntroduced.toInt() > 0,
                )
            } else {
                assertTrue(
                    "A library version is not a platform API level",
                    row.apiIntroduced.isEmpty(),
                )
            }
        }
    }

    @Test
    fun implementedRowsNameAvailableSamplesAndTheirActualSources() {
        inventory()
            .filter { it.status == "Implemented" }
            .forEach { row ->
                assertTrue("No sample for ${row.source}", row.catalogIds.isNotEmpty())
                row.catalogIds.forEach { id ->
                    val component = LabComponent.valueOf(id)
                    val family = family(row.provider)
                    val supportedApi = component.minimumApi.coerceAtLeast(24)
                    assertNull(
                        "Unavailable $id for ${row.provider} at its declared API",
                        family.unsupportedReason(component, supportedApi),
                    )
                    if (row.provider == "PLATFORM") {
                        assertTrue(
                            "Missing API guard for ${row.source}",
                            component.minimumApi >= row.apiIntroduced.toInt(),
                        )
                    }
                    // Some samples use a container as well as their primary control.
                    val supportingSources =
                        when {
                            row.provider == "PLATFORM" && component == LabComponent.RADIO ->
                                setOf("android.widget.RadioGroup")
                            row.provider == "PLATFORM" &&
                                component == LabComponent.TIME_PICKER_DIALOG ->
                                setOf("android.widget.TimePicker")
                            row.provider == "MATERIAL2" && component == LabComponent.POPUP_MENU ->
                                setOf("androidx.compose.material.DropdownMenuItem")
                            row.provider == "MATERIAL3" && component == LabComponent.POPUP_MENU ->
                                setOf("androidx.compose.material3.DropdownMenuItem")
                            row.provider == "MATERIAL3" &&
                                component == LabComponent.DATE_PICKER_DIALOG ->
                                setOf("androidx.compose.material3.DatePicker")
                            row.provider == "MATERIAL3" &&
                                component == LabComponent.TIME_PICKER_DIALOG ->
                                setOf(
                                    "androidx.compose.material3.TimePicker",
                                    "androidx.compose.material3.TimeInput",
                                )
                            row.provider == "MATERIAL3" &&
                                component in
                                    listOf(
                                        LabComponent.SINGLE_SEGMENTED,
                                        LabComponent.MULTI_SEGMENTED,
                                    ) -> setOf("androidx.compose.material3.SegmentedButton")
                            else -> emptySet()
                        }
                    assertTrue(
                        "Wrong source for $id: ${row.source}",
                        row.source == family.source(component) || row.source in supportingSources,
                    )
                    if (row.source in supportingSources)
                        assertTrue("Explain the supporting source", row.notes.isNotBlank())
                }
            }
    }

    @Test
    fun everyDeclaredSupportedCombinationHasAnImplementedInventoryEntry() {
        val implemented = inventory().filter { it.status == "Implemented" }
        LabComponent.entries.forEach { component ->
            DesignFamily.entries.forEach { family ->
                if (
                    family.unsupportedReason(component, component.minimumApi.coerceAtLeast(24)) ==
                        null
                ) {
                    val provider = if (family.platform != null) "PLATFORM" else family.name
                    assertTrue(
                        "Missing $provider inventory entry for ${component.name}",
                        implemented.any {
                            it.provider == provider &&
                                it.source == family.source(component) &&
                                component.name in it.catalogIds
                        },
                    )
                }
            }
        }
    }

    @Test
    fun pendingApisNeverClaimRunnableSamples() {
        val pending = inventory().filter { it.status == "Pending" }
        pending.forEach { row ->
            assertTrue(
                "Pending ${row.source} claims a runnable catalog ID",
                row.catalogIds.isEmpty(),
            )
        }
    }

    private fun family(provider: String) =
        if (provider == "PLATFORM") DesignFamily.CLASSIC else DesignFamily.valueOf(provider)

    private fun inventory(): List<InventoryRow> {
        val lines = File("../docs/component-inventory.csv").readLines()
        assertEquals("family,source,apiIntroduced,status,catalogId,notes", lines.first())
        // The audited file uses six quoted, single-line fields; escaped quotes are allowed.
        val field = Regex("\"((?:[^\"]|\"\")*)\"(?:,|$)")
        return lines.drop(1).mapIndexed { index, line ->
            val matches = field.findAll(line).toList()
            assertEquals(
                "Malformed inventory line ${index + 2}",
                line,
                matches.joinToString("") { it.value },
            )
            assertEquals("Wrong column count at line ${index + 2}", 6, matches.size)
            val values = matches.map { it.groupValues[1].replace("\"\"", "\"") }
            InventoryRow(
                values[0],
                values[1],
                values[2],
                values[3],
                values[4].split(';').filter { it.isNotEmpty() },
                values[5],
            )
        }
    }

    private data class InventoryRow(
        val provider: String,
        val source: String,
        val apiIntroduced: String,
        val status: String,
        val catalogIds: List<String>,
        val notes: String,
    )
}
