package com.mcanererdem.journey.ui.screens

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
import com.mcanererdem.journey.data.model.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.model.NavigationTab
import com.mcanererdem.journey.ui.theme.*
import com.mcanererdem.journey.ui.viewmodel.GameViewModel

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
    val playerStatuses by viewModel.playerStatuses.collectAsStateWithLifecycle()
    val enemyStatuses by viewModel.enemyStatuses.collectAsStateWithLifecycle()
    val currentEnemyIntent by viewModel.currentEnemyIntent.collectAsStateWithLifecycle()

    val actionMessage by viewModel.lastActionMessage.collectAsStateWithLifecycle()

    val themeSelection by viewModel.themeSelection.collectAsStateWithLifecycle()
    val showNotificationBanner by viewModel.showNotificationBanner.collectAsStateWithLifecycle()
    val showTitlePrefix by viewModel.showTitlePrefix.collectAsStateWithLifecycle()
    val firebaseSyncState by viewModel.firebaseSyncState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        if (actionMessage.key.isNotEmpty()) {
            if (!showNotificationBanner) {
                val text = actionMessage.getFormattedText(activeLang).ifBlank { 
                    LocalizationManager.getString(activeLang, actionMessage.key)
                }
                snackbarHostState.showSnackbar(
                    message = text,
                    duration = SnackbarDuration.Short
                )
            } else {
                // Auto-dismiss banner after 3 seconds
                kotlinx.coroutines.delay(3000)
                viewModel.setShowNotificationBanner(false)
            }
        }
    }

    val uiMode by viewModel.uiMode.collectAsStateWithLifecycle()
    val activeThemeSide by viewModel.activeThemeSide.collectAsStateWithLifecycle()
    val animationsEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val glowEffectsEnabled by viewModel.glowEffectsEnabled.collectAsStateWithLifecycle()

    RpgTheme(side = activeThemeSide, uiMode = uiMode) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                val isPlayerInCombat = player?.let { p ->
                    currentFloorNodes.getOrNull(p.currentNodeIndex)?.let { node ->
                        (node.type == NodeType.COMBAT || node.type == NodeType.BOSS) && !p.currentNodeCompleted
                    }
                } ?: false

                Column {
                    // Global Notification Banner just above bottom navigation
                    val bannerVisible = showNotificationBanner && actionMessage.key.isNotEmpty()
                    
                    if (bannerVisible) {
                        val dismissState = rememberSwipeToDismissBoxState()
                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                viewModel.setShowNotificationBanner(false)
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {},
                            content = {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Dimens.SpacingXs, vertical = Dimens.SpacingXxs),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                                    ),
                                    shape = RoundedCornerShape(Dimens.RadiusS),
                                    border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Dimens.SpacingS),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(Dimens.IconS)
                                        )
                                        Spacer(modifier = Modifier.width(Dimens.SpacingS))
                                        Text(
                                            text = actionMessage.getFormattedText(activeLang).ifBlank { 
                                                LocalizationManager.getString(activeLang, actionMessage.key)
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        )
                    }

                    CustomBottomNavigationBar(
                        currentTab = tab,
                        isPlayerInCombat = isPlayerInCombat,
                        onTabSelected = { viewModel.selectTab(it) },
                        activeLang = activeLang
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
                // Header Area - ONLY shown on TOWER tab
                if (tab == NavigationTab.TOWER) {
                    player?.let { p ->
                        HeaderStatsBlock(
                            player = p,
                            activeLang = activeLang,
                            showTitlePrefix = showTitlePrefix,
                            onLangChange = { viewModel.changeLanguage(if (activeLang == "TR") "EN" else "TR") },
                            onResetGame = { viewModel.resetGame() },
                            onSettingsClick = { viewModel.selectTab(NavigationTab.SETTINGS) },
                            onStoreClick = { viewModel.setPurchaseDialogShown(true) }
                        )
                    } ?: Box(modifier = Modifier.fillMaxWidth().height(Dimens.IconXl)) // Loading Placeholder
                }

                // Dynamic Page Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (tab) {
                        NavigationTab.TOWER -> TowerClimbTab(
                            player = player,
                            nodes = currentFloorNodes,
                            scenario = scenario,
                            activeEnemyHp = activeEnemyHp,
                            combatLog = combatLog,
                            activeLang = activeLang,
                            journal = journal ?: emptyList(),
                            playerStatuses = playerStatuses,
                            enemyStatuses = enemyStatuses,
                            currentEnemyIntent = currentEnemyIntent,
                            actionMessage = actionMessage,
                            showNotificationBanner = false, 
                            animationsEnabled = animationsEnabled,
                            glowEffectsEnabled = glowEffectsEnabled,
                            onDismissNotification = { viewModel.setShowNotificationBanner(false) },
                            onLockedClicked = { key -> viewModel.showActionMessage(ActionMessage(key)) },
                            onChoiceSelected = { viewModel.selectNodeChoice(it) },
                            onScenarioChoiceSelected = { viewModel.handleRpgChoice(it) },
                            onNextNodeClick = { depth, col -> viewModel.selectNodeAt(depth, col) },
                            onAscendFloorClick = { viewModel.ascendToNextFloor() },
                            onCombatAction = { viewModel.executeCombatTurn(it) },
                            onResetClick = { viewModel.resetGame() },
                            onInitCombat = { p, n, l -> viewModel.combatViewModel.checkAndInitCombat(p, n, l) }
                        )
                        NavigationTab.OUTER_WORLD -> OuterWorldTab(
                            player = player,
                            activeLang = activeLang,
                            onHeal = { viewModel.healAndRest(it) },
                            onScout = { viewModel.performAbyssScouting() },
                            onTrade = { viewModel.tradeCurrency(it) }
                        )
                        NavigationTab.CHAR_SHEET -> CharacterSheetTab(
                            player = player,
                            activeLang = activeLang,
                            firebaseSyncState = firebaseSyncState,
                            onFactionSelect = { viewModel.selectFaction(it) },
                            onRenounce = { viewModel.renounceAllegiance() },
                            onNameUpdate = { viewModel.setPlayerName(it) },
                            onSyncCloud = { viewModel.syncProfileToFirebase() },
                            onRestoreCloud = { viewModel.restoreProfileFromFirebase() }
                        )
                        NavigationTab.QUESTS -> QuestsTab(
                            player = player,
                            viewModel = viewModel,
                            activeLang = activeLang
                        )
                        NavigationTab.LEGACY -> LegacyTab(
                            player = player,
                            activeLang = activeLang,
                            animationsEnabled = animationsEnabled,
                            onUpgradePurchased = { viewModel.purchaseUpgrade(it) },
                            onClaimQuestReward = { /* reward logic handled in viewmodel */ }
                        )
                        NavigationTab.JOURNAL -> JournalTab(
                            journal = journal,
                            activeLang = activeLang,
                            onClear = { viewModel.resetGame() }
                        )
                        NavigationTab.SETTINGS -> {
                            val currentSoundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
                            SettingsTab(
                                player = player,
                                activeLang = activeLang,
                                themeSelection = themeSelection,
                                showNotificationBanner = showNotificationBanner,
                                soundEnabled = currentSoundEnabled,
                                showTitlePrefix = showTitlePrefix,
                                glowEffectsEnabled = glowEffectsEnabled,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderStatsBlock(
    player: PlayerProfile,
    activeLang: String,
    showTitlePrefix: Boolean = true,
    onLangChange: () -> Unit,
    onResetGame: () -> Unit,
    onSettingsClick: () -> Unit,
    onStoreClick: () -> Unit
) {
    val factionColor = when (player.side) {
        "SANCTUM" -> ColorSanctumPrimary
        "COVENANT" -> ColorCovenantGlow
        else -> ColorNeutralPrimary
    }
    
    val initial = player.playerName.take(1).uppercase()
    val isSovereignPassActive = player.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }
    var isHeaderExpanded by remember { mutableStateOf(false) }
    
    val headerHeight = Dimens.IconXl // 48dp
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isHeaderExpanded = !isHeaderExpanded }
            .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(headerHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Avatar + Character Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(headerHeight)
                        .border(Dimens.BorderNormal, factionColor, CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = factionColor
                    )
                }
                
                Spacer(modifier = Modifier.width(Dimens.SpacingS))
                
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = player.playerName.uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = Dimens.LetterSpacingTight
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1
                    )
                    
                    val factionText = when (player.side) {
                        "SANCTUM" -> LocalizationManager.getString(activeLang, "ui.header_faction_sanctum")
                        "COVENANT" -> LocalizationManager.getString(activeLang, "ui.header_faction_covenant")
                        else -> LocalizationManager.getString(activeLang, "ui.header_faction_neutral")
                    }
                    Text(
                        text = "$factionText | ${player.chosenClass.substringBefore("(").trim()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = factionColor),
                        maxLines = 1
                    )
                    
                    Text(
                        text = "Lv ${player.level}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Right: Bars Stack (HP, Will, Aether)
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // HP Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "HP",
                        tint = ColorDanger,
                        modifier = Modifier.size(10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(5.dp)
                            .background(ColorBorderMuted, RoundedCornerShape(Dimens.RadiusXs))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (player.currentHp.toFloat() / player.maxHp.toFloat()).coerceIn(0f, 1f))
                                .background(ColorDanger, RoundedCornerShape(Dimens.RadiusXs))
                        )
                    }
                }
                
                // Will/Essence Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Essence",
                        tint = ColorStatGold,
                        modifier = Modifier.size(10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(5.dp)
                            .background(ColorBorderMuted, RoundedCornerShape(Dimens.RadiusXs))
                    ) {
                        val willpowerFraction = if (isSovereignPassActive) 1.0f else (player.currentWill.toFloat() / player.maxWill.toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = willpowerFraction)
                                .background(ColorStatGold, RoundedCornerShape(Dimens.RadiusXs))
                        )
                    }
                }

                // Aether Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Aether",
                        tint = ColorStatAether,
                        modifier = Modifier.size(10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(5.dp)
                            .background(ColorBorderMuted, RoundedCornerShape(Dimens.RadiusXs))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (player.aether.toFloat() / 100f).coerceIn(0f, 1f))
                                .background(ColorStatAether, RoundedCornerShape(Dimens.RadiusXs))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingXs))

        // Global EXP Bar at the bottom of header
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { (player.exp.toFloat() / player.maxExp.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp), // More prominent
                color = ColorSanctumPrimary,
                trackColor = ColorBorderMuted
            )
        }
    }

    if (isHeaderExpanded) {
        Spacer(modifier = Modifier.height(Dimens.SpacingS))
        // Extra divider removed here
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpacingS),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.header_detailed_stats"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.label_hp_short") + "${player.currentHp} / ${player.maxHp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorDanger
                )
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.label_will_short") + "${player.currentWill} / ${player.maxWill}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorStatGold
                )
                Text(
                    text = "Aether Flow: ${player.aether}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorStatAether
                )
            }
            
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.header_economy"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorStatGold
                )
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.label_gold") + ": ${player.gold} 🪙",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorStatGold
                )
                Text(
                    text = "Rank: ${player.equippedTitle.ifEmpty { "Wanderer" }}",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = ColorInfo
                )
                val itemCount = player.itemsEncoded.split(",").filter { it.isNotBlank() }.size
                Text(
                    text = "Artifacts: $itemCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorHeal
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Dimens.SpacingS))
    }
}

