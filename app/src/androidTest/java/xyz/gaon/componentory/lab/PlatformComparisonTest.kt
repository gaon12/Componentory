package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.WindowManager
import android.widget.Button
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
class PlatformComparisonTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun waitForTheLabToBeReady() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.waitForIdle()
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("compare_screen").assertExists()
    }

    @Test
    fun eachFamilyUsesTheFrameworkThemeAndRespondsToTouch() {
        PlatformFamily.entries.forEach { family ->
            chooseFamily("LEFT", family)
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                assertEquals(Button::class.java, view.javaClass)
                val expected = TypedValue()
                val actual = TypedValue()
                ContextThemeWrapper(compose.activity, family.themeId)
                    .theme
                    .resolveAttribute(android.R.attr.buttonStyle, expected, true)
                view.context.theme.resolveAttribute(android.R.attr.buttonStyle, actual, true)
                assertEquals(expected.resourceId, actual.resourceId)
            }
            compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 0")
            touchNative("LEFT")
            compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
            compose.onNodeWithTag("reset").performScrollTo().performClick()
        }
    }

    @Test
    fun panelsAreIndependentAndDisabledButtonsIgnoreTouch() {
        touchNative("LEFT")
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Clicks: 0")
        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        touchNative("LEFT")
        touchNative("RIGHT")
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Clicks: 0")
        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        touchNative("RIGHT")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Clicks: 1")
        compose.onNodeWithTag("reset").performScrollTo().performClick()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 0")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Clicks: 0")
    }

    @Test
    fun changingFamilyStartsANewSampleWithoutResettingTheOtherPanel() {
        touchNative("LEFT")
        touchNative("RIGHT")
        chooseFamily("LEFT", PlatformFamily.MATERIAL)
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 0")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Clicks: 1")
    }

    private fun chooseFamily(panel: String, family: PlatformFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performClick()
    }

    private fun touchNative(panel: String) {
        compose.onNodeWithTag("native_$panel").performScrollTo()
        onView(withId(if (panel == "LEFT") R.id.sample_left else R.id.sample_right))
            .perform(click())
    }
}
