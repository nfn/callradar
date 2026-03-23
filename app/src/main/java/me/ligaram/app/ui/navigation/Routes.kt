package me.ligaram.app.ui.navigation

// ─── Navigation Routes ────────────────────────────────────────────────────────
object Routes {
    const val HOME             = "home"
    const val WALKTHROUGH      = "walkthrough"
    const val ABOUT            = "about"
    const val SETTINGS         = "settings"
    const val COMMUNITY_HOME   = "community_home"
    const val COMMUNITY_NUMBER = "community_number"
    const val ADD_COMMENT      = "add_comment"
    const val OVERLAY_STYLE    = "overlay_style"
}

// ─── Animation Constants ──────────────────────────────────────────────────────
// Constantes de animação partilhadas por todas as rotas
const val ANIM_DURATION = 280
const val SLIDE_OFFSET  = 0.30f   // 30% da largura — elimina a faixa lateral
