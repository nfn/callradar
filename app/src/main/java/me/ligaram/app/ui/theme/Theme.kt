package me.ligaram.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colors
val NavyDeep = Color(0xFF0F172A)
val NavyMid = Color(0xFF1E293B)
val NavyLight = Color(0xFF334155)
val AccentBlue = Color(0xFF3B82F6)
val AccentBlueDark = Color(0xFF2563EB)
val AccentGreen = Color(0xFF10B981)
val AccentRed = Color(0xFFEF4444)
val AccentOrange = Color(0xFFF59E0B)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val SurfaceCard = Color(0xFF1E293B)
val Divider = Color(0xFF334155)

val RiskLow = Color(0xFF10B981)
val RiskMedium = Color(0xFFF59E0B)
val RiskHigh = Color(0xFFEF4444)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = TextPrimary,
    secondary = AccentGreen,
    onSecondary = TextPrimary,
    background = NavyDeep,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = NavyLight,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
)

@Composable
fun LigaramTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
