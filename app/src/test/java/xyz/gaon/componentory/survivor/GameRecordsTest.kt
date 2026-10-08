package xyz.gaon.componentory.survivor

import org.junit.Assert.*
import org.junit.Test

class GameRecordsTest {
    private fun record(
        mode: RunMode,
        rule: String,
        weapon: WeaponId,
        score: Long,
        outcome: RunOutcome = RunOutcome.DEFEATED,
    ): GameRunRecord {
        val s = GameEngine.create(weapon, mode).session
        s.outcome = outcome
        return GameLedger.finish(GameSave(), s).records.single().copy(ruleset = rule, score = score)
    }

    @Test
    fun groupsAndBestBuildsNeverMixModesOrRules() {
        val normal = record(RunMode.NORMAL, "v1", WeaponId.BUTTON, 500)
        val ranked = record(RunMode.RANKED, "v1", WeaponId.BUTTON, 1000)
        val old = record(RunMode.NORMAL, "v0", WeaponId.BUTTON, 9000)
        val spinner = record(RunMode.NORMAL, "v1", WeaponId.SPINNER, 700)
        val abandoned = record(RunMode.NORMAL, "v1", WeaponId.BUTTON, 9999, RunOutcome.ABANDONED)
        val all = listOf(normal, ranked, old, spinner, abandoned)
        val group = GameRecords.filter(all, RunMode.NORMAL, "v1")
        assertEquals(spinner, GameRecords.best(group))
        assertEquals(
            normal,
            GameRecords.best(GameRecords.filter(all, RunMode.NORMAL, "v1", WeaponId.BUTTON)),
        )
        assertFalse(GameRecords.comparable(normal, ranked))
        assertFalse(GameRecords.comparable(normal, old))
        assertFalse(GameRecords.comparable(normal, normal))
        assertTrue(GameRecords.comparable(normal, spinner))
    }

    @Test
    fun theScoreUsesExclusiveCountsAndOnlyTwentyMinutesOfSurvival() {
        assertEquals(1110L, GameScore.calculate(1, 1, 1, 0, RunOutcome.DEFEATED))
        assertEquals(6000L, GameScore.calculate(0, 0, 0, 1200, RunOutcome.DEFEATED))
        assertEquals(6000L, GameScore.calculate(0, 0, 0, 1800, RunOutcome.ABANDONED))
        assertEquals(16000L, GameScore.calculate(0, 0, 0, 1200, RunOutcome.WON))
        assertEquals(5L, GameScore.calculate(0, 0, 0, 1, RunOutcome.DEFEATED))
        assertThrows(IllegalArgumentException::class.java) {
            GameScore.calculate(-1, 0, 0, 0, RunOutcome.WON)
        }
    }
}
