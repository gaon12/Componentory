package xyz.gaon.componentory.survivor

import kotlin.math.*

/** Fixed ticks keep combat independent of rendering speed and Android version. */
internal class GameEngine(val session: GameSession) {
    private val s
        get() = session

    fun step(input: GameInput = GameInput()) {
        if (s.outcome != RunOutcome.ACTIVE || s.choices.isNotEmpty()) return
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
        GameWaves.update(s, ::random)
        for (enemy in s.enemies) {
            if (enemy.health <= 0f) continue
            val dx = s.x - enemy.x
            val dy = s.y - enemy.y
            val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val speed = if (s.freezeTicks > 0) 0f else 45f + min(s.seconds / 60f, 20f) * 2
            enemy.x += dx / distance * speed / TICKS_PER_SECOND
            enemy.y += dy / distance * speed / TICKS_PER_SECOND
            if (s.freezeTicks == 0) {
                if (distance < if (enemy.kind == EnemyKind.BOSS) 70f else 30f)
                    hurt(if (enemy.kind == EnemyKind.BOSS) 18f else 8f)
                if (enemy.kind == EnemyKind.BOSS && --enemy.attackTicks <= 0) {
                    enemy.attackTicks = 180 - enemy.bossStage * 15
                    repeat(8) { index ->
                        val angle = index * PI.toFloat() / 4 + s.tick * .01f
                        val speed = 140f + enemy.bossStage * 20f
                        addShot(
                            GameShot(
                                s.nextId++,
                                enemy.x,
                                enemy.y,
                                cos(angle) * speed,
                                sin(angle) * speed,
                                12f,
                                360,
                                enemy.source.art,
                                hostile = true,
                            )
                        )
                    }
                }
            }
        }
        for (weapon in s.weapons) {
            if (weapon.ticks > 0) weapon.ticks--
            else
                closest(s.x, s.y)?.let { target ->
                    attack(weapon, target)
                    weapon.ticks =
                        (when (weapon.id) {
                                WeaponId.BUTTON -> if (weapon.evolved) 72 else 24
                                WeaponId.SLIDER -> 48
                                WeaponId.SWITCH -> if (weapon.evolved) 90 else 60
                                WeaponId.SPINNER -> 45
                            } * (1f - (s.supports[SupportId.PROGRESS] ?: 0) * .05f))
                            .roundToInt()
                }
        }
        updateShots()
        collectDeaths()
        val drops = s.drops.iterator()
        while (drops.hasNext()) {
            val drop = drops.next()
            if (distanceSquared(drop.x, drop.y, s.x, s.y) < 80f * 80f) {
                val earned =
                    drop.amount *
                        (1.0 +
                            s.permanent.experience * .05 +
                            (s.supports[SupportId.NEKO] ?: 0) * .05) + s.experienceRemainder
                val whole = floor(earned + 1e-9).toInt()
                s.experience += whole
                s.experienceRemainder = (earned - whole).coerceAtLeast(0.0)
                drops.remove()
            }
        }
        GameGrowth.offer(s)
    }

