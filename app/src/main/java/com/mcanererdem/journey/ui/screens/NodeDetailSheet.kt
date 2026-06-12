package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.data.model.AdventureNode
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.NodeType

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.ui.theme.*

@Composable
fun NodeDetailModal(
    node: AdventureNode,
    floor: Int,
    nodeIndex: Int,
    isCurrent: Boolean,
    isCleared: Boolean,
    isAccessible: Boolean,
    activeLang: String,
    journal: List<JournalEntry>,
    onDismiss: () -> Unit,
    onEnterNode: (Int, Int) -> Unit
) {
    val nodeName = LocalizationManager.getString(activeLang, node.titleKey)
    val nodeDesc = LocalizationManager.getString(activeLang, node.descriptionKey)

    val primaryColor = when (node.type) {
        NodeType.COMBAT, NodeType.BOSS -> ColorDanger
        NodeType.NARRATIVE -> MaterialTheme.colorScheme.primary
        NodeType.CHEST -> ColorSanctumPrimary
        NodeType.SHRINE, NodeType.MERCHANT -> ColorCovenantGlow
        NodeType.CAMP -> ColorHeal
        NodeType.EVENT -> ColorInfo
        NodeType.SECRET -> ColorWarning
    }

    val typeIcon = when (node.type) {
        NodeType.COMBAT -> "⚔️"
        NodeType.BOSS -> "💀"
        NodeType.CHEST -> "💎"
        NodeType.SHRINE -> "🔮"
        NodeType.MERCHANT -> "🔮"
        NodeType.NARRATIVE -> "📜"
        NodeType.CAMP -> "⛺"
        NodeType.EVENT -> "🎲"
        NodeType.SECRET -> "🕵️"
    }

    val typeLabel = when (node.type) {
        NodeType.COMBAT -> LocalizationManager.getString(activeLang, "ui.node_type_combat")
        NodeType.BOSS -> LocalizationManager.getString(activeLang, "ui.node_type_boss")
        NodeType.CHEST -> LocalizationManager.getString(activeLang, "ui.node_type_treasure")
        NodeType.SHRINE -> LocalizationManager.getString(activeLang, "ui.node_type_shrine")
        NodeType.MERCHANT -> LocalizationManager.getString(activeLang, "ui.node_type_merchant")
        NodeType.NARRATIVE -> LocalizationManager.getString(activeLang, "ui.node_type_narrative")
        NodeType.CAMP -> LocalizationManager.getString(activeLang, "ui.node_type_camp")
        NodeType.EVENT -> LocalizationManager.getString(activeLang, "ui.node_type_event")
        NodeType.SECRET -> LocalizationManager.getString(activeLang, "ui.node_type_secret")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = typeIcon, fontSize = 20.sp, modifier = Modifier.padding(end = Dimens.SpacingS))
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
            ) {
                Text(
                    text = nodeName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                
                Text(
                    text = nodeDesc,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = Dimens.TextXxl),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val matchEntry = journal.find { it.floor == floor && it.nodeIndex == nodeIndex }
                if (matchEntry != null) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingS))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ColorHeal.copy(alpha = 0.1f)),
                        border = BorderStroke(Dimens.BorderThin, ColorHeal.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                            Text(
                                text = LocalizationManager.getString(activeLang, "ui.journal_record_detail"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = Dimens.TextXxs),
                                color = ColorHeal
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                            Text(
                                text = matchEntry.getActionTaken(activeLang),
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (!isCleared && !isCurrent) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingS))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚡", fontSize = Dimens.TextS)
                        Text(
                            text = LocalizationManager.formatString(activeLang, "ui.node_explore_cost", node.willCost),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = ColorSanctumPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isAccessible && !isCleared && !isCurrent) {
                Button(
                    onClick = {
                        onEnterNode(node.depth, node.column)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.btn_explore_label"),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.btn_dismiss"),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        dismissButton = {
            if (isAccessible && !isCleared && !isCurrent) {
                OutlinedButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.btn_dismiss"),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    )
}
