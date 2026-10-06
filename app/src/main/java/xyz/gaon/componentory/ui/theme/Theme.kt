package xyz.gaon.componentory.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
    darkColorScheme(
        primary = AppDarkBlue,
        onPrimary = AppDarkBackground,
        primaryContainer = AppDarkBlueSoft,
        onPrimaryContainer = AppDarkBlue,
        secondary = AppDarkSecondaryText,
        onSecondary = AppDarkBackground,
        secondaryContainer = AppDarkBorder,
        onSecondaryContainer = AppDarkText,
        background = AppDarkBackground,
        onBackground = AppDarkText,
        surface = AppDarkSurface,
        onSurface = AppDarkText,
        surfaceVariant = AppDarkBorder,
        onSurfaceVariant = AppDarkSecondaryText,
        surfaceContainerLowest = AppDarkBackground,
        surfaceContainerLow = AppDarkSurface,
        surfaceContainer = AppDarkSurface,
        surfaceContainerHigh = AppDarkBorder,
        surfaceContainerHighest = AppDarkBorder,
        surfaceTint = Color.Transparent,
        outline = AppDarkSecondaryText,
        outlineVariant = AppDarkBorder,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = AppBlue,
        onPrimary = Color.White,
        primaryContainer = AppBlueSoft,
        onPrimaryContainer = AppBlue,
        secondary = AppSecondaryText,
        onSecondary = Color.White,
        secondaryContainer = AppBackground,
        onSecondaryContainer = AppText,
        background = AppBackground,
        onBackground = AppText,
        surface = Color.White,
        onSurface = AppText,
        surfaceVariant = AppBackground,
        onSurfaceVariant = AppSecondaryText,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color.White,
        surfaceContainer = Color.White,
        surfaceContainerHigh = AppBackground,
        surfaceContainerHighest = AppBorder,
        surfaceTint = Color.Transparent,
        outline = AppSecondaryText,
        outlineVariant = AppBorder,
    )

private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )

@Composable
fun ComponentoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