    private fun attack(weapon: GameWeapon, target: GameEnemy) {
        val multiplier =
            (1f + (weapon.level - 1) * 0.3f) *
                s.damageMultiplier *
                (1f + (s.supports[SupportId.JELLY_BEAN] ?: 0) * .05f)
        when (weapon.id) {
            WeaponId.BUTTON -> {
                if (weapon.evolved)
                    repeat(6) { index ->
                        fireAt(target, 16f * multiplier, "button", delay = index * 6)
                    }
                else fireAt(target, 12f * multiplier, "button")
            }
            WeaponId.SLIDER ->
                fireAt(
                    target,
                    24f * multiplier,
                    "slider",
                    pierce = if (weapon.evolved) 8 else 3,
                    bounces = if (weapon.evolved) 5 else 0,
                )
            WeaponId.SWITCH -> {
                if (weapon.evolved) {
                    repeat(4) { fireAt(target, 25f * multiplier, "neko", homing = true) }
                    s.shieldTicks = maxOf(s.shieldTicks, 30)
                }
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
                        radius = 170f * (1f + (s.supports[SupportId.OCTOPUS] ?: 0) * .05f),
                    )
                )
            }
            WeaponId.SPINNER ->
                repeat(if (weapon.evolved) 6 else 3) { index ->
                    addShot(
                        GameShot(
                            s.nextId++,
                            s.x,
                            s.y,
                            0f,
                            0f,
                            (if (weapon.evolved) 12f else 7.5f) * multiplier,
                            45,
                            if (weapon.evolved) "octopus" else "spinner",
                            pierce = ENEMY_LIMIT,
                            orbit = true,
                            angle = index * 2f * PI.toFloat() / (if (weapon.evolved) 6 else 3),
                        )
                    )
                }
        }
    }

    private fun useSkill() {
        s.skillTicks = 20 * TICKS_PER_SECOND
        when (GameCatalog.skill(s.startingWeapon)) {
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
                            "jellybean",
                            pierce = 2,
                        )
                    )
                }
            SkillKind.FREEZE -> s.freezeTicks = 4 * TICKS_PER_SECOND
            SkillKind.SHIELD -> s.shieldTicks = 4 * TICKS_PER_SECOND
            SkillKind.SUMMON -> {
                val target = closest(s.x, s.y) ?: GameEnemy(0, s.x + 300f, s.y, 1f, 1f)
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
        delay: Int = 0,
        bounces: Int = 0,
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
                delay = delay,
                bounces = bounces,
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
                val orbitRadius = 115f * (1f + (s.supports[SupportId.OCTOPUS] ?: 0) * .05f)
                shot.x = s.x + cos(shot.angle) * orbitRadius
                shot.y = s.y + sin(shot.angle) * orbitRadius
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
            if (shot.hostile) {
                val radius = shot.radius + 18f
                if (distanceSquared(shot.x, shot.y, s.x, s.y) < radius * radius) {
                    hurt(shot.damage)
                    shot.life = 0
                }
            } else
                for (enemy in s.enemies) {
                    if (enemy.health <= 0f || enemy.id in shot.hits) continue
                    val radius = shot.radius + if (enemy.kind == EnemyKind.BOSS) 40f else 18f
                    if (distanceSquared(shot.x, shot.y, enemy.x, enemy.y) < radius * radius) {
                        enemy.health -= shot.damage
                        shot.hits += enemy.id
                        if (shot.bounces > 0) {
                            val next =
                                s.enemies
                                    .filter { it.health > 0 && it.id !in shot.hits }
                                    .minByOrNull { distanceSquared(shot.x, shot.y, it.x, it.y) }
                            if (next != null) {
                                val dx = next.x - shot.x
                                val dy = next.y - shot.y
                                val length = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                                shot.vx = dx / length * 650
                                shot.vy = dy / length * 650
                                shot.bounces--
                                shot.pierce--
                                shot.life = 120
                                break
                            }
                        }
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
                EnemyKind.BOSS -> {
                    s.bossKills++
                    if (enemy.bossStage == 4 && s.outcome == RunOutcome.ACTIVE)
                        s.outcome = RunOutcome.WON
                }
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

    private fun hurt(amount: Float) {
        if (s.outcome != RunOutcome.ACTIVE || s.shieldTicks > 0 || s.hurtTicks > 0) return
        s.health = (s.health - amount).coerceAtLeast(0f)
        s.hurtTicks = 45
        if (s.health <= 0f) s.outcome = RunOutcome.DEFEATED
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
            startingWeapon: WeaponId,
            mode: RunMode,
            permanent: PermanentLevels = PermanentLevels(),
            unlocked: Set<SupportId> = setOf(SupportId.PROGRESS),
            seed: Long = System.nanoTime(),
            rankedProfileId: String? = null,
        ): GameEngine {
            return GameEngine(
                GameSession(
                    startingWeapon = startingWeapon,
                    mode = mode,
                    seed = if (mode == RunMode.RANKED) GameCatalog.RANKED_SEED else seed,
                    permanent = if (mode == RunMode.RANKED) PermanentLevels() else permanent,
                    unlocked = if (mode == RunMode.RANKED) SupportId.entries.toSet() else unlocked,
                    rankedProfileId = if (mode == RunMode.RANKED) rankedProfileId else null,
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
