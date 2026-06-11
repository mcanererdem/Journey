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
import com.mcanererdem.journey.data.engine.AdventureNode
import com.mcanererdem.journey.data.engine.NodeType
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

    val scoutedNodeIndices by viewModel.scoutedNodeIndices.collectAsStateWithLifecycle()

    val actionMessageEn by viewModel.lastActionMessageEn.collectAsStateWithLifecycle()
    val actionMessageTr by viewModel.lastActionMessageTr.collectAsStateWithLifecycle()

    // Add Settings flows collection
    val themeSelection by viewModel.themeSelection.collectAsStateWithLifecycle()
    val showNotificationBanner by viewModel.showNotificationBanner.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val showTitlePrefix by viewModel.showTitlePrefix.collectAsStateWithLifecycle()
    var isSettingsDialogShown by remember { mutableStateOf(false) }
    val firebaseSyncState by viewModel.firebaseSyncState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val p = player
    val currentSide = p?.side ?: "NEUTRAL"

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                    onSettingsClick = { isSettingsDialogShown = true },
                    onStoreClick = { viewModel.setPurchaseDialogShown(true) }
                )
                
                PathTimeline(
                    player = p,
                    nodes = currentFloorNodes,
                    activeLang = activeLang
                )
            }

            // Flash action logger alert banner
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
                            contentDescription = "Notification",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.IconS)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpacingS))
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
                        activeEnemyHp = activeEnemyHp,
                        combatLog = combatLog,
                        activeLang = activeLang,
                        journal = journal ?: emptyList(),
                        scoutedNodeIndices = scoutedNodeIndices,
                        playerStatuses = playerStatuses,
                        enemyStatuses = enemyStatuses,
                        currentEnemyIntent = currentEnemyIntent,
                        onScoutClick = { viewModel.performScouting() },
                        onLockedClicked = { en, tr -> viewModel.showActionMessage(en, tr) },
                        onChoiceSelected = { viewModel.selectNodeChoice(it) },
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
                    Text(if (activeLang == "TR") "Kapat" else "Close")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡ ", fontSize = Dimens.TextXl)
                    Text(
                        text = if (activeLang == "TR") "İRADEYİ YENİLE" else "RECHARGE WILLPOWER",
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
                        text = if (activeLang == "TR") {
                            "Kule katlarını tırmanmak (-1/sektör, -2/kat transit) ve kararlar seçmek için irade gerekir. Sponsor videoları izleyerek bedava irade kazanın veya sınırsız irade için Sezonluk Geçiş satın alın!"
                        } else {
                            "Willpower is required to climb sectors (-1/sector, -2/floor transit) and unlock critical decisions. Watch sponsor videos to earn free willpower, or acquire seasonal passes for infinite willpower!"
                        },
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
                                text = if (activeLang == "TR") "🎬 MİNİ REKLAM BÖLÜMÜ" else "🎬 SPONSOR REWARDED AD",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                            Text(
                                text = if (activeLang == "TR") {
                                    "Kısa sponsor klibini izleyin ve hediye +5 İrade kazanın! (60sn bekleme süresi vardır)"
                                } else {
                                    "Watch a quick sponsor clip to claim +5 Willpower! (60s cooldown)"
                                },
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
                                        text = if (activeLang == "TR") "Sponsor reklam izleniyor (5sn)..." else "Watching sponsor clip (5s)...",
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
                                            if (activeLang == "TR") "Bekleme Süresi (${adCooldownSeconds}sn)" else "Ad on Cooldown (${adCooldownSeconds}s)"
                                        } else {
                                            if (activeLang == "TR") "İZLE VE +5 İRADE KAZAN" else "WATCH AD TO GET +5 WILL"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    Text(
                        text = if (activeLang == "TR") "💎 PREMIUM MARKET TEKLİFLERİ" else "💎 PREMIUM STORE OFFERS",
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
                                    text = if (activeLang == "TR") "⚡ İrade Özütü İksiri" else "⚡ Elixir of Willpower",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (activeLang == "TR") "+10 İrade Gücü + 50 Altın bonus" else "+10 Willpower + 50 Gold bonus",
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
                                    text = if (activeLang == "TR") "⚡ Hükümdar İrade Sandığı" else "⚡ Sovereign Will Chest",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (activeLang == "TR") "+40 İrade Gücü + 200 Altın bonus" else "+40 Willpower + 200 Gold bonus",
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
                                        text = if (activeLang == "TR") "SEZONLUK HÜKÜMDAR KARTI" else "SEASONAL SOVEREIGN PASS",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = ColorCovenantGlow
                                    )
                                }
                                Text(
                                    text = if (activeLang == "TR") {
                                        "SINIRSIZ İRADE! Tüm sektör tırmanışları ve transitler bedava olur!"
                                    } else {
                                        "INFINITE WILLPOWER! All climbings, transits and event choices won't cost any Willpower!"
                                    },
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
        val productName = when (activeSku) {
            "pack_elixir" -> if (activeLang == "TR") "İrade Özütü İksiri" else "Elixir of Willpower"
            "pack_chest" -> if (activeLang == "TR") "Hükümdar İrade Sandığı" else "Sovereign Will Chest"
            else -> if (activeLang == "TR") "Sezonluk Hükümdar Kartı" else "Seasonal Sovereign Pass"
        }
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
                    Text(if (activeLang == "TR") "Simüle Satın Alımı Tamamla" else "Simulate Approved Purchase")
                }
            },
            dismissButton = {
                TextButton(onClick = { simulatedBillingSku = null }) {
                    Text(if (activeLang == "TR") "Vazgeç" else "Cancel")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🤖 Google Play Billing", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
                    Text(
                        text = if (activeLang == "TR") {
                            "Güvenli Google Play ödemesi simüle ediliyor. Kart onay veya Google Pay arayüzü başlatıldı."
                        } else {
                            "Secured Google Play payment service is simulated. Card approval flow initialized."
                        },
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
                                text = "${if (activeLang == "TR") "Fiyat: " else "Price: "} $productPrice",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            Text(
                                text = if (activeLang == "TR") "Satıcı: AI Studio Game Studio s.r.o." else "Merchant: AI Studio Game Studio s.r.o.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        )
    }

    if (isSettingsDialogShown) {
        AlertDialog(
            onDismissRequest = { isSettingsDialogShown = false },
            confirmButton = {
                Button(
                    onClick = { isSettingsDialogShown = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("settings_done_button")
                ) {
                    Text(if (activeLang == "TR") "Kapat" else "Close")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (activeLang == "TR") "OYUN AYARLARI" else "GAME SETTINGS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category 1: Player Account & Stats
                    Text(
                        text = if (activeLang == "TR") "👤 KAHRAMAN DURUMU & STATLAR" else "👤 HERO STATUS & STATS",
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
                                        text = if (activeLang == "TR") "İsim:" else "Hero Name:",
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
                                        text = if (activeLang == "TR") "Seviye / Deneyim (EXP):" else "Level / Experience (EXP):",
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
                                        text = if (activeLang == "TR") "Momentum:" else "Momentum Score:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    val alignText = when {
                                         p.momentum > 50 -> "Light / Saintly (+${p.momentum - 50})"
                                         p.momentum < 50 -> "Abyss / Void (${p.momentum - 50})"
                                         else -> "Neutral (50)"
                                    }
                                    val alignTextTr = when {
                                        p.momentum > 50 -> "Işık / Aziz (+${p.momentum - 50})"
                                        p.momentum < 50 -> "Boşluk / Kaos (${p.momentum - 50})"
                                        else -> "Nötr (50)"
                                    }
                                    Text(
                                        text = if (activeLang == "TR") alignTextTr else alignText,
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
                                        text = if (activeLang == "TR") "Sınıf (Class):" else "Current Class:",
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
                                        text = if (activeLang == "TR") "Kuşanılmış Unvan:" else "Equipped Title:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = p.equippedTitle.ifEmpty { if (activeLang == "TR") "Yok" else "None" },
                                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
                                    )
                                }
                             }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    // Category 2: Theme & Palette Settings (with activeThemeSide reactivity)
                    Text(
                        text = if (activeLang == "TR") "🎨 GÖRÜNÜM & TEMA SEÇİMİ" else "🎨 DISPLAY & THEME SETTINGS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
                        Text(
                            text = if (activeLang == "TR") {
                                "Nasıl bir kule ambiyansı istersiniz? Hizalanmaya Göre seçeneği, hizalanma puanınıza göre temayı otomatik belirler."
                            } else {
                                "How do you want the environment to look? 'Alignment Driven' dynamically chooses between Sanctum and Abyss based on your alignment score."
                            },
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
                                text = if (activeLang == "TR") "Hizalanmaya Göre (Dinamik)" else "Alignment Driven (Dynamic)",
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
                                text = if (activeLang == "TR") "Işık Teması (Celestial Sanctum)" else "Light Theme (Celestial Sanctum)",
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
                                text = if (activeLang == "TR") "Boşluk Teması (Abyss Covenant)" else "Abyss Theme (Void Covenant)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    // Category 3: Display Settings & Notifications
                    Text(
                        text = if (activeLang == "TR") {
                            "🔔 BİLDİRİM & GÖRÜNTÜLEME AYARLARI"
                        } else {
                            "🔔 NOTIFICATIONS & PREFERENCES"
                        },
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
                                text = if (activeLang == "TR") "Hızlı Karar Bildirimi" else "Action Banner Notification",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (activeLang == "TR") "Son oyun kararlarını gösteren başlık kartı" else "The floating banner showing recent action logs",
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
                                text = if (activeLang == "TR") "Unvan Başlığını Göster" else "Show Active Title Prefix",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (activeLang == "TR") "Kuşanılmış unvanı ismin önünde gösterir" else "Prepends active equipped title before name",
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
                                text = if (activeLang == "TR") "Oyun Efekt Sesleri" else "Combat Sound Effects",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (activeLang == "TR") "Savaş ve buton tıklama tınıları" else "Atmospheric audio and selection tones",
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
                }
            }
        )
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
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
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
            
            Spacer(modifier = Modifier.width(6.dp))
            
            // Name and Details
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.playerName.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = ColorOnBackground
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Faction badge (e.g. LAWFUL CHOIR, COVENANT)
                    val factionText = when (player.side) {
                        "SANCTUM" -> if (activeLang == "TR") "SEMAVİ KORO" else "LAWFUL CHOIR"
                        "COVENANT" -> if (activeLang == "TR") "KARA AHİT" else "VOID COVENANT"
                        else -> if (activeLang == "TR") "SÜRGÜN" else "NEUTRAL"
                    }
                    Box(
                        modifier = Modifier
                            .background(factionColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, factionColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = factionText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = factionColor
                        )
                    }
                    
                    Text(
                        text = "Lv ${player.level} • ${player.chosenClass}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorOnSurfaceMuted
                    )
                }
            }
        }
        
        // Right: HP, Essence bars and Floor
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bars Stack
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
                            .width(60.dp)
                            .height(4.dp)
                            .background(ColorBorderMuted, RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (player.currentHp.toFloat() / player.maxHp.toFloat()).coerceIn(0f, 1f))
                                .background(ColorDanger, RoundedCornerShape(2.dp))
                        )
                    }
                    Text(
                        text = "${player.currentHp}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = ColorOnSurface
                    )
                }
                
                // Will/Essence Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Essence",
                        tint = ColorCovenantGlow,
                        modifier = Modifier.size(10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .background(ColorBorderMuted, RoundedCornerShape(2.dp))
                    ) {
                        val willpowerFraction = if (isSovereignPassActive) 1.0f else (player.currentWill.toFloat() / player.maxWill.toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = willpowerFraction)
                                .background(ColorCovenantGlow, RoundedCornerShape(2.dp))
                        )
                    }
                    Text(
                        text = if (isSovereignPassActive) "∞" else "${player.currentWill}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = ColorOnSurface
                    )
                }
            }
            
            // Floor Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(ColorBorder)
            )
            
            // Floor text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${player.currentFloor}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ColorOnBackground
                )
                Text(
                    text = if (activeLang == "TR") "KAT" else "FLOOR",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, letterSpacing = 1.sp),
                    color = ColorOnSurfaceMuted
                )
            }

            // Inline Settings Dropdown Menu
            Box {
                var isMenuExpanded by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { isMenuExpanded = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = ColorOnSurfaceMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    modifier = Modifier.background(ColorSurface)
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = if (activeLang == "TR") "Dil Değiştir (EN)" else "Change Language (TR)",
                                color = ColorOnSurface
                            )
                        },
                        onClick = {
                            onLangChange()
                            isMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = if (activeLang == "TR") "Oyunu Sıfırla" else "Reset Game",
                                color = ColorDanger
                            )
                        },
                        onClick = {
                            onResetGame()
                            isMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = if (activeLang == "TR") "Ayarlar" else "Settings",
                                color = ColorSanctumPrimary
                            )
                        },
                        onClick = {
                            onSettingsClick()
                            isMenuExpanded = false
                        }
                    )
                }
            }
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
    
    val currentDepth = nodes.firstOrNull { it.index == player.currentNodeIndex }?.depth ?: 0
    
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
                text = if (activeLang == "TR") "YOL" else "PATH",
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
            text = if (activeLang == "TR") "• HARİTA" else "• MAP",
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
            if (activeLang == "TR") "DÖVÜŞ" to "⚔️" else "COMBAT" to "⚔️"
        } else {
            if (activeLang == "TR") "KAT" to "🏰" else "FLOOR" to "🏰"
        },
        NavigationTab.OUTER_WORLD to (if (activeLang == "TR") "DÜNYA" to "🌍" else "WORLD" to "🌍"),
        NavigationTab.QUESTS to (if (activeLang == "TR") "GÖREV" to "📜" else "QUEST" to "📜"),
        NavigationTab.CHAR_SHEET to (if (activeLang == "TR") "KAHRAMAN" to "👤" else "HERO" to "👤"),
        NavigationTab.LEGACY to (if (activeLang == "TR") "MİRAS" to "💎" else "LEGACY" to "💎"),
        NavigationTab.JOURNAL to (if (activeLang == "TR") "GÜNLÜK" to "📖" else "JOURNAL" to "📖")
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
