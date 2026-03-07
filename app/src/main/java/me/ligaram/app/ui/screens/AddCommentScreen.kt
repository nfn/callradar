package me.ligaram.app.ui.screens

import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ligaram.app.data.CommunityApi
import me.ligaram.app.data.CommunityResult
import me.ligaram.app.data.EntityItem
import me.ligaram.app.data.PostCommentRequest
import me.ligaram.app.ui.theme.AccentBlue
import me.ligaram.app.ui.theme.RiskHigh

private val ADD_CLASSIFICATIONS = listOf("Perigoso", "Suspeito", "Incómodo", "Seguro", "Neutro")

private val ADD_CLASS_ICONS = mapOf(
    "Perigoso" to Icons.Default.Warning,
    "Suspeito" to Icons.AutoMirrored.Filled.Help,
    "Incómodo" to Icons.Default.NotificationsOff,
    "Seguro"   to Icons.Default.CheckCircle,
    "Neutro"   to Icons.Default.Remove
)

@Composable
fun AddCommentScreen(navController: NavController, number: String) {
    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboard     = LocalSoftwareKeyboardController.current

    var name           by remember { mutableStateOf("") }
    var commentText    by remember { mutableStateOf("") }
    var entity         by remember { mutableStateOf("") }
    var rating         by remember { mutableIntStateOf(0) }
    var classification by remember { mutableStateOf("") }

    var entitySuggestions by remember { mutableStateOf<List<EntityItem>>(emptyList()) }
    var showSuggestions   by remember { mutableStateOf(false) }
    var entityJob         by remember { mutableStateOf<Job?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }

    val canSubmit = commentText.isNotBlank() && rating > 0 && classification.isNotBlank()

    fun dismissKeyboardAndSuggestions() {
        showSuggestions   = false
        entitySuggestions = emptyList()
        focusManager.clearFocus()
        keyboard?.hide()
    }

    fun submit() {
        if (!canSubmit || isSubmitting) return
        dismissKeyboardAndSuggestions()
        isSubmitting = true
        errorMsg     = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                CommunityApi.postComment(
                    PostCommentRequest(
                        number         = number,
                        name           = name.ifBlank { null },
                        comment        = commentText.trim(),
                        entity         = entity.ifBlank { null },
                        rating         = rating,
                        classification = classification
                    )
                )
            }
            when (result) {
                is CommunityResult.Success -> {
                    delay(400)
                    // Volta ao screen do número e força reload
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh", true)
                    navController.popBackStack()
                }
                is CommunityResult.Error -> {
                    errorMsg     = result.message
                    isSubmitting = false
                }
            }
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 12.dp, top = 48.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Novo comentário - ${formatPhoneNumber(number)}",
                        color      = MaterialTheme.colorScheme.onBackground,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // ── Formulário (scroll) ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(pass = PointerEventPass.Main)
                            dismissKeyboardAndSuggestions()
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Nome (opcional)
                SheetSection("Nome (opcional)") {
                    OutlinedTextField(
                        value           = name,
                        onValueChange   = { name = it.take(128) },
                        modifier        = Modifier.fillMaxWidth(),
                        placeholder     = { Text("O teu nome ou alcunha", fontSize = 13.sp) },
                        leadingIcon     = {
                            Icon(Icons.Default.Person, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        singleLine      = true,
                        shape           = RoundedCornerShape(12.dp),
                        colors          = sheetFieldColors(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType   = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words
                        )
                    )
                }

                // Entidade (autocomplete)
                SheetSection("Entidade (opcional)") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value         = entity,
                            onValueChange = { value ->
                                entity = value.take(100)
                                entityJob?.cancel()
                                if (value.length >= 2) {
                                    showSuggestions = true
                                    entityJob = scope.launch {
                                        delay(300)
                                        val result = withContext(Dispatchers.IO) {
                                            CommunityApi.searchEntities(value)
                                        }
                                        if (result is CommunityResult.Success) {
                                            entitySuggestions = result.data.take(6)
                                            showSuggestions   = entitySuggestions.isNotEmpty()
                                        }
                                    }
                                } else {
                                    entitySuggestions = emptyList()
                                    showSuggestions   = false
                                }
                            },
                            modifier    = Modifier.fillMaxWidth(),
                            placeholder = { Text("NOS, EDP, Banco… (ou escreve livremente)",
                                fontSize = 13.sp) },
                            leadingIcon  = {
                                Icon(Icons.Default.Business, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingIcon = {
                                if (entity.isNotEmpty()) {
                                    IconButton(onClick = {
                                        entity            = ""
                                        entitySuggestions = emptyList()
                                        showSuggestions   = false
                                    }) {
                                        Icon(Icons.Default.Close, null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            singleLine      = true,
                            shape           = RoundedCornerShape(12.dp),
                            colors          = sheetFieldColors(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { dismissKeyboardAndSuggestions() }
                            )
                        )

                        // Dropdown autocomplete para CIMA
                        androidx.compose.animation.AnimatedVisibility(
                            visible  = showSuggestions && entitySuggestions.isNotEmpty(),
                            enter    = expandVertically(expandFrom = Alignment.Bottom),
                            exit     = shrinkVertically(shrinkTowards = Alignment.Bottom),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .offset(y = (-64).dp)
                        ) {
                            Surface(
                                shape           = RoundedCornerShape(12.dp),
                                color           = MaterialTheme.colorScheme.surface,
                                shadowElevation = 6.dp,
                                tonalElevation  = 2.dp,
                                modifier        = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(12.dp))
                            ) {
                                Column {
                                    val snapshot = remember(entitySuggestions) {
                                        entitySuggestions.toList()
                                    }
                                    snapshot.forEachIndexed { idx, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication        = null
                                                ) {
                                                    entity            = item.entity
                                                    showSuggestions   = false
                                                    entitySuggestions = emptyList()
                                                    focusManager.clearFocus()
                                                    keyboard?.hide()
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(Icons.Default.Business, null,
                                                tint     = AccentBlue,
                                                modifier = Modifier.size(16.dp))
                                            Text(item.entity,
                                                color    = MaterialTheme.colorScheme.onBackground,
                                                fontSize = 14.sp)
                                        }
                                        if (idx < snapshot.lastIndex) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Comentário *
                SheetSection("Comentário *") {
                    OutlinedTextField(
                        value          = commentText,
                        onValueChange  = { commentText = it.take(2000) },
                        modifier       = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                        placeholder    = { Text("Descreve a tua experiência com este número…",
                            fontSize = 13.sp) },
                        shape          = RoundedCornerShape(12.dp),
                        colors         = sheetFieldColors(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType   = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        supportingText = {
                            Text("${commentText.length}/2000",
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp)
                        }
                    )
                }

                // Classificação *
                SheetSection("Classificação *") {
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(6.dp)
                    ) {
                        ADD_CLASSIFICATIONS.forEach { cls ->
                            val selected = classification == cls
                            val clsColor = classificationColor(cls)
                            androidx.compose.material3.FilterChip(
                                selected = selected,
                                onClick  = { classification = cls },
                                label    = { Text(cls, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(ADD_CLASS_ICONS[cls] ?: Icons.Default.Circle, null,
                                        modifier = Modifier.size(15.dp))
                                },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor   = clsColor.copy(alpha = 0.15f),
                                    selectedLabelColor       = clsColor,
                                    selectedLeadingIconColor = clsColor,
                                    containerColor           = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor               = MaterialTheme.colorScheme.onSurfaceVariant,
                                    iconColor                = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                                    enabled             = true,
                                    selected            = selected,
                                    borderColor         = MaterialTheme.colorScheme.outline,
                                    selectedBorderColor = clsColor
                                )
                            )
                        }
                    }
                }

                // Avaliação *
                SheetSection("Avaliação *") {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        repeat(5) { idx ->
                            val star = idx + 1
                            IconButton(onClick = { rating = star },
                                modifier = Modifier.size(44.dp)) {
                                Icon(
                                    if (star <= rating) Icons.Default.Star
                                    else Icons.Default.StarBorder,
                                    "$star estrelas",
                                    tint     = if (rating > 0) starColor(rating)
                                               else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        if (rating > 0) {
                            Text(when (rating) {
                                1 -> "Muito mau"; 2 -> "Mau"; 3 -> "Neutro"
                                4 -> "Bom";       else -> "Excelente"
                            }, color = starColor(rating),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Erro
                if (errorMsg != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = RiskHigh.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = RiskHigh,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(errorMsg!!, color = RiskHigh, fontSize = 13.sp,
                                modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ── Botão submeter ────────────────────────────────────────────────
            Surface(
                color           = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier        = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick  = ::submit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(52.dp),
                    shape   = RoundedCornerShape(14.dp),
                    enabled = canSubmit && !isSubmitting,
                    colors  = ButtonDefaults.buttonColors(
                        containerColor         = AccentBlue,
                        disabledContainerColor = MaterialTheme.colorScheme.outline
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp),
                            color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar comentário",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
