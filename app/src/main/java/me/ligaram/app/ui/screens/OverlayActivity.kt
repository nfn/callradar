package me.ligaram.app.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.AccentGreen
import me.ligaram.app.ui.theme.LigaramTheme
import me.ligaram.app.ui.theme.NavyDeep
import me.ligaram.app.ui.theme.NavyLight
import me.ligaram.app.ui.theme.NavyMid
import me.ligaram.app.ui.theme.RiskHigh
import me.ligaram.app.ui.theme.RiskLow
import me.ligaram.app.ui.theme.RiskMedium
import me.ligaram.app.ui.theme.TextPrimary
import me.ligaram.app.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

/** Dados do overlay; atualizados quando chega um novo Intent (ex.: segunda chamada). */
private data class OverlayData(
    val number: String,
    val rating: String,
    val risk: String,
    val category: String,
    val subcategory: String,
    val contactName: String?
)

class OverlayActivity : ComponentActivity() {

    companion object {
        const val ACTION_DISMISS = "me.ligaram.app.DISMISS_OVERLAY"
    }

    private val dismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_DISMISS) finish()
        }
    }

    private var overlayDataState = mutableStateOf(OverlayData("", "0", "", "", "", null))

    private fun overlayDataFromIntent(intent: Intent): OverlayData = OverlayData(
        number      = intent.getStringExtra("number")      ?: "",
        rating      = intent.getStringExtra("rating")      ?: "0",
        risk        = intent.getStringExtra("risk")        ?: "",
        category    = intent.getStringExtra("category")    ?: "",
        subcategory = intent.getStringExtra("subcategory") ?: "",
        contactName = intent.getStringExtra("contactName")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)

        overlayDataState.value = overlayDataFromIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dismissReceiver, IntentFilter(ACTION_DISMISS), RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(dismissReceiver, IntentFilter(ACTION_DISMISS))
        }

        setContent {
            val data = overlayDataState.value
            LigaramTheme(darkTheme = true) {
                OverlayScreen(
                    number      = data.number,
                    risk        = data.risk,
                    category    = data.category,
                    subcategory = data.subcategory,
                    contactName = data.contactName,
                    onDismiss   = { finish() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        overlayDataState.value = overlayDataFromIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val current = overlayDataFromIntent(intent)
        if (current.number != overlayDataState.value.number) {
            overlayDataState.value = current
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dismissReceiver)
    }
}

// ─── Risk color helper ────────────────────────────────────────────────────────
fun riskColor(risk: String): Color = when {
    risk.contains("Alto",     ignoreCase = true) ||
            risk.contains("Elevado",  ignoreCase = true) ||
            risk.contains("High",     ignoreCase = true) -> RiskHigh
    risk.contains("Medio",    ignoreCase = true) ||
            risk.contains("Médio",    ignoreCase = true) ||
            risk.contains("Moderado", ignoreCase = true) -> RiskMedium
    else -> RiskLow
}

// ─── Overlay selector ────────────────────────────────────────────────────────
// Muda este valor para testar cada estilo: 1 a 10
// 1  — Pill expansível (compacto, toca para ver detalhes)
// 2  — Banner expansível (linha + painel ao clicar)
// 3  — Banner completo (tudo visível, botão X)
// 4  — Banner completo tap-to-dismiss (fecha ao tocar, sem botão X)
// 5  — Card com barra lateral colorida
// 6  — Split: identidade à esquerda, risco à direita
// 7  — Score card com barra de risco
// 8  — Chip flutuante no canto inferior (expande para cima)
// 9  — Minimal pill sem expansão
// 10 — Banner topo com linha de cor
private const val OVERLAY_STYLE = 4

@Composable
fun OverlayScreen(
    number: String,
    risk: String,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    val color = riskColor(risk)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Estilos que gerem o seu próprio drag internamente (4, 8, 11)
    // não devem receber o drag do OverlayScreen — ficam sem offset externo.
    val selfManagesDrag = OVERLAY_STYLE == 4 || OVERLAY_STYLE == 8

    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = if (OVERLAY_STYLE == 8) Alignment.BottomEnd else Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(spring(dampingRatio = 0.6f, stiffness = 500f)) + fadeIn(),
            exit  = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, offsetY.roundToInt()) }
                    .then(
                        if (!selfManagesDrag)
                            Modifier.pointerInput(Unit) {
                                detectDragGestures { _, dragAmount -> offsetY += dragAmount.y }
                            }
                        else Modifier
                    )
            ) {
                when (OVERLAY_STYLE) {
                    1  -> StylePill(number, risk, color, category, subcategory, contactName, onDismiss)
                    2  -> StyleBanner(number, risk, color, category, subcategory, contactName, onDismiss)
                    3  -> StyleBannerFull(number, risk, color, category, subcategory, contactName, onDismiss)
                    4  -> StyleBannerFullTap(number, risk, color, category, subcategory, contactName, onDismiss)
                    5  -> StyleCard(number, risk, color, category, subcategory, contactName, onDismiss)
                    6  -> StyleSplit(number, risk, color, category, subcategory, contactName, onDismiss)
                    7  -> StyleScore(number, risk, color, category, subcategory, contactName, onDismiss)
                    8  -> StyleFloatingChip(number, risk, color, category, subcategory, contactName, onDismiss)
                    9  -> StyleMinimal(number, risk, color, category, subcategory, contactName, onDismiss)
                    else -> StyleBannerTop(number, risk, color, category, subcategory, contactName, onDismiss)
                }
            }
        }
    }
}

