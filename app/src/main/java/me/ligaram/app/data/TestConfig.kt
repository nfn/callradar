package me.ligaram.app.data

// ╔══════════════════════════════════════════════════════════════════════════════╗
// ║                                                                              ║
// ║   ██████╗ ███████╗███╗   ███╗ ██████╗     ███╗   ███╗ ██████╗ ██████╗ ███████╗ ║
// ║   ██╔══██╗██╔════╝████╗ ████║██╔═══██╗    ████╗ ████║██╔═══██╗██╔══██╗██╔════╝ ║
// ║   ██║  ██║█████╗  ██╔████╔██║██║   ██║    ██╔████╔██║██║   ██║██║  ██║█████╗   ║
// ║   ██║  ██║██╔══╝  ██║╚██╔╝██║██║   ██║    ██║╚██╔╝██║██║   ██║██║  ██║██╔══╝   ║
// ║   ██████╔╝███████╗██║ ╚═╝ ██║╚██████╔╝    ██║ ╚═╝ ██║╚██████╔╝██████╔╝███████╗ ║
// ║   ╚═════╝ ╚══════╝╚═╝     ╚═╝ ╚═════╝     ╚═╝     ╚═╝ ╚═════╝ ╚═════╝ ╚══════╝ ║
// ║                                                                              ║
// ╠══════════════════════════════════════════════════════════════════════════════╣
// ║  FICHEIRO DE CONFIGURAÇÃO DE TESTES — REMOVER / DESATIVAR ANTES DE PRODUÇÃO ║
// ║                                                                              ║
// ║  PARA LANÇAR EM PRODUÇÃO:                                                    ║
// ║    1. Mudar DEMO_MODE = false  (o toggle desaparece automaticamente)         ║
// ║    2. Ou apagar este ficheiro e remover as referências marcadas com:         ║
// ║       // ⚠️ DEMO_MODE                                                       ║
// ║                                                                              ║
// ║  Ficheiros com referências a este objecto:                                   ║
// ║    • Screens.kt          (card de aviso no HomeScreen)                       ║
// ║    • OverlayActivity.kt  (bypass dos dados da API)                           ║
// ╚══════════════════════════════════════════════════════════════════════════════╝

import android.content.Context
import android.content.SharedPreferences

object TestConfig {

    // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
    // ⚠️ DEMO_MODE — PORTÃO MESTRE
    // true  → modo demo ativo: toggle visível, overlay usa dados fictícios
    // false → modo produção: toggle invisível, app comporta-se normalmente
    // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
    const val DEMO_MODE = true
    // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

    private const val PREFS_NAME   = "demo_prefs"
    private const val KEY_ENABLED  = "demo_overlay_enabled"
    private const val KEY_SCENARIO = "demo_scenario_index"

    // ── Toggle runtime (persiste entre sessões) ───────────────────────────────
    fun isDemoOverlayEnabled(context: Context): Boolean {
        if (!DEMO_MODE) return false
        return prefs(context).getBoolean(KEY_ENABLED, true)
    }

    fun setDemoOverlayEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    // ── Cenários rotativos ────────────────────────────────────────────────────
    // Cada chamada avança para o próximo cenário: Alto → Médio → Seguro → Alto…
    // O índice persiste para que a rotação não reinicie ao abrir a app.

    val SCENARIOS = listOf(
        DemoScenario(
            number      = "213 456 789",
            risk        = "Risco Alto",
            category    = "Fraude",
            subcategory = "Falso investimento",
            rating      = "1.2",
            contactName = null
        ),
        DemoScenario(
            number      = "910 123 456",
            risk        = "Risco Médio",
            category    = "Telemarketing",
            subcategory = "Seguros",
            rating      = "2.8",
            contactName = null
        ),
        DemoScenario(
            number      = "225 987 654",
            risk        = "Risco Baixo",
            category    = "Empresa",
            subcategory = "Atendimento ao cliente",
            rating      = "4.5",
            contactName = "Vodafone Portugal"
        )
    )

    fun nextScenario(context: Context): DemoScenario {
        val prefs = prefs(context)
        val current = prefs.getInt(KEY_SCENARIO, 0)
        val next    = (current + 1) % SCENARIOS.size
        prefs.edit().putInt(KEY_SCENARIO, next).apply()
        return SCENARIOS[current]
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

// ── Modelo de cenário demo ────────────────────────────────────────────────────
data class DemoScenario(
    val number:      String,
    val risk:        String,
    val category:    String,
    val subcategory: String,
    val rating:      String,
    val contactName: String?
)
