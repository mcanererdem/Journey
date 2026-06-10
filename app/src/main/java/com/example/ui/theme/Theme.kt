package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// ══════════════════════════════════════════════════════════════════════════════
// Journey Dark Fantasy — Tema Sistemi
// Tüm temalar koyu (dark) tabanlıdır — dark fantasy atmosferi için.
// Momentum değerine göre dinamik tema geçişi desteklenir.
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Sanctum Teması — Celestial Gold.
 * Işık, düzen ve kutsallık. Altın kenarlıklar, sıcak koyu arka plan.
 */
private val SanctumColorScheme = darkColorScheme(
    primary               = ColorSanctumPrimary,
    onPrimary             = ColorBackground,
    primaryContainer      = ColorSanctumSurface,
    onPrimaryContainer    = ColorSanctumText,
    secondary             = ColorSanctumSecondary,
    onSecondary           = ColorBackground,
    background            = ColorBackground,
    onBackground          = ColorOnBackground,
    surface               = ColorSurface,
    onSurface             = ColorOnSurface,
    surfaceVariant        = ColorSurfaceVariant,
    onSurfaceVariant      = ColorOnSurfaceMuted,
    outline               = ColorSanctumBorder,
    outlineVariant        = ColorBorderMuted,
    error                 = ColorDanger,
    onError               = ColorOnBackground,
    scrim                 = ColorScrimDark
)

/**
 * Covenant Teması — Void Purple.
 * Karanlık, kaos ve güç. Mor parıltılar, obsidyen arkaplan.
 */
private val CovenantColorScheme = darkColorScheme(
    primary               = ColorCovenantPrimary,
    onPrimary             = ColorOnBackground,
    primaryContainer      = ColorCovenantSurface,
    onPrimaryContainer    = ColorCovenantText,
    secondary             = ColorCovenantSecondary,
    onSecondary           = ColorOnBackground,
    background            = ColorBackground,
    onBackground          = ColorOnBackground,
    surface               = ColorSurface,
    onSurface             = ColorOnSurface,
    surfaceVariant        = ColorSurfaceVariant,
    onSurfaceVariant      = ColorOnSurfaceMuted,
    outline               = ColorCovenantBorder,
    outlineVariant        = ColorBorderMuted,
    error                 = ColorDanger,
    onError               = ColorOnBackground,
    scrim                 = ColorScrimDark
)

/**
 * Nötr / Demir Teması — Iron Gray.
 * Bağımsız, pragmatik, savaşçı. Koyu demir ve bronz tonları.
 */
private val NeutralColorScheme = darkColorScheme(
    primary               = ColorNeutralPrimary,
    onPrimary             = ColorBackground,
    primaryContainer      = ColorSurfaceVariant,
    onPrimaryContainer    = ColorNeutralText,
    secondary             = ColorNeutralSecondary,
    onSecondary           = ColorBackground,
    background            = ColorBackground,
    onBackground          = ColorOnBackground,
    surface               = ColorSurface,
    onSurface             = ColorOnSurface,
    surfaceVariant        = ColorSurfaceVariant,
    onSurfaceVariant      = ColorOnSurfaceMuted,
    outline               = ColorBorder,
    outlineVariant        = ColorBorderMuted,
    error                 = ColorDanger,
    onError               = ColorOnBackground,
    scrim                 = ColorScrimDark
)

/**
 * Ana Tema composable.
 *
 * @param side "SANCTUM", "COVENANT" veya "NEUTRAL" — renk şemasını belirler
 * @param content İçerik lambda
 */
@Composable
fun RpgTheme(
    side: String = "NEUTRAL",
    content: @Composable () -> Unit
) {
    val colorScheme = when (side) {
        "SANCTUM"  -> SanctumColorScheme
        "COVENANT" -> CovenantColorScheme
        else       -> NeutralColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = JourneyTypography,
        content     = content
    )
}

/**
 * Geriye dönük uyumluluk için.
 * Testler ve eski referanslar bu composable'ı kullanabilir.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RpgTheme(side = if (darkTheme) "NEUTRAL" else "SANCTUM", content = content)
}
