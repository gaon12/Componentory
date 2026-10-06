package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class BasicAlertDialogsTest {
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
    fun basicDialogOpensConfirmsAndReportsOutcome() {
        configure()
        compose
            .onNodeWithTag("source_LEFT")
            .assertTextEquals("androidx.compose.material3.BasicAlertDialog")
        status("LEFT", "Last action: Not opened")

        compose.onNodeWithTag("library_LEFT").performScrollTo().performTouchInput { click() }
        compose.waitForIdle()
        compose.onNodeWithTag("library_LEFT_dialog").assertIsDisplayed()
        status("LEFT", "Last action: Opened")

        compose.onNodeWithTag("dialog_confirm").assertIsDisplayed().performTouchInput { click() }
        compose.waitForIdle()
        compose.onNodeWithTag("library_LEFT_dialog").assertDoesNotExist()
        status("LEFT", "Last action: Confirmed")
    }

    @Test
    fun basicDialogDismissesOutsideAndBlocksCopy() {
        configure()
        compose.onNodeWithTag("library_LEFT").performScrollTo().performTouchInput { click() }
        compose.waitForIdle()
        compose.onNodeWithTag("dialog_cancel").assertIsDisplayed().performTouchInput { click() }
        compose.waitForIdle()
        status("LEFT", "Last action: Cancelled")

        blockedCopy("LEFT_TO_RIGHT", "This sample has no inputs to copy.")
    }

    @Test
    fun material2CellExplainsTheMissingSource() {
        configure(right = DesignFamily.MATERIAL2)
        compose.onNodeWithTag("source_RIGHT").assertTextEquals("Not provided")
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 2 library does not provide Basic alert dialog.",
                substring = true,
            )
    }

    private fun configure(right: DesignFamily = DesignFamily.MATERIAL3) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose
            .onNodeWithTag("picker_search")
            .performTextReplacement(LabComponent.BASIC_ALERT_DIALOG.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_BASIC_ALERT_DIALOG"))
        compose.onNodeWithTag("component_BASIC_ALERT_DIALOG").performClick()
        chooseFamily("LEFT", DesignFamily.MATERIAL3)
        chooseFamily("RIGHT", right)
        resetSamples()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun status(panel: String, expected: String) {
        compose.onNodeWithTag("status_$panel").performScrollTo().assertTextEquals(expected)
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

    private fun resetSamples() = touchTag("reset")

    private fun touchTag(tag: String) {
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performTouchInput {
            click()
        }
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
        compose.onNodeWithTag("nav_compare").performClick()
    }
}
