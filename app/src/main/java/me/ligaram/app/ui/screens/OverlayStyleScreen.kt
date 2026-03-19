package me.ligaram.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import me.ligaram.app.data.OverlayPreferences
import me.ligaram.app.data.OverlayStyle
import me.ligaram.app.ui.components.AppBackground
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.AccentGreen
import me.ligaram.app.ui.theme.NavyLight
import me.ligaram.app.ui.theme.NavyMid
import me.ligaram.app.ui.theme.RiskHigh

// Dados fictícios para o preview
private const val PREVIEW_NUMBER      = "987 654 321"
private const val PREVIEW_RISK        = "Risco Alto"
private const val PREVIEW_CATEGORY    = "Fraude"
private const val PREVIEW_SUBCATEGORY = "Falso investimento"

@Composable
fun OverlayStyleScreen(navController: NavController) {
    val context  = LocalContext.current
    var selected by remember { mutableStateOf(OverlayPreferences.getStyle(context)) }
    val listState = rememberLazyListState()

    // Scroll automático para o item seleccionado na lazy row
    LaunchedEffect(selected) {
        val idx = OverlayStyle.entries.indexOf(selected)
        if (idx >= 0) listState.animateScrollToItem(idx)
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 12.dp, top = 60.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Estilo do overlay",
                        color      = MaterialTheme.colorScheme.onBackground,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Toque fora para fechar em todos os estilos",
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // ── Preview ao vivo ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RectangleShape)
                    .background(
                        if (isSystemInDarkTheme())
                            Brush.verticalGradient(listOf(NavyMid, NavyLight))
                        else
                            Brush.verticalGradient(
                                listOf(
                                    androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                                    androidx.compose.ui.graphics.Color(0xFFE2E8F0)
                                )
                            )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Label "Pré-visualização"
                Text(
                    "Pré-visualização",
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                )

                // Overlay renderizado com dados fictícios
                // graphicsLayer faz scale visual mas não altera o layout - usamos
                // scale() no Modifier que reduz também o espaço ocupado, evitando overflow.
                AnimatedContent(
                    targetState    = selected,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label          = "overlay_preview"
                ) { style ->
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .scale(0.78f),
                        contentAlignment = if (style == OverlayStyle.FLOATING)
                            Alignment.BottomEnd else Alignment.Center
                    ) {
                        OverlayPreviewContent(style = style)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // ── Selector horizontal ───────────────────────────────────────────
            LazyRow(
                state               = listState,
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding      = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(OverlayStyle.entries) { style ->
                    val isSelected = style == selected
                    Surface(
                        onClick = {
                            selected = style
                            OverlayPreferences.setStyle(context, style)
                        },
                        shape  = RoundedCornerShape(12.dp),
                        color  = if (isSelected) AccentBlue.copy(alpha = 0.12f)
                                 else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .width(130.dp)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AccentBlue
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(
                            modifier            = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier              = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    style.label,
                                    color      = if (isSelected) AccentBlue
                                                 else MaterialTheme.colorScheme.onBackground,
                                    fontSize   = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold
                                                 else FontWeight.SemiBold,
                                    maxLines   = 2,
                                    lineHeight = 15.sp,
                                    modifier   = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint     = AccentGreen,
                                        modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(
                                style.description,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 2,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // ── Descrição do estilo seleccionado ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    selected.label,
                    color      = MaterialTheme.colorScheme.onBackground,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    selected.description,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ─── Renderiza o overlay correcto com dados fictícios ─────────────────────────
@Composable
private fun OverlayPreviewContent(style: OverlayStyle) {
    val color = RiskHigh // sempre alto para o preview ser visualmente claro
    val onDismiss = {} // no-op - o preview não fecha

    when (style) {
        OverlayStyle.PILL        -> StylePill(PREVIEW_NUMBER, PREVIEW_RISK, color, PREVIEW_CATEGORY, PREVIEW_SUBCATEGORY, null, onDismiss)
        OverlayStyle.BANNER      -> StyleBanner(PREVIEW_NUMBER, PREVIEW_RISK, color, PREVIEW_CATEGORY, PREVIEW_SUBCATEGORY, null, onDismiss)
        OverlayStyle.BANNER_FULL -> StyleBannerFull(PREVIEW_NUMBER, PREVIEW_RISK, color, PREVIEW_CATEGORY, PREVIEW_SUBCATEGORY, null, onDismiss)
        OverlayStyle.CARD        -> StyleCard(PREVIEW_NUMBER, PREVIEW_RISK, color, PREVIEW_CATEGORY, PREVIEW_SUBCATEGORY, null, onDismiss)
        OverlayStyle.SPLIT       -> StyleSplit(PREVIEW_NUMBER, PREVIEW_RISK, color, PREVIEW_CATEGORY, PREVIEW_SUBCATEGORY, null, onDismiss)
        OverlayStyle.SCORE       -> StyleScore(PREVIEW_NUMBER, PREVIEW_RISK, color, PREVIEW_CATEGORY, PREVIEW_SUBCATEGORY, null, onDismiss)
        OverlayStyle.FLOATING    -> StyleFloatingChip(PREVIEW_NUMBER, PREVIEW_RISK, color, PREVIEW_CATEGORY, PREVIEW_SUBCATEGORY, null, onDismiss)
        OverlayStyle.MINIMAL     -> StyleMinimal(PREVIEW_NUMBER, PREVIEW_RISK, color, PREVIEW_CATEGORY, PREVIEW_SUBCATEGORY, null, onDismiss)
        OverlayStyle.BANNER_TOP  -> StyleBannerTop(PREVIEW_NUMBER, PREVIEW_RISK, color, PREVIEW_CATEGORY, PREVIEW_SUBCATEGORY, null, onDismiss)
    }
}
