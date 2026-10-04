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
                    val supportingSource =
                        when {
                            row.provider == "PLATFORM" && component == LabComponent.RADIO ->
                                "android.widget.RadioGroup"
                            row.provider == "MATERIAL3" &&
                                component in
                                    listOf(
                                        LabComponent.SINGLE_SEGMENTED,
                                        LabComponent.MULTI_SEGMENTED,
                                    ) -> "androidx.compose.material3.SegmentedButton"
                            else -> null
                        }
                    assertTrue(
                        "Wrong source for $id: ${row.source}",
                        row.source == family.source(component) || row.source == supportingSource,
                    )
                    if (row.source == supportingSource)
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
