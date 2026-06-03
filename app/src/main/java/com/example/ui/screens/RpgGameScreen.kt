package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.engine.FloorScenario
import com.example.data.engine.GameOption
import com.example.data.engine.LocalizationManager
import com.example.data.engine.NodeType
import com.example.data.engine.AdventureNode
import com.example.data.engine.NodeChoice
import androidx.compose.ui.text.font.FontStyle
import com.example.data.model.JournalEntry
import com.example.data.model.PlayerProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RpgGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val player by viewModel.playerProfile.collectAsStateWithLifecycle()
    val journal by viewModel.journalEntries.collectAsStateWithLifecycle()
    val activeLang by viewModel.activeLanguage.collectAsStateWithLifecycle()
    val scenario by viewModel.currentScenario.collectAsStateWithLifecycle()
    val tab by viewModel.currentTab.collectAsStateWithLifecycle()

    val currentFloorNodes by viewModel.currentFloorNodes.collectAsStateWithLifecycle()
    val activeEnemyHp by viewModel.activeEnemyHp.collectAsStateWithLifecycle()
    val combatLog by viewModel.combatLog.collectAsStateWithLifecycle()

    val actionMessageEn by viewModel.lastActionMessageEn.collectAsStateWithLifecycle()
    val actionMessageTr by viewModel.lastActionMessageTr.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val currentSide = player?.side ?: "NEUTRAL"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LIGHT & DARKNESS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Text-Based Tower RPG",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    // Language Switcher Toggle
                    OutlinedButton(
                        onClick = {
                            viewModel.changeLanguage(if (activeLang == "TR") "EN" else "TR")
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("lang_toggle"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (activeLang == "TR") "EN 🌐" else "TR 🌐",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Reset Button
                    IconButton(
                        onClick = { viewModel.resetGame() },
                        modifier = Modifier.testTag("reset_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart RPG",
                            tint = BlightDamageColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = tab == "TOWER",
                    onClick = { viewModel.selectTab("TOWER") },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    label = { Text(LocalizationManager.getString(activeLang, "tab_tower")) },
                    modifier = Modifier.testTag("nav_tower")
                )
                NavigationBarItem(
                    selected = tab == "OUTER_WORLD",
                    onClick = { viewModel.selectTab("OUTER_WORLD") },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text(LocalizationManager.getString(activeLang, "tab_outer")) },
                    modifier = Modifier.testTag("nav_outer")
                )
                NavigationBarItem(
                    selected = tab == "CHAR_SHEET",
                    onClick = { viewModel.selectTab("CHAR_SHEET") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(LocalizationManager.getString(activeLang, "tab_character")) },
                    modifier = Modifier.testTag("nav_char")
                )
                NavigationBarItem(
                    selected = tab == "JOURNAL",
                    onClick = { viewModel.selectTab("JOURNAL") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text(LocalizationManager.getString(activeLang, "tab_journal")) },
                    modifier = Modifier.testTag("nav_journal")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header stats block
            player?.let { p ->
                HeaderStatsBlock(player = p, activeLang = activeLang)
            }

            // Flash action logger alert banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Notification",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (activeLang == "TR") actionMessageTr else actionMessageEn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Central Game content switching
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (tab) {
                    "TOWER" -> TowerClimbTab(
                        player = player,
                        nodes = currentFloorNodes,
                        activeEnemyHp = activeEnemyHp,
                        combatLog = combatLog,
                        activeLang = activeLang,
                        onChoiceSelected = { viewModel.selectNodeChoice(it) },
                        onNextNodeClick = { viewModel.advanceToNextNode() },
                        onAscendFloorClick = { viewModel.ascendToNextFloor() },
                        onCombatAction = { viewModel.executeCombatTurn(it) },
                        onResetClick = { viewModel.resetGame() }
                    )
                    "OUTER_WORLD" -> OuterWorldTab(
                        player = player,
                        activeLang = activeLang,
                        onHeal = { viewModel.healAndRest(it) },
                        onScout = { viewModel.performAbyssScouting() },
                        onTrade = { viewModel.tradeCurrency(it) }
                    )
                    "CHAR_SHEET" -> CharacterSheetTab(
                        player = player,
                        activeLang = activeLang,
                        onFactionSelect = { viewModel.selectFaction(it) },
                        onRenounce = { viewModel.renounceAllegiance() },
                        onNameUpdate = { viewModel.setPlayerName(it) }
                    )
                    "JOURNAL" -> JournalTab(
                        journal = journal,
                        activeLang = activeLang,
                        onClear = { viewModel.resetGame() }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderStatsBlock(
    player: PlayerProfile,
    activeLang: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 10.dp, 16.dp, 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Profile top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = player.playerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = player.chosenClass,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${LocalizationManager.getString(activeLang, "label_level")} ${player.level}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${player.exp}/${player.maxExp} XP]",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Surface(
                    color = when (player.side) {
                        "SANCTUM" -> SanctumGold.copy(alpha = 0.15f)
                        "COVENANT" -> VoidNeonPurple.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(
                        1.dp,
                        when (player.side) {
                            "SANCTUM" -> SanctumGold
                            "COVENANT" -> VoidNeonPurple
                            else -> MaterialTheme.colorScheme.primary
                        },
                        RoundedCornerShape(16.dp)
                    )
                ) {
                    Text(
                        text = when (player.side) {
                            "SANCTUM" -> if (activeLang == "TR") "Semavi" else "Sanctum"
                            "COVENANT" -> if (activeLang == "TR") "Kara Ahit" else "Covenant"
                            else -> if (activeLang == "TR") "Sürgün" else "Neutral"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (player.side) {
                            "SANCTUM" -> SanctumGold
                            "COVENANT" -> VoidNeonPurple
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Vitality Progress Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "♥ HP",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(42.dp),
                    color = BlightDamageColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = { player.currentHp.toFloat() / player.maxHp.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (player.currentHp < 30) BlightDamageColor else SpiritHealColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${player.currentHp}/${player.maxHp}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Willpower Progress Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚡ WILL",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(42.dp),
                    color = SanctumGold
                )
                Spacer(modifier = Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = { player.currentWill.toFloat() / player.maxWill.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SanctumGold,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${player.currentWill}/${player.maxWill}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resources values Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Gold
                ResourceChip(
                    icon = "🪙",
                    value = "${player.gold}",
                    label = if (activeLang == "TR") "Altın" else "Gold"
                )
                // Gleam
                ResourceChip(
                    icon = "✨",
                    value = "${player.gleam}",
                    label = if (activeLang == "TR") "Işıltı" else "Gleam",
                    accentColor = GleamGold
                )
                // Pyre
                ResourceChip(
                    icon = "🔥",
                    value = "${player.pyre}",
                    label = if (activeLang == "TR") "Yarık" else "Pyre",
                    accentColor = PyrePurple
                )
            }
        }
    }
}

@Composable
fun ResourceChip(
    icon: String,
    value: String,
    label: String,
    accentColor: Color = Color.Unspecified
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (accentColor != Color.Unspecified) accentColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
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
    onChoiceSelected: (NodeChoice) -> Unit,
    onNextNodeClick: () -> Unit,
    onAscendFloorClick: () -> Unit,
    onCombatAction: (String) -> Unit,
    onResetClick: () -> Unit
) {
    if (player == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floor indicator header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = LocalizationManager.formatString(activeLang, "label_floor_title", player.currentFloor),
                    modifier = Modifier.padding(16.dp, 8.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Checkpoint indicator
        item {
            Text(
                text = LocalizationManager.formatString(activeLang, "label_checkpoint", player.savedFloorCheckpoint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Visually Crawling Map Node Progress Row
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "label_nodes"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
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
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) MaterialTheme.colorScheme.primaryContainer
                                            else if (isCompleted) SpiritHealColor.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = if (isActive) 2.dp else 1.dp,
                                            color = if (isActive) MaterialTheme.colorScheme.primary
                                            else if (isCompleted) SpiritHealColor
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
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
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
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(3.dp, SanctumGold)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👑 SOVEREIGN CONQUEROR 👑",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = SanctumGold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You have climbed all 100 floors of the Cosmic Spires! The universe is forever saved and your name is written in stars.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onResetClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SanctumGold),
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
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, SpiritHealColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(18.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (activeLang == "TR") "Sektörü Tamamladınız! 🎉" else "Sector Cleared! 🎉",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SpiritHealColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
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
                                Spacer(modifier = Modifier.height(18.dp))

                                if (activeNode.type == NodeType.BOSS) {
                                    // Ascend to the next floor
                                    Button(
                                        onClick = onAscendFloorClick,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("btn_ascend_floor"),
                                        colors = ButtonDefaults.buttonColors(containerColor = SpiritHealColor)
                                    ) {
                                        Text(
                                            text = LocalizationManager.getString(activeLang, "btn_ascend_floor"),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                } else {
                                    // Move to next node
                                    Button(
                                        onClick = onNextNodeClick,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("btn_next_node"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(
                                            text = LocalizationManager.getString(activeLang, "btn_next_node"),
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
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                width = if (activeNode.type == NodeType.BOSS) 2.dp else 1.dp,
                                color = if (activeNode.type == NodeType.BOSS) SanctumGold else BlightDamageColor.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                        color = if (activeNode.type == NodeType.BOSS) SanctumGold else BlightDamageColor
                                    )
                                    
                                    if (activeNode.type == NodeType.BOSS) {
                                        Surface(
                                            color = SanctumGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "BOSS",
                                                modifier = Modifier.padding(6.dp, 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = SanctumGold
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                val mobHp = activeEnemyHp ?: activeNode.enemyHp
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LinearProgressIndicator(
                                        progress = { mobHp.toFloat() / activeNode.enemyHp.toFloat() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp)),
                                        color = BlightDamageColor,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "$mobHp/${activeNode.enemyHp}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }
                        }
                    }

                    // Turn-Based input options
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                            // Option 1: Basic Strike
                            Button(
                                onClick = { onCombatAction("STRIKE") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .height(48.dp)
                                    .testTag("combat_strike_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
                            ) {
                                Text(
                                    text = LocalizationManager.getString(activeLang, "btn_strike"),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            
                            // Option 2: Unique reflective alignment skills (HP cost Fatigue)
                            Button(
                                onClick = { onCombatAction("SKILL") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .height(48.dp)
                                    .testTag("combat_skill_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = LocalizationManager.getString(activeLang, "btn_skill"),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // Option 3: Healing potion (rests HP back, drains coins)
                            OutlinedButton(
                                onClick = { onCombatAction("POTION") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .height(48.dp)
                                    .testTag("combat_potion_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpiritHealColor),
                                border = BorderStroke(1.2.dp, SpiritHealColor)
                            ) {
                                Text(
                                    text = LocalizationManager.getString(activeLang, "btn_potion"),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // Scrolling Battle Logs
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp, max = 220.dp)
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = LocalizationManager.getString(activeLang, "label_combat_logs"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(combatLog.reversed()) { log ->
                                        Text(
                                            text = log,
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Story choice node
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = if (activeLang == "TR") activeNode.titleTr else activeNode.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = if (activeLang == "TR") activeNode.descriptionTr else activeNode.description,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = 24.sp,
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
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Choices
                    activeNode.optionA?.let { choice ->
                        item {
                            NodeChoiceButton(
                                choice = choice,
                                activeLang = activeLang,
                                highlightColor = SanctumGold,
                                testTagValue = "choice_a_btn",
                                onClick = { onChoiceSelected(choice) }
                            )
                        }
                    }

                    activeNode.optionB?.let { choice ->
                        item {
                            NodeChoiceButton(
                                choice = choice,
                                activeLang = activeLang,
                                highlightColor = VoidNeonPurple,
                                testTagValue = "choice_b_btn",
                                onClick = { onChoiceSelected(choice) }
                            )
                        }
                    }

                    activeNode.optionC?.let { choice ->
                        item {
                            NodeChoiceButton(
                                choice = choice,
                                activeLang = activeLang,
                                highlightColor = SlateBronze,
                                testTagValue = "choice_c_btn",
                                onClick = { onChoiceSelected(choice) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
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
            .padding(vertical = 5.dp)
            .clickable { onClick() }
            .testTag(testTagValue),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, highlightColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (activeLang == "TR") choice.textTr else choice.textEn,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Micro rewards list indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alignment shift indicators
                if (choice.alignmentShift != 0) {
                    val shiftLabel = if (choice.alignmentShift > 0) "+Align ✨" else "-Align 🔥"
                    Text(
                        text = shiftLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (choice.alignmentShift > 0) SanctumGold else VoidNeonPurple,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (choice.goldChange != 0) {
                    val goldLabel = if (choice.goldChange > 0) "+${choice.goldChange}🪙" else "${choice.goldChange}🪙"
                    Text(
                        text = goldLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldYellow,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (choice.gleamChange != 0) {
                    Text(
                        text = "+${choice.gleamChange}✨",
                        style = MaterialTheme.typography.labelSmall,
                        color = GleamGold,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (choice.pyreChange != 0) {
                    Text(
                        text = "+${choice.pyreChange}🔥",
                        style = MaterialTheme.typography.labelSmall,
                        color = PyrePurple,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (choice.willChange != 0) {
                    val willLabel = if (choice.willChange > 0) "+${choice.willChange}⚡" else "${choice.willChange}⚡"
                    Text(
                        text = willLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SanctumGold,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (choice.hpChange != 0) {
                    val hpLabel = if (choice.hpChange > 0) "+${choice.hpChange} HP" else "${choice.hpChange} HP"
                    Text(
                        text = hpLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (choice.hpChange > 0) SpiritHealColor else BlightDamageColor,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (choice.rewardItem.isNotEmpty()) {
                    Text(
                        text = "🎁 Eşya",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpiritHealColor
                    )
                }
            }
        }
    }
}

@Composable
fun OuterWorldTab(
    player: PlayerProfile?,
    activeLang: String,
    onHeal: (Int) -> Unit,
    onScout: () -> Unit,
    onTrade: (String) -> Unit
) {
    if (player == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = LocalizationManager.getString(activeLang, "outer_haven_title"),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = LocalizationManager.getString(activeLang, "outer_haven_desc"),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
        }

        // Action 1: Rest at hot springs (Heal)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🏺", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LocalizationManager.getString(activeLang, "rest_title"),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = LocalizationManager.getString(activeLang, "rest_desc"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Button(
                        onClick = { onHeal(50) },
                        colors = ButtonDefaults.buttonColors(containerColor = SpiritHealColor),
                        enabled = player.gold >= 50 && player.currentHp < player.maxHp,
                        modifier = Modifier.testTag("heal_button")
                    ) {
                        Text(LocalizationManager.getString(activeLang, "rest_btn"))
                    }
                }
            }
        }

        // Action 2: Scout the Blighted fringes (A repeatable gold adventure)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚔️", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LocalizationManager.getString(activeLang, "scout_title"),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = LocalizationManager.getString(activeLang, "scout_desc"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Button(
                        onClick = onScout,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateBronze),
                        modifier = Modifier.testTag("scout_button")
                    ) {
                        Text(LocalizationManager.getString(activeLang, "scout_btn"))
                    }
                }
            }
        }

        // Faction currency Exchange board
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = LocalizationManager.getString(activeLang, "exchange_title"),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Buy Gleam
        item {
            ExchangeCard(
                title = LocalizationManager.getString(activeLang, "buy_gleam_title"),
                description = LocalizationManager.getString(activeLang, "buy_gleam_desc"),
                goldCost = 50,
                currentGold = player.gold,
                badgeColor = SanctumGold,
                onClick = { onTrade("GOLD_TO_GLEAM") },
                testTagValue = "buy_gleam_btn"
            )
        }

        // Buy Pyre
        item {
            ExchangeCard(
                title = LocalizationManager.getString(activeLang, "buy_pyre_title"),
                description = LocalizationManager.getString(activeLang, "buy_pyre_desc"),
                goldCost = 50,
                currentGold = player.gold,
                badgeColor = VoidNeonPurple,
                onClick = { onTrade("GOLD_TO_PYRE") },
                testTagValue = "buy_pyre_btn"
            )
        }
    }
}

@Composable
fun ExchangeCard(
    title: String,
    description: String,
    goldCost: Int,
    currentGold: Int,
    badgeColor: Color,
    onClick: () -> Unit,
    testTagValue: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(badgeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            OutlinedButton(
                onClick = onClick,
                enabled = currentGold >= goldCost,
                modifier = Modifier.testTag(testTagValue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = badgeColor)
            ) {
                Text(text = "Trade")
            }
        }
    }
}

@Composable
fun CharacterSheetTab(
    player: PlayerProfile?,
    activeLang: String,
    onFactionSelect: (String) -> Unit,
    onRenounce: () -> Unit,
    onNameUpdate: (String) -> Unit
) {
    if (player == null) return

    var nameInput by remember(player.playerName) { mutableStateOf(player.playerName) }

    val itemsList = remember(player.itemsEncoded) {
        if (player.itemsEncoded.isEmpty()) emptyList() else player.itemsEncoded.split(",").filter { it.isNotBlank() }
    }
    val titlesList = remember(player.titlesEncoded) {
        if (player.titlesEncoded.isEmpty()) emptyList() else player.titlesEncoded.split(",").filter { it.isNotBlank() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Alignment Scale Widget
        item {
            Text(
                text = if (activeLang == "TR") "HİZALANMA DURUMU" else "FACTION ALIGNMENT SPECTRUM",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Spec graphic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                VoidNeonPurple,
                                SlateSecondary,
                                SanctumGold
                            )
                        )
                    )
            ) {
                // Marker pointer
                val percentFloat = (player.alignment + 100).toFloat() / 200f
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percentFloat)
                        .background(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(Color.White)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🔥 Abyss (-100)", style = MaterialTheme.typography.labelSmall, color = VoidNeonPurple)
                Text("Neutral (${player.alignment})", style = MaterialTheme.typography.labelSmall)
                Text("Sanctum (+100) ✨", style = MaterialTheme.typography.labelSmall, color = SanctumGold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Editable name card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (activeLang == "TR") "KAHRAMAN UNVANI" else "SOVEREIGN TITLE DESIGNATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("char_name_input"),
                        singleLine = true,
                        placeholder = { Text("E.g. Lord Aldous") }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onNameUpdate(nameInput) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_name_btn")
                    ) {
                        Text(if (activeLang == "TR") "Güncelle" else "Save Name")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Faction swearing mechanics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (activeLang == "TR") "YEMİNLİ BAĞLILIK ANDI" else "SWEAR AN OATH OF POWER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = if (activeLang == "TR") {
                            "Bir cepheye katılmak, moral değerlerine bağlı olarak özel Yansıma Sınıfınızın kilidini açar."
                        } else {
                            "Joining a faction unlocks specialized reflection classes (e.g. Holy Aegis, Death Herald) dependent on your core Alignment."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { onFactionSelect("SANCTUM") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pledge_sanctum_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SanctumGold)
                        ) {
                            Text(if (activeLang == "TR") "Semavi" else "Sanctum")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { onFactionSelect("COVENANT") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pledge_covenant_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = VoidNeonPurple)
                        ) {
                            Text(if (activeLang == "TR") "Kara Ahit" else "Void")
                        }
                    }

                    if (player.side != "NEUTRAL") {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onRenounce,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("outcast_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BlightDamageColor),
                            border = BorderStroke(1.dp, BlightDamageColor)
                        ) {
                            Text(if (activeLang == "TR") "Bağlılıktan İhanet Et (Sürgün Ol)" else "Renounce Allegiance (Become Outcast)")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stats checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (activeLang == "TR") "MİSTİK UNVAN KAYDI" else "SOVEREIGN ARCHIVE DECREE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Rank:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = player.rank,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Spirit Fractures:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${player.totalFractures} deaths",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = BlightDamageColor
                        )
                    }
                }
            }
        }

        // Dynamic Materials & Loot Inventory
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎒 ", fontSize = 18.sp)
                        Text(
                            text = LocalizationManager.getString(activeLang, "label_items"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (itemsList.isEmpty()) {
                        Text(
                            text = if (activeLang == "TR") "Sırt çantanız boş. Kule çarpışmalarından teçhizat kazanın." else "Your inventory is empty. Complete battles to loot gear.",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        itemsList.forEach { gear ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⚔️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = gear,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Titles Inventory Ledger
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👑 ", fontSize = 18.sp)
                        Text(
                            text = LocalizationManager.getString(activeLang, "label_titles"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = SanctumGold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (titlesList.isEmpty()) {
                        Text(
                            text = if (activeLang == "TR") "Henüz kazanılmış bir unvanınız yok. Seçimleriniz kaderi yazar." else "No sovereign titles acquired yet. Make history on higher floors.",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        titlesList.forEach { title ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⚜️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = SanctumGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JournalTab(
    journal: List<JournalEntry>,
    activeLang: String,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (activeLang == "TR") "EBEDİ KRONOLOJİ GÜNCESİ" else "THE ETERNAL CHRONOLOGY",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (activeLang == "TR") {
                "Tırmanışınız sırasında verdiğiniz kararların ve kader mühürlerinizin kutsal kaydı."
            } else {
                "The immutable history of your choices made across the 100 floors of the tower."
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        if (journal.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📜", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (activeLang == "TR") {
                            "Günce boş. Kule tırmanışına başla!"
                        } else {
                            "Your Chronology registry is empty. Begin climbing."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(journal) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("journal_item_${entry.floor}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            when (entry.sideAlignmentShift) {
                                "SANCTUM" -> SanctumGold.copy(alpha = 0.4f)
                                "COVENANT" -> VoidNeonPurple.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (activeLang == "TR") "Kat ${entry.floor} Kaydı" else "Floor ${entry.floor} Ledger",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                val shiftSymbol = if (entry.alignmentImpact > 0) "✨ Sanctum" else if (entry.alignmentImpact < 0) "🔥 Covenant" else "⚖️"
                                Text(
                                    text = shiftSymbol,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (entry.alignmentImpact > 0) SanctumGold else if (entry.alignmentImpact < 0) VoidNeonPurple else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (activeLang == "TR") entry.actionTakenTr else entry.actionTakenEs,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
