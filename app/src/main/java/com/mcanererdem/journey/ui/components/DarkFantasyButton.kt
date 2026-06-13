package com.mcanererdem.journey.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.ui.theme.*

/**
 * Standard Dark Fantasy Button.
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
    val pulseScale by rememberInfiniteTransition(label = "btn_pulse").animateFloat(
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
            .background(brush = if (enabled) gradientBrush else Brush.horizontalGradient(listOf(disabledColor, disabledColor)))
            .border(border = BorderStroke(width = Dimens.BorderNormal, color = if (enabled) primaryColor.copy(alpha = 0.6f) else ColorBorderMuted), shape = RoundedCornerShape(Dimens.RadiusM))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Dimens.SpacingXxl, vertical = Dimens.SpacingM),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (enabled) ColorOnBackground else ColorOnSurfaceMuted, textAlign = TextAlign.Center, letterSpacing = Dimens.LetterSpacingWide)
    }
}

/**
 * Epic Choice Button - Rectangular, themed, and stylized.
 */
@Composable
fun NodeChoiceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = ColorNeutralPrimary,
    enabled: Boolean = true,
    glowEffectsEnabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_glow")
    
    // Wave effect offset
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut), 
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val waveBrush = if (glowEffectsEnabled && enabled) {
        Brush.linearGradient(
            0.0f to accentColor.copy(alpha = 0f),
            0.5f to accentColor.copy(alpha = 0.25f),
            1.0f to accentColor.copy(alpha = 0f),
            start = androidx.compose.ui.geometry.Offset(waveOffset * 1000f, 0f),
            end = androidx.compose.ui.geometry.Offset(waveOffset * 1000f + 300f, 0f)
        )
    } else {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(Dimens.BorderThick, if (enabled) accentColor.copy(alpha = 0.8f) else ColorBorderMuted)
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(accentColor.copy(alpha = if (glowEffectsEnabled) glowAlpha else 0.05f))) {
            // Wave layer
            if (glowEffectsEnabled && enabled) {
                Box(modifier = Modifier.matchParentSize().background(waveBrush))
            }

            Row(
                modifier = Modifier.padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        val parts = text.split("(", ")")
                        parts.forEachIndexed { index, s ->
                            if (index % 2 == 1) { // Inside parentheses
                                withStyle(style = SpanStyle(color = accentColor, fontWeight = FontWeight.Black)) {
                                    append(" ($s)")
                                }
                            } else {
                                append(s)
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else ColorOnSurfaceMuted,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (enabled) accentColor else ColorOnSurfaceMuted,
                    modifier = Modifier.size(Dimens.IconM)
                )
            }
        }
    }
}

@Composable
fun NodeTypeTag(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(Dimens.RadiusXs)).background(color.copy(alpha = 0.15f)).border(BorderStroke(Dimens.BorderThin, color.copy(alpha = 0.5f)), RoundedCornerShape(Dimens.RadiusXs)).padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXxs)) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, letterSpacing = Dimens.LetterSpacingXWide)
    }
}

/**
 * A transparent, text-only button for secondary actions.
 */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = ColorOnSurfaceMuted
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.RadiusM)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Medium,
            letterSpacing = Dimens.LetterSpacingWide
        )
    }
}
