package me.ligaram.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
// import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
// import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
// import me.ligaram.app.R

// ── Fonte Outfit (variável) ───────────────────────────────────────────────────
// Um único ficheiro TTF cobre todos os pesos via FontVariation
/*
@OptIn(ExperimentalTextApi::class)
val OutfitFontFamily = FontFamily(
    Font(
        resId = R.font.cabin_variable,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        resId = R.font.cabin_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.cabin_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.cabin_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.cabin_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
)
*/
// ── Ponto de troca único ──────────────────────────────────────────────────────
// Para mudar de fonte ou reverter para o sistema, alterar apenas esta linha:
//   Outfit:  private val AppFontFamily = OutfitFontFamily
//   Sistema: private val AppFontFamily = FontFamily.Default
// @OptIn(ExperimentalTextApi::class)
private val AppFontFamily = FontFamily.Default

// ── Escala tipográfica ────────────────────────────────────────────────────────
// Estratégia: pesos descendentes — títulos maiores mais leves, menores mais pesados.
// Cria hierarquia visual natural sem exigir grandes diferenças de tamanho.

// @OptIn(ExperimentalTextApi::class)
internal fun buildTypography(f: FontFamily) = Typography(
    headlineMedium = TextStyle(fontFamily = f, fontSize = 28.sp, fontWeight = FontWeight.Light),
    headlineSmall  = TextStyle(fontFamily = f, fontSize = 20.sp, fontWeight = FontWeight.Light),
    titleLarge     = TextStyle(fontFamily = f, fontSize = 22.sp, fontWeight = FontWeight.Light),
    titleMedium    = TextStyle(fontFamily = f, fontSize = 18.sp, fontWeight = FontWeight.Normal),
    titleSmall     = TextStyle(fontFamily = f, fontSize = 15.sp, fontWeight = FontWeight.Medium),
    bodyLarge      = TextStyle(fontFamily = f, fontSize = 14.sp, fontWeight = FontWeight.Normal,   lineHeight = 22.sp),
    bodyMedium     = TextStyle(fontFamily = f, fontSize = 13.sp, fontWeight = FontWeight.Normal,   lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = f, fontSize = 12.sp, fontWeight = FontWeight.Normal,   lineHeight = 18.sp),
    labelLarge     = TextStyle(fontFamily = f, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    labelMedium    = TextStyle(fontFamily = f, fontSize = 11.sp, fontWeight = FontWeight.Normal),
    labelSmall     = TextStyle(fontFamily = f, fontSize = 10.sp, fontWeight = FontWeight.Normal),
)

// @OptIn(ExperimentalTextApi::class)
val LigaramTypography = buildTypography(AppFontFamily)
