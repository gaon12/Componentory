package xyz.gaon.componentory.survivor

import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import java.io.File
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

class GameBattleUiTest {
    @get:Rule val compose = createAndroidComposeRule<SurvivorActivity>()

    private lateinit var directory: File

    @Before
    fun useAnIsolatedGameSave() {
        directory = File(compose.activity.cacheDir, "game-battle-ui-" + System.nanoTime())
        val store = GameStore(directory)
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
    }

    @After
    fun removeTheTestSave() {
        compose.mainClock.autoAdvance = true
        compose.runOnUiThread { compose.activity.setContent {} }
        directory.deleteRecursively()
    }

    @Test
    fun thePauseMenuStopsTheBattleClock() {
        compose.onNodeWithTag("game_start").performScrollTo()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("game_start").performClick()
        compose.mainClock.advanceTimeBy(1200)
        compose.onNodeWithTag("game_pause").performClick()
        val before =
            compose
                .onNodeWithTag("game_time")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
        compose.mainClock.advanceTimeBy(5000)
        val after =
            compose
                .onNodeWithTag("game_time")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
        assertEquals(before, after)
        compose.onNodeWithTag("game_abandon").performClick()
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithTag("game_return").performClick()
        compose.mainClock.advanceTimeBy(32)
        compose.mainClock.autoAdvance = true
        compose.onNodeWithTag("game_selected").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun holdingMovementAndTappingASkillUseTwoIndependentPointers() {
        val engine = GameEngine.create(WeaponId.SWITCH, RunMode.NORMAL, seed = 123)
        compose.mainClock.autoAdvance = false
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    GameBattle(engine, remember { GameAssets(compose.activity) }, onFinished = {})
                }
            }
        }
        compose.mainClock.advanceTimeBy(32)
        val stick = compose.onNodeWithTag("game_move").fetchSemanticsNode().boundsInRoot.center
        val skill = compose.onNodeWithTag("game_skill").fetchSemanticsNode().boundsInRoot.center
        compose.onRoot().performTouchInput {
            down(0, stick)
            moveTo(0, stick + Offset(-50f, 0f))
        }
        compose.mainClock.advanceTimeBy(500)
        compose.onRoot().performTouchInput {
            down(1, skill)
            up(1)
        }
        compose.mainClock.advanceTimeBy(100)
        compose.onRoot().performTouchInput { up(0) }
        compose.runOnIdle {
            assertTrue(engine.session.x < 800f)
            assertTrue(engine.session.skillTicks > 0)
            assertTrue(engine.session.shieldTicks > 0)
        }
    }

    @Test
    fun growthOffersStopTimeAndResumeWithTheChosenEquipment() {
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        e.session.experience = e.session.requiredExperience
        GameGrowth.offer(e.session)
        val choice = e.session.choices.first()
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    GameBattle(e, remember { GameAssets(compose.activity) }, onFinished = {})
                }
            }
        }
        compose.onNodeWithTag("game_growth").assertIsDisplayed()
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(5000)
        compose.runOnIdle { assertEquals(0, e.session.tick) }
        compose.onNodeWithTag("game_upgrade_" + choice.key).performClick()
        compose.mainClock.advanceTimeBy(1000)
        compose.runOnIdle {
            assertEquals(2, e.session.level)
            assertTrue(e.session.tick > 0)
            assertTrue(e.session.choices.isEmpty())
        }
    }

    @Test
    fun leavingTheForegroundRequiresAnExplicitResume() {
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        compose.mainClock.autoAdvance = false
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    GameBattle(e, remember { GameAssets(compose.activity) }, onFinished = {})
                }
            }
        }
        compose.mainClock.advanceTimeBy(1000)
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        val tick = e.session.tick
        compose.mainClock.advanceTimeBy(2000)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.mainClock.advanceTimeBy(4000)
        compose.runOnIdle { assertEquals(tick, e.session.tick) }
        compose.onNodeWithTag("game_resume").performClick()
        compose.mainClock.advanceTimeBy(1000)
        compose.runOnIdle { assertTrue(e.session.tick > tick) }
    }
}
