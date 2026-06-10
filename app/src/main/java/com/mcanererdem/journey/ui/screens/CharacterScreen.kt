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
                text = if (activeLang == "TR") "HİZALANMA DURUMU" else "FACTION ALIGNMENT SPECTRUM",
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
                Text("🔥 Abyss (0)", style = MaterialTheme.typography.labelSmall, color = ColorCovenantGlow)
                Text("Neutral (${player.momentum})", style = MaterialTheme.typography.labelSmall)
                Text("Sanctum (100) ✨", style = MaterialTheme.typography.labelSmall, color = ColorSanctumPrimary)
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
                        text = if (activeLang == "TR") "KAHRAMAN UNVANI" else "SOVEREIGN TITLE DESIGNATION",
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
                        placeholder = { Text("E.g. Lord Aldous") }
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
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
                        text = if (activeLang == "TR") "YEMİNLİ BAĞLILIK ANDI" else "SWEAR AN OATH OF POWER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Dimens.SpacingS)
                    )

                    Text(
                        text = if (activeLang == "TR") {
                            "Bir cepheye katılmak, moral değerlerine bağlı olarak özel Yansıma Sınıfınızın kilidini açar."
                        } else {
                            "Joining a faction unlocks specialized reflection classes (e.g. Holy Aegis, Death Herald) dependent on your core Alignment."
                        },
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
                            Text(if (activeLang == "TR") "Semavi" else "Sanctum")
                        }
                        Spacer(modifier = Modifier.width(Dimens.SpacingM))
                        Button(
                            onClick = { onFactionSelect("COVENANT") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pledge_covenant_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorCovenantGlow)
                        ) {
                            Text(if (activeLang == "TR") "Kara Ahit" else "Void")
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
                            Text(if (activeLang == "TR") "Bağlılıktan İhanet Et (Sürgün Ol)" else "Renounce Allegiance (Become Outcast)")
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
                        text = if (activeLang == "TR") "MİSTİK UNVAN KAYDI" else "SOVEREIGN ARCHIVE DECREE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Dimens.SpacingM)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Rank:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = player.rank,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.SpacingS))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Spirit Fractures:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${player.totalFractures} deaths",
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
                        text = if (activeLang == "TR") "KADER BULUT YEDEĞİ" else "SPIRES CLOUD BACKUP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Dimens.SpacingS)
                    )
                    Text(
                        text = if (activeLang == "TR") {
                            "İlerlemenizi Firebase bulut veritabanına yedekleyin veya eski bir kaydı geri yükleyin."
                        } else {
                            "Backup your character progress to the Firebase cloud database or restore an existing save."
                        },
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
                                    (if (activeLang == "TR") "Yedekleniyor..." else "Backing Up...")
                                } else {
                                    (if (activeLang == "TR") "Buluta Yedekle" else "Backup Cloud")
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
                            Text(if (activeLang == "TR") "Kayıt Geri Yükle" else "Restore Cloud")
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
                            text = LocalizationManager.getString(activeLang, "label_items"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))

                    if (itemsList.isEmpty()) {
                        Text(
                            text = if (activeLang == "TR") "Sırt çantanız boş. Kule çarpışmalarından teçhizat kazanın." else "Your inventory is empty. Complete battles to loot gear.",
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
                            text = LocalizationManager.getString(activeLang, "label_titles"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                            color = ColorSanctumPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))

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
