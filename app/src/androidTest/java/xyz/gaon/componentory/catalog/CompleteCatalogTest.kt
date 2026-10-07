package xyz.gaon.componentory.catalog

import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.navigation.ComponentoryApp

@RunWith(AndroidJUnit4::class)
class CompleteCatalogTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun formerlyPendingApisAreClickableComponents() {
        compose.onNodeWithTag("catalog_mode_SAMPLES").assertIsSelected()
        compose.onNodeWithTag("catalog_mode_HISTORY").assertIsDisplayed()
        compose.onNodeWithTag("catalog_mode_PLANNED").assertDoesNotExist()
        listOf("ACTION_BAR" to "ActionBar", "INLINE_CONTENT_VIEW" to "InlineContentView").forEach {
            (id, source) ->
            compose.onNodeWithTag("component_search").performTextReplacement(source)
            compose.onNodeWithTag("component_search").performImeAction()
            compose.onNodeWithTag("component_list").performScrollToNode(hasTestTag("list_$id"))
            compose.onNodeWithTag("list_$id").assertHasClickAction().performClick()
            compose.onNodeWithTag("detail_screen").assertIsDisplayed()
            compose.onNodeWithTag("detail_back").performClick()
        }
    }

    @Test
    fun compactNavigationHasOnlyComponentsAndHistoryAcrossTabs() {
        compose.runOnUiThread {
            compose.activity.setContent { Box(Modifier.width(320.dp)) { ComponentoryApp() } }
        }
        compose.onNodeWithTag("catalog_mode_SAMPLES").assertIsDisplayed()
        compose.onNodeWithTag("catalog_mode_HISTORY").performClick()
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("catalog_mode_HISTORY").assertIsSelected()
        compose.onNodeWithTag("catalog_mode_PLANNED").assertDoesNotExist()
        compose.onNodeWithTag("catalog_mode_SAMPLES").performClick()
        compose.onNodeWithTag("component_list").assertIsDisplayed()
    }

    @Test
    fun historyModeSurvivesRecreationWithoutAPlannedTab() {
        compose.onNodeWithTag("catalog_mode_HISTORY").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("catalog_mode_HISTORY").assertIsSelected()
        compose.onNodeWithTag("catalog_mode_PLANNED").assertDoesNotExist()
    }
}
