package xyz.gaon.componentory.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentAvailabilityTest {
    @Test
    fun standaloneTimeControlsUseActualFrameworkAndMaterial3Suppliers() {
        DesignFamily.entries.forEach { family ->
            val clockSupported = family != DesignFamily.MATERIAL2
            assertEquals(
                clockSupported,
                family.unsupportedReason(LabComponent.TIME_PICKER, 24) == null,
            )
            assertEquals(
                family == DesignFamily.MATERIAL3,
                family.unsupportedReason(LabComponent.TIME_INPUT, 24) == null,
            )
            assertEquals(
                when {
                    family.platform != null -> "android.widget.TimePicker"
                    family == DesignFamily.MATERIAL3 -> "androidx.compose.material3.TimePicker"
                    else -> "Not provided"
                },
                family.source(LabComponent.TIME_PICKER),
            )
            assertEquals(
                if (family == DesignFamily.MATERIAL3) "androidx.compose.material3.TimeInput"
                else "Not provided",
                family.source(LabComponent.TIME_INPUT),
            )
        }
        assertEquals(1, LabComponent.TIME_PICKER.minimumApi)
        assertEquals(ComponentCategory.PICKER, LabComponent.TIME_PICKER.category)
        assertEquals(ComponentCategory.PICKER, LabComponent.TIME_INPUT.category)
    }

    @Test
    fun standaloneClocksUseOnlyFrameworkSuppliersWithTheirRealApiLevels() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        val clocks =
            mapOf(
                LabComponent.TEXT_CLOCK to ("android.widget.TextClock" to 17),
                LabComponent.ANALOG_CLOCK to ("android.widget.AnalogClock" to 1),
                LabComponent.DIGITAL_CLOCK to ("android.widget.DigitalClock" to 1),
                LabComponent.CHRONOMETER to ("android.widget.Chronometer" to 1),
            )
        clocks.forEach { (component, metadata) ->
            val (source, minimumApi) = metadata
            platform.forEach { family ->
                if (minimumApi > 1)
                    assertNotNull(family.unsupportedReason(component, minimumApi - 1))
                assertNull(family.unsupportedReason(component, minimumApi))
                assertNull(family.unsupportedReason(component, 36))
                assertEquals(source, family.source(component))
            }
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                assertNotNull(family.unsupportedReason(component, 36))
                assertEquals("Not provided", family.source(component))
            }
            assertTrue(component.matchesSearch(source))
        }
        assertEquals(17, LabComponent.TEXT_CLOCK.minimumApi)
        assertEquals(ComponentCategory.CONTENT, LabComponent.TEXT_CLOCK.category)
        assertEquals(ComponentCategory.CONTENT, LabComponent.CHRONOMETER.category)
        assertEquals(ComponentCategory.LEGACY, LabComponent.ANALOG_CLOCK.category)
        assertEquals(ComponentCategory.LEGACY, LabComponent.DIGITAL_CLOCK.category)
    }

    @Test
    fun scrollContainersUseOnlyFrameworkSuppliersWithTheirRealApiLevels() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        mapOf(
                LabComponent.SCROLL_VIEW to ("android.widget.ScrollView" to 1),
                LabComponent.HORIZONTAL_SCROLL_VIEW to ("android.widget.HorizontalScrollView" to 3),
            )
            .forEach { (component, metadata) ->
                val (source, minimumApi) = metadata
                platform.forEach { family ->
                    if (minimumApi > 1)
                        assertNotNull(family.unsupportedReason(component, minimumApi - 1))
                    assertNull(family.unsupportedReason(component, minimumApi))
                    assertNull(family.unsupportedReason(component, 36))
                    assertEquals(source, family.source(component))
                }
                listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                    assertNotNull(family.unsupportedReason(component, 36))
                    assertEquals("Not provided", family.source(component))
                }
                assertEquals(ComponentCategory.LAYOUT, component.category)
                assertTrue(component.matchesSearch(source))
            }
    }

    @Test
    fun viewSwitchersUseOnlyFrameworkSuppliersFromApiOne() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        mapOf(
                LabComponent.VIEW_ANIMATOR to "android.widget.ViewAnimator",
                LabComponent.VIEW_SWITCHER to "android.widget.ViewSwitcher",
                LabComponent.VIEW_FLIPPER to "android.widget.ViewFlipper",
                LabComponent.TEXT_SWITCHER to "android.widget.TextSwitcher",
                LabComponent.IMAGE_SWITCHER to "android.widget.ImageSwitcher",
            )
            .forEach { (component, source) ->
                platform.forEach { family ->
                    assertNull(family.unsupportedReason(component, 1))
                    assertNull(family.unsupportedReason(component, 36))
                    assertEquals(source, family.source(component))
                }
                listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                    assertNotNull(family.unsupportedReason(component, 36))
                    assertEquals("Not provided", family.source(component))
                }
                assertEquals(1, component.minimumApi)
                assertEquals(ComponentCategory.LAYOUT, component.category)
                assertTrue(component.matchesSearch(source))
                assertFalse(component.matchesSearch("androidx.compose.material3.Switcher"))
            }
        assertEquals(4, LabComponent.VIEW_ANIMATOR.switcherPageCount)
        assertEquals(2, LabComponent.VIEW_SWITCHER.switcherPageCount)
        assertEquals(4, LabComponent.VIEW_FLIPPER.switcherPageCount)
        assertEquals(4, LabComponent.TEXT_SWITCHER.switcherPageCount)
        assertEquals(2, LabComponent.IMAGE_SWITCHER.switcherPageCount)
    }

    @Test
    fun frameworkLayoutsUseOnlyFrameworkSuppliersWithTheirRealApiLevels() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        mapOf(
                LabComponent.FRAME_LAYOUT to ("android.widget.FrameLayout" to 1),
                LabComponent.LINEAR_LAYOUT to ("android.widget.LinearLayout" to 1),
                LabComponent.TABLE_LAYOUT to ("android.widget.TableLayout" to 1),
                LabComponent.GRID_LAYOUT to ("android.widget.GridLayout" to 14),
                LabComponent.RELATIVE_LAYOUT to ("android.widget.RelativeLayout" to 1),
                LabComponent.SPACE to ("android.widget.Space" to 14),
                LabComponent.ABSOLUTE_LAYOUT to ("android.widget.AbsoluteLayout" to 1),
            )
            .forEach { (component, metadata) ->
                val (source, minimumApi) = metadata
                platform.forEach { family ->
                    if (minimumApi > 1)
                        assertNotNull(family.unsupportedReason(component, minimumApi - 1))
                    assertNull(family.unsupportedReason(component, minimumApi))
                    assertNull(family.unsupportedReason(component, 36))
                    assertEquals(source, family.source(component))
                }
                listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                    assertNotNull(family.unsupportedReason(component, 36))
                    assertEquals("Not provided", family.source(component))
                }
                assertEquals(minimumApi, component.minimumApi)
                assertTrue(component.matchesSearch(source))
            }
        assertEquals(ComponentCategory.LEGACY, LabComponent.ABSOLUTE_LAYOUT.category)
        listOf(
                LabComponent.FRAME_LAYOUT,
                LabComponent.LINEAR_LAYOUT,
                LabComponent.TABLE_LAYOUT,
                LabComponent.GRID_LAYOUT,
                LabComponent.RELATIVE_LAYOUT,
                LabComponent.SPACE,
            )
            .forEach { assertEquals(ComponentCategory.LAYOUT, it.category) }
    }

    @Test
    fun adapterListsUseOnlyFrameworkSuppliersAndCarryRealRowCounts() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        mapOf(
                LabComponent.LIST_VIEW to ("android.widget.ListView" to 6),
                LabComponent.GRID_VIEW to ("android.widget.GridView" to 9),
                LabComponent.EXPANDABLE_LIST_VIEW to ("android.widget.ExpandableListView" to 3),
            )
            .forEach { (component, metadata) ->
                val (source, rows) = metadata
                platform.forEach { family ->
                    assertNull(family.unsupportedReason(component, 24))
                    assertEquals(source, family.source(component))
                }
                listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                    assertNotNull(family.unsupportedReason(component, 36))
                    assertEquals("Not provided", family.source(component))
                }
                assertEquals(1, component.minimumApi)
                assertEquals(rows, component.listRowCount)
                assertEquals(ComponentCategory.LAYOUT, component.category)
                assertTrue(component.matchesSearch(source))
            }
        // The expandable list starts with one open group; plain lists start unchecked.
        assertEquals(0, LabComponent.LIST_VIEW.initialValue)
        assertEquals(0, LabComponent.GRID_VIEW.initialValue)
        assertEquals(1, LabComponent.EXPANDABLE_LIST_VIEW.initialValue)
    }

    @Test
    fun zoomControlsUseDeprecatedFrameworkSuppliersWithRealApiLevels() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        mapOf(
                LabComponent.ZOOM_CONTROLS to ("android.widget.ZoomControls" to (1 to 29)),
                LabComponent.ZOOM_BUTTON to ("android.widget.ZoomButton" to (1 to 26)),
                LabComponent.ZOOM_BUTTONS_CONTROLLER to
                    ("android.widget.ZoomButtonsController" to (4 to 26)),
            )
            .forEach { (component, metadata) ->
                val (source, levels) = metadata
                val (minimumApi, deprecatedApi) = levels
                platform.forEach { family ->
                    if (minimumApi > 1)
                        assertNotNull(family.unsupportedReason(component, minimumApi - 1))
                    assertNull(family.unsupportedReason(component, minimumApi))
                    assertNull(family.unsupportedReason(component, 36))
                    assertEquals(source, family.source(component))
                }
                listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                    assertNotNull(family.unsupportedReason(component, 36))
                    assertEquals("Not provided", family.source(component))
                }
                assertEquals(minimumApi, component.minimumApi)
                assertEquals(deprecatedApi, component.deprecatedApi)
                assertEquals(ComponentCategory.LEGACY, component.category)
                assertEquals(5, component.initialValue)
                assertEquals(10, component.zoomLevelMax)
                assertTrue(component.matchesSearch(source))
            }
    }

    @Test
    fun adapterAnimatorsUseOnlyFrameworkSuppliersWithRealApiLevels() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        mapOf(
                LabComponent.ADAPTER_VIEW_FLIPPER to ("android.widget.AdapterViewFlipper" to 4),
                LabComponent.STACK_VIEW to ("android.widget.StackView" to 6),
            )
            .forEach { (component, metadata) ->
                val (source, pages) = metadata
                platform.forEach { family ->
                    assertNotNull(family.unsupportedReason(component, 10))
                    assertNull(family.unsupportedReason(component, 11))
                    assertNull(family.unsupportedReason(component, 36))
                    assertEquals(source, family.source(component))
                }
                listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                    assertNotNull(family.unsupportedReason(component, 36))
                    assertEquals("Not provided", family.source(component))
                }
                assertEquals(11, component.minimumApi)
                assertNull(component.deprecatedApi)
                assertEquals(pages, component.adapterPageCount)
                assertEquals(ComponentCategory.LAYOUT, component.category)
                assertEquals(0, component.initialValue)
                assertTrue(component.matchesSearch(source))
            }
        assertFalse(LabComponent.ADAPTER_VIEW_FLIPPER.matchesSearch("android.widget.StackView"))
    }

    @Test
    fun transientWindowsUseOnlyFrameworkSuppliersAndCopyOnlyTheOpenCount() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        mapOf(
                LabComponent.PLAIN_DIALOG to ("android.app.Dialog" to ComponentCategory.FEEDBACK),
                LabComponent.PROGRESS_DIALOG to
                    ("android.app.ProgressDialog" to ComponentCategory.LEGACY),
                LabComponent.TOAST to ("android.widget.Toast" to ComponentCategory.FEEDBACK),
            )
            .forEach { (component, metadata) ->
                val (source, category) = metadata
                platform.forEach { family ->
                    assertNull(family.unsupportedReason(component, 1))
                    assertNull(family.unsupportedReason(component, 36))
                    assertEquals(source, family.source(component))
                }
                listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                    assertNotNull(family.unsupportedReason(component, 36))
                    assertEquals("Not provided", family.source(component))
                }
                assertEquals(1, component.minimumApi)
                assertEquals(category, component.category)
                assertTrue(component.isTransientWindow)
                assertTrue(component.matchesSearch(source))
            }
        assertEquals(26, LabComponent.PROGRESS_DIALOG.deprecatedApi)
        assertNull(LabComponent.PLAIN_DIALOG.deprecatedApi)
        assertNull(LabComponent.TOAST.deprecatedApi)
        // The plain Dialog stays separate from the AlertDialog-based DIALOG entry.
        assertEquals("android.app.Dialog", LabComponent.PLAIN_DIALOG.platformSource)
        assertEquals("android.app.AlertDialog", LabComponent.DIALOG.platformSource)
    }

    @Test
    fun tabHostUsesTheDeprecatedFrameworkSupplierWithThreeTabs() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        platform.forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.TAB_HOST, 1))
            assertNull(family.unsupportedReason(LabComponent.TAB_HOST, 36))
            assertEquals("android.widget.TabHost", family.source(LabComponent.TAB_HOST))
        }
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            assertNotNull(family.unsupportedReason(LabComponent.TAB_HOST, 36))
            assertEquals("Not provided", family.source(LabComponent.TAB_HOST))
        }
        assertEquals(1, LabComponent.TAB_HOST.minimumApi)
        assertEquals(30, LabComponent.TAB_HOST.deprecatedApi)
        assertEquals(ComponentCategory.LEGACY, LabComponent.TAB_HOST.category)
        assertEquals(3, LabComponent.TAB_HOST.tabCount)
        assertEquals(0, LabComponent.TAB_HOST.initialValue)
        assertTrue(LabComponent.TAB_HOST.matchesSearch("android.widget.TabHost"))
        assertTrue(LabComponent.TAB_HOST.matchesSearch("tab"))
        assertFalse(LabComponent.TAB_HOST.matchesSearch("androidx.compose.material3.Tab"))
    }

    @Test
    fun legacyContainersKeepPlatformOnlySourcesAndDeprecationLevels() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        val componentToSource =
            mapOf(
                LabComponent.GALLERY to "android.widget.Gallery",
                LabComponent.SLIDING_DRAWER to "android.widget.SlidingDrawer",
                LabComponent.TWO_LINE_LIST_ITEM to "android.widget.TwoLineListItem",
            )
        componentToSource.forEach { (component, source) ->
            platform.forEach { family ->
                assertNull(family.unsupportedReason(component, component.minimumApi))
                assertNull(family.unsupportedReason(component, 36))
                assertEquals(source, family.source(component))
            }
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                assertNotNull(family.unsupportedReason(component, 36))
                assertEquals("Not provided", family.source(component))
            }
            assertTrue(component.isLegacyContainer)
            assertEquals(ComponentCategory.LEGACY, component.category)
            assertFalse(component.matchesSearch("androidx.compose.material3.Drawer"))
        }
        assertEquals(1, LabComponent.GALLERY.minimumApi)
        assertEquals(16, LabComponent.GALLERY.deprecatedApi)
        assertEquals(6, LabComponent.GALLERY.galleryItemCount)
        assertEquals(3, LabComponent.SLIDING_DRAWER.minimumApi)
        assertEquals(17, LabComponent.SLIDING_DRAWER.deprecatedApi)
        assertEquals(1, LabComponent.TWO_LINE_LIST_ITEM.minimumApi)
        assertEquals(17, LabComponent.TWO_LINE_LIST_ITEM.deprecatedApi)
    }

    @Test
    fun popupWindowsKeepPlatformOnlySourcesAndAnchoredState() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        val componentToSource =
            mapOf(
                LabComponent.POPUP_WINDOW to "android.widget.PopupWindow",
                LabComponent.LIST_POPUP_WINDOW to "android.widget.ListPopupWindow",
            )
        componentToSource.forEach { (component, source) ->
            platform.forEach { family ->
                assertNull(family.unsupportedReason(component, component.minimumApi))
                assertNull(family.unsupportedReason(component, 36))
                assertEquals(source, family.source(component))
            }
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                assertNotNull(family.unsupportedReason(component, 36))
                assertEquals("Not provided", family.source(component))
            }
            assertTrue(component.isPopupWindow)
            assertTrue(component.matchesSearch(source))
            assertFalse(component.matchesSearch("androidx.compose.material3.PopupWindow"))
        }
        assertEquals(1, LabComponent.POPUP_WINDOW.minimumApi)
        assertEquals(11, LabComponent.LIST_POPUP_WINDOW.minimumApi)
        // The two popup entries are distinct from the PopupMenu component.
        assertFalse(LabComponent.POPUP_WINDOW.isTransientWindow)
        assertFalse(LabComponent.POPUP_MENU.matchesSearch("android.widget.ListPopupWindow"))
    }

    @Test
    fun menuHostsKeepPlatformOnlyApi21Sources() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        val componentToSource =
            mapOf(
                LabComponent.TOOLBAR to "android.widget.Toolbar",
                LabComponent.ACTION_MENU_VIEW to "android.widget.ActionMenuView",
            )
        componentToSource.forEach { (component, source) ->
            platform.forEach { family ->
                assertNotNull(family.unsupportedReason(component, 20))
                assertNull(family.unsupportedReason(component, 21))
                assertNull(family.unsupportedReason(component, 36))
                assertEquals(source, family.source(component))
            }
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                assertNotNull(family.unsupportedReason(component, 36))
                assertEquals("Not provided", family.source(component))
            }
            assertTrue(component.isMenuHost)
            assertEquals(21, component.minimumApi)
            assertEquals(ComponentCategory.NAVIGATION, component.category)
            assertFalse(component.matchesSearch("androidx.appcompat.widget.Toolbar"))
        }
    }

    @Test
    fun contentSurfacesKeepPlatformOnlySourcesAndMinimumApis() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        val componentToSource =
            mapOf(
                LabComponent.WEB_VIEW to ("android.webkit.WebView" to 1),
                LabComponent.QUICK_CONTACT_BADGE to ("android.widget.QuickContactBadge" to 5),
            )
        componentToSource.forEach { (component, metadata) ->
            platform.forEach { family ->
                if (metadata.second > 1) {
                    assertNotNull(family.unsupportedReason(component, metadata.second - 1))
                }
                assertNull(family.unsupportedReason(component, metadata.second))
                assertNull(family.unsupportedReason(component, 36))
                assertEquals(metadata.first, family.source(component))
            }
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                assertNotNull(family.unsupportedReason(component, 36))
                assertEquals("Not provided", family.source(component))
            }
            assertTrue(component.isContentSurface)
            assertFalse(component.isMenuHost)
            assertEquals(metadata.second, component.minimumApi)
            assertEquals(ComponentCategory.CONTENT, component.category)
            assertTrue(component.matchesSearch(metadata.first))
        }
    }

    @Test
    fun dialerFilterKeepsItsDeprecatedApi1Identity() {
        val component = LabComponent.DIALER_FILTER
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNull(family.unsupportedReason(component, 1))
            assertNull(family.unsupportedReason(component, 36))
            assertEquals("android.widget.DialerFilter", family.source(component))
        }
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            assertNotNull(family.unsupportedReason(component, 36))
            assertEquals("Not provided", family.source(component))
        }
        assertEquals(1, component.minimumApi)
        assertEquals(26, component.deprecatedApi)
        assertEquals(ComponentCategory.LEGACY, component.category)
        assertTrue(component.matchesSearch("android.widget.DialerFilter"))
        assertFalse(component.matchesSearch("androidx.appcompat.widget.DialerFilter"))
    }

    @Test
    fun mediaWidgetsKeepApi1PlatformSources() {
        val componentToSource =
            mapOf(
                LabComponent.VIDEO_VIEW to "android.widget.VideoView",
                LabComponent.MEDIA_CONTROLLER to "android.widget.MediaController",
            )
        componentToSource.forEach { (component, source) ->
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family
                ->
                assertNull(family.unsupportedReason(component, 1))
                assertNull(family.unsupportedReason(component, 36))
                assertEquals(source, family.source(component))
            }
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                assertNotNull(family.unsupportedReason(component, 36))
                assertEquals("Not provided", family.source(component))
            }
            assertTrue(component.isMediaWidget)
            assertEquals(1, component.minimumApi)
            assertEquals(ComponentCategory.MEDIA, component.category)
            assertNull(component.deprecatedApi)
            assertTrue(component.matchesSearch(source))
        }
    }

    @Test
    fun shareActionProviderKeepsItsApi14PlatformSource() {
        val component = LabComponent.SHARE_ACTION_PROVIDER
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNotNull(family.unsupportedReason(component, 13))
            assertNull(family.unsupportedReason(component, 14))
            assertNull(family.unsupportedReason(component, 36))
            assertEquals("android.widget.ShareActionProvider", family.source(component))
        }
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            assertNotNull(family.unsupportedReason(component, 36))
            assertEquals("Not provided", family.source(component))
        }
        assertEquals(14, component.minimumApi)
        assertNull(component.deprecatedApi)
        assertEquals(ComponentCategory.ACTION, component.category)
        assertTrue(component.matchesSearch("android.widget.ShareActionProvider"))
        assertFalse(component.matchesSearch("androidx.appcompat.widget.ShareActionProvider"))
    }

    @Test
    fun edgeEffectKeepsItsApi14PlatformSource() {
        val component = LabComponent.EDGE_EFFECT
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNotNull(family.unsupportedReason(component, 13))
            assertNull(family.unsupportedReason(component, 14))
            assertNull(family.unsupportedReason(component, 36))
            assertEquals("android.widget.EdgeEffect", family.source(component))
        }
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            assertNotNull(family.unsupportedReason(component, 36))
            assertEquals("Not provided", family.source(component))
        }
        assertTrue(component.isEdgeEffect)
        assertEquals(14, component.minimumApi)
        assertEquals(ComponentCategory.CONTENT, component.category)
        assertTrue(component.matchesSearch("android.widget.EdgeEffect"))
    }

    @Test
    fun navigationSuiteUsesBothComposeLibraries() {
        val expected =
            mapOf(
                LabComponent.NAVIGATION_BAR to
                    ("androidx.compose.material.BottomNavigation" to
                        "androidx.compose.material3.NavigationBar"),
                LabComponent.NAVIGATION_RAIL to
                    ("androidx.compose.material.NavigationRail" to
                        "androidx.compose.material3.NavigationRail"),
            )
        expected.forEach { (component, sources) ->
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family
                ->
                assertNotNull(family.unsupportedReason(component, 36))
                assertEquals("Not provided", family.source(component))
            }
            assertNull(DesignFamily.MATERIAL2.unsupportedReason(component, 36))
            assertNull(DesignFamily.MATERIAL3.unsupportedReason(component, 36))
            assertEquals(sources.first, DesignFamily.MATERIAL2.source(component))
            assertEquals(sources.second, DesignFamily.MATERIAL3.source(component))
            assertTrue(component.isNavigationSuite)
            assertEquals(3, component.navigationItemCount)
            assertEquals(ComponentCategory.NAVIGATION, component.category)
            assertTrue(component.matchesSearch(component.label))
        }
    }

    @Test
    fun tabRowsUseTheRealRowsInBothComposeLibraries() {
        val expected =
            mapOf(
                LabComponent.TAB_ROW to ("TabRow" to 3),
                LabComponent.SCROLLABLE_TAB_ROW to ("ScrollableTabRow" to 8),
            )
        expected.forEach { (component, spec) ->
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family
                ->
                assertNotNull(family.unsupportedReason(component, 36))
                assertEquals("Not provided", family.source(component))
            }
            assertNull(DesignFamily.MATERIAL2.unsupportedReason(component, 36))
            assertNull(DesignFamily.MATERIAL3.unsupportedReason(component, 36))
            assertEquals(
                "androidx.compose.material.${spec.first}",
                DesignFamily.MATERIAL2.source(component),
            )
            assertEquals(
                "androidx.compose.material3.${spec.first}",
                DesignFamily.MATERIAL3.source(component),
            )
            assertTrue(component.isTabRow)
            assertEquals(spec.second, component.tabCount)
            assertEquals(ComponentCategory.NAVIGATION, component.category)
        }
    }

    @Test
    fun clockSearchDoesNotInventLibrarySources() {
        listOf(
                LabComponent.TEXT_CLOCK,
                LabComponent.ANALOG_CLOCK,
                LabComponent.DIGITAL_CLOCK,
                LabComponent.CHRONOMETER,
            )
            .forEach { component ->
                assertTrue(component.matchesSearch(component.platformSource!!))
                assertFalse(component.matchesSearch("androidx.compose.material.Clock"))
                assertFalse(component.matchesSearch("androidx.compose.material3.Clock"))
            }
        assertFalse(LabComponent.TEXT_CLOCK.matchesSearch("android.widget.Chronometer"))
        assertFalse(LabComponent.CHRONOMETER.matchesSearch("android.widget.TextClock"))
    }

    @Test
    fun textSamplesUseActualSuppliersAndKeepCheckedTextFrameworkOnly() {
        DesignFamily.entries.forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.TEXT, 24))
            assertEquals(
                when (family) {
                    DesignFamily.MATERIAL2 -> "androidx.compose.material.Text"
                    DesignFamily.MATERIAL3 -> "androidx.compose.material3.Text"
                    else -> "android.widget.TextView"
                },
                family.source(LabComponent.TEXT),
            )
            if (family.platform != null) {
                assertNull(family.unsupportedReason(LabComponent.CHECKED_TEXT_VIEW, 24))
                assertEquals(
                    "android.widget.CheckedTextView",
                    family.source(LabComponent.CHECKED_TEXT_VIEW),
                )
            } else {
                assertNotNull(family.unsupportedReason(LabComponent.CHECKED_TEXT_VIEW, 36))
                assertEquals("Not provided", family.source(LabComponent.CHECKED_TEXT_VIEW))
            }
        }
        assertEquals(1, LabComponent.TEXT.minimumApi)
        assertEquals(1, LabComponent.CHECKED_TEXT_VIEW.minimumApi)
        assertEquals(0, LabComponent.CHECKED_TEXT_VIEW.initialValue)
        assertEquals(ComponentCategory.CONTENT, LabComponent.TEXT.category)
        assertEquals(ComponentCategory.CONTENT, LabComponent.CHECKED_TEXT_VIEW.category)
    }

    @Test
    fun textSearchFindsRealClassNamesWithoutInventingCheckedTextLibrarySources() {
        listOf(
                "android.widget.TextView",
                "androidx.compose.material.Text",
                "androidx.compose.material3.Text",
            )
            .forEach { source -> assertTrue(LabComponent.TEXT.matchesSearch(source)) }
        assertTrue(LabComponent.CHECKED_TEXT_VIEW.matchesSearch("android.widget.CheckedTextView"))
        assertFalse(
            LabComponent.CHECKED_TEXT_VIEW.matchesSearch(
                "androidx.compose.material.CheckedTextView"
            )
        )
        assertFalse(
            LabComponent.CHECKED_TEXT_VIEW.matchesSearch(
                "androidx.compose.material3.CheckedTextView"
            )
        )
        assertFalse(LabComponent.TEXT.matchesSearch("android.widget.CheckedTextView"))
    }

    @Test
    fun inlineDatePickersUseOnlyTheirGenuineSuppliers() {
        val platformFamilies =
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        platformFamilies.forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.DATE_PICKER, 24))
            assertNull(family.unsupportedReason(LabComponent.CALENDAR_VIEW, 24))
            assertNotNull(family.unsupportedReason(LabComponent.DATE_RANGE_PICKER, 36))
            // API 10 and 11 check metadata below the application's API 24 execution minimum.
            assertNotNull(family.unsupportedReason(LabComponent.CALENDAR_VIEW, 10))
            assertNull(family.unsupportedReason(LabComponent.CALENDAR_VIEW, 11))
        }
        listOf(LabComponent.DATE_PICKER, LabComponent.CALENDAR_VIEW, LabComponent.DATE_RANGE_PICKER)
            .forEach { component ->
                assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(component, 36))
            }
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.DATE_PICKER, 24))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.DATE_RANGE_PICKER, 24))
        assertNotNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.CALENDAR_VIEW, 36))
    }

    @Test
    fun popupMenuUsesFrameworkApi11AndBothPinnedLibraryFamilies() {
        // API 10 and 11 check metadata; the application itself runs from API 24.
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNotNull(family.unsupportedReason(LabComponent.POPUP_MENU, 10))
            assertNull(family.unsupportedReason(LabComponent.POPUP_MENU, 11))
        }
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.POPUP_MENU, 24))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.POPUP_MENU, 24))
    }

    @Test
    fun containersUseOnlyLibrariesThatSupplyTheirActualApi() {
        val shared = listOf(LabComponent.CARD, LabComponent.SURFACE)
        val material3Only = listOf(LabComponent.ELEVATED_CARD, LabComponent.OUTLINED_CARD)
        (shared + material3Only).forEach { component ->
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family
                ->
                assertNotNull(family.unsupportedReason(component, 36))
            }
            assertNull(DesignFamily.MATERIAL3.unsupportedReason(component, 24))
        }
        shared.forEach { component ->
            assertNull(DesignFamily.MATERIAL2.unsupportedReason(component, 24))
        }
        material3Only.forEach { component ->
            assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(component, 36))
        }
    }

    @Test
    fun timePickerDialogHasGenuineFrameworkAndMaterial3SuppliersOnly() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.TIME_PICKER_DIALOG, 24))
        }
        assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.TIME_PICKER_DIALOG, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.TIME_PICKER_DIALOG, 24))
    }

    @Test
    fun datePickerDialogHasGenuineFrameworkAndMaterial3SuppliersOnly() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.DATE_PICKER_DIALOG, 24))
        }
        assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.DATE_PICKER_DIALOG, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.DATE_PICKER_DIALOG, 24))
    }

    @Test
    fun frameworkAvailabilityUsesTheRunningOsRatherThanTheThemeOrigin() {
        assertNotNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.SWITCH, 13))
        assertNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.SWITCH, 14))
        assertNotNull(DesignFamily.HOLO.unsupportedReason(LabComponent.NUMBER_PICKER, 10))
        assertNull(DesignFamily.HOLO.unsupportedReason(LabComponent.NUMBER_PICKER, 11))
    }

    @Test
    fun frameworkOnlyWidgetsAreNotSubstitutedWithComposeRecreations() {
        listOf(
                LabComponent.TOGGLE_BUTTON,
                LabComponent.IMAGE_BUTTON,
                LabComponent.RATING,
                LabComponent.NUMBER_PICKER,
            )
            .forEach { component ->
                assertNull(DesignFamily.MATERIAL.unsupportedReason(component, 36))
                assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(component, 36))
                assertNotNull(DesignFamily.MATERIAL3.unsupportedReason(component, 36))
            }
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.BUTTON, 36))
        assertNotNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.RANGE_SLIDER, 36))
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.RANGE_SLIDER, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.RANGE_SLIDER, 36))
    }

    @Test
    fun newerLibraryVariantsAreUnavailableInOlderFamilies() {
        assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.TONAL_BUTTON, 36))
        assertNotNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.TONAL_BUTTON, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.TONAL_BUTTON, 36))
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.OUTLINED_BUTTON, 36))
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.CHIP, 36))
        assertNotNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.CHIP, 36))
        assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.MULTI_SEGMENTED, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.MULTI_SEGMENTED, 36))
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.SECURE_TEXT_FIELD, 36))
        assertNull(
            DesignFamily.MATERIAL3.unsupportedReason(LabComponent.OUTLINED_SECURE_TEXT_FIELD, 36)
        )
        assertNotNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.SPINNER, 36))
        assertNotNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.SEARCH_VIEW, 10))
        assertNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.SEARCH_VIEW, 11))
    }
}
