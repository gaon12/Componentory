package xyz.gaon.componentory.onboarding

import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class IntroductionTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var preferences: OnboardingPreferences
    private lateinit var scenario: ActivityScenario<MainActivity>
    private var originalCompleted = false

    @Before
    fun launchAsAFirstTimeUser() {
        preferences =
            OnboardingPreferences(InstrumentationRegistry.getInstrumentation().targetContext)
        originalCompleted = preferences.completed()
        preferences.saveCompleted(false)
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    @After
    fun restoreIntroductionPreference() {
        scenario.close()
        preferences.saveCompleted(originalCompleted)
    }

    @Test
    fun completingAllPagesSurvivesRecreationAndCanBeReplayedFromAbout() {
        compose.onNodeWithTag("introduction").assertIsDisplayed()
        compose.onNodeWithTag("introduction_page").assertTextEquals("1 of 3")
        compose.onNodeWithTag("introduction_next").performClick()
        scenario.recreate()
        compose.onNodeWithTag("introduction_page").assertTextEquals("2 of 3")
        compose.onNodeWithTag("introduction_back").performClick()
        compose.onNodeWithTag("introduction_page").assertTextEquals("1 of 3")
        repeat(3) { compose.onNodeWithTag("introduction_next").performClick() }
        compose.onNodeWithTag("introduction").assertDoesNotExist()
        assertTrue(preferences.completed())
        scenario.recreate()
        compose.onNodeWithTag("introduction").assertDoesNotExist()
        compose.openSettingsPage("ABOUT")
        compose.onNodeWithTag("show_introduction").performScrollTo().performClick()
        compose.onNodeWithTag("introduction_page").assertTextEquals("1 of 3")
    }

    @Test
    fun skippingTheIntroductionDoesNotShowItAgainOnTheNextLaunch() {
        compose.onNodeWithTag("introduction_skip").performClick()
        assertTrue(preferences.completed())
        scenario.close()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.onNodeWithTag("introduction").assertDoesNotExist()
        compose.onNodeWithTag("nav_list").assertIsDisplayed()
    }
}
