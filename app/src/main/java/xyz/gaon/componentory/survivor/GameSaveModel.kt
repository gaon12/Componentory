package xyz.gaon.componentory.survivor

internal enum class PermanentUpgrade {
    HEALTH,
    DAMAGE,
    EXPERIENCE,
}

internal data class GameProgress(
    val currency: Long = 0,
    val permanent: PermanentLevels = PermanentLevels(),
    val unlocked: Set<SupportId> = setOf(SupportId.PROGRESS),
) {
    init {
        require(currency >= 0)
    }

    fun level(upgrade: PermanentUpgrade) =
        when (upgrade) {
            PermanentUpgrade.HEALTH -> permanent.health
            PermanentUpgrade.DAMAGE -> permanent.damage
            PermanentUpgrade.EXPERIENCE -> permanent.experience
        }

    fun price(upgrade: PermanentUpgrade) = 50L * (level(upgrade) + 1)

    fun buy(upgrade: PermanentUpgrade): GameProgress? {
        val level = level(upgrade)
        if (level >= 5 || currency < price(upgrade)) return null
        val levels =
            when (upgrade) {
                PermanentUpgrade.HEALTH -> permanent.copy(health = level + 1)
                PermanentUpgrade.DAMAGE -> permanent.copy(damage = level + 1)
                PermanentUpgrade.EXPERIENCE -> permanent.copy(experience = level + 1)
            }
        return copy(currency = currency - price(upgrade), permanent = levels)
    }

    fun unlock(support: SupportId): GameProgress? {
        if (support in unlocked || currency < UNLOCK_PRICE) return null
        return copy(currency = currency - UNLOCK_PRICE, unlocked = unlocked + support)
    }

    companion object {
        const val UNLOCK_PRICE = 100L
    }
}

internal data class FinalWeapon(val id: WeaponId, val level: Int, val evolved: Boolean)

internal data class GameRunRecord(
    val id: String,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long,
    val playerArt: String,
    val startingWeapon: WeaponId,
    val mode: RunMode,
    val ruleset: String,
    val seed: Long,
    val permanent: PermanentLevels,
    val weapons: List<FinalWeapon>,
    val supports: Map<SupportId, Int>,
    val survivalTicks: Int,
    val regularKills: Int,
    val eliteKills: Int,
    val bossKills: Int,
    val outcome: RunOutcome,
    val score: Long,
    val currency: Long,
) {
    val seconds
        get() = survivalTicks / 60

    val kills
        get() = regularKills + eliteKills + bossKills
}

internal enum class SubmissionStatus {
    QUEUED,
    SENT,
}

internal data class GameSubmission(
    val runId: String,
    val ruleset: String,
    val score: Long,
    val profileId: String?,
    val status: SubmissionStatus = SubmissionStatus.QUEUED,
    val attempts: Int = 0,
)

/** One atomic document makes completion, the reward, and its pending score a single change. */
internal data class GameSave(
    val progress: GameProgress = GameProgress(),
    val activeId: String? = null,
    val activeJson: String? = null,
    val records: List<GameRunRecord> = emptyList(),
    val submissions: List<GameSubmission> = emptyList(),
)

internal object GameLedger {
    fun finish(
        save: GameSave,
        s: GameSession,
        completedAt: Long = System.currentTimeMillis(),
    ): GameSave {
        require(s.outcome != RunOutcome.ACTIVE)
        if (save.records.any { it.id == s.id }) {
            return if (save.activeId == s.id) save.copy(activeId = null, activeJson = null)
            else save
        }
        val reward =
            s.regularKills / 10L +
                s.eliteKills +
                s.bossKills * 10L +
                minOf(s.seconds, 1200) / 30L +
                (if (s.outcome == RunOutcome.WON) 50L else 0L) +
                s.bonusCurrency
        val score =
            s.regularKills * 10L +
                s.eliteKills * 100L +
                s.bossKills * 1000L +
                minOf(s.seconds, 1200) * 5L +
                (if (s.outcome == RunOutcome.WON) 10000L else 0L)
        val record =
            GameRunRecord(
                s.id,
                s.startedAtEpochMillis,
                completedAt,
                GameCatalog.PLAYER_ART,
                s.startingWeapon,
                s.mode,
                s.ruleset,
                s.seed,
                s.permanent,
                s.weapons.map { FinalWeapon(it.id, it.level, it.evolved) },
                s.supports.toMap(),
                s.tick,
                s.regularKills,
                s.eliteKills,
                s.bossKills,
                s.outcome,
                score,
                reward,
            )
        val eligible =
            s.mode == RunMode.RANKED && s.outcome in setOf(RunOutcome.WON, RunOutcome.DEFEATED)
        return save.copy(
            progress = save.progress.copy(currency = save.progress.currency + reward),
            activeId = if (save.activeId == s.id) null else save.activeId,
            activeJson = if (save.activeId == s.id) null else save.activeJson,
            records = save.records + record,
            submissions =
                save.submissions +
                    if (eligible) listOf(GameSubmission(s.id, s.ruleset, score, s.rankedProfileId))
                    else emptyList(),
        )
    }
}
