package xyz.gaon.componentory.survivor

import android.util.AtomicFile
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class GameStoreTest {
    @Test
    fun explicitOfflineBindingNeverReassignsAnotherProfilesScore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "game-profile-store-" + System.nanoTime())
        try {
            val store = GameStore(directory)
            for (profile in listOf("one", null, "two")) {
                val s =
                    GameEngine.create(WeaponId.BUTTON, RunMode.RANKED, rankedProfileId = profile)
                        .session
                s.outcome = RunOutcome.DEFEATED
                store.finish(GameJson.session(s))
            }
            store.bindUnowned("three", GameCatalog.RULESET)
            assertEquals(
                listOf("one", "three", "two"),
                store.read().submissions.map { it.profileId },
            )
            store.bindUnowned("four", GameCatalog.RULESET)
            assertEquals(
                listOf("one", "three", "two"),
                store.read().submissions.map { it.profileId },
            )
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun interruptedWritesStaleCheckpointsAndRepeatedCompletionKeepOneReward() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "game-store-test-" + System.nanoTime())
        try {
            val store = GameStore(directory)
            val e = GameEngine.create(WeaponId.SWITCH, RunMode.RANKED)
            val s = e.session
            val initial = GameJson.session(s)
            store.start(initial)
            repeat(60) { e.step() }
            val current = GameJson.session(s)
            store.checkpoint(current)
            store.checkpoint(initial)
            assertEquals(60, GameJson.session(requireNotNull(store.read().activeJson)).tick)
            val atomic = AtomicFile(File(directory, "save.json"))
            val interrupted = atomic.startWrite()
            interrupted.write("incomplete".toByteArray())
            atomic.failWrite(interrupted)
            assertEquals(current, store.read().activeJson)
            s.outcome = RunOutcome.DEFEATED
            s.eliteKills = 1
            val terminal = GameJson.session(s)
            store.finish(terminal)
            store.finish(terminal)
            store.checkpoint(current)
            val save = GameStore(directory).read()
            assertNull(save.activeId)
            assertEquals(1, save.records.size)
            assertEquals(1L, save.progress.currency)
            assertEquals(1, save.submissions.size)
            assertTrue(File(directory, "save.json").exists())
        } finally {
            directory.deleteRecursively()
        }
    }
}
