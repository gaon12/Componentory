package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class AppBarScopesTest {
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
    fun bothScopesRenderRealItemsAndOverflow() {
        listOf(LabComponent.APP_BAR_ROW, LabComponent.APP_BAR_COLUMN).forEach { component ->
            configure(component, DesignFamily.MATERIAL3, DesignFamily.MATERIAL3)
            compose
                .onNodeWithTag("source_LEFT")
                .assertTextEquals("androidx.compose.material3.${component.sourceName}")
            status("LEFT", "Clicks: 0")
            // Scope items accept no modifier; find them by their click action.
            compose
                .onAllNodes(
                    hasAnyAncestor(hasTestTag("library_LEFT")) and hasClickAction(),
                    useUnmergedTree = true,
                )
                .fetchSemanticsNodes()
                .also { assert(it.isNotEmpty()) }
            compose
                .onNodeWithTag("library_LEFT_overflow", useUnmergedTree = true)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    fun itemAndOverflowClicksCountAndCopyAcrossPanels() {
        configure(LabComponent.APP_BAR_ROW, DesignFamily.MATERIAL3, DesignFamily.MATERIAL3)
        compose
            .onNode(
                hasAnyAncestor(hasTestTag("library_LEFT")) and hasClickAction(),
                useUnmergedTree = true,
            )
            .performClick()
        status("LEFT", "Clicks: 1")
        // Three items with maxItemCount 2 push the last one into the
        // overflow menu; the indicator opens it and the item still clicks.
        compose.onNodeWithTag("library_LEFT_overflow").performClick()
        compose.onNodeWithText("Item 3").assertIsDisplayed().performClick()
        status("LEFT", "Clicks: 2")
        copyInputs("LEFT_TO_RIGHT")
        status("RIGHT", "Clicks: 2")
        recreateActivity()
        status("LEFT", "Clicks: 2")
        status("RIGHT", "Clicks: 2")
    }

    @Test
    fun disabledScopeIgnoresClicksAndPlatformExplainsTheGap() {
        configure(LabComponent.APP_BAR_COLUMN, DesignFamily.MATERIAL3, DesignFamily.CLASSIC)
        setEnabled(false)
        compose.onNodeWithTag("library_LEFT_overflow", useUnmergedTree = true).assertIsNotEnabled()
        status("LEFT", "Clicks: 0")
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Android platform does not provide a dedicated App bar column component.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    private val LabComponent.sourceName: String
        get() =
            when (this) {
                LabComponent.APP_BAR_ROW -> "AppBarRow"
                else -> "AppBarColumn"
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

    private fun status(panel: String, expected: String) {
        compose.onNodeWithTag("status_$panel").performScrollTo().assertTextEquals(expected)
    }

    private fun copyInputs(direction: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsDisplayed()
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
        compose.onNodeWithTag("nav_settings").performClick()
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
}
