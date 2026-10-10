package xyz.gaon.componentory.icons

import android.content.pm.ActivityInfo
import android.view.WindowManager
import android.widget.ImageButton
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

@RunWith(AndroidJUnit4::class)
class IconBrowserTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun catalogContainsEveryPinnedIconAndCurrentPublicFrameworkDrawable() {
        val entries = IconCatalog.material(compose.activity)
        assertEquals(11_385, entries.size)
        assertEquals(entries.size, entries.map { it.id }.distinct().size)
        IconStyle.entries.forEach { style ->
            assertEquals(2_277, entries.count { it.style == style })
            listOf(false, true).forEach { mirrored ->
                assertNotNull(
                    entries.first { it.style == style && it.autoMirrored == mirrored }.vector()
                )
            }
        }
        // Verify all indexed public getters without retaining all vectors in memory.
        entries.forEach { icon ->
            assertEquals(
                1,
                Class.forName(icon.id).methods.count { it.returnType == ImageVector::class.java },
            )
        }
        val framework = IconCatalog.platform(compose.activity)
        assertEquals(
            android.R.drawable::class.java.fields.count { it.type == Int::class.javaPrimitiveType },
            framework.size,
        )
        framework
            .filter { it.available }
            .forEach { assertNotNull(compose.activity.getDrawable(requireNotNull(it.drawableId))) }
    }

    @Test
    fun compactPickerKeepsStyleAndMirroringFiltersReachable() {
        val originalOrientation = compose.activity.requestedOrientation
        try {
            compose.runOnUiThread {
                compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            compose.waitUntil(timeoutMillis = 10_000) {
                compose.activity.resources.configuration.orientation ==
                    android.content.res.Configuration.ORIENTATION_LANDSCAPE
            }
            compare(LabComponent.ICON, DesignFamily.MATERIAL2)
            compose.onNodeWithTag("icon_picker_LEFT").performScrollTo().performClick()
            compose.onNodeWithTag("icon_search").performTextReplacement("arrow_back")
            // The filters remain reachable while the keyboard reduces the grid's height.
            selectFilter("icon_style_OUTLINED")
            selectFilter("icon_mirrored")
            compose.onNodeWithTag("icon_search").performImeAction()
            val id = "androidx.compose.material.icons.automirrored.outlined.ArrowBackKt"
            compose.onNodeWithTag("icon_grid").performScrollToNode(hasTestTag("icon_entry_$id"))
            compose.onNodeWithTag("icon_entry_$id").performClick()
            compose.onNodeWithTag("library_LEFT").assertContentDescriptionEquals("ArrowBack")
            compose.onNodeWithTag("icon_picker_LEFT").performScrollTo().performClick()
            openCompactFilters()
            compose.onNodeWithTag("icon_style_OUTLINED").assertIsSelected()
            compose.onNodeWithTag("icon_mirrored").assertIsSelected().performClick()
            compose.onNodeWithTag("icon_reset").performClick()
            compose
                .onNodeWithTag("icon_count")
                .assertTextEquals(compose.activity.getString(R.string.icon_count, 11_385, 11_385))
            compose.onNodeWithTag("icon_close").performClick()
            compose.activityRule.scenario.recreate()
            compose.onNodeWithTag("library_LEFT").assertContentDescriptionEquals("ArrowBack")
        } finally {
            compose.runOnUiThread { compose.activity.requestedOrientation = originalOrientation }
        }
    }

    @Test
    fun searchStyleAndMirroringSelectRealIconsAndRestoreEachPanel() {
        compare(LabComponent.ICON, DesignFamily.MATERIAL2)
        compose.onNodeWithTag("icon_picker_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("icon_search").performTextReplacement("arrow_back")
        compose.onNodeWithTag("icon_search").performImeAction()
        selectFilter("icon_style_OUTLINED")
        selectFilter("icon_mirrored")
        val id = "androidx.compose.material.icons.automirrored.outlined.ArrowBackKt"
        compose.onNodeWithTag("icon_grid").performScrollToNode(hasTestTag("icon_entry_$id"))
        compose.onNodeWithTag("icon_entry_$id").performClick()
        compose.onNodeWithTag("library_LEFT").assertContentDescriptionEquals("ArrowBack")
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextEquals(compose.activity.getString(R.string.icon_status, "ArrowBack"))
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("library_LEFT").assertContentDescriptionEquals("ArrowBack")
        compare(LabComponent.ICON_BUTTON, DesignFamily.MATERIAL3)
        compose.onNodeWithTag("family_RIGHT").performScrollTo().performClick()
        compose.onNodeWithTag("family_RIGHT_MATERIAL2").performClick()
        compose.onNodeWithTag("icon_picker_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("icon_search").performTextReplacement("this icon does not exist")
        compose.onNodeWithTag("icon_empty").assertExists()
        compose.onNodeWithTag("icon_search").performTextReplacement("_360")
        val numbered = "androidx.compose.material.icons.filled._360Kt"
        compose.onNodeWithTag("icon_grid").performScrollToNode(hasTestTag("icon_entry_$numbered"))
        compose.onNodeWithTag("icon_entry_$numbered").performClick()
        compose.onNodeWithTag("icon_dialog").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Clicks: 0")
    }

    @Test
    fun imageButtonUsesTheChosenFrameworkDrawable() {
        compare(LabComponent.IMAGE_BUTTON, DesignFamily.CLASSIC)
        compose.onNodeWithTag("icon_picker_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("icon_search").performTextReplacement("ic_menu_camera")
        compose.onNodeWithTag("icon_entry_android:ic_menu_camera").performClick()
        compose.runOnIdle {
            val button = compose.activity.findViewById<ImageButton>(R.id.sample_left)
            assertEquals("ic_menu_camera", button.contentDescription.toString())
        }
        compose.onNodeWithTag("native_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
    }

    @Test
    fun pickerMarksTheSelectedIconAndResetsFilters() {
        compare(LabComponent.ICON, DesignFamily.MATERIAL2)
        compose.onNodeWithTag("icon_picker_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("icon_search").performTextReplacement("arrow_back")
        compose.onNodeWithTag("icon_search").performImeAction()
        selectFilter("icon_style_OUTLINED")
        selectFilter("icon_mirrored")
        val id = "androidx.compose.material.icons.automirrored.outlined.ArrowBackKt"
        compose.onNodeWithTag("icon_grid").performScrollToNode(hasTestTag("icon_entry_$id"))
        compose.onNodeWithTag("icon_entry_$id").performClick()
        compose.onNodeWithTag("icon_picker_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("icon_entry_$id").assertIsSelected()
        compose.onNodeWithTag("icon_reset").performClick()
        openCompactFilters()
        compose.onNodeWithTag("icon_style_ALL").assertIsSelected()
        compose.onNodeWithTag("icon_mirrored").assertIsNotSelected()
        if (compose.onAllNodesWithTag("icon_filters").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("icon_style_ALL").performClick()
        }
        compose
            .onNodeWithTag("icon_count")
            .assertTextEquals(compose.activity.getString(R.string.icon_count, 11_385, 11_385))
    }

    private fun openCompactFilters() {
        if (compose.onAllNodesWithTag("icon_filters").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("icon_filters").performClick()
        }
    }

    private fun selectFilter(tag: String) {
        val compact = compose.onAllNodesWithTag("icon_filters").fetchSemanticsNodes().isNotEmpty()
        if (compact) {
            compose.onNodeWithTag("icon_filters").performClick()
            compose.onNodeWithTag(tag).performScrollTo().performClick()
        } else {
            compose.onNodeWithTag(tag).performClick()
        }
    }

    private fun compare(component: LabComponent, family: DesignFamily) {
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose.onNodeWithTag("picker_search").performImeAction()
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
    }
}
