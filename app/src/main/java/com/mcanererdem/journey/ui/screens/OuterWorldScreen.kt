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
import com.mcanererdem.journey.ui.theme.*

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
            .padding(Dimens.SpacingL),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = LocalizationManager.getString(activeLang, "outer_haven_title"),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
            Text(
                text = LocalizationManager.getString(activeLang, "outer_haven_desc"),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingXl))
        }

        // Rest & Recover section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingS),
                shape = RoundedCornerShape(Dimens.RadiusM),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "action_rest_title"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingS))
                    Text(
                        text = LocalizationManager.getString(activeLang, "action_rest_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                    ) {
                        Button(
                            onClick = { onHeal(20) },
                            enabled = player.gold >= 15 && player.currentHp < player.maxHp,
                            modifier = Modifier.weight(1f).testTag("rest_light_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorHeal)
                        ) {
                            Text(
                                text = if (activeLang == "TR") "Hafif Şifa (15🪙)" else "Light Rest (15🪙)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        
                        Button(
                            onClick = { onHeal(50) },
                            enabled = player.gold >= 30 && player.currentHp < player.maxHp,
                            modifier = Modifier.weight(1f).testTag("rest_deep_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSanctumPrimary)
                        ) {
                            Text(
                                text = if (activeLang == "TR") "Tam Şifa (30🪙)" else "Deep Rest (30🪙)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                            )
                        }
                    }
                }
            }
        }

        // Scout Abyss / Celestial Realm section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingS),
                shape = RoundedCornerShape(Dimens.RadiusM),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacingL)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "action_scout_title"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingS))
                    Text(
                        text = LocalizationManager.getString(activeLang, "action_scout_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingM))
                    
                    Button(
                        onClick = onScout,
                        enabled = player.gold >= 25,
                        modifier = Modifier.fillMaxWidth().testTag("scout_abyss_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorCovenantGlow)
                    ) {
                        Text(
                            text = if (activeLang == "TR") "Kozmik İzleme Yeteneği (25 Altın)" else "Chronicle Scouting Map (25 Gold)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Trade Resources / Exchange marketplace section
        item {
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            Text(
                text = LocalizationManager.getString(activeLang, "market_header"),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = Dimens.LetterSpacingNormal),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
        }

        // Buy Aether
        item {
            ExchangeCard(
                title = LocalizationManager.getString(activeLang, "buy_aether_title"),
                description = LocalizationManager.getString(activeLang, "buy_aether_desc"),
                goldCost = 50,
                currentGold = player.gold,
                badgeColor = ColorSanctumPrimary,
                onClick = { onTrade("GOLD_TO_AETHER") },
                testTagValue = "buy_aether_btn"
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
            .padding(vertical = Dimens.SpacingXs),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(Dimens.RadiusS)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.SpacingS)
                    .background(badgeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingM))
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
