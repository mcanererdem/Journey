package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import com.example.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
// Journey Dark Fantasy — Aksiyon Banner & Faction Badge Bileşenleri
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Oyuncu aksiyonlarının sonucunu anlatan banner.
 * Ekranın üstünde animasyonla görünür-kaybolur.
 *
 * @param message Gösterilecek mesaj
 * @param visible Görünür mü?
 * @param isError Hata mesajı mı? (kırmızı renk)
 */
@Composable
fun ActionBanner(
    message: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    isError: Boolean = false,
    isSuccess: Boolean = false
) {
    val borderColor = when {
        isError   -> ColorDanger.copy(alpha = 0.5f)
        isSuccess -> ColorHeal.copy(alpha = 0.5f)
        else      -> ColorBorderGlow
    }
    val iconTint = when {
        isError   -> ColorDangerLight
        isSuccess -> ColorHealLight
        else      -> ColorNeutralGlow
    }

    AnimatedVisibility(
        visible = visible && message.isNotBlank(),
        enter = fadeIn(tween(Dimens.AnimFast)) + slideInVertically(
            tween(Dimens.AnimFast),
            initialOffsetY = { -it / 2 }
        ),
        exit = fadeOut(tween(Dimens.AnimNormal)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.RadiusS))
                .background(ColorSurface.copy(alpha = 0.85f))
                .border(BorderStroke(Dimens.BorderThin, borderColor), RoundedCornerShape(Dimens.RadiusS))
                .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(Dimens.IconS)
                    .padding(top = Dimens.SpacingXxs)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = ColorOnSurface,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Faction rozeti — "SANCTUM", "COVENANT" veya "NEUTRAL".
 * Momentum değerinden türetilen sınıf adı ile birlikte gösterilebilir.
 *
 * @param side "SANCTUM", "COVENANT", "NEUTRAL"
 * @param className Sınıf adı (ör. "Lightseeker")
 * @param lang Dil kodu
 */
@Composable
fun FactionBadge(
    side: String,
    modifier: Modifier = Modifier,
    className: String? = null,
    lang: String = "EN"
) {
    val (label, color) = when (side) {
        "SANCTUM"  -> (if (lang == "TR") "SANCTUM" else "SANCTUM") to ColorSanctumPrimary
        "COVENANT" -> (if (lang == "TR") "COVENANT" else "COVENANT") to ColorCovenantPrimary
        else       -> (if (lang == "TR") "NÖTR" else "NEUTRAL") to ColorNeutralPrimary
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Dimens.RadiusXs))
                .background(color.copy(alpha = 0.15f))
                .border(BorderStroke(Dimens.BorderThin, color.copy(alpha = 0.4f)), RoundedCornerShape(Dimens.RadiusXs))
                .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXxs)
        ) {
            Text(
                text = label,
                style = TextStyleFactionTitle,
                color = color
            )
        }

        if (className != null) {
            Text(
                text = className,
                style = MaterialTheme.typography.labelSmall,
                color = ColorOnSurfaceMuted
            )
        }
    }
}

/**
 * Kompakt stat header satırı — HP / Will / Gold / Aether.
 * Tüm ekranların üstünde kullanılır.
 */
@Composable
fun PlayerStatsHeader(
    hp: Int,
    maxHp: Int,
    will: Int,
    maxWill: Int,
    gold: Int,
    aether: Int,
    factionSide: String,
    modifier: Modifier = Modifier
) {
    val aetherColor = when (factionSide) {
        "SANCTUM"  -> ColorStatAetherSanctum
        "COVENANT" -> ColorStatAether
        else       -> ColorNeutralGlow
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorSurface)
            .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatChip(emoji = "❤️", value = "$hp/$maxHp", color = ColorStatHp)
        StatChip(emoji = "⚡", value = "$will/$maxWill", color = ColorStatWill)
        StatChip(emoji = "💰", value = "$gold", color = ColorStatGold)
        StatChip(emoji = "✨", value = "$aether", color = aetherColor)
    }
}
