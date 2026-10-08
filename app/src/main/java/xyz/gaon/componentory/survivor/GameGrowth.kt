package xyz.gaon.componentory.survivor

internal enum class UpgradeKind {
    WEAPON,
    SUPPORT,
    EVOLUTION,
    HEAL,
    CURRENCY,
    RECOVERY,
}

internal data class UpgradeChoice(
    val kind: UpgradeKind,
    val weapon: WeaponId? = null,
    val support: SupportId? = null,
) {
    val key
        get() = kind.name + "_" + (weapon?.name ?: support?.name ?: "")
}

/** Growth draws have their own random stream so choices do not move enemy spawn positions. */
internal object GameGrowth {
    fun available(s: GameSession): List<UpgradeChoice> = buildList {
        for (id in WeaponId.entries) {
            val weapon = s.weapons.firstOrNull { it.id == id }
            if (
                weapon == null && s.weapons.size < GameCatalog.SLOT_LIMIT ||
                    weapon != null && !weapon.evolved && weapon.level < GameCatalog.MAX_LEVEL
            )
                add(UpgradeChoice(UpgradeKind.WEAPON, weapon = id))
            if (
                weapon != null &&
                    !weapon.evolved &&
                    weapon.level == 5 &&
                    (s.supports[GameCatalog.evolutionSupport(id)] ?: 0) >= 3
            )
                add(UpgradeChoice(UpgradeKind.EVOLUTION, weapon = id))
        }
        for (id in s.unlocked) {
            val level = s.supports[id] ?: 0
            if (level < 5 && (level > 0 || s.supports.size < GameCatalog.SLOT_LIMIT))
                add(UpgradeChoice(UpgradeKind.SUPPORT, support = id))
        }
    }

    fun offer(s: GameSession) {
        if (
            s.outcome != RunOutcome.ACTIVE ||
                s.choices.isNotEmpty() ||
                s.experience < s.requiredExperience
        )
            return
        val available = available(s).toMutableList()
        val evolution = available.filter { it.kind == UpgradeKind.EVOLUTION }.take(3)
        s.choices.addAll(evolution)
        available.removeAll(evolution.toSet())
        while (s.choices.size < 3 && available.isNotEmpty()) {
            var value = s.offerRandomState
            value = value xor (value shl 13)
            value = value xor (value ushr 7)
            value = value xor (value shl 17)
            s.offerRandomState = value
            val index = ((value ushr 40) % available.size).toInt()
            s.choices += available.removeAt(index)
        }
        for (kind in listOf(UpgradeKind.HEAL, UpgradeKind.CURRENCY, UpgradeKind.RECOVERY)) {
            if (s.choices.size == 3) break
            s.choices += UpgradeChoice(kind)
        }
    }

    fun choose(s: GameSession, choice: UpgradeChoice): Boolean {
        if (s.outcome != RunOutcome.ACTIVE || choice !in s.choices) return false
        // Recheck equipment conditions before applying an offer restored from disk.
        if (
            choice.kind in listOf(UpgradeKind.WEAPON, UpgradeKind.SUPPORT, UpgradeKind.EVOLUTION) &&
                choice !in available(s)
        )
            return false
        s.experience -= s.requiredExperience
        s.level++
        when (choice.kind) {
            UpgradeKind.WEAPON -> {
                val id = requireNotNull(choice.weapon)
                val existing = s.weapons.firstOrNull { it.id == id }
                if (existing == null) s.weapons += GameWeapon(id) else existing.level++
            }
            UpgradeKind.SUPPORT -> {
                val id = requireNotNull(choice.support)
                s.supports[id] = (s.supports[id] ?: 0) + 1
            }
            UpgradeKind.EVOLUTION ->
                s.weapons
                    .first { it.id == choice.weapon }
                    .apply {
                        evolved = true
                        ticks = 0
                    }
            UpgradeKind.HEAL -> s.health = (s.health + s.maxHealth * .25f).coerceAtMost(s.maxHealth)
            UpgradeKind.CURRENCY -> s.bonusCurrency += 10
            UpgradeKind.RECOVERY -> {
                s.health = (s.health + s.maxHealth * .125f).coerceAtMost(s.maxHealth)
                s.bonusCurrency += 5
            }
        }
        s.choices.clear()
        offer(s)
        return true
    }
}
