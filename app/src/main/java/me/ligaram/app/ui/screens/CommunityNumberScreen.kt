package me.ligaram.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import me.ligaram.app.data.LikeCache
import me.ligaram.app.data.NumberAnalysis
import me.ligaram.app.data.NumberComment
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.AccentOrange
import me.ligaram.app.ui.theme.RiskHigh
import me.ligaram.app.ui.theme.RiskLow

// PRÉ-PRODUÇÃO - [Bug] - popBackStack() em vez de navigate(COMMUNITY_HOME):
// volta ao entry COMMUNITY_HOME já existente no backstack — o MainShell
// não é recriado, o listState do CommunityHomeScreen mantém a posição.
// Fallback para navigate() se COMMUNITY_HOME não estiver no backstack
// (ex: entrada directa pelo overlay)
private fun NavController.backToCommunity() {
    val wentBack = popBackStack(Routes.COMMUNITY_HOME, inclusive = false)
    if (!wentBack) {
        navigate(Routes.COMMUNITY_HOME) {
            popUpTo(Routes.HOME) { inclusive = false }
        }
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
    var errorMsg     by remember { mutableStateOf<String?>(null) }

    fun loadPage(cursor: Int? = null) {
        if (isLoading && !isRefreshing) return
        isLoading = true
        if (cursor == null) {
            // reset completo — garante que todos os campos JSON ficam actualizados
            comments     = emptyList()
            analysis     = null
            numberRating = null
            hasMore      = true
            nextCursor   = null
            notFound     = false
            errorMsg     = null
        }
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
                    errorMsg     = null
                }
                is CommunityResult.Error -> {
                    if (result.message.contains("404")) notFound = true
                    else errorMsg = result.message
                }
            }
            isLoading    = false
            isRefreshing = false
        }
    }

    LaunchedEffect(number) { loadPage() }

    // Refresh quando volta do AddCommentScreen
    val refreshSignal = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh", false)
    LaunchedEffect(refreshSignal) {
        refreshSignal?.collect { shouldRefresh ->
            if (shouldRefresh) {
                navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
                loadPage()
            }
        }
    }

    // Infinite scroll
    val shouldLoadMore by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            hasMore && !isLoading && errorMsg == null && total > 0 && last >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) loadPage(nextCursor) }

    // Scroll ao topo quando o refresh termina
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && listState.firstVisibleItemIndex > 0) {
            listState.animateScrollToItem(0)
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 48.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.backToCommunity() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                Text("Número - ${formatPhoneNumber(number)}",
                    color    = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
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
                        Text(formatPhoneNumber(number), color = MaterialTheme.colorScheme.onBackground,
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
                    when {
                        comments.isEmpty() && isLoading && !isRefreshing -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AccentBlue)
                            }
                        }
                        comments.isEmpty() && errorMsg != null -> {
                            CommunityErrorState(message = errorMsg!!, onRetry = {
                                errorMsg = null
                                loadPage()
                            })
                        }
                        notFound || (comments.isEmpty() && !isLoading) -> {
                            EmptyCommentsState(number = number, onAdd = {
                                navController.navigate("${Routes.ADD_COMMENT}/$number")
                            })
                        }
                        else -> {
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

                                // ── Separador "Comentários" ────────────────────
                                if (comments.isNotEmpty()) {
                                    item(key = "comments_header") {
                                        Row(
                                            modifier          = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                "Comentários",
                                                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize   = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.8.sp
                                            )
                                            HorizontalDivider(
                                                modifier = Modifier.weight(1f),
                                                color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            )
                                        }
                                    }
                                }

                                items(comments, key = { it.id }) { comment ->
                                    NumberCommentCard(
                                        comment        = comment,
                                        onLikeToggled  = { id, isLiked, newCount ->
                                            // Actualiza o estado local desta screen
                                            comments = comments.map {
                                                if (it.id == id) it.copy(likes = newCount) else it
                                            }
                                            // PRÉ-PRODUÇÃO - [Melhoria] - escreve no LikeCache partilhado
                                            // O CommunityHomeScreen lê directamente deste cache via
                                            // LikeCache.getLikes() — sem savedStateHandle, sem re-navegação
                                            LikeCache.update(id, isLiked, newCount)
                                        }
                                    )
                                }

                                item {
                                    when {
                                        errorMsg != null && comments.isNotEmpty() -> {
                                            Row(
                                                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment     = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Erro ao carregar mais",
                                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 12.sp)
                                                androidx.compose.material3.TextButton(onClick = {
                                                    errorMsg = null
                                                    loadPage(nextCursor)
                                                }) {
                                                    Text("Tentar", color = AccentBlue, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                        isLoading -> {
                                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentBlue, strokeWidth = 2.dp)
                                            }
                                        }
                                        !hasMore -> {
                                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                                Text("Não há mais comentários", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // FAB sobre a lista
                FloatingActionButton(
                    onClick        = { navController.navigate("${Routes.ADD_COMMENT}/$number") },
                    modifier       = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                    containerColor = AccentBlue,
                    contentColor   = Color.White,
                    shape          = RoundedCornerShape(16.dp)
                ) { Icon(Icons.Default.AddComment, "Comentar") }
            }
        }
    }
}

// ─── Analysis card (colapsável) ───────────────────────────────────────────────
@Composable
fun NumberAnalysisCard(analysis: NumberAnalysis) {
    var expanded by remember { mutableStateOf(false) }

    val riskColor = when (analysis.riskLevel) {
        "Risco Alto"        -> RiskHigh
        "Risco Médio"       -> AccentOrange
        "Risco Baixo"       -> RiskLow
        "Risco Desconhecido" -> Color(0xFF94A3B8)
        else                -> Color(0xFF94A3B8)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.06f)),
        border   = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.3f)),
        onClick  = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Linha 1: ícone + "Análise"  |  badge de risco ────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Esquerda - ícone + label
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(riskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Analytics, null,
                            tint     = riskColor,
                            modifier = Modifier.size(16.dp))
                    }
                    Text("Análise",
                        color      = MaterialTheme.colorScheme.onBackground,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold)
                }

                // Direita - badge de risco pill + chevron
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!analysis.riskLevel.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = riskColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier              = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(riskColor)
                                )
                                Text(analysis.riskLevel,
                                    color      = riskColor,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                        tint     = riskColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Linha 2: Categoria · Subcategoria ────────────────────────────
            val hasCat    = !analysis.category.isNullOrBlank()
            val hasSubCat = !analysis.subcategory.isNullOrBlank()
            if (hasCat || hasSubCat) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (hasCat) {
                        Text(analysis.category,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                    if (hasCat && hasSubCat) {
                        Text("·",
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                    if (hasSubCat) {
                        Text(analysis.subcategory,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Conteúdo expandido ────────────────────────────────────────────
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {

                    HorizontalDivider(color = riskColor.copy(alpha = 0.2f))

                    // Sobre este número - seoSummary
                    if (!analysis.seoSummary.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(analysis.seoSummary,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize   = 13.sp,
                            lineHeight  = 19.sp)
                    }

                    // Recomendação - advice
                    if (!analysis.advice.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = riskColor.copy(alpha = 0.10f)
                        ) {
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Default.Info, null,
                                        tint     = riskColor,
                                        modifier = Modifier.size(12.dp))
                                    Text("RECOMENDAÇÃO",
                                        color         = riskColor,
                                        fontSize      = 10.sp,
                                        fontWeight    = FontWeight.Bold,
                                        letterSpacing = 0.6.sp)
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(analysis.advice,
                                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize   = 13.sp,
                                    lineHeight  = 19.sp,
                                    fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }

            // Dica apenas quando colapsado
            if (!expanded) {
                Spacer(Modifier.height(6.dp))
                Text("Toque para ver a análise completa",
                    color    = riskColor.copy(alpha = 0.6f),
                    fontSize = 11.sp)
            }
        }
    }
}

// ─── Comment card com like interactivo ───────────────────────────────────────
// Row 1: nome (esq) | classification (dir)
// Row 2: separador
// Row 3: comentário
// Row 4: hora (esq) | like (dir)
@Composable
fun NumberCommentCard(
    comment: NumberComment,
    onLikeToggled: (commentId: Int, liked: Boolean, newCount: Int) -> Unit
) {
    val scope      = rememberCoroutineScope()
    val classColor = classificationColor(comment.classification)

    var localLikes  by remember(comment.id) { mutableStateOf(LikeCache.getLikes(comment.id, comment.likes)) }
    // PRÉ-PRODUÇÃO - [Bug] - sem cache, localLiked inicializava sempre como false mesmo que
    // o utilizador já tivesse dado like nesta sessão — ao interagir de novo o optimismo
    // calculava +1 sobre um valor já incrementado, causando o flash 2 → 0
    var localLiked  by remember(comment.id) { mutableStateOf(LikeCache.getLiked(comment.id) ?: false) }
    var likeLoading by remember(comment.id) { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Row 1: badge inicial + nome (esq) | classification (dir)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier              = Modifier.weight(1f)
                ) {
                    Box(
                        modifier         = Modifier.size(36.dp).clip(CircleShape).background(classColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = comment.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                        Text(initial, color = classColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(
                        comment.name?.ifBlank { "Anónimo" } ?: "Anónimo",
                        color      = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    )
                }
                if (!comment.classification.isNullOrBlank()) {
                    Surface(shape = RoundedCornerShape(8.dp), color = classColor.copy(alpha = 0.12f)) {
                        Text(
                            comment.classification,
                            color      = classColor,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Row 2: separador
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(Modifier.height(10.dp))

            // Row 3: comentário
            if (!comment.comment.isNullOrBlank()) {
                Text(
                    comment.comment,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize   = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(10.dp))
            }

            // Row 4: hora (esq) | like (dir)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    timeAgo(comment.createdAt),
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                // Botão de like
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick  = {
                            if (likeLoading) return@IconButton
                            val wasLiked = localLiked
                            localLiked = !wasLiked
                            localLikes = if (!wasLiked) localLikes + 1 else (localLikes - 1).coerceAtLeast(0)
                            likeLoading = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) { CommunityApi.toggleLike(comment.id) }
                                likeLoading = false
                                when (result) {
                                    is CommunityResult.Success -> {
                                        localLiked = result.data.liked
                                        localLikes = result.data.likes
                                        onLikeToggled(comment.id, result.data.liked, result.data.likes)
                                    }
                                    is CommunityResult.Error -> {
                                        localLiked = wasLiked
                                        localLikes = if (wasLiked) localLikes + 1 else (localLikes - 1).coerceAtLeast(0)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = if (localLiked) "Remover gosto" else "Gostar",
                            tint     = if (localLiked) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        "$localLikes",
                        color      = if (localLiked) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize   = 12.sp,
                        fontWeight = if (localLiked) FontWeight.SemiBold else FontWeight.Normal
                    )
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
