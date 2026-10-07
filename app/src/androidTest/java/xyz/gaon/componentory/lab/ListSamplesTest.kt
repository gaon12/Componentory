package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.ExpandableListView
import android.widget.GridView
import android.widget.ListView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences
import xyz.gaon.componentory.testing.openSettingsPage

// The lists own real adapter rows: item clicks toggle the single-choice
// checked state and group headers expand or collapse children.
@RunWith(AndroidJUnit4::class)
class ListSamplesTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareEnglishComparison() {
        keepScreenOn()
        changeLanguage(AppLanguage.ENGLISH)
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @After
    fun restoreEnglishBaseline() {
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
        keepScreenOn()
    }

    @Test
    fun adapterListsRenderRealRowsAndTrackCheckedAndExpandedState() {
        configure(LabComponent.LIST_VIEW, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
        assertNativeIdentity("LEFT", LabComponent.LIST_VIEW, DesignFamily.CLASSIC)
        compose.runOnIdle {
            val list = widget("LEFT") as ListView
            assertEquals(6, list.adapter.count)
            assertEquals(AbsListView.CHOICE_MODE_SINGLE, list.choiceMode)
            assertEquals(-1, list.checkedItemPosition)
        }
        status("LEFT", "No selection")
        clickItem("LEFT", 2)
        compose.runOnIdle { assertEquals(2, (widget("LEFT") as ListView).checkedItemPosition) }
        status("LEFT", "Selected: Item 3")

        configure(LabComponent.GRID_VIEW, DesignFamily.HOLO, DesignFamily.MATERIAL3)
        compose.runOnIdle {
            val grid = widget("LEFT") as GridView
            assertEquals(9, grid.adapter.count)
            assertEquals(3, grid.numColumns)
        }
        clickItem("LEFT", 4)
        compose.runOnIdle { assertEquals(4, (widget("LEFT") as GridView).checkedItemPosition) }
        status("LEFT", "Selected: Item 5")

        configure(LabComponent.EXPANDABLE_LIST_VIEW, DesignFamily.MATERIAL, DesignFamily.MATERIAL3)
        compose.runOnIdle {
            val list = widget("LEFT") as ExpandableListView
            assertEquals(3, list.expandableListAdapter.groupCount)
            assertTrue("The sample starts with group 1 open", list.isGroupExpanded(0))
        }
        status("LEFT", "Expanded: Group 1")
        compose.runOnIdle { (widget("LEFT") as ExpandableListView).expandGroup(2) }
        compose.waitForIdle()
        status("LEFT", "Expanded: Group 1, Group 3")
        compose.runOnIdle { (widget("LEFT") as ExpandableListView).collapseGroup(0) }
        compose.waitForIdle()
        status("LEFT", "Expanded: Group 3")
    }

    @Test
    fun listStateCopiesBetweenPanelsAndBlocksLibraryTargets() {
        configure(LabComponent.LIST_VIEW, DesignFamily.CLASSIC, DesignFamily.HOLO)
        clickItem("LEFT", 1)
        copyInputs("LEFT_TO_RIGHT")
        compose.runOnIdle { assertEquals(1, (widget("RIGHT") as ListView).checkedItemPosition) }
        status("RIGHT", "Selected: Item 2")
        status("LEFT", "Selected: Item 2")

        configure(LabComponent.EXPANDABLE_LIST_VIEW, DesignFamily.MATERIAL, DesignFamily.CLASSIC)
        compose.runOnIdle { (widget("LEFT") as ExpandableListView).expandGroup(1) }
        compose.waitForIdle()
        copyInputs("LEFT_TO_RIGHT")
        compose.runOnIdle {
            val list = widget("RIGHT") as ExpandableListView
            assertTrue(list.isGroupExpanded(0) && list.isGroupExpanded(1))
        }
        status("RIGHT", "Expanded: Group 1, Group 2")

        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 3 library does not provide Expandable list view.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    @Test
    fun adapterListsFollowTheEnabledSwitch() {
        configure(LabComponent.GRID_VIEW, DesignFamily.MATERIAL, DesignFamily.CLASSIC)
        setEnabled(false)
        compose.runOnIdle { assertFalse(widget("LEFT").isEnabled) }
        setEnabled(true)
        compose.runOnIdle { assertTrue(widget("LEFT").isEnabled) }
    }

    @Test
    fun fiveLanguagesLocalizeListLabelsAndSelection() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.LIST_VIEW, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", text.noSelection)
            clickItem("LEFT", 0)
            status("LEFT", String.format(text.selected, String.format(text.item, 1)))
        }
        changeLanguage(AppLanguage.ENGLISH)
    }

