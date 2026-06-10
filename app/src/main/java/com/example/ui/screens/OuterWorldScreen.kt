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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.LocalizationManager
import com.example.data.model.PlayerProfile
import com.example.ui.theme.*

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
                text = LocalizationManager.getString(activeLang, "outer_haven_title"),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = LocalizationManager.getString(activeLang, "outer_haven_desc"),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Rest & Recover section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "action_rest_title"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = LocalizationManager.getString(activeLang, "action_rest_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onHeal(20) },
                            enabled = player.gold >= 15 && player.currentHp < player.maxHp,
                            modifier = Modifier.weight(1f).testTag("rest_light_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiritHealColor)
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
                            colors = ButtonDefaults.buttonColors(containerColor = SanctumGold)
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
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "action_scout_title"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = LocalizationManager.getString(activeLang, "action_scout_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Button(
                        onClick = onScout,
                        enabled = player.gold >= 25,
                        modifier = Modifier.fillMaxWidth().testTag("scout_abyss_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = VoidNeonPurple)
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
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = LocalizationManager.getString(activeLang, "market_header"),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Buy Aether
        item {
            ExchangeCard(
                title = LocalizationManager.getString(activeLang, "buy_aether_title"),
                description = LocalizationManager.getString(activeLang, "buy_aether_desc"),
                goldCost = 50,
                currentGold = player.gold,
                badgeColor = SanctumGold,
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
