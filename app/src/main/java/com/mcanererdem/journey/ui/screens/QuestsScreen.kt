package com.mcanererdem.journey.ui.screens

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
import com.mcanererdem.journey.data.engine.FloorBlueprintSystem
import com.mcanererdem.journey.data.engine.FloorStateManager
import com.mcanererdem.journey.data.engine.NarrativeEventProcessor
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.engine.QuestTitleSystem
import com.mcanererdem.journey.data.engine.QuestType
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.ui.theme.*
import com.mcanererdem.journey.ui.viewmodel.GameViewModel
import com.mcanererdem.journey.ui.screens.*

@Composable
fun QuestsTab(
    player: PlayerProfile?,
    viewModel: GameViewModel,
    activeLang: String
) {
    if (player == null) return

    val completedEvents by viewModel.completedEvents.collectAsStateWithLifecycle()
    val activeNarrativeEvent by viewModel.activeNarrativeEvent.collectAsStateWithLifecycle()
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

    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpacingL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL)
        ) {
        // --- TITLES CARD BOARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(Dimens.SpacingM),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimens.BorderThick)
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑 ", fontSize = Dimens.TextXxl)
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.quests_titles_header"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                            color = ColorSanctumPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.quests_titles_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpacingM))

                    QuestTitleSystem.titles.forEach { title ->
                        val isUnlocked = player.titlesEncoded.split(",").filter { it.isNotBlank() }.contains(title.id)
                        val isEquipped = player.equippedTitle == title.id

                        if (title.isHidden && !isUnlocked) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.SpacingXs),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(Dimens.SpacingM)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔒 ", fontSize = Dimens.TextL)
                                    Column {
                                        Text(
                                            text = LocalizationManager.getString(activeLang, "ui.quests_mystery_pact"),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = LocalizationManager.getString(activeLang, "ui.quests_mystery_locked"),
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
                                    .padding(vertical = Dimens.SpacingXs),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isEquipped) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isEquipped) Dimens.BorderThick else Dimens.BorderThin,
                                    color = if (isEquipped) ColorSanctumPrimary else if (isUnlocked) ColorSanctumPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(Dimens.SpacingM)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = title.getName(activeLang),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isUnlocked) ColorSanctumPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                            if (isUnlocked) {
                                                Spacer(modifier = Modifier.width(Dimens.SpacingS))
                                                Box(
                                                    modifier = Modifier
                                                        .background(ColorSanctumPrimary.copy(alpha = 0.15f), RoundedCornerShape(Dimens.SpacingXs))
                                                        .padding(horizontal = Dimens.SpacingXs, vertical = Dimens.BorderThick)
                                                ) {
                                                    Text(
                                                        text = LocalizationManager.getString(activeLang, "ui.label_unlocked"),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                                                        color = ColorSanctumPrimary
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = title.getDescription(activeLang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                                        Text(
                                            text = LocalizationManager.formatString(activeLang, "ui.title_synergies", title.hpBonus, title.getRequirementDesc(activeLang)),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(Dimens.SpacingS))
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
                                                containerColor = if (isEquipped) ColorDanger else ColorSanctumPrimary
                                            ),
                                            shape = RoundedCornerShape(Dimens.SpacingS),
                                            contentPadding = PaddingValues(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingS)
                                        ) {
                                            Text(
                                                text = if (isEquipped) {
                                                    LocalizationManager.getString(activeLang, "ui.quests_btn_unequip").uppercase()
                                                } else {
                                                    LocalizationManager.getString(activeLang, "ui.quests_btn_equip").uppercase()
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .border(Dimens.BorderThin, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(Dimens.SpacingS))
                                                .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingS)
                                        ) {
                                            Text(
                                                text = LocalizationManager.getString(activeLang, "ui.node_status_locked"),
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
                shape = RoundedCornerShape(Dimens.SpacingM)
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📐 ", fontSize = Dimens.TextXxl)
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.quests_floor_objectives"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpacingS))
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.quests_floor_objectives_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                    
                    // Quick Floor Selector Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                    ) {
                        for (f in 1..3) {
                            val isActive = selectedFloorToView == f
                            val isPlayerHere = player.currentFloor == f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Dimens.SpacingS))
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedFloorToView = f }
                                    .padding(vertical = Dimens.SpacingS),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = LocalizationManager.formatString(activeLang, "ui.quests_floor_f", f) + if (isPlayerHere) " 📍" else "",
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
            val floorTitle = LocalizationManager.getString(activeLang, blueprint.titleKey)
            val floorDesc = LocalizationManager.getString(activeLang, blueprint.descriptionKey)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingXs),
                shape = RoundedCornerShape(Dimens.SpacingM),
                colors = CardDefaults.cardColors(
                    containerColor = if (player.currentFloor == floorNum) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    Dimens.BorderThin,
                    if (player.currentFloor == floorNum) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(Dimens.SpacingM)
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
                                            .size(Dimens.SpacingS)
                                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.SpacingS))
                                }
                                Text(
                                    text = LocalizationManager.formatString(activeLang, "ui.quests_floor_title_fmt", floorNum, floorTitle),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (player.currentFloor == floorNum) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(Dimens.BorderThick))
                            Text(
                                text = floorDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                maxLines = if (expanded) Int.MAX_VALUE else 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(Dimens.SpacingM))
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    if (percentage >= 1f) Color(0xFF4CAF50).copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(Dimens.SpacingS)
                                )
                                .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXs)
                        ) {
                            Text(
                                text = "${(percentage * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (percentage >= 1f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Dimens.SpacingS))
                    
                    LinearProgressIndicator(
                        progress = { percentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.SpacingS)
                            .clip(RoundedCornerShape(Dimens.BorderGlow)),
                        color = if (percentage >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    if (expanded) {
                        Spacer(modifier = Modifier.height(Dimens.SpacingM))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(Dimens.SpacingM))
                        
                        objectives.forEach { obj ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.SpacingXs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (obj.isCompleted) "✅" else "⏳",
                                    fontSize = Dimens.TextM
                                )
                                Spacer(modifier = Modifier.width(Dimens.SpacingM))
                                Text(
                                    text = obj.getText(activeLang),
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
                            Spacer(modifier = Modifier.height(Dimens.SpacingM))
                            Button(
                                onClick = { viewModel.initiateTransitionToFloor(floorNum) },
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(Dimens.SpacingS),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (player.currentFloor > floorNum) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (player.currentFloor > floorNum) {
                                        LocalizationManager.getString(activeLang, "ui.quests_btn_backtrack")
                                    } else {
                                        LocalizationManager.getString(activeLang, "ui.quests_btn_ascend_will")
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
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📁 ", fontSize = Dimens.TextXxl)
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.chronology_title"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
            Text(
                text = LocalizationManager.getString(activeLang, "ui.chronology_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // --- FILTER CHIPS CONTROLLER ---
        item {
            val categories = listOf(
                "ALL" to LocalizationManager.getString(activeLang, "ui.label_all"),
                "MAIN" to LocalizationManager.getString(activeLang, "ui.label_main"),
                "SIDE" to LocalizationManager.getString(activeLang, "ui.label_side"),
                "NORMAL" to LocalizationManager.getString(activeLang, "ui.label_normal"),
                "SPECIAL" to LocalizationManager.getString(activeLang, "ui.label_special"),
                "CHAIN" to LocalizationManager.getString(activeLang, "ui.label_chain"),
                "HIDDEN" to LocalizationManager.getString(activeLang, "ui.label_hidden"),
                "EVENT" to LocalizationManager.getString(activeLang, "ui.label_event")
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingXs),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
            ) {
                items(categories.size) { index ->
                    val (catId, catLabel) = categories[index]
                    val isSelected = selectedCategory == catId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Dimens.SpacingS))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .border(Dimens.BorderThin, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(Dimens.SpacingS))
                            .clickable { selectedCategory = catId }
                            .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingM),
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
                            .padding(Dimens.SpacingXxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.quests_empty_cat"),
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
                    qStatus.requirementMet -> ColorSanctumPrimary
                    !qStatus.isUnlocked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.BorderThick),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = BorderStroke(if (qStatus.requirementMet) Dimens.BorderThick else Dimens.BorderThin, borderStrokeColor)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(Dimens.SpacingM)
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
                                    QuestType.SIDE -> ColorSanctumPrimary
                                    QuestType.NORMAL -> MaterialTheme.colorScheme.secondary
                                    QuestType.SPECIAL -> MaterialTheme.colorScheme.tertiary
                                    QuestType.CHAIN -> ColorCovenantGlow
                                    QuestType.HIDDEN -> ColorDanger
                                    QuestType.EVENT -> Color(0xFFFF9800)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(Dimens.SpacingXs))
                                        .padding(horizontal = Dimens.SpacingS, vertical = Dimens.BorderGlow)
                                ) {
                                    Text(
                                        text = q.type.name,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                                        color = badgeColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(Dimens.SpacingS))

                                val displayTitle = if (q.type == QuestType.HIDDEN && !qStatus.isCompleted && !qStatus.requirementMet) {
                                    LocalizationManager.getString(activeLang, "ui.quests_mystery_pact")
                                } else {
                                    q.getTitle(activeLang)
                                }
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            when {
                                qStatus.isCompleted -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(Dimens.SpacingL))
                                qStatus.requirementMet -> Icon(Icons.Default.Star, contentDescription = "Ready to Claim", tint = ColorSanctumPrimary, modifier = Modifier.size(Dimens.SpacingL))
                                !qStatus.isUnlocked -> Icon(Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(Dimens.SpacingL))
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.SpacingS))

                        val displayDesc = if (q.type == QuestType.HIDDEN && !qStatus.isCompleted && !qStatus.requirementMet) {
                            LocalizationManager.getString(activeLang, "ui.quests_mystery_locked")
                        } else {
                            q.getDescription(activeLang)
                        }
                        Text(
                            text = displayDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(Dimens.SpacingM))

                        val displayGoal = if (q.type == QuestType.HIDDEN && !qStatus.isCompleted && !qStatus.requirementMet) {
                            "??? (???)"
                        } else {
                            q.getRequirement(activeLang)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LocalizationManager.getString(activeLang, "ui.choice_prerequisite"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = displayGoal,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (qStatus.requirementMet) ColorSanctumPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val (progressLabel, progressFraction) = qStatus.getProgressLabelAndFraction(player, activeLang)
                        if (progressLabel.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LocalizationManager.getString(activeLang, "ui.label_momentum"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = progressLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (qStatus.requirementMet) ColorSanctumPrimary else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimens.SpacingXs)
                                    .clip(RoundedCornerShape(Dimens.BorderGlow)),
                                color = if (qStatus.requirementMet) ColorSanctumPrimary else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.SpacingS))
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(Dimens.SpacingS))
                                .padding(Dimens.SpacingS)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = LocalizationManager.getString(activeLang, "ui.exchange_title"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = Dimens.TextXxs),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                            val rList = mutableListOf<String>()
                            if (q.rewardExp > 0) rList.add("+${q.rewardExp} EXP")
                            if (q.rewardGold > 0) rList.add("+${q.rewardGold} GP")
                            if (q.rewardAether > 0) rList.add("+${q.rewardAether} Aether")
                            q.rewardItem?.let { rList.add("🎒 $it") }
                            q.rewardTitle?.let {
                                val tObj = QuestTitleSystem.getTitleDef(it)
                                val tName = tObj?.getName(activeLang) ?: it
                                rList.add("👑 Name Title: $tName")
                            }
                            Text(
                                text = rList.joinToString("  •  "),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ColorSanctumPrimary
                            )
                        }

                        if (qStatus.isCompleted) {
                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Green.copy(alpha = 0.1f), RoundedCornerShape(Dimens.SpacingS))
                                    .padding(vertical = Dimens.SpacingS),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓ " + LocalizationManager.getString(activeLang, "ui.label_completed").uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Green
                                )
                            }
                        } else if (qStatus.requirementMet) {
                            Spacer(modifier = Modifier.height(Dimens.SpacingM))
                            Button(
                                onClick = { viewModel.claimQuestReward(q.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("claim_quest_${q.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorSanctumPrimary),
                                shape = RoundedCornerShape(Dimens.SpacingS)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(Dimens.SpacingL))
                                Spacer(modifier = Modifier.width(Dimens.SpacingS))
                                Text(
                                    text = LocalizationManager.getString(activeLang, "ui.btn_trade").uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        } else if (!qStatus.isUnlocked && q.prerequisiteQuestId != null) {
                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            val preQuest = QuestTitleSystem.quests.find { it.id == q.prerequisiteQuestId }
                            val preName = preQuest?.getTitle(activeLang) ?: q.prerequisiteQuestId
                            Text(
                                text = LocalizationManager.formatString(activeLang, "ui.quests_prereq_required", preName),
                                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                                color = ColorDanger
                            )
                        }
                    }
                }
            }
        }

        if (selectedCategory == "ALL" || selectedCategory == "EVENT" || selectedCategory == "HIDDEN") {
                    item {
                        Spacer(modifier = Modifier.height(Dimens.SpacingM))
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.quests_world_events"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = Dimens.LetterSpacingNormal,
                                fontFamily = FontFamily.Serif
                            ),
                            color = ColorCovenantGlow
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.outer_haven_desc"),
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
                                .padding(vertical = Dimens.SpacingXs),
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
                                width = Dimens.BorderThin,
                                color = if (canUnlock && !isCompleted) ColorSanctumPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (isCompleted) Color.Green.copy(alpha = 0.15f) else if (canUnlock) ColorSanctumPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(Dimens.SpacingXs)
                                        ) {
                                            Text(
                                                text = if (isCompleted) "✓ " + LocalizationManager.getString(activeLang, "ui.label_completed")
                                                       else if (canUnlock) "🌟 " + LocalizationManager.getString(activeLang, "ui.node_status_active")
                                                       else "🔒 " + LocalizationManager.getString(activeLang, "ui.node_status_locked"),
                                                modifier = Modifier.padding(horizontal = Dimens.SpacingS, vertical = Dimens.BorderThick),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Bold),
                                                color = if (isCompleted) Color.Green else if (canUnlock) ColorSanctumPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            )
                                        }
                                        Text(
                                            text = if (canUnlock || isCompleted) event.getTitle(activeLang) else "???",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(Dimens.SpacingS))
                                Text(
                                    text = if (canUnlock || isCompleted) event.getDescription(activeLang) 
                                           else LocalizationManager.getString(activeLang, "ui.quests_mystery_locked"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(Dimens.SpacingM))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = LocalizationManager.getString(activeLang, "ui.quests_unfolding_conditions"),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = event.getPreconditionDesc(activeLang),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic),
                                        color = if (canUnlock) ColorSanctumPrimary else ColorDanger
                                    )
                                }

                                if (canUnlock && !isCompleted) {
                                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                                    Button(
                                        onClick = { viewModel.startNarrativeEvent(event) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(Dimens.SpacingS)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(Dimens.SpacingL))
                                        Spacer(modifier = Modifier.width(Dimens.SpacingS))
                                        Text(
                                            text = LocalizationManager.getString(activeLang, "ui.btn_initiate").uppercase(),
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