    private fun configure(component: LabComponent, left: DesignFamily, right: DesignFamily) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
        chooseFamily("LEFT", left)
        chooseFamily("RIGHT", right)
        setEnabled(true)
        resetSamples()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun widget(panel: String): View =
        compose.activity.findViewById(if (panel == "LEFT") R.id.sample_left else R.id.sample_right)

    private fun clickItem(panel: String, position: Int) {
        compose.runOnIdle {
            val list = widget(panel) as AbsListView
            list.performItemClick(list.getChildAt(position), position, position.toLong())
        }
        compose.waitForIdle()
    }

    private fun assertNativeIdentity(panel: String, component: LabComponent, family: DesignFamily) {
        expandDetails(panel)
        compose
            .onNodeWithTag("source_$panel")
            .performScrollTo()
            .assertTextEquals(component.platformSource!!)
        expandDetails(panel)
        compose
            .onNodeWithTag("implementation_$panel")
            .performScrollTo()
            .assertTextContains(
                "android:${requireNotNull(family.platform).themeName}",
                substring = true,
            )
            .assertTextContains(
                compose.activity.getString(R.string.widget_api, component.minimumApi),
                substring = true,
            )
        compose.runOnIdle {
            val view = widget(panel)
            assertEquals(ContextThemeWrapper::class.java, view.context.javaClass)
            val expected = TypedValue()
            val actual = TypedValue()
            val theme =
                ContextThemeWrapper(compose.activity, requireNotNull(family.platform).themeId).theme
            assertEquals(
                theme.resolveAttribute(android.R.attr.colorBackground, expected, true),
                view.context.theme.resolveAttribute(android.R.attr.colorBackground, actual, true),
            )
            assertEquals(expected.resourceId, actual.resourceId)
        }
    }

    private fun status(panel: String, expected: String) {
        compose.onNodeWithTag("status_$panel").performScrollTo().assertTextEquals(expected)
    }

    private fun copyInputs(direction: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertDoesNotExist()
        compose.onNodeWithTag("copy_setup_result").assertExists()
    }

    private fun blockedCopy(direction: String, reason: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_reason_$direction", useUnmergedTree = true)
            .assertTextEquals(reason)
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsNotEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertIsDisplayed()
        pressBack()
    }

    private fun setEnabled(enabled: Boolean) {
        val control = compose.onNodeWithTag("enabled").performScrollTo().assertIsDisplayed()
        if (
            (control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On) != enabled
        )
            control.performTouchInput { click() }
        if (enabled) control.assertIsOn() else control.assertIsOff()
    }

    private fun resetSamples() = touchTag("reset")

    private fun touchTag(tag: String) {
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performTouchInput {
            click()
        }
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.openSettingsPage("LANGUAGE")
        compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
        compose.waitUntil(10_000) { LanguagePreferences.read(compose.activity) == language }
        compose.waitForIdle()
        recreateActivity()
    }

    private fun recreateActivity() {
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        keepScreenOn()
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private data class ListNames(
        val language: AppLanguage,
        val noSelection: String,
        val selected: String,
        val item: String,
    )

    private val names =
        listOf(
            ListNames(AppLanguage.ENGLISH, "No selection", "Selected: %s", "Item %d"),
            ListNames(AppLanguage.KOREAN, "선택 없음", "선택: %s", "항목 %d"),
            ListNames(AppLanguage.JAPANESE, "選択なし", "選択: %s", "項目 %d"),
            ListNames(AppLanguage.SIMPLIFIED_CHINESE, "未选择", "已选择：%s", "项目 %d"),
            ListNames(AppLanguage.TRADITIONAL_CHINESE, "未選取", "已選取：%s", "項目 %d"),
        )

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
