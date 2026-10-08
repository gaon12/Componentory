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

internal enum class GameFamily(val art: String, val sourceRelease: String) {
    CLASSIC("classic", "Android 2.3.7"),
    GINGERBREAD("gingerbread", "Android 2.3"),
    HONEYCOMB("honeycomb", "Android 3.0"),
    ICE_CREAM("icecream", "Android 4.0"),
    JELLY_BEAN("jellybean", "Android 4.1"),
    KITKAT("kitkat", "Android 4.4"),
    WATCH("holo", "Android 4.4.4 controls"),
    LOLLIPOP("lollipop", "Android 5.0"),
    MARSHMALLOW("marshmallow", "Android 6.0"),
    NOUGAT("neko", "Android 7.0"),
    OREO("octopus", "Android 8.0"),
    PIE("pie", "Android 9"),
    Q("q", "Android 10"),
    R("r", "Android 11"),
    S("s", "Android 12"),
    T("t", "Android 13"),
    U("u", "Android 14"),
    V("v", "Android 15"),
    BAKLAVA("baklava", "Android 16"),
    CINNAMON("cinnamon", "Android 17"),
}

internal data class GameResourceRelease(val api: Int, val version: String, val family: GameFamily) {
    val id
        get() = "api-" + api

    val title
        get() = "Android " + version
}

internal object GameCatalog {
    const val RULESET = "survival-v2"
    const val RANKED_SEED = 20261009L
    const val RUN_SECONDS = 1200
    const val SLOT_LIMIT = 4
    const val MAX_LEVEL = 5

    val releases: List<GameResourceRelease> =
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
                GameResourceRelease(api, version, family)
            }

    fun release(api: Int) = releases.first { it.api == api }

    const val PLAYER_ART = "neko"

    fun weaponArt(id: WeaponId) = id.name.lowercase(java.util.Locale.ROOT)

    fun weaponSource(id: WeaponId) =
        when (id) {
            WeaponId.BUTTON,
            WeaponId.SLIDER -> "Android 2.3.7"
            WeaponId.SWITCH,
            WeaponId.SPINNER -> "Android 4.4.4"
        }

    fun skill(id: WeaponId) =
        when (id) {
            WeaponId.BUTTON -> SkillKind.BURST
            WeaponId.SLIDER -> SkillKind.FREEZE
            WeaponId.SWITCH -> SkillKind.SHIELD
            WeaponId.SPINNER -> SkillKind.SUMMON
        }

    fun evolutionSupport(weapon: WeaponId) = SupportId.entries[weapon.ordinal]
}
