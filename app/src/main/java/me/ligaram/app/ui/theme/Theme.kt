package me.ligaram.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Risk and Accent (same in both themes)
val RiskLow     = Color(0xFF10B981)
val RiskMedium  = Color(0xFFF59E0B)
val RiskHigh    = Color(0xFFEF4444)
val AccentBlue  = Color(0xFF3B82F6)
val AccentGreen = Color(0xFF10B981)
val AccentOrange = Color(0xFFF59E0B)
val AccentRed   = Color(0xFFEF4444)

// Semantic color tokens that change with theme
data class LigaramColors(
    val bgPrimary: Color,
    val bgSecondary: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val isDark: Boolean
)

val LigaramColorsDark = LigaramColors(
    bgPrimary      = Color(0xFF0F172A),
    bgSecondary    = Color(0xFF0A1628),
    surface        = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    textPrimary    = Color(0xFFF8FAFC),
    textSecondary  = Color(0xFF94A3B8),
    divider        = Color(0xFF334155),
    isDark         = true
)

val LigaramColorsLight = LigaramColors(
    bgPrimary      = Color(0xFFF8FAFC),
    bgSecondary    = Color(0xFFEFF6FF),
    surface        = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2E8F0),
    textPrimary    = Color(0xFF0F172A),
    textSecondary  = Color(0xFF64748B),
    divider        = Color(0xFFE2E8F0),
    isDark         = false
)

// Legacy fixed dark vals for OverlayActivity (always dark overlay on call screen)
val NavyDeep      = Color(0xFF0F172A)
val NavyMid       = Color(0xFF1E293B)
val NavyLight     = Color(0xFF334155)
val TextPrimary   = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val SurfaceCard   = Color(0xFF1E293B)

// CompositionLocal
val LocalLigaramColors = staticCompositionLocalOf { LigaramColorsDark }

val ligaramColors: LigaramColors
    @Composable @ReadOnlyComposable
    get() = LocalLigaramColors.current

private val DarkColorScheme = darkColorScheme(
    primary          = AccentBlue,
    onPrimary        = Color.White,
    secondary        = AccentGreen,
    onSecondary      = Color.White,
    background       = Color(0xFF0F172A),
    onBackground     = Color(0xFFF8FAFC),
    surface          = Color(0xFF1E293B),
    onSurface        = Color(0xFFF8FAFC),
    surfaceVariant   = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline          = Color(0xFF334155),
)

private val LightColorScheme = lightColorScheme(
    primary          = AccentBlue,
    onPrimary        = Color.White,
    secondary        = AccentGreen,
    onSecondary      = Color.White,
    background       = Color(0xFFF8FAFC),
    onBackground     = Color(0xFF0F172A),
    surface          = Color(0xFFFFFFFF),
    onSurface        = Color(0xFF0F172A),
    surfaceVariant   = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF64748B),
    outline          = Color(0xFFE2E8F0),
)

@Composable
fun LigaramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val ligaramColors = if (darkTheme) LigaramColorsDark else LigaramColorsLight
    val colorScheme   = if (darkTheme) DarkColorScheme   else LightColorScheme

    CompositionLocalProvider(LocalLigaramColors provides ligaramColors) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
