package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ZoomButton
import android.widget.ZoomControls
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

// Zoom widgets share a 0..10 level counter that copies between panels.
@RunWith(AndroidJUnit4::class)
@Suppress("DEPRECATION")
class ZoomControlsTest {
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
    fun zoomWidgetsStepTheLevelInsideEveryPlatformTheme() {
        configure(LabComponent.ZOOM_CONTROLS, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
        assertNativeIdentity("LEFT", LabComponent.ZOOM_CONTROLS, DesignFamily.CLASSIC)
        status("LEFT", "Zoom level 5 of 10")
        // The framework layout orders zoom out first, then zoom in.
        compose.runOnIdle {
            val controls = widget("LEFT") as ZoomControls
            assertEquals(2, controls.childCount)
            controls.getChildAt(1).performClick()
        }
        compose.waitForIdle()
        status("LEFT", "Zoom level 6 of 10")
        compose.runOnIdle {
            val controls = widget("LEFT") as ZoomControls
            controls.getChildAt(0).performClick()
            controls.getChildAt(0).performClick()
        }
        compose.waitForIdle()
        status("LEFT", "Zoom level 4 of 10")

        configure(LabComponent.ZOOM_BUTTON, DesignFamily.HOLO, DesignFamily.MATERIAL3)
        compose.runOnIdle { (widget("LEFT") as ZoomButton).performClick() }
        compose.waitForIdle()
        status("LEFT", "Zoom level 6 of 10")

        configure(
            LabComponent.ZOOM_BUTTONS_CONTROLLER,
            DesignFamily.MATERIAL,
            DesignFamily.MATERIAL3,
        )
        compose.waitUntil(10_000) {
            (compose.activity.findViewById<View?>(R.id.sample_left) as? FrameLayout)?.let {
                it.childCount >= 2
            } == true
        }
        compose.runOnIdle {
            val host = widget("LEFT") as FrameLayout
            // The controller inserts its container next to the owner view, and
            // the container wraps a real ZoomControls (out button first).
            val controls = (host.getChildAt(1) as ViewGroup).getChildAt(0) as ZoomControls
            controls.getChildAt(1).performClick()
        }
        compose.waitForIdle()
        status("LEFT", "Zoom level 6 of 10")

        mapOf(
                LabComponent.ZOOM_CONTROLS to 29,
                LabComponent.ZOOM_BUTTON to 26,
                LabComponent.ZOOM_BUTTONS_CONTROLLER to 26,
            )
            .forEach { (component, api) ->
                configure(component, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
                compose
                    .onNodeWithTag("clock_note_LEFT")
                    .performScrollTo()
                    .assertTextContains("Deprecated since API $api", substring = true)
                compose.onNodeWithTag("zoom_note_LEFT").performScrollTo().assertIsDisplayed()
            }
    }

    @Test
    fun zoomLevelCopiesBetweenPanelsAndBlocksLibraryTargets() {
        configure(LabComponent.ZOOM_CONTROLS, DesignFamily.CLASSIC, DesignFamily.HOLO)
        compose.runOnIdle { (widget("LEFT") as ZoomControls).getChildAt(1).performClick() }
        compose.waitForIdle()
        copyInputs("LEFT_TO_RIGHT")
        status("RIGHT", "Zoom level 6 of 10")
        status("LEFT", "Zoom level 6 of 10")

        chooseFamily("RIGHT", DesignFamily.MATERIAL2)
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 2 library does not provide Zoom controls.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    @Test
    fun zoomWidgetsClampAndFollowTheEnabledSwitch() {
        configure(LabComponent.ZOOM_BUTTON, DesignFamily.MATERIAL, DesignFamily.CLASSIC)
        repeat(8) { compose.runOnIdle { (widget("LEFT") as ZoomButton).performClick() } }
        compose.waitForIdle()
        status("LEFT", "Zoom level 10 of 10")
        compose.runOnIdle { assertFalse(widget("LEFT").isEnabled) }
        setEnabled(false)
        compose.runOnIdle { assertFalse(widget("LEFT").isEnabled) }
        setEnabled(true)
        resetSamples()
        compose.waitForIdle()
        status("LEFT", "Zoom level 5 of 10")
        compose.runOnIdle { assertTrue(widget("LEFT").isEnabled) }
    }

    @Test
    fun fiveLanguagesLocalizeZoomLabelsAndLevel() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.ZOOM_CONTROLS, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", String.format(text.level, 5, 10))
            compose.runOnIdle { (widget("LEFT") as ZoomControls).getChildAt(1).performClick() }
            compose.waitForIdle()
            status("LEFT", String.format(text.level, 6, 10))
            compose.onNodeWithTag("zoom_note_LEFT").performScrollTo().assertIsDisplayed()
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

    private fun assertNativeIdentity(panel: String, component: LabComponent, family: DesignFamily) {
        compose
            .onNodeWithTag("source_$panel")
            .performScrollTo()
            .assertTextEquals(component.platformSource!!)
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

    private data class ZoomNames(val language: AppLanguage, val level: String)

    private val names =
        listOf(
            ZoomNames(AppLanguage.ENGLISH, "Zoom level %d of %d"),
            ZoomNames(AppLanguage.KOREAN, "줌 레벨 %d/%d"),
            ZoomNames(AppLanguage.JAPANESE, "ズームレベル %d/%d"),
            ZoomNames(AppLanguage.SIMPLIFIED_CHINESE, "缩放级别 %d/%d"),
            ZoomNames(AppLanguage.TRADITIONAL_CHINESE, "縮放層級 %d/%d"),
        )
}
