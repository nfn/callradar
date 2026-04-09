package me.ligaram.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import me.ligaram.app.ui.components.AboutSection
import me.ligaram.app.ui.components.AppBackground
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.AccentGreen
import me.ligaram.app.ui.theme.AccentOrange

@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current

    fun openSite() {
        context.startActivity(Intent(Intent.ACTION_VIEW, "https://ligaram.me".toUri()))
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick  = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        LigaramLogo(size = 56.dp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("CallRadar",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text("por ligaram.me",
                        color = AccentBlue,
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    // PRÉ-PRODUÇÃO - [Melhoria] - versão lida dinamicamente do manifesto
                    val versionName = remember {
                        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0" }
                        catch (_: Exception) { "1.0.0" }
                    }
                    Text("v$versionName",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    // Link para o site
                    OutlinedButton(
                        onClick = ::openSite,
                        shape   = RoundedCornerShape(10.dp),
                        border  = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                            tint     = AccentBlue,
                            modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ligaram.me", color = AccentBlue,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {

                AboutSection(
                    title = "O que é o CallRadar?",
                    content = "O CallRadar é uma aplicação de proteção contra chamadas indesejadas desenvolvida pelo ligaram.me. Quando recebe uma chamada, a app consulta automaticamente a base de dados e apresenta informação sobre o número - incluindo nível de risco, categoria e avaliação da comunidade."
                )

                Spacer(modifier = Modifier.height(16.dp))

                AboutSection(
                    title = "Como protege os utilizadores?",
                    content = "Através de uma sobreposição visual apresentada diretamente sobre o ecrã de chamada, o utilizador consegue ver antes de atender se o número está associado a fraude, spam, telemarketing ou outras categorias de risco."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Comunidade ligaram.me ─────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = AccentBlue.copy(alpha = 0.07f)),
                    border   = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.25f)),
                    onClick  = ::openSite
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Forum, null,
                                tint     = AccentBlue,
                                modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Comunidade ligaram.me",
                                color = AccentBlue,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "O CallRadar é também a aplicação oficial da comunidade de visitantes do ligaram.me. Pesquise números, consulte comentários de outros utilizadores, reporte chamadas suspeitas e ajude a proteger mais pessoas.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp, lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Visitar ligaram.me",
                                color      = AccentBlue,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                                tint     = AccentBlue,
                                modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Features
                Text("Funcionalidades", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                listOf(
                    Icons.Default.Bolt       to "Identificação em tempo real",
                    Icons.Default.Shield     to "Base de dados comunitária",
                    Icons.Default.Forum      to "Comunidade de reporte",
                    Icons.Default.Visibility to "Aviso sobre chamadas não intrusivo",
                    Icons.Default.Notifications to "Notificação após chamada curta",
                    Icons.Default.BatteryFull to "Baixo consumo de bateria",
                    Icons.Default.Lock       to "Sem armazenamento de dados pessoais",
                    Icons.Default.Update     to "Base de dados sempre atualizada"
                ).forEach { (icon, text) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Permissões ────────────────────────────────────────────────
                Text("Permissões utilizadas", color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                listOf(
                    Triple(Icons.Default.Phone, "READ_PHONE_STATE",
                        "Permite detetar quando uma chamada é recebida, para que a identificação ocorra em tempo real sem necessidade de interação do utilizador."),
                    Triple(Icons.Default.History, "READ_CALL_LOG",
                        "Permite ler o número de telefone da chamada recebida. Este número é enviado à API do ligaram.me apenas para consulta de risco - nunca é armazenado localmente."),
                    Triple(Icons.Default.Contacts, "READ_CONTACTS",
                        "Utilizado para mostrar o nome do contacto no aviso sobre chamadas, caso o número já exista na agenda. Nenhum dado de contacto é transmitido ou armazenado."),
                    Triple(Icons.Default.Notifications, "POST_NOTIFICATIONS",
                        "Permite apresentar notificações com sugestão de comentário após chamadas curtas de números sem dados na base de dados. Esta permissão é opcional e pode ser desativada nas Definições da app."),
                    Triple(Icons.Default.Layers, "SYSTEM_ALERT_WINDOW",
                        "Necessário para apresentar o aviso de identificação sobre a interface de chamada do sistema. Sem esta permissão, a informação de risco não pode ser exibida durante a chamada.")
                ).forEach { (icon, perm, desc) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(perm, color = AccentBlue, fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Bateria ───────────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.07f)),
                    border   = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryFull, null,
                                tint     = AccentOrange,
                                modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Funcionamento em segundo plano",
                                color      = AccentOrange,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Para garantir que o CallRadar funciona corretamente em todas as chamadas, recomendamos duas configurações no Android:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp, lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Item 1
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AccentOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", color = AccentOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Desativar otimização de bateria",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "Aceda a Definições → Bateria → Otimização de bateria e exclua o CallRadar. Alguns fabricantes (Xiaomi, Samsung, Huawei) podem encerrar o serviço de deteção de chamadas se esta opção estiver ativa.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp, lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        // Item 2
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AccentOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", color = AccentOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Excluir da lista de apps não utilizadas",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "Alguns sistemas Android desativam automaticamente apps que não são abertas com frequência. Aceda a Definições → Apps → CallRadar e desative a opção \"Pausar app se não usada\" ou equivalente.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp, lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Privacy
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PrivacyTip, null, tint = AccentGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Privacidade", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "A aplicação envia apenas o número da chamada recebida à API do ligaram.me para consulta. Nenhum dado pessoal, histórico de chamadas ou informação de contacto é armazenado ou transmitido.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer com link
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    /*
                    // Remoção do link no rodapé

                    TextButton(onClick = ::openSite) {
                        Text("ligaram.me",
                            color      = AccentBlue,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                     */
                    Text(
                        "© 2026 · ligaram.me · Todos os direitos reservados",
                        color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize  = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

