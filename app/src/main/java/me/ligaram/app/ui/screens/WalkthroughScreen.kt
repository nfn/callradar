package me.ligaram.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ligaram.app.ui.components.AppBackground
import me.ligaram.app.ui.theme.AccentBlue

data class WalkthroughPage(
    val title: String,
    val description: String,
    val points: List<String>,
    val icon: @Composable () -> Unit
)

@Composable
fun WalkthroughScreen(
    onFinish: () -> Unit
) {
    val pages = remember {
        listOf(
            WalkthroughPage(
                title = "Proteção em tempo real",
                description = "A app identifica chamadas recebidas e mostra informação útil durante a chamada.",
                points = listOf(
                    "Deteta o número em tempo real",
                    "Consulta a comunidade ligaram.me",
                    "Mostra risco e contexto no ecrã"
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(52.dp)
                    )
                }
            ),
            WalkthroughPage(
                title = "Porque pedimos permissões",
                description = "As permissões servem apenas para as funções de proteção e para melhorar a experiência.",
                points = listOf(
                    "Chamadas: identificar quem liga",
                    "Sobreposição: mostrar aviso durante chamada",
                    "Notificações (opcional): sugerir comentário após chamada curta"
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(52.dp)
                    )
                }
            ),
            WalkthroughPage(
                title = "Resultados para ti",
                description = "Com mais contexto antes de atender, decides mais rápido e com maior confiança.",
                points = listOf(
                    "Reduz chamadas indesejadas",
                    "Decisão mais informada ao atender",
                    "Contributo para proteger outros utilizadores"
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(52.dp)
                    )
                }
            )
        )
    }

    var currentPage by remember { mutableIntStateOf(0) }
    val isLastPage = currentPage == pages.lastIndex
    val page = pages[currentPage]

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinish) {
                    Text("Saltar")
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    page.icon()
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = page.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = page.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    page.points.forEach { point ->
                        Text(
                            text = "- $point",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPage) 10.dp else 8.dp)
                                .background(
                                    color = if (index == currentPage) AccentBlue else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isLastPage) onFinish() else currentPage += 1
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLastPage) "Começar" else "Seguinte")
                }
            }
        }
    }
}
