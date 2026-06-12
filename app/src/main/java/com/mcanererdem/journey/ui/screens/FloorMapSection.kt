package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.model.AdventureNode
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.NodeType

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.ui.theme.*

@Composable
fun LegendItem(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = Dimens.TextXs, modifier = Modifier.padding(end = Dimens.BorderThick))
        Text(text = text, style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
    }
}

@Composable
fun NodeCircle(
    node: AdventureNode,
    isCurrent: Boolean,
    isCleared: Boolean,
    isAccessible: Boolean,
    isTypeHidden: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circle_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    val circleBorderColor = when {
        isSelected -> ColorSanctumPrimary
        isCurrent -> MaterialTheme.colorScheme.primary
        isCleared -> ColorHeal
        isAccessible -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    }

    val circleBgColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        isCleared -> ColorHeal.copy(alpha = 0.08f)
        isSelected -> ColorSanctumPrimary.copy(alpha = 0.08f)
        isAccessible -> MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(circleBgColor)
            .border(
                width = if (isSelected) Dimens.BorderThick else if (isCurrent) Dimens.BorderNormal else Dimens.BorderThin,
                color = if (isSelected || isCurrent) circleBorderColor.copy(alpha = pulseAlpha) else circleBorderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isCleared && !isCurrent) {
            Text("✓", fontSize = Dimens.TextXs, fontWeight = FontWeight.Bold, color = ColorHeal)
        } else if (isTypeHidden) {
            Text("❓", fontSize = Dimens.TextXs, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        } else {
            Text(
                text = when (node.type) {
                    NodeType.COMBAT -> "⚔️"
                    NodeType.BOSS -> "💀"
                    NodeType.CHEST -> "💎"
                    NodeType.SHRINE -> "🔮"
                    NodeType.MERCHANT -> "🔮"
                    NodeType.NARRATIVE -> "📜"
                    NodeType.CAMP -> "⛺"
                    NodeType.EVENT -> "🎲"
                    NodeType.SECRET -> "🕵️"
                },
                fontSize = Dimens.TextXs,
                color = if (isCleared || isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun FloorProgressCartographyMap(
    player: PlayerProfile,
    nodes: List<AdventureNode>,
    activeLang: String,
    journal: List<JournalEntry>,
    scoutedNodeIndices: Set<Int>,
    onScoutClick: () -> Unit,
    onLockedClicked: (String, String) -> Unit,
    onNextNodeClick: (Int, Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val selectedFloor = player.currentFloor
    val defaultNodeIdx = if (nodes.isEmpty()) 0 else player.currentNodeIndex.coerceIn(0, nodes.size - 1)
    
    var selectedNodeIdx by remember(player.currentFloor, player.currentNodeIndex) { mutableIntStateOf(defaultNodeIdx) }
    var activeModalNode by remember { mutableStateOf<AdventureNode?>(null) }

    val currentDepth = if (player.currentNodeIndex in nodes.indices) nodes[player.currentNodeIndex].depth else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.SpacingS)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(Dimens.SpacingM),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingS)) {
            if (!isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.map_label"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        val totalDepths = 20
                        val windowSize = 7
                        val halfWindow = windowSize / 2
                        val startDepth = (currentDepth - halfWindow).coerceIn(0, (totalDepths - windowSize).coerceAtLeast(0))
                        val endDepth = (startDepth + windowSize - 1).coerceAtMost(totalDepths - 1)
                        
                        for (d in startDepth..endDepth) {
                            val nodeAtDepth = nodes.firstOrNull { it.depth == d }
                            if (nodeAtDepth != null) {
                                val isCurrent = d == currentDepth
                                val isCleared = d < currentDepth || (isCurrent && player.currentNodeCompleted)
                                
                                val (icon, color) = when {
                                    isCurrent -> {
                                        val iconStr = when (nodeAtDepth.type) {
                                            NodeType.COMBAT -> "⚔️"
                                            NodeType.BOSS -> "💀"
                                            NodeType.CHEST -> "🎁"
                                            NodeType.SHRINE -> "⛩️"
                                            NodeType.MERCHANT -> "⚱️"
                                            NodeType.NARRATIVE -> "📜"
                                            NodeType.CAMP -> "⛺"
                                            NodeType.EVENT -> "🎲"
                                            NodeType.SECRET -> "🕵️"
                                        }
                                        Pair(iconStr, ColorSanctumPrimary)
                                    }
                                    isCleared -> Pair("✓", ColorHeal)
                                    else -> {
                                        if (d == totalDepths - 1) {
                                            Pair("💀", ColorOnSurfaceSubtle)
                                        } else {
                                            Pair("⬡", ColorOnSurfaceSubtle)
                                        }
                                    }
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(
                                            if (isCurrent) ColorSanctumPrimary.copy(alpha = 0.15f)
                                            else if (isCleared) ColorHeal.copy(alpha = 0.15f)
                                            else ColorSurface,
                                            CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrent) ColorSanctumPrimary else if (isCleared) ColorHeal else ColorBorder,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = icon,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isCurrent) ColorSanctumPrimary else if (isCleared) ColorHeal else ColorOnSurfaceMuted
                                    )
                                }
                                
                                if (d < endDepth) {
                                    Box(
                                        modifier = Modifier
                                            .width(8.dp)
                                            .height(1.dp)
                                            .background(if (isCleared) ColorHeal else ColorBorder)
                                    )
                                }
                            }
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = LocalizationManager.getString(activeLang, "ui.desc_expand"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧭",
                        fontSize = Dimens.TextXl,
                        modifier = Modifier.padding(end = Dimens.SpacingS)
                    )
                    Text(
                        text = "${selectedFloor}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.map_floor_label"),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                            color = ColorOnSurfaceMuted
                        )
                    }

                    IconButton(onClick = { isExpanded = false }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = LocalizationManager.getString(activeLang, "ui.desc_collapse"),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingM))

                val scrollState = rememberScrollState()
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f), RoundedCornerShape(Dimens.SpacingS))
                        .border(Dimens.BorderThin, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(Dimens.SpacingS))
                        .padding(vertical = Dimens.SpacingS, horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(vertical = Dimens.SpacingS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (d in 0..19) {
                            val node = nodes.find { it.depth == d }
                            if (node != null) {
                                val nodeIdx = nodes.indexOf(node)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.width(44.dp)
                                ) {
                                    val isSelected = selectedNodeIdx == nodeIdx
                                    val isCurrent = player.currentFloor == selectedFloor && nodeIdx == player.currentNodeIndex
                                    val isCleared = player.currentFloor > selectedFloor || (player.currentFloor == selectedFloor && (node.depth < currentDepth || (nodeIdx == player.currentNodeIndex && player.currentNodeCompleted)))
                                    val isAccessible = player.currentFloor == selectedFloor && (
                                        (nodeIdx == player.currentNodeIndex) ||
                                        (player.currentNodeCompleted && node.depth == currentDepth + 1)
                                    )
                                    val isScouted = scoutedNodeIndices.contains(nodeIdx)
                                    val isTypeHidden = !isCurrent && !isCleared && !isAccessible && !isScouted

                                    NodeCircle(
                                        node = node,
                                        isCurrent = isCurrent,
                                        isCleared = isCleared,
                                        isAccessible = isAccessible,
                                        isTypeHidden = isTypeHidden,
                                        isSelected = isSelected,
                                        onClick = {
                                            selectedNodeIdx = nodeIdx
                                            if (!isAccessible) {
                                                onLockedClicked(
                                                    LocalizationManager.getString(activeLang, "ui.map_locked_sector_msg"),
                                                    ""
                                                )
                                            } else {
                                                activeModalNode = node
                                            }
                                        }
                                    )
                                }
                            }

                            if (d < 19) {
                                val pathCleared = player.currentFloor > selectedFloor || (player.currentFloor == selectedFloor && d < currentDepth)
                                val pathColor = if (pathCleared) ColorHeal else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                val pathHeight = if (pathCleared) Dimens.BorderThick else Dimens.BorderThin
                                
                                Column(
                                    modifier = Modifier.width(Dimens.SpacingS),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier.height(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HorizontalDivider(
                                            modifier = Modifier.fillMaxWidth(),
                                            thickness = pathHeight,
                                            color = pathColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingM))

                val inspectNode = nodes.getOrNull(selectedNodeIdx)
                
                if (inspectNode != null) {
                    val nodeCategory = when (inspectNode.type) {
                        NodeType.COMBAT -> LocalizationManager.getString(activeLang, "ui.node_cat_combat")
                        NodeType.BOSS -> LocalizationManager.getString(activeLang, "ui.node_cat_boss")
                        NodeType.CHEST -> LocalizationManager.getString(activeLang, "ui.node_cat_treasure")
                        NodeType.SHRINE, NodeType.MERCHANT -> LocalizationManager.getString(activeLang, "ui.node_cat_mystery")
                        NodeType.NARRATIVE -> LocalizationManager.getString(activeLang, "ui.node_cat_narrative")
                        NodeType.CAMP -> LocalizationManager.getString(activeLang, "ui.node_cat_camp")
                        NodeType.EVENT -> LocalizationManager.getString(activeLang, "ui.node_cat_event")
                        NodeType.SECRET -> LocalizationManager.getString(activeLang, "ui.node_cat_mystery")
                    }

                    val inspectIsCurrent = player.currentFloor == selectedFloor && player.currentNodeIndex == selectedNodeIdx
                    val inspectIsCleared = player.currentFloor > selectedFloor || 
                            (player.currentFloor == selectedFloor && (inspectNode.depth < currentDepth || (player.currentNodeIndex == selectedNodeIdx && player.currentNodeCompleted)))
                    val inspectIsAccessible = selectedNodeIdx == 0 || inspectIsCurrent || (inspectNode.depth == currentDepth + 1 && player.currentNodeCompleted)

                    val isSelectScouted = scoutedNodeIndices.contains(selectedNodeIdx)
                    val isSelectTypeHidden = !inspectIsCurrent && !inspectIsCleared && !inspectIsAccessible && !isSelectScouted

                    val dangerLevel = when {
                        isSelectTypeHidden -> LocalizationManager.getString(activeLang, "ui.node_threat_unknown")
                        inspectNode.type == NodeType.BOSS -> LocalizationManager.getString(activeLang, "ui.node_threat_boss")
                        inspectNode.type == NodeType.COMBAT -> LocalizationManager.getString(activeLang, "ui.node_threat_combat")
                        inspectNode.type == NodeType.NARRATIVE -> LocalizationManager.getString(activeLang, "ui.node_threat_narrative")
                        else -> LocalizationManager.getString(activeLang, "ui.node_threat_safe")
                    }

                    val costText = when {
                        inspectIsCleared -> LocalizationManager.getString(activeLang, "ui.node_cost_cleared")
                        inspectIsCurrent -> LocalizationManager.getString(activeLang, "ui.node_cost_occupied")
                        inspectIsAccessible -> LocalizationManager.getString(activeLang, "ui.node_cost_movement")
                        else -> LocalizationManager.getString(activeLang, "ui.node_cost_locked")
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(Dimens.SpacingM),
                        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(Dimens.SpacingXxxl)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSelectTypeHidden) "❓" else when (inspectNode.type) {
                                            NodeType.COMBAT -> "⚔️"
                                            NodeType.BOSS -> "💀"
                                            NodeType.CHEST -> "💎"
                                            NodeType.SHRINE -> "🔮"
                                            NodeType.MERCHANT -> "🔮"
                                            NodeType.NARRATIVE -> "📜"
                                            NodeType.CAMP -> "⛺"
                                            NodeType.EVENT -> "🎲"
                                            NodeType.SECRET -> "🕵️"
                                        },
                                        fontSize = Dimens.TextM
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(Dimens.SpacingS))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    val nodeName = LocalizationManager.getString(activeLang, inspectNode.titleKey)
                                    Text(
                                        text = if (isSelectTypeHidden) LocalizationManager.getString(activeLang, "ui.node_hidden_sector") else nodeName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val sectorText = LocalizationManager.getString(activeLang, "ui.label_sector")
                                    Text(
                                        text = if (isSelectTypeHidden) LocalizationManager.getString(activeLang, "ui.node_unexplored") else "$nodeCategory • $sectorText ${selectedNodeIdx + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(Dimens.SpacingS))

                                val statusTagText = when {
                                    inspectIsCurrent -> LocalizationManager.getString(activeLang, "ui.node_status_active")
                                    inspectIsCleared -> LocalizationManager.getString(activeLang, "ui.node_status_cleared")
                                    else -> LocalizationManager.getString(activeLang, "ui.node_status_locked")
                                }
                                val statusBgColor = when {
                                    inspectIsCurrent -> ColorSanctumPrimary.copy(alpha = 0.15f)
                                    inspectIsCleared -> ColorHeal.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                }
                                val statusTextColor = when {
                                    inspectIsCurrent -> ColorSanctumSecondary
                                    inspectIsCleared -> Color(0xFF2E7D32)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(statusBgColor, RoundedCornerShape(Dimens.SpacingXs))
                                        .padding(horizontal = Dimens.SpacingXs, vertical = Dimens.BorderThick)
                                ) {
                                    Text(
                                        text = statusTagText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = Dimens.TextXxs,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = statusTextColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            
                            Text(
                                text = LocalizationManager.getString(activeLang, inspectNode.descriptionKey),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = Dimens.TextXs),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(Dimens.SpacingS))

                            Text(
                                text = LocalizationManager.formatString(activeLang, "ui.node_threat_profile", dangerLevel),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                                color = if (inspectNode.type == NodeType.BOSS || inspectNode.type == NodeType.COMBAT) ColorDanger else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = LocalizationManager.formatString(activeLang, "ui.node_cost_profile", costText),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            if (!isSelectTypeHidden && inspectNode.enemy != null) {
                                val enemy = inspectNode.enemy!!
                                val enemyNameKey = "enemy.${enemy.enemyId}.name"
                                val enemyName = LocalizationManager.getString(activeLang, enemyNameKey)
                                val eHp = enemy.overrideHp ?: 50
                                val eAtk = enemy.overrideAtk ?: 10

                                Text(
                                    text = LocalizationManager.formatString(activeLang, "ui.node_hostile_info", enemyName, eHp, eAtk),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold),
                                    color = if (inspectNode.type == NodeType.BOSS) ColorDanger else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    activeModalNode?.let { n ->
        val nIndex = nodes.indexOf(n)
        val nodeIsCurrent = player.currentFloor == selectedFloor && player.currentNodeIndex == nIndex
        val nodeIsCleared = player.currentFloor > selectedFloor || 
                (player.currentFloor == selectedFloor && (n.depth < currentDepth || (player.currentNodeIndex == nIndex && player.currentNodeCompleted)))
        val nodeIsAccessible = nIndex == player.currentNodeIndex || (player.currentNodeCompleted && n.depth == currentDepth + 1)
        
        NodeDetailModal(
            node = n,
            floor = selectedFloor,
            nodeIndex = nIndex,
            isCurrent = nodeIsCurrent,
            isCleared = nodeIsCleared,
            isAccessible = nodeIsAccessible,
            activeLang = activeLang,
            journal = journal,
            onDismiss = { activeModalNode = null },
            onEnterNode = onNextNodeClick
        )
    }
}
