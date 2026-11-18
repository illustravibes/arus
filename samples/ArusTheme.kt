package com.illustravibes.arus.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Starter styling tokens. Align changes with docs/DESIGN_SYSTEM.md. */
object ArusColors {
    val Brand = Color(0xFF0009FF)
    val BrandSoft = Color(0xFFECEEFF)
    val Page = Color(0xFFF6F8FE)
    val Card = Color(0xFFFFFFFF)
    val SubtleSurface = Color(0xFFE8EFFE)
    val Ink = Color(0xFF34364A)
    val MutedInk = Color(0xFF60657A)
    val Border = Color(0xFFE5E9F2)
    val Income = Color(0xFF087E56)
    val IncomeSoft = Color(0xFFE3F8EE)
    val Expense = Color(0xFFB7324C)
    val ExpenseSoft = Color(0xFFFDEAF0)
    val Warning = Color(0xFF986105)
    val WarningSoft = Color(0xFFFFF4D7)
}

private val ArusLightScheme = lightColorScheme(
    primary = ArusColors.Brand,
    onPrimary = Color.White,
    primaryContainer = ArusColors.BrandSoft,
    onPrimaryContainer = ArusColors.Ink,
    background = ArusColors.Page,
    onBackground = ArusColors.Ink,
    surface = ArusColors.Card,
    onSurface = ArusColors.Ink,
    surfaceVariant = ArusColors.SubtleSurface,
    onSurfaceVariant = ArusColors.MutedInk,
    outline = ArusColors.Border,
    error = ArusColors.Expense,
    onError = Color.White,
)

private val ArusShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Fallback typography: attach bundled/licensed Plus Jakarta Sans fonts later. */
private val ArusTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 31.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
    ),
)

@Composable
fun ArusTheme(content: @Composable () -> Unit) {
    // Light reference UI only. Do not claim dark-theme support until designed and tested.
    MaterialTheme(
        colorScheme = ArusLightScheme,
        typography = ArusTypography,
        shapes = ArusShapes,
        content = content,
    )
}
