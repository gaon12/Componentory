package xyz.gaon.componentory.eastereggs

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class ComponentoryEasterEggScreenTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun matchTheGameOrientation() {
        // The game Activity is landscape. A portrait caller would rotate back when the game
        // closes and be recreated, which discards the test content set below. The real app
        // restores its Settings state across that recreation, and
        // ComponentoryEasterEggNavigationTest covers that path.
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
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun closeButtonReturnsToTheCaller() {
        showScreen()
        compose.onNodeWithTag("componentory_easter_egg").assertIsDisplayed()
        compose.onNodeWithTag("componentory_easter_egg_close").performClick()
        assertClosed()
    }

    @Test
    fun systemBackReturnsToTheCaller() {
        showScreen()
        pressBack()
        assertClosed()
    }

    @Test
    fun thePrivateGameLobbyReplacesTheOldWelcomeScreen() {
        showScreen(compact = true)
        compose.onNodeWithTag("game_selected", useUnmergedTree = true).assertTextEquals("Button")
        compose.onNodeWithTag("componentory_easter_egg_close").assertIsDisplayed().performClick()
        assertClosed()
    }

    private fun showScreen(compact: Boolean = false) {
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    var open by remember { mutableStateOf(true) }
                    if (open) {
                        val density = LocalDensity.current
                        CompositionLocalProvider(
                            LocalDensity provides
                                Density(density.density, if (compact) 2f else density.fontScale)
                        ) {
                            Box(if (compact) Modifier.size(320.dp, 300.dp) else Modifier) {
                                ComponentoryEasterEggScreen { open = false }
                            }
                        }
                    } else Text("Closed", Modifier.testTag("egg_caller"))
                }
            }
        }
    }

    private fun assertClosed() {
        // The result comes back through the caller's Activity lifecycle, which Compose
        // idling does not track, so wait for the caller instead of checking at once.
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("egg_caller").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        compose.onNodeWithTag("egg_caller").assertIsDisplayed()
    }
}
