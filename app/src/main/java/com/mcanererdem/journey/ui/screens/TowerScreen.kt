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
            .padding(horizontal = Dimens.SpacingL),
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
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Dimens.SpacingM),
                            shape = RoundedCornerShape(Dimens.SpacingM),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                width = if (activeNode.type == NodeType.BOSS) Dimens.BorderThick else Dimens.BorderThin,
                                color = if (activeNode.type == NodeType.BOSS) ColorSanctumPrimary else ColorDanger.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (activeLang == "TR") activeNode.enemyNameTr else activeNode.enemyNameEn,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif
                                        ),
                                        color = if (activeNode.type == NodeType.BOSS) ColorSanctumPrimary else ColorDanger
                                    )
                                    
                                    // Enemy Intent Badge
                                    val intentBadgeText = when (currentEnemyIntent) {
                                        EnemyIntent.ATTACK -> if (activeLang == "TR") "SaldÄ±rÄ± âš”ï¸" else "Attack âš”ï¸"
                                        EnemyIntent.DEFEND -> if (activeLang == "TR") "Savunma ğŸ›¡ï¸" else "Defend ğŸ›¡ï¸"
                                        EnemyIntent.DEBUFF -> if (activeLang == "TR") "Zehirleme ğŸ§ª" else "Debuff ğŸ§ª"
                                        EnemyIntent.BUFF -> if (activeLang == "TR") "Kutsama âœ¨" else "Buff âœ¨"
                                    }
                                    val intentBadgeColor = when (currentEnemyIntent) {
                                        EnemyIntent.ATTACK -> ColorDanger
                                        EnemyIntent.DEFEND -> ColorSanctumPrimary
                                        EnemyIntent.DEBUFF -> ColorWarning
                                        EnemyIntent.BUFF -> ColorStatGold
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(intentBadgeColor.copy(alpha = 0.15f), RoundedCornerShape(Dimens.SpacingXs))
                                            .border(Dimens.BorderNormal, intentBadgeColor, RoundedCornerShape(Dimens.SpacingXs))
                                            .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXs)
                                    ) {
                                        Text(
                                            text = intentBadgeText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = intentBadgeColor
                                        )
                                    }
                                    
                                    if (activeNode.type == NodeType.BOSS) {
                                        Surface(
                                            color = ColorSanctumPrimary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(Dimens.SpacingXs)
                                        ) {
                                            Text(
                                                text = "BOSS",
                                                modifier = Modifier.padding(Dimens.SpacingS, Dimens.BorderThick),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = ColorSanctumPrimary
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(Dimens.SpacingM))
                                
                                val mobHp = activeEnemyHp ?: activeNode.enemyHp
                                // Enemy Status Effects
                                StatusEffectsRow(statuses = enemyStatuses, activeLang = activeLang)
                                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LinearProgressIndicator(
                                        progress = { mobHp.toFloat() / activeNode.enemyHp.toFloat() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(Dimens.SpacingM)
                                            .clip(RoundedCornerShape(Dimens.SpacingXs)),
                                        color = ColorDanger,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.SpacingM))
                                    Text(
                                        text = "$mobHp/${activeNode.enemyHp}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    )
                                }

                                // Advanced turn-based stats and alignment-influenced tactical modifiers panel
                                val enemyFaction = com.mcanererdem.journey.data.model.EnemyFaction.fromName(activeNode.enemyNameEn)
                                val isSanctum = player.side == "SANCTUM" || player.momentum > 70
                                val isCovenant = player.side == "COVENANT" || player.momentum < 30
                                val critChance = (10 + player.currentWill * 4).coerceIn(10, 50)
                                val hpPercentage = player.currentHp.toFloat() / player.maxHp.toFloat()

                                val textText = when (enemyFaction) {
                                    com.mcanererdem.journey.data.model.EnemyFaction.SANCTUM_WRATH -> if (activeLang == "TR") "KUTSAL ORDU" else "HOLY WRATH"
                                    com.mcanererdem.journey.data.model.EnemyFaction.VOID_CORRUPTION -> if (activeLang == "TR") "ABIS MUSİBETİ" else "VOID CORRUPTION"
                                    com.mcanererdem.journey.data.model.EnemyFaction.BLIGHTED_AMALGAM -> if (activeLang == "TR") "KADİM MUSİBET" else "CORRUPTED BLIGHT"
                                }
                                val badgeColor = when (enemyFaction) {
                                    com.mcanererdem.journey.data.model.EnemyFaction.SANCTUM_WRATH -> ColorSanctumPrimary
                                    com.mcanererdem.journey.data.model.EnemyFaction.VOID_CORRUPTION -> ColorCovenantGlow
                                    com.mcanererdem.journey.data.model.EnemyFaction.BLIGHTED_AMALGAM -> ColorDanger
                                }
                                val descText = when (enemyFaction) {
                                    com.mcanererdem.journey.data.model.EnemyFaction.SANCTUM_WRATH -> if (activeLang == "TR") "Karanlık/Boşluk saldırılarına karşı zayıf, Kutsal inançlılara dirençli." else "Weak to Void alignment, resistant to Sanctum holy radiance."
                                    com.mcanererdem.journey.data.model.EnemyFaction.VOID_CORRUPTION -> if (activeLang == "TR") "Kutsal Işıltıya karşı son derece zayıf, Abis saldırılarına dirençli." else "Weak to Sanctum holy radiance, resistant to Void corruption."
                                    com.mcanererdem.journey.data.model.EnemyFaction.BLIGHTED_AMALGAM -> if (activeLang == "TR") "Nötr ve dengeli savaşçılara karşı zayıftır." else "Weak to Neutral alignment and steady balanced strikes."
                                }

                                Spacer(modifier = Modifier.height(Dimens.SpacingM))

                                // Faction badge and Weakness details flow
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(Dimens.SpacingXs))
                                            .border(Dimens.BorderThin, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(Dimens.SpacingXs))
                                            .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXs)
                                    ) {
                                        Text(
                                            text = textText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                                            color = badgeColor
                                        )
                                    }

                                    Text(
                                        text = descText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Normal),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(Dimens.SpacingM))

                                // Detailed tactical modifiers description card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                                    ),
                                    border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(Dimens.SpacingS)
                                ) {
                                    Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                                        Text(
                                            text = if (activeLang == "TR") "⚔️ SAVAŞ ETKİNLİĞİ VE DURUM ETKİLERİ" else "⚔️ COMBAT EFFECTIVENESS & MODIFIERS",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(modifier = Modifier.height(Dimens.SpacingS))

                                        // Row 1: Faction effectiveness
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.BorderThick),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (activeLang == "TR") "🛡️ İnanç-Düşman Uyumu:" else "🛡️ Faction Matchup:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )

                                            val (matchText, matchColor) = when {
                                                isSanctum && enemyFaction == com.mcanererdem.journey.data.model.EnemyFaction.VOID_CORRUPTION -> {
                                                     val bonus = 20 + ((player.momentum - 50) / 4)
                                                    Pair(if (activeLang == "TR") "✨ Avantaj: +$bonus% (Işık)" else "✨ Advantage: +$bonus% (Light)", ColorSanctumPrimary)
                                                }
                                                isCovenant && enemyFaction == com.mcanererdem.journey.data.model.EnemyFaction.SANCTUM_WRATH -> {
                                                    val bonus = 20 + (Math.abs(player.momentum - 50) / 4)
                                                    Pair(if (activeLang == "TR") "🔮 Avantaj: +$bonus% (Boşluk)" else "🔮 Advantage: +$bonus% (Void)", ColorCovenantGlow)
                                                }
                                                isSanctum && enemyFaction == com.mcanererdem.journey.data.model.EnemyFaction.SANCTUM_WRATH -> {
                                                    Pair(if (activeLang == "TR") "⚠️ Direnç: -15% Azalmış Hasar" else "⚠️ Resisted: -15% Holy Kinship", ColorDanger)
                                                }
                                                isCovenant && enemyFaction == com.mcanererdem.journey.data.model.EnemyFaction.VOID_CORRUPTION -> {
                                                    Pair(if (activeLang == "TR") "⚠️ Direnç: -15% Azalmış Hasar" else "⚠️ Resisted: -15% Shadow Kinship", ColorDanger)
                                                }
                                                (!isSanctum && !isCovenant) && enemyFaction == com.mcanererdem.journey.data.model.EnemyFaction.BLIGHTED_AMALGAM -> {
                                                    Pair(if (activeLang == "TR") "⚖️ Nötr Odak: +20% Hasar" else "⚖️ Neutral Focus: +20% Dmg", ColorSanctumPrimary)
                                                }
                                                else -> {
                                                    Pair(if (activeLang == "TR") "⚖️ Dengeli: Standart" else "⚖️ Balanced: Standard", MaterialTheme.colorScheme.onSurface)
                                                }
                                            }

                                            Text(
                                                text = matchText,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = matchColor
                                            )
                                        }

                                        // Row 2: Willpower critical rate
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.BorderThick),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (activeLang == "TR") "🧠 Konsantrasyon (İrade):" else "🧠 Focus & Critical Chance:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )

                                            val critText = if (activeLang == "TR") "%$critChance Kritik Şans (x1.5 Hasar)" else "$critChance% Crit Rate (x1.5 Dmg)"
                                            Text(
                                                text = "🔥 $critText",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (player.currentWill >= 7) ColorSanctumPrimary else if (player.currentWill == 0) ColorDanger else MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        // Row 3: Mind state
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.BorderThick),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (activeLang == "TR") "⚡ Zihin Durumu:" else "⚡ Cognitive State:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )

                                            val (stateText, stateColor) = when {
                                                player.currentWill == 0 -> Pair(if (activeLang == "TR") "Zihinsel Sürsaj (-%25 Hasar)" else "Mental Fatigue (-25% Dmg)", ColorDanger)
                                                player.currentWill >= 7 -> Pair(if (activeLang == "TR") "Berrak Zihin (+%15 Hasar)" else "Inspired State (+15% Dmg)", ColorSanctumPrimary)
                                                else -> Pair(if (activeLang == "TR") "Odaklanmış" else "Standard Focus", MaterialTheme.colorScheme.onSurface)
                                            }

                                            Text(
                                                text = stateText,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = stateColor
                                            )
                                        }

                                        // Row 4: Low HP adren surge
                                        if (hpPercentage < 0.3f && hpPercentage > 0.0f) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.BorderThick),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (activeLang == "TR") "🩸 Cam Havli (Öfke):" else "🩸 Low HP Survival Fury:",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ColorDanger
                                                )

                                                Text(
                                                    text = if (activeLang == "TR") "+%30 Hasar / %25 Karşı Saldırı" else "+30% Damage / +25% Recoil",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = ColorDanger
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(Dimens.SpacingM))

                                // Player Status Effects
                                Text(
                                    text = if (activeLang == "TR") "Aktif Durum Etkileriniz:" else "Your Active Status Effects:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                StatusEffectsRow(statuses = playerStatuses, activeLang = activeLang)
                                
                                Spacer(modifier = Modifier.height(Dimens.SpacingM))

                                // Turn action buttons grid
                                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                                    ) {
                                        Button(
                                            onClick = { onCombatAction("LIGHT_STRIKE") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(Dimens.AvatarSize)
                                                .testTag("btn_combat_attack"),
                                            colors = ButtonDefaults.buttonColors(containerColor = ColorDanger)
                                        ) {
                                            Text(
                                                text = if (activeLang == "TR") "HAMLE YAP ⚔️" else "STRIKE ⚔️",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Button(
                                            onClick = { onCombatAction("HEAVY_BLOW") },
                                            enabled = player.aether >= 15,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(Dimens.AvatarSize)
                                                .testTag("btn_combat_heavy_blow"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ColorWarning,
                                                disabledContainerColor = ColorWarning.copy(alpha = 0.3f),
                                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Text(
                                                text = if (activeLang == "TR") "AĞIR DARBE 🔥 (15)" else "HEAVY BLOW 🔥 (15)",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = if (player.aether >= 15) Color.Black else Color.White.copy(alpha = 0.5f))
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                                    ) {
                                        Button(
                                            onClick = { onCombatAction("BARRIER") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(Dimens.AvatarSize)
                                                .testTag("btn_combat_barrier"),
                                            colors = ButtonDefaults.buttonColors(containerColor = ColorHeal)
                                        ) {
                                            Text(
                                                text = if (activeLang == "TR") "BARİYER 🛡️" else "BARRIER 🛡️",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Button(
                                            onClick = { onCombatAction("ESCAPE") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(Dimens.AvatarSize)
                                                .testTag("btn_combat_flee"),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                        ) {
                                            Text(
                                                text = if (activeLang == "TR") "KAÇIŞ 🏃" else "ESCAPE 🏃",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }

                                // Combat logs console
                                if (combatLog.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                                    Text(
                                        text = if (activeLang == "TR") "Savaş Günlüğü:" else "Combat Console Log:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    ) {
                                        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                                            combatLog.takeLast(4).forEach { log ->
                                                Text(
                                                    text = log,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = Dimens.TextXxs),
                                                    color = if (log.contains("Critical") || log.contains("Kritik") || log.contains("Sovereign")) ColorSanctumPrimary 
                                                            else if (log.contains("defeat") || log.contains("hasar aldı") || log.contains("hit")) ColorDanger 
                                                            else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
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
                        if (hasFlag) {
                            item {
                                NodeChoiceButton(
                                    choice = choice,
                                    activeLang = activeLang,
                                    highlightColor = ColorSanctumPrimary,
                                    testTagValue = "choice_a_btn",
                                    onClick = { onChoiceSelected(choice) }
                                )
                            }
                        }
                    }

                    activeNode.optionB?.let { choice ->
                        val hasFlag = choice.requiredStoryFlag.isEmpty() || player.storyFlagsEncoded.split(",").contains(choice.requiredStoryFlag)
                        if (hasFlag) {
                            item {
                                NodeChoiceButton(
                                    choice = choice,
                                    activeLang = activeLang,
                                    highlightColor = ColorCovenantGlow,
                                    testTagValue = "choice_b_btn",
                                    onClick = { onChoiceSelected(choice) }
                                )
                            }
                        }
                    }

                    activeNode.optionC?.let { choice ->
                        val hasFlag = choice.requiredStoryFlag.isEmpty() || player.storyFlagsEncoded.split(",").contains(choice.requiredStoryFlag)
                        if (hasFlag) {
                            item {
                                NodeChoiceButton(
                                    choice = choice,
                                    activeLang = activeLang,
                                    highlightColor = ColorNeutralPrimary,
                                    testTagValue = "choice_c_btn",
                                    onClick = { onChoiceSelected(choice) }
                                )
                            }
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpacingXs)
            .clickable { onClick() }
            .testTag(testTagValue),
        shape = RoundedCornerShape(Dimens.SpacingM),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.BorderNormal, highlightColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
            Text(
                text = if (activeLang == "TR") choice.textTr else choice.textEn,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingS))

            // Micro rewards list indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alignment shift indicators
                if (choice.alignmentShift != 0) {
                    val shiftLabel = if (choice.alignmentShift > 0) "+Momentum ✨" else "-Momentum 🔥"
                    Text(
                        text = shiftLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (choice.alignmentShift > 0) ColorSanctumPrimary else ColorCovenantGlow,
                        modifier = Modifier.padding(end = Dimens.SpacingM)
                    )
                }

                if (choice.goldChange != 0) {
                    val goldLabel = if (choice.goldChange > 0) "+${choice.goldChange}🪙" else "${choice.goldChange}🪙"
                    Text(
                        text = goldLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorWarning,
                        modifier = Modifier.padding(end = Dimens.SpacingM)
                    )
                }

                if (choice.aetherChange != 0) {
                    val aetherLabel = if (choice.aetherChange > 0) "+${choice.aetherChange}✨" else "${choice.aetherChange}✨"
                    Text(
                        text = aetherLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorStatGold,
                        modifier = Modifier.padding(end = Dimens.SpacingM)
                    )
                }

                if (choice.willChange != 0) {
                    val willLabel = if (choice.willChange > 0) "+${choice.willChange}⚡" else "${choice.willChange}⚡"
                    Text(
                        text = willLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ColorSanctumPrimary,
                        modifier = Modifier.padding(end = Dimens.SpacingM)
                    )
                }

                if (choice.hpChange != 0) {
                    val hpLabel = if (choice.hpChange > 0) "+${choice.hpChange} HP" else "${choice.hpChange} HP"
                    Text(
                        text = hpLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (choice.hpChange > 0) ColorHeal else ColorDanger,
                        modifier = Modifier.padding(end = Dimens.SpacingM)
                    )
                }

                if (choice.rewardItem.isNotEmpty()) {
                    Text(
                        text = "🎁 Eşya",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorHeal
                    )
                }
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
            .padding(bottom = Dimens.SpacingM),
        shape = RoundedCornerShape(Dimens.SpacingM),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
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
                    .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingM)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(vertical = Dimens.SpacingM),
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
                color = circleBorderColor,
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
