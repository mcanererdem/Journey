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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun StatusEffectsRow(statuses: List<CombatStatus>, activeLang: String) {
    if (statuses.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = Dimens.SpacingXs)
    ) {
        statuses.forEach { status ->
            val statusColor = when (status.type) {
                StatusType.POISONED -> ColorWarning
                StatusType.STUNNED -> ColorCovenantGlow
                StatusType.BLESSED -> ColorStatGold
                StatusType.SHIELDED -> ColorSanctumPrimary
            }
            val statusText = when (status.type) {
                StatusType.POISONED -> LocalizationManager.getString(activeLang, "ui.status_poisoned")
                StatusType.STUNNED -> LocalizationManager.getString(activeLang, "ui.status_stunned")
                StatusType.BLESSED -> LocalizationManager.getString(activeLang, "ui.status_blessed")
                StatusType.SHIELDED -> LocalizationManager.getString(activeLang, "ui.status_shielded")
            }
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(Dimens.SpacingXs))
                    .border(Dimens.BorderThin, statusColor.copy(alpha = 0.6f), RoundedCornerShape(Dimens.SpacingXs))
                    .padding(horizontal = Dimens.SpacingS, vertical = Dimens.BorderThick)
            ) {
                Text(
                    text = "${statusText} (${status.durationTurns}t)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = Dimens.TextXxs),
                    color = statusColor
                )
            }
        }
    }
}

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
    val mobHp = activeEnemyHp ?: maxEnemyHp
    val isBoss = activeNode.type == NodeType.BOSS
    
    var lastHp by remember { mutableIntStateOf(mobHp) }
    val shakeTrigger = remember { mutableIntStateOf(0) }
    LaunchedEffect(mobHp) {
        if (mobHp < lastHp) shakeTrigger.value += 1
        lastHp = mobHp
    }
 
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.SpacingL)
            .shake(shakeTrigger.value)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalizationManager.formatString(activeLang, "ui.combat_sector_label", activeNode.depth + 1).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(color = ColorDanger, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
            Box(
                modifier = Modifier.background(ColorSanctumPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).border(0.5.dp, ColorSanctumPrimary, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = LocalizationManager.getString(activeLang, "ui.combat_your_turn"), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ColorSanctumPrimary))
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).border(1.dp, ColorDanger.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).background(ColorSurface, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text(text = if (isBoss) "💀" else "⚔️", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                val enemyId = activeNode.enemy?.enemyId ?: "unknown"
                val stats = LocalizationManager.getEnemyStats(enemyId)
                val enemyNameKey = stats?.optString("nameKey") ?: "enemy.$enemyId.name"
                val enemyName = LocalizationManager.getString(activeLang, enemyNameKey)
                
                Text(text = enemyName.uppercase(), style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), color = ColorOnBackground)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.background(ColorDanger.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                        Text(text = if (isBoss) LocalizationManager.getString(activeLang, "ui.label_boss") else LocalizationManager.getString(activeLang, "ui.node_threat_hostile"), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ColorDanger))
                    }
                    Text(text = "Lv ${player.level} • ${LocalizationManager.getString(activeLang, "ui.label_neutral")}", style = MaterialTheme.typography.labelSmall, color = ColorOnSurfaceMuted)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = LocalizationManager.getString(activeLang, "ui.combat_hostility"), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ColorDanger))
                Text(text = "$mobHp / $maxEnemyHp", style = MaterialTheme.typography.labelSmall, color = ColorOnSurface)
            }
            LinearProgressIndicator(progress = { mobHp.toFloat() / maxEnemyHp.toFloat() }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = ColorDanger, trackColor = ColorBorderMuted)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                var lastPlayerHp by remember { mutableIntStateOf(player.currentHp) }
                val flashTrigger = remember { mutableIntStateOf(0) }
                LaunchedEffect(player.currentHp) {
                    if (player.currentHp < lastPlayerHp) flashTrigger.value += 1
                    lastPlayerHp = player.currentHp
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = LocalizationManager.getString(activeLang, "ui.combat_vitality"), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ColorDanger))
                    Text(text = "${player.currentHp}/${player.maxHp}", style = MaterialTheme.typography.labelSmall, color = ColorOnSurfaceMuted)
                }
                LinearProgressIndicator(progress = { player.currentHp.toFloat() / player.maxHp.toFloat() }, modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)).damageFlash(flashTrigger.value), color = ColorDanger, trackColor = ColorBorderMuted)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = LocalizationManager.getString(activeLang, "ui.combat_essence"), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ColorCovenantGlow))
                    Text(text = "${player.currentWill}/${player.maxWill}", style = MaterialTheme.typography.labelSmall, color = ColorOnSurfaceMuted)
                }
                LinearProgressIndicator(progress = { player.currentWill.toFloat() / player.maxWill.toFloat() }, modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)), color = ColorCovenantGlow, trackColor = ColorBorderMuted)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(text = LocalizationManager.getString(activeLang, "ui.combat_battle_register"), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ColorOnSurfaceMuted, letterSpacing = 0.5.sp))
        Spacer(modifier = Modifier.height(6.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorSurface.copy(alpha = 0.5f)), border = BorderStroke(0.5.dp, ColorBorder)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (combatLog.isEmpty()) {
                    Text(text = LocalizationManager.getString(activeLang, "ui.combat_initiated"), style = MaterialTheme.typography.bodySmall, color = ColorOnSurfaceMuted)
                } else {
                    combatLog.takeLast(5).forEach { log ->
                        val logText = log.getFormattedText(activeLang)
                        val (logIcon, logColor) = when {
                            log.key == "ui.combat_log_initiated" || log.key == "ui.combat_log_secret_boss_initiated" -> Pair("✦", ColorCovenantGlow)
                            log.key.startsWith("ui.combat_log_player_") || log.key == "ui.combat_log_crit" -> Pair("›", ColorSanctumPrimary)
                            log.key.startsWith("ui.combat_log_enemy_") || log.key.startsWith("ui.combat_log_boss_") -> Pair("†", ColorDanger)
                            log.key == "ui.combat_log_victory" || log.key == "ui.combat_log_loot" || log.key == "ui.combat_log_title" || log.key == "ui.combat_log_level_up" -> Pair("★", ColorStatGold)
                            else -> Pair("•", ColorOnSurface)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(14.dp).background(logColor.copy(alpha = 0.15f), RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
                                Text(text = logIcon, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp, color = logColor))
                            }
                            Text(text = logText, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif), color = ColorOnSurface)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CombatActionCard(title = LocalizationManager.getString(activeLang, "ui.combat_action_strike"), subtitle = LocalizationManager.getString(activeLang, "ui.combat_action_strike_desc"), icon = "⚔️", borderColor = ColorSanctumPrimary, onClick = { onCombatAction("LIGHT_STRIKE") })
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) { CombatActionCard(title = LocalizationManager.getString(activeLang, "ui.combat_action_skills"), subtitle = LocalizationManager.formatString(activeLang, "ui.combat_action_skills_desc", 3), icon = "✦", borderColor = ColorCovenantGlow, enabled = player.aether >= 15, onClick = { onCombatAction("HEAVY_BLOW") }) }
                Box(modifier = Modifier.weight(1f)) { CombatActionCard(title = LocalizationManager.getString(activeLang, "ui.combat_action_item"), subtitle = LocalizationManager.formatString(activeLang, "ui.combat_action_item_desc", 2), icon = "⚱️", borderColor = ColorHeal, onClick = { onCombatAction("BARRIER") }) }
            }
            CombatActionCard(title = LocalizationManager.getString(activeLang, "ui.combat_action_defend"), subtitle = LocalizationManager.getString(activeLang, "ui.combat_action_defend_desc"), icon = "🛡️", borderColor = ColorOnSurfaceMuted, onClick = { onCombatAction("BARRIER") })
            CombatActionCard(title = LocalizationManager.getString(activeLang, "ui.combat_action_flee"), subtitle = if (isBoss) LocalizationManager.getString(activeLang, "ui.combat_action_flee_boss_desc") else LocalizationManager.getString(activeLang, "ui.combat_action_flee_desc"), icon = "🏃", borderColor = ColorOnSurfaceSubtle, enabled = !isBoss, onClick = { onCombatAction("ESCAPE") })
            if (isBoss) CombatActionCard(title = LocalizationManager.getString(activeLang, "ui.combat_action_covenant_call"), subtitle = LocalizationManager.getString(activeLang, "ui.combat_action_covenant_call_desc"), icon = "✨", borderColor = ColorSanctumPrimary, extraRightText = LocalizationManager.formatString(activeLang, "ui.combat_essence_cost", 12), onClick = { onCombatAction("HEAVY_BLOW") })
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = LocalizationManager.getString(activeLang, "ui.combat_encounter_timer"), style = MaterialTheme.typography.labelSmall.copy(color = ColorOnSurfaceMuted, fontWeight = FontWeight.Bold, fontSize = 8.sp, letterSpacing = 0.5.sp))
                LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.fillMaxWidth(0.6f).height(2.dp).clip(RoundedCornerShape(1.dp)), color = ColorDanger, trackColor = ColorBorderMuted)
            }
            Text(text = LocalizationManager.formatString(activeLang, "ui.combat_round", 3), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ColorOnSurface))
        }
    }
}

