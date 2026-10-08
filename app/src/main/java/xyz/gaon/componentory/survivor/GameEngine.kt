package xyz.gaon.componentory.survivor

import kotlin.math.*

/** Fixed ticks keep combat independent of rendering speed and Android version. */
internal class GameEngine(val session: GameSession) {
    private val s
        get() = session

    fun step(input: GameInput = GameInput()) {
        if (s.outcome != RunOutcome.ACTIVE) return
        s.tick++
        s.skillTicks = (s.skillTicks - 1).coerceAtLeast(0)
        s.shieldTicks = (s.shieldTicks - 1).coerceAtLeast(0)
        s.freezeTicks = (s.freezeTicks - 1).coerceAtLeast(0)
        s.hurtTicks = (s.hurtTicks - 1).coerceAtLeast(0)
        val ix = input.x.takeIf { it.isFinite() } ?: 0f
        val iy = input.y.takeIf { it.isFinite() } ?: 0f
        val length = sqrt(ix * ix + iy * iy).coerceAtLeast(1f)
        s.x = (s.x + ix / length * 200f / TICKS_PER_SECOND).coerceIn(24f, WIDTH - 24f)
        s.y = (s.y + iy / length * 200f / TICKS_PER_SECOND).coerceIn(24f, HEIGHT - 24f)
        if (input.skill && s.skillTicks == 0) useSkill()
        if (--s.spawnTicks <= 0) {
            s.spawnTicks = (45 - s.seconds / 40).coerceAtLeast(10)
            if (s.enemies.size < ENEMY_LIMIT) spawnEnemy()
        }
        for (enemy in s.enemies) {
            if (enemy.health <= 0f) continue
            val dx = s.x - enemy.x
            val dy = s.y - enemy.y
            val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val speed = if (s.freezeTicks > 0) 0f else 45f + min(s.seconds / 60f, 20f) * 2
            enemy.x += dx / distance * speed / TICKS_PER_SECOND
            enemy.y += dy / distance * speed / TICKS_PER_SECOND
            if (distance < 30f && s.shieldTicks == 0 && s.hurtTicks == 0) {
                s.health -= if (enemy.kind == EnemyKind.BOSS) 18f else 8f
                s.hurtTicks = 45
                if (s.health <= 0) s.outcome = RunOutcome.DEFEATED
            }
        }
        for (weapon in s.weapons) {
            if (weapon.ticks > 0) weapon.ticks--
            else
                closest(s.x, s.y)?.let { target ->
                    attack(weapon, target)
                    weapon.ticks =
                        when (weapon.id) {
                            WeaponId.BUTTON -> 24
                            WeaponId.SLIDER -> 48
                            WeaponId.SWITCH -> 60
                            WeaponId.SPINNER -> 45
                        }
                }
        }
        updateShots()
        collectDeaths()
        val drops = s.drops.iterator()
        while (drops.hasNext()) {
            val drop = drops.next()
            if (distanceSquared(drop.x, drop.y, s.x, s.y) < 80f * 80f) {
                s.experience += (drop.amount * (1f + s.permanent.experience * 0.05f)).roundToInt()
                drops.remove()
            }
        }
    }

    private fun attack(weapon: GameWeapon, target: GameEnemy) {
        val multiplier = (1f + (weapon.level - 1) * 0.3f) * s.damageMultiplier
        when (weapon.id) {
            WeaponId.BUTTON -> fireAt(target, 12f * multiplier, "button")
            WeaponId.SLIDER -> fireAt(target, 24f * multiplier, "slider", pierce = 3)
            WeaponId.SWITCH -> {
                addShot(
                    GameShot(
                        s.nextId++,
                        s.x,
                        s.y,
                        0f,
                        0f,
                        30f * multiplier,
                        8,
                        "switch",
                        pierce = ENEMY_LIMIT,
                        radius = 170f,
                    )
                )
            }
            WeaponId.SPINNER ->
                repeat(3) { index ->
                    addShot(
                        GameShot(
                            s.nextId++,
                            s.x,
                            s.y,
                            0f,
                            0f,
                            7.5f * multiplier,
                            45,
                            "spinner",
                            pierce = ENEMY_LIMIT,
                            orbit = true,
                            angle = index * 2f * PI.toFloat() / 3,
                        )
                    )
                }
        }
    }

    private fun useSkill() {
        s.skillTicks = 20 * TICKS_PER_SECOND
        when (GameCatalog.character(s.api).family.skill) {
            SkillKind.BURST ->
                repeat(12) { index ->
                    val angle = index * PI.toFloat() / 6
                    addShot(
                        GameShot(
                            s.nextId++,
                            s.x,
                            s.y,
                            cos(angle) * 500,
                            sin(angle) * 500,
                            15f * s.damageMultiplier,
                            90,
                            GameCatalog.character(s.api).family.art,
                            pierce = 2,
                        )
                    )
                }
            SkillKind.FREEZE -> s.freezeTicks = 4 * TICKS_PER_SECOND
            SkillKind.SHIELD -> s.shieldTicks = 4 * TICKS_PER_SECOND
            SkillKind.SUMMON ->
                closest(s.x, s.y)?.let { target ->
                    repeat(8) { fireAt(target, 15f * s.damageMultiplier, "neko", homing = true) }
                }
        }
    }

