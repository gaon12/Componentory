package xyz.gaon.componentory.settings

import android.content.res.Configuration
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import xyz.gaon.componentory.testing.openSettingsPage

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
        compose.openSettingsPage("APPEARANCE")
        compose.onNodeWithTag("appearance_DARK").performScrollTo().performClick().assertIsSelected()
        assertTrue(backgroundLuminance() < 0.1f)
        assertSystemBars(dark = true)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("appearance_DARK").assertIsSelected()
        assertTrue(backgroundLuminance() < 0.1f)
        compose
            .onNodeWithTag("appearance_LIGHT")
            .performScrollTo()
            .performClick()
            .assertIsSelected()
        assertTrue(backgroundLuminance() > 0.8f)
        assertSystemBars(dark = false)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("appearance_LIGHT").assertIsSelected()
        assertTrue(backgroundLuminance() > 0.8f)
        compose.onNodeWithTag("appearance_SYSTEM").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("appearance_SYSTEM").assertIsSelected()
    }

    @Test
    fun nativeLightThemesDoNotInheritNightResources() {
        val configuration =
            Configuration(compose.activity.resources.configuration).apply {
                uiMode =
                    (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                        Configuration.UI_MODE_NIGHT_YES
            }
        val nightContext = compose.activity.createConfigurationContext(configuration)
        PlatformFamily.entries.forEach { family ->
            val themed = family.createContext(nightContext)
            assertEquals(
                Configuration.UI_MODE_NIGHT_NO,
                themed.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK,
            )
            assertEquals(configuration.fontScale, themed.resources.configuration.fontScale)
            assertEquals(configuration.locales, themed.resources.configuration.locales)
            val background = TypedValue()
            assertTrue(
                themed.theme.resolveAttribute(android.R.attr.colorBackground, background, true)
            )
            assertTrue(Color(background.data).luminance() > 0.7f)
        }
        assertEquals(
            Configuration.UI_MODE_NIGHT_YES,
            nightContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK,
        )
        compose.onNodeWithTag("list_BUTTON").performClick()
        onView(withId(R.id.sample_left)).check { view, error ->
            if (error != null) throw error
            assertEquals(
                Configuration.UI_MODE_NIGHT_NO,
                view.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK,
            )
        }
    }

    @Test
    fun changingAppAppearanceKeepsTheNativeSampleThemeAndLiveState() {
        compose.onNodeWithTag("list_BUTTON").performClick()
        onView(withId(R.id.sample_left)).perform(click())
        compose.openSettingsPage("APPEARANCE")
        compose.onNodeWithTag("appearance_DARK").performScrollTo().performClick().assertIsSelected()
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
