package xyz.gaon.componentory.history

import android.view.WindowManager
import android.widget.TabWidget
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
class HistoryDetailTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openHistory() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("catalog_mode_HISTORY").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("history_version").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun selectingAClassShowsItsDescriptionSourceAndOriginalCaptureStatus() {
        select("android.widget.Button")
        compose.onNodeWithTag("history_detail_class").assertTextEquals("android.widget.Button")
        compose
            .onNodeWithTag("history_detail_snapshot")
            .assertTextContains("API 19", substring = true)
        compose
            .onNodeWithTag("history_detail_description")
            .assertTextEquals(compose.activity.getString(R.string.component_button_description))
        compose
            .onNodeWithTag("history_detail_parent")
            .performScrollTo()
            .assertTextContains("android.widget.TextView", substring = true)
        compose.activityRule.scenario.recreate()
        waitForDetail()
        compose
            .onNodeWithTag("history_detail_capture")
            .performScrollTo()
            .assertTextEquals(compose.activity.getString(R.string.history_capture_missing))
        compose
            .onNodeWithTag("history_detail_behavior")
            .assertTextEquals(compose.activity.getString(R.string.history_behavior_unverified))
        compose
            .onNodeWithTag("history_detail_source")
            .assertTextContains("3af7c93524be6f51e092b87b17f009f13ee98b43", substring = true)
    }

    @Test
    fun returningFromAnInteractiveSampleRestoresTheHistoricalSelection() {
        select("android.widget.Button")
        compose.onNodeWithTag("history_open_sample").performScrollTo().performClick()
        compose.onNodeWithTag("native_LEFT").performScrollTo()
        onView(withId(R.id.sample_left)).perform(click())
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
        compose.onNodeWithTag("detail_back").performClick()
        waitForDetail()
        compose.onNodeWithTag("history_detail_class").assertTextEquals("android.widget.Button")
        compose.onNodeWithTag("component_search").assertTextEquals("android.widget.Button")
        compose.onNodeWithTag("history_detail_back").performScrollTo().performClick()
        compose.onNodeWithTag("history_version").assertTextContains("API 19", substring = true)
    }

    @Test
    fun anAbstractBaseShowsMetadataAndAChildOpensItsRealContainer() {
        select("android.widget.AbsSpinner")
        compose.onNodeWithTag("history_no_sample").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("history_open_sample").assertDoesNotExist()
        compose.onNodeWithTag("history_detail_back").performScrollTo().performClick()
        select("android.widget.TabWidget")
        compose.onNodeWithTag("history_child_sample").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("history_open_sample").performScrollTo().performClick()
        compose.onNodeWithTag("native_LEFT").performScrollTo()
        onView(isAssignableFrom(TabWidget::class.java)).check(matches(isDisplayed()))
    }

    private fun select(name: String) {
        compose.onNodeWithTag("component_search").performTextReplacement(name)
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("history_$name").performScrollTo().performClick()
        waitForDetail()
    }

    private fun waitForDetail() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("history_detail_class").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
