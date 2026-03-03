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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ligaram.app.ui.theme.*

class OverlayActivity : ComponentActivity() {

    companion object {
        const val ACTION_DISMISS = "me.ligaram.app.DISMISS_OVERLAY"
    }

    private val dismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_DISMISS) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val number = intent.getStringExtra("number") ?: ""
        val rating = intent.getStringExtra("rating") ?: "0"
        val risk = intent.getStringExtra("risk") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val subcategory = intent.getStringExtra("subcategory") ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dismissReceiver, IntentFilter(ACTION_DISMISS), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(dismissReceiver, IntentFilter(ACTION_DISMISS))
        }

        setContent {
            LigaramTheme {
                OverlayScreen(
                    number = number,
                    rating = rating,
                    risk = risk,
                    category = category,
                    subcategory = subcategory,
                    onDismiss = { finish() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dismissReceiver)
    }
}

@Composable
fun OverlayScreen(
    number: String,
    rating: String,
    risk: String,
    category: String,
    subcategory: String,
    onDismiss: () -> Unit
) {
    val ratingFloat = rating.toFloatOrNull() ?: 0f
    val riskColor = when {
        risk.contains("Alto", ignoreCase = true) || risk.contains("Elevado", ignoreCase = true) -> RiskHigh
        risk.contains("Medio", ignoreCase = true) || risk.contains("Médio", ignoreCase = true) -> RiskMedium
        else -> RiskLow
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            OverlayCard(
                number = number,
                rating = ratingFloat,
                ratingStr = rating,
                risk = risk,
                riskColor = riskColor,
                category = category,
                subcategory = subcategory,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun OverlayCard(
    number: String,
    rating: Float,
    ratingStr: String,
    risk: String,
    riskColor: Color,
    category: String,
    subcategory: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 48.dp)
            .shadow(24.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMid)
    ) {
        Column(modifier = Modifier.padding(0.dp)) {

            // Header gradient bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(riskColor.copy(alpha = 0.8f), riskColor.copy(alpha = 0.3f)))
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Chamada Identificada",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                // Number & Rating row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Número", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(
                            number,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Rating circle
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(riskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                ratingStr,
                                color = riskColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("/ 5", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Risk badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(riskColor.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(riskColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Nível de Risco:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(risk, color = riskColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category & Subcategory
                InfoRow(label = "Categoria", value = category, icon = Icons.Default.Category)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Subcategoria", value = subcategory, icon = Icons.Default.Info)

                Spacer(modifier = Modifier.height(16.dp))

                // Powered by
                Text(
                    "Identificado por ligaram.me",
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NavyLight.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, color = TextSecondary, fontSize = 11.sp)
            Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
