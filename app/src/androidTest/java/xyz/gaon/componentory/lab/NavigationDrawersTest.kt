package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class NavigationDrawersTest {
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
    fun realDrawersOpenThroughTheirDrawerState() {
        configure(
            LabComponent.MODAL_NAVIGATION_DRAWER,
            DesignFamily.MATERIAL2,
            DesignFamily.MATERIAL3,
        )
        expandDetails("LEFT")
        compose
            .onNodeWithTag("source_LEFT")
            .assertTextEquals("androidx.compose.material.ModalDrawer")
        expandDetails("RIGHT")
        compose
            .onNodeWithTag("source_RIGHT")
            .assertTextEquals("androidx.compose.material3.ModalNavigationDrawer")
        status("LEFT", "Drawer closed")
        status("RIGHT", "Drawer closed")

        openDrawer("LEFT")
        status("LEFT", "Drawer open")
        compose.onNodeWithTag("library_LEFT_item_1").assertIsDisplayed()
    }

    @Test
    fun drawerItemClosesTheSheetAndOpenStateCopies() {
        configure(
            LabComponent.MODAL_NAVIGATION_DRAWER,
            DesignFamily.MATERIAL2,
            DesignFamily.MATERIAL3,
        )
        openDrawer("LEFT")
        status("LEFT", "Drawer open")
        // A real item pick closes the drawer through its DrawerState.
        compose.onNodeWithTag("library_LEFT_item_2").performTouchInput { click() }
        compose.waitForIdle()
        status("LEFT", "Drawer closed")

        openDrawer("RIGHT")
        status("RIGHT", "Drawer open")
        copyInputs("RIGHT_TO_LEFT")
        status("LEFT", "Drawer open")

        recreateActivity()
        status("LEFT", "Drawer open")
        status("RIGHT", "Drawer open")
    }

    @Test
    fun permanentDrawerNeedsNoOpenState() {
        configure(
            LabComponent.PERMANENT_NAVIGATION_DRAWER,
            DesignFamily.MATERIAL3,
            DesignFamily.MATERIAL2,
        )
        // The sheet is always visible without any open action.
        compose.onNodeWithTag("library_LEFT_item_1").assertIsDisplayed()
        status("LEFT", "Preview: Permanent navigation drawer")
        compose.onNodeWithTag("unsupported_RIGHT").performScrollTo().assertIsDisplayed()
        compose
            .onNode(
                hasText("The Material 2 library does not provide Permanent navigation drawer.") and
                    hasAnyAncestor(hasTestTag("unsupported_RIGHT"))
            )
            .assertExists()
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        blockedCopy("LEFT_TO_RIGHT", "This sample has no inputs to copy.")
    }

    @Test
    fun disabledDrawersIgnoreOpeningAndItems() {
        configure(LabComponent.BOTTOM_DRAWER, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        compose.onNodeWithTag("unsupported_RIGHT").performScrollTo().assertIsDisplayed()
        setEnabled(false)
        compose.onNodeWithTag("library_LEFT_open").assertIsNotEnabled()
        openDrawer("LEFT")
        status("LEFT", "Drawer closed")
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

    private fun openDrawer(panel: String) {
        compose.onNodeWithTag("library_${panel}_open").performScrollTo().performTouchInput {
            click()
        }
        compose.waitForIdle()
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

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
