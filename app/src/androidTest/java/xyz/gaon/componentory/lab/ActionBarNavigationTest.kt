package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
class ActionBarNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun actualMenuActionsReturnToThePanelAndSurviveRecreation() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("component_search").performTextReplacement("android.app.ActionBar")
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("list_ACTION_BAR").performClick()
        compose.onNodeWithTag("native_LEFT").performScrollTo()
        onView(withId(R.id.sample_left)).perform(click())
        onView(withId(R.id.action_bar_sample_action)).perform(click())
        onView(withId(R.id.action_bar_sample_return)).perform(scrollTo(), click())
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        compose.onNodeWithTag("native_LEFT").performScrollTo()
        onView(withId(R.id.sample_left)).perform(click())
        onView(withId(R.id.action_bar_sample_action)).perform(click())
        onView(withId(R.id.action_bar_sample_return)).perform(scrollTo(), click())
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 2")
    }
}
