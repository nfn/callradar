package me.ligaram.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ligaram.app.data.CommunityApi
import me.ligaram.app.data.CommunityResult
import me.ligaram.app.data.HomeComment
import me.ligaram.app.ui.theme.*
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─── Time ago helper ──────────────────────────────────────────────────────────
fun timeAgo(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val now     = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val hours   = ChronoUnit.HOURS.between(instant, now)
        val days    = ChronoUnit.DAYS.between(instant, now)
        when {
            minutes < 1  -> "agora mesmo"
            minutes < 60 -> "há ${minutes}min"
            hours < 24   -> "há ${hours}h"
            days == 1L   -> "ontem"
            days < 7     -> "há ${days} dias"
            days / 7 == 1L -> "há 1 semana"
            days / 7 < 5   -> "há ${days / 7} semanas"
            days / 30 == 1L -> "há 1 mês"
            days / 30 < 12  -> "há ${days / 30} meses"
            days / 365 == 1L -> "há 1 ano"
            else             -> "há ${days / 365} anos"
        }
    } catch (_: Exception) { "" }
}

// ─── Classification color ─────────────────────────────────────────────────────
fun classificationColor(c: String?): Color = when (c) {
    "Perigoso" -> RiskHigh
    "Suspeito" -> AccentOrange
    "Incómodo" -> Color(0xFFF59E0B)
    "Seguro"   -> RiskLow
    "Neutro"   -> Color(0xFF94A3B8)
    else       -> Color(0xFF94A3B8)
}

// ─── Star color: 1=vermelho → 5=verde ────────────────────────────────────────
fun starColor(rating: Int): Color = when (rating) {
    1    -> Color(0xFFEF4444)
    2    -> Color(0xFFF97316)
    3    -> Color(0xFFF59E0B)
    4    -> Color(0xFF84CC16)
    5    -> Color(0xFF10B981)
    else -> Color(0xFF94A3B8)
}

// ─── Phone search bar ─────────────────────────────────────────────────────────
@Composable
fun PhoneSearchBar(onSearch: (String) -> Unit, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val focus   = LocalFocusManager.current
    val isValid = query.matches(Regex("^[239][0-9]{8}$"))

    OutlinedTextField(
        value         = query,
        onValueChange = { query = it.filter { c -> c.isDigit() }.take(9) },
        modifier      = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder   = {
            Text("Pesquisar número — ex: 912345678",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 14.sp)
        },
        leadingIcon  = {
            Icon(Icons.Default.Search, null,
                tint = if (isValid) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    if (isValid) { focus.clearFocus(); onSearch(query) } else query = ""
                }) {
                    Icon(
                        if (isValid) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Close,
                        null,
                        tint = if (isValid) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine      = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { if (isValid) { focus.clearFocus(); onSearch(query) } }),
        shape           = RoundedCornerShape(14.dp),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = AccentBlue,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
            focusedContainerColor   = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        isError = query.isNotEmpty() && !isValid
    )
}

// ─── Community Home Screen ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHomeScreen(navController: NavController) {
    val scope     = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val ptrState  = rememberPullToRefreshState()

    var items        by remember { mutableStateOf<List<HomeComment>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var hasMore      by remember { mutableStateOf(true) }
    var nextCursor   by remember { mutableStateOf<Int?>(null) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }

    fun loadPage(cursor: Int? = null) {
        if (isLoading) return
        isLoading = true
        errorMsg  = null
        scope.launch {
            val result = withContext(Dispatchers.IO) { CommunityApi.fetchHome(cursor = cursor) }
            when (result) {
                is CommunityResult.Success -> {
                    val page = result.data
                    items      = if (cursor == null) page.data else items + page.data
                    hasMore    = page.pagination.hasMore
                    nextCursor = page.pagination.nextCursor
                }
                is CommunityResult.Error -> errorMsg = result.message
            }
            isLoading    = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) { loadPage() }

    // Infinite scroll
    val shouldLoadMore by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            hasMore && !isLoading && total > 0 && last >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) loadPage(nextCursor) }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LigaramLogo(size = 28.dp)
                // Spacer(Modifier.width(10.dp))
                Text("Comunidade",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f))
            }

            PhoneSearchBar(onSearch = { n ->
                navController.navigate("${Routes.COMMUNITY_NUMBER}/$n")
            })

            when {
                items.isEmpty() && isLoading && !isRefreshing -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
                items.isEmpty() && errorMsg != null -> {
                    CommunityErrorState(message = errorMsg!!, onRetry = { loadPage() })
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
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(items, key = { it.id }) { item ->
                                HomeCommentCard(item = item, onClick = {
                                    navController.navigate("${Routes.COMMUNITY_NUMBER}/${item.number}")
                                })
                            }
                            item {
                                if (isLoading && items.isNotEmpty()) {
                                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentBlue, strokeWidth = 2.dp)
                                    }
                                } else if (!hasMore && items.isNotEmpty()) {
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
    }
}

// ─── Home comment card ────────────────────────────────────────────────────────
@Composable
fun HomeCommentCard(item: HomeComment, onClick: () -> Unit) {
    val classColor = classificationColor(item.classification)
    val ratingColor = item.rating?.let { starColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(classColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (item.classification) {
                            "Perigoso" -> Icons.Default.Warning
                            "Suspeito" -> Icons.AutoMirrored.Filled.Help
                            "Seguro"   -> Icons.Default.CheckCircle
                            else       -> Icons.Default.Phone
                        },
                        null, tint = classColor, modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.number, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (!item.classification.isNullOrBlank()) {
                        Text(item.classification, color = classColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                // Rating com cor gradiente vermelho→verde
                if (item.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        Icon(Icons.Default.Star, null, tint = ratingColor, modifier = Modifier.size(14.dp))
                        Text("${item.rating}", color = ratingColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            if (!item.name.isNullOrBlank()) {
                Text(item.name, color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
            }
            if (!item.comment.isNullOrBlank()) {
                Text(item.comment, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp, lineHeight = 19.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.height(10.dp))

            // Footer: data à esquerda, likes à direita
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(timeAgo(item.createdAt), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.ThumbUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                    Text("${item.likes}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─── Error state ──────────────────────────────────────────────────────────────
@Composable
fun CommunityErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.WifiOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry, shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
                Text("Tentar novamente")
            }
        }
    }
}
