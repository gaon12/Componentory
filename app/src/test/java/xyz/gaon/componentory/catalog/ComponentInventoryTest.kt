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
    fun standaloneClocksReuseTheirOwnCanonicalRowsWithoutSharingClockSources() {
        val rows = inventory()
        mapOf(
                "android.widget.TextClock" to ("TEXT_CLOCK" to "17"),
                "android.widget.AnalogClock" to ("ANALOG_CLOCK" to "1"),
                "android.widget.DigitalClock" to ("DIGITAL_CLOCK" to "1"),
                "android.widget.Chronometer" to ("CHRONOMETER" to "1"),
                "android.widget.ScrollView" to ("SCROLL_VIEW" to "1"),
                "android.widget.HorizontalScrollView" to ("HORIZONTAL_SCROLL_VIEW" to "3"),
            )
            .forEach { (source, metadata) ->
                val row = rows.single { it.source == source }
                assertEquals("PLATFORM", row.provider)
                assertEquals("Implemented", row.status)
                assertEquals(listOf(metadata.first), row.catalogIds)
                assertEquals(metadata.second, row.apiIntroduced)
            }
        mapOf(
                "android.widget.FrameLayout" to ("FRAME_LAYOUT" to "1"),
                "android.widget.LinearLayout" to ("LINEAR_LAYOUT" to "1"),
                "android.widget.TableLayout" to ("TABLE_LAYOUT" to "1"),
                "android.widget.GridLayout" to ("GRID_LAYOUT" to "14"),
                "android.widget.RelativeLayout" to ("RELATIVE_LAYOUT" to "1"),
                "android.widget.Space" to ("SPACE" to "14"),
                "android.widget.AbsoluteLayout" to ("ABSOLUTE_LAYOUT" to "1"),
            )
            .forEach { (source, metadata) ->
                val row = rows.single { it.source == source }
                assertEquals("PLATFORM", row.provider)
                assertEquals("Implemented", row.status)
                assertEquals(listOf(metadata.first), row.catalogIds)
                assertEquals(metadata.second, row.apiIntroduced)
            }
        val tableRow = rows.single { it.source == "android.widget.TableRow" }
        assertEquals("Implemented", tableRow.status)
        assertEquals(listOf("TABLE_LAYOUT"), tableRow.catalogIds)
        mapOf(
                "android.widget.ListView" to "LIST_VIEW",
                "android.widget.GridView" to "GRID_VIEW",
                "android.widget.ExpandableListView" to "EXPANDABLE_LIST_VIEW",
            )
            .forEach { (source, catalogId) ->
                val row = rows.single { it.source == source }
                assertEquals("PLATFORM", row.provider)
                assertEquals("Implemented", row.status)
                assertEquals(listOf(catalogId), row.catalogIds)
                assertEquals("1", row.apiIntroduced)
            }
        mapOf(
                "android.widget.ZoomControls" to ("ZOOM_CONTROLS" to "1"),
                "android.widget.ZoomButton" to ("ZOOM_BUTTON" to "1"),
                "android.widget.ZoomButtonsController" to ("ZOOM_BUTTONS_CONTROLLER" to "4"),
                "android.widget.AdapterViewFlipper" to ("ADAPTER_VIEW_FLIPPER" to "11"),
                "android.widget.StackView" to ("STACK_VIEW" to "11"),
                "android.app.Dialog" to ("PLAIN_DIALOG" to "1"),
                "android.app.ProgressDialog" to ("PROGRESS_DIALOG" to "1"),
                "android.widget.Toast" to ("TOAST" to "1"),
            )
            .forEach { (source, metadata) ->
                val row = rows.single { it.source == source }
                assertEquals("PLATFORM", row.provider)
                assertEquals("Implemented", row.status)
                assertEquals(listOf(metadata.first), row.catalogIds)
                assertEquals(metadata.second, row.apiIntroduced)
            }
        // The TabWidget row lives inside the TabHost sample like TableRow inside
        // TableLayout.
        listOf("android.widget.TabHost", "android.widget.TabWidget").forEach { source ->
            val row = rows.single { it.source == source }
            assertEquals("PLATFORM", row.provider)
            assertEquals("Implemented", row.status)
            assertEquals(listOf("TAB_HOST"), row.catalogIds)
            assertEquals("1", row.apiIntroduced)
        }
        mapOf(
                "android.widget.Gallery" to ("GALLERY" to "1"),
                "android.widget.SlidingDrawer" to ("SLIDING_DRAWER" to "3"),
                "android.widget.TwoLineListItem" to ("TWO_LINE_LIST_ITEM" to "1"),
                "android.widget.PopupWindow" to ("POPUP_WINDOW" to "1"),
                "android.widget.ListPopupWindow" to ("LIST_POPUP_WINDOW" to "11"),
                "android.widget.Toolbar" to ("TOOLBAR" to "21"),
                "android.widget.ActionMenuView" to ("ACTION_MENU_VIEW" to "21"),
                "android.webkit.WebView" to ("WEB_VIEW" to "1"),
                "android.widget.QuickContactBadge" to ("QUICK_CONTACT_BADGE" to "5"),
                "android.widget.DialerFilter" to ("DIALER_FILTER" to "1"),
                "android.widget.VideoView" to ("VIDEO_VIEW" to "1"),
                "android.widget.MediaController" to ("MEDIA_CONTROLLER" to "1"),
                "android.widget.ShareActionProvider" to ("SHARE_ACTION_PROVIDER" to "14"),
            )
            .forEach { (source, metadata) ->
                val row = rows.single { it.source == source }
                assertEquals("PLATFORM", row.provider)
                assertEquals("Implemented", row.status)
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
                val row = rows.single { it.source == source }
                assertEquals("PLATFORM", row.provider)
                assertEquals("Implemented", row.status)
                assertEquals(listOf(catalogId), row.catalogIds)
                assertEquals("1", row.apiIntroduced)
            }
    }

    @Test
    fun standaloneTimeControlsReuseCanonicalRowsWithoutInflatingSourceCounts() {
        val rows = inventory()
        mapOf(
                "android.widget.TimePicker" to "TIME_PICKER",
                "androidx.compose.material3.TimePicker" to "TIME_PICKER",
                "androidx.compose.material3.TimeInput" to "TIME_INPUT",
            )
            .forEach { (source, sampleId) ->
                val row = rows.single { it.source == source }
                assertEquals("Implemented", row.status)
                assertEquals(listOf(sampleId, "TIME_PICKER_DIALOG"), row.catalogIds)
                assertEquals(if (row.provider == "PLATFORM") "1" else "", row.apiIntroduced)
                assertTrue(row.notes.startsWith("Standalone inline sample and interactive"))
            }
    }

    @Test
    fun catalogCountsCanonicalRowsWithoutCountingThemesAsSeparateSources() {
        val rows = inventory()
        assertEquals(239, rows.size)
        assertEquals(114, LabComponent.entries.size)
        assertEquals(
            303,
            LabComponent.entries.sumOf { component ->
                DesignFamily.entries.count { family ->
                    family.unsupportedReason(component, 24) == null
                }
            },
        )
        assertEquals(
            mapOf("Implemented" to 161, "Pending" to 78),
            rows.groupingBy { it.status }.eachCount(),
        )
        assertEquals(
            mapOf("PLATFORM" to 71, "MATERIAL2" to 31, "MATERIAL3" to 59),
            rows.filter { it.status == "Implemented" }.groupingBy { it.provider }.eachCount(),
        )
        assertEquals(
            mapOf("PLATFORM" to 3, "MATERIAL2" to 21, "MATERIAL3" to 54),
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
                            row.provider == "PLATFORM" && component == LabComponent.TABLE_LAYOUT ->
                                setOf("android.widget.TableRow")
                            row.provider == "PLATFORM" && component == LabComponent.TAB_HOST ->
                                setOf("android.widget.TabWidget")
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
