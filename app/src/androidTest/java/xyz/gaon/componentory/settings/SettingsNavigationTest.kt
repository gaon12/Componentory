package xyz.gaon.componentory.settings

import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class SettingsNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun categoriesOpenOnlyTheirOwnOptionsAndPreserveThePageOnRecreation() {
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("settings_category_APPEARANCE").assertIsDisplayed()
        compose.onNodeWithTag("appearance_LIGHT").assertDoesNotExist()
        compose.openSettingsPage("APPEARANCE")
        compose.onNodeWithTag("appearance_LIGHT").assertIsDisplayed()
        compose.onNodeWithTag("language_ENGLISH").assertDoesNotExist()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("appearance_LIGHT").assertIsDisplayed()
        pressBack()
        compose.onNodeWithTag("appearance_LIGHT").assertDoesNotExist()
        compose.onNodeWithTag("settings_category_LANGUAGE").assertIsDisplayed()
        compose.onNodeWithTag("settings_category_LANGUAGE").performClick()
        compose.onNodeWithTag("language_ENGLISH").assertIsDisplayed()
        compose.onNodeWithTag("appearance_LIGHT").assertDoesNotExist()
    }

    @Test
    fun aboutShowsTheGoogleTrademarkAttribution() {
        compose.openSettingsPage("ABOUT")
        compose
            .onNodeWithTag("android_trademark")
            .performScrollTo()
            .assertTextEquals("Android is a trademark of Google LLC.")
    }

    @Test
    fun contributorsAreAvailableOfflineAndTheSelectedPageSurvivesRecreation() {
        compose.openSettingsPage("CONTRIBUTORS")
        compose.onNodeWithTag("contributor_gaon12").performScrollTo().assertTextEquals("gaon12")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("contributors_list").assertIsDisplayed()
        compose.onNodeWithTag("contributors_on_github").performScrollTo().assertIsDisplayed()
    }
}
