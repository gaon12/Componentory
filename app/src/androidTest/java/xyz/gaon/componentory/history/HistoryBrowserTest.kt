package xyz.gaon.componentory.history

import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity

@RunWith(AndroidJUnit4::class)
class HistoryBrowserTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openHistory() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("catalog_mode_HISTORY").performClick()
        waitForHistory()
    }

    @Test
    fun versionSearchTracksRealAvailabilityAndRetainsSelectionAfterRecreation() {
        search("Toolbar")
        compose.onNodeWithTag("history_empty").assertIsDisplayed()
        chooseApi(21)
        compose.onNodeWithTag("history_android.widget.Toolbar").assertIsDisplayed()
        compose.onNodeWithTag("history_filter_ADDED").performClick().assertIsSelected()
        compose.onNodeWithTag("history_android.widget.Toolbar").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        waitForHistory()
        compose.onNodeWithTag("history_filter_ADDED").assertIsSelected()
        compose.onNodeWithTag("history_version").assertTextContains("API 21", substring = true)
        compose.onNodeWithTag("history_android.widget.Toolbar").assertIsDisplayed()
        compose.onNodeWithTag("history_evidence").assertIsDisplayed()
        compose.onNodeWithTag("detail_screen").assertDoesNotExist()
    }

    @Test
    fun packagedTableIsVerifiedAndClassesKeepTheirPublicMetadata() {
        val history = AndroidHistory.read(compose.activity)
        assertEquals("3af7c93524be6f51e092b87b17f009f13ee98b43", history.sourceCommit)
        assertEquals(35, history.identicalSnapshots[36])
        assertEquals(1, history.introduced("android.widget.Button"))
        assertEquals(14, history.introduced("android.widget.Switch"))
        search("ActionBar")
        compose.onNodeWithTag("history_android.app.ActionBar").assertIsDisplayed()
        compose.onNodeWithTag("detail_screen").assertDoesNotExist()
    }

    private fun chooseApi(api: Int) {
        compose.onNodeWithTag("history_version").performClick()
        compose.onNodeWithTag("history_api_$api").performScrollTo().performClick()
    }

    private fun waitForHistory() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("history_version").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun search(query: String) {
        compose.onNodeWithTag("component_search").performTextReplacement(query)
        compose.onNodeWithTag("component_search").performImeAction()
    }
}
