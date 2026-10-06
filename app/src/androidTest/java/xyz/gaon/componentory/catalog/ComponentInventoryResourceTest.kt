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
            mapOf(InventoryStatus.IMPLEMENTED to 162, InventoryStatus.PENDING to 77),
            entries.groupingBy { it.status }.eachCount(),
        )
        assertEquals(
            mapOf(
                InventoryFamily.PLATFORM to 72,
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
        mapOf(
                "android.widget.TextClock" to ("TEXT_CLOCK" to 17),
                "android.widget.AnalogClock" to ("ANALOG_CLOCK" to 1),
                "android.widget.DigitalClock" to ("DIGITAL_CLOCK" to 1),
                "android.widget.Chronometer" to ("CHRONOMETER" to 1),
                "android.widget.ScrollView" to ("SCROLL_VIEW" to 1),
                "android.widget.HorizontalScrollView" to ("HORIZONTAL_SCROLL_VIEW" to 3),
            )
            .forEach { (source, metadata) ->
                val row = entries.single { it.source == source }
                assertEquals(InventoryFamily.PLATFORM, row.family)
                assertEquals(InventoryStatus.IMPLEMENTED, row.status)
                assertEquals(listOf(metadata.first), row.catalogIds)
                assertEquals(metadata.second, row.apiIntroduced)
            }
        mapOf(
                "android.widget.ViewAnimator" to "VIEW_ANIMATOR",
                "android.widget.ViewSwitcher" to "VIEW_SWITCHER",
                "android.widget.ViewFlipper" to "VIEW_FLIPPER",
                "android.widget.TextSwitcher" to "TEXT_SWITCHER",
                "android.widget.ImageSwitcher" to "IMAGE_SWITCHER",
            )
            .forEach { (source, catalogId) ->
                val row = entries.single { it.source == source }
                assertEquals(InventoryFamily.PLATFORM, row.family)
                assertEquals(InventoryStatus.IMPLEMENTED, row.status)
                assertEquals(listOf(catalogId), row.catalogIds)
                assertEquals(1, row.apiIntroduced)
            }
        mapOf(
                "android.widget.FrameLayout" to ("FRAME_LAYOUT" to 1),
                "android.widget.LinearLayout" to ("LINEAR_LAYOUT" to 1),
                "android.widget.TableLayout" to ("TABLE_LAYOUT" to 1),
                "android.widget.GridLayout" to ("GRID_LAYOUT" to 14),
                "android.widget.RelativeLayout" to ("RELATIVE_LAYOUT" to 1),
                "android.widget.Space" to ("SPACE" to 14),
                "android.widget.AbsoluteLayout" to ("ABSOLUTE_LAYOUT" to 1),
            )
            .forEach { (source, metadata) ->
                val row = entries.single { it.source == source }
                assertEquals(InventoryFamily.PLATFORM, row.family)
                assertEquals(InventoryStatus.IMPLEMENTED, row.status)
                assertEquals(listOf(metadata.first), row.catalogIds)
                assertEquals(metadata.second, row.apiIntroduced)
            }
        // TableRow rows live inside the TableLayout sample like RadioGroup inside RADIO.
        val tableRow = entries.single { it.source == "android.widget.TableRow" }
        assertEquals(InventoryStatus.IMPLEMENTED, tableRow.status)
        assertEquals(listOf("TABLE_LAYOUT"), tableRow.catalogIds)
        mapOf(
                "android.widget.ListView" to "LIST_VIEW",
                "android.widget.GridView" to "GRID_VIEW",
                "android.widget.ExpandableListView" to "EXPANDABLE_LIST_VIEW",
            )
            .forEach { (source, catalogId) ->
                val row = entries.single { it.source == source }
                assertEquals(InventoryFamily.PLATFORM, row.family)
                assertEquals(InventoryStatus.IMPLEMENTED, row.status)
                assertEquals(listOf(catalogId), row.catalogIds)
                assertEquals(1, row.apiIntroduced)
            }
        mapOf(
                "android.widget.ZoomControls" to ("ZOOM_CONTROLS" to 1),
                "android.widget.ZoomButton" to ("ZOOM_BUTTON" to 1),
                "android.widget.ZoomButtonsController" to ("ZOOM_BUTTONS_CONTROLLER" to 4),
            )
            .forEach { (source, metadata) ->
                val row = entries.single { it.source == source }
                assertEquals(InventoryFamily.PLATFORM, row.family)
                assertEquals(InventoryStatus.IMPLEMENTED, row.status)
                assertEquals(listOf(metadata.first), row.catalogIds)
                assertEquals(metadata.second, row.apiIntroduced)
            }
        mapOf(
                "android.widget.AdapterViewFlipper" to "ADAPTER_VIEW_FLIPPER",
                "android.widget.StackView" to "STACK_VIEW",
            )
            .forEach { (source, catalogId) ->
                val row = entries.single { it.source == source }
                assertEquals(InventoryFamily.PLATFORM, row.family)
                assertEquals(InventoryStatus.IMPLEMENTED, row.status)
                assertEquals(listOf(catalogId), row.catalogIds)
                assertEquals(11, row.apiIntroduced)
            }
        mapOf(
                "android.app.Dialog" to "PLAIN_DIALOG",
                "android.app.ProgressDialog" to "PROGRESS_DIALOG",
                "android.widget.Toast" to "TOAST",
            )
            .forEach { (source, catalogId) ->
                val row = entries.single { it.source == source }
                assertEquals(InventoryFamily.PLATFORM, row.family)
                assertEquals(InventoryStatus.IMPLEMENTED, row.status)
                assertEquals(listOf(catalogId), row.catalogIds)
                assertEquals(1, row.apiIntroduced)
            }
        // The TabWidget row lives inside the TabHost sample like TableRow inside
        // TableLayout.
        listOf("android.widget.TabHost", "android.widget.TabWidget").forEach { source ->
            val row = entries.single { it.source == source }
            assertEquals(InventoryFamily.PLATFORM, row.family)
            assertEquals(InventoryStatus.IMPLEMENTED, row.status)
            assertEquals(listOf("TAB_HOST"), row.catalogIds)
            assertEquals(1, row.apiIntroduced)
        }
        mapOf(
                "android.widget.Gallery" to ("GALLERY" to 1),
                "android.widget.SlidingDrawer" to ("SLIDING_DRAWER" to 3),
                "android.widget.TwoLineListItem" to ("TWO_LINE_LIST_ITEM" to 1),
                "android.widget.PopupWindow" to ("POPUP_WINDOW" to 1),
                "android.widget.ListPopupWindow" to ("LIST_POPUP_WINDOW" to 11),
                "android.widget.Toolbar" to ("TOOLBAR" to 21),
                "android.widget.ActionMenuView" to ("ACTION_MENU_VIEW" to 21),
            )
            .forEach { (source, metadata) ->
                val row = entries.single { it.source == source }
                assertEquals(InventoryFamily.PLATFORM, row.family)
                assertEquals(InventoryStatus.IMPLEMENTED, row.status)
                assertEquals(listOf(metadata.first), row.catalogIds)
                assertEquals(metadata.second, row.apiIntroduced)
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
        assertEquals(77, pending.size)
        assertEquals(
            mapOf(
                InventoryFamily.PLATFORM to 2,
                InventoryFamily.MATERIAL2 to 21,
                InventoryFamily.MATERIAL3 to 54,
            ),
            pending.groupingBy { it.family }.eachCount(),
        )
        pending.forEach {
            assertEquals(InventoryStatus.PENDING, it.status)
            assertTrue("Pending sources have no runnable sample IDs", it.catalogIds.isEmpty())
        }
        assertTrue(
            ComponentInventory.pending(entries, "  POPUP  ", InventoryFamily.PLATFORM).isEmpty()
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
        assertTrue(
            ComponentInventory.pending(entries, "listpopupwindow", InventoryFamily.PLATFORM)
                .isEmpty()
        )
        assertTrue(ComponentInventory.pending(entries, "datepicker").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "calendarview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "daterangepicker").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "checkedtextview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "clock").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "chronometer").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "scrollview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "horizontalscrollview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "viewanimator").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "viewswitcher").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "viewflipper").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "adapterviewflipper").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "stackview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "toast").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "progressdialog").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "tabhost").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "tabwidget").isEmpty())
        assertEquals(
            emptyList<String>(),
            ComponentInventory.pending(entries, "dialog", InventoryFamily.PLATFORM).map {
                it.source
            },
        )
        assertEquals(
            listOf("androidx.compose.material3.BasicAlertDialog"),
            ComponentInventory.pending(entries, "dialog", InventoryFamily.MATERIAL3).map {
                it.source
            },
        )
        assertTrue(ComponentInventory.pending(entries, "webview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "dialerfilter").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "videoview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "mediacontroller").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "shareactionprovider").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "edgeeffect").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "quickcontactbadge").isEmpty())
        assertEquals(
            listOf("android.widget.inline.InlineContentView"),
            ComponentInventory.pending(entries, "inlinecontentview", InventoryFamily.PLATFORM).map {
                it.source
            },
        )
        assertTrue(ComponentInventory.pending(entries, "textswitcher").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "imageswitcher").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "framelayout").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "linearlayout").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "tablelayout").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "tablerow").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "gridlayout").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "relativelayout").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "space").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "absolutelayout").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "listview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "gridview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "expandablelistview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "zoomcontrols").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "zoombutton").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "zoombuttonscontroller").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "popupwindow").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "toolbar").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "actionmenuview").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "gallery").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "slidingdrawer").isEmpty())
        assertTrue(ComponentInventory.pending(entries, "twolinelistitem").isEmpty())
        listOf(
                "android.widget.TextView",
                "androidx.compose.material.Text",
                "androidx.compose.material3.Text",
            )
            .forEach { source -> assertTrue(ComponentInventory.pending(entries, source).isEmpty()) }
    }
}
