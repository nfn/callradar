package me.ligaram.app.data

import android.content.Context
import androidx.core.content.edit

enum class OverlayStyle(val id: Int, val label: String, val description: String) {
    PILL        (1,  "Pill expansível",     "Cápsula compacta - toca para ver detalhes"),
    BANNER      (2,  "Banner expansível",   "Linha com ícone - toca para expandir"),
    BANNER_FULL (3,  "Banner completo",     "Toda a informação visível de imediato"),
    CARD        (5,  "Card lateral",        "Card com barra lateral colorida"),
    SPLIT       (6,  "Split identidade",    "Identidade à esquerda, risco à direita"),
    SCORE       (7,  "Score card",          "Barra de risco com pontuação visual"),
    FLOATING    (8,  "Chip flutuante",      "Chip no canto - arrasta e expande"),
    MINIMAL     (9,  "Minimal pill",        "Ultra-compacto, só risco e nome"),
    BANNER_TOP  (10, "Banner topo",         "Faixa no topo com linha de cor");

    companion object {
        fun fromId(id: Int) = entries.firstOrNull { it.id == id } ?: BANNER_FULL
    }
}

object OverlayPreferences {
    private const val PREFS_NAME  = "overlay_prefs"
    private const val KEY_STYLE   = "overlay_style"
    private const val KEY_SUGGEST = "suggest_comment"

    fun getStyle(context: Context): OverlayStyle =
        OverlayStyle.fromId(
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_STYLE, OverlayStyle.PILL.id)
        )

    fun setStyle(context: Context, style: OverlayStyle) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putInt(KEY_STYLE, style.id) }
    }

    // Toggle ligado por omissão. Se a chave não existe (nova instalação ou
    // primeira vez nesta versão), grava true para que o estado fique persistido.
    fun getSuggestComment(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (!prefs.contains(KEY_SUGGEST)) {
            prefs.edit { putBoolean(KEY_SUGGEST, true) }
            true
        } else {
            prefs.getBoolean(KEY_SUGGEST, true)
        }
    }

    fun setSuggestComment(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_SUGGEST, enabled) }
    }
}
