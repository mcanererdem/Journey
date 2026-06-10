package com.example.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Journey Dark Fantasy — Merkezi boyut sabitleri.
 * Tüm padding, spacing, radius ve elevation değerleri buradan alınır.
 * Hiçbir dosyada inline dp/sp değeri YAZILMAZ.
 */
object Dimens {

    // ── Spacing ──────────────────────────────────────────────────────────
    val SpacingXxs  = 2.dp
    val SpacingXs   = 4.dp
    val SpacingS    = 8.dp
    val SpacingM    = 12.dp
    val SpacingL    = 16.dp
    val SpacingXl   = 20.dp
    val SpacingXxl  = 24.dp
    val SpacingXxxl = 32.dp
    val SpacingHuge = 48.dp

    // ── Corner Radius ────────────────────────────────────────────────────
    val RadiusXs     = 4.dp
    val RadiusS      = 8.dp
    val RadiusM      = 12.dp
    val RadiusL      = 16.dp
    val RadiusXl     = 20.dp
    val RadiusCircle = 50.dp   // Pill shape

    // ── Icon Sizes ───────────────────────────────────────────────────────
    val IconXs  = 14.dp
    val IconS   = 18.dp
    val IconM   = 24.dp
    val IconL   = 32.dp
    val IconXl  = 48.dp
    val IconXxl = 64.dp

    // ── Component Sizes ──────────────────────────────────────────────────
    val StatBarHeight        = 8.dp
    val StatBarHeightThick   = 12.dp
    val BottomNavHeight      = 64.dp
    val TopBarHeight         = 56.dp
    val NodeCardMinHeight    = 120.dp
    val CombatPanelMinHeight = 200.dp
    val AvatarSize           = 56.dp
    val BadgeSize            = 32.dp

    // ── Border & Stroke ──────────────────────────────────────────────────
    val BorderThin   = 1.dp
    val BorderNormal = 1.5.dp
    val BorderThick  = 2.dp
    val BorderGlow   = 3.dp

    // ── Elevation ────────────────────────────────────────────────────────
    val ElevationNone  = 0.dp
    val ElevationLow   = 2.dp
    val ElevationMid   = 6.dp
    val ElevationHigh  = 12.dp

    // ── Typography (sp) ──────────────────────────────────────────────────
    val TextXxs    = 10.sp
    val TextXs     = 11.sp
    val TextS      = 12.sp
    val TextM      = 14.sp
    val TextL      = 16.sp
    val TextXl     = 18.sp
    val TextXxl    = 22.sp
    val TextTitle  = 26.sp
    val TextHero   = 32.sp

    // ── Letter Spacing ───────────────────────────────────────────────────
    val LetterSpacingNone   = 0.sp
    val LetterSpacingTight  = 0.5.sp
    val LetterSpacingNormal = 1.sp
    val LetterSpacingWide   = 2.sp
    val LetterSpacingXWide  = 3.sp

    // ── Line Height ──────────────────────────────────────────────────────
    val LineHeightBody  = 22.sp
    val LineHeightTitle = 30.sp
    val LineHeightHero  = 38.sp

    // ── Animation Durations (ms) ─────────────────────────────────────────
    const val AnimFast   = 200
    const val AnimNormal = 300
    const val AnimSlow   = 500
    const val AnimBoss   = 800
}
