package xyz.gaon.componentory.survivor

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class GameSaveTest {
    @Test
    fun aCheckpointRestoresRandomnessTimersProjectilesAndGrowth() {
        val e =
            GameEngine.create(
                WeaponId.SPINNER,
                RunMode.NORMAL,
                PermanentLevels(2, 3, 4),
                SupportId.entries.toSet(),
                91,
            )
        val s = e.session
        s.supports[SupportId.JELLY_BEAN] = 3
        s.weapons[0].level = 5
        s.weapons[0].evolved = true
        repeat(400) { e.step(GameInput(.4f, skill = it == 20)) }
        s.experience = s.requiredExperience
        GameGrowth.offer(s)
        val restored = GameJson.session(GameJson.session(s))
        assertEquals(GameJson.session(s), GameJson.session(restored))
        val choice = s.choices.first()
        assertTrue(GameGrowth.choose(s, choice))
        assertTrue(GameGrowth.choose(restored, choice))
        val next = GameEngine(restored)
        repeat(400) {
            val input = GameInput(-.2f, .3f)
            e.step(input)
            next.step(input)
        }
        assertEquals(GameJson.session(s), GameJson.session(restored))
    }

    @Test
    fun scoreRewardAndSubmissionAreCommittedOncePerRunId() {
        val s = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED).session
        s.tick = 1201 * 60
        s.regularKills = 11
        s.eliteKills = 2
        s.bossKills = 4
        s.outcome = RunOutcome.WON
        s.bonusCurrency = 3
        val before = GameSave(activeId = s.id, activeJson = GameJson.session(s))
        val once = GameLedger.finish(before, s, 100)
        val twice = GameLedger.finish(once, s, 200)
        assertEquals(once, twice)
        assertEquals(20310L, once.records.single().score)
        assertEquals(136L, once.progress.currency)
        assertEquals(1, once.submissions.size)
        assertNull(once.activeId)
        assertEquals(once, GameJson.save(GameJson.save(once)))
    }

    @Test
    fun abandonmentRecordsItsBuildWithoutAnOnlineSubmission() {
        val s = GameEngine.create(WeaponId.SLIDER, RunMode.RANKED).session
        s.outcome = RunOutcome.ABANDONED
        s.supports[SupportId.JELLY_BEAN] = 3
        val result = GameLedger.finish(GameSave(), s)
        assertTrue(result.submissions.isEmpty())
        assertEquals(RunOutcome.ABANDONED, result.records.single().outcome)
        assertEquals(s.supports, result.records.single().supports)
    }

    @Test
    fun oldCompletionCannotClearAnotherRunOrPayAgain() {
        val old = GameEngine.create(WeaponId.BUTTON, RunMode.NORMAL).session
        old.outcome = RunOutcome.DEFEATED
        val next = GameEngine.create(WeaponId.SWITCH, RunMode.NORMAL).session
        val finished = GameLedger.finish(GameSave(), old)
        val running = finished.copy(activeId = next.id, activeJson = GameJson.session(next))
        assertEquals(running, GameLedger.finish(running, old))
    }

    @Test
    fun commonPurchasesRespectBudgetAndFiveLevelLimit() {
        var p = GameProgress(1000)
        repeat(5) { p = requireNotNull(p.buy(PermanentUpgrade.HEALTH)) }
        assertEquals(250L, p.currency)
        assertEquals(5, p.permanent.health)
        assertNull(p.buy(PermanentUpgrade.HEALTH))
        p = requireNotNull(p.unlock(SupportId.NEKO))
        assertNull(p.unlock(SupportId.NEKO))
        assertNull(GameProgress(49).buy(PermanentUpgrade.DAMAGE))
        assertNull(GameProgress(99).unlock(SupportId.OCTOPUS))
        WeaponId.entries.forEach { weapon ->
            assertEquals(
                125f,
                GameEngine.create(weapon, RunMode.NORMAL, p.permanent, p.unlocked).session.maxHealth,
            )
            assertEquals(
                100f,
                GameEngine.create(weapon, RunMode.RANKED, p.permanent, p.unlocked).session.maxHealth,
            )
        }
    }

    @Test
    fun invalidSaveStateIsRejectedInsteadOfResettingProgress() {
        val s = GameEngine.create(WeaponId.BUTTON, RunMode.NORMAL).session
        val o = JSONObject(GameJson.session(s)).put("randomState", 0)
        assertThrows(IllegalArgumentException::class.java) { GameJson.session(o.toString()) }
        val save = JSONObject(GameJson.save(GameSave())).put("schema", 999)
        assertThrows(IllegalArgumentException::class.java) { GameJson.save(save.toString()) }
    }
}
