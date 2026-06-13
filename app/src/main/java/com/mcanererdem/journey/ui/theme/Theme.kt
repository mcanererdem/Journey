package com.mcanererdem.journey.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Sanctum Theme - Dark Mode (Celestial Gold)
 */
private val SanctumDarkColorScheme = darkColorScheme(
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
 * Sanctum Theme - Light Mode (Ivory Gold)
 */
private val SanctumLightColorScheme = lightColorScheme(
    primary               = ColorSanctumPrimary,
    onPrimary             = Color.White,
    primaryContainer      = Color(0xFFF8F3E9),
    onPrimaryContainer    = Color(0xFF5A4010),
    secondary             = ColorSanctumSecondary,
    background            = Color(0xFFFAF9F6),
    onBackground          = Color(0xFF1C1608),
    surface               = Color.White,
    onSurface             = Color(0xFF2C2518),
    surfaceVariant        = Color(0xFFEBE8E0),
    onSurfaceVariant      = Color(0xFF5D4E67),
    outline               = Color(0xFFD6CFC7),
    outlineVariant        = Color(0xFFE5E0DA),
    error                 = ColorDanger
)

/**
 * Covenant Theme - Dark Mode (Void Purple)
 */
private val CovenantDarkColorScheme = darkColorScheme(
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
 * Covenant Theme - Light Mode (Lavender Void)
 */
private val CovenantLightColorScheme = lightColorScheme(
    primary               = ColorCovenantPrimary,
    onPrimary             = Color.White,
    primaryContainer      = Color(0xFFF2EAFA),
    onPrimaryContainer    = Color(0xFF3A1060),
    secondary             = ColorCovenantSecondary,
    background            = Color(0xFFF9F7FC),
    onBackground          = Color(0xFF0E0818),
    surface               = Color.White,
    onSurface             = Color(0xFF1A1225),
    surfaceVariant        = Color(0xFFE9E4F0),
    onSurfaceVariant      = Color(0xFF5D4E67),
    outline               = Color(0xFFCDC6D4),
    outlineVariant        = Color(0xFFE1DCE8),
    error                 = ColorDanger
)

@Composable
fun RpgTheme(
    side: String = "NEUTRAL",
    uiMode: String = "DARK", // "LIGHT", "DARK", "SYSTEM"
    content: @Composable () -> Unit
) {
    val isDark = when (uiMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when (side) {
        "SANCTUM" -> if (isDark) SanctumDarkColorScheme else SanctumLightColorScheme
        "COVENANT" -> if (isDark) CovenantDarkColorScheme else CovenantLightColorScheme
        else -> {
             // Neutral Fallback
             if (isDark) {
                 darkColorScheme(
                    primary = ColorNeutralPrimary,
                    onPrimary = ColorBackground,
                    background = ColorBackground,
                    onBackground = ColorOnBackground,
                    surface = ColorSurface,
                    onSurface = ColorOnSurface,
                    outline = ColorBorder
                 )
             } else {
                 SanctumLightColorScheme // Reuse light gold for neutral light
             }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = JourneyTypography,
        content     = content
    )
}
