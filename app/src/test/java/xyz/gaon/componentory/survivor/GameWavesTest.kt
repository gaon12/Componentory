package xyz.gaon.componentory.survivor

import org.junit.Assert.*
import org.junit.Test

class GameWavesTest {
    @Test
    fun theFourBossMilestonesSpawnOnceWithoutReplacingTheirIdentity() {
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        e.session.shieldTicks = Int.MAX_VALUE
        for (stage in 1..4) {
            e.session.tick = stage * 300 * 60 - 1
            e.step()
            val boss = e.session.enemies.single { it.bossStage == stage }
            assertEquals(EnemyKind.BOSS, boss.kind)
            e.step()
            assertEquals(boss.id, e.session.enemies.single { it.bossStage == stage }.id)
            assertEquals(stage, e.session.spawnedBosses.size)
        }
    }

    @Test
    fun aFullTwentyMinuteClockHasBoundedEncountersAndStopsOrdinarySpawnsAtTheFinalBoss() {
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        val s = e.session
        s.shieldTicks = Int.MAX_VALUE
        s.weapons.clear() // Keep all bosses alive to exercise the complete spawn schedule.
        var enemies = 0
        var shots = 0
        repeat(GameCatalog.RUN_SECONDS * 60) {
            e.step()
            enemies = maxOf(enemies, s.enemies.size)
            shots = maxOf(shots, s.shots.size)
        }
        assertEquals(1200, s.seconds)
        assertEquals(setOf(1, 2, 3, 4), s.spawnedBosses)
        assertTrue(enemies <= GameEngine.ENEMY_LIMIT)
        assertTrue(shots <= GameEngine.SHOT_LIMIT)
        val random = s.randomState
        repeat(600) { e.step() }
        assertEquals(random, s.randomState)
    }

    @Test
    fun onlyDefeatingTheFinalBossWinsAndFurtherTicksCannotDuplicateTheKill() {
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        val s = e.session
        s.shieldTicks = Int.MAX_VALUE
        s.tick = 1199 * 60
        e.step()
        assertEquals(RunOutcome.ACTIVE, s.outcome)
        s.tick = 1200 * 60 - 1
        e.step()
        s.enemies.first { it.bossStage == 4 }.health = 0f
        e.step()
        assertEquals(RunOutcome.WON, s.outcome)
        val kills = s.bossKills
        val tick = s.tick
        repeat(600) { e.step() }
        assertEquals(kills, s.bossKills)
        assertEquals(tick, s.tick)
    }

    @Test
    fun deathOnTheFinalBossDefeatTickIsStillADeath() {
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        val s = e.session
        s.spawnedBosses.addAll(1..4)
        s.health = 1f
        s.enemies += GameEnemy(900, s.x, s.y, 0f, 100f, EnemyKind.BOSS, 4)
        s.enemies += GameEnemy(901, s.x, s.y, 100f, 100f)
        e.step()
        assertEquals(RunOutcome.DEFEATED, s.outcome)
        assertEquals(1, s.bossKills)
        assertEquals(0, s.regularKills)
    }

    @Test
    fun bossesShootHostileProjectilesThatDoNotHurtOtherEnemies() {
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        val s = e.session
        s.weapons.clear()
        s.enemies += GameEnemy(900, s.x + 100f, s.y, 100f, 100f, EnemyKind.BOSS, 1, attackTicks = 1)
        e.step()
        assertTrue(s.shots.any { it.hostile })
        assertEquals(100f, s.enemies.first().health, 0f)
        s.enemies.clear()
        s.shots.clear()
        s.shots += GameShot(902, s.x, s.y, 0f, 0f, 12f, 60, "jellybean", hostile = true)
        e.step()
        assertEquals(88f, s.health, 0f)
        assertTrue(s.shots.isEmpty())
    }
}