fun Modifier.shake(trigger: Int): Modifier = composed {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 300
                    -10f at 50
                    10f at 100
                    -10f at 150
                    10f at 200
                    -5f at 250
                }
            )
        }
    }
    this.graphicsLayer {
        translationX = shake.value
    }
}

fun Modifier.damageFlash(trigger: Int): Modifier = composed {
    val color = remember { Animatable(Color.Transparent) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            color.animateTo(Color.Red.copy(alpha = 0.5f), animationSpec = tween(50))
            color.animateTo(Color.Transparent, animationSpec = tween(200))
        }
    }
    this.background(color.value)
}

@Composable
fun CombatActionCard(
    title: String,
    subtitle: String,
    icon: String,
    borderColor: Color,
    enabled: Boolean = true,
    extraRightText: String = "",
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_pulse")
    val glowAlpha by if (enabled) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )
    } else {
        remember { mutableStateOf(0.4f) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) ColorSurface else ColorSurface.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, if (enabled) borderColor.copy(alpha = glowAlpha) else ColorBorderMuted)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(borderColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 14.sp,
                        color = if (enabled) borderColor else ColorOnSurfaceMuted
                    )
                }
                
                Column {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (enabled) ColorOnBackground else ColorOnSurfaceMuted
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = ColorOnSurfaceMuted
                    )
                }
            }
            
            if (extraRightText.isNotEmpty()) {
                Text(
                    text = extraRightText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ColorSanctumPrimary,
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}
