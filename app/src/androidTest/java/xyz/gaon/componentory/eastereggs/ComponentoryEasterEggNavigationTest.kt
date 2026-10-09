package xyz.gaon.componentory.eastereggs

import android.os.SystemClock
import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.survivor.SurvivorActivity

@RunWith(AndroidJUnit4::class)
class ComponentoryEasterEggNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openSettings() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("app_version").performScrollTo()
    }

    @Test
    fun tapsKeepTheirCountAcrossPausesAndOnlyTheSeventhOpensTheScreen() {
        tapVersion(2)
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        SystemClock.sleep(1_100)
        tapVersion(4)
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        tapVersion(1)
        compose.onNodeWithTag("componentory_easter_egg").assertIsDisplayed()
        compose.onNodeWithTag("nav_settings").assertDoesNotExist()
        compose.onNodeWithTag("settings_screen").assertDoesNotExist()
    }

    @Test
    fun gameActivityRecreationKeepsTheLobbyOpenAndBackRestoresSettings() {
        openEgg()
        compose.onNodeWithTag("game_weapon_open").performScrollTo().performClick()
        compose.onNodeWithTag("game_weapon_SPINNER").performScrollTo().performClick()
        compose.onNodeWithTag("game_panel_close").performClick()
        lateinit var original: SurvivorActivity
        compose.runOnUiThread {
            original =
                ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED)
                    .filterIsInstance<SurvivorActivity>()
                    .single()
            original.recreate()
        }
        compose.waitUntil(10_000) {
            var restored = false
            compose.runOnUiThread {
                restored =
                    ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED)
                        .filterIsInstance<SurvivorActivity>()
                        .any { it !== original }
            }
            restored &&
                runCatching { compose.onNodeWithTag("componentory_easter_egg").isDisplayed() }
                    .getOrDefault(false)
        }
        compose.onNodeWithTag("game_selected", useUnmergedTree = true).assertTextEquals("Spinner")
        pressBack()
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        compose.onNodeWithTag("nav_settings").assertIsSelected()
        compose.onNodeWithTag("app_version").assertIsDisplayed()
    }

    @Test
    fun theCloseButtonReturnsToSettingsAndAnotherOpeningNeedsSevenNewTaps() {
        openEgg()
        compose.onNodeWithTag("componentory_easter_egg_close").performClick()
        compose.onNodeWithTag("nav_settings").assertIsSelected()
        tapVersion(6)
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        tapVersion(1)
        compose.onNodeWithTag("componentory_easter_egg").assertIsDisplayed()
        pressBack()
        compose.onNodeWithTag("app_version").assertIsDisplayed()
    }

    @Test
    fun partialTapsAreDiscardedAfterTabChangesAndRecreation() {
        tapVersion(6)
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("nav_settings").performClick()
        tapVersion(1)
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        compose.activityRule.scenario.recreate()
        tapVersion(6)
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
    }

    @Test
    fun stoppingTheActivityDiscardsPartialTaps() {
        tapVersion(6)
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        tapVersion(1)
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
    }

    private fun tapVersion(count: Int) {
        compose.onNodeWithTag("app_version").performScrollTo().performTouchInput {
            repeat(count) { click() }
        }
    }

    private fun openEgg() {
        tapVersion(7)
        compose.onNodeWithTag("componentory_easter_egg").assertIsDisplayed()
    }
}
