package com.mcanererdem.journey.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.ui.theme.*

@Composable
fun CharacterSheetTab(
    player: PlayerProfile?,
    activeLang: String,
    firebaseSyncState: String,
    onFactionSelect: (String) -> Unit,
    onRenounce: () -> Unit,
    onNameUpdate: (String) -> Unit,
    onSyncCloud: () -> Unit,
    onRestoreCloud: () -> Unit
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
            .padding(Dimens.SpacingL),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Alignment Scale Widget
        item {
            Text(
                text = LocalizationManager.getString(activeLang, "ui.label_momentum"),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingS))

            // Spec graphic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.BadgeSize)
                    .clip(RoundedCornerShape(Dimens.SpacingS))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                ColorCovenantPrimary,
                                ColorNeutralPrimary,
                                ColorSanctumPrimary
                            )
                        )
                    )
            ) {
                // Marker pointer
                val percentFloat = player.momentum.toFloat() / 100f
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percentFloat)
                        .background(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(Dimens.SpacingS)
                            .fillMaxHeight()
                            .background(Color.White)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.SpacingXs),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🔥 " + LocalizationManager.getString(activeLang, "ui.label_abyss") + " (0)", style = MaterialTheme.typography.labelSmall, color = ColorCovenantGlow)
                Text(LocalizationManager.getString(activeLang, "ui.label_neutral") + " (${player.momentum})", style = MaterialTheme.typography.labelSmall)
                Text(LocalizationManager.getString(activeLang, "ui.label_sanctum") + " (100) ✨", style = MaterialTheme.typography.labelSmall, color = ColorSanctumPrimary)
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingL))
        }

        // Editable name card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.hero_title"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("char_name_input"),
                        singleLine = true,
                        placeholder = { Text(LocalizationManager.getString(activeLang, "ui.char_name_placeholder")) }
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                    Button(
                        onClick = { onNameUpdate(nameInput) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_name_btn")
                    ) {
                        Text(LocalizationManager.getString(activeLang, "ui.char_save_name"))
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingL))
        }

        // Faction swearing mechanics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.alliance_oath"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Dimens.SpacingS)
                    )

                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.alliance_details"),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = Dimens.SpacingM)
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
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSanctumPrimary)
                        ) {
                            Text(LocalizationManager.getString(activeLang, "ui.char_faction_sanctum"))
                        }
                        Spacer(modifier = Modifier.width(Dimens.SpacingM))
                        Button(
                            onClick = { onFactionSelect("COVENANT") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pledge_covenant_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorCovenantGlow)
                        ) {
                            Text(LocalizationManager.getString(activeLang, "ui.char_faction_covenant"))
                        }
                    }

                    if (player.side != "NEUTRAL") {
                        Spacer(modifier = Modifier.height(Dimens.SpacingM))
                        OutlinedButton(
                            onClick = onRenounce,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("outcast_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorDanger),
                            border = BorderStroke(Dimens.BorderThin, ColorDanger)
                        ) {
                            Text(LocalizationManager.getString(activeLang, "ui.renounce_btn"))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingL))
        }

        // Stats checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.archive_decree"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Dimens.SpacingM)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(LocalizationManager.getString(activeLang, "ui.active_rank_lbl"), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = player.rank,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.SpacingS))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(LocalizationManager.getString(activeLang, "ui.spirit_fractures_lbl"), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = LocalizationManager.formatString(activeLang, "ui.spirit_fracture_deaths", player.totalFractures),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorDanger
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingL))
        }

        // Firebase Cloud Save Sync Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.char_backup_title"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Dimens.SpacingS)
                    )
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.char_backup_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = Dimens.SpacingM)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
                    ) {
                        Button(
                            onClick = onSyncCloud,
                            enabled = firebaseSyncState != "SYNCING",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_cloud_btn")
                        ) {
                            Text(
                                text = if (firebaseSyncState == "SYNCING") {
                                    LocalizationManager.getString(activeLang, "ui.char_backing_up")
                                } else {
                                    LocalizationManager.getString(activeLang, "ui.char_backup_btn")
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = onRestoreCloud,
                            enabled = firebaseSyncState != "SYNCING",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restore_cloud_btn"),
                            border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(LocalizationManager.getString(activeLang, "ui.char_restore_btn"))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingL))
        }

        // Dynamic Materials & Loot Inventory
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎒 ", fontSize = Dimens.TextXl)
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.label_items"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))

                    if (itemsList.isEmpty()) {
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.char_inventory_empty"),
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        itemsList.forEach { gear ->
                            val bulletIcon = when {
                                gear.startsWith("Familiar:") -> "🐾"
                                gear.startsWith("Companion:") -> "🧚"
                                gear.contains("Shield", ignoreCase = true) || gear.contains("Aegis", ignoreCase = true) -> "🛡️"
                                gear.contains("Plate", ignoreCase = true) || gear.contains("Cloak", ignoreCase = true) -> "🧥"
                                gear.contains("Ring", ignoreCase = true) || gear.contains("Signet", ignoreCase = true) -> "💍"
                                else -> "⚔️"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.SpacingXs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                  Text(text = bulletIcon, fontSize = Dimens.TextM)
                                  Spacer(modifier = Modifier.width(Dimens.SpacingS))
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
            Spacer(modifier = Modifier.height(Dimens.SpacingL))
        }

        // Dynamic Titles Inventory Ledger
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👑 ", fontSize = Dimens.TextXl)
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.label_titles"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                            color = ColorSanctumPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))

                    if (titlesList.isEmpty()) {
                        Text(
                            text = LocalizationManager.getString(activeLang, "ui.char_titles_empty"),
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        titlesList.forEach { title ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.SpacingXs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⚜️", fontSize = Dimens.TextM)
                                Spacer(modifier = Modifier.width(Dimens.SpacingS))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ColorSanctumPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
