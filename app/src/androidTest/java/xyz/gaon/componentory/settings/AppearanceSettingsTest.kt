package xyz.gaon.componentory.settings

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.WindowManager
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.PlatformFamily

@RunWith(AndroidJUnit4::class)
class AppearanceSettingsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var preferences: AppearancePreferences
    private lateinit var originalAppearance: AppAppearance

    @Before
    fun prepareSettings() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.waitForIdle()
        preferences = AppearancePreferences(compose.activity)
        originalAppearance = preferences.read()
    }

    @After
    fun restoreAppearance() {
        preferences.save(originalAppearance)
    }

    @Test
    fun lightAndDarkAppearanceChangeActualPixelsAndSurviveRecreation() {
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("appearance_DARK").performClick()
        assertTrue(backgroundLuminance() < 0.1f)
        assertSystemBars(dark = true)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("appearance_DARK").assertIsSelected()
        assertTrue(backgroundLuminance() < 0.1f)
        compose.onNodeWithTag("appearance_LIGHT").performClick()
        assertTrue(backgroundLuminance() > 0.8f)
        assertSystemBars(dark = false)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("appearance_LIGHT").assertIsSelected()
        assertTrue(backgroundLuminance() > 0.8f)
        compose.onNodeWithTag("appearance_SYSTEM").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("appearance_SYSTEM").assertIsSelected()
    }

    @Test
    fun changingAppAppearanceKeepsTheNativeSampleThemeAndLiveState() {
        compose.onNodeWithTag("list_BUTTON").performClick()
        onView(withId(R.id.sample_left)).perform(click())
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("appearance_DARK").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        onView(withId(R.id.sample_left)).check { view, error ->
            if (error != null) throw error
            val actual = TypedValue()
            val expected = TypedValue()
            view.context.theme.resolveAttribute(android.R.attr.buttonStyle, actual, true)
            ContextThemeWrapper(compose.activity, PlatformFamily.CLASSIC.themeId)
                .theme
                .resolveAttribute(android.R.attr.buttonStyle, expected, true)
            assertEquals(expected.resourceId, actual.resourceId)
        }
        onView(withId(R.id.sample_left)).perform(click())
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 2")
    }

    private fun backgroundLuminance(): Float {
        val pixels = compose.onNodeWithTag("settings_screen").captureToImage().toPixelMap()
        return pixels[0, 0].luminance()
    }

    private fun assertSystemBars(dark: Boolean) {
        compose.runOnIdle {
            val window = compose.activity.window
            val bars = WindowCompat.getInsetsController(window, window.decorView)
            assertEquals(!dark, bars.isAppearanceLightStatusBars)
            assertEquals(!dark, bars.isAppearanceLightNavigationBars)
        }
    }
}
