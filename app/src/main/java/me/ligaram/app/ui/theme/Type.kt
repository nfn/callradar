package me.ligaram.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.ligaram.app.R

// ── Fonte Inter (variável) ────────────────────────────────────────────────────
// Um único ficheiro TTF cobre todos os pesos via FontVariation

@OptIn(ExperimentalTextApi::class)
val DMSansFontFamily = FontFamily(
    Font(
        resId = R.font.dm_sans_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.dm_sans_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.dm_sans_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        resId = R.font.dm_sans_variable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    ),
)

// ── Ponto de troca único ──────────────────────────────────────────────────────
// Para mudar de fonte ou reverter para o sistema, alterar apenas esta linha:
//   Inter:   private val AppFontFamily = DMSansFontFamily
//   Sistema: private val AppFontFamily = FontFamily.Default
@OptIn(ExperimentalTextApi::class)
private val AppFontFamily = DMSansFontFamily

// ── Escala tipográfica ────────────────────────────────────────────────────────

@OptIn(ExperimentalTextApi::class)
internal fun buildTypography(f: FontFamily) = Typography(
    headlineMedium = TextStyle(fontFamily = f, fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineSmall  = TextStyle(fontFamily = f, fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleLarge     = TextStyle(fontFamily = f, fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium    = TextStyle(fontFamily = f, fontSize = 18.sp, fontWeight = FontWeight.Bold),
    titleSmall     = TextStyle(fontFamily = f, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge      = TextStyle(fontFamily = f, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodyMedium     = TextStyle(fontFamily = f, fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = f, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = f, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    labelMedium    = TextStyle(fontFamily = f, fontSize = 11.sp, fontWeight = FontWeight.Normal),
    labelSmall     = TextStyle(fontFamily = f, fontSize = 10.sp,  fontWeight = FontWeight.Normal),
)

@OptIn(ExperimentalTextApi::class)
val LigaramTypography = buildTypography(AppFontFamily)
