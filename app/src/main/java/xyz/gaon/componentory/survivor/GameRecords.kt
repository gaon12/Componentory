package xyz.gaon.componentory.survivor

internal object GameScore {
    fun calculate(regular: Int, elite: Int, bosses: Int, seconds: Int, outcome: RunOutcome): Long {
        require(listOf(regular, elite, bosses, seconds).all { it >= 0 })
        return regular * 10L +
            elite * 100L +
            bosses * 1000L +
            minOf(seconds, GameCatalog.RUN_SECONDS) * 5L +
            if (outcome == RunOutcome.WON) 10000L else 0L
    }
}

internal object GameRecords {
    fun filter(
        records: List<GameRunRecord>,
        mode: RunMode,
        ruleset: String,
        weapon: WeaponId? = null,
    ) =
        records
            .filter {
                it.mode == mode &&
                    it.ruleset == ruleset &&
                    (weapon == null || it.startingWeapon == weapon)
            }
            .sortedByDescending { it.completedAtEpochMillis }

    fun best(records: List<GameRunRecord>) =
        records
            .filter { it.outcome in setOf(RunOutcome.WON, RunOutcome.DEFEATED) }
            .maxWithOrNull(
                compareBy<GameRunRecord> { it.score }
                    .thenBy { it.seconds }
                    .thenBy { it.completedAtEpochMillis }
            )

    fun comparable(first: GameRunRecord, second: GameRunRecord) =
        first.id != second.id && first.mode == second.mode && first.ruleset == second.ruleset
}
