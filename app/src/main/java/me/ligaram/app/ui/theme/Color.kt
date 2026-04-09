package me.ligaram.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Neutral palette (Slate) ───────────────────────────────────────────────────
// Tons 0 (preto) → 100 (branco), seguindo a convenção M3
val Neutral6   = Color(0xFF0A1628) // bgSecondary dark (fundo gradiente)
val Neutral10  = Color(0xFF0F172A) // background dark
val Neutral20  = Color(0xFF1E293B) // surface dark
val Neutral30  = Color(0xFF334155) // surfaceVariant dark / outline
val Neutral50  = Color(0xFF64748B) // onSurfaceVariant light
val Neutral60  = Color(0xFF94A3B8) // onSurfaceVariant dark
val Neutral90  = Color(0xFFE2E8F0) // surfaceVariant light / outline light
val Neutral95  = Color(0xFFF1F5F9) // bgSecondary light (fundo gradiente)
val Neutral99  = Color(0xFFF8FAFC) // background light / onBackground dark
val Neutral100 = Color(0xFFFFFFFF) // surface light (branco puro)

// ── Primary palette (Blue) ────────────────────────────────────────────────────
val Primary40  = Color(0xFF3B82F6) // ações primárias, links, foco

// ── Secondary palette (Green) ─────────────────────────────────────────────────
val Secondary40 = Color(0xFF10B981) // sucesso, permissões concedidas, risco baixo

// ── Tertiary palette (Amber) ──────────────────────────────────────────────────
val Tertiary40  = Color(0xFFF59E0B) // avisos, risco médio, estrelas neutras

// ── Error palette (Red) ───────────────────────────────────────────────────────
val Error40     = Color(0xFFEF4444) // erros, risco alto, perigoso

// ── Custom semantic (fora do sistema M3) ──────────────────────────────────────
val StarOrange  = Color(0xFFF97316) // rating 2/5 estrelas
val StarLime    = Color(0xFF84CC16) // rating 4/5 estrelas
