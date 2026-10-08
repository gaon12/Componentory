package xyz.gaon.componentory.survivor

internal enum class WeaponId {
    BUTTON,
    SLIDER,
    SWITCH,
    SPINNER,
}

internal enum class SupportId {
    PROGRESS,
    JELLY_BEAN,
    NEKO,
    OCTOPUS,
}

internal enum class SkillKind {
    BURST,
    FREEZE,
    SHIELD,
    SUMMON,
}

internal enum class GameFamily(
    val art: String,
    val sourceRelease: String,
    val weapon: WeaponId,
    val skill: SkillKind,
) {
    CLASSIC("classic", "Android 2.3.7", WeaponId.BUTTON, SkillKind.SHIELD),
    GINGERBREAD("gingerbread", "Android 2.3", WeaponId.BUTTON, SkillKind.BURST),
    HONEYCOMB("honeycomb", "Android 3.0", WeaponId.SPINNER, SkillKind.SUMMON),
    ICE_CREAM("icecream", "Android 4.0", WeaponId.SLIDER, SkillKind.FREEZE),
    JELLY_BEAN("jellybean", "Android 4.1", WeaponId.BUTTON, SkillKind.BURST),
    KITKAT("kitkat", "Android 4.4", WeaponId.SPINNER, SkillKind.SHIELD),
    WATCH("holo", "Android 4.4.4 controls", WeaponId.SLIDER, SkillKind.FREEZE),
    LOLLIPOP("lollipop", "Android 5.0", WeaponId.BUTTON, SkillKind.BURST),
    MARSHMALLOW("marshmallow", "Android 6.0", WeaponId.BUTTON, SkillKind.SHIELD),
    NOUGAT("neko", "Android 7.0", WeaponId.SWITCH, SkillKind.SUMMON),
    OREO("octopus", "Android 8.0", WeaponId.SPINNER, SkillKind.FREEZE),
    PIE("pie", "Android 9", WeaponId.SLIDER, SkillKind.BURST),
    Q("q", "Android 10", WeaponId.SLIDER, SkillKind.SHIELD),
    R("r", "Android 11", WeaponId.SWITCH, SkillKind.SUMMON),
    S("s", "Android 12", WeaponId.SPINNER, SkillKind.FREEZE),
    T("t", "Android 13", WeaponId.SPINNER, SkillKind.BURST),
    U("u", "Android 14", WeaponId.SLIDER, SkillKind.FREEZE),
    V("v", "Android 15", WeaponId.SLIDER, SkillKind.BURST),
    BAKLAVA("baklava", "Android 16", WeaponId.SLIDER, SkillKind.SHIELD),
    CINNAMON("cinnamon", "Android 17", WeaponId.SLIDER, SkillKind.SUMMON),
}

internal data class GameCharacter(val api: Int, val version: String, val family: GameFamily) {
    val id
        get() = "api-" + api

    val title
        get() = "Android " + version
}

internal object GameCatalog {
    const val RULESET = "survival-v1"
    const val RANKED_SEED = 20261008L
    const val RUN_SECONDS = 1200
    const val SLOT_LIMIT = 4
    const val MAX_LEVEL = 5

    val characters: List<GameCharacter> =
        listOf(
                "1.0",
                "1.1",
                "1.5",
                "1.6",
                "2.0",
                "2.0.1",
                "2.1",
                "2.2",
                "2.3",
                "2.3.3",
                "3.0",
                "3.1",
                "3.2",
                "4.0",
                "4.0.3",
                "4.1",
                "4.2",
                "4.3",
                "4.4",
                "4.4W",
                "5.0",
                "5.1",
                "6.0",
                "7.0",
                "7.1",
                "8.0",
                "8.1",
                "9",
                "10",
                "11",
                "12",
                "12L",
                "13",
                "14",
                "15",
                "16",
                "17",
            )
            .mapIndexed { index, version ->
                val api = index + 1
                val family =
                    when (api) {
                        in 1..8 -> GameFamily.CLASSIC
                        in 9..10 -> GameFamily.GINGERBREAD
                        in 11..13 -> GameFamily.HONEYCOMB
                        in 14..15 -> GameFamily.ICE_CREAM
                        in 16..18 -> GameFamily.JELLY_BEAN
                        19 -> GameFamily.KITKAT
                        20 -> GameFamily.WATCH
                        in 21..22 -> GameFamily.LOLLIPOP
                        23 -> GameFamily.MARSHMALLOW
                        in 24..25 -> GameFamily.NOUGAT
                        in 26..27 -> GameFamily.OREO
                        28 -> GameFamily.PIE
                        29 -> GameFamily.Q
                        30 -> GameFamily.R
                        in 31..32 -> GameFamily.S
                        33 -> GameFamily.T
                        34 -> GameFamily.U
                        35 -> GameFamily.V
                        36 -> GameFamily.BAKLAVA
                        else -> GameFamily.CINNAMON
                    }
                GameCharacter(api, version, family)
            }

    fun character(api: Int) = characters.first { it.api == api }

    fun evolutionSupport(weapon: WeaponId) = SupportId.entries[weapon.ordinal]
}
