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

// ── M3 Color Schemes ──────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary          = Primary40,
    onPrimary        = Neutral100,
    secondary        = Secondary40,
    onSecondary      = Neutral100,
    tertiary         = Tertiary40,
    onTertiary       = Neutral100,
    error            = Error40,
    onError          = Neutral100,
    background       = Neutral10,
    onBackground     = Neutral99,
    surface          = Neutral20,
    onSurface        = Neutral99,
    surfaceVariant   = Neutral30,
    onSurfaceVariant = Neutral60,
    outline          = Neutral30,
    outlineVariant   = Neutral30,
)

private val LightColorScheme = lightColorScheme(
    primary          = Primary40,
    onPrimary        = Neutral100,
    secondary        = Secondary40,
    onSecondary      = Neutral100,
    tertiary         = Tertiary40,
    onTertiary       = Neutral100,
    error            = Error40,
    onError          = Neutral100,
    background       = Neutral99,
    onBackground     = Neutral10,
    surface          = Neutral100,
    onSurface        = Neutral10,
    surfaceVariant   = Neutral90,
    onSurfaceVariant = Neutral50,
    outline          = Neutral90,
    outlineVariant   = Neutral90,
)

// ── Custom tokens (apenas o que o M3 não cobre) ───────────────────────────────

data class LigaramColors(
    val bgSecondary: Color, // segundo tom do gradiente de fundo — M3 não tem
    val isDark: Boolean
)

val LigaramColorsDark  = LigaramColors(bgSecondary = Neutral6,  isDark = true)
val LigaramColorsLight = LigaramColors(bgSecondary = Neutral95, isDark = false)

val LocalLigaramColors = staticCompositionLocalOf { LigaramColorsDark }

val ligaramColors: LigaramColors
    @Composable @ReadOnlyComposable
    get() = LocalLigaramColors.current

// ── Semantic aliases ──────────────────────────────────────────────────────────
// Apontam para Color.kt — mudar a paleta aqui propaga-se a toda a app

// Interação primária
val AccentBlue   = Primary40

// Estados positivos / risco baixo
val AccentGreen  = Secondary40
val RiskLow      = Secondary40

// Avisos / risco médio
val AccentOrange = Tertiary40
val RiskMedium   = Tertiary40

// Erros / risco alto
val AccentRed    = Error40
val RiskHigh     = Error40

// Escala de estrelas
val StarRating2  = StarOrange
val StarRating4  = StarLime

// Aliases legados para OverlayActivity (sempre escuro — overlay por cima do dialer)
val NavyDeep      = Neutral10
val NavyMid       = Neutral20
val NavyLight     = Neutral30
val TextPrimary   = Neutral99
val TextSecondary = Neutral60
val SurfaceCard   = Neutral20

// ── Theme ─────────────────────────────────────────────────────────────────────

@Composable
fun LigaramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val ligaramColors = if (darkTheme) LigaramColorsDark else LigaramColorsLight
    val colorScheme   = if (darkTheme) DarkColorScheme   else LightColorScheme

    CompositionLocalProvider(LocalLigaramColors provides ligaramColors) {
        MaterialTheme(colorScheme = colorScheme, typography = LigaramTypography, content = content)
    }
}
