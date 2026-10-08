package xyz.gaon.componentory.survivor

import java.util.UUID

internal enum class RunMode {
    NORMAL,
    RANKED,
}

internal enum class RunOutcome {
    ACTIVE,
    WON,
    DEFEATED,
    ABANDONED,
}

internal enum class EnemyKind {
    NORMAL,
    ELITE,
    BOSS,
}

internal data class PermanentLevels(
    val health: Int = 0,
    val damage: Int = 0,
    val experience: Int = 0,
) {
    init {
        require(listOf(health, damage, experience).all { it in 0..5 })
    }
}

internal data class GameInput(val x: Float = 0f, val y: Float = 0f, val skill: Boolean = false)

internal data class GameEnemy(
    val id: Int,
    var x: Float,
    var y: Float,
    var health: Float,
    val maxHealth: Float,
    val kind: EnemyKind = EnemyKind.NORMAL,
    val bossStage: Int = 0,
    var attackTicks: Int = 180,
    val source: GameFamily = GameFamily.JELLY_BEAN,
)

internal data class GameShot(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val damage: Float,
    var life: Int,
    val art: String,
    var pierce: Int = 0,
    val radius: Float = 12f,
    val orbit: Boolean = false,
    val homing: Boolean = false,
    val hostile: Boolean = false,
    var angle: Float = 0f,
    var delay: Int = 0,
    var bounces: Int = 0,
    val hits: MutableSet<Int> = mutableSetOf(),
)

internal data class ExperienceDrop(var x: Float, var y: Float, var amount: Int)

internal data class GameWeapon(
    val id: WeaponId,
    var level: Int = 1,
    var evolved: Boolean = false,
    var ticks: Int = 0,
)

internal class GameSession(
    val id: String = UUID.randomUUID().toString(),
    val startingWeapon: WeaponId,
    val mode: RunMode,
    val seed: Long,
    val permanent: PermanentLevels,
    val unlocked: Set<SupportId>,
    val ruleset: String = GameCatalog.RULESET,
) {
    var randomState = if (seed == 0L) 1L else seed
    var tick = 0
    var nextId = 1
    var x = GameEngine.WIDTH / 2
    var y = GameEngine.HEIGHT / 2
    val maxHealth = 100f * (1 + permanent.health * 0.05f)
    var health = maxHealth
    var experience = 0
    var level = 1
    var skillTicks = 0
    var shieldTicks = 0
    var freezeTicks = 0
    var hurtTicks = 0
    var spawnTicks = 45
    var regularKills = 0
    var eliteKills = 0
    var bossKills = 0
    var bonusCurrency = 0
    var outcome = RunOutcome.ACTIVE
    val weapons = mutableListOf(GameWeapon(startingWeapon))
    val supports = mutableMapOf<SupportId, Int>()
    val enemies = mutableListOf<GameEnemy>()
    val shots = mutableListOf<GameShot>()
    val drops = mutableListOf<ExperienceDrop>()
    val spawnedBosses = mutableSetOf<Int>()
    val seconds
        get() = tick / GameEngine.TICKS_PER_SECOND

    val damageMultiplier
        get() = 1f + permanent.damage * 0.05f

    val requiredExperience
        get() = 8 + level * 4
}
