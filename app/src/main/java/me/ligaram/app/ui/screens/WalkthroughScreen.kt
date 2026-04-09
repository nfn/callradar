package me.ligaram.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.ligaram.app.ui.components.AppBackground
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.Primary40
import me.ligaram.app.ui.theme.Secondary40
import me.ligaram.app.ui.theme.Tertiary40

data class WalkthroughPage(
    val title: String,
    val description: String,
    val heroIcon: ImageVector,
    val points: List<Pair<ImageVector, String>>,
    val accentColor: Color
)

@Composable
fun WalkthroughScreen(
    onFinish: () -> Unit
) {
    val pages = remember {
        listOf(
            WalkthroughPage(
                title       = "Proteção em tempo real",
                description = "A app identifica chamadas recebidas e mostra informação útil durante a chamada.",
                heroIcon    = Icons.Default.PhoneInTalk,
                accentColor = Primary40,
                points = listOf(
                    Icons.Default.Bolt    to "Deteta o número em tempo real",
                    Icons.Default.Group   to "Consulta a comunidade ligaram.me",
                    Icons.Default.Shield  to "Mostra risco e contexto no ecrã"
                )
            ),
            WalkthroughPage(
                title       = "Porque pedimos permissões",
                description = "As permissões servem apenas para as funções de proteção e para melhorar a experiência.",
                heroIcon    = Icons.Default.Layers,
                accentColor = Tertiary40,
                points = listOf(
                    Icons.Default.Phone         to "Chamadas: identificar quem liga",
                    Icons.Default.Layers        to "Sobreposição: mostrar aviso durante chamada",
                    Icons.Default.Notifications to "Notificações (opcional): sugerir comentário após chamada curta"
                )
            ),
            WalkthroughPage(
                title       = "Resultados para ti",
                description = "Com mais contexto antes de atender, decides mais rápido e com maior confiança.",
                heroIcon    = Icons.Default.CheckCircle,
                accentColor = Secondary40,
                points = listOf(
                    Icons.Default.Block        to "Reduz chamadas indesejadas",
                    Icons.Default.CheckCircle  to "Decisão mais informada ao atender",
                    Icons.Default.Favorite     to "Contributo para proteger outros utilizadores"
                )
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
            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinish) {
                    Text("Saltar")
                }
            }

            // Card principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    // Gradiente de cor no topo do card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f)
                            .align(Alignment.TopCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        page.accentColor.copy(alpha = 0.18f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Ícone hero com fundo circular colorido
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(page.accentColor.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = page.heroIcon,
                                contentDescription = null,
                                tint               = page.accentColor,
                                modifier           = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            text      = page.title,
                            color     = MaterialTheme.colorScheme.onSurface,
                            style     = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text      = page.description,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            style     = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Bullets com ícone
                        Column(
                            modifier            = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            page.points.forEach { (icon, text) ->
                                Row(
                                    modifier          = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(page.accentColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector        = icon,
                                            contentDescription = null,
                                            tint               = page.accentColor,
                                            modifier           = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text  = text,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Indicadores de página + botão
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPage) 10.dp else 7.dp)
                                .background(
                                    color = if (index == currentPage) AccentBlue
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick  = { if (isLastPage) onFinish() else currentPage += 1 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLastPage) "Começar" else "Seguinte")
                }
            }
        }
    }
}
