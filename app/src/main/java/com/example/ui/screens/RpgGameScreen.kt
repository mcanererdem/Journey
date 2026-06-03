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
                    label = { Text(stringResource(R.string.tab_tower)) },
                    modifier = Modifier.testTag("nav_tower")
                )
                NavigationBarItem(
                    selected = tab == "OUTER_WORLD",
                    onClick = { viewModel.selectTab("OUTER_WORLD") },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_outer)) },
                    modifier = Modifier.testTag("nav_outer")
                )
                NavigationBarItem(
                    selected = tab == "CHAR_SHEET",
                    onClick = { viewModel.selectTab("CHAR_SHEET") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_character)) },
                    modifier = Modifier.testTag("nav_char")
                )
                NavigationBarItem(
                    selected = tab == "JOURNAL",
                    onClick = { viewModel.selectTab("JOURNAL") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_journal)) },
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
                        scenario = scenario,
                        activeLang = activeLang,
                        onChoiceSelected = { viewModel.handleRpgChoice(it) },
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
    scenario: FloorScenario?,
    activeLang: String,
    onChoiceSelected: (GameOption) -> Unit,
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
                    text = if (activeLang == "TR") "KULE KAT: ${player.currentFloor} / 100" else "TOWER FLOOR: ${player.currentFloor} / 100",
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
                text = if (activeLang == "TR") "Uyanış Noktası: Kat ${player.savedFloorCheckpoint}" else "Shatter Checkpoint: Floor ${player.savedFloorCheckpoint}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Main chronicle/lore text card
        item {
            if (scenario != null) {
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
                            text = if (activeLang == "TR") scenario.titleTr else scenario.titleEn,
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
                            text = if (activeLang == "TR") scenario.descriptionTr else scenario.descriptionEn,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 26.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                // Game Completed state!
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
                            text = "👑 SOVEREIGN PENTACLE 👑",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = SanctumGold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (activeLang == "TR") {
                                "Kule fhedilerek tamamen mühürlendi! Sen 100 katı da tırmanarak yeni çağın Hükümdarı (Sovereign) oldun. Ebedi Çürüme'nin yazgısı artık senin seçiminde kalacak."
                            } else {
                                "The Tower is fully conquered! You climbed all 100 floors and were crowned Sovereign of the Cosmos. The timeline belongs to your faction."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onResetClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SanctumGold),
                            modifier = Modifier.testTag("ascend_victory_reset")
                        ) {
                            Text(if (activeLang == "TR") "Yeniden Başla" else "Begin New Loop")
                        }
                    }
                }
            }
        }

        // Choices block
        if (scenario != null) {
            item {
                Text(
                    text = if (activeLang == "TR") "KARARINI SEÇ" else "DECLARE YOUR CHOICE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Choice A (Celestial Path)
            item {
                ChoiceButton(
                    option = scenario.optionA,
                    activeLang = activeLang,
                    highlightColor = SanctumGold,
                    testTagValue = "choice_a_btn",
                    onClick = { onChoiceSelected(scenario.optionA) }
                )
            }

            // Choice B (Void Path)
            item {
                ChoiceButton(
                    option = scenario.optionB,
                    activeLang = activeLang,
                    highlightColor = VoidNeonPurple,
                    testTagValue = "choice_b_btn",
                    onClick = { onChoiceSelected(scenario.optionB) }
                )
            }

            // Choice C (Neutral Path)
            item {
                ChoiceButton(
                    option = scenario.optionC,
                    activeLang = activeLang,
                    highlightColor = SlateBronze,
                    testTagValue = "choice_c_btn",
                    onClick = { onChoiceSelected(scenario.optionC) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ChoiceButton(
    option: GameOption,
    activeLang: String,
    highlightColor: Color,
    testTagValue: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
            .testTag(testTagValue),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, highlightColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (activeLang == "TR") option.textTr else option.textEn,
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
                if (option.alignmentShift != 0) {
                    val shiftLabel = if (option.alignmentShift > 0) "+Align ✨" else "-Align 🔥"
                    Text(
                        text = shiftLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (option.alignmentShift > 0) SanctumGold else VoidNeonPurple,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (option.goldChange != 0) {
                    val goldLabel = if (option.goldChange > 0) "+${option.goldChange}🪙" else "${option.goldChange}🪙"
                    Text(
                        text = goldLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldYellow,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (option.gleamChange != 0) {
                    Text(
                        text = "+${option.gleamChange}✨",
                        style = MaterialTheme.typography.labelSmall,
                        color = GleamGold,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (option.pyreChange != 0) {
                    Text(
                        text = "+${option.pyreChange}🔥",
                        style = MaterialTheme.typography.labelSmall,
                        color = PyrePurple,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                if (option.hpChange != 0) {
                    val hpLabel = if (option.hpChange > 0) "+${option.hpChange} HP" else "${option.hpChange} HP"
                    Text(
                        text = hpLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (option.hpChange > 0) SpiritHealColor else BlightDamageColor
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
                text = if (activeLang == "TR") "DIŞ YAŞAM SIĞINAĞI" else "OUTER HAVEN REFUGE",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (activeLang == "TR") {
                    "Ruh Kırılmasından sonra iyileştiğiniz veya Kule seferlerine hazırlandığınız dış sınırlar."
                } else {
                    "Beyond the tower gates. Trade items, Rest to replenish HP, or Scout dangers for extra gold."
                },
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
                            text = if (activeLang == "TR") "Kaplıcada Şifalan" else "Rest at Hotsprings",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (activeLang == "TR") {
                                "Yaşam gücünüzü tamamen doldurur. Maliyet: 50 Altın."
                            } else {
                                "Restore all HP to full vitality. Costs 50 Gold."
                            },
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
                        Text(if (activeLang == "TR") "Dinlen" else "Rest")
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
                            text = if (activeLang == "TR") "Musibet Gözcülüğü Yap" else "Scout Abyss Fringes",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (activeLang == "TR") {
                                "Altın ödülü verir ama Musibetten ötürü Can kaybetme riski taşır."
                            } else {
                                "Earn gold tokens by clearing local blight. Risk of taking HP decay."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Button(
                        onClick = onScout,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateBronze),
                        modifier = Modifier.testTag("scout_button")
                    ) {
                        Text(if (activeLang == "TR") "Gözcü" else "Scout")
                    }
                }
            }
        }

        // Faction currency Exchange board
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (activeLang == "TR") "KARABORSA TAKAS TEZGÂHI" else "BLACK EXCHANGE TRADER",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Buy Gleam
        item {
            ExchangeCard(
                title = if (activeLang == "TR") "Semavi Işıltı Satın Al" else "Aquire Celestial Gleam",
                description = if (activeLang == "TR") "50 Altın ödeyerek 15 Işıltı alırsınız." else "Trade 50 Gold to earn 15 Gleam shards.",
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
                title = if (activeLang == "TR") "Kara Ateş Satın Al" else "Aquire Abyssal Pyre",
                description = if (activeLang == "TR") "50 Altın ödeyerek 15 Kara Ateş alırsınız." else "Trade 50 Gold to earn 15 Pyre fire.",
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