@Composable
fun CustomBottomNavigationBar(
    currentTab: NavigationTab,
    isPlayerInCombat: Boolean,
    onTabSelected: (NavigationTab) -> Unit,
    activeLang: String
) {
    val tabs = listOf(
        NavigationTab.TOWER to if (isPlayerInCombat) {
            LocalizationManager.getString(activeLang, "ui.nav_tower_combat") to "⚔️"
        } else {
            LocalizationManager.getString(activeLang, "ui.nav_tower_floor") to "🏰"
        },
        NavigationTab.OUTER_WORLD to (LocalizationManager.getString(activeLang, "ui.nav_world") to "🌍"),
        NavigationTab.QUESTS to (LocalizationManager.getString(activeLang, "ui.nav_quest") to "📜"),
        NavigationTab.CHAR_SHEET to (LocalizationManager.getString(activeLang, "ui.nav_hero") to "👤"),
        NavigationTab.LEGACY to (LocalizationManager.getString(activeLang, "ui.nav_legacy") to "💎"),
        NavigationTab.JOURNAL to (LocalizationManager.getString(activeLang, "ui.nav_journal") to "📖"),
        NavigationTab.SETTINGS to (LocalizationManager.getString(activeLang, "ui.nav_settings") to "⚙️")
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.SpacingXs, vertical = Dimens.SpacingXs),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (tabId, pair) ->
                val (label, icon) = pair
                val isSelected = currentTab == tabId
                val activeColor = when (tabId) {
                    NavigationTab.TOWER -> if (isPlayerInCombat) ColorDanger else MaterialTheme.colorScheme.primary
                    NavigationTab.OUTER_WORLD -> ColorHeal
                    NavigationTab.QUESTS -> ColorWarning
                    NavigationTab.CHAR_SHEET -> ColorCovenantGlow
                    NavigationTab.LEGACY -> ColorStatGold
                    NavigationTab.JOURNAL -> ColorInfo
                    NavigationTab.SETTINGS -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val tintColor = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                Column(
                    modifier = Modifier
                        .clickable { onTabSelected(tabId) }
                        .padding(horizontal = Dimens.SpacingXs, vertical = Dimens.SpacingXxs),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = icon,
                        fontSize = 18.sp,
                        color = tintColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = tintColor
                    )
                }
            }
        }
    }
}
