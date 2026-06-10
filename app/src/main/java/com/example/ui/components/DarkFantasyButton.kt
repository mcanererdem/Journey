package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
// Journey Dark Fantasy — Ortak Buton Bileşenleri
// Tüm butonlar bu dosyadan kullanılır.
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Birincil aksiyonlar için ana dark fantasy butonu.
 * Faction rengine göre gradient ve glow efekti.
 *
 * @param text Buton etiketi
 * @param onClick Tıklama aksiyonu
 * @param factionSide "SANCTUM", "COVENANT" veya "NEUTRAL"
 * @param enabled Aktif mi?
 * @param modifier Ek modifier
 */
@Composable
fun DarkFantasyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    factionSide: String = "NEUTRAL",
    enabled: Boolean = true,
    isDestructive: Boolean = false
) {
    val primaryColor = when {
        isDestructive -> ColorDanger
        factionSide == "SANCTUM"  -> ColorSanctumPrimary
        factionSide == "COVENANT" -> ColorCovenantPrimary
        else -> ColorNeutralPrimary
    }
    val glowColor = when {
        isDestructive -> ColorDangerLight
        factionSide == "SANCTUM"  -> ColorSanctumGlow
        factionSide == "COVENANT" -> ColorCovenantGlow
        else -> ColorNeutralGlow
    }
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.85f),
            glowColor.copy(alpha = 0.60f),
            primaryColor.copy(alpha = 0.85f)
        )
    )
    val disabledColor = ColorOnSurfaceMuted.copy(alpha = 0.4f)

    val interactionSource = remember { MutableInteractionSource() }

    // Pulse animasyonu — buton aktifken yavaş titreşim
    val infiniteTransition = rememberInfiniteTransition(label = "btn_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .scale(if (enabled) pulseScale else 1f)
            .clip(RoundedCornerShape(Dimens.RadiusM))
            .background(
                brush = if (enabled) gradientBrush
                else Brush.horizontalGradient(listOf(disabledColor, disabledColor))
            )
            .border(
                border = BorderStroke(
                    width = Dimens.BorderNormal,
                    color = if (enabled) primaryColor.copy(alpha = 0.6f) else ColorBorderMuted
                ),
                shape = RoundedCornerShape(Dimens.RadiusM)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = Dimens.SpacingXxl, vertical = Dimens.SpacingM),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (enabled) ColorOnBackground else ColorOnSurfaceMuted,
            textAlign = TextAlign.Center,
            letterSpacing = Dimens.LetterSpacingWide
        )
    }
}

/**
 * İkincil, daha sade outline butonu.
 * Az önemli aksiyonlar (İptal, Geç, vs.) için.
 */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = ColorOnSurfaceMuted
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusS))
            .border(
                border = BorderStroke(Dimens.BorderThin, color.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(Dimens.RadiusS)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingS),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) color else color.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Node seçim butonu — büyük, tam genişlik, üç seçenek için.
 * Tıklandığında border parlar.
 */
@Composable
fun NodeChoiceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = ColorNeutralPrimary,
    enabled: Boolean = true,
    isSelected: Boolean = false
) {
    val borderColor = if (isSelected) accentColor else accentColor.copy(alpha = 0.25f)
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.12f) else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusM))
            .background(bgColor)
            .border(BorderStroke(Dimens.BorderNormal, borderColor), RoundedCornerShape(Dimens.RadiusM))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) ColorOnSurface else ColorOnSurfaceMuted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Tehdit/node etiketi — "BOSS", "SECRET", "COMBAT" gibi küçük etiket.
 */
@Composable
fun NodeTypeTag(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusXs))
            .background(color.copy(alpha = 0.15f))
            .border(BorderStroke(Dimens.BorderThin, color.copy(alpha = 0.5f)), RoundedCornerShape(Dimens.RadiusXs))
            .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXxs)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            letterSpacing = Dimens.LetterSpacingXWide
        )
    }
}
