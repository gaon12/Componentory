package xyz.gaon.componentory.survivor

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.FrameMetrics
import androidx.activity.compose.setContent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

class GamePerformanceTest {
    @Test
    fun crowdedMixedArtworkRunsOnTheRealFrameClock() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        val s = e.session
        s.tick = 18 * 60 * 60
        s.shieldTicks = Int.MAX_VALUE
        s.freezeTicks = Int.MAX_VALUE
        s.spawnedBosses.addAll(1..4)
        s.nextId = 10000
        s.weapons.clear()
        WeaponId.entries.forEach { s.weapons += GameWeapon(it, 5, true) }
        SupportId.entries.forEach { s.supports[it] = 5 }
        repeat(150) { index ->
            s.enemies +=
                GameEnemy(
                    100 + index,
                    200f + (index % 15) * 85,
                    100f + (index / 15) * 70,
                    20000f,
                    20000f,
                    if (index % 10 == 0) EnemyKind.ELITE else EnemyKind.NORMAL,
                    source = GameFamily.entries[index % GameFamily.entries.size],
                )
        }
        val durations = mutableListOf<Long>()
        ActivityScenario.launch(SurvivorActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val assets = GameAssets(activity)
                (GameFamily.entries.map { it.art } +
                        WeaponId.entries.map(GameCatalog::weaponArt) +
                        listOf("progress"))
                    .distinct()
                    .forEach { assets.bitmap(it) }
                activity.window.addOnFrameMetricsAvailableListener(
                    { _, metrics, _ ->
                        durations += metrics.getMetric(FrameMetrics.TOTAL_DURATION)
                    },
                    Handler(Looper.getMainLooper()),
                )
                activity.setContent { ComponentoryTheme { GameBattle(e, assets, onFinished = {}) } }
            }
            SystemClock.sleep(1000)
            var before = 0
            scenario.onActivity {
                before = s.tick
                durations.clear()
            }
            SystemClock.sleep(10000)
            var ticks = 0
            var measured = emptyList<Long>()
            scenario.onActivity {
                ticks = s.tick - before
                measured = durations.sorted()
            }
            val p95 =
                if (measured.isEmpty()) Long.MAX_VALUE
                else measured[((measured.size - 1) * .95).toInt()]
            val screenshot = instrumentation.uiAutomation.takeScreenshot()
            val directory =
                requireNotNull(instrumentation.targetContext.getExternalFilesDir("game-evidence"))
            directory.mkdirs()
            File(directory, "mixed-combat-stress.png").outputStream().use {
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            screenshot.recycle()
            instrumentation.sendStatus(
                0,
                Bundle().apply {
                    putString(
                        "game_render_fixture",
                        "150 frozen mixed-source enemies; four evolved weapons; invulnerable rendering fixture, not a completed run",
                    )
                    putInt("game_engine_ticks_in_ten_seconds", ticks)
                    putInt("game_frame_count", measured.size)
                    putLong("game_frame_p95_nanoseconds", p95)
                    putString("game_capture", "game-evidence/mixed-combat-stress.png")
                },
            )
            assertTrue("Too few real game ticks: " + ticks, ticks >= 450)
            assertTrue("Too few rendered frames: " + measured.size, measured.size >= 200)
            assertTrue("95th percentile frame duration: " + p95, p95 < 50_000_000L)
            assertTrue(s.enemies.size <= GameEngine.ENEMY_LIMIT)
            assertTrue(s.shots.size <= GameEngine.SHOT_LIMIT)
        }
    }
}
