package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
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
import androidx.compose.ui.test.swipe
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click as nativeClick
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
class LibraryComparisonTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val families = listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)

    @Before
    fun prepareLab() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.waitForIdle()
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("compare_screen").assertExists()
    }

    @Test
    fun buttonsUseSeparateLibraryIdentitiesAndIgnoreDisabledTouch() {
        families.forEach { family ->
            chooseFamily(family)
            onView(withId(R.id.sample_left)).check(doesNotExist())
            val packageName =
                if (family == DesignFamily.MATERIAL2) "androidx.compose.material"
                else "androidx.compose.material3"
            val artifact = if (family == DesignFamily.MATERIAL2) "material" else "material3"
            val version =
                if (family == DesignFamily.MATERIAL2) BuildConfig.MATERIAL2_VERSION
                else BuildConfig.MATERIAL3_VERSION
            val theme = if (family == DesignFamily.MATERIAL2) "" else " · MaterialTheme"
            expandDetails("LEFT")
            compose.onNodeWithTag("source_LEFT").assertTextEquals("$packageName.Button")
            compose
                .onNodeWithTag("implementation_LEFT")
                .assertTextEquals("$packageName:$artifact:$version · light$theme")
            sample().performScrollTo().performClick()
            status("LEFT", "Clicks: 1")
            status("RIGHT", "Clicks: 0")
            compose.onNodeWithTag("enabled").performScrollTo().performClick()
            sample().performScrollTo().assertIsNotEnabled().performTouchInput { click() }
            status("LEFT", "Clicks: 1")
            compose.onNodeWithTag("enabled").performScrollTo().performClick()
            compose.onNodeWithTag("reset").performScrollTo().performClick()
            status("LEFT", "Clicks: 0")
        }
    }

    @Test
    fun selectionControlsWorkInBothLibraries() {
        families.forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.CHECKBOX)
            sample().performScrollTo().performClick().assertIsOn()
            status("LEFT", "Checked")
            status("RIGHT", "Unchecked")
            compose.onNodeWithTag("enabled").performScrollTo().performClick()
            sample().performScrollTo().assertIsNotEnabled().performTouchInput { click() }
            sample().assertIsOn()
            compose.onNodeWithTag("enabled").performScrollTo().performClick()
            chooseComponent(LabComponent.SWITCH)
            sample().performScrollTo().performClick().assertIsOn()
            status("LEFT", "On")
            chooseComponent(LabComponent.RADIO)
            compose.onNodeWithTag("library_LEFT_2").performScrollTo().performClick()
            status("LEFT", "Selected: Option B")
            status("RIGHT", "No selection")
        }
    }

    @Test
    fun textSliderAndProgressValuesStayIndependentInBothLibraries() {
        families.forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.TEXT_FIELD)
            sample().performScrollTo().performTextReplacement("Componentory")
            closeSoftKeyboard()
            status("LEFT", "Text: Componentory")
            status("RIGHT", "Text: empty")
            chooseComponent(LabComponent.SLIDER)
            sample().performScrollTo().performTouchInput {
                swipe(center, Offset(width * 0.9f, center.y))
            }
            val progress =
                sample()
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.ProgressBarRangeInfo]
                    .current
            assertTrue(progress > 50f)
            status("RIGHT", "Value: 50 / 100")
            compose.onNodeWithTag("enabled").performScrollTo().performClick()
            sample().performScrollTo().assertIsNotEnabled().performTouchInput {
                swipe(Offset(width * 0.9f, center.y), center)
            }
            assertEquals(
                progress,
                sample()
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.ProgressBarRangeInfo]
                    .current,
                0.001f,
            )
            compose.onNodeWithTag("enabled").performScrollTo().performClick()
            compose.onNodeWithTag("reset").performScrollTo().performClick()
            status("LEFT", "Value: 50 / 100")
            chooseComponent(LabComponent.PROGRESS)
            compose.onNodeWithTag("increase_LEFT").performScrollTo().performClick()
            assertEquals(
                0.6f,
                sample()
                    .fetchSemanticsNode()
                    .config[SemanticsProperties.ProgressBarRangeInfo]
                    .current,
                0.001f,
            )
            status("RIGHT", "Value: 50 / 100")
        }
    }

    @Test
    fun dialogsReportConfirmCancelAndBackInBothLibraries() {
        chooseComponent(LabComponent.DIALOG)
        families.forEach { family ->
            chooseFamily(family)
            sample().performScrollTo().performClick()
            compose.onNodeWithTag("dialog_confirm").performClick()
            status("LEFT", "Last action: Confirmed")
            sample().performScrollTo().performClick()
            compose.onNodeWithTag("dialog_cancel").performClick()
            status("LEFT", "Last action: Cancelled")
            sample().performScrollTo().performClick()
            pressBack()
            status("LEFT", "Last action: Dismissed")
            status("RIGHT", "Last action: Not opened")
        }
    }

    @Test
    fun frameworkAndLibraryPanelsAcceptTouchWithoutSharingState() {
        compose.onNodeWithTag("family_RIGHT").performScrollTo().performClick()
        compose.onNodeWithTag("family_RIGHT_MATERIAL3").performClick()
        touchNativeLeft()
        compose.onNodeWithTag("library_RIGHT").performScrollTo().performClick()
        status("LEFT", "Clicks: 1")
        status("RIGHT", "Clicks: 1")
        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        touchNativeLeft()
        compose.onNodeWithTag("library_RIGHT").performScrollTo().performTouchInput { click() }
        status("LEFT", "Clicks: 1")
        status("RIGHT", "Clicks: 1")
        compose.onNodeWithTag("reset").performScrollTo().performClick()
        status("LEFT", "Clicks: 0")
        status("RIGHT", "Clicks: 0")
    }

    private fun chooseFamily(family: DesignFamily) {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
    }

    private fun sample() = compose.onNodeWithTag("library_LEFT")

    private fun touchNativeLeft() {
        compose.onNodeWithTag("native_LEFT").performScrollTo()
        onView(withId(R.id.sample_left)).perform(nativeClick())
    }

    private fun status(panel: String, text: String) {
        compose.onNodeWithTag("status_$panel").assertTextEquals(text)
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
