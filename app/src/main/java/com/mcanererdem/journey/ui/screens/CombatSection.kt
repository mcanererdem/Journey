package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.data.model.CombatStatus
import com.mcanererdem.journey.data.model.EnemyIntent
import com.mcanererdem.journey.data.model.StatusType
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.model.AdventureNode
import com.mcanererdem.journey.data.model.NodeType

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.composed
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.ui.theme.*

import com.mcanererdem.journey.data.model.CombatLogEntry

@Composable
fun CombatSection(
    player: PlayerProfile,
    activeNode: AdventureNode,
    activeEnemyHp: Int?,
    combatLog: List<CombatLogEntry>,
    activeLang: String,
    onCombatAction: (String) -> Unit
) {
    val maxEnemyHp = activeNode.enemy?.overrideHp ?: 50
    val isVictory = combatLog.any { it.key == "combat_log_victory" || it.key == "ui.combat_log_victory" }
    val mobHp = if (isVictory) 0 else (activeEnemyHp ?: maxEnemyHp)
    val isBoss = activeNode.type == NodeType.BOSS
    
    var lastHp by remember { mutableIntStateOf(mobHp) }
    val shakeTrigger = remember { mutableIntStateOf(0) }
    LaunchedEffect(mobHp) {
        if (mobHp < lastHp) shakeTrigger.intValue += 1
        lastHp = mobHp
    }

    var showSkillsDialog by remember { mutableStateOf(false) }
    var showItemsDialog by remember { mutableStateOf(false) }
    val itemList = remember(player.itemsEncoded) { player.itemsEncoded.split(",").filter { it.isNotBlank() } }
 
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
                .shake(shakeTrigger.intValue)
        ) {
            // VS Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingS),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Box(modifier = Modifier.size(36.dp).border(Dimens.BorderThin, ColorHeal, CircleShape).background(ColorHeal.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(text = "👤", fontSize = 14.sp)
                    }
                    Text(text = player.playerName.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.sp), color = MaterialTheme.colorScheme.onBackground)
                }

                Text(text = "VS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))

                Column(horizontalAlignment = Alignment.End) {
                    Box(modifier = Modifier.size(36.dp).border(Dimens.BorderThin, ColorDanger, CircleShape).background(ColorDanger.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(text = if (isBoss) "💀" else "⚔️", fontSize = 14.sp)
                    }
                    val enemyId = activeNode.enemy?.enemyId ?: "unknown"
                    val stats = LocalizationManager.getEnemyStats(enemyId)
                    val enemyName = stats?.optString("nameKey")?.let { LocalizationManager.getString(activeLang, it) } ?: enemyId.replace("_", " ").uppercase()
                    Text(text = enemyName.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.sp), color = ColorDanger)
                }
            }

            // Health Area
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpacingS), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
                    val pRatio = (player.currentHp.toFloat() / player.maxHp.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(progress = { pRatio }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(Dimens.RadiusXs)), color = ColorHeal, trackColor = ColorBorderMuted)
                    Text(text = "HP ${player.currentHp}/${player.maxHp}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = ColorHeal)
                    
                    val aRatio = (player.aether.toFloat() / 100f).coerceIn(0f, 1f)
                    LinearProgressIndicator(progress = { aRatio }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(Dimens.RadiusXs)), color = ColorStatAether, trackColor = ColorBorderMuted)
                    Text(text = "AETHER ${player.aether}%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold), color = ColorStatAether)
                }
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val eRatio = (mobHp.toFloat() / maxEnemyHp.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(progress = { eRatio }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(Dimens.RadiusXs)), color = ColorDanger, trackColor = ColorBorderMuted)
                    Text(text = "VITALITY ${mobHp}/${maxEnemyHp}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = ColorDanger)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingS))

            // Scrollable Combat Log
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = Dimens.SpacingXs),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(Dimens.RadiusS)
            ) {
                val logScrollState = rememberScrollState()
                LaunchedEffect(combatLog.size) { logScrollState.animateScrollTo(logScrollState.maxValue) }
                Column(modifier = Modifier.padding(Dimens.SpacingM).verticalScroll(logScrollState), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (combatLog.isEmpty()) {
                        Text(text = LocalizationManager.getString(activeLang, "ui.combat_initiated"), style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    } else {
                        combatLog.forEach { log ->
                            val cleanKey = log.key.removePrefix("ui.")
                            val logColor = when {
                                cleanKey.startsWith("combat_log_player") -> ColorHeal
                                cleanKey.startsWith("combat_log_enemy") || cleanKey.startsWith("combat_log_boss") -> ColorDanger
                                cleanKey.contains("victory") || cleanKey.contains("reward") || cleanKey.contains("loot") -> ColorStatGold
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            Text(
                                text = "› ${log.getFormattedText(activeLang)}", 
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                                color = logColor
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(Dimens.SpacingXs)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), RoundedCornerShape(Dimens.RadiusM))
                .border(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(Dimens.RadiusM))
                .padding(Dimens.SpacingXxs),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)
        ) {
            val itemCount = itemList.size
            CompactCombatButton(icon = "⚔️", label = "STRIKE", color = ColorSanctumPrimary, onClick = { onCombatAction("LIGHT_STRIKE") }, modifier = Modifier.weight(1f))
            CompactCombatButton(icon = "🛡️", label = "DEFEND", color = ColorInfo, onClick = { onCombatAction("BARRIER") }, modifier = Modifier.weight(1f))
            CompactCombatButton(icon = "✦", label = "SKILLS", color = ColorStatAether, badgeCount = 3, onClick = { showSkillsDialog = true }, modifier = Modifier.weight(1f))
            CompactCombatButton(icon = "🎒", label = "ITEMS", color = ColorHeal, badgeCount = itemCount, enabled = itemCount > 0, onClick = { showItemsDialog = true }, modifier = Modifier.weight(1f))
        }

        if (showSkillsDialog) {
            CombatSelectionDialog(
                title = "REFLECTION SKILLS",
                options = listOf("HEAVY_BLOW" to "Heavy Blow (-15 Aether)", "RESTORATION" to "Aether Pulse (+10 Will)"),
                onSelect = { onCombatAction(it); showSkillsDialog = false },
                onDismiss = { showSkillsDialog = false }
            )
        }
        if (showItemsDialog) {
            CombatSelectionDialog(
                title = "EQUIPMENT ARSENAL",
                options = itemList.map { it to it },
                onSelect = { showItemsDialog = false },
                onDismiss = { showItemsDialog = false }
            )
        }
    }
}

@Composable
fun CombatSelectionDialog(title: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("CLOSE") } },
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
                options.forEach { (id, label) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(id) },
                        border = BorderStroke(Dimens.BorderThin, ColorBorder)
                    ) {
                        Text(text = label, modifier = Modifier.padding(Dimens.SpacingM), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    )
}

@Composable
fun CompactCombatButton(
    icon: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp).clickable(enabled = enabled) { onClick() },
            shape = RoundedCornerShape(Dimens.RadiusS),
            color = if (enabled) color.copy(alpha = 0.1f) else ColorSurface.copy(alpha = 0.4f),
            border = BorderStroke(Dimens.BorderThin, if (enabled) color.copy(alpha = 0.3f) else ColorBorderMuted)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = icon, fontSize = 18.sp)
                Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 7.sp), color = if (enabled) color else ColorOnSurfaceMuted)
            }
        }
        if (badgeCount > 0) {
            Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp).size(14.dp).background(color, CircleShape).border(Dimens.BorderThin, ColorSurface, CircleShape), contentAlignment = Alignment.Center) {
                Text(text = badgeCount.toString(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Black, color = ColorOnBackground))
            }
        }
    }
}

fun Modifier.shake(trigger: Int): Modifier = composed {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            shake.animateTo(0f, animationSpec = keyframes { durationMillis = 400; -8f at 50; 8f at 100; -8f at 150; 8f at 200; -4f at 250; 4f at 300; -2f at 350; 0f at 400 })
        }
    }
    this.graphicsLayer { translationX = shake.value }
}
