package me.ligaram.app.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ligaram.app.ui.theme.*
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

    /** Estado observável para que onNewIntent atualize a UI quando a mesma instância é reutilizada (singleInstance). */
    private var overlayDataState = mutableStateOf(OverlayData("", "0", "", "", "", null))

    private fun overlayDataFromIntent(intent: Intent): OverlayData = OverlayData(
        number = intent.getStringExtra("number") ?: "",
        rating = intent.getStringExtra("rating") ?: "0",
        risk = intent.getStringExtra("risk") ?: "",
        category = intent.getStringExtra("category") ?: "",
        subcategory = intent.getStringExtra("subcategory") ?: "",
        contactName = intent.getStringExtra("contactName")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep this activity in its own visual layer so the dialer remains visible.
        // FLAG_NOT_TOUCH_MODAL: touches outside the overlay card pass through to dialer.
        // FLAG_LAYOUT_IN_SCREEN: fills screen without pushing dialer away.
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
            // Overlay is always dark — it floats above the phone dialer
            LigaramTheme(darkTheme = true) {
                OverlayScreen(
                    number = data.number,
                    rating = data.rating,
                    risk = data.risk,
                    category = data.category,
                    subcategory = data.subcategory,
                    contactName = data.contactName,
                    onDismiss = { finish() }
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
        // Garantir que a UI reflete sempre o Intent atual (ex.: activity reutilizada sem onNewIntent ou intent atualizado pelo sistema).
        val current = overlayDataFromIntent(intent)
        if (current.number != overlayDataState.value.number || current.rating != overlayDataState.value.rating) {
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
    risk.contains("Alto", ignoreCase = true) ||
    risk.contains("Elevado", ignoreCase = true) ||
    risk.contains("High", ignoreCase = true) -> RiskHigh
    risk.contains("Medio", ignoreCase = true) ||
    risk.contains("Médio", ignoreCase = true) ||
    risk.contains("Moderado", ignoreCase = true) -> RiskMedium
    else -> RiskLow
}

// ─── Overlay selector — change OVERLAY_STYLE to 1, 2 or 3 ───────────────────
private const val OVERLAY_STYLE = 1

@Composable
fun OverlayScreen(
    number: String,
    rating: String,
    risk: String,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    val color = riskColor(risk)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Drag offset — shared across all styles
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(spring(dampingRatio = 0.6f, stiffness = 500f)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, offsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            offsetY += dragAmount.y
                        }
                    }
            ) {
                when (OVERLAY_STYLE) {
                    1 -> StylePill(number, rating, risk, color, category, subcategory, contactName, onDismiss)
                    2 -> StyleCard(number, rating, risk, color, category, subcategory, contactName, onDismiss)
                    else -> StyleBanner(number, rating, risk, color, category, subcategory, contactName, onDismiss)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 1 — Pill / Chip compacto
// Mostra risco + categoria numa cápsula. Toca para expandir detalhes.
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StylePill(
    number: String,
    rating: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        // Collapsed pill
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
                // Risk dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                // Name or number
                Text(
                    text = contactName ?: number,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 160.dp)
                )
                // Risk label
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = color.copy(alpha = 0.18f)
                ) {
                    Text(
                        risk,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                // Rating
                Text(
                    "★ $rating",
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                // Close
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDismiss() }
                )
            }
        }

        // Expanded detail panel
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(spring(dampingRatio = 0.7f)) + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NavyMid,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .border(1.dp, NavyLight, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (contactName != null) {
                        PillDetailRow(Icons.Default.Person, "Contacto", contactName, AccentGreen)
                        PillDetailRow(Icons.Default.Phone, "Número", number, TextSecondary)
                    } else {
                        PillDetailRow(Icons.Default.Phone, "Número", number, TextSecondary)
                    }
                    PillDetailRow(Icons.Default.Category, "Categoria", category, AccentBlue)
                    PillDetailRow(Icons.Default.Info, "Subcategoria", subcategory, TextSecondary)
                    Text(
                        "ligaram.me",
                        color = TextSecondary.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PillDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Column {
            Text(label, color = TextSecondary, fontSize = 10.sp)
            Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 2 — Card flutuante compacto com gradiente lateral
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleCard(
    number: String,
    rating: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = NavyMid,
        shadowElevation = 20.dp,
        modifier = Modifier
            .widthIn(max = 300.dp)
            .padding(horizontal = 24.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Colored left bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(color, color.copy(alpha = 0.3f)))
                    )
            )
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (contactName != null) {
                            Text(contactName, color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(number, color = TextSecondary, fontSize = 11.sp)
                        } else {
                            Text(number, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    // Rating badge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("★ $rating", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Risk chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text(
                        risk,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(category, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subcategory.isNotBlank()) {
                    Text(subcategory, color = TextSecondary.copy(alpha = 0.6f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(6.dp))
                Text("ligaram.me", color = TextSecondary.copy(alpha = 0.35f), fontSize = 9.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STYLE 3 — Banner horizontal ultra-compacto (1 linha + ícone)
// Ideal se quiseres o mínimo de intrusão possível
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StyleBanner(
    number: String,
    rating: String,
    risk: String,
    color: Color,
    category: String,
    subcategory: String,
    contactName: String?,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NavyDeep.copy(alpha = 0.97f),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Risk icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        contactName ?: number,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(
                            "$risk · $category",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text("★ $rating", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null, tint = TextSecondary, modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.Close, null,
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDismiss() }
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(spring(dampingRatio = 0.7f)) + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                color = NavyMid,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyLight, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (contactName != null) {
                        BannerDetailRow(Icons.Default.Person, "Contacto", contactName, AccentGreen)
                        BannerDetailRow(Icons.Default.Phone, "Número", number, TextSecondary)
                    }
                    BannerDetailRow(Icons.Default.Category, "Categoria", category, AccentBlue)
                    if (subcategory.isNotBlank()) {
                        BannerDetailRow(Icons.Default.Info, "Subcategoria", subcategory, TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun BannerDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(72.dp))
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
