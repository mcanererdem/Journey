package com.mcanererdem.journey.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
// Journey Dark Fantasy — Merkezi Renk Paleti
// Tüm renkler semantic isimle tanımlanır.
// Hiçbir dosyada inline Color(0xFF...) YAZILMAZ.
// ══════════════════════════════════════════════════════════════════════════════

// ── Arkaplan & Yüzey ─────────────────────────────────────────────────────────
val ColorBackground         = Color(0xFF0C080E)   // Plum Black — ana arkaplan
val ColorSurface            = Color(0xFF140E19)   // Plum Stone — kart/panel arkaplanı
val ColorSurfaceVariant     = Color(0xFF1B1322)   // Slightly lifted surface
val ColorSurfaceHighlight   = Color(0xFF231A2C)   // Hover / selected surface

// ── Kenarlık & Ayırıcılar ────────────────────────────────────────────────────
val ColorBorder             = Color(0xFF2C1A32)   // Plum Border — standart kenarlık
val ColorBorderMuted        = Color(0xFF1F1223)   // Subtle divider
val ColorBorderGlow         = Color(0xFF452650)   // Glowing border (active)

// ── Metin ────────────────────────────────────────────────────────────────────
val ColorOnBackground       = Color(0xFFFAF6FF)   // Primary text on dark bg
val ColorOnSurface          = Color(0xFFD3C6DF)   // Secondary text on surface
val ColorOnSurfaceMuted     = Color(0xFF8D7C9A)   // Muted / disabled text
val ColorOnSurfaceSubtle    = Color(0xFF5D4E67)   // Very subtle hint text

// ── Sanctum / Işık Teması (Celestial Gold) ────────────────────────────────────
val ColorSanctumPrimary     = Color(0xFFC8A94A)   // Antique Gold — Sanctum ana
val ColorSanctumSecondary   = Color(0xFF8A6E22)   // Tarnished Gold — Sanctum ikincil
val ColorSanctumGlow        = Color(0xFFFFD980)   // Warm Gold Glow — parıltı efekti
val ColorSanctumSurface     = Color(0xFF1C1608)   // Very dark warm bg (Sanctum theme)
val ColorSanctumText        = Color(0xFFF5DFA0)   // Golden parchment text
val ColorSanctumBorder      = Color(0xFF5A4010)   // Dark gold border

// ── Covenant / Karanlık Teması (Void Purple) ──────────────────────────────────
val ColorCovenantPrimary    = Color(0xFF7B2FBE)   // Deep Void Purple — Covenant ana
val ColorCovenantSecondary  = Color(0xFF5A1B9A)   // Darker Void — Covenant ikincil
val ColorCovenantGlow       = Color(0xFFD352FF)   // Neon Violet — parıltı efekti
val ColorCovenantSurface    = Color(0xFF0E0818)   // Very dark purple bg (Covenant)
val ColorCovenantText       = Color(0xFFE0B0FF)   // Pale violet text
val ColorCovenantBorder     = Color(0xFF3A1060)   // Dark purple border

// ── Nötr / Demir Teması ──────────────────────────────────────────────────────
val ColorNeutralPrimary     = Color(0xFF9A8870)   // Aged Iron — nötr ana
val ColorNeutralSecondary   = Color(0xFF6A5A48)   // Rusty Bronze — nötr ikincil
val ColorNeutralGlow        = Color(0xFFD4B896)   // Warm Copper Glow
val ColorNeutralText        = Color(0xFFD0C4B0)   // Parchment text

// ── Durum Renkleri (Feedback) ─────────────────────────────────────────────────
val ColorDanger             = Color(0xFFC62828)   // Blood Red — hasar, tehlike
val ColorDangerLight        = Color(0xFFFF5252)   // Bright Red — kritik uyarı
val ColorDangerGlow         = Color(0xFF8B0000)   // Dark Blood — hasar glow
val ColorHeal               = Color(0xFF2E7D32)   // Forest Green — iyileşme
val ColorHealLight          = Color(0xFF66BB6A)   // Bright Green — HP artışı
val ColorWarning            = Color(0xFFFF8F00)   // Amber — uyarı
val ColorInfo               = Color(0xFF78909C)   // Slate Silver — bilgi, sistem

// ── Stat Renkleri ─────────────────────────────────────────────────────────────
val ColorStatHp             = Color(0xFFC62828)   // Kırmızı — HP barı
val ColorStatHpLow          = Color(0xFFFF3D00)   // Turuncu-kırmızı — düşük HP uyarısı
val ColorStatWill           = Color(0xFF1565C0)   // Deep Blue — İrade barı
val ColorStatGold           = Color(0xFFD4A017)   // Rich Gold — Altın sayacı
val ColorStatAether         = Color(0xFF6A1B9A)   // Violet — Aether (Covenant yönü)
val ColorStatAetherSanctum  = Color(0xFFC8A94A)   // Gold — Aether (Sanctum yönü)

// ── Node Tipleri ──────────────────────────────────────────────────────────────
val ColorNodeNarrative      = Color(0xFF78909C)   // Gümüş — hikaye anı
val ColorNodeCombat         = Color(0xFFC62828)   // Kırmızı — savaş
val ColorNodeBoss           = Color(0xFF8B0000)   // Koyu kırmızı — boss
val ColorNodeChest          = Color(0xFFD4A017)   // Altın — sandık
val ColorNodeShrine         = Color(0xFF4527A0)   // Mor — sunak
val ColorNodeMerchant       = Color(0xFF2E7D32)   // Yeşil — tüccar
val ColorNodeCamp           = Color(0xFF795548)   // Toprak — kamp
val ColorNodeSecret         = Color(0xFFFF8F00)   // Amber — gizli
val ColorNodeEvent          = Color(0xFF00838F)   // Teal — özel olay

// ── Boss Renkleri ─────────────────────────────────────────────────────────────
val ColorBossGradientStart  = Color(0xFF8B0000)
val ColorBossGradientEnd    = Color(0xFF4A0072)

// ── Momentum Barı Renkleri ────────────────────────────────────────────────────
val ColorMomentumSanctum    = Color(0xFFC8A94A)   // Altın (Sanctum)
val ColorMomentumNeutral    = Color(0xFF78909C)   // Gümüş (Nötr)
val ColorMomentumCovenant   = Color(0xFF7B2FBE)   // Mor (Covenant)

// ── Overlay & Scrim ──────────────────────────────────────────────────────────
val ColorScrimDark          = Color(0xCC000000)   // 80% siyah overlay
val ColorScrimMid           = Color(0x99000000)   // 60% siyah overlay
val ColorScrimLight         = Color(0x4D000000)   // 30% siyah overlay

// ── Spirit Fracture (Ölüm) ───────────────────────────────────────────────────
val ColorFracturePrimary    = Color(0xFF4A0000)   // Koyu kan kırmızısı
val ColorFractureGlow       = Color(0xFFFF1744)   // Parlayan kırmızı

