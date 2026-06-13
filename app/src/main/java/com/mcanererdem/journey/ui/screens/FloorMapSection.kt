package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.model.AdventureNode
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.NodeType

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.ui.theme.*

@Composable
fun getNodeIcon(type: NodeType): String {
    return when (type) {
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
}

@Composable
fun NodeCircle(
    node: AdventureNode,
    isCurrent: Boolean,
    isCleared: Boolean,
    isAccessible: Boolean,
    isSelected: Boolean,
    size: Dp = 34.dp,
    onClick: () -> Unit
) {
    val circleBorderColor = when {
        isSelected -> ColorSanctumPrimary
        isCurrent -> MaterialTheme.colorScheme.primary
        isCleared -> ColorHeal
        isAccessible -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    }

    val circleBgColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        isCleared -> ColorHeal.copy(alpha = 0.1f)
        isSelected -> ColorSanctumPrimary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(circleBgColor)
            .border(
                width = if (isSelected || isCurrent) Dimens.BorderNormal else Dimens.BorderThin,
                color = circleBorderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isCleared && !isCurrent) {
            Text("✓", fontSize = (size.value * 0.45f).sp, fontWeight = FontWeight.Bold, color = ColorHeal)
        } else {
            Text(
                text = getNodeIcon(node.type),
                fontSize = (size.value * 0.55f).sp,
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
    animationsEnabled: Boolean,
    onLockedClicked: (String) -> Unit,
    onNextNodeClick: (Int, Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val currentNodeIdx = player.currentNodeIndex.coerceIn(0, nodes.size - 1)
    val currentDepth = if (nodes.isNotEmpty()) nodes[currentNodeIdx].depth else 0
    var selectedNodeIdx by remember(player.currentNodeIndex, isExpanded) { mutableIntStateOf(player.currentNodeIndex) }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = Dimens.SpacingXs, vertical = Dimens.SpacingXxs)) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    if (animationsEnabled) {
                        (fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)))
                            .togetherWith(fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150)))
                    } else {
                        fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                    }
                },
                label = "MapExpansion"
            ) { expanded ->
                if (!expanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingXs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${player.currentFloor}.${currentDepth + 1}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = "FLOOR", style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold), color = ColorOnSurfaceMuted)
                        }
                        
                        // Windowing logic for collapsed view (like in 'changes' file)
                        val windowSize = 7
                        val halfWindow = windowSize / 2
                        val startIdx = (player.currentNodeIndex - halfWindow).coerceIn(0, (nodes.size - windowSize).coerceAtLeast(0))
                        val endIdx = (startIdx + windowSize - 1).coerceAtMost(nodes.size - 1)

                        Row(
                            modifier = Modifier.weight(1f).padding(horizontal = Dimens.SpacingS),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            for (idx in startIdx..endIdx) {
                                val n = nodes[idx]
                                val isCurr = idx == player.currentNodeIndex
                                val isClrd = idx < player.currentNodeIndex || (idx == player.currentNodeIndex && player.currentNodeCompleted)
                                
                                NodeCircle(
                                    node = n,
                                    isCurrent = isCurr,
                                    isCleared = isClrd,
                                    isAccessible = true,
                                    isSelected = false,
                                    size = 26.dp,
                                    onClick = { isExpanded = true }
                                )
                                if (idx < endIdx) {
                                    Box(modifier = Modifier.width(6.dp).height(Dimens.BorderThin).background(if (isClrd) ColorHeal else ColorBorderMuted))
                                }
                            }
                        }
                        
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimens.IconS))
                    }
                } else {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingXs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${player.currentFloor}.${currentDepth + 1}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 22.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(text = "FLOOR", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = ColorOnSurfaceMuted)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
                                val inspectNode = nodes.getOrNull(selectedNodeIdx)
                                val nodeTypeLabel = inspectNode?.type?.name ?: "UNKNOWN"
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(Dimens.RadiusXs)
                                ) {
                                    Text(
                                        text = nodeTypeLabel,
                                        modifier = Modifier.padding(horizontal = Dimens.SpacingS, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(onClick = { isExpanded = false }, modifier = Modifier.size(Dimens.IconM)) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        val scrollStateExpanded = rememberScrollState()
                        LaunchedEffect(isExpanded, player.currentNodeIndex) {
                            if (isExpanded) {
                                // Improved focus calculation: (index * width) - center_offset
                                // Node 36dp + Space 32dp = 68dp per node roughly.
                                val target = (player.currentNodeIndex * 68) - 160
                                if (animationsEnabled) {
                                    scrollStateExpanded.animateScrollTo(target.coerceAtLeast(0))
                                } else {
                                    scrollStateExpanded.scrollTo(target.coerceAtLeast(0))
                                }
                            }
                        }
                        
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.SpacingS).horizontalScroll(scrollStateExpanded)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = Dimens.SpacingM)) {
                                nodes.forEachIndexed { idx, node ->
                                    NodeCircle(
                                        node = node,
                                        isCurrent = idx == player.currentNodeIndex,
                                        isCleared = idx < player.currentNodeIndex || (idx == player.currentNodeIndex && player.currentNodeCompleted),
                                        isAccessible = true,
                                        isSelected = selectedNodeIdx == idx,
                                        size = 36.dp, 
                                        onClick = { selectedNodeIdx = idx }
                                    )
                                    if (idx < nodes.size - 1) {
                                        val isClrd = idx < player.currentNodeIndex || (idx == player.currentNodeIndex && player.currentNodeCompleted)
                                        Box(modifier = Modifier.width(Dimens.SpacingL).height(Dimens.BorderNormal).background(if (isClrd) ColorHeal else ColorBorderMuted.copy(alpha = 0.5f)))
                                    }
                                }
                            }
                        }

                        val inspectNode = nodes.getOrNull(selectedNodeIdx)
                        if (inspectNode != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingXs),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(Dimens.RadiusS),
                                border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(Dimens.SpacingS)) {
                                    Text(
                                        text = LocalizationManager.getString(activeLang, inspectNode.titleKey).uppercase(), 
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), 
                                        color = ColorOnSurface
                                    )
                                    
                                    val dangerLevel = player.currentFloor / 10 + 1
                                    val dangerText = LocalizationManager.getString(activeLang, "ui.label_danger") + ": $dangerLevel"
                                    
                                    if (inspectNode.type == NodeType.COMBAT || inspectNode.type == NodeType.BOSS) {
                                         Text(
                                             text = dangerText,
                                             style = MaterialTheme.typography.labelSmall,
                                             color = ColorDanger,
                                             modifier = Modifier.padding(top = 2.dp)
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
}
