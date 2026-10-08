package xyz.gaon.componentory.survivor

import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

class GameRecordUiTest {
    @get:Rule val compose = createAndroidComposeRule<SurvivorActivity>()

    @Test
    fun buildsFromTwoRunsCanBeComparedWithinOneRuleset() {
        val records =
            WeaponId.entries.take(2).map { weapon ->
                val s = GameEngine.create(weapon, RunMode.NORMAL).session
                s.outcome = RunOutcome.DEFEATED
                GameLedger.finish(GameSave(), s).records.single()
            }
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    GameRecordScreen(
                        records,
                        remember { GameAssets(compose.activity) },
                        onClose = {},
                    )
                }
            }
        }
        records.forEach { record ->
            compose
                .onNodeWithTag("game_records_list")
                .performScrollToNode(hasTestTag("game_record_" + record.id))
            compose.onNodeWithTag("game_record_" + record.id).performClick()
        }
        compose.onNodeWithTag("game_compare").assertIsEnabled().performClick()
        compose.onNodeWithTag("game_compare_close").performClick()
        compose.onNodeWithTag("game_record_mode_RANKED").performClick()
        compose.onNodeWithTag("game_compare").assertIsNotEnabled()
        compose.onNodeWithTag("game_best").assertDoesNotExist()
    }
}
