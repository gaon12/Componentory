package xyz.gaon.componentory.survivor

import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    @Test
    fun diagonalInputDoesNotMoveFasterAndInvalidInputCannotCorruptTheWorld() {
        val diagonal = GameEngine.create(1, RunMode.NORMAL, seed = 4)
        val straight = GameEngine.create(37, RunMode.NORMAL, seed = 4)
        repeat(30) {
            diagonal.step(GameInput(1f, 1f))
            straight.step(GameInput(1f))
        }
        val ds = diagonal.session
        assertEquals(
            straight.session.x - 800f,
            kotlin.math.sqrt((ds.x - 800f) * (ds.x - 800f) + (ds.y - 450f) * (ds.y - 450f)),
            0.01f,
        )
        diagonal.step(GameInput(Float.NaN, Float.POSITIVE_INFINITY))
        assertTrue(ds.x.isFinite() && ds.y.isFinite())
    }

    @Test
    fun rankedConditionsIgnorePermanentPowerAndLocalLocks() {
        val engine = GameEngine.create(1, RunMode.RANKED, PermanentLevels(5, 5, 5), emptySet(), 999)
        assertEquals(100f, engine.session.maxHealth)
        assertEquals(PermanentLevels(), engine.session.permanent)
        assertEquals(GameCatalog.RANKED_SEED, engine.session.seed)
        assertEquals(SupportId.entries.toSet(), engine.session.unlocked)
    }

    @Test
    fun aSeedAndInputSequenceReproduceTheSameBattle() {
        val first = GameEngine.create(9, RunMode.NORMAL, seed = 713)
        val second = GameEngine.create(9, RunMode.NORMAL, seed = 713)
        repeat(600) { index ->
            val input = GameInput(if (index < 300) 0.5f else -0.5f, skill = index == 20)
            first.step(input)
            second.step(input)
        }
        assertEquals(first.session.enemies, second.session.enemies)
        assertEquals(first.session.shots, second.session.shots)
        assertEquals(first.session.randomState, second.session.randomState)
        assertEquals(first.session.health, second.session.health)
    }

    @Test
    fun autoAttackKillsOnceAndDropsExperience() {
        val engine = GameEngine.create(1, RunMode.NORMAL, seed = 1)
        val s = engine.session
        s.enemies += GameEnemy(900, s.x + 70f, s.y, 1f, 1f)
        repeat(30) { engine.step() }
        assertEquals(1, s.regularKills)
        assertEquals(0, s.eliteKills)
        assertTrue(s.experience > 0)
    }

    @Test
    fun shieldAndFreezeHaveARealCooldown() {
        val shield = GameEngine.create(1, RunMode.NORMAL)
        shield.step(GameInput(skill = true))
        assertEquals(240, shield.session.shieldTicks)
        shield.step(GameInput(skill = true))
        assertEquals(239, shield.session.shieldTicks)
        val freeze = GameEngine.create(14, RunMode.NORMAL)
        freeze.step(GameInput(skill = true))
        assertEquals(240, freeze.session.freezeTicks)
    }

    @Test
    fun allVersionsShareTheSameBasePowerAndGlobalUpgrades() {
        for (character in GameCatalog.characters) {
            val s =
                GameEngine.create(character.api, RunMode.NORMAL, PermanentLevels(5, 5, 5)).session
            assertEquals(125f, s.maxHealth)
            assertEquals(1.25f, s.damageMultiplier)
        }
    }
}
