package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.data.engine.SecretBossEncounter
import com.mcanererdem.journey.data.model.PlayerProfile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.ui.theme.*

import com.mcanererdem.journey.data.model.CombatLogEntry

@Composable
fun SecretBossCombatView(
    boss: SecretBossEncounter,
    player: PlayerProfile,
    bossHp: Int,
    combatLog: List<CombatLogEntry>,
    onAction: (String) -> Unit,
    onEscape: () -> Unit,
    activeLang: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpacingL),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(Dimens.SpacingL),
        border = BorderStroke(Dimens.BorderThick, ColorDanger)
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingL)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = boss.getName(activeLang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = ColorDanger
                    )
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.combat_boss_subtitle"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Surface(
                    color = ColorDanger.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(Dimens.SpacingXs)
                ) {
                    Text(
                        text = "BOSS",
                        modifier = Modifier.padding(Dimens.SpacingS, Dimens.SpacingXs),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ColorDanger
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { bossHp.toFloat() / boss.hp.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.SpacingM)
                        .clip(RoundedCornerShape(Dimens.SpacingS)),
                    color = ColorDanger,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(Dimens.SpacingM))
                Text(
                    text = "$bossHp/${boss.hp}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingM))
            
            val isPhase2 = bossHp < (boss.hp / 2)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isPhase2) ColorDanger.copy(alpha = 0.08f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        RoundedCornerShape(Dimens.SpacingS)
                    )
                    .padding(Dimens.SpacingS)
            ) {
                Text(
                    text = if (isPhase2) {
                        LocalizationManager.getString(activeLang, "ui.combat_boss_phase2")
                    } else {
                        LocalizationManager.getString(activeLang, "ui.combat_boss_phase1")
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isPhase2) ColorDanger else MaterialTheme.colorScheme.primary
                )
            }

            if (combatLog.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingM))
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.combat_console_logs"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                        combatLog.takeLast(3).forEach { log ->
                            val logText = log.getFormattedText(activeLang)
                            Text(
                                text = logText,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = Dimens.TextXxs),
                                color = if (log.key == "combat_log_secret_boss_initiated") ColorSanctumPrimary 
                                        else if (log.key.startsWith("combat_log_boss_") || log.key.startsWith("combat_log_enemy_")) ColorDanger 
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
            ) {
                Button(
                    onClick = { onAction("ATTACK") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorDanger)
                ) {
                    Text(LocalizationManager.getString(activeLang, "ui.combat_action_boss_attack"), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                
                Button(
                    onClick = { onAction("DEFEND") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorNeutralPrimary)
                ) {
                    Text(LocalizationManager.getString(activeLang, "ui.combat_action_boss_defend"), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
            ) {
                Button(
                    onClick = { onAction("POTION") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorSanctumPrimary)
                ) {
                    Text(LocalizationManager.getString(activeLang, "ui.combat_action_boss_heal"), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                
                OutlinedButton(
                    onClick = onEscape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(LocalizationManager.getString(activeLang, "ui.combat_action_boss_flee"), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
