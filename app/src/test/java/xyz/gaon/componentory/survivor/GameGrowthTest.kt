package xyz.gaon.componentory.survivor

import org.junit.Assert.*
import org.junit.Test

class GameGrowthTest {
    @Test
    fun experiencePausesTicksUntilOneOfThreeDistinctOffersIsChosen() {
        val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        e.session.experience = e.session.requiredExperience
        e.step()
        assertEquals(3, e.session.choices.distinct().size)
        val tick = e.session.tick
        repeat(60) { e.step(GameInput(x = 1f, skill = true)) }
        assertEquals(tick, e.session.tick)
        assertFalse(
            GameGrowth.choose(e.session, UpgradeChoice(UpgradeKind.EVOLUTION, WeaponId.BUTTON))
        )
        assertTrue(GameGrowth.choose(e.session, e.session.choices.first()))
        e.step()
        assertEquals(tick + 1, e.session.tick)
        assertEquals(2, e.session.level)
    }

    @Test
    fun lockedSupportsAndMaximumLevelsAreNeverOffered() {
        val s = GameEngine.create(WeaponId.BUTTON, RunMode.NORMAL).session
        s.weapons.clear()
        WeaponId.entries.forEach { s.weapons += GameWeapon(it, 5, true) }
        s.supports[SupportId.PROGRESS] = 5
        s.experience = s.requiredExperience
        s.health = s.maxHealth - 1
        GameGrowth.offer(s)
        assertEquals(
            setOf(UpgradeKind.HEAL, UpgradeKind.CURRENCY, UpgradeKind.RECOVERY),
            s.choices.map { it.kind }.toSet(),
        )
        GameGrowth.choose(s, s.choices.first { it.kind == UpgradeKind.HEAL })
        assertEquals(s.maxHealth, s.health, 0f)
        assertEquals(4, s.weapons.size)
        assertEquals(1, s.supports.size)
    }

    @Test
    fun eachEvolutionNeedsWeaponFiveAndMatchingSupportThreeAndKeepsItsSlot() {
        for (id in WeaponId.entries) {
            val s = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED).session
            s.weapons.clear()
            WeaponId.entries.forEach { s.weapons += GameWeapon(it, 5) }
            val weapon = s.weapons.first { it.id == id }
            s.supports[GameCatalog.evolutionSupport(id)] = 2
            assertFalse(
                GameGrowth.available(s).any { it.kind == UpgradeKind.EVOLUTION && it.weapon == id }
            )
            s.supports[GameCatalog.evolutionSupport(id)] = 3
            weapon.level = 4
            assertFalse(
                GameGrowth.available(s).any { it.kind == UpgradeKind.EVOLUTION && it.weapon == id }
            )
            weapon.level = 5
            s.experience = s.requiredExperience
            GameGrowth.offer(s)
            val choice = s.choices.first { it.kind == UpgradeKind.EVOLUTION && it.weapon == id }
            assertTrue(GameGrowth.choose(s, choice))
            assertEquals(4, s.weapons.size)
            assertTrue(weapon.evolved)
            assertFalse(GameGrowth.available(s).any { it.weapon == id })
            assertFalse(GameGrowth.choose(s, choice))
        }
    }

    @Test
    fun drawingGrowthOffersDoesNotChangeEnemySpawnRandomness() {
        val a = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        val b = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
        a.session.experience = a.session.requiredExperience
        GameGrowth.offer(a.session)
        a.session.choices.clear()
        a.session.experience = 0
        repeat(60) {
            a.step()
            b.step()
        }
        assertEquals(b.session.randomState, a.session.randomState)
        assertEquals(b.session.enemies, a.session.enemies)
    }

    @Test
    fun smallExperienceDropsKeepTheFractionalFivePercentBonus() {
        val e =
            GameEngine.create(
                WeaponId.BUTTON,
                RunMode.NORMAL,
                permanent = PermanentLevels(experience = 1),
            )
        repeat(10) { e.session.drops += ExperienceDrop(800f, 450f, 2) }
        e.step()
        assertEquals(21, e.session.experience)
        assertEquals(0.0, e.session.experienceRemainder, 1e-8)
    }

    @Test
    fun theFourEvolutionsProduceTheirDistinctCombatActions() {
        for (id in WeaponId.entries) {
            val e = GameEngine.create(WeaponId.BUTTON, RunMode.RANKED)
            val s = e.session
            s.weapons.clear()
            s.weapons += GameWeapon(id, 5, true)
            s.enemies += GameEnemy(100, 1000f, 450f, 10000f, 10000f)
            e.step()
            when (id) {
                WeaponId.BUTTON -> assertTrue(s.shots.any { it.delay > 0 })
                WeaponId.SLIDER -> assertTrue(s.shots.any { it.bounces == 5 && it.pierce == 8 })
                WeaponId.SWITCH -> {
                    assertEquals(4, s.shots.count { it.homing && it.art == "neko" })
                    assertTrue(s.shieldTicks > 0)
                }
                WeaponId.SPINNER ->
                    assertEquals(6, s.shots.count { it.orbit && it.art == "octopus" })
            }
        }
    }
}
