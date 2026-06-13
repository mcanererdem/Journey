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

    val isAdWatching by viewModel.isAdWatching.collectAsStateWithLifecycle()
    val adCooldownSeconds by viewModel.adCooldownSeconds.collectAsStateWithLifecycle()
    val isPurchaseDialogShown by viewModel.isPurchaseDialogShown.collectAsStateWithLifecycle()

    val actionMessage by viewModel.lastActionMessage.collectAsStateWithLifecycle()

    // Add Settings flows collection
    val themeSelection by viewModel.themeSelection.collectAsStateWithLifecycle()
    val showNotificationBanner by viewModel.showNotificationBanner.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val showTitlePrefix by viewModel.showTitlePrefix.collectAsStateWithLifecycle()
    val firebaseSyncState by viewModel.firebaseSyncState.collectAsStateWithLifecycle()

    val p = player
    val currentSide = p?.side ?: "NEUTRAL"

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        if (actionMessage.key.isNotEmpty() && !showNotificationBanner) {
            val text = actionMessage.getFormattedText(activeLang).ifBlank { 
                LocalizationManager.getString(activeLang, actionMessage.key)
            }
            snackbarHostState.showSnackbar(
                message = text,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val isPlayerInCombat = player?.let { p ->
                currentFloorNodes.getOrNull(p.currentNodeIndex)?.let { node ->
                    (node.type == NodeType.COMBAT || node.type == NodeType.BOSS) && !p.currentNodeCompleted
                }
            } ?: false

            CustomBottomNavigationBar(
                currentTab = tab,
                isPlayerInCombat = isPlayerInCombat,
                onTabSelected = { viewModel.selectTab(it) },
                activeLang = activeLang
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(ColorBackground) // Plum Black
        ) {
            // Header stats block
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
            }

            if (showNotificationBanner) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingS),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(Dimens.RadiusS),
                    border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpacingS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = LocalizationManager.getString(activeLang, "ui.desc_notification"),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.IconS)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpacingS))
                        Text(
                            text = actionMessage.getFormattedText(activeLang).ifBlank { 
                                LocalizationManager.getString(activeLang, actionMessage.key)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Serif
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.setShowNotificationBanner(false) },
                            modifier = Modifier.size(Dimens.IconS)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = LocalizationManager.getString(activeLang, "ui.desc_dismiss"),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Central Game content switching
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
                        onLockedClicked = { key -> viewModel.showActionMessage(ActionMessage(key)) },
                        onChoiceSelected = { viewModel.selectNodeChoice(it) },
                        onScenarioChoiceSelected = { viewModel.handleRpgChoice(it) },
                        onNextNodeClick = { depth, column -> viewModel.selectNodeAt(depth, column) },
                        onAscendFloorClick = { viewModel.ascendToNextFloor() },
                        onCombatAction = { viewModel.executeCombatTurn(it) },
                        onResetClick = { viewModel.resetGame() }
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
                        onUpgradePurchased = { viewModel.purchaseUpgrade(it) },
                        onClaimQuestReward = { viewModel.claimDailyQuestReward(it) }
                    )
                    NavigationTab.JOURNAL -> JournalTab(
                        journal = journal,
                        activeLang = activeLang,
                        onClear = { viewModel.resetGame() }
                    )
                    NavigationTab.SETTINGS -> SettingsTab(
                        player = player,
                        activeLang = activeLang,
                        themeSelection = themeSelection,
                        showNotificationBanner = showNotificationBanner,
                        soundEnabled = soundEnabled,
                        showTitlePrefix = showTitlePrefix,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    var simulatedBillingSku by remember { mutableStateOf<String?>(null) }

    if (isPurchaseDialogShown) {
        AlertDialog(
            onDismissRequest = { viewModel.setPurchaseDialogShown(false) },
            confirmButton = {
                TextButton(onClick = { viewModel.setPurchaseDialogShown(false) }) {
                    Text(LocalizationManager.getString(activeLang, "ui.btn_dismiss"))
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡ ", fontSize = Dimens.TextXl)
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.will_recharge_title"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.will_recharge_desc"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    // Simulated Rewarded Ad section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(Dimens.RadiusM),
                        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                            Text(
                                text = LocalizationManager.getString(activeLang, "ui.will_ad_title"),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                            Text(
                                text = LocalizationManager.getString(activeLang, "ui.will_ad_desc"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(Dimens.SpacingM))

                            if (isAdWatching) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(Dimens.IconM),
                                        strokeWidth = Dimens.BorderThick,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.SpacingS))
                                    Text(
                                        text = LocalizationManager.getString(activeLang, "ui.will_ad_watching"),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.watchRewardedAd() },
                                    enabled = adCooldownSeconds <= 0,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("watch_ad_button")
                                ) {
                                    Text(
                                        text = if (adCooldownSeconds > 0) {
                                            LocalizationManager.formatString(activeLang, "ui.will_ad_cooldown", adCooldownSeconds)
                                        } else {
                                            LocalizationManager.getString(activeLang, "ui.will_ad_btn")
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.will_premium_title"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorSanctumPrimary
                    )

                    // Offer 1: Pack Small
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { simulatedBillingSku = "pack_elixir" },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(Dimens.RadiusS),
                        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.SpacingM),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LocalizationManager.getString(activeLang, "ui.will_pack_elixir"),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = LocalizationManager.getString(activeLang, "ui.will_pack_elixir_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$1.99",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ColorSanctumPrimary),
                                modifier = Modifier.padding(start = Dimens.SpacingS)
                            )
                        }
                    }

                    // Offer 2: Pack Medium
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { simulatedBillingSku = "pack_chest" },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(Dimens.RadiusS),
                        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.SpacingM),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LocalizationManager.getString(activeLang, "ui.will_pack_chest"),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = LocalizationManager.getString(activeLang, "ui.will_pack_chest_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$3.99",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ColorSanctumPrimary),
                                modifier = Modifier.padding(start = Dimens.SpacingS)
                            )
                        }
                    }

                    // Offer 3: Lifetime / Season Pass!
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { simulatedBillingSku = "season_pass" },
                        colors = CardDefaults.cardColors(containerColor = ColorCovenantGlow.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(Dimens.RadiusM),
                        border = BorderStroke(Dimens.BorderThick, ColorCovenantGlow)
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.SpacingM),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("👑 ", fontSize = Dimens.TextL)
                                    Text(
                                        text = LocalizationManager.getString(activeLang, "ui.will_season_pass"),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = ColorCovenantGlow
                                    )
                                }
                                Text(
                                    text = LocalizationManager.getString(activeLang, "ui.will_season_pass_desc"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$4.99",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = ColorCovenantGlow),
                                modifier = Modifier.padding(start = Dimens.SpacingS)
                            )
                        }
                    }
                }
            }
        )
    }

    // Google Play Simulated Checkout Dialogue
    if (simulatedBillingSku != null) {
        val activeSku = simulatedBillingSku!!
        val productKey = when (activeSku) {
            "pack_elixir" -> "ui.will_pack_elixir"
            "pack_chest" -> "ui.will_pack_chest"
            else -> "ui.will_season_pass"
        }
        val productName = LocalizationManager.getString(activeLang, productKey)
        val productPrice = when (activeSku) {
            "pack_elixir" -> "$1.99"
            "pack_chest" -> "$3.99"
            else -> "$4.99"
        }

        AlertDialog(
            onDismissRequest = { simulatedBillingSku = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.purchaseProduct(activeSku)
                        simulatedBillingSku = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorHeal)
                ) {
                    Text(LocalizationManager.getString(activeLang, "ui.billing_btn_complete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { simulatedBillingSku = null }) {
                    Text(LocalizationManager.getString(activeLang, "ui.billing_btn_cancel"))
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(LocalizationManager.getString(activeLang, "ui.billing_title"), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.billing_desc"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                            Text(
                                text = productName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                            Text(
                                text = LocalizationManager.getString(activeLang, "ui.billing_price_label") + productPrice,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            Text(
                                text = LocalizationManager.getString(activeLang, "ui.billing_merchant"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun SettingsTab(
    player: PlayerProfile?,
    activeLang: String,
    themeSelection: String,
    showNotificationBanner: Boolean,
    soundEnabled: Boolean,
    showTitlePrefix: Boolean,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = LocalizationManager.getString(activeLang, "ui.desc_settings"),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
            text = LocalizationManager.getString(activeLang, "ui.settings_title"),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }

    // Category 1: Player Account & Stats
    Text(
        text = LocalizationManager.getString(activeLang, "ui.settings_hero_status"),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
    )

    player?.let { p ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.settings_label_name"),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = p.playerName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.settings_label_level_exp"),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Lv ${p.level} (${p.exp}/${p.maxExp} EXP)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.settings_label_momentum"),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    val alignText = when {
                         p.momentum > 50 -> LocalizationManager.formatString(activeLang, "ui.settings_momentum_light", p.momentum - 50)
                         p.momentum < 50 -> LocalizationManager.formatString(activeLang, "ui.settings_momentum_abyss", p.momentum - 50)
                         else -> LocalizationManager.getString(activeLang, "ui.settings_momentum_neutral")
                    }
                    Text(
                        text = alignText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (p.momentum > 50) ColorSanctumPrimary else if (p.momentum < 50) ColorCovenantGlow else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.settings_label_class"),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = p.chosenClass,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.settings_label_title"),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = p.equippedTitle.ifEmpty { LocalizationManager.getString(activeLang, "ui.settings_none") },
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
                    )
                }
             }
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

    // Language Selection option
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
        Text(
            text = LocalizationManager.getString(activeLang, "ui.settings_lang_title"),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.changeLanguage(if (activeLang == "TR") "EN" else "TR") }
                .padding(vertical = Dimens.SpacingXs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_lang_label"),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Button(
                onClick = { viewModel.changeLanguage(if (activeLang == "TR") "EN" else "TR") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.settings_lang_btn"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

    // Category 2: Theme & Palette Settings (with activeThemeSide reactivity)
    Text(
        text = LocalizationManager.getString(activeLang, "ui.settings_theme_title"),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
    )

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
        Text(
            text = LocalizationManager.getString(activeLang, "ui.settings_theme_desc"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Alignment Driven option
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setThemeSelection("ALIGNMENT") }
                .padding(vertical = Dimens.SpacingXs)
        ) {
            RadioButton(
                selected = themeSelection == "ALIGNMENT",
                onClick = { viewModel.setThemeSelection("ALIGNMENT") },
                modifier = Modifier.testTag("theme_radio_alignment")
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingS))
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_theme_alignment"),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Light (Sanctum) option
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setThemeSelection("LIGHT") }
                .padding(vertical = Dimens.SpacingXs)
        ) {
            RadioButton(
                selected = themeSelection == "LIGHT",
                onClick = { viewModel.setThemeSelection("LIGHT") },
                modifier = Modifier.testTag("theme_radio_light")
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingS))
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_theme_light"),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Abyss (Covenant) option
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setThemeSelection("ABYSS") }
                .padding(vertical = Dimens.SpacingXs)
        ) {
            RadioButton(
                selected = themeSelection == "ABYSS",
                onClick = { viewModel.setThemeSelection("ABYSS") },
                modifier = Modifier.testTag("theme_radio_abyss")
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingS))
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_theme_abyss"),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

    // Category 3: Display Settings & Notifications
    Text(
        text = LocalizationManager.getString(activeLang, "ui.settings_notif_title"),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
    )

    // Show Notification Card Switch
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_notif_banner"),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_notif_banner_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = showNotificationBanner,
            onCheckedChange = { viewModel.setShowNotificationBanner(it) },
            modifier = Modifier.testTag("switch_notification_banner")
        )
    }

    Spacer(modifier = Modifier.height(Dimens.SpacingXs))

    // Prefix Equipped Title Switch
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_notif_title_prefix"),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_notif_title_prefix_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = showTitlePrefix,
            onCheckedChange = { viewModel.setShowTitlePrefix(it) },
            modifier = Modifier.testTag("switch_title_prefix")
        )
    }

    Spacer(modifier = Modifier.height(Dimens.SpacingXs))

    // Sound Effects Switch
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_notif_sound"),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_notif_sound_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = soundEnabled,
            onCheckedChange = { viewModel.setSoundEnabled(it) },
            modifier = Modifier.testTag("switch_sound_effects")
        )
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = { viewModel.selectTab(NavigationTab.TOWER) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(LocalizationManager.getString(activeLang, "ui.btn_go_back"))
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
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isHeaderExpanded = !isHeaderExpanded }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Avatar + Character Name & Faction
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circle Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.5.dp, factionColor, CircleShape)
                        .background(ColorSurface, CircleShape),
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
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Name and Details
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = player.playerName.uppercase(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = ColorOnBackground
                        )
                        
                        Text(
                            text = "Lv ${player.level}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = ColorOnSurfaceMuted
                        )
                    }
                    
                    // Faction and Class info stacked
                    val factionText = when (player.side) {
                        "SANCTUM" -> LocalizationManager.getString(activeLang, "ui.header_faction_sanctum")
                        "COVENANT" -> LocalizationManager.getString(activeLang, "ui.header_faction_covenant")
                        else -> LocalizationManager.getString(activeLang, "ui.header_faction_neutral")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = factionText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = factionColor
                            )
                        )
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = ColorOnSurfaceMuted
                        )
                        Text(
                            text = player.chosenClass.substringBefore("(").trim(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontStyle = FontStyle.Italic),
                            color = ColorOnSurfaceMuted
                        )
                    }
                }
            }

            // Right: Bars Stack
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // HP Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            .background(ColorBorderMuted, RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (player.currentHp.toFloat() / player.maxHp.toFloat()).coerceIn(0f, 1f))
                                .background(ColorDanger, RoundedCornerShape(3.dp))
                        )
                    }
                }
                
                // Will/Essence Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            .background(ColorBorderMuted, RoundedCornerShape(3.dp))
                    ) {
                        val willpowerFraction = if (isSovereignPassActive) 1.0f else (player.currentWill.toFloat() / player.maxWill.toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = willpowerFraction)
                                .background(ColorStatGold, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }

    if (isHeaderExpanded) {
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = ColorBorderMuted.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.header_detailed_stats"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.label_hp_short") + "${player.currentHp} / ${player.maxHp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface
                )
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.label_will_short") + "${player.currentWill} / ${player.maxWill}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface
                )
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.label_exp_short") + "${player.exp} / ${player.maxExp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.header_economy"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorStatGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.label_gold") + ": ${player.gold} 🪙",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface
                )
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.label_gleam") + ": ${player.aether} ✨",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = LocalizationManager.getString(activeLang, "ui.header_tap_to_collapse"),
                style = MaterialTheme.typography.labelSmall,
                color = ColorOnSurfaceMuted,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun PathTimeline(
    player: PlayerProfile,
    nodes: List<AdventureNode>,
    activeLang: String
) {
    if (nodes.isEmpty()) return
    
    val currentDepth = if (player.currentNodeIndex in nodes.indices) nodes[player.currentNodeIndex].depth else 0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = LocalizationManager.getString(activeLang, "ui.path_label"),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ColorOnSurfaceMuted
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
                                else -> "❓"
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
        
        Text(
            text = LocalizationManager.getString(activeLang, "ui.label_map_tab"),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                letterSpacing = 0.5.sp
            ),
            color = ColorOnSurfaceMuted
        )
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
        color = ColorSurface,
        border = BorderStroke(1.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (tabId, pair) ->
                val (label, icon) = pair
                val isSelected = currentTab == tabId
                val activeColor = when (tabId) {
                    NavigationTab.TOWER -> if (isPlayerInCombat) ColorDanger else ColorSanctumPrimary
                    NavigationTab.OUTER_WORLD -> ColorHeal
                    NavigationTab.QUESTS -> ColorWarning
                    NavigationTab.CHAR_SHEET -> ColorCovenantGlow
                    NavigationTab.LEGACY -> ColorStatGold
                    NavigationTab.JOURNAL -> ColorInfo
                    NavigationTab.SETTINGS -> ColorOnSurfaceMuted
                }
                val tintColor = if (isSelected) activeColor else ColorOnSurfaceSubtle

                Column(
                    modifier = Modifier
                        .clickable { onTabSelected(tabId) }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
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
