package xyz.gaon.componentory.catalog

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentInventoryResourceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun packagedInventoryRetainsAuditedSourcesAndStatuses() {
        val bytes = context.assets.open("component-inventory.csv").use { it.readBytes() }
        val entries = bytes.inputStream().reader(Charsets.UTF_8).use(ComponentInventory::parse)
        assertEquals(239, entries.size)
        assertEquals(entries.size, entries.map { it.family to it.source }.toSet().size)
        assertEquals(
            mapOf(
                InventoryFamily.PLATFORM to 74,
                InventoryFamily.MATERIAL2 to 52,
                InventoryFamily.MATERIAL3 to 113,
            ),
            entries.groupingBy { it.family }.eachCount(),
        )
        assertEquals(
            mapOf(InventoryStatus.IMPLEMENTED to 90, InventoryStatus.PENDING to 149),
            entries.groupingBy { it.status }.eachCount(),
        )
        val group = entries.single { it.source == "android.widget.RadioGroup" }
        assertEquals(listOf("RADIO"), group.catalogIds)
        assertEquals("RadioButton sample uses a real RadioGroup.", group.notes)
        val segmented = entries.single { it.source == "androidx.compose.material3.SegmentedButton" }
        assertEquals(listOf("SINGLE_SEGMENTED", "MULTI_SEGMENTED"), segmented.catalogIds)
        assertEquals("Used in both segmented-row samples.", segmented.notes)

        // The host can compare these exact asset bytes with the audited source file.
        val hash =
            MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") {
                (it.toInt() and 0xff).toString(16).padStart(2, '0')
            }
        InstrumentationRegistry.getInstrumentation()
            .sendStatus(
                2,
                Bundle().apply {
                    putString("stream", "ComponentInventoryResourceTest: sha256=$hash\n")
                },
            )
    }

    @Test
    fun pendingQueriesDistinguishProvidersAndKeepSourcesNonRunnable() {
        val entries = ComponentInventory.read(context)
        val pending = ComponentInventory.pending(entries)
        assertEquals(149, pending.size)
        assertEquals(
            mapOf(
                InventoryFamily.PLATFORM to 56,
                InventoryFamily.MATERIAL2 to 26,
                InventoryFamily.MATERIAL3 to 67,
            ),
            pending.groupingBy { it.family }.eachCount(),
        )
        pending.forEach {
            assertEquals(InventoryStatus.PENDING, it.status)
            assertTrue("Pending sources have no runnable sample IDs", it.catalogIds.isEmpty())
        }
        assertEquals(
            setOf("android.app.DatePickerDialog", "android.widget.DatePicker"),
            ComponentInventory.pending(entries, "  DATEPICKER  ", InventoryFamily.PLATFORM)
                .map { it.source }
                .toSet(),
        )
        val libraryDates =
            ComponentInventory.pending(entries, "datepicker", InventoryFamily.MATERIAL3)
        assertEquals(
            setOf(
                "androidx.compose.material3.DatePicker",
                "androidx.compose.material3.DatePickerDialog",
            ),
            libraryDates.map { it.source }.toSet(),
        )
        libraryDates.forEach { assertNull(it.apiIntroduced) }
        assertTrue(
            ComponentInventory.pending(entries, "datepicker", InventoryFamily.MATERIAL2).isEmpty()
        )
        assertTrue(ComponentInventory.pending(entries, "no-matching-source-api").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "checkbox").isEmpty())
    }
}
