package com.illustravibes.arus.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.illustravibes.arus.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// 1. COLOR TOKENS (Bevel B&W Base + Notion Accents)
// ============================================================================

val ArusBackground = Color(0xFFF6F8FE)       // Soft Blue-Gray Page Background
val ArusSurface = Color(0xFFFFFFFF)          // Pure White Card Surface
val ArusTextPrimary = Color(0xFF1F2029)      // High Contrast Dark Text
val ArusTextSecondary = Color(0xFF60657A)    // Muted Slate Label/Caption Text
val ArusBorder = Color(0xFFE5E9F2)           // Clean Subtle Border

val ArusBrandPrimary = Color(0xFF1F2029)     // Bevel Dark Black Primary CTA (#1F2029)
val ArusBrandSoft = Color(0xFFF0F2F7)        // Soft Neutral Accent
val ArusBrandPressed = Color(0xFF000000)

val ArusIncome = Color(0xFF00A86B)           // Emerald Green
val ArusIncomeSoft = Color(0xFFE3F8EE)
val ArusExpense = Color(0xFFE53935)          // Crimson Red
val ArusExpenseSoft = Color(0xFFFDEAF0)

// Notion Accent Color Palette
val NotionMarigold = Color(0xFFFFB110)
val NotionMarigoldSoft = Color(0xFFFFF7E6)
val NotionCoral = Color(0xFFF64932)
val NotionCoralSoft = Color(0xFFFEECE9)
val NotionSky = Color(0xFF62AEF0)
val NotionSkySoft = Color(0xFFEFF7FF)
val NotionMocha = Color(0xFFB18164)
val NotionMochaSoft = Color(0xFFF7F2EF)
val NotionMint = Color(0xFF00A86B)
val NotionMintSoft = Color(0xFFE3F8EE)
val NotionLavender = Color(0xFF8B5CF6)
val NotionLavenderSoft = Color(0xFFF3E8FF)

@Immutable
data class ArusCustomColors(
    val background: Color = ArusBackground,
    val surface: Color = ArusSurface,
    val textPrimary: Color = ArusTextPrimary,
    val textSecondary: Color = ArusTextSecondary,
    val border: Color = ArusBorder,
    val brandPrimary: Color = ArusBrandPrimary,
    val brandSoft: Color = ArusBrandSoft,
    val income: Color = ArusIncome,
    val incomeSoft: Color = ArusIncomeSoft,
    val expense: Color = ArusExpense,
    val expenseSoft: Color = ArusExpenseSoft
)

val LocalArusColors = staticCompositionLocalOf { ArusCustomColors() }

// ============================================================================
// 2. TYPOGRAPHY (Inter + Tabular Figures for Financial Numbers)
// ============================================================================

val TabularNumberFeature = "tnum"

// P1-2: Inter dibundel sebagai resource (res/font), bukan sans-serif sistem.
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val ArusTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = TabularNumberFeature,
        color = ArusTextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp,
        fontFeatureSettings = TabularNumberFeature,
        color = ArusTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
        color = ArusTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = ArusTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = ArusTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = ArusTextPrimary
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = ArusTextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp,
        color = ArusTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = ArusTextSecondary
    )
)

// ============================================================================
// 3. SHAPES (Bevel Style Corner Radii)
// ============================================================================

val ArusShapes = Shapes(
    small = RoundedCornerShape(12.dp),      // Soft Tiles / Small Buttons
    medium = RoundedCornerShape(16.dp),     // Input Fields / Modals
    large = RoundedCornerShape(24.dp),      // Cards & Sheets
    extraLarge = RoundedCornerShape(100.dp) // Pill Buttons & Chips
)

val SoftTileShape = RoundedCornerShape(14.dp)
val CardShape = RoundedCornerShape(24.dp)
val LargeCardShape = RoundedCornerShape(24.dp)
val PillShape = RoundedCornerShape(100.dp)

// ============================================================================
// 4. MATERIAL 3 COLOR SCHEME MAPPING
// ============================================================================

private val LightColorScheme = lightColorScheme(
    primary = ArusBrandPrimary,
    onPrimary = Color.White,
    primaryContainer = ArusBrandSoft,
    onPrimaryContainer = ArusBrandPrimary,
    background = ArusBackground,
    onBackground = ArusTextPrimary,
    surface = ArusSurface,
    onSurface = ArusTextPrimary,
    surfaceVariant = ArusBackground,
    onSurfaceVariant = ArusTextSecondary,
    outline = ArusBorder
)

@Composable
fun ArusTheme(
    content: @Composable () -> Unit
) {
    // P2-10: Arus tampil terang penuh (light-only) — keputusan eksplisit.
    // Tidak ada token dark; jangan setengah-setengah (campuran terang/gelap).
    val customColors = ArusCustomColors()

    CompositionLocalProvider(
        LocalArusColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = ArusTypography,
            shapes = ArusShapes,
            content = content
        )
    }
}
