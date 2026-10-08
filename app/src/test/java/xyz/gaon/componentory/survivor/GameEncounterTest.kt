package xyz.gaon.componentory.survivor

import org.junit.Assert.*
import org.junit.Test

class GameEncounterTest {
    @Test
    fun fourSourceThemedBossesHaveDistinctAttacks() {
        val s = GameEngine.create(WeaponId.BUTTON, RunMode.NORMAL).session
        val volleys =
            (1..4).map { stage ->
                val shots = mutableListOf<GameShot>()
                val enemy =
                    GameEnemy(
                        s.nextId++,
                        800f,
                        0f,
                        100f,
                        100f,
                        EnemyKind.BOSS,
                        stage,
                        source =
                            listOf(
                                GameFamily.OREO,
                                GameFamily.JELLY_BEAN,
                                GameFamily.KITKAT,
                                GameFamily.HONEYCOMB,
                            )[stage - 1],
                    )
                GameBosses.shoot(s, enemy, shots::add)
                assertTrue(shots.all { it.hostile && it.art == enemy.source.art })
                shots
            }
        assertEquals(listOf(8, 5, 6, 4), volleys.map { it.size })
        assertTrue(volleys[1].all { it.bounces == 2 && !it.homing })
        assertTrue(volleys[3].all { it.homing })
    }

    @Test
    fun sourceAgeCannotChangeCrowdSeparationOrBaseCombatPower() {
        val old =
            listOf(
                GameEnemy(1, 800f, 450f, 20f, 20f, source = GameFamily.CLASSIC),
                GameEnemy(2, 800f, 450f, 20f, 20f, source = GameFamily.HONEYCOMB),
            )
        val recent = old.map { it.copy(source = GameFamily.CINNAMON) }
        GameCrowd.separate(old)
        GameCrowd.separate(recent)
        old.zip(recent).forEach { (a, b) ->
            assertEquals(a.x, b.x)
            assertEquals(a.y, b.y)
            assertEquals(a.health, b.health)
        }
        assertTrue(GameEngine.distanceSquared(old[0].x, old[0].y, old[1].x, old[1].y) > 100f)
        WeaponId.entries.forEach {
            assertEquals(100f, GameEngine.create(it, RunMode.NORMAL).session.maxHealth)
        }
    }

    @Test
    fun hostileHomingAimsAtThePlayerAndJellyShotsBounceWithinTheArena() {
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.NORMAL)
        val s = e.session
        s.weapons.clear()
        s.shieldTicks = 100
        s.enemies += GameEnemy(s.nextId++, 100f, 450f, 100f, 100f)
        val homing =
            GameShot(
                s.nextId++,
                500f,
                450f,
                0f,
                120f,
                10f,
                100,
                "honeycomb",
                hostile = true,
                homing = true,
            )
        val bounce =
            GameShot(
                s.nextId++,
                1f,
                100f,
                -230f,
                0f,
                10f,
                100,
                "jellybean",
                hostile = true,
                bounces = 2,
            )
        s.shots.addAll(listOf(homing, bounce))
        e.step()
        assertTrue(homing.vx > 0)
        assertTrue(bounce.vx > 0 && bounce.x >= 0)
        assertEquals(1, bounce.bounces)
        assertEquals(100f, s.enemies.single().health)
    }
}