// ─── Componentes auxiliares ───────────────────────────────────────────────────

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(72.dp))
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun RiskChip(risk: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.18f)) {
        Text(
            risk, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 1 — Pill expansível
// Cápsula compacta com dot de risco. Toca para expandir detalhes.
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StylePill(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = NavyMid,
            shadowElevation = 16.dp,
            modifier = Modifier
                .clickable { expanded = !expanded }
                .border(1.5.dp, color.copy(alpha = 0.6f), RoundedCornerShape(50.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                Text(
                    contactName ?: number,
                    color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 160.dp)
                )
                RiskChip(risk, color)
                Icon(Icons.Default.Close, null, tint = TextSecondary,
                    modifier = Modifier.size(16.dp).clickable { onDismiss() })
            }
        }

        AnimatedVisibility(visible = expanded,
            enter = expandVertically(spring(dampingRatio = 0.7f)) + fadeIn(),
            exit  = shrinkVertically() + fadeOut()
        ) {
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp), color = NavyMid, shadowElevation = 12.dp,
                modifier = Modifier.widthIn(max = 320.dp).border(1.dp, NavyLight, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (contactName != null) {
                        DetailRow(Icons.Default.Person,   "Contacto",    contactName, AccentGreen)
                        DetailRow(Icons.Default.Phone,    "Número",      number,      TextSecondary)
                    } else {
                        DetailRow(Icons.Default.Phone,    "Número",      number,      TextSecondary)
                    }
                    DetailRow(Icons.Default.Category, "Categoria",   category,    AccentBlue)
                    if (subcategory.isNotBlank())
                        DetailRow(Icons.Default.Info, "Subcategoria", subcategory, TextSecondary)
                    Text("ligaram.me", color = TextSecondary.copy(alpha = 0.4f), fontSize = 10.sp,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 2 — Banner expansível
// Linha compacta com ícone de risco. Toca para ver detalhes.
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleBanner(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NavyDeep.copy(alpha = 0.97f),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(contactName ?: number, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                        Text(risk, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Close, null, tint = TextSecondary,
                    modifier = Modifier.size(18.dp).clickable { onDismiss() })
            }
        }

        AnimatedVisibility(visible = expanded,
            enter = expandVertically(spring(dampingRatio = 0.7f)) + fadeIn(),
            exit  = shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                color = NavyMid,
                modifier = Modifier.fillMaxWidth().border(1.dp, NavyLight, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (contactName != null) {
                        DetailRow(Icons.Default.Person,   "Contacto",    contactName, AccentGreen)
                        DetailRow(Icons.Default.Phone,    "Número",      number,      TextSecondary)
                    }
                    DetailRow(Icons.Default.Category, "Categoria",   category,    AccentBlue)
                    if (subcategory.isNotBlank())
                        DetailRow(Icons.Default.Info, "Subcategoria", subcategory, TextSecondary)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 3 — Banner completo (toda a informação visível, botão X)
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleBannerFull(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NavyDeep.copy(alpha = 0.97f),
        shadowElevation = 16.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(contactName ?: number, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (contactName != null)
                        Text(number, color = TextSecondary, fontSize = 11.sp)
                    else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                            Text(risk, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
                Icon(Icons.Default.Close, null, tint = TextSecondary,
                    modifier = Modifier.size(18.dp).clickable { onDismiss() })
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NavyLight))
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (contactName != null)
                    DetailRow(Icons.Default.Warning, "Risco", risk, color)
                DetailRow(Icons.Default.Category, "Categoria", category, AccentBlue)
                if (subcategory.isNotBlank())
                    DetailRow(Icons.Default.Info, "Subcategoria", subcategory, TextSecondary)
                Text("ligaram.me", color = TextSecondary.copy(alpha = 0.35f), fontSize = 9.sp,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 4 — Banner completo tap-to-dismiss
// Igual ao 3 mas fecha ao tocar. Drag também funciona: distingue tap de drag
// medindo o deslocamento total — se for pequeno é tap, caso contrário é drag.
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleBannerFullTap(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    // O drag é gerido aqui dentro (não no OverlayScreen) para poder distinguir tap de drag.
    var offsetY by remember { mutableStateOf(0f) }
    var totalDrag by remember { mutableStateOf(0f) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NavyDeep.copy(alpha = 0.97f),
        shadowElevation = 16.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            // Um único pointerInput que trata drag E tap sem conflito
            .pointerInput(onDismiss) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent().changes.firstOrNull() ?: continue
                        if (!down.pressed) continue
                        totalDrag = 0f
                        var dragging = false
                        // Seguir o dedo enquanto está pressionado
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) {
                                // Dedo levantou — se não arrastou muito, é tap → dismiss
                                if (!dragging) onDismiss()
                                break
                            }
                            val dy = change.position.y - change.previousPosition.y
                            totalDrag += abs(dy)
                            if (totalDrag > 10f) dragging = true
                            if (dragging) {
                                offsetY += dy
                                change.consume()
                            }
                        }
                    }
                }
            }
    ) {
        Column {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(contactName ?: number, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (contactName != null)
                        Text(number, color = TextSecondary, fontSize = 11.sp)
                    else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                            Text(risk, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
                // Hint subtil no lugar do X
                Icon(Icons.Default.TouchApp, null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NavyLight))
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (contactName != null)
                    DetailRow(Icons.Default.Warning, "Risco", risk, color)
                DetailRow(Icons.Default.Category, "Categoria", category, AccentBlue)
                if (subcategory.isNotBlank())
                    DetailRow(Icons.Default.Info, "Subcategoria", subcategory, TextSecondary)
                Text("CallRadar por ligaram.me", color = TextSecondary.copy(alpha = 0.50f), fontSize = 9.sp,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 5 — Card com barra lateral colorida
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleCard(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp), color = NavyMid, shadowElevation = 20.dp,
        modifier = Modifier.widthIn(max = 300.dp).padding(horizontal = 24.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(5.dp).fillMaxHeight()
                .background(Brush.verticalGradient(listOf(color, color.copy(alpha = 0.3f)))))
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (contactName != null) {
                            Text(contactName, color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(number, color = TextSecondary, fontSize = 11.sp)
                        } else {
                            Text(number, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                RiskChip(risk, color)
                Spacer(Modifier.height(6.dp))
                Text(category, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subcategory.isNotBlank())
                    Text(subcategory, color = TextSecondary.copy(alpha = 0.6f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Text("ligaram.me", color = TextSecondary.copy(alpha = 0.35f), fontSize = 9.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 6 — Split: identidade à esquerda, risco à direita
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleSplit(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp), color = NavyMid, shadowElevation = 18.dp,
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().border(1.dp, NavyLight, RoundedCornerShape(16.dp))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Chamada Recebida", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                Text(contactName ?: number, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (contactName != null)
                    Text(number, color = TextSecondary, fontSize = 11.sp)
                Spacer(Modifier.height(2.dp))
                Text(category, color = AccentBlue, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subcategory.isNotBlank())
                    Text(subcategory, color = TextSecondary.copy(alpha = 0.7f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.weight(1f))
                Text("ligaram.me", color = TextSecondary.copy(alpha = 0.35f), fontSize = 9.sp)
            }
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(NavyLight))
            Box(
                modifier = Modifier.width(90.dp).fillMaxHeight().background(color.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(12.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.18f)) {
                        Text(risk, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(8.dp))
                    Icon(Icons.Default.Close, null, tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp).clickable { onDismiss() })
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 7 — Score card com barra visual de risco
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleScore(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    // Mapeamento de risco para fracção da barra (sem depender do rating)
    val barFraction = when {
        risk.contains("Alto",    ignoreCase = true) ||
                risk.contains("Elevado", ignoreCase = true) ||
                risk.contains("High",    ignoreCase = true) -> 0.85f
        risk.contains("Medio",   ignoreCase = true) ||
                risk.contains("Médio",   ignoreCase = true) ||
                risk.contains("Moderado",ignoreCase = true) -> 0.5f
        else -> 0.2f
    }

    Surface(
        shape = RoundedCornerShape(18.dp), color = NavyMid, shadowElevation = 20.dp,
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.05f)))),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.PhoneInTalk, null, tint = color, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(contactName ?: number, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (contactName != null) Text(number, color = TextSecondary, fontSize = 11.sp)
                }
                Icon(Icons.Default.Close, null, tint = TextSecondary,
                    modifier = Modifier.size(18.dp).clickable { onDismiss() })
            }
            Spacer(Modifier.height(16.dp))
            Text("Nível de risco", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(NavyLight)) {
                Box(modifier = Modifier.fillMaxWidth(barFraction).fillMaxHeight().clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color))))
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Baixo", color = TextSecondary, fontSize = 9.sp)
                Text(risk, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Alto", color = TextSecondary, fontSize = 9.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(6.dp), color = AccentBlue.copy(alpha = 0.12f)) {
                    Text(category, color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (subcategory.isNotBlank()) {
                    Surface(shape = RoundedCornerShape(6.dp), color = NavyLight.copy(alpha = 0.5f)) {
                        Text(subcategory, color = TextSecondary, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("ligaram.me", color = TextSecondary.copy(alpha = 0.35f), fontSize = 9.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 8 — Chip flutuante no canto inferior direito
// Toca na seta para expandir painel completo. Drag no chip.
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleFloatingChip(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var offsetY by remember { mutableStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp, bottom = 32.dp)
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .pointerInput(Unit) { detectDragGestures { _, d -> offsetY += d.y } }
    ) {
        AnimatedVisibility(visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Bottom, animationSpec = spring(dampingRatio = 0.7f)) + fadeIn(),
            exit  = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp),
                color = NavyMid, shadowElevation = 16.dp,
                modifier = Modifier.widthIn(max = 260.dp)
                    .border(1.dp, NavyLight, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp))
                    .padding(bottom = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (contactName != null) {
                        DetailRow(Icons.Default.Person,   "Contacto",    contactName, AccentGreen)
                        DetailRow(Icons.Default.Phone,    "Número",      number,      TextSecondary)
                    } else {
                        DetailRow(Icons.Default.Phone,    "Número",      number,      TextSecondary)
                    }
                    DetailRow(Icons.Default.Warning,  "Risco",       risk,        color)
                    DetailRow(Icons.Default.Category, "Categoria",   category,    AccentBlue)
                    if (subcategory.isNotBlank())
                        DetailRow(Icons.Default.Info, "Subcategoria", subcategory, TextSecondary)
                    Text("ligaram.me", color = TextSecondary.copy(alpha = 0.35f), fontSize = 9.sp,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(50.dp), color = NavyDeep, shadowElevation = 12.dp,
            modifier = Modifier.border(1.5.dp, color, RoundedCornerShape(50.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(12.dp))
                }
                Text(risk, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Icon(if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    null, tint = TextSecondary, modifier = Modifier.size(16.dp).clickable { expanded = !expanded })
                Box(modifier = Modifier.width(1.dp).height(16.dp).background(NavyLight))
                Icon(Icons.Default.Close, null, tint = TextSecondary,
                    modifier = Modifier.size(16.dp).clickable { onDismiss() })
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 9 — Minimal pill sem expansão
// Ultra-compacto: só risco + nome + fechar. Zero clutter.
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleMinimal(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50.dp), color = NavyDeep.copy(alpha = 0.95f), shadowElevation = 12.dp,
        modifier = Modifier.padding(horizontal = 32.dp).border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(50.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(contactName ?: number, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 140.dp))
            Box(modifier = Modifier.width(1.dp).height(14.dp).background(NavyLight))
            Text(risk, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Close, null, tint = TextSecondary,
                modifier = Modifier.size(15.dp).clickable { onDismiss() })
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 10 — Banner topo com linha de cor e detalhes visíveis
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleBannerTop(
    number: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Surface(
            shape = RoundedCornerShape(14.dp), color = NavyDeep.copy(alpha = 0.97f), shadowElevation = 20.dp,
            modifier = Modifier.fillMaxWidth().border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(3.dp)
                    .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.3f)))))
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contactName ?: number, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(risk, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.Close, null, tint = TextSecondary,
                        modifier = Modifier.size(18.dp).clickable { onDismiss() })
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NavyLight))
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (contactName != null) DetailRow(Icons.Default.Phone, "Número", number, TextSecondary)
                    DetailRow(Icons.Default.Category, "Categoria", category, AccentBlue)
                    if (subcategory.isNotBlank())
                        DetailRow(Icons.Default.Info, "Subcategoria", subcategory, TextSecondary)
                    Text("ligaram.me", color = TextSecondary.copy(alpha = 0.35f), fontSize = 9.sp,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}