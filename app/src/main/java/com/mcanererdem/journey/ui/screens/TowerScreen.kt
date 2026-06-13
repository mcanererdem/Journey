package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.data.model.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.ui.components.NodeChoiceButton
import com.mcanererdem.journey.ui.theme.*

@Composable
fun TypewriterText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    var visibleText by remember(text) { mutableStateOf("") }
    LaunchedEffect(text) {
        visibleText = ""
        text.forEach { char ->
            visibleText += char
            kotlinx.coroutines.delay(20) 
        }
    }
    Text(text = visibleText, style = style, color = color, modifier = modifier)
}

@Composable
fun ChoiceContainer(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpacingS)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            shape = RoundedCornerShape(Dimens.RadiusS),
            border = BorderStroke(Dimens.BorderThick, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(Dimens.SpacingS)) {
                Spacer(modifier = Modifier.height(Dimens.SpacingS))
                content()
            }
        }
        
        Surface(
            modifier = Modifier.padding(start = Dimens.SpacingM),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(Dimens.RadiusXs)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = Dimens.LetterSpacingNormal, fontSize = 9.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Dimens.SpacingXs)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TowerClimbTab(
    player: PlayerProfile?,
    nodes: List<AdventureNode>,
    scenario: FloorScenario?,
    activeEnemyHp: Int?,
    combatLog: List<CombatLogEntry>,
    activeLang: String,
    journal: List<JournalEntry>,
    playerStatuses: List<CombatStatus>,
    enemyStatuses: List<CombatStatus>,
    currentEnemyIntent: EnemyIntent,
    actionMessage: ActionMessage,
    showNotificationBanner: Boolean,
    animationsEnabled: Boolean,
    glowEffectsEnabled: Boolean,
    onDismissNotification: () -> Unit,
    onLockedClicked: (String) -> Unit,
    onChoiceSelected: (NodeChoice) -> Unit,
    onScenarioChoiceSelected: (GameOption) -> Unit,
    onNextNodeClick: (Int, Int) -> Unit,
    onAscendFloorClick: () -> Unit,
    onCombatAction: (String) -> Unit,
    onResetClick: () -> Unit,
    onInitCombat: (PlayerProfile, List<AdventureNode>, String) -> Unit
) {
    if (player == null) return

    // CRITICAL: Robust reset logic
    LaunchedEffect(player.currentNodeIndex, player.currentNodeCompleted) {
        onInitCombat(player, nodes, activeLang)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FloorProgressCartographyMap(
            player = player,
            nodes = nodes,
            activeLang = activeLang,
            journal = journal,
            animationsEnabled = animationsEnabled,
            onLockedClicked = onLockedClicked,
            onNextNodeClick = onNextNodeClick
        )
        
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                val activeNode = nodes.getOrNull(player.currentNodeIndex)
                
                // Content - Top Aligned
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (player.currentFloor > 100) {
                        // Win state UI
                    } else if (scenario != null && player.currentNodeIndex == 0 && !player.currentNodeCompleted && !journal.any { it.floor == player.currentFloor }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpacingS)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(Dimens.RadiusM))
                                .border(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(Dimens.RadiusM))
                                .padding(Dimens.SpacingM)
                                .verticalScroll(rememberScrollState())
                        ) {
                             Text(
                                text = LocalizationManager.getString(activeLang, scenario.titleKey).uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Serif, letterSpacing = 2.sp, fontSize = 20.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = Dimens.SpacingS))
                            TypewriterText(
                                text = LocalizationManager.getString(activeLang, scenario.descriptionKey),
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, lineHeight = 26.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else if (activeNode != null) {
                        if (activeNode.type == NodeType.COMBAT || activeNode.type == NodeType.BOSS) {
                            CombatSection(
                                player = player,
                                activeNode = activeNode,
                                activeEnemyHp = activeEnemyHp,
                                combatLog = combatLog,
                                activeLang = activeLang,
                                onCombatAction = onCombatAction
                            )
                        } else if (!player.currentNodeCompleted) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.SpacingS)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(Dimens.RadiusM))
                                    .border(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(Dimens.RadiusM))
                                    .padding(Dimens.SpacingM)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                 Text(
                                    text = LocalizationManager.getString(activeLang, activeNode.titleKey).uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Serif, letterSpacing = 2.sp, fontSize = 20.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = Dimens.SpacingS))
                                TypewriterText(
                                    text = LocalizationManager.getString(activeLang, activeNode.descriptionKey),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, lineHeight = 26.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            // Automatically handle transitions or show progress
                             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                                }
                            }
                        }
                    }
                }

                // Actions/Choices - Pinned to Bottom
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpacingXs, vertical = Dimens.SpacingXs)) {
                    if (scenario != null && player.currentNodeIndex == 0 && !player.currentNodeCompleted) {
                        ChoiceContainer(title = LocalizationManager.getString(activeLang, "ui.declare_choice")) {
                            scenario.options.forEach { option ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.SpacingXxs).clickable { onScenarioChoiceSelected(option) },
                                    shape = RoundedCornerShape(Dimens.RadiusXs),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                    border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                ) {
                                    Row(modifier = Modifier.padding(Dimens.SpacingM), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = LocalizationManager.getString(activeLang, option.labelKey),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 0.5.sp),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(Dimens.IconS), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    } else if (activeNode != null && !player.currentNodeCompleted && activeNode.type != NodeType.COMBAT && activeNode.type != NodeType.BOSS) {
                        ChoiceContainer(title = LocalizationManager.getString(activeLang, "ui.declare_choice")) {
                            activeNode.choices.forEach { choice ->
                                val isPositive = choice.effects.hpChange > 0 || choice.effects.goldChange > 0 || choice.effects.expChange > 0
                                val isNegative = choice.effects.hpChange < 0 || choice.effects.momentumShift < 0
                                val accent = when {
                                    isNegative -> ColorDanger
                                    isPositive -> ColorHeal
                                    choice.effects.momentumShift > 0 -> ColorSanctumPrimary
                                    else -> MaterialTheme.colorScheme.primary
                                }

                                NodeChoiceButton(
                                    text = LocalizationManager.getString(activeLang, choice.labelKey),
                                    onClick = { onChoiceSelected(choice) },
                                    accentColor = accent,
                                    enabled = choice.effects.requiredFlag.isEmpty() || player.storyFlagsEncoded.split(",").contains(choice.effects.requiredFlag),
                                    glowEffectsEnabled = glowEffectsEnabled
                                )
                                Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            }
                        }
                    } else if (player.currentNodeCompleted && activeNode?.type == NodeType.BOSS) {
                        Button(
                            onClick = onAscendFloorClick,
                            modifier = Modifier.fillMaxWidth().height(Dimens.AvatarSize),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorHeal),
                            shape = RoundedCornerShape(Dimens.RadiusS)
                        ) {
                            Text(text = LocalizationManager.getString(activeLang, "ui.btn_ascend_floor").uppercase(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
