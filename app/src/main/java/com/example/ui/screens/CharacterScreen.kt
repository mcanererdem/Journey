package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.LocalizationManager
import com.example.data.model.PlayerProfile
import com.example.ui.theme.*

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
                Text("🔥 Abyss (0)", style = MaterialTheme.typography.labelSmall, color = VoidNeonPurple)
                Text("Neutral (${player.momentum})", style = MaterialTheme.typography.labelSmall)
                Text("Sanctum (100) ✨", style = MaterialTheme.typography.labelSmall, color = SanctumGold)
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
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = bulletIcon, fontSize = 14.sp)
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
