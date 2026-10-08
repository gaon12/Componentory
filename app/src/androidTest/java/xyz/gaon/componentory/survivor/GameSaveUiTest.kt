package xyz.gaon.componentory.survivor

import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

class GameSaveUiTest {
    @get:Rule val compose = createAndroidComposeRule<SurvivorActivity>()

    @Test
    fun aSavedRunWaitsForExplicitContinueAndKeepsItsBuildAndReward() {
        val directory = File(compose.activity.cacheDir, "game-resume-ui-" + System.nanoTime())
        val store = GameStore(directory)
        val s = GameEngine.create(WeaponId.SWITCH, RunMode.RANKED).session
        s.tick = 600
        s.eliteKills = 2
        s.supports[SupportId.NEKO] = 3
        store.start(GameJson.session(s))
        try {
            compose.runOnUiThread {
                compose.activity.setContent {
                    ComponentoryTheme {
                        GameHost(
                            remember { GameAssets(compose.activity) },
                            onClose = {},
                            storage = store,
                        )
                    }
                }
            }
            compose.onNodeWithTag("game_continue").performScrollTo().assertIsEnabled()
            assertEquals(600, GameJson.session(requireNotNull(store.read().activeJson)).tick)
            compose.mainClock.autoAdvance = false
            compose.onNodeWithTag("game_continue").performClick()
            compose.mainClock.advanceTimeBy(32)
            compose.onNodeWithTag("game_time").assertExists()
            compose.onNodeWithTag("game_pause").performClick()
            compose.mainClock.advanceTimeBy(32)
            compose.onNodeWithTag("game_equipment").assertExists()
            compose.onNodeWithTag("game_abandon").performClick()
            compose.mainClock.advanceTimeBy(32)
            compose.onNodeWithTag("game_reward").assertExists()
            val completed = store.read()
            assertNull(completed.activeId)
            assertEquals(1, completed.records.size)
            assertEquals(s.supports, completed.records.single().supports)
            assertEquals(2L, completed.progress.currency)
            assertTrue(completed.submissions.isEmpty())
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("game_return").performScrollTo().performClick()
            compose.onNodeWithTag("game_start").performScrollTo().assertIsEnabled()
        } finally {
            directory.deleteRecursively()
        }
    }
}
