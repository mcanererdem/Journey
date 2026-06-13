package com.mcanererdem.journey.ui.screens

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
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.model.LegacyUpgradeType
import com.mcanererdem.journey.ui.theme.*

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
                        text = LocalizationManager.getString(activeLang, "ui.legacy_power"),
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
                        text = LocalizationManager.getString(activeLang, "ui.legacy_earn_desc"),
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
                            text = LocalizationManager.getString(activeLang, "ui.legacy_login_streak"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = LocalizationManager.formatString(activeLang, "ui.legacy_active_days", player.loginStreak),
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
                            text = LocalizationManager.getString(activeLang, "ui.label_active"),
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
                text = LocalizationManager.getString(activeLang, "ui.legacy_daily_quests"),
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
                        0 -> LocalizationManager.getString(activeLang, "ui.legacy_quest_defeat")
                        1 -> LocalizationManager.getString(activeLang, "ui.legacy_quest_pillage")
                        else -> LocalizationManager.getString(activeLang, "ui.legacy_quest_will")
                    }
                    val description = when (index) {
                        0 -> LocalizationManager.getString(activeLang, "ui.legacy_quest_defeat_desc")
                        1 -> LocalizationManager.getString(activeLang, "ui.legacy_quest_pillage_desc")
                        else -> LocalizationManager.getString(activeLang, "ui.legacy_quest_will_desc")
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
                                        text = LocalizationManager.getString(activeLang, "ui.legacy_quest_reward"),
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
                                            claimed == 1 -> LocalizationManager.getString(activeLang, "ui.legacy_claimed")
                                            progress >= target -> LocalizationManager.getString(activeLang, "ui.legacy_claim_btn")
                                            else -> LocalizationManager.getString(activeLang, "ui.legacy_in_progress")
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
                text = LocalizationManager.getString(activeLang, "ui.legacy_upgrades_title"),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingXs),
                textAlign = TextAlign.Start
            )
        }

        // List all Legacy Upgrades
        items(LegacyUpgradeType.entries.size) { idx ->
            val upgrade = LegacyUpgradeType.entries[idx]
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
                                text = LocalizationManager.getString(activeLang, upgrade.nameKey),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (lvl > 0) ColorSanctumPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = LocalizationManager.getString(activeLang, upgrade.descriptionKey),
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
                                LocalizationManager.getString(activeLang, "ui.legacy_max_level")
                            } else {
                                LocalizationManager.formatString(activeLang, "ui.legacy_upgrade_btn", cost)
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
