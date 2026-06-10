package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
// Journey Dark Fantasy — Stat Barları (HP, Will, EXP vb.)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Oyuncunun HP'sini gösteren animasyonlu can barı.
 * Düşük HP'de renk değişimi ve titreme efekti.
 *
 * @param current Mevcut can değeri
 * @param max Maksimum can değeri
 * @param showLabel Üstte "HP: 80/100" etiketi göster
 */
@Composable
fun HpBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val fraction = if (max > 0) (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f

    // HP seviyesine göre renk geçişi: yeşil → sarı → turuncu → kırmızı
    val barColor by animateColorAsState(
        targetValue = when {
            fraction > 0.6f -> ColorHeal
            fraction > 0.35f -> ColorWarning
            fraction > 0.15f -> Color(0xFFFF6D00) // Deep orange
            else -> ColorDangerLight
        },
        animationSpec = tween(Dimens.AnimNormal),
        label = "hp_color"
    )

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(Dimens.AnimNormal),
        label = "hp_fraction"
    )

    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HP",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorOnSurfaceMuted
                )
                Text(
                    text = "$current / $max",
                    style = TextStyleStatValue.copy(fontSize = Dimens.TextS),
                    color = barColor
                )
            }
            Spacer(Modifier.height(Dimens.SpacingXxs))
        }

        StatBarCanvas(
            fraction = animatedFraction,
            filledColor = barColor,
            trackColor = ColorSurfaceVariant,
            height = Dimens.StatBarHeight,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * İrade (Will) barı — mavi tonda.
 */
@Composable
fun WillBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val fraction = if (max > 0) (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(Dimens.AnimNormal),
        label = "will_fraction"
    )

    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "WILL",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorOnSurfaceMuted
                )
                Text(
                    text = "$current / $max",
                    style = TextStyleStatValue.copy(fontSize = Dimens.TextS),
                    color = ColorStatWill
                )
            }
            Spacer(Modifier.height(Dimens.SpacingXxs))
        }

        StatBarCanvas(
            fraction = animatedFraction,
            filledColor = ColorStatWill,
            trackColor = ColorSurfaceVariant,
            height = Dimens.StatBarHeight,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Momentum barı — Sanctum (sağ) ↔ Covenant (sol).
 * Ortası nötr gri, kenarlara doğru faction renkleri.
 *
 * @param momentum 0-100 arası. 50 = nötr.
 */
@Composable
fun MomentumBar(
    momentum: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val normalizedFraction = (momentum.toFloat() / 100f).coerceIn(0f, 1f)

    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "COVENANT",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorMomentumCovenant.copy(alpha = 0.7f)
                )
                Text(
                    text = "SANCTUM",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorMomentumSanctum.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(Dimens.SpacingXxs))
        }

        // Gradyan çubuk — sol Covenant mor, sağ Sanctum altın
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(Dimens.StatBarHeight)
                .clip(RoundedCornerShape(Dimens.RadiusCircle))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ColorMomentumCovenant.copy(alpha = 0.25f),
                            ColorMomentumNeutral.copy(alpha = 0.15f),
                            ColorMomentumSanctum.copy(alpha = 0.25f)
                        )
                    )
                )
        ) {
            // Momentum göstergesi — küçük dikey çizgi
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalizedFraction)
                    .background(Color.Transparent)
            )
            // İşaretçi nokta
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (normalizedFraction * 280).dp.coerceAtMost(280.dp))
                    .size(Dimens.StatBarHeight)
                    .clip(RoundedCornerShape(Dimens.RadiusCircle))
                    .background(
                        when {
                            momentum > 66 -> ColorMomentumSanctum
                            momentum < 34 -> ColorMomentumCovenant
                            else -> ColorMomentumNeutral
                        }
                    )
            )
        }
    }
}

/**
 * Temel bar canvas çizici — diğer barlar bunu kullanır.
 */
@Composable
private fun StatBarCanvas(
    fraction: Float,
    filledColor: Color,
    trackColor: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(Dimens.RadiusCircle))
            .drawWithCache {
                onDrawBehind {
                    // Track (arkaplan)
                    drawRect(color = trackColor)
                    // Doldu kısım
                    drawRect(
                        color = filledColor,
                        size = size.copy(width = size.width * fraction)
                    )
                    // Parlak üst çizgi efekti
                    drawLine(
                        color = filledColor.copy(alpha = 0.5f),
                        start = Offset(0f, 1.dp.toPx()),
                        end = Offset(size.width * fraction, 1.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
    )
}

/**
 * Kompakt stat satırı — ikon + değer formatında yatay gösterim.
 * Header'da HP/Will/Gold/Aether için kullanılır.
 */
@Composable
fun StatChip(
    emoji: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = value,
            style = TextStyleStatValue,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
