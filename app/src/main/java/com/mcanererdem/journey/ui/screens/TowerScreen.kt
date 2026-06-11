package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.ui.viewmodel.CombatStatus
import com.mcanererdem.journey.ui.viewmodel.EnemyIntent
import com.mcanererdem.journey.ui.viewmodel.StatusType

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.engine.NodeType
import com.mcanererdem.journey.data.engine.AdventureNode
import com.mcanererdem.journey.data.engine.NodeChoice
import com.mcanererdem.journey.data.engine.NarrativeEvent
import com.mcanererdem.journey.data.engine.NarrativeBranchOption
import com.mcanererdem.journey.data.engine.SecretBossEncounter
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.ui.theme.*

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
                StatusType.POISONED -> if (activeLang == "TR") "Zehir ğŸ§ª" else "Poison ğŸ§ª"
                StatusType.STUNNED -> if (activeLang == "TR") "Sersem ğŸŒ€" else "Stun ğŸŒ€"
                StatusType.BLESSED -> if (activeLang == "TR") "Kutsal âœ¨" else "Blessed âœ¨"
                StatusType.SHIELDED -> if (activeLang == "TR") "Kalkan ğŸ›¡ï¸" else "Shield ğŸ›¡ï¸"
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
fun TowerClimbTab(
    player: PlayerProfile?,
    nodes: List<AdventureNode>,
    activeEnemyHp: Int?,
    combatLog: List<String>,
    activeLang: String,
    journal: List<JournalEntry>,
    scoutedNodeIndices: Set<Int>,
    playerStatuses: List<CombatStatus>,
    enemyStatuses: List<CombatStatus>,
    currentEnemyIntent: EnemyIntent,
    onScoutClick: () -> Unit,
    onLockedClicked: (String, String) -> Unit,
    onChoiceSelected: (NodeChoice) -> Unit,
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
        // Subtle top spacing
        item {
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
        }

        // Checkpoint indicator
        item {
            Text(
                text = LocalizationManager.formatString(activeLang, "label_checkpoint", player.savedFloorCheckpoint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
        }

        // Handcrafted Spires Cartography & Navigation Radar
        item {
            FloorProgressCartographyMap(
                player = player,
                activeLang = activeLang,
                journal = journal,
                scoutedNodeIndices = scoutedNodeIndices,
                onScoutClick = onScoutClick,
                onLockedClicked = onLockedClicked,
                onNextNodeClick = onNextNodeClick
            )
        }

        // Visually Crawling Map Node Progress Row
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.SpacingM),
                shape = RoundedCornerShape(Dimens.SpacingM),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "label_nodes"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                    
                    val currentIndex = player.currentNodeIndex
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sliding window view of 5 exploration nodes around the active node
                        val start = (currentIndex - 2).coerceAtLeast(0)
                        val end = (start + 4).coerceAtMost(nodes.size - 1)
                        for (idx in start..end) {
                            val node = nodes.getOrNull(idx) ?: continue
                            val isActive = idx == currentIndex
                            val isCompleted = idx < currentIndex || (idx == currentIndex && player.currentNodeCompleted)
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(Dimens.BadgeSize)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) MaterialTheme.colorScheme.primaryContainer
                                            else if (isCompleted) ColorHeal.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = if (isActive) Dimens.BorderThick else Dimens.BorderThin,
                                            color = if (isActive) MaterialTheme.colorScheme.primary
                                            else if (isCompleted) ColorHeal
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (node.type) {
                                            NodeType.COMBAT -> "⚔️"
                                            NodeType.BOSS -> "💀"
                                            NodeType.CHEST -> "🎁"
                                            NodeType.SHRINE -> "⛩️"
                                            NodeType.MERCHANT -> "⚱️"
                                            NodeType.NARRATIVE -> "📜"
                                        },
                                        fontSize = Dimens.TextS
                                    )
                                }
                                Spacer(modifier = Modifier.height(Dimens.BorderGlow))
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = Dimens.TextXxs
                                    ),
                                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Central Active Scenario Decisions or Combat Arena
        if (player.currentFloor > 100) {
            // Game Fully Completed Victory State
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
                            text = "👑 SOVEREIGN CONQUEROR 👑",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorSanctumPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacingL))
                        Text(
                            text = "You have climbed all 100 floors of the Cosmic Spires! The universe is forever saved and your name is written in stars.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacingXxl))
                        Button(
                            onClick = onResetClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSanctumPrimary),
                            modifier = Modifier.testTag("ascend_victory_reset")
                        ) {
                            Text(LocalizationManager.getString(activeLang, "ascend_victory_reset"))
                        }
                    }
                }
            }
        } else {
            val activeNode = nodes.getOrNull(player.currentNodeIndex)
            if (activeNode != null) {
                if (player.currentNodeCompleted) {
                    // Sector cleared, show reward summary and continue advancing!
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
                                    text = if (activeLang == "TR") "Sektörü Tamamladınız! 🎉" else "Sector Cleared! 🎉",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ColorHeal
                                )
                                Spacer(modifier = Modifier.height(Dimens.SpacingS))
                                Text(
                                    text = if (activeLang == "TR") {
                                        "Bu bölgedeki sınamalar başarıyla aşıldı. Kule'nin bir sonraki katına çıkmaya veya derindeki diğer olay sektörüne sızmaya hazırsınız."
                                    } else {
                                        "All challenges here have been overcome. You are ready to transcend floors or advance to the next event sector."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(Dimens.SpacingL))

                                if (activeNode.type == NodeType.BOSS) {
                                    // Ascend to the next floor
                                    Button(
                                        onClick = onAscendFloorClick,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(Dimens.AvatarSize)
                                            .testTag("btn_ascend_floor"),
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorHeal)
                                    ) {
                                        Text(
                                            text = LocalizationManager.getString(activeLang, "btn_ascend_floor"),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                } else {
                                    // Prompt to select node on map
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(Dimens.AvatarSize),
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Text(
                                            text = if (activeLang == "TR") "İlerlemek için haritadan bir sektör seçin" else "Select a sector on map to advance",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                }
                            }
                        }
                    }
                }
            } else if (activeNode.type == NodeType.COMBAT || activeNode.type == NodeType.BOSS) {
                    // Turn-Based Combat UI
                    val mobHp = activeEnemyHp ?: activeNode.enemyHp
                    val isBoss = activeNode.type == NodeType.BOSS
                    
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Dimens.SpacingL)
                        ) {
                            // 1. Sector Header & Turn Badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "— SECTOR ${activeNode.depth + 1} · COMBAT —".uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ColorDanger,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .background(ColorSanctumPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, ColorSanctumPrimary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (activeLang == "TR") "SIRA SENDE" else "YOUR TURN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorSanctumPrimary
                                        )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // 2. Boss/Hostile Info Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .border(1.dp, ColorDanger.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .background(ColorSurface, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBoss) "💀" else "⚔️",
                                        fontSize = 20.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = (if (activeLang == "TR") activeNode.enemyNameTr else activeNode.enemyNameEn).uppercase(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ColorOnBackground
                                    )
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(ColorDanger.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (isBoss) "BOSS" else "HOSTILE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ColorDanger
                                                )
                                            )
                                        }
                                        
                                        Text(
                                            text = "Lv ${player.level} • Neutral",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ColorOnSurfaceMuted
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 3. Hostility progress bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (activeLang == "TR") "DÜŞMANLIK" else "HOSTILITY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ColorDanger
                                        ),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Text(
                                        text = "$mobHp / ${activeNode.enemyHp}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ColorOnSurface
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { mobHp.toFloat() / activeNode.enemyHp.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = ColorDanger,
                                    trackColor = ColorBorderMuted
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 4. Vitality and Essence progress bars
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (activeLang == "TR") "YAŞAMSAL GÜÇ" else "VITALITY",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ColorDanger
                                            )
                                        )
                                        Text(
                                            text = "${player.currentHp}/${player.maxHp}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ColorOnSurfaceMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    LinearProgressIndicator(
                                        progress = { player.currentHp.toFloat() / player.maxHp.toFloat() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp)),
                                        color = ColorDanger,
                                        trackColor = ColorBorderMuted
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (activeLang == "TR") "ÖZ ENERJİ" else "ESSENCE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ColorCovenantGlow
                                            )
                                        )
                                        Text(
                                            text = "${player.currentWill}/${player.maxWill}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ColorOnSurfaceMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    LinearProgressIndicator(
                                        progress = { player.currentWill.toFloat() / player.maxWill.toFloat() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp)),
                                        color = ColorCovenantGlow,
                                        trackColor = ColorBorderMuted
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // 5. Battle Register
                            Text(
                                text = if (activeLang == "TR") "SAVAŞ SİCİLİ" else "BATTLE REGISTER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ColorOnSurfaceMuted,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = ColorSurface.copy(alpha = 0.5f)),
                                border = BorderStroke(0.5.dp, ColorBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (combatLog.isEmpty()) {
                                        Text(
                                            text = if (activeLang == "TR") "Savaş başladı..." else "Combat initiated...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ColorOnSurfaceMuted
                                        )
                                    } else {
                                        combatLog.takeLast(5).forEach { log ->
                                            val (logIcon, logColor) = when {
                                                log.contains("begins") || log.contains("savaşa girdiniz") || log.contains("awakens") -> Pair("✦", ColorCovenantGlow)
                                                log.contains("Strike") || log.contains("strike") || log.contains("Vuruş") || log.contains("Ağır Darbe") || log.contains("Heavy Blow") || log.contains("uses") -> Pair("›", ColorSanctumPrimary)
                                                log.contains("retaliates") || log.contains("saldırdı") || log.contains("hasar aldı") || log.contains("hit") || log.contains("dmg") -> Pair("†", ColorDanger)
                                                log.contains("defend") || log.contains("Bariyer") || log.contains("Barier") || log.contains("block") -> Pair("›", ColorHeal)
                                                else -> Pair("•", ColorOnSurface)
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .background(logColor.copy(alpha = 0.15f), RoundedCornerShape(2.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = logIcon,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 8.sp,
                                                            color = logColor
                                                        )
                                                    )
                                                }
                                                Text(
                                                    text = log,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = FontFamily.Serif
                                                    ),
                                                    color = ColorOnSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // 6. Action selection cards
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                CombatActionCard(
                                    title = if (activeLang == "TR") "HAMLE" else "STRIKE",
                                    subtitle = if (activeLang == "TR") "Temel vuruş" else "Basic attack",
                                    icon = "⚔️",
                                    borderColor = ColorSanctumPrimary,
                                    onClick = { onCombatAction("LIGHT_STRIKE") }
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        CombatActionCard(
                                            title = if (activeLang == "TR") "YETENEKLER" else "SKILLS",
                                            subtitle = if (activeLang == "TR") "3 kullanılabilir" else "3 available",
                                            icon = "✦",
                                            borderColor = ColorCovenantGlow,
                                            enabled = player.aether >= 15,
                                            onClick = { onCombatAction("HEAVY_BLOW") }
                                        )
                                    }
                                    
                                    Box(modifier = Modifier.weight(1f)) {
                                        CombatActionCard(
                                            title = if (activeLang == "TR") "EŞYA" else "ITEM",
                                            subtitle = if (activeLang == "TR") "Çantada 2 adet" else "2 in bag",
                                            icon = "⚱️",
                                            borderColor = ColorHeal,
                                            onClick = { onCombatAction("BARRIER") }
                                        )
                                    }
                                }
                                
                                CombatActionCard(
                                    title = if (activeLang == "TR") "SAVUNMA" else "DEFEND",
                                    subtitle = if (activeLang == "TR") "Sonraki blok" else "Block next",
                                    icon = "🛡️",
                                    borderColor = ColorOnSurfaceMuted,
                                    onClick = { onCombatAction("BARRIER") }
                                )
                                
                                val canEscape = !isBoss
                                CombatActionCard(
                                    title = if (activeLang == "TR") "KAÇIŞ" else "FLEE",
                                    subtitle = if (isBoss) {
                                        if (activeLang == "TR") "Mümkün değil — boss dövüşü" else "Unavailable — boss encounter"
                                    } else {
                                        if (activeLang == "TR") "Güvenli bölgeye kaç" else "Escape safely"
                                    },
                                    icon = "🏃",
                                    borderColor = ColorOnSurfaceSubtle,
                                    enabled = canEscape,
                                    onClick = { onCombatAction("ESCAPE") }
                                )
                                
                                if (isBoss) {
                                    CombatActionCard(
                                        title = if (activeLang == "TR") "AHİT ÇAĞRISI" else "COVENANT CALL",
                                        subtitle = if (activeLang == "TR") "Tek seferlik boss engelleme · Semavi Koro" else "One-time boss interrupt · Lawful Choir",
                                        icon = "✨",
                                        borderColor = ColorSanctumPrimary,
                                        extraRightText = "12 Essence",
                                        onClick = { onCombatAction("HEAVY_BLOW") }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // 7. Encounter Timer and Round Counter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (activeLang == "TR") "SAVAŞ ZAMANLAYICISI" else "ENCOUNTER TIMER",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ColorOnSurfaceMuted,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    LinearProgressIndicator(
                                        progress = { 0.4f },
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp)),
                                        color = ColorDanger,
                                        trackColor = ColorBorderMuted
                                    )
                                }
                                
                                Text(
                                    text = if (activeLang == "TR") "ROUND 3" else "ROUND 3",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ColorOnSurface
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // Narrative story node decision choices
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
                                    text = if (activeLang == "TR") activeNode.titleTr else activeNode.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = Dimens.SpacingM)
                                )

                                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                                Spacer(modifier = Modifier.height(Dimens.SpacingM))

                                Text(
                                    text = if (activeLang == "TR") activeNode.descriptionTr else activeNode.description,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = Dimens.TextXxl,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = LocalizationManager.getString(activeLang, "declare_choice"),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = Dimens.LetterSpacingNormal
                            ),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacingM))
                    }

                    // Choices
                    activeNode.optionA?.let { choice ->
                        val hasFlag = choice.requiredStoryFlag.isEmpty() || player.storyFlagsEncoded.split(",").contains(choice.requiredStoryFlag)
                        item {
                            NodeChoiceButton(
                                choice = choice,
                                activeLang = activeLang,
                                highlightColor = ColorSanctumPrimary,
                                testTagValue = "choice_a_btn",
                                enabled = hasFlag,
                                onClick = { onChoiceSelected(choice) }
                            )
                        }
                    }

                    activeNode.optionB?.let { choice ->
                        val hasFlag = choice.requiredStoryFlag.isEmpty() || player.storyFlagsEncoded.split(",").contains(choice.requiredStoryFlag)
                        item {
                            NodeChoiceButton(
                                choice = choice,
                                activeLang = activeLang,
                                highlightColor = ColorCovenantGlow,
                                testTagValue = "choice_b_btn",
                                enabled = hasFlag,
                                onClick = { onChoiceSelected(choice) }
                            )
                        }
                    }

                    activeNode.optionC?.let { choice ->
                        val hasFlag = choice.requiredStoryFlag.isEmpty() || player.storyFlagsEncoded.split(",").contains(choice.requiredStoryFlag)
                        item {
                            NodeChoiceButton(
                                choice = choice,
                                activeLang = activeLang,
                                highlightColor = ColorNeutralPrimary,
                                testTagValue = "choice_c_btn",
                                enabled = hasFlag,
                                onClick = { onChoiceSelected(choice) }
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

@Composable
fun NodeChoiceButton(
    choice: NodeChoice,
    activeLang: String,
    highlightColor: Color,
    testTagValue: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val isTr = activeLang == "TR"
    
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
                // Choice Icon based on alignment/type
                val choiceIcon = when {
                    !enabled -> "🔒"
                    choice.alignmentShift > 0 -> "🙏" // Sanctum/Light
                    choice.alignmentShift < 0 -> "🗡️" // Covenant/Void
                    else -> "📖" // Neutral/Lore
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
                        text = if (isTr) choice.textTr else choice.textEn,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (enabled) ColorOnBackground else ColorOnSurfaceMuted
                    )
                    
                    if (!enabled && choice.requiredStoryFlag.isNotEmpty()) {
                        Text(
                            text = if (isTr) "Gereksinim: ${choice.requiredStoryFlag}" else "Prerequisite: ${choice.requiredStoryFlag}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = ColorDanger
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Badges row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // HP change badge
                        if (choice.hpChange != 0) {
                            val isHpPositive = choice.hpChange > 0
                            val hpText = if (isHpPositive) "+${choice.hpChange} HP" else "${choice.hpChange} HP"
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
                        
                        // Alignment shift badge
                        if (choice.alignmentShift != 0) {
                            val isShiftPositive = choice.alignmentShift > 0
                            val shiftText = if (isShiftPositive) "+Light" else "+Corruption"
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
                        
                        // Gleam / Gold Change badge
                        if (choice.goldChange != 0) {
                            val isGoldPositive = choice.goldChange > 0
                            val goldText = if (isGoldPositive) "+${choice.goldChange} Gleam" else "${choice.goldChange} Gleam"
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
                        
                        // Aether change badge
                        if (choice.aetherChange != 0) {
                            val isAetherPositive = choice.aetherChange > 0
                            val aetherText = if (isAetherPositive) "+${choice.aetherChange} Aether" else "${choice.aetherChange} Aether"
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
                        
                        // Item reward badge
                        if (choice.rewardItem.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(ColorHeal.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "+Item",
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
                    contentDescription = "Select",
                    tint = ColorOnSurfaceMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FloorProgressCartographyMap(
    player: PlayerProfile,
    activeLang: String,
    journal: List<JournalEntry>,
    scoutedNodeIndices: Set<Int>,
    onScoutClick: () -> Unit,
    onLockedClicked: (String, String) -> Unit,
    onNextNodeClick: (Int, Int) -> Unit
) {
    val isTr = activeLang == "TR"
    
    // Always focus exclusively on the current active floor
    val selectedFloor = player.currentFloor
    val defaultNodeIdx = player.currentNodeIndex.coerceIn(0, 37)
    
    var selectedNodeIdx by remember(player.currentFloor, player.currentNodeIndex) { mutableStateOf(defaultNodeIdx) }
    var activeModalNode by remember { mutableStateOf<AdventureNode?>(null) }

    // Load blueprint for currently selected floor
    val blueprint = com.mcanererdem.journey.data.engine.FloorBlueprintSystem.getBlueprintForFloor(selectedFloor, player)
    val nodes = blueprint.nodes
    val currentDepth = nodes.find { it.index == player.currentNodeIndex }?.depth ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.SpacingS),
        shape = RoundedCornerShape(Dimens.SpacingM),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingS)) {
            // Compact Header Title Row with Scouting triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🧭",
                    fontSize = Dimens.TextXl,
                    modifier = Modifier.padding(end = Dimens.SpacingS)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTr) "KAT $selectedFloor HARİTASI" else "FLOOR $selectedFloor MAP",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isTr) "Sektör odalarını yana kaydırarak inceleyin" else "Scroll to inspect path sector rooms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Beautiful Outlined scout trigger button
                OutlinedButton(
                    onClick = onScoutClick,
                    modifier = Modifier.height(30.dp).testTag("scout_action_btn"),
                    contentPadding = PaddingValues(horizontal = Dimens.SpacingS, vertical = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorSanctumPrimary),
                    border = BorderStroke(Dimens.BorderThin, ColorSanctumPrimary.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = if (isTr) "Gözle (-1 ⚡)" else "Scout (-1 ⚡)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingM))

            // Compact Horizontally Scrollable Road Path
            val scrollState = rememberScrollState()
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f), RoundedCornerShape(Dimens.SpacingS))
                    .border(Dimens.BorderThin, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(Dimens.SpacingS))
                    .padding(horizontal = Dimens.SpacingXs, vertical = Dimens.SpacingS)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(vertical = Dimens.SpacingS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (d in 0..19) {
                        val nodesAtDepth = nodes.filter { it.depth == d }
                        if (nodesAtDepth.isNotEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.width(60.dp)
                            ) {
                                if (d == 0 || d == 19) {
                                    val node = nodesAtDepth[0]
                                    val isSelected = selectedNodeIdx == node.index
                                    val isCurrent = player.currentFloor == selectedFloor && node.index == player.currentNodeIndex
                                    val isCleared = player.currentFloor > selectedFloor || (player.currentFloor == selectedFloor && (node.depth < currentDepth || (node.index == player.currentNodeIndex && player.currentNodeCompleted)))
                                    val isAccessible = player.currentFloor == selectedFloor && (
                                        (node.index == player.currentNodeIndex) ||
                                        (player.currentNodeCompleted && node.depth == currentDepth + 1)
                                    )
                                    val isScouted = scoutedNodeIndices.contains(node.index)
                                    val isTypeHidden = !isCurrent && !isCleared && !isAccessible && !isScouted

                                    Spacer(modifier = Modifier.height(30.dp))
                                    NodeCircle(
                                        node = node,
                                        isCurrent = isCurrent,
                                        isCleared = isCleared,
                                        isAccessible = isAccessible,
                                        isTypeHidden = isTypeHidden,
                                        isSelected = isSelected,
                                        onClick = {
                                            selectedNodeIdx = node.index
                                            if (!isAccessible) {
                                                onLockedClicked(
                                                    "🔒 Sector Locked! Must complete previous sector first.",
                                                    "🔒 Sektör Kilitli! Öncelikle bir önceki sektörü tamamlamalısınız."
                                                )
                                            } else {
                                                activeModalNode = node
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(30.dp))
                                } else {
                                    val topNode = nodesAtDepth.find { it.column == 0 }
                                    val bottomNode = nodesAtDepth.find { it.column == 1 }

                                    if (topNode != null) {
                                        val isSelected = selectedNodeIdx == topNode.index
                                        val isCurrent = player.currentFloor == selectedFloor && topNode.index == player.currentNodeIndex
                                        val isCleared = player.currentFloor > selectedFloor || (player.currentFloor == selectedFloor && (topNode.depth < currentDepth || (topNode.index == player.currentNodeIndex && player.currentNodeCompleted)))
                                        val isAccessible = player.currentFloor == selectedFloor && (
                                            (topNode.index == player.currentNodeIndex) ||
                                            (player.currentNodeCompleted && topNode.depth == currentDepth + 1)
                                        )
                                        val isScouted = scoutedNodeIndices.contains(topNode.index)
                                        val isTypeHidden = !isCurrent && !isCleared && !isAccessible && !isScouted

                                        NodeCircle(
                                            node = topNode,
                                            isCurrent = isCurrent,
                                            isCleared = isCleared,
                                            isAccessible = isAccessible,
                                            isTypeHidden = isTypeHidden,
                                            isSelected = isSelected,
                                            onClick = {
                                                selectedNodeIdx = topNode.index
                                                if (!isAccessible) {
                                                    onLockedClicked(
                                                        "🔒 Sector Locked! Must complete previous sector first.",
                                                        "🔒 Sektör Kilitli! Öncelikle bir önceki sektörü tamamlamalısınız."
                                                    )
                                                } else {
                                                    activeModalNode = topNode
                                                }
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(Dimens.SpacingXxl))

                                    if (bottomNode != null) {
                                        val isSelected = selectedNodeIdx == bottomNode.index
                                        val isCurrent = player.currentFloor == selectedFloor && bottomNode.index == player.currentNodeIndex
                                        val isCleared = player.currentFloor > selectedFloor || (player.currentFloor == selectedFloor && (bottomNode.depth < currentDepth || (bottomNode.index == player.currentNodeIndex && player.currentNodeCompleted)))
                                        val isAccessible = player.currentFloor == selectedFloor && (
                                            (bottomNode.index == player.currentNodeIndex) ||
                                            (player.currentNodeCompleted && bottomNode.depth == currentDepth + 1)
                                        )
                                        val isScouted = scoutedNodeIndices.contains(bottomNode.index)
                                        val isTypeHidden = !isCurrent && !isCleared && !isAccessible && !isScouted

                                        NodeCircle(
                                            node = bottomNode,
                                            isCurrent = isCurrent,
                                            isCleared = isCleared,
                                            isAccessible = isAccessible,
                                            isTypeHidden = isTypeHidden,
                                            isSelected = isSelected,
                                            onClick = {
                                                selectedNodeIdx = bottomNode.index
                                                if (!isAccessible) {
                                                    onLockedClicked(
                                                        "🔒 Sector Locked! Must complete previous sector first.",
                                                        "🔒 Sektör Kilitli! Öncelikle bir önceki sektörü tamamlamalısınız."
                                                    )
                                                } else {
                                                    activeModalNode = bottomNode
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (d < 19) {
                            val pathCleared = player.currentFloor > selectedFloor || (player.currentFloor == selectedFloor && d < currentDepth)
                            val pathColor = if (pathCleared) ColorHeal else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                            val pathHeight = if (pathCleared) Dimens.BorderThick else Dimens.BorderThin
                            
                            Column(
                                modifier = Modifier.width(Dimens.SpacingXl),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.height(90.dp),
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

            Spacer(modifier = Modifier.height(Dimens.SpacingS))

            // Beautiful Compact Legend of diverse node types (Combat, Narrative, Treasure, Mystery)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.BorderThick),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem("⚔️", if (isTr) "Dövüş" else "Combat")
                LegendItem("📜", if (isTr) "Hikaye" else "Narrative")
                LegendItem("💎", if (isTr) "Hazine" else "Treasure")
                LegendItem("🔮", if (isTr) "Gizem" else "Mystery")
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingM))

            // Inspection Panel with cleaner shorter info
            val inspectBlueprint = com.mcanererdem.journey.data.engine.FloorBlueprintSystem.getBlueprintForFloor(selectedFloor, player)
            val inspectNode = inspectBlueprint.nodes.getOrNull(selectedNodeIdx)
            
            if (inspectNode != null) {
                val nodeName = if (isTr) inspectNode.titleTr else inspectNode.title
                val nodeDesc = if (isTr) inspectNode.descriptionTr else inspectNode.description
                
                val nodeCategoryEn = when (inspectNode.type) {
                    NodeType.COMBAT -> "Combat ⚔️"
                    NodeType.BOSS -> "Boss 💀"
                    NodeType.CHEST -> "Treasure 💎"
                    NodeType.SHRINE -> "Mystery 🔮"
                    NodeType.MERCHANT -> "Mystery 🔮"
                    NodeType.NARRATIVE -> "Narrative 📜"
                }

                val nodeCategoryTr = when (inspectNode.type) {
                    NodeType.COMBAT -> "Dövüş ⚔️"
                    NodeType.BOSS -> "Bölüm Sonu 💀"
                    NodeType.CHEST -> "Hazine 💎"
                    NodeType.SHRINE -> "Gizem 🔮"
                    NodeType.MERCHANT -> "Gizem 🔮"
                    NodeType.NARRATIVE -> "Hikaye 📜"
                }

                val inspectIsCurrent = player.currentFloor == selectedFloor && player.currentNodeIndex == selectedNodeIdx
                val inspectIsCleared = player.currentFloor > selectedFloor || 
                        (player.currentFloor == selectedFloor && (inspectNode.depth < currentDepth || (player.currentNodeIndex == selectedNodeIdx && player.currentNodeCompleted)))
                val inspectIsAccessible = selectedNodeIdx == 0 || inspectIsCurrent || (inspectNode.depth == currentDepth + 1 && player.currentNodeCompleted)

                val isSelectScouted = scoutedNodeIndices.contains(selectedNodeIdx)
                val isSelectTypeHidden = !inspectIsCurrent && !inspectIsCleared && !inspectIsAccessible && !isSelectScouted

                // Threat or danger tooltip
                val dangerLevelEn = when {
                    isSelectTypeHidden -> "❓ UNKNOWN SECTOR (Scout to analyze)"
                    inspectNode.type == NodeType.BOSS -> "☠️ APEX OVERLORD THREAT LEVEL"
                    inspectNode.type == NodeType.COMBAT -> "🔴 HOSTILE DETECTED (High Risk)"
                    inspectNode.type == NodeType.NARRATIVE -> "🟡 STABLE STORY BRANCH (Neutral)"
                    else -> "🟢 SECURE VAULT COMPARTMENT (Safe)"
                }
                val dangerLevelTr = when {
                    isSelectTypeHidden -> "❓ BİLİNMEYEN SEKTÖR (Gözlem yeteneği kullanın)"
                    inspectNode.type == NodeType.BOSS -> "☠️ ZİRVE CANAVARI TEHLİKE DÜZEYİ"
                    inspectNode.type == NodeType.COMBAT -> "🔴 AKTİF DÜŞMAN (Yüksek Risk)"
                    inspectNode.type == NodeType.NARRATIVE -> "🟡 KARAR SEKTÖRÜ (Normal Tehdit)"
                    else -> "🟢 SAKİN BÖLGE (Güvenli Alan)"
                }

                val costTextEn = when {
                    inspectIsCleared -> "⚡ Energy Cost: None (Cleared)"
                    inspectIsCurrent -> "⚡ Energy Cost: None (Occupied)"
                    inspectIsAccessible -> "⚡ Energy Cost: 1 Willpower (Movement Entry)"
                    else -> "🔒 Dynamic Cost: Requires Sequence Unlock"
                }
                val costTextTr = when {
                    inspectIsCleared -> "⚡ İrade Maliyeti: Yok (Temizlendi)"
                    inspectIsCurrent -> "⚡ İrade Maliyeti: Yok (Mevcut Sektör)"
                    inspectIsAccessible -> "⚡ İrade Maliyeti: 1 İrade (İlerlemek İçin)"
                    else -> "🔒 İrade Maliyeti: Sıradaki sektör olmalıdır"
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
                                    },
                                    fontSize = Dimens.TextM
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(Dimens.SpacingS))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isSelectTypeHidden) (if (isTr) "Bilinmeyen Sektör" else "Hidden Sector") else nodeName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSelectTypeHidden) (if (isTr) "Keşfedilmemiş" else "Unexplored") else "${if (isTr) nodeCategoryTr else nodeCategoryEn} • ${if (isTr) "Sektör ${selectedNodeIdx + 1}" else "Sector ${selectedNodeIdx + 1}"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.width(Dimens.SpacingS))

                            // Status Tag
                            val statusTagText = when {
                                inspectIsCurrent -> if (isTr) "📍 AKTİF" else "📍 ACTIVE"
                                inspectIsCleared -> if (isTr) "✓ GEÇİLDİ" else "✓ CLEARED"
                                else -> if (isTr) "🔒 KİLİTLİ" else "🔒 LOCKED"
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
                            text = if (isSelectTypeHidden) (if (isTr) "Bu sektör gelecekteki yolda yer alıyor. Türünü ve tehlikesini görmek için Gözlem (Scout) yeteneğini çalıştırın." else "This sector is hidden further along the path. Use the Scout skill to reveal its contents.") else nodeDesc,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = Dimens.TextXs),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(Dimens.SpacingS))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(Dimens.SpacingS))

                        // Brief dynamic hover details: Danger assessment and energy costs
                        Text(
                            text = if (isTr) "⚠️ Tehdit Seviyesi: $dangerLevelTr" else "⚠️ Threat Profile: $dangerLevelEn",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                            color = if (inspectNode.type == NodeType.BOSS || inspectNode.type == NodeType.COMBAT) ColorDanger else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isTr) costTextTr else costTextEn,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        // If combat or boss, show threat assessments
                        if (!isSelectTypeHidden && (inspectNode.type == NodeType.COMBAT || inspectNode.type == NodeType.BOSS)) {
                            val enemyName = if (isTr) inspectNode.enemyNameTr else inspectNode.enemyNameEn
                            Text(
                                text = if (isTr) {
                                    "✨ Canavar: $enemyName | HP: ${inspectNode.enemyHp} | Saldırı: +${inspectNode.enemyAtk}"
                                } else {
                                    "✨ Hostile: $enemyName | HP: ${inspectNode.enemyHp} | Atk: +${inspectNode.enemyAtk}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold),
                                color = if (inspectNode.type == NodeType.BOSS) ColorDanger else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }

    activeModalNode?.let { n ->
        val nodeIsCurrent = player.currentFloor == selectedFloor && player.currentNodeIndex == n.index
        val nodeIsCleared = player.currentFloor > selectedFloor || 
                (player.currentFloor == selectedFloor && (n.depth < currentDepth || (player.currentNodeIndex == n.index && player.currentNodeCompleted)))
        val nodeIsAccessible = n.index == player.currentNodeIndex || (player.currentNodeCompleted && n.depth == currentDepth + 1)
        
        NodeDetailModal(
            node = n,
            floor = selectedFloor,
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

@Composable
fun NodeDetailModal(
    node: AdventureNode,
    floor: Int,
    isCurrent: Boolean,
    isCleared: Boolean,
    isAccessible: Boolean,
    activeLang: String,
    journal: List<JournalEntry>,
    onDismiss: () -> Unit,
    onEnterNode: (Int, Int) -> Unit
) {
    val isTr = activeLang == "TR"
    val nodeName = if (isTr) node.titleTr else node.title
    val nodeDesc = if (isTr) node.descriptionTr else node.description

    val primaryColor = when (node.type) {
        NodeType.COMBAT, NodeType.BOSS -> ColorDanger
        NodeType.NARRATIVE -> MaterialTheme.colorScheme.primary
        NodeType.CHEST -> ColorSanctumPrimary
        NodeType.SHRINE, NodeType.MERCHANT -> ColorCovenantGlow
    }

    val typeIcon = when (node.type) {
        NodeType.COMBAT -> "⚔️"
        NodeType.BOSS -> "💀"
        NodeType.CHEST -> "💎"
        NodeType.SHRINE -> "🔮"
        NodeType.MERCHANT -> "🔮"
        NodeType.NARRATIVE -> "📜"
    }

    val typeLabelEn = when (node.type) {
        NodeType.COMBAT -> "Combat Sector"
        NodeType.BOSS -> "Apex Boss Sanctum"
        NodeType.CHEST -> "Treasure Hoard"
        NodeType.SHRINE -> "Shrine Altar"
        NodeType.MERCHANT -> "Wandering Merchant"
        NodeType.NARRATIVE -> "Narrative Sector"
    }
    val typeLabelTr = when (node.type) {
        NodeType.COMBAT -> "Muharebe Sektörü"
        NodeType.BOSS -> "Kozmik Zirve Mihrabı"
        NodeType.CHEST -> "Kayıp Hazine Sandığı"
        NodeType.SHRINE -> "Kutsal Sunak"
        NodeType.MERCHANT -> "Gezgin Tüccar"
        NodeType.NARRATIVE -> "Hikaye Karar Odası"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = typeIcon, fontSize = 20.sp, modifier = Modifier.padding(end = Dimens.SpacingS))
                Text(
                    text = if (isTr) typeLabelTr else typeLabelEn,
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
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = Dimens.TextXl),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Render decision history outcomes if this node was previously visited
                val matchEntry = journal.find { it.floor == floor && it.nodeIndex == node.index }
                if (matchEntry != null) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingS))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ColorHeal.copy(alpha = 0.1f)),
                        border = BorderStroke(Dimens.BorderThin, ColorHeal.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                            Text(
                                text = if (isTr) "GÜNLÜK KAYIT DETAYI:" else "JOURNAL EXPLORE RECORD:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = Dimens.TextXxs),
                                color = ColorHeal
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                            Text(
                                text = if (isTr) matchEntry.actionTakenTr else matchEntry.actionTakenEs,
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
                            text = if (isTr) "Keşif İrade Maliyeti: ${node.willCost} İrade" else "Exploration Will Cost: ${node.willCost} Will",
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
                        text = if (isTr) "Yola Çık ⚡" else "Explore ⚡",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        text = if (isTr) "Kapat" else "Dismiss",
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
                        text = if (isTr) "Kapat" else "Dismiss",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    )
}

@Composable
fun LegendItem(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = Dimens.TextXs, modifier = Modifier.padding(end = Dimens.BorderThick))
        Text(text = text, style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
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
                        text = if (activeLang == "TR") "🔮 BOYUTSAL GİZEM" else "🔮 SPATIAL MYSTERY",
                        modifier = Modifier.padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXs),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ColorSanctumPrimary
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            Text(
                text = if (activeLang == "TR") event.titleTr else event.titleEn,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            Text(
                text = if (activeLang == "TR") event.descriptionTr else event.descriptionEn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingXl))
            Text(
                text = if (activeLang == "TR") "KADERSEL SEÇİMİNİZ:" else "CHOOSE YOUR DESTINY:",
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
                            text = if (activeLang == "TR") option.textTr else option.textEn,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                        Text(
                            text = if (activeLang == "TR") "Olası Sonuç: ${option.outcomeTr}" else "Outcome: ${option.outcomeEn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacingS))
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
                            if (option.alignmentImpact != 0) {
                                val alignLabel = if (option.alignmentImpact > 0) "+${option.alignmentImpact} Sanctum" else "${option.alignmentImpact} Covenant"
                                val alignCol = if (option.alignmentImpact > 0) ColorSanctumPrimary else ColorCovenantGlow
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
                                        text = if (option.goldChange > 0) "+${option.goldChange} Gold" else "${option.goldChange} Gold",
                                        modifier = Modifier.padding(horizontal = Dimens.SpacingS, vertical = Dimens.BorderThick),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                            if (option.itemReward.isNotEmpty()) {
                                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(Dimens.SpacingXs)) {
                                    Text(
                                        text = "🎒 ${option.itemReward}",
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

@Composable
fun SecretBossCombatView(
    boss: SecretBossEncounter,
    player: PlayerProfile,
    bossHp: Int,
    combatLog: List<String>,
    onAction: (String) -> Unit,
    onEscape: () -> Unit,
    activeLang: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpacingL),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(Dimens.SpacingL),
        border = BorderStroke(Dimens.BorderThick, ColorDanger)
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingL)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (activeLang == "TR") boss.nameTr else boss.nameEn,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = ColorDanger
                    )
                    Text(
                        text = if (activeLang == "TR") "Kozmik Zirve Ejderhası" else "Celestial Overlord Dragon",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Surface(
                    color = ColorDanger.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(Dimens.SpacingXs)
                ) {
                    Text(
                        text = "BOSS",
                        modifier = Modifier.padding(Dimens.SpacingS, Dimens.SpacingXs),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ColorDanger
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            
            // HP Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { bossHp.toFloat() / boss.hp.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.SpacingM)
                        .clip(RoundedCornerShape(Dimens.SpacingS)),
                    color = ColorDanger,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(Dimens.SpacingM))
                Text(
                    text = "$bossHp/${boss.hp}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            
            // Dragon's Phase indicator
            val isPhase2 = bossHp < (boss.hp / 2)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isPhase2) ColorDanger.copy(alpha = 0.08f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        RoundedCornerShape(Dimens.SpacingS)
                    )
                    .padding(Dimens.SpacingS)
            ) {
                Text(
                    text = if (isPhase2) {
                        if (activeLang == "TR") "💥 FAZ 2: EJDERHA GAZABI (Çift Hasar!)" else "💥 PHASE 2: DRAGON FURY (Double Attack Dmg!)"
                    } else {
                        if (activeLang == "TR") "🛡️ FAZ 1: Kozmik Hüküm Zırhı Aktif" else "🛡️ PHASE 1: Cosmic Decrees Armor Active"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isPhase2) ColorDanger else MaterialTheme.colorScheme.primary
                )
            }

            // Combat Log
            if (combatLog.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingM))
                Text(
                    text = if (activeLang == "TR") "Savaş Console Logları:" else "Combat Console Log:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                        combatLog.takeLast(3).forEach { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = Dimens.TextXxs),
                                color = if (log.contains("Sovereign") || log.contains("Vanquished")) ColorSanctumPrimary 
                                        else if (log.contains("defeat") || log.contains("hasar aldı")) ColorDanger 
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingL))

            // Action grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
            ) {
                Button(
                    onClick = { onAction("ATTACK") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorDanger)
                ) {
                    Text(if (activeLang == "TR") "HÜCUM ET ⚔️" else "STRIKE ⚔️", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                
                Button(
                    onClick = { onAction("DEFEND") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(if (activeLang == "TR") "SAVUNMA 🛡️" else "PARRY 🛡️", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
            ) {
                Button(
                    onClick = { onAction("POTION") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorSanctumPrimary)
                ) {
                    Text(if (activeLang == "TR") "ŞİFA İKSİRİ 🧪" else "HEAL FLASK 🧪", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                
                OutlinedButton(
                    onClick = onEscape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (activeLang == "TR") "KAÇIŞ 🏃" else "ESCAPE 🏃", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
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
                },
                fontSize = Dimens.TextXs,
                color = if (isCleared || isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
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
