package xyz.gaon.componentory.navigation

import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

@RunWith(AndroidJUnit4::class)
class CatalogNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareCatalog() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.waitForIdle()
        compose.onNodeWithTag("list_screen").assertExists()
    }

    @Test
    fun searchFiltersTheListAndSurvivesDetailBackAndTabChanges() {
        search("Switch")
        compose.onNodeWithTag("list_SWITCH").assertIsDisplayed()
        compose.onNodeWithTag("list_BUTTON").assertDoesNotExist()
        compose.onNodeWithTag("list_SWITCH").performClick()
        compose.onNodeWithTag("detail_screen").assertExists()
        onView(withId(R.id.sample_left)).perform(click())
        compose.onNodeWithTag("status_LEFT").assertTextEquals("On")
        compose.onNodeWithTag("detail_back").performClick()
        compose.onNodeWithTag("component_search").assertTextEquals("Switch")
        compose.onNodeWithTag("nav_settings").performClick()
        compose
            .onNodeWithTag("runtime")
            .assertTextEquals(
                "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT} · ${Build.MODEL}"
            )
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("component_search").assertTextEquals("Switch")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("component_search").assertTextEquals("Switch")
    }

    @Test
    fun emptySearchCanBeClearedAndNativeClassNamesCanBeFound() {
        search("nothing matches")
        compose.onNodeWithTag("search_empty").assertIsDisplayed()
        compose.onNodeWithTag("clear_search").performClick()
        compose.onNodeWithTag("list_BUTTON").assertIsDisplayed()
        search("EditText")
        compose.onNodeWithTag("list_TEXT_FIELD").assertIsDisplayed()
        compose.onNodeWithTag("list_BUTTON").assertDoesNotExist()
    }

    @Test
    fun detailSwitchesBetweenAllFiveRealUiFamilies() {
        compose.onNodeWithTag("list_BUTTON").performClick()
        DesignFamily.entries.forEach { family ->
            compose.onNodeWithTag("family_LEFT").performClick()
            compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
            compose
                .onNodeWithTag("source_LEFT")
                .assertTextEquals(family.source(LabComponent.BUTTON))
            if (family.platform != null) onView(withId(R.id.sample_left)).perform(click())
            else compose.onNodeWithTag("library_LEFT").performClick()
            compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        }
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        pressBack()
        compose.onNodeWithTag("list_screen").assertExists()
    }

    @Test
    fun detailTransfersItsComponentAndFamilyToComparison() {
        compose.onNodeWithTag("nav_compare").performClick()
        onView(withId(R.id.sample_left)).perform(click())
        compose.onNodeWithTag("nav_list").performClick()
        search("Checkbox")
        compose.onNodeWithTag("list_CHECKBOX").performClick()
        compose.onNodeWithTag("family_LEFT").performClick()
        compose.onNodeWithTag("family_LEFT_MATERIAL3").performClick()
        compose.onNodeWithTag("library_LEFT").performClick()
        compose.onNodeWithTag("detail_compare").performClick()
        compose.onNodeWithTag("nav_compare").assertIsSelected()
        compose.onNodeWithTag("component_picker").assertTextEquals("Checkbox")
        compose.onNodeWithTag("source_LEFT").assertTextEquals("androidx.compose.material3.Checkbox")
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Unchecked")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Unchecked")
        compose.onNodeWithTag("library_LEFT").performClick()
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Checked")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Unchecked")
        pressBack()
        compose.onNodeWithTag("detail_screen").assertExists()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Checked")
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("list_screen").assertExists()
    }

    @Test
    fun compactWidthKeepsNavigationSearchAndLastComponentReachable() {
        compose.runOnUiThread {
            compose.activity.setContent { Box(Modifier.width(360.dp)) { ComponentoryApp() } }
        }
        compose.onNodeWithTag("nav_list").assertIsDisplayed()
        compose.onNodeWithTag("nav_compare").assertIsDisplayed()
        compose.onNodeWithTag("nav_settings").assertIsDisplayed()
        compose.onNodeWithTag("component_list").performScrollToNode(hasTestTag("list_DIALOG"))
        compose.onNodeWithTag("list_DIALOG").performClick()
        compose.onNodeWithTag("detail_screen").assertExists()
        compose.onNodeWithTag("detail_back").performClick()
        search("Switch")
        compose.onNodeWithTag("list_SWITCH").assertIsDisplayed()
    }

    private fun search(text: String) {
        compose.onNodeWithTag("component_search").performTextReplacement(text)
        compose.onNodeWithTag("component_search").performImeAction()
    }
}
