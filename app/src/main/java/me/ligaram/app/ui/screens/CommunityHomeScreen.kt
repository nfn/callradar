package me.ligaram.app.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.AccentOrange
import me.ligaram.app.ui.theme.RiskHigh
import me.ligaram.app.ui.theme.RiskLow
import java.time.Instant
import java.time.temporal.ChronoUnit

fun timeAgo(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val now     = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val hours   = ChronoUnit.HOURS.between(instant, now)
        val days    = ChronoUnit.DAYS.between(instant, now)
        when {
            minutes < 1      -> "agora mesmo"
            minutes < 60     -> "há ${minutes}min"
            hours < 24       -> "há ${hours}h"
            days == 1L       -> "ontem"
            days < 7         -> "há ${days} dias"
            days / 7 == 1L   -> "há 1 semana"
            days / 7 < 5     -> "há ${days / 7} semanas"
            days / 30 == 1L  -> "há 1 mês"
            days / 30 < 12   -> "há ${days / 30} meses"
            days / 365 == 1L -> "há 1 ano"
            else             -> "há ${days / 365} anos"
        }
    } catch (_: Exception) { "" }
}

fun classificationColor(c: String?): Color = when (c) {
    "Perigoso" -> RiskHigh
    "Suspeito" -> AccentOrange
    "Incómodo" -> Color(0xFFF59E0B)
    "Seguro"   -> RiskLow
    "Neutro"   -> Color(0xFF94A3B8)
    else       -> Color(0xFF94A3B8)
}

fun starColor(rating: Int): Color = when (rating) {
    1    -> Color(0xFFEF4444)
    2    -> Color(0xFFF97316)
    3    -> Color(0xFFF59E0B)
    4    -> Color(0xFF84CC16)
    5    -> Color(0xFF10B981)
    else -> Color(0xFF94A3B8)
}

// Formatar número PT nacional (9 dígitos) → XXX XXX XXX
fun formatPhoneNumber(number: String): String {
    val digits = number.replace(Regex("[^0-9]"), "")
    return if (digits.length == 9)
        "${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6, 9)}"
    else number
}

@Composable
fun PhoneSearchBar(onSearch: (String) -> Unit, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val focus   = LocalFocusManager.current
    val isValid = query.matches(Regex("^[23789][0-9]{8}$"))

    OutlinedTextField(
        value         = query,
        onValueChange = { query = it.filter { c -> c.isDigit() }.take(9) },
        modifier      = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder   = {
            Text("Pesquisar número - ex: 912345678",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp)
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
        if (cursor == null) errorMsg = null
        scope.launch {
            val result = withContext(Dispatchers.IO) { CommunityApi.fetchHome(cursor = cursor) }
            when (result) {
                is CommunityResult.Success -> {
                    val page = result.data
                    items      = if (cursor == null) page.data else items + page.data
                    hasMore    = page.pagination.hasMore
                    nextCursor = page.pagination.nextCursor
                    errorMsg   = null
                }
                is CommunityResult.Error -> errorMsg = result.message
            }
            isLoading    = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) { loadPage() }

    val shouldLoadMore by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            hasMore && !isLoading && errorMsg == null && total > 0 && last >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) loadPage(nextCursor) }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Números",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f))
            }

            PhoneSearchBar(onSearch = { n -> navController.navigate("${Routes.COMMUNITY_NUMBER}/$n") })

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
                            if (!isRefreshing) { isRefreshing = true; nextCursor = null; hasMore = true; loadPage(null) }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state               = listState,
                            contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(items, key = { it.id }) { item ->
                                HomeCommentCard(item = item, onClick = {
                                    navController.navigate("${Routes.COMMUNITY_NUMBER}/${item.number}")
                                })
                            }
                            item {
                                when {
                                    errorMsg != null -> {
                                        // Erro durante paginação — mostrar inline com retry
                                        Column(
                                            modifier            = Modifier.fillMaxWidth().padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "Não foi possível carregar mais comentários.",
                                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize  = 13.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            androidx.compose.material3.TextButton(onClick = {
                                                errorMsg = null
                                                loadPage(nextCursor)
                                            }) {
                                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(15.dp), tint = AccentBlue)
                                                Spacer(Modifier.width(4.dp))
                                                Text("Tentar novamente", color = AccentBlue, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                    isLoading && items.isNotEmpty() -> {
                                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentBlue, strokeWidth = 2.dp)
                                        }
                                    }
                                    !hasMore && items.isNotEmpty() -> {
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
}

// Row 1: número (esq) | rating (dir)
// Row 2: separador
// Row 3: comentário
// Row 4: hora · autor · classification (esq) | like (dir)
@Composable
fun HomeCommentCard(item: HomeComment, onClick: () -> Unit) {
    val classColor  = classificationColor(item.classification)
    val ratingColor = item.rating?.let { starColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Row 1
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    Box(
                        modifier         = Modifier.size(36.dp).clip(CircleShape).background(classColor.copy(alpha = 0.15f)),
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
                    Text(formatPhoneNumber(item.number), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                if (item.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.Star, null, tint = ratingColor, modifier = Modifier.size(14.dp))
                        Text("%.1f".format(item.rating.toFloat()), color = ratingColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Row 2
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            // Row 3
            if (!item.comment.isNullOrBlank()) {
                Text(item.comment, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
            }

            // Row 4
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(timeAgo(item.createdAt), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    if (!item.name.isNullOrBlank()) {
                        Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), fontSize = 11.sp)
                        Text(item.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (!item.classification.isNullOrBlank()) {
                        Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), fontSize = 11.sp)
                        Surface(shape = RoundedCornerShape(5.dp), color = classColor.copy(alpha = 0.12f)) {
                            Text(item.classification, color = classColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.ThumbUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                    Text("${item.likes}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CommunityErrorState(message: String, onRetry: () -> Unit) {
    val isNetwork = message.contains("network", ignoreCase = true)
                || message.contains("connect", ignoreCase = true)
                || message.contains("rede",    ignoreCase = true)
                || message.contains("timeout", ignoreCase = true)
                || message.contains("Unable",  ignoreCase = true)

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Icon(
                if (isNetwork) Icons.Default.WifiOff else Icons.Default.ErrorOutline,
                null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(52.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                if (isNetwork) "Sem ligação à internet" else "Não foi possível carregar",
                color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (isNetwork) "Verifica a tua ligação e tenta novamente." else "O servidor pode estar temporariamente indisponível.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 19.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tentar novamente")
            }
        }
    }
}
