package xyz.gaon.componentory.lab

import android.content.Context
import android.content.res.Configuration
import android.view.ContextThemeWrapper

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
    );

    fun createContext(context: Context): ContextThemeWrapper =
        ContextThemeWrapper(context, themeId).apply {
            // A light theme still resolves night-qualified resources from its base context.
            // Override that configuration before accessing this wrapper's resources or theme.
            applyOverrideConfiguration(
                Configuration(context.resources.configuration).apply {
                    uiMode =
                        (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                            Configuration.UI_MODE_NIGHT_NO
                }
            )
        }
}
