package xyz.gaon.componentory.lab

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun sampleColorScheme(family: DesignFamily, context: Context): ColorScheme =
    when (family) {
        DesignFamily.MATERIAL_YOU -> {
            if (Build.VERSION.SDK_INT >= 31) dynamicLightColorScheme(context)
            else error("Dynamic color requires Android 12 or later.")
        }
        DesignFamily.EXPRESSIVE -> expressiveLightColorScheme()
        else -> lightColorScheme()
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun Material3SampleTheme(family: DesignFamily, content: @Composable () -> Unit) {
    val colors = sampleColorScheme(family, LocalContext.current)
    // Each supplier owns its theme; the app shell must not overwrite sample shapes or motion.
    if (family == DesignFamily.EXPRESSIVE)
        MaterialExpressiveTheme(colorScheme = colors, content = content)
    else
        MaterialTheme(
            colorScheme = colors,
            motionScheme = MotionScheme.standard(),
            shapes = Shapes(),
            typography = Typography(),
            content = content,
        )
}

internal fun sampleThemeSnapshot(
    family: DesignFamily,
    context: Context,
    side: String,
): Map<String, String> {
    if (!family.isMaterial3) return emptyMap()
    if (family == DesignFamily.MATERIAL_YOU && Build.VERSION.SDK_INT < 31)
        return mapOf("${side}ColorSource" to "UNSUPPORTED_BEFORE_API_31")
    val colors = sampleColorScheme(family, context)
    fun color(value: androidx.compose.ui.graphics.Color) =
        value.toArgb().toUInt().toString(16).padStart(8, '0')
    return mapOf(
        "${side}ColorSource" to
            if (family == DesignFamily.MATERIAL_YOU) "ANDROID_DYNAMIC_LIGHT" else "LIBRARY_LIGHT",
        "${side}Theme" to
            if (family == DesignFamily.EXPRESSIVE) "MaterialExpressiveTheme" else "MaterialTheme",
        "${side}Motion" to if (family == DesignFamily.EXPRESSIVE) "EXPRESSIVE" else "STANDARD",
        "${side}PrimaryArgb" to color(colors.primary),
        "${side}SecondaryArgb" to color(colors.secondary),
        "${side}TertiaryArgb" to color(colors.tertiary),
        "${side}SurfaceArgb" to color(colors.surface),
        "${side}BackgroundArgb" to color(colors.background),
    )
}
