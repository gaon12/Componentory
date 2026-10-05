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
            mapOf(InventoryStatus.IMPLEMENTED to 116, InventoryStatus.PENDING to 123),
            entries.groupingBy { it.status }.eachCount(),
        )
        assertEquals(
            mapOf(
                InventoryFamily.PLATFORM to 26,
                InventoryFamily.MATERIAL2 to 31,
                InventoryFamily.MATERIAL3 to 59,
            ),
            entries
                .filter { it.status == InventoryStatus.IMPLEMENTED }
                .groupingBy { it.family }
                .eachCount(),
        )
        val group = entries.single { it.source == "android.widget.RadioGroup" }
        assertEquals(listOf("RADIO"), group.catalogIds)
        assertEquals("RadioButton sample uses a real RadioGroup.", group.notes)
        val segmented = entries.single { it.source == "androidx.compose.material3.SegmentedButton" }
        assertEquals(listOf("SINGLE_SEGMENTED", "MULTI_SEGMENTED"), segmented.catalogIds)
        assertEquals("Used in both segmented-row samples.", segmented.notes)
        mapOf(
                "android.widget.TextView" to "TEXT",
                "android.widget.CheckedTextView" to "CHECKED_TEXT_VIEW",
                "androidx.compose.material.Text" to "TEXT",
                "androidx.compose.material3.Text" to "TEXT",
            )
            .forEach { (source, catalogId) ->
                val text = entries.single { it.source == source }
                assertEquals(InventoryStatus.IMPLEMENTED, text.status)
                assertEquals(listOf(catalogId), text.catalogIds)
                if (text.family == InventoryFamily.PLATFORM) assertEquals(1, text.apiIntroduced)
                else assertNull(text.apiIntroduced)
            }
        listOf("android.app.DatePickerDialog", "androidx.compose.material3.DatePickerDialog")
            .forEach { source ->
                val date = entries.single { it.source == source }
                assertEquals(InventoryStatus.IMPLEMENTED, date.status)
                assertEquals(listOf("DATE_PICKER_DIALOG"), date.catalogIds)
            }
        mapOf(
                "android.widget.DatePicker" to ("DATE_PICKER" to 1),
                "android.widget.CalendarView" to ("CALENDAR_VIEW" to 11),
            )
            .forEach { (source, metadata) ->
                val date = entries.single { it.source == source }
                assertEquals(InventoryFamily.PLATFORM, date.family)
                assertEquals(InventoryStatus.IMPLEMENTED, date.status)
                assertEquals(listOf(metadata.first), date.catalogIds)
                assertEquals(metadata.second, date.apiIntroduced)
            }
        // One canonical API row covers the inline picker and its genuine dialog content.
        val inlineDate = entries.single { it.source == "androidx.compose.material3.DatePicker" }
        assertEquals(InventoryFamily.MATERIAL3, inlineDate.family)
        assertEquals(InventoryStatus.IMPLEMENTED, inlineDate.status)
        assertEquals(listOf("DATE_PICKER", "DATE_PICKER_DIALOG"), inlineDate.catalogIds)
        assertNull(inlineDate.apiIntroduced)
        assertEquals(
            "Standalone inline sample and interactive calendar content inside the DatePickerDialog sample.",
            inlineDate.notes,
        )
        val dateRange = entries.single { it.source == "androidx.compose.material3.DateRangePicker" }
        assertEquals(InventoryFamily.MATERIAL3, dateRange.family)
        assertEquals(InventoryStatus.IMPLEMENTED, dateRange.status)
        assertEquals(listOf("DATE_RANGE_PICKER"), dateRange.catalogIds)
        assertNull(dateRange.apiIntroduced)
        listOf("android.app.TimePickerDialog", "androidx.compose.material3.TimePickerDialog")
            .forEach { source ->
                val time = entries.single { it.source == source }
                assertEquals(InventoryStatus.IMPLEMENTED, time.status)
                assertEquals(listOf("TIME_PICKER_DIALOG"), time.catalogIds)
            }
        mapOf(
                "android.widget.TimePicker" to "TIME_PICKER",
                "androidx.compose.material3.TimePicker" to "TIME_PICKER",
                "androidx.compose.material3.TimeInput" to "TIME_INPUT",
            )
            .forEach { (source, catalogId) ->
                val time = entries.single { it.source == source }
                assertEquals(InventoryStatus.IMPLEMENTED, time.status)
                assertEquals(listOf(catalogId, "TIME_PICKER_DIALOG"), time.catalogIds)
                assertTrue(time.notes.startsWith("Standalone inline sample and interactive"))
                if (time.family == InventoryFamily.PLATFORM) assertEquals(1, time.apiIntroduced)
                else assertNull(time.apiIntroduced)
            }
        mapOf(
                "androidx.compose.material.Card" to "CARD",
                "androidx.compose.material.Surface" to "SURFACE",
                "androidx.compose.material3.Card" to "CARD",
                "androidx.compose.material3.ElevatedCard" to "ELEVATED_CARD",
                "androidx.compose.material3.OutlinedCard" to "OUTLINED_CARD",
                "androidx.compose.material3.Surface" to "SURFACE",
            )
            .forEach { (source, catalogId) ->
                val container = entries.single { it.source == source }
                assertEquals(InventoryStatus.IMPLEMENTED, container.status)
                assertEquals(listOf(catalogId), container.catalogIds)
                assertNull(container.apiIntroduced)
            }

        listOf(
                "android.widget.PopupMenu",
                "androidx.compose.material.DropdownMenu",
                "androidx.compose.material.DropdownMenuItem",
                "androidx.compose.material3.DropdownMenu",
                "androidx.compose.material3.DropdownMenuItem",
            )
            .forEach { source ->
                val menu = entries.single { it.source == source }
                assertEquals(InventoryStatus.IMPLEMENTED, menu.status)
                assertEquals(listOf("POPUP_MENU"), menu.catalogIds)
                if (source.startsWith("android.widget.")) assertEquals(11, menu.apiIntroduced)
                else assertNull(menu.apiIntroduced)
                if (source.endsWith("DropdownMenuItem")) assertTrue(menu.notes.isNotBlank())
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
        assertEquals(123, pending.size)
        assertEquals(
            mapOf(
                InventoryFamily.PLATFORM to 48,
                InventoryFamily.MATERIAL2 to 21,
                InventoryFamily.MATERIAL3 to 54,
            ),
            pending.groupingBy { it.family }.eachCount(),
        )
        pending.forEach {
            assertEquals(InventoryStatus.PENDING, it.status)
            assertTrue("Pending sources have no runnable sample IDs", it.catalogIds.isEmpty())
        }
        assertEquals(
            setOf("android.widget.ListPopupWindow", "android.widget.PopupWindow"),
            ComponentInventory.pending(entries, "  POPUP  ", InventoryFamily.PLATFORM)
                .map { it.source }
                .toSet(),
        )
        val libraryMenus =
            ComponentInventory.pending(entries, "dropdownmenu", InventoryFamily.MATERIAL3)
        assertEquals(
            setOf("androidx.compose.material3.ExposedDropdownMenuBox"),
            libraryMenus.map { it.source }.toSet(),
        )
        libraryMenus.forEach { assertNull(it.apiIntroduced) }
        assertTrue(
            ComponentInventory.pending(entries, "dropdownmenu", InventoryFamily.PLATFORM).isEmpty()
        )
        assertEquals(
            1,
            ComponentInventory.pending(entries, "dropdownmenu", InventoryFamily.MATERIAL2).size,
        )
        assertTrue(ComponentInventory.pending(entries, "popupmenu").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "timepicker").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "timeinput").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "card").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "surface").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "no-matching-source-api").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "checkbox").isEmpty())
        assertEquals(
            listOf("android.widget.ListPopupWindow"),
            ComponentInventory.pending(entries, "listpopupwindow", InventoryFamily.PLATFORM).map {
                it.source
            },
        )
        assertTrue(ComponentInventory.pending(entries, "datepicker").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "calendarview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "daterangepicker").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "checkedtextview").isEmpty())
        listOf(
                "android.widget.TextView",
                "androidx.compose.material.Text",
                "androidx.compose.material3.Text",
            )
            .forEach { source -> assertTrue(ComponentInventory.pending(entries, source).isEmpty()) }
    }
}
