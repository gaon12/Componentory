package xyz.gaon.componentory.eastereggs

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.SystemClock
import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
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
        useTheGameOrientation()
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
        useTheGameOrientation()
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
        awaitSettings()
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        compose.onNodeWithTag("nav_settings").assertIsSelected()
        compose.onNodeWithTag("app_version").assertIsDisplayed()
    }

    @Test
    fun theCloseButtonReturnsToSettingsAndAnotherOpeningNeedsSevenNewTaps() {
        useTheGameOrientation()
        openEgg()
        compose.onNodeWithTag("componentory_easter_egg_close").performClick()
        awaitSettings()
        compose.onNodeWithTag("nav_settings").assertIsSelected()
        tapVersion(6)
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        tapVersion(1)
        compose.onNodeWithTag("componentory_easter_egg").assertIsDisplayed()
        pressBack()
        awaitSettings()
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

    /**
     * A portrait phone rotates twice around the landscape game, and Android may recreate
     * MainActivity several times in a row. A short-lived copy can leave a Compose root that never
     * attaches, and Compose test idling then waits forever. The app itself restores Settings
     * correctly, so tests that open the game first match its orientation. The stop and recreation
     * tests stay in the default orientation.
     */
    private fun useTheGameOrientation() {
        compose.runOnUiThread {
            compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        compose.waitUntil(10_000) {
            runCatching {
                    compose.activity.resources.configuration.orientation ==
                        Configuration.ORIENTATION_LANDSCAPE
                }
                .getOrDefault(false)
        }
        awaitSettings()
        // The rotation recreated the Activity, so its window needs the flag again.
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * Closing the game finishes its Activity, and Settings returns through an Activity result.
     * Compose idling does not track that hand-off, and for a moment there can be no Compose window
     * at all, so wait for Settings before checking it.
     */
    private fun awaitSettings() {
        compose.waitUntil(10_000) {
            runCatching {
                    compose
                        .onAllNodesWithTag("componentory_easter_egg")
                        .fetchSemanticsNodes()
                        .isEmpty() &&
                        compose.onAllNodesWithTag("nav_settings").fetchSemanticsNodes().isNotEmpty()
                }
                .getOrDefault(false)
        }
    }

    private fun openEgg() {
        tapVersion(7)
        compose.onNodeWithTag("componentory_easter_egg").assertIsDisplayed()
    }
}
