package xyz.gaon.componentory.lab

@Suppress("DEPRECATION")
enum class PlatformFamily(
    val label: String,
    val origin: String,
    val themeName: String,
    val themeId: Int,
) {
    CLASSIC("Classic", "Android 1.0 · API 1", "Theme.Light", android.R.style.Theme_Light),
    HOLO("Holo", "Android 3.0 · API 11", "Theme.Holo.Light", android.R.style.Theme_Holo_Light),
    MATERIAL(
        "Material",
        "Android 5.0 · API 21",
        "Theme.Material.Light",
        android.R.style.Theme_Material_Light,
    ),
}
