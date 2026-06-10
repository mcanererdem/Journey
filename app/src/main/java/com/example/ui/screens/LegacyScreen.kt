package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.data.model.PlayerProfile
import com.example.data.model.LegacyUpgradeType
import com.example.ui.theme.*

@Composable
fun LegacyTab(
    player: PlayerProfile?,
    activeLang: String,
    onUpgradePurchased: (String) -> Unit,
    onClaimQuestReward: (Int) -> Unit
) {
    if (player == null) return

    val currentUpgrades = LegacyUpgradeType.getUpgradesMap(player.upgradesEncoded)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.SpacingL),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Miras Puanı Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingXs),
                shape = RoundedCornerShape(Dimens.RadiusL),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                border = BorderStroke(Dimens.BorderThick, ColorSanctumPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpacingL),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (activeLang == "TR") "Miras Gücü" else "Legacy Power",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = Dimens.LetterSpacingNormal),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                    Text(
                        text = "${player.legacyPoints} ⚜️",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = ColorSanctumPrimary
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                    Text(
                        text = if (activeLang == "TR") "Kule tırmanışlarını sıfırlayarak Miras Puanı kazanın." else "Earn Legacy Points by resetting your tower climbs.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
        }

        // Daily Login Streak Info
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingXs),
                shape = RoundedCornerShape(Dimens.RadiusM),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpacingL),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅",
                        fontSize = Dimens.TextHero
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingM))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activeLang == "TR") "Giriş Serisi" else "Login Streak",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (activeLang == "TR") "${player.loginStreak} Gün Boyunca Giriş Yapıldı" else "${player.loginStreak} Day(s) Active Streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(ColorHeal.copy(alpha = 0.2f), RoundedCornerShape(Dimens.RadiusXs))
                            .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXs)
                    ) {
                        Text(
                            text = if (activeLang == "TR") "Aktif" else "Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ColorHeal)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
        }

        // Daily Quests Section Title
        item {
            Text(
                text = if (activeLang == "TR") "GÜNLÜK GÖREVLER" else "DAILY QUESTS",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingXs),
                textAlign = TextAlign.Start
            )
        }

        // 3 Quests Cards
        val dailyQuestsList = player.dailyQuestsEncoded.split(",")
        if (dailyQuestsList.size >= 3) {
            items(3) { index ->
                val questParts = dailyQuestsList[index].split("/")
                if (questParts.size >= 3) {
                    val progress = questParts[0].toIntOrNull() ?: 0
                    val target = questParts[1].toIntOrNull() ?: 0
                    val claimed = questParts[2].toIntOrNull() ?: 0

                    val title = when (index) {
                        0 -> if (activeLang == "TR") "Düşmanları Bertaraf Et" else "Defeat Foes"
                        1 -> if (activeLang == "TR") "Hazineleri Yağmala" else "Pillage Chests"
                        else -> if (activeLang == "TR") "İrade Gücünü Harca" else "Exert Willpower"
                    }
                    val description = when (index) {
                        0 -> if (activeLang == "TR") "Kulede 3 dövüş veya bölüm sonu canavarı kazan." else "Win 3 combat or boss encounters in the tower."
                        1 -> if (activeLang == "TR") "Kulede 2 Hazine/Sandık hücresiyle etkileşime gir." else "Interact with 2 Chest/Treasure nodes in the tower."
                        else -> if (activeLang == "TR") "Kule tırmanışlarında 5 İrade gücü harca." else "Spend 5 Willpower on tower actions."
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.SpacingXs),
                        shape = RoundedCornerShape(Dimens.RadiusM),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(Dimens.BadgeSize)
                                        .background(
                                            if (progress >= target) ColorHeal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (index) {
                                            0 -> "⚔️"
                                            1 -> "🎁"
                                            else -> "⚡"
                                        },
                                        fontSize = Dimens.TextL
                                    )
                                }
                                Spacer(modifier = Modifier.width(Dimens.SpacingM))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Dimens.SpacingS))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${progress}/${target}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (progress >= target) ColorHeal else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (activeLang == "TR") "Ödül: +50 Altın, +15 Işıltı" else "Reward: +50 Gold, +15 Aether",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ColorSanctumPrimary
                                    )
                                }

                                Button(
                                    onClick = { onClaimQuestReward(index) },
                                    enabled = progress >= target && claimed == 0,
                                    modifier = Modifier.testTag("claim_quest_${index}_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ColorHeal,
                                        disabledContainerColor = if (claimed == 1) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Text(
                                        text = when {
                                            claimed == 1 -> if (activeLang == "TR") "Alındı" else "Claimed"
                                            progress >= target -> if (activeLang == "TR") "Ödülü Al" else "Claim Reward"
                                            else -> if (activeLang == "TR") "Devam Ediyor" else "In Progress"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (progress >= target && claimed == 0) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upgrades Section Title
        item {
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            Text(
                text = if (activeLang == "TR") "KALICI GÜÇLENDİRMELER" else "PERMANENT UPGRADES",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingXs),
                textAlign = TextAlign.Start
            )
        }

        // List all Legacy Upgrades
        items(LegacyUpgradeType.values().size) { idx ->
            val upgrade = LegacyUpgradeType.values()[idx]
            val lvl = currentUpgrades[upgrade.key] ?: 0
            val cost = upgrade.getCostForLevel(lvl)
            val isMax = lvl >= upgrade.maxLevel

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingXs),
                shape = RoundedCornerShape(Dimens.RadiusM),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(Dimens.BorderThin, if (lvl > 0) ColorSanctumPrimary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (activeLang == "TR") upgrade.nameTr else upgrade.nameEn,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (lvl > 0) ColorSanctumPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (activeLang == "TR") upgrade.descriptionTr else upgrade.descriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isMax) ColorDanger.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(Dimens.RadiusS)
                                )
                                .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXs)
                        ) {
                            Text(
                                text = if (isMax) "MAX" else "Lvl $lvl/${upgrade.maxLevel}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMax) ColorDanger else MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                    
                    // Dots indicator for upgrade levels
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)
                    ) {
                        for (i in 1..upgrade.maxLevel) {
                            Box(
                                modifier = Modifier
                                    .size(width = Dimens.SpacingXl, height = Dimens.SpacingXs)
                                    .background(
                                        color = if (i <= lvl) ColorSanctumPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(Dimens.RadiusXs)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpacingM))

                    Button(
                        onClick = { onUpgradePurchased(upgrade.key) },
                        enabled = !isMax && player.legacyPoints >= cost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upgrade_${upgrade.key.lowercase()}_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorSanctumPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = if (isMax) {
                                if (activeLang == "TR") "Maksimum Düzey" else "Maximum Level"
                            } else {
                                if (activeLang == "TR") "Yükselt ($cost ⚜️)" else "Upgrade ($cost ⚜️)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (!isMax && player.legacyPoints >= cost) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
