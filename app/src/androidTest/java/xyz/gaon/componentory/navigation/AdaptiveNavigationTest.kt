package xyz.gaon.componentory.navigation

import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class AdaptiveNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private var orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            orientation = compose.activity.requestedOrientation
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @After
    fun restoreOrientation() {
        compose.runOnUiThread { compose.activity.requestedOrientation = orientation }
    }

    @Test
    fun availableWidthMovesTheSameCatalogAndSampleBetweenLayouts() {
        compose.runOnUiThread {
            compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        compose.waitUntil(10_000) { compose.activity.resources.configuration.screenWidthDp >= 800 }
        val width = mutableStateOf(800.dp)
        compose.runOnUiThread {
            compose.activity.setContent { Box(Modifier.width(width.value)) { ComponentoryApp() } }
        }
        compose.onNodeWithTag("app_navigation_rail").assertIsDisplayed()
        compose.onNodeWithTag("catalog_split").assertIsDisplayed()
        compose.onNodeWithTag("catalog_detail_empty").assertIsDisplayed()
        compose.onNodeWithTag("catalog_mode_HISTORY").assertIsDisplayed()
        compose.onNodeWithTag("component_search").performTextReplacement("Switch")
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("list_SWITCH").performClick()
        compose.onNodeWithTag("list_screen").assertIsDisplayed()
        compose.onNodeWithTag("detail_screen").assertIsDisplayed()
        val listBounds = compose.onNodeWithTag("list_screen").fetchSemanticsNode().boundsInRoot
        val detailBounds = compose.onNodeWithTag("detail_screen").fetchSemanticsNode().boundsInRoot
        assertTrue(detailBounds.left >= listBounds.right)
        compose.onNodeWithTag("native_LEFT").performScrollTo()
        onView(withId(R.id.sample_left)).perform(click())
        compose.onNodeWithTag("status_LEFT").assertTextEquals("On")
        compose.runOnUiThread { width.value = 360.dp }
        compose.onNodeWithTag("app_navigation_rail").assertDoesNotExist()
        compose.onNodeWithTag("list_screen").assertDoesNotExist()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("On")
        compose.runOnUiThread { width.value = 800.dp }
        compose.onNodeWithTag("component_search").assertTextEquals("Switch")
        compose.onNodeWithTag("status_LEFT").assertTextEquals("On")
        compose.onNodeWithTag("detail_back").performClick()
        compose.onNodeWithTag("catalog_detail_empty").assertIsDisplayed()
        compose.onNodeWithTag("component_search").assertTextEquals("Switch")
    }

    @Test
    fun wideSettingsKeepCategoriesBesideTheSelectedPage() {
        compose.runOnUiThread {
            compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        compose.waitForIdle()
        assumeTrue(
            "Settings master and detail panes need a window wider than 936dp including the navigation rail.",
            compose.activity.resources.configuration.screenWidthDp >= 936,
        )
        compose.openSettingsPage("LANGUAGE")
        compose.onNodeWithTag("settings_columns").assertIsDisplayed()
        val categories =
            compose.onNodeWithTag("settings_categories").fetchSemanticsNode().boundsInRoot
        val detail = compose.onNodeWithTag("settings_detail").fetchSemanticsNode().boundsInRoot
        assertTrue(detail.left >= categories.right)
        compose.onNodeWithTag("settings_category_DEVICE").performClick()
        compose.onNodeWithTag("runtime").assertIsDisplayed()
        compose.onNodeWithTag("settings_category_LANGUAGE").performClick()
        compose.onNodeWithTag("runtime").assertDoesNotExist()
        compose.onNodeWithTag("language_ENGLISH").assertIsDisplayed()
        compose.onNodeWithTag("settings_columns").assertExists()
    }
}
