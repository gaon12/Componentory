package xyz.gaon.componentory.eastereggs

internal const val eggSourceRevision = "63d3e4549efbd6f714f6c19764d9520906c18c57"
internal const val eggSourceRepository = "https://github.com/hushenghao/AndroidEasterEggs"

internal data class EggStage(val title: String, val className: String, val minimumApi: Int = 24)

internal enum class EggIntegrationKind {
    DREAM,
    TILE,
    CONTROLS,
    WIDGET,
}

internal data class EggIntegration(
    val kind: EggIntegrationKind,
    val className: String,
    val minimumApi: Int,
)

internal data class EasterEggFamily(
    val module: String,
    val nickname: String,
    val packageName: String,
    val stages: List<EggStage>,
    val integrations: List<EggIntegration> = emptyList(),
) {
    val logo
        get() = EggStage("PlatLogo", "$packageName.PlatLogoActivity")
}

internal data class EasterEggRelease(
    val version: String,
    val family: EasterEggFamily,
    val pointOne: Boolean = false,
) {
    val id
        get() = version.replace('.', '_')

    val title
        get() = "Android $version"

    val logo
        get() =
            if (pointOne)
                family.logo.copy(className = "${family.packageName}.PlatLogoActivity\$Point1")
            else family.logo

    fun matches(query: String, translatedName: String): Boolean =
        "$title ${family.nickname} ${family.module} Easter egg AOSP $translatedName"
            .contains(query.trim(), ignoreCase = true)
}

private fun family(
    module: String,
    nickname: String,
    code: String,
    vararg screens: String,
    integrations: List<EggIntegration> = emptyList(),
    api: Int = 24,
): EasterEggFamily {
    val packageName = "com.android_$code.egg"
    return EasterEggFamily(
        module,
        nickname,
        packageName,
        screens.map { screen ->
            val minimumApi =
                when {
                    screen == "widget.PaintChipsActivity" -> 31
                    screen == "neko.NekoLand" && code != "n" -> 30
                    screen.startsWith("beta.") -> 24
                    else -> api
                }
            EggStage(screen, "$packageName.$screen", minimumApi)
        },
        integrations,
    )
}

private fun integration(code: String, name: String, kind: EggIntegrationKind, api: Int) =
    EggIntegration(kind, "com.android_$code.egg.$name", api)

private val gingerbread = family("Gingerbread", "Gingerbread", "g")
private val honeycomb = family("Honeycomb", "Honeycomb", "h")
private val iceCream =
    family("IceCreamSandwich", "Ice Cream Sandwich", "i", "Nyandroid", "preview.PlatLogoActivity")
private val jellyBean =
    family(
        "JellyBean",
        "Jelly Bean",
        "j",
        "BeanBag",
        integrations = listOf(integration("j", "BeanBagDream", EggIntegrationKind.DREAM, 24)),
    )
private val kitKat =
    family(
        "KitKat",
        "KitKat",
        "k",
        "DessertCase",
        "preview.PlatLogoActivity",
        integrations = listOf(integration("k", "DessertCaseDream", EggIntegrationKind.DREAM, 24)),
    )
private val lollipop =
    family("Lollipop", "Lollipop", "l", "LLandActivity", "preview.PlatLogoActivity")
private val marshmallow =
    family(
        "Marshmallow",
        "Marshmallow",
        "m",
        "MLandActivity",
        "preview.PlatLogoActivity",
        "preview.ShruggyActivity",
    )
private val nougat =
    family(
        "Nougat",
        "Nougat",
        "n",
        "neko.NekoLand",
        "preview.PlatLogoActivity",
        integrations = listOf(integration("n", "neko.NekoTile", EggIntegrationKind.TILE, 24)),
    )
private val oreo = family("Oreo", "Oreo", "o", "octo.Ocquarium")
private val pie = family("Pie", "Pie", "p", "paint.PaintActivity")
private val q = family("Q", "Quince Tart", "q", "quares.QuaresActivity")
private val r =
    family(
        "R",
        "Red Velvet Cake",
        "r",
        "neko.NekoLand",
        integrations =
            listOf(integration("r", "neko.NekoControlsService", EggIntegrationKind.CONTROLS, 30)),
        api = 30,
    )

private fun colors(module: String, nickname: String, code: String, beta: Boolean = false) =
    family(
        module,
        nickname,
        code,
        *if (beta) arrayOf("widget.PaintChipsActivity", "neko.NekoLand", "beta.PlatLogoActivity")
        else arrayOf("widget.PaintChipsActivity", "neko.NekoLand"),
        integrations =
            listOf(
                integration(code, "neko.NekoControlsService", EggIntegrationKind.CONTROLS, 30),
                integration(code, "widget.PaintChipsWidget", EggIntegrationKind.WIDGET, 31),
            ),
        api = 31,
    )

private val s = colors("S", "Snow Cone", "s")
private val t = colors("Tiramisu", "Tiramisu", "t", beta = true)

private fun space(module: String, nickname: String, code: String, dream: Boolean = true) =
    family(
        module,
        nickname,
        code,
        "landroid.MainActivity",
        integrations =
            if (dream)
                listOf(integration(code, "landroid.DreamUniverse", EggIntegrationKind.DREAM, 24))
            else emptyList(),
    )

private val u = space("UpsideDownCake", "Upside Down Cake", "u", dream = false)
private val v = space("VanillaIceCream", "Vanilla Ice Cream", "v")
private val baklava = space("Baklava", "Baklava", "baklava")
private val cinnamon = space("CinnamonBun", "Cinnamon Bun", "cinnamon_bun")

internal val easterEggReleases =
    listOf(
        EasterEggRelease("2.3", gingerbread),
        EasterEggRelease("3.0", honeycomb),
        EasterEggRelease("3.1", honeycomb),
        EasterEggRelease("3.2", honeycomb),
        EasterEggRelease("4.0", iceCream),
        EasterEggRelease("4.1", jellyBean),
        EasterEggRelease("4.2", jellyBean),
        EasterEggRelease("4.3", jellyBean),
        EasterEggRelease("4.4", kitKat),
        EasterEggRelease("5.0", lollipop),
        EasterEggRelease("5.1", lollipop),
        EasterEggRelease("6.0", marshmallow),
        EasterEggRelease("7.0", nougat),
        EasterEggRelease("7.1", nougat),
        EasterEggRelease("8.0", oreo),
        EasterEggRelease("8.1", oreo, pointOne = true),
        EasterEggRelease("9", pie),
        EasterEggRelease("10", q),
        EasterEggRelease("11", r),
        EasterEggRelease("12", s),
        EasterEggRelease("12L", s),
        EasterEggRelease("13", t),
        EasterEggRelease("14", u),
        EasterEggRelease("15", v),
        EasterEggRelease("16", baklava),
        EasterEggRelease("17", cinnamon),
    )
