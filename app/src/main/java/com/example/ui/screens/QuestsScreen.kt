package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.engine.FloorBlueprintSystem
import com.example.data.engine.FloorStateManager
import com.example.data.engine.NarrativeEventProcessor
import com.example.data.engine.QuestTitleSystem
import com.example.data.engine.QuestType
import com.example.data.model.PlayerProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun QuestsTab(
    player: PlayerProfile?,
    viewModel: GameViewModel,
    activeLang: String
) {
    if (player == null) return

    val completedEvents by viewModel.completedEvents.collectAsStateWithLifecycle()
    val slainSecretBosses by viewModel.slainSecretBosses.collectAsStateWithLifecycle()
    val activeNarrativeEvent by viewModel.activeNarrativeEvent.collectAsStateWithLifecycle()
    val activeSecretBossCombat by viewModel.activeSecretBossCombat.collectAsStateWithLifecycle()
    val activeSecretBossHp by viewModel.activeSecretBossHp.collectAsStateWithLifecycle()
    val combatLog by viewModel.combatLog.collectAsStateWithLifecycle()

    val questsProgress = remember(player) {
        QuestTitleSystem.getQuestProgress(player)
    }

    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }
    var selectedFloorToView by remember(player.currentFloor) { mutableStateOf(player.currentFloor.coerceIn(1, 3)) }

    if (activeNarrativeEvent != null) {
        NarrativeEventView(
            event = activeNarrativeEvent!!,
            onChoiceMade = { choice -> viewModel.selectNarrativeEventOption(activeNarrativeEvent!!, choice) },
            onCancel = { viewModel.cancelNarrativeEvent() },
            activeLang = activeLang
        )
    } else if (activeSecretBossCombat != null && activeSecretBossHp != null) {
        SecretBossCombatView(
            boss = activeSecretBossCombat!!,
            player = player,
            bossHp = activeSecretBossHp!!,
            combatLog = combatLog,
            onAction = { action -> viewModel.executeSecretBossTurn(action) },
            onEscape = { viewModel.escapeSecretBoss() },
            activeLang = activeLang
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // --- TITLES CARD BOARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑 ", fontSize = 20.sp)
                        Text(
                            text = if (activeLang == "TR") "ŞANLI MİSTİK UNVANLAR" else "ARCHIVE OF SOVEREIGN TITLES",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = SanctumGold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (activeLang == "TR") {
                            "Bu kadim unvanları gerekli şartları sağlayarak açabilir ve kuşanıp pasif can (+HP) bonusu kazanabilirsiniz."
                        } else {
                            "Unlock and equip legendary titles by satisfying unique requirements to receive passive Vitality augmentation."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    QuestTitleSystem.titles.forEach { title ->
                        val isUnlocked = player.titlesEncoded.split(",").filter { it.isNotBlank() }.contains(title.id)
                        val isEquipped = player.equippedTitle == title.id

                        if (title.isHidden && !isUnlocked) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔒 ", fontSize = 16.sp)
                                    Column {
                                        Text(
                                            text = if (activeLang == "TR") "Bilinmeyen Kadim Sır" else "🔒 Mystery Ancient Pact",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = if (activeLang == "TR") "Şartlar kule derinliklerinde kilitli kalmış." else "The conditions are locked in the depths of current era.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isEquipped) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isEquipped) 2.dp else 1.dp,
                                    color = if (isEquipped) SanctumGold else if (isUnlocked) SanctumGold.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (activeLang == "TR") title.nameTr else title.nameEn,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isUnlocked) SanctumGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                            if (isUnlocked) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(SanctumGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (activeLang == "TR") "KAZANILDI" else "UNLOCKED",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                                        color = SanctumGold
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = if (activeLang == "TR") title.descTr else title.descEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (activeLang == "TR") "Bonus: +${title.hpBonus} HP • Şart: ${title.requirementDescTr}" else "Synergies: +${title.hpBonus} HP • Goal: ${title.requirementDescEn}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (isUnlocked) {
                                        Button(
                                            onClick = {
                                                if (isEquipped) {
                                                    viewModel.equipTitle("")
                                                } else {
                                                    viewModel.equipTitle(title.id)
                                                }
                                            },
                                            modifier = Modifier.testTag("equip_title_${title.id}"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isEquipped) BlightDamageColor else SanctumGold
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (isEquipped) {
                                                    if (activeLang == "TR") "ÇIKAR" else "REMOVE"
                                                } else {
                                                    if (activeLang == "TR") "KUŞAN" else "EQUIP"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (activeLang == "TR") "KİLİTLİ" else "LOCKED",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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

        // --- HANDCRAFTED BLUEPRINTS DECREES & OBJECTIVES (Single-Floor View with Selector Mode) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📐 ", fontSize = 20.sp)
                        Text(
                            text = if (activeLang == "TR") "KAT PLANI HEDEFLERİ" else "HANDCRAFTED FLOOR OBJECTIVES",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (activeLang == "TR") {
                            "Bulunduğunuz katın özel hedeflerini, gizli boss savaşlarını ve faksiyon planlarını bu ekrandan takip edebilirsiniz."
                        } else {
                            "Track active floor targets, narrative events and secret boss objectives loaded from handcrafted blueprints."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Quick Floor Selector Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (f in 1..3) {
                            val isActive = selectedFloorToView == f
                            val isPlayerHere = player.currentFloor == f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedFloorToView = f }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (if (activeLang == "TR") "Kat $f" else "Floor $f") + if (isPlayerHere) " 📍" else "",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            val floorNum = selectedFloorToView
            val (completed, total) = FloorStateManager.getFloorProgress(floorNum, player)
            val percentage = if (total > 0) completed.toFloat() / total else 0f
            val objectives = FloorStateManager.getObjectivesForFloor(floorNum, player)
            
            var expanded by remember(floorNum) { mutableStateOf(true) }
            
            val blueprint = FloorBlueprintSystem.getBlueprintForFloor(floorNum, player)
            val floorTitle = if (activeLang == "TR") blueprint.titleTr else blueprint.titleEn
            val floorDesc = if (activeLang == "TR") blueprint.descriptionTr else blueprint.descriptionEn

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (player.currentFloor == floorNum) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (player.currentFloor == floorNum) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (player.currentFloor == floorNum) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (activeLang == "TR") "$floorNum. Kat: $floorTitle" else "Floor $floorNum: $floorTitle",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (player.currentFloor == floorNum) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = floorDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                maxLines = if (expanded) Int.MAX_VALUE else 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    if (percentage >= 1f) Color(0xFF4CAF50).copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${(percentage * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (percentage >= 1f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { percentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (percentage >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    if (expanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        objectives.forEach { obj ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (obj.isCompleted) "✅" else "⏳",
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (activeLang == "TR") obj.textTr else obj.textEn,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (obj.isCompleted) FontWeight.Normal else FontWeight.Medium
                                    ),
                                    color = if (obj.isCompleted) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                        
                        if (player.currentFloor != floorNum) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.initiateTransitionToFloor(floorNum) },
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (player.currentFloor > floorNum) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (player.currentFloor > floorNum) {
                                        if (activeLang == "TR") "Bu Kata Geri Dön (-2 İrade) ↩️" else "Backtrack to Floor (-2 Will) ↩️"
                                    } else {
                                        if (activeLang == "TR") "Bu Kata Yüksel (-2 İrade) 🚀" else "Ascend to Floor (-2 Will) 🚀"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- QUESTS SECTION ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📁 ", fontSize = 18.sp)
                Text(
                    text = if (activeLang == "TR") "KULE GÖREVLERİ DEKRETİ" else "SPIRE QUESTS & DECREES",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (activeLang == "TR") {
                    "Kategorize edilmiş görev turlarını tamamlayarak kadim paralar, şanlı unvanlar ve mühürlü ekipmanlar kazanın."
                } else {
                    "Complete special trials of power to capture currency bags, royal titles and high artifacts."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // --- FILTER CHIPS CONTROLLER ---
        item {
            val categories = listOf(
                "ALL" to (if (activeLang == "TR") "HEPSİ" else "ALL"),
                "MAIN" to (if (activeLang == "TR") "ANA" else "MAIN"),
                "SIDE" to (if (activeLang == "TR") "YAN" else "SIDE"),
                "NORMAL" to (if (activeLang == "TR") "NORMAL" else "NORMAL"),
                "SPECIAL" to (if (activeLang == "TR") "ÖZEL" else "SPECIAL"),
                "CHAIN" to (if (activeLang == "TR") "ZİNCİR" else "CHAIN"),
                "HIDDEN" to (if (activeLang == "TR") "GİZLİ" else "HIDDEN"),
                "EVENT" to (if (activeLang == "TR") "ETKİNLİK" else "EVENT")
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.size) { index ->
                    val (catId, catLabel) = categories[index]
                    val isSelected = selectedCategory == catId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clickable { selectedCategory = catId }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = catLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // --- SCROLLABLE QUEST ENTRIES ---
        val filteredQuests = questsProgress.filter { item ->
            selectedCategory == "ALL" || item.quest.type.name == selectedCategory
        }

        if (filteredQuests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (activeLang == "TR") "Bu kategoride henüz ferman bulunmuyor." else "No decrees in this category list currently.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(filteredQuests.size) { index ->
                val qStatus = filteredQuests[index]
                val q = qStatus.quest

                val cardColor = when {
                    qStatus.isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    qStatus.requirementMet -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    !qStatus.isUnlocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderStrokeColor = when {
                    qStatus.isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    qStatus.requirementMet -> SanctumGold
                    !qStatus.isUnlocked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = BorderStroke(if (qStatus.requirementMet) 2.dp else 1.dp, borderStrokeColor)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val badgeColor = when (q.type) {
                                    QuestType.MAIN -> MaterialTheme.colorScheme.primary
                                    QuestType.SIDE -> SanctumGold
                                    QuestType.NORMAL -> MaterialTheme.colorScheme.secondary
                                    QuestType.SPECIAL -> MaterialTheme.colorScheme.tertiary
                                    QuestType.CHAIN -> VoidNeonPurple
                                    QuestType.HIDDEN -> BlightDamageColor
                                    QuestType.EVENT -> Color(0xFFFF9800)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = q.type.name,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                        color = badgeColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))

                                val displayTitle = if (q.type == QuestType.HIDDEN && !qStatus.isCompleted && !qStatus.requirementMet) {
                                    if (activeLang == "TR") "🔒 BİLİNMEYEN GİZEMLİ BULMACA" else "🔒 MYSTICLE TEMPLATE"
                                } else {
                                    if (activeLang == "TR") q.titleTr else q.titleEn
                                }
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            when {
                                qStatus.isCompleted -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(18.dp))
                                qStatus.requirementMet -> Icon(Icons.Default.Star, contentDescription = "Ready to Claim", tint = SanctumGold, modifier = Modifier.size(18.dp))
                                !qStatus.isUnlocked -> Icon(Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val displayDesc = if (q.type == QuestType.HIDDEN && !qStatus.isCompleted && !qStatus.requirementMet) {
                            if (activeLang == "TR") "Kozmos eylemlerinizi sayıyor. Doğru katalizörü bulana dek devam edin." else "The matrix monitors your ascension metrics. Prove your power to unfold details."
                        } else {
                            if (activeLang == "TR") q.descTr else q.descEn
                        }
                        Text(
                            text = displayDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val displayGoal = if (q.type == QuestType.HIDDEN && !qStatus.isCompleted && !qStatus.requirementMet) {
                            "??? (???)"
                        } else {
                            if (activeLang == "TR") q.requirementTr else q.requirementEn
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (activeLang == "TR") "Koşul: " else "Goal: ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = displayGoal,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (qStatus.requirementMet) SanctumGold else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val (progressLabel, progressFraction) = qStatus.getProgressLabelAndFraction(player, activeLang == "TR")
                        if (progressLabel.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (activeLang == "TR") "İlerleme" else "Progress",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = progressLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (qStatus.requirementMet) SanctumGold else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (qStatus.requirementMet) SanctumGold else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = if (activeLang == "TR") "VAADEDİLEN GANİMETLER:" else "PROMISED LOOT REWARDS:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val rList = mutableListOf<String>()
                            if (q.rewardExp > 0) rList.add("+${q.rewardExp} EXP")
                            if (q.rewardGold > 0) rList.add("+${q.rewardGold} GP")
                            if (q.rewardAether > 0) rList.add("+${q.rewardAether} Aether")
                            q.rewardItem?.let { rList.add("🎒 $it") }
                            q.rewardTitle?.let {
                                val tObj = QuestTitleSystem.getTitleDef(it)
                                val tName = if (tObj != null) (if (activeLang == "TR") tObj.nameTr else tObj.nameEn) else it
                                rList.add("👑 Name Title: $tName")
                            }
                            Text(
                                text = rList.joinToString("  •  "),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SanctumGold
                            )
                        }

                        if (qStatus.isCompleted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Green.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (activeLang == "TR") "✓ ÖDÜLLER TAMAMEN ALINDI" else "✓ REWARDS FULLY CLAIMED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Green
                                )
                            }
                        } else if (qStatus.requirementMet) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.claimQuestReward(q.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("claim_quest_${q.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = SanctumGold),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeLang == "TR") "ÖDÜLLERİ HEYBEYE EKLE 🎁" else "RECEIVE DECREE REWARDS 🎁",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        } else if (!qStatus.isUnlocked && q.prerequisiteQuestId != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val preQuest = QuestTitleSystem.quests.find { it.id == q.prerequisiteQuestId }
                            val preName = if (preQuest != null) (if (activeLang == "TR") preQuest.titleTr else preQuest.titleEn) else q.prerequisiteQuestId
                            Text(
                                text = if (activeLang == "TR") "⚠️ Önce '${preName}' aşamasını tamamlamış olmalısınız." else "⚠️ Requires preceding trial '${preName}' complete.",
                                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                                color = BlightDamageColor
                            )
                        }
                    }
                }
            }
        }

        if (selectedCategory == "ALL" || selectedCategory == "EVENT" || selectedCategory == "HIDDEN") {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (activeLang == "TR") "🔮 ZAMANSAL KRONİKLER: DİNAMİK SIRLAR" else "🔮 CHRONICLES OF TIME: DISSOLVED SECRETS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = VoidNeonPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (activeLang == "TR") {
                            "Karakterinizin hizalanması ve seviyesine göre boyutsal yırtıklar açılır. Kaderini seçip gizli bosslarla savaşın."
                        } else {
                            "Based on align affinity and level, spatial tears are detected. Select your path and wage battle against secret trial overlords."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Show all Narrative Events from NarrativeEventProcessor
                NarrativeEventProcessor.events.forEach { event ->
                    item(key = "event_" + event.id) {
                        val isCompleted = completedEvents.contains(event.id)
                        val canUnlock = event.checkPreconditions(player)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCompleted) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                } else if (canUnlock) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                }
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (canUnlock && !isCompleted) SanctumGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (isCompleted) Color.Green.copy(alpha = 0.15f) else if (canUnlock) SanctumGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (isCompleted) "✓ " + (if (activeLang == "TR") "TAMAMLANDI" else "COMPLETED")
                                                       else if (canUnlock) "🌟 " + (if (activeLang == "TR") "KEŞFEDİLDİ" else "DISCOVERED")
                                                       else "🔒 " + (if (activeLang == "TR") "KİLİTLİ SEYİR" else "VEILED PATH"),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = if (isCompleted) Color.Green else if (canUnlock) SanctumGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (canUnlock || isCompleted) (if (activeLang == "TR") event.titleTr else event.titleEn) else "???",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (canUnlock || isCompleted) (if (activeLang == "TR") event.descriptionTr else event.descriptionEn) 
                                           else (if (activeLang == "TR") "Kozmos bu boyutsal halkayı mühürlemiş durumda. Şartları tamamlayıp boyutlararası gerilimi tetikleyin." else "The cosmos has sealed this rift. Unlock the spatial pressure threshold to evoke details."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (activeLang == "TR") "Giriş Şartları: " else "Unfolding Conditions: ",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    val reqTr = when (event.id) {
                                        "event_gatekeeper_pact" -> "Seviye >= 3, Kutsal Hizalanma >= 15"
                                        "event_shadow_broker" -> "Seviye >= 5, Kaotik Hizalanma <= -15"
                                        "event_whispering_well" -> "Seviye >= 2, Saf Tarafsızlık (Denge)"
                                        else -> "Gizemli İvme"
                                    }
                                    val reqEn = when (event.id) {
                                        "event_gatekeeper_pact" -> "Level >= 3, Sanctum Alignment >= 15"
                                        "event_shadow_broker" -> "Level >= 5, Covenant Alignment <= -15"
                                        "event_whispering_well" -> "Level >= 2, Pure Neutrality (Balance)"
                                        else -> "Mystic Drift"
                                    }
                                    Text(
                                        text = if (activeLang == "TR") reqTr else reqEn,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic),
                                        color = if (canUnlock) SanctumGold else BlightDamageColor
                                    )
                                }

                                if (canUnlock && !isCompleted) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.startNarrativeEvent(event) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (activeLang == "TR") "BOYUTSAL ETKİLEŞİMİ BAŞLAT 🔮" else "TRIGGER TEMPORAL RIFT 🔮",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Show all Secret Bosses from NarrativeEventProcessor
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (activeLang == "TR") "🐉 KADİM İMTİHAN DEREBEYLERİ" else "🐉 ANCIENT TRIAL OVERLORDS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = BlightDamageColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (activeLang == "TR") {
                            "Son derece tehlikeli imtihan arenalarında gizemli ejderhalara meydan okuyun. Büyük risk, muazzam rütbe ganimetleri."
                        } else {
                            "Challenge esoteric celestial dragons in extreme risk arenas. Mighty drop rates, sovereign currencies and items."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                NarrativeEventProcessor.secretBosses.forEach { boss ->
                    item(key = "boss_" + boss.id) {
                        val isSlain = slainSecretBosses.contains(boss.id)
                        val canChallenge = boss.checkUnlock(player)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSlain) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                } else if (canChallenge) {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                }
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (canChallenge && !isSlain) BlightDamageColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (isSlain) Color.Green.copy(alpha = 0.15f) else if (canChallenge) BlightDamageColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (isSlain) "✓ " + (if (activeLang == "TR") "YIKILDI" else "VANQUISHED")
                                                       else if (canChallenge) "🔥 " + (if (activeLang == "TR") "MEYDAN OKUMA HAZIR" else "CHALLENGE ACTIVE")
                                                       else "🔒 " + (if (activeLang == "TR") "KAPALI MİHRAK" else "SEALED TRIANGLE"),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = if (isSlain) Color.Green else if (canChallenge) BlightDamageColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (canChallenge || isSlain) (if (activeLang == "TR") boss.nameTr else boss.nameEn) else "???",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (canChallenge || isSlain) (if (activeLang == "TR") boss.descriptionTr else boss.descriptionEn) 
                                           else (if (activeLang == "TR") "Bu efsanevi varlığın aurası henüz gizli. Gerekli kılıç aşamalarını ve ruh düzeylerini tamamlayın." else "The dynamic presence of this overlord remains veiled. Perfect your stats and alignments to unravel limits."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (activeLang == "TR") "Aura Şartları: " else "Vortex Requirements: ",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    val reqTr = when (boss.id) {
                                        "boss_solstice_dragon" -> "Seviye >= 4, Kutsal Fütüvvet >= 30"
                                        "boss_void_reaper" -> "Seviye >= 4, Kaotik Kaos <= -30"
                                        "boss_equilibrium_arbiter" -> "Ruh Kırılmaları >= 1, Saf Tarafsızlık (Denge)"
                                        else -> "Yükseliş Zinciri"
                                    }
                                    val reqEn = when (boss.id) {
                                        "boss_solstice_dragon" -> "Level >= 4, Sanctum Alignment >= 30"
                                        "boss_void_reaper" -> "Level >= 4, Covenant Alignment <= -30"
                                        "boss_equilibrium_arbiter" -> "Spirit Fractures >= 1, Pure Neutrality (Balance)"
                                        else -> "Ascended Thread"
                                    }
                                    Text(
                                        text = if (activeLang == "TR") reqTr else reqEn,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic),
                                        color = if (canChallenge) SanctumGold else BlightDamageColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (activeLang == "TR") "KİLİTLİ ZAFER GANİMETLERİ:" else "VEILED TRIUMPH REWARDS:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+${boss.rewardGold} GP  •  +${boss.rewardAether} Aether  •  🎒 ${boss.rewardItem}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = SanctumGold
                                    )
                                }

                                if (canChallenge && !isSlain) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.startSecretBossEncounter(boss) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = BlightDamageColor),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (activeLang == "TR") "MEYDAN OKUMAYI BAŞLAT ⚔️" else "ENGAGE ELDRITCH BATTLE ⚔️",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
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
}
