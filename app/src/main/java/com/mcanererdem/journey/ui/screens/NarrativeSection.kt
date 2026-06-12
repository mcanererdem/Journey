package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.data.model.NodeChoice
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.engine.NarrativeEvent
import com.mcanererdem.journey.data.engine.NarrativeBranchOption

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.ui.theme.*

@Composable
fun NodeChoiceButton(
    choice: NodeChoice,
    activeLang: String,
    highlightColor: Color,
    testTagValue: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val cardBg = if (enabled) ColorSurface else ColorSurface.copy(alpha = 0.4f)
    val cardBorderColor = if (enabled) highlightColor.copy(alpha = 0.6f) else ColorBorderMuted
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(enabled = enabled) { onClick() }
            .testTag(testTagValue),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                val choiceIcon = when {
                    !enabled -> "🔒"
                    choice.effects.alignmentShift > 0 -> "🙏"
                    choice.effects.alignmentShift < 0 -> "🗡️"
                    else -> "📖"
                }
                
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            if (enabled) highlightColor.copy(alpha = 0.15f) else ColorBorderMuted,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = choiceIcon,
                        fontSize = 14.sp
                    )
                }
                
                Column {
                    Text(
                        text = LocalizationManager.getString(activeLang, choice.labelKey),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (enabled) ColorOnBackground else ColorOnSurfaceMuted
                    )
                    
                    if (!enabled && choice.effects.requiredFlag.isNotEmpty()) {
                        Text(
                            text = LocalizationManager.formatString(activeLang, "ui.choice_prerequisite", choice.effects.requiredFlag),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = ColorDanger
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (choice.effects.hpChange != 0) {
                            val isHpPositive = choice.effects.hpChange > 0
                            val hpText = if (isHpPositive) "+${choice.effects.hpChange} HP" else "${choice.effects.hpChange} HP"
                            val hpColor = if (isHpPositive) ColorHeal else ColorDanger
                            Box(
                                modifier = Modifier
                                    .background(hpColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = hpText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = hpColor
                                )
                            }
                        }
                        
                        if (choice.effects.alignmentShift != 0) {
                            val isShiftPositive = choice.effects.alignmentShift > 0
                            val shiftText = if (isShiftPositive) LocalizationManager.getString(activeLang, "ui.badge_light") else LocalizationManager.getString(activeLang, "ui.badge_corruption")
                            val shiftColor = if (isShiftPositive) ColorSanctumPrimary else ColorCovenantGlow
                            Box(
                                modifier = Modifier
                                    .background(shiftColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = shiftText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = shiftColor
                                )
                            }
                        }
                        
                        if (choice.effects.goldChange != 0) {
                            val goldText = LocalizationManager.formatString(activeLang, "ui.badge_gleam", choice.effects.goldChange)
                            val goldColor = ColorStatGold
                            Box(
                                modifier = Modifier
                                    .background(goldColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = goldText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = goldColor
                                )
                            }
                        }
                        
                        if (choice.effects.aetherChange != 0) {
                            val aetherText = LocalizationManager.formatString(activeLang, "ui.badge_aether", choice.effects.aetherChange)
                            val aetherColor = ColorCovenantGlow
                            Box(
                                modifier = Modifier
                                    .background(aetherColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = aetherText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = aetherColor
                                )
                            }
                        }
                        
                        if (choice.effects.rewardItemId.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(ColorHeal.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = LocalizationManager.getString(activeLang, "ui.badge_item"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = ColorHeal
                                )
                            }
                        }
                    }
                }
            }
            
            if (enabled) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = LocalizationManager.getString(activeLang, "ui.desc_select"),
                    tint = ColorOnSurfaceMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun NarrativeEventView(
    event: NarrativeEvent,
    onChoiceMade: (NarrativeBranchOption) -> Unit,
    onCancel: () -> Unit,
    activeLang: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpacingL),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(Dimens.SpacingL),
        border = BorderStroke(Dimens.BorderNormal, ColorSanctumPrimary)
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingL)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ColorSanctumPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(Dimens.SpacingS)
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.narrative_mystery"),
                        modifier = Modifier.padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXs),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ColorSanctumPrimary
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = LocalizationManager.getString(activeLang, "ui.desc_close"), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            Text(
                text = event.getTitle(activeLang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            Text(
                text = event.getDescription(activeLang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingXl))
            Text(
                text = LocalizationManager.getString(activeLang, "ui.narrative_destiny"),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                color = ColorCovenantGlow
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
            event.options.forEach { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.SpacingS)
                        .clickable { onChoiceMade(option) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(Dimens.SpacingM)
                ) {
                    Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                        Text(
                            text = option.getText(activeLang),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                        if (option.outcomeKey.isNotEmpty()) {
                            Text(
                                text = LocalizationManager.formatString(activeLang, "ui.narrative_outcome", option.getOutcome(activeLang)),
                                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.SpacingS))
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
                            if (option.alignmentImpact != 0) {
                                val isImpactPositive = option.alignmentImpact > 0
                                val alignLabel = if (isImpactPositive) LocalizationManager.formatString(activeLang, "ui.badge_sanctum", option.alignmentImpact) else LocalizationManager.formatString(activeLang, "ui.badge_covenant", option.alignmentImpact)
                                val alignCol = if (isImpactPositive) ColorSanctumPrimary else ColorCovenantGlow
                                Surface(color = alignCol.copy(alpha = 0.1f), shape = RoundedCornerShape(Dimens.SpacingXs)) {
                                    Text(
                                        text = alignLabel,
                                        modifier = Modifier.padding(horizontal = Dimens.SpacingS, vertical = Dimens.BorderThick),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                                        color = alignCol
                                    )
                                }
                            }
                            if (option.goldChange != 0) {
                                Surface(color = Color(0xFFFFD700).copy(alpha = 0.1f), shape = RoundedCornerShape(Dimens.SpacingXs)) {
                                    Text(
                                        text = LocalizationManager.formatString(activeLang, "ui.badge_gold", option.goldChange),
                                        modifier = Modifier.padding(horizontal = Dimens.SpacingS, vertical = Dimens.BorderThick),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                            if (option.itemReward.isNotEmpty()) {
                                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(Dimens.SpacingXs)) {
                                    Text(
                                        text = "🎒 " + LocalizationManager.getString(activeLang, "ui.badge_item"),
                                        modifier = Modifier.padding(horizontal = Dimens.SpacingS, vertical = Dimens.BorderThick),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
