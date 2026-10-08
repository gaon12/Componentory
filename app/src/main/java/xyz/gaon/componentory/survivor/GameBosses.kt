package xyz.gaon.componentory.survivor

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Source-themed attacks use timed boss roles, never an Android API power tier. */
internal object GameBosses {
    fun shoot(s: GameSession, enemy: GameEnemy, emit: (GameShot) -> Unit) {
        val aim = atan2(s.y - enemy.y, s.x - enemy.x)
        val count =
            when (enemy.bossStage) {
                1 -> 8
                2 -> 5
                3 -> 6
                else -> 4
            }
        enemy.attackTicks =
            when (enemy.bossStage) {
                1 -> 150
                2 -> 150
                else -> 90
            }
        repeat(count) { index ->
            val angle =
                when (enemy.bossStage) {
                    1 -> index * PI.toFloat() / 4 + s.tick * .01f
                    2 -> aim + (index - 2) * .18f
                    3 -> index * PI.toFloat() / 3 + s.tick * .035f
                    else -> aim + (index - 1.5f) * .3f
                }
            val speed =
                when (enemy.bossStage) {
                    1 -> 180f
                    2 -> 230f
                    3 -> 210f
                    else -> 120f
                }
            emit(
                GameShot(
                    s.nextId++,
                    enemy.x,
                    enemy.y,
                    cos(angle) * speed,
                    sin(angle) * speed,
                    if (enemy.bossStage == 4) 10f else 12f,
                    300,
                    enemy.source.art,
                    hostile = true,
                    homing = enemy.bossStage == 4,
                    bounces = if (enemy.bossStage == 2) 2 else 0,
                )
            )
        }
    }
}

/** Separate nearby bodies so a pursuing crowd does not collapse into one sprite. */
internal object GameCrowd {
    fun separate(enemies: List<GameEnemy>) {
        for (first in enemies.indices) for (second in 0 until first) {
            val a = enemies[first]
            val b = enemies[second]
            if (a.health <= 0 || b.health <= 0) continue
            var dx = a.x - b.x
            var dy = a.y - b.y
            val radius =
                (if (a.kind == EnemyKind.BOSS) 50f else 18f) +
                    (if (b.kind == EnemyKind.BOSS) 50f else 18f)
            val squared = dx * dx + dy * dy
            if (squared >= radius * radius) continue
            if (squared < .001f) {
                dx = if (a.id % 2 == 0) 1f else -1f
                dy = if (b.id % 2 == 0) .5f else -.5f
            }
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            val push = (radius - distance) * .5f
            val px = dx / distance * push
            val py = dy / distance * push
            a.x = (a.x + px).coerceIn(0f, GameEngine.WIDTH)
            a.y = (a.y + py).coerceIn(0f, GameEngine.HEIGHT)
            b.x = (b.x - px).coerceIn(0f, GameEngine.WIDTH)
            b.y = (b.y - py).coerceIn(0f, GameEngine.HEIGHT)
        }
    }
}
