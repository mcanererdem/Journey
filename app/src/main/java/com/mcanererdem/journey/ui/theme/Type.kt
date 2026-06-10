package com.mcanererdem.journey.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.R

// ══════════════════════════════════════════════════════════════════════════════
// Journey Dark Fantasy — Font Ailesi & Typography Scale
// Fontlar res/font/ klasöründen yüklenir.
// ══════════════════════════════════════════════════════════════════════════════

/**
 * CinzelDecorative — Büyük başlıklar, kat adları, boss isimleri.
 * Gotik serifli, mimari hissi verir.
 */
val CinzelDecorative = FontFamily(
    Font(R.font.cinzel_decorative_regular, FontWeight.Normal),
    Font(R.font.cinzel_decorative_bold, FontWeight.Bold)
)

/**
 * Cinzel — Ekran başlıkları, sekme isimleri, UI başlıkları.
 * CinzelDecorative'den daha sade, okunabilirlik öncelikli.
 */
val Cinzel = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_medium, FontWeight.Medium),
    Font(R.font.cinzel_semibold, FontWeight.SemiBold),
    Font(R.font.cinzel_bold, FontWeight.Bold)
)

/**
 * CrimsonPro — Hikaye metinleri, lore açıklamaları, karar günlüğü.
 * Serif okunabilirlik, antik belge hissi verir.
 */
val CrimsonPro = FontFamily(
    Font(R.font.crimson_pro_regular, FontWeight.Normal),
    Font(R.font.crimson_pro_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.crimson_pro_medium, FontWeight.Medium),
    Font(R.font.crimson_pro_semibold, FontWeight.SemiBold),
    Font(R.font.crimson_pro_bold, FontWeight.Bold),
    Font(R.font.crimson_pro_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

/**
 * Rajdhani — UI etiketleri, stat sayıları, buton metinleri.
 * Modern, küçük cap desteği, rakamlar okunaklı.
 */
val Rajdhani = FontFamily(
    Font(R.font.rajdhani_regular, FontWeight.Normal),
    Font(R.font.rajdhani_medium, FontWeight.Medium),
    Font(R.font.rajdhani_semibold, FontWeight.SemiBold),
    Font(R.font.rajdhani_bold, FontWeight.Bold)
)

// ══════════════════════════════════════════════════════════════════════════════
// Typography Scale
// Material 3 tipografi hiyerarşisini dark fantasy fontlarına eşle.
// ══════════════════════════════════════════════════════════════════════════════

val JourneyTypography = Typography(

    // ── Display — Oyun logosu, splash ekranı ───────────────────────────
    displayLarge = TextStyle(
        fontFamily = CinzelDecorative,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = 3.sp
    ),
    displayMedium = TextStyle(
        fontFamily = CinzelDecorative,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 2.sp
    ),
    displaySmall = TextStyle(
        fontFamily = CinzelDecorative,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 2.sp
    ),

    // ── Headline — Kat adı, ekran başlığı ──────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 1.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 1.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),

    // ── Title — Node başlıkları, dialog başlıkları ─────────────────────
    titleLarge = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),

    // ── Body — Hikaye metinleri, açıklamalar ───────────────────────────
    bodyLarge = TextStyle(
        fontFamily = CrimsonPro,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CrimsonPro,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CrimsonPro,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),

    // ── Label — Stat etiketleri, buton metinleri, küçük UI ─────────────
    labelLarge = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

// ── Özel Text Style Uzantıları ─────────────────────────────────────────────
// Standart Material hiyerarşisine uymayan özel durumlar için

/** Faction isimleri için — Cinzel, büyük harf, geniş karakter aralığı */
val TextStyleFactionTitle = TextStyle(
    fontFamily = Cinzel,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    letterSpacing = 2.sp
)

/** Stat değerleri (HP: 80, Gold: 120) için — Rajdhani Bold */
val TextStyleStatValue = TextStyle(
    fontFamily = Rajdhani,
    fontWeight = FontWeight.Bold,
    fontSize = 15.sp,
    letterSpacing = 0.sp
)

/** Hikaye metni italik vurgu — karar günlüğü, flavor text */
val TextStyleLoreItalic = TextStyle(
    fontFamily = CrimsonPro,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Italic,
    fontSize = 14.sp,
    lineHeight = 21.sp,
    letterSpacing = 0.sp
)

/** Büyük sayısal değerler — can barının üstündeki "85/100" gibi */
val TextStyleStatLarge = TextStyle(
    fontFamily = Rajdhani,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    letterSpacing = 0.sp
)

/** Küçük sistem etiketleri — "KAT 7", "BOSS", "SECRET" etiketleri */
val TextStyleSystemTag = TextStyle(
    fontFamily = Rajdhani,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    letterSpacing = 2.sp
)
