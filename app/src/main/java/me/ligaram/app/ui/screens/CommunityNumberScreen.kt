package me.ligaram.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ligaram.app.data.CommunityApi
import me.ligaram.app.data.CommunityResult
import me.ligaram.app.data.NumberAnalysis
import me.ligaram.app.data.NumberComment
import me.ligaram.app.ui.theme.*

// ─── Navegar sempre para a community home ─────────────────────────────────────
private fun NavController.backToCommunity() {
    navigate(Routes.COMMUNITY_HOME) {
        popUpTo(Routes.HOME) { inclusive = false }
    }
}

// ─── Number comments screen ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityNumberScreen(navController: NavController, number: String) {
    val scope     = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val ptrState  = rememberPullToRefreshState()

    // Intercepta o gesto/botão físico de back → vai sempre para community home
    BackHandler { navController.backToCommunity() }

    var comments     by remember { mutableStateOf<List<NumberComment>>(emptyList()) }
    var analysis     by remember { mutableStateOf<NumberAnalysis?>(null) }
    var numberRating by remember { mutableStateOf<String?>(null) }
    var isLoading    by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var hasMore      by remember { mutableStateOf(true) }
    var nextCursor   by remember { mutableStateOf<Int?>(null) }
    var notFound     by remember { mutableStateOf(false) }

    // Estado para o BottomSheet do formulário
    var showAddSheet by remember { mutableStateOf(false) }

    fun loadPage(cursor: Int? = null) {
        if (isLoading) return
        isLoading = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                CommunityApi.fetchComments(number, cursor = cursor)
            }
            when (result) {
                is CommunityResult.Success -> {
                    val page = result.data
                    numberRating = page.numberRating
                    analysis     = page.analysis
                    comments     = if (cursor == null) page.data else comments + page.data
                    hasMore      = page.pagination.hasMore
                    nextCursor   = page.pagination.nextCursor
                    notFound     = false
                }
                is CommunityResult.Error -> {
                    notFound = result.message.contains("404")
                }
            }
            isLoading    = false
            isRefreshing = false
        }
    }

    LaunchedEffect(number) { loadPage() }

    // Infinite scroll
    val shouldLoadMore by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            hasMore && !isLoading && total > 0 && last >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) loadPage(nextCursor) }

    // BottomSheet para adicionar comentário
    if (showAddSheet) {
        CommunityAddSheet(
            number    = number,
            onDismiss = { showAddSheet = false },
            onSuccess = { showAddSheet = false; loadPage() }
        )
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 12.dp, top = 48.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.backToCommunity() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(number,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                /*
                IconButton(onClick = { showAddSheet = true }) {
                    Icon(Icons.Default.AddComment, null, tint = AccentBlue)
                }
                 */
            }

            // ── Search bar ────────────────────────────────────────────────────
            PhoneSearchBar(onSearch = { n ->
                navController.navigate("${Routes.COMMUNITY_NUMBER}/$n") {
                    popUpTo("${Routes.COMMUNITY_NUMBER}/$number") { inclusive = true }
                }
            })

            // ── Número header card ────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.08f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Phone, null, tint = AccentBlue, modifier = Modifier.size(22.dp)) }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(number, color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("${comments.size} comentário${if (comments.size != 1) "s" else ""}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    if (!numberRating.isNullOrBlank() && numberRating != "null") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("★ $numberRating", color = AccentOrange,
                                fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text("avaliação", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        }
                    }
                }
            }

            // ── Lista com pull-to-refresh ─────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    comments.isEmpty() && isLoading && !isRefreshing -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentBlue)
                        }
                    }
                    notFound || (comments.isEmpty() && !isLoading) -> {
                        EmptyCommentsState(number = number, onAdd = { showAddSheet = true })
                    }
                    else -> {
                        PullToRefreshBox(
                            state        = ptrState,
                            isRefreshing = isRefreshing,
                            onRefresh    = {
                                if (!isRefreshing) {
                                    isRefreshing = true
                                    nextCursor   = null
                                    hasMore      = true
                                    loadPage(null)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                state          = listState,
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // ── Analysis card (apenas se existir) ─────────
                                analysis?.let { a ->
                                    item(key = "analysis") {
                                        NumberAnalysisCard(analysis = a)
                                    }
                                }

                                items(comments, key = { it.id }) { comment ->
                                    NumberCommentCard(comment = comment)
                                }

                                item {
                                    if (isLoading) {
                                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentBlue, strokeWidth = 2.dp)
                                        }
                                    } else if (!hasMore) {
                                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                            Text("Não há mais comentários", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // FAB sobre a lista
                FloatingActionButton(
                    onClick        = { showAddSheet = true },
                    modifier       = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                    containerColor = AccentBlue,
                    contentColor   = Color.White,
                    shape          = RoundedCornerShape(16.dp)
                ) { Icon(Icons.Default.AddComment, "Comentar") }
            }
        }
    }
}

// ─── Analysis card ────────────────────────────────────────────────────────────
@Composable
fun NumberAnalysisCard(analysis: NumberAnalysis) {
    val riskColor = when {
        analysis.riskLevel?.contains("Alto",    ignoreCase = true) == true ||
        analysis.riskLevel?.contains("Elevado", ignoreCase = true) == true -> RiskHigh
        analysis.riskLevel?.contains("Médio",   ignoreCase = true) == true ||
        analysis.riskLevel?.contains("Medio",   ignoreCase = true) == true -> AccentOrange
        analysis.riskLevel?.contains("Baixo",   ignoreCase = true) == true -> RiskLow
        else -> Color(0xFF94A3B8)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.06f)),
        border   = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Cabeçalho: nível de risco
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(riskColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Analytics, null, tint = riskColor, modifier = Modifier.size(18.dp)) }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Análise", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    if (!analysis.riskLevel.isNullOrBlank()) {
                        Text(analysis.riskLevel, color = riskColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                // Categoria como chip
                if (!analysis.category.isNullOrBlank()) {
                    Surface(shape = RoundedCornerShape(8.dp), color = riskColor.copy(alpha = 0.12f)) {
                        Text(analysis.category, color = riskColor, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }

            // Subcategoria
            if (!analysis.subcategory.isNullOrBlank()) {
                Text(analysis.subcategory, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            // Mensagem de aviso (warning_message)
            if (!analysis.warningMessage.isNullOrBlank()) {
                HorizontalDivider(color = riskColor.copy(alpha = 0.2f))
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, null, tint = riskColor, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                    Text(analysis.warningMessage, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

// ─── Comment card ─────────────────────────────────────────────────────────────
@Composable
fun NumberCommentCard(comment: NumberComment) {
    val classColor = classificationColor(comment.classification)
    val starsColor = comment.rating?.let { starColor(it) } ?: AccentOrange

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(classColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = comment.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    Text(initial, color = classColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(comment.name?.ifBlank { "Anónimo" } ?: "Anónimo",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    if (!comment.entity.isNullOrBlank()) {
                        Text(comment.entity, color = AccentBlue, fontSize = 11.sp)
                    }
                }
                if (!comment.classification.isNullOrBlank()) {
                    Surface(shape = RoundedCornerShape(8.dp), color = classColor.copy(alpha = 0.12f)) {
                        Text(comment.classification, color = classColor, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (!comment.comment.isNullOrBlank()) {
                Text(comment.comment, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp, lineHeight = 20.sp)
            }

            Spacer(Modifier.height(10.dp))

            // Footer: estrelas+data à esquerda, likes à direita
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (comment.rating != null) {
                        Row {
                            repeat(5) { idx ->
                                Icon(
                                    if (idx < comment.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    null, tint = starsColor, modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    Text(timeAgo(comment.createdAt), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Default.ThumbUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                    Text("${comment.likes}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────
@Composable
fun EmptyCommentsState(number: String, onAdd: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Forum, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text("Sem comentários para $number", color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Sê o primeiro a partilhar a tua experiência com este número.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAdd, shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
                Icon(Icons.Default.AddComment, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Adicionar comentário")
            }
        }
    }
}