    private fun fireAt(
        target: GameEnemy,
        damage: Float,
        art: String,
        pierce: Int = 0,
        homing: Boolean = false,
    ) {
        val dx = target.x - s.x
        val dy = target.y - s.y
        val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        addShot(
            GameShot(
                s.nextId++,
                s.x,
                s.y,
                dx / distance * 650,
                dy / distance * 650,
                damage,
                if (homing) 300 else 120,
                art,
                pierce = pierce,
                homing = homing,
            )
        )
    }

    private fun addShot(shot: GameShot) {
        if (s.shots.size < SHOT_LIMIT) s.shots.add(shot)
    }

    private fun updateShots() {
        val shots = s.shots.iterator()
        while (shots.hasNext()) {
            val shot = shots.next()
            if (shot.delay > 0) {
                shot.delay--
                continue
            }
            if (shot.orbit) {
                shot.angle += 0.07f
                shot.x = s.x + cos(shot.angle) * 115
                shot.y = s.y + sin(shot.angle) * 115
            } else {
                if (shot.homing)
                    closest(shot.x, shot.y)?.let { target ->
                        val dx = target.x - shot.x
                        val dy = target.y - shot.y
                        val length = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                        shot.vx = dx / length * 400
                        shot.vy = dy / length * 400
                    }
                shot.x += shot.vx / TICKS_PER_SECOND
                shot.y += shot.vy / TICKS_PER_SECOND
            }
            for (enemy in s.enemies) {
                if (enemy.health <= 0f || enemy.id in shot.hits) continue
                val radius = shot.radius + if (enemy.kind == EnemyKind.BOSS) 40f else 18f
                if (distanceSquared(shot.x, shot.y, enemy.x, enemy.y) < radius * radius) {
                    enemy.health -= shot.damage
                    shot.hits += enemy.id
                    if (shot.pierce-- <= 0) {
                        shot.life = 0
                        break
                    }
                }
            }
            if (--shot.life <= 0 || shot.x !in -100f..WIDTH + 100 || shot.y !in -100f..HEIGHT + 100)
                shots.remove()
        }
    }

    private fun collectDeaths() {
        val enemies = s.enemies.iterator()
        while (enemies.hasNext()) {
            val enemy = enemies.next()
            if (enemy.health > 0) continue
            when (enemy.kind) {
                EnemyKind.NORMAL -> s.regularKills++
                EnemyKind.ELITE -> s.eliteKills++
                EnemyKind.BOSS -> s.bossKills++
            }
            val amount =
                when (enemy.kind) {
                    EnemyKind.NORMAL -> 2
                    EnemyKind.ELITE -> 12
                    EnemyKind.BOSS -> 80
                }
            if (s.drops.size < DROP_LIMIT) s.drops.add(ExperienceDrop(enemy.x, enemy.y, amount))
            else
                s.drops
                    .minByOrNull { distanceSquared(it.x, it.y, enemy.x, enemy.y) }
                    ?.let { it.amount += amount }
            enemies.remove()
        }
    }

    private fun spawnEnemy() {
        val side = (random() * 4).toInt()
        val x =
            when (side) {
                0 -> 0f
                1 -> WIDTH
                else -> random() * WIDTH
            }
        val y =
            when (side) {
                2 -> 0f
                3 -> HEIGHT
                else -> random() * HEIGHT
            }
        val health = 18f + s.seconds / 30f
        s.enemies += GameEnemy(s.nextId++, x, y, health, health)
    }

    private fun closest(x: Float, y: Float) =
        s.enemies
            .asSequence()
            .filter { it.health > 0 }
            .minByOrNull { distanceSquared(x, y, it.x, it.y) }

    private fun random(): Float {
        var value = s.randomState
        value = value xor (value shl 13)
        value = value xor (value ushr 7)
        value = value xor (value shl 17)
        s.randomState = value
        return ((value ushr 40).toDouble() / 16777216.0).toFloat()
    }

    companion object {
        const val WIDTH = 1600f
        const val HEIGHT = 900f
        const val TICKS_PER_SECOND = 60
        const val ENEMY_LIMIT = 160
        const val SHOT_LIMIT = 256
        const val DROP_LIMIT = 256

        fun create(
            api: Int,
            mode: RunMode,
            permanent: PermanentLevels = PermanentLevels(),
            unlocked: Set<SupportId> = setOf(SupportId.PROGRESS),
            seed: Long = System.nanoTime(),
        ): GameEngine {
            GameCatalog.character(api)
            return GameEngine(
                GameSession(
                    api = api,
                    mode = mode,
                    seed = if (mode == RunMode.RANKED) GameCatalog.RANKED_SEED else seed,
                    permanent = if (mode == RunMode.RANKED) PermanentLevels() else permanent,
                    unlocked = if (mode == RunMode.RANKED) SupportId.entries.toSet() else unlocked,
                )
            )
        }

        fun distanceSquared(x: Float, y: Float, tx: Float, ty: Float): Float {
            val dx = tx - x
            val dy = ty - y
            return dx * dx + dy * dy
        }
    }
}
