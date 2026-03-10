package me.ligaram.app.data

import androidx.compose.runtime.mutableStateMapOf

/**
 * Cache em memória de likes confirmados pela API.
 *
 * Partilhado entre CommunityNumberScreen e CommunityHomeScreen:
 *   - NumberScreen escreve após confirmação da API
 *   - HomeScreen lê para sobrepor o valor da lista sem fazer refresh
 *
 * Vive enquanto o processo estiver vivo (sessão da app).
 * Não persiste entre sessões — comportamento correcto: ao reabrir a app
 * os valores frescos vêm sempre da API.
 *
 * Chave: commentId (Int)
 */
object LikeCache {
    // mutableStateMapOf → Compose observa automaticamente as mudanças
    // Contagem de likes confirmada pela API
    val likes  = mutableStateMapOf<Int, Int>()
    // Estado liked/unliked confirmado pela API — evita o flash optimista
    // quando o utilizador entra de novo no comentário na mesma sessão
    val liked  = mutableStateMapOf<Int, Boolean>()

    fun update(commentId: Int, isLiked: Boolean, newCount: Int) {
        likes[commentId] = newCount
        liked[commentId] = isLiked
    }

    fun getLikes(commentId: Int, fallback: Int): Int =
        likes[commentId] ?: fallback

    fun getLiked(commentId: Int): Boolean? =
        liked[commentId]  // null = desconhecido (nunca interagiu nesta sessão)
}
