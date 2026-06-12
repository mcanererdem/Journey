package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.data.model.CombatStatus
import com.mcanererdem.journey.data.model.EnemyIntent
import com.mcanererdem.journey.data.model.StatusType
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.model.AdventureNode
import com.mcanererdem.journey.data.model.FloorScenario
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.NodeChoice
import com.mcanererdem.journey.data.model.GameOption
import com.mcanererdem.journey.data.model.NodeType

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.ui.theme.*

import com.mcanererdem.journey.data.model.CombatLogEntry

@Composable
fun TowerClimbTab(
    player: PlayerProfile?,
    nodes: List<AdventureNode>,
    scenario: FloorScenario?,
    activeEnemyHp: Int?,
    combatLog: List<CombatLogEntry>,
    activeLang: String,
    journal: List<JournalEntry>,
    scoutedNodeIndices: Set<Int>,
    playerStatuses: List<CombatStatus>,
    enemyStatuses: List<CombatStatus>,
    currentEnemyIntent: EnemyIntent,
    onScoutClick: () -> Unit,
    onLockedClicked: (String) -> Unit,
    onChoiceSelected: (NodeChoice) -> Unit,
    onScenarioChoiceSelected: (GameOption) -> Unit,
    onNextNodeClick: (Int, Int) -> Unit,
    onAscendFloorClick: () -> Unit,
    onCombatAction: (String) -> Unit,
    onResetClick: () -> Unit
) {
    if (player == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingS),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
        }

        item {
            Text(
                text = LocalizationManager.formatString(activeLang, "ui.label_checkpoint", player.savedFloorCheckpoint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
        }

        item {
            FloorProgressCartographyMap(
                player = player,
                nodes = nodes,
                activeLang = activeLang,
                journal = journal,
                scoutedNodeIndices = scoutedNodeIndices,
                onScoutClick = onScoutClick,
                onLockedClicked = onLockedClicked,
                onNextNodeClick = onNextNodeClick
            )
        }

        if (player.currentFloor > 100) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.SpacingL),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(Dimens.SpacingL),
                    border = BorderStroke(Dimens.BorderGlow, ColorSanctumPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(Dimens.SpacingXxl)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.ascend_victory_title"),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorSanctumPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacingL))
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.ascend_victory_desc"),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacingXxl))
                        Button(
                            onClick = onResetClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSanctumPrimary),
                            modifier = Modifier.testTag("ascend_victory_reset")
                        ) {
                            Text(LocalizationManager.getString(activeLang, "ui.ascend_victory_reset"))
                        }
                    }
                }
            }
        } else {
            val hasProgressedFloor = journal.any { it.floor == player.currentFloor }
            if (scenario != null && player.currentNodeIndex == 0 && !player.currentNodeCompleted && !hasProgressedFloor) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Dimens.SpacingL),
                        shape = RoundedCornerShape(Dimens.SpacingM),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                            Text(
                                text = LocalizationManager.getString(activeLang, scenario.titleKey),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            Text(
                                text = LocalizationManager.getString(activeLang, scenario.descriptionKey),
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                items(scenario.options) { option ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.SpacingXs)
                            .clickable { onScenarioChoiceSelected(option) },
                        shape = RoundedCornerShape(Dimens.SpacingS),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.SpacingM),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LocalizationManager.getString(activeLang, option.labelKey),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                val activeNode = nodes.getOrNull(player.currentNodeIndex)
                if (activeNode != null) {
                    if (player.currentNodeCompleted) {
                        if (activeNode.type == NodeType.BOSS) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = Dimens.SpacingL),
                                    shape = RoundedCornerShape(Dimens.SpacingM),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(Dimens.BorderNormal, ColorHeal)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(Dimens.SpacingL)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = LocalizationManager.getString(activeLang, "ui.floor_cleared"),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ColorHeal
                                        )
                                        Spacer(modifier = Modifier.height(Dimens.SpacingS))
                                        Text(
                                            text = LocalizationManager.getString(activeLang, "ui.floor_cleared_desc"),
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(Dimens.SpacingL))

                                        Button(
                                            onClick = onAscendFloorClick,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(Dimens.AvatarSize)
                                                .testTag("btn_ascend_floor"),
                                            colors = ButtonDefaults.buttonColors(containerColor = ColorHeal)
                                        ) {
                                            Text(
                                                text = LocalizationManager.getString(activeLang, "ui.btn_ascend_floor"),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingL),
                                    shape = RoundedCornerShape(Dimens.SpacingM),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(Dimens.BorderThin, ColorHeal.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(Dimens.SpacingL),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = LocalizationManager.getString(activeLang, "ui.sector_cleared_msg"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(Dimens.SpacingM))
                                        
                                        Button(
                                            onClick = { },
                                            colors = ButtonDefaults.buttonColors(containerColor = ColorHeal.copy(alpha = 0.8f)),
                                            modifier = Modifier.fillMaxWidth().height(Dimens.BadgeSize + 4.dp)
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(Dimens.SpacingS))
                                            Text(
                                                text = LocalizationManager.getString(activeLang, "ui.btn_proceed_via_map"),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (activeNode.type == NodeType.COMBAT || activeNode.type == NodeType.BOSS) {
                        item {
                            CombatSection(
                                player = player,
                                activeNode = activeNode,
                                activeEnemyHp = activeEnemyHp,
                                combatLog = combatLog,
                                activeLang = activeLang,
                                onCombatAction = onCombatAction
                            )
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.SpacingL),
                                shape = RoundedCornerShape(Dimens.SpacingM),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                                    Text(text = LocalizationManager.getString(activeLang, activeNode.titleKey), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif), color = MaterialTheme.colorScheme.primary)
                                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                                    Text(text = LocalizationManager.getString(activeLang, activeNode.descriptionKey), style = MaterialTheme.typography.bodyLarge.copy(lineHeight = Dimens.TextXxl, fontFamily = FontFamily.Serif), color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                        item {
                            Text(text = LocalizationManager.getString(activeLang, "ui.declare_choice"), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(Dimens.SpacingM))
                        }
                        items(activeNode.choices) { choice ->
                            val hasFlag = choice.effects.requiredFlag.isEmpty() || player.storyFlagsEncoded.split(",").contains(choice.effects.requiredFlag)
                            NodeChoiceButton(choice = choice, activeLang = activeLang, highlightColor = when { choice.effects.alignmentShift > 0 -> ColorSanctumPrimary; choice.effects.alignmentShift < 0 -> ColorCovenantGlow; else -> ColorNeutralPrimary }, testTagValue = "choice_btn_${choice.id}", enabled = hasFlag, onClick = { onChoiceSelected(choice) })
                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingL),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = LocalizationManager.getString(activeLang, "ui.loading_sector_data"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(Dimens.SpacingXl))
        }
    }
}
