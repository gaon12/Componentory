package xyz.gaon.componentory.navigation

import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class TabReselectionTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun componentsReselectionClosesDetailsAndClearsSearchAndHistoryMode() {
        compose.onNodeWithTag("component_search").performTextReplacement("Switch")
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("list_SWITCH").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("detail_screen").assertDoesNotExist()
        compose.onNodeWithTag("component_search").assertTextEquals("")
        compose.onNodeWithTag("egg_2_3").assertIsDisplayed()
        compose.onNodeWithTag("catalog_mode_HISTORY").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("catalog_mode_SAMPLES").assertIsSelected()
        compose.onNodeWithTag("egg_2_3").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("component_search").assertTextEquals("")
        compose.onNodeWithTag("catalog_mode_SAMPLES").assertIsSelected()
    }

    @Test
    fun comparisonReselectionRestoresItsDefaultProvidersAndLiveState() {
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("native_LEFT").performScrollTo()
        onView(withId(R.id.sample_left)).perform(click())
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_MATERIAL3").performClick()
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement("Switch")
        compose.onNodeWithTag("component_SWITCH").performClick()
        compose.onNodeWithTag("nav_compare").performClick()
        compose
            .onNodeWithTag("component_picker")
            .assertTextEquals(compose.activity.getString(LabComponent.BUTTON.labelRes))
        compose.onNodeWithTag("native_LEFT").performScrollTo().assertExists()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 0")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("status_LEFT").performScrollTo().assertTextEquals("Clicks: 0")
    }

    @Test
    fun settingsReselectionReturnsToItsCategoriesAndStaysThereAfterRecreation() {
        compose.openSettingsPage("DEVICE")
        compose.onNodeWithTag("settings_page_DEVICE").assertExists()
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("settings_page_DEVICE").assertDoesNotExist()
        compose.onNodeWithTag("settings_category_APPEARANCE").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("settings_page_DEVICE").assertDoesNotExist()
        compose.onNodeWithTag("settings_category_APPEARANCE").assertIsDisplayed()
    }
}
