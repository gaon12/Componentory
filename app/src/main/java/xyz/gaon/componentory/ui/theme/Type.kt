package xyz.gaon.componentory.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun appText(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = height.sp,
        letterSpacing = 0.sp,
    )

val Typography =
    Typography(
        displayLarge = appText(36, 46, FontWeight.Bold),
        displayMedium = appText(32, 42, FontWeight.Bold),
        displaySmall = appText(30, 40, FontWeight.Bold),
        headlineLarge = appText(30, 40, FontWeight.Bold),
        headlineMedium = appText(26, 36, FontWeight.Bold),
        headlineSmall = appText(22, 32, FontWeight.SemiBold),
        titleLarge = appText(20, 28, FontWeight.SemiBold),
        titleMedium = appText(16, 24, FontWeight.SemiBold),
        titleSmall = appText(14, 22, FontWeight.SemiBold),
        bodyLarge = appText(16, 26),
        bodyMedium = appText(14, 22),
        bodySmall = appText(12, 18),
        labelLarge = appText(14, 20, FontWeight.SemiBold),
        labelMedium = appText(12, 18, FontWeight.SemiBold),
        labelSmall = appText(11, 16, FontWeight.Medium),
    )
