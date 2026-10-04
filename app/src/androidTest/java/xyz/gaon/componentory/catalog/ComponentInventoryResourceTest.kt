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
            mapOf(InventoryStatus.IMPLEMENTED to 98, InventoryStatus.PENDING to 141),
            entries.groupingBy { it.status }.eachCount(),
        )
        val group = entries.single { it.source == "android.widget.RadioGroup" }
        assertEquals(listOf("RADIO"), group.catalogIds)
        assertEquals("RadioButton sample uses a real RadioGroup.", group.notes)
        val segmented = entries.single { it.source == "androidx.compose.material3.SegmentedButton" }
        assertEquals(listOf("SINGLE_SEGMENTED", "MULTI_SEGMENTED"), segmented.catalogIds)
        assertEquals("Used in both segmented-row samples.", segmented.notes)
        listOf(
                "android.app.DatePickerDialog",
                "androidx.compose.material3.DatePickerDialog",
                "androidx.compose.material3.DatePicker",
            )
            .forEach { source ->
                val date = entries.single { it.source == source }
                assertEquals(InventoryStatus.IMPLEMENTED, date.status)
                assertEquals(listOf("DATE_PICKER_DIALOG"), date.catalogIds)
            }
        assertTrue(
            entries
                .single { it.source == "androidx.compose.material3.DatePicker" }
                .notes
                .isNotBlank()
        )
        listOf(
                "android.app.TimePickerDialog",
                "android.widget.TimePicker",
                "androidx.compose.material3.TimePickerDialog",
                "androidx.compose.material3.TimePicker",
                "androidx.compose.material3.TimeInput",
            )
            .forEach { source ->
                val time = entries.single { it.source == source }
                assertEquals(InventoryStatus.IMPLEMENTED, time.status)
                assertEquals(listOf("TIME_PICKER_DIALOG"), time.catalogIds)
            }
        listOf(
                "android.widget.TimePicker",
                "androidx.compose.material3.TimePicker",
                "androidx.compose.material3.TimeInput",
            )
            .forEach { source ->
                assertTrue(entries.single { it.source == source }.notes.isNotBlank())
            }

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
        assertEquals(141, pending.size)
        assertEquals(
            mapOf(
                InventoryFamily.PLATFORM to 53,
                InventoryFamily.MATERIAL2 to 26,
                InventoryFamily.MATERIAL3 to 62,
            ),
            pending.groupingBy { it.family }.eachCount(),
        )
        pending.forEach {
            assertEquals(InventoryStatus.PENDING, it.status)
            assertTrue("Pending sources have no runnable sample IDs", it.catalogIds.isEmpty())
        }
        assertEquals(
            setOf(
                "android.widget.ListPopupWindow",
                "android.widget.PopupMenu",
                "android.widget.PopupWindow",
            ),
            ComponentInventory.pending(entries, "  POPUP  ", InventoryFamily.PLATFORM)
                .map { it.source }
                .toSet(),
        )
        val libraryMenus =
            ComponentInventory.pending(entries, "dropdownmenu", InventoryFamily.MATERIAL3)
        assertEquals(
            setOf(
                "androidx.compose.material3.DropdownMenu",
                "androidx.compose.material3.DropdownMenuItem",
                "androidx.compose.material3.ExposedDropdownMenuBox",
            ),
            libraryMenus.map { it.source }.toSet(),
        )
        libraryMenus.forEach { assertNull(it.apiIntroduced) }
        assertTrue(
            ComponentInventory.pending(entries, "dropdownmenu", InventoryFamily.PLATFORM).isEmpty()
        )
        assertEquals(
            3,
            ComponentInventory.pending(entries, "dropdownmenu", InventoryFamily.MATERIAL2).size,
        )
        assertTrue(ComponentInventory.pending(entries, "timepicker").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "timeinput").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "no-matching-source-api").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "checkbox").isEmpty())
        assertEquals(
            listOf("android.widget.DatePicker"),
            ComponentInventory.pending(entries, "datepicker", InventoryFamily.PLATFORM).map {
                it.source
            },
        )
        assertTrue(
            ComponentInventory.pending(entries, "datepicker", InventoryFamily.MATERIAL3).isEmpty()
        )
    }
}
