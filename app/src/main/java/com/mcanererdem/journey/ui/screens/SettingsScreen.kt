package com.mcanererdem.journey.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.model.NavigationTab
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.ui.theme.*
import com.mcanererdem.journey.ui.viewmodel.GameViewModel

@Composable
fun SettingsTab(
    player: PlayerProfile?,
    activeLang: String,
    themeSelection: String,
    showNotificationBanner: Boolean,
    soundEnabled: Boolean,
    showTitlePrefix: Boolean,
    glowEffectsEnabled: Boolean,
    viewModel: GameViewModel
) {
    val uiMode by viewModel.uiMode.collectAsStateWithLifecycle()
    val animationsEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.SpacingL)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = Dimens.SpacingS)
            )
            Text(
                text = LocalizationManager.getString(activeLang, "ui.settings_title"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Category 1: Visual Theme (Alignment vs Custom)
        SettingsSectionHeader(title = "VISUAL THEME")
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
            val themeOptions = listOf(
                "ALIGNMENT" to "Follow Alignment",
                "SANCTUM" to "Sanctum Gold",
                "COVENANT" to "Void Purple"
            )
            themeOptions.forEach { (id, label) ->
                ThemeRadioButton(
                    label = label,
                    selected = themeSelection == id,
                    onClick = { viewModel.setThemeSelection(id) }
                )
            }
        }

        // Category 2: UI Appearance (Light/Dark/System)
        SettingsSectionHeader(title = "INTERFACE MODE")
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
            val modeOptions = listOf(
                "DARK" to "Dark Mode",
                "LIGHT" to "Light Mode",
                "SYSTEM" to "System Default"
            )
            modeOptions.forEach { (id, label) ->
                ThemeRadioButton(
                    label = label,
                    selected = uiMode == id,
                    onClick = { viewModel.setUiMode(id) }
                )
            }
        }

        // Category 3: Effects & Animations
        SettingsSectionHeader(title = "EFFECTS & FEEDBACK")
        
        SettingSwitchRow(
            label = "Animations Enabled",
            desc = "Toggle shake and pulse effects",
            checked = animationsEnabled,
            onCheckedChange = { viewModel.setAnimationsEnabled(it) }
        )

        SettingSwitchRow(
            label = "Glow Effects",
            desc = "Toggle shimmer and wave effects",
            checked = glowEffectsEnabled,
            onCheckedChange = { viewModel.setGlowEffectsEnabled(it) }
        )

        SettingSwitchRow(
            label = "Sound Effects",
            desc = "In-game combat and UI sounds",
            checked = soundEnabled,
            onCheckedChange = { viewModel.setSoundEnabled(it) }
        )

        SettingSwitchRow(
            label = "Notification Banner",
            desc = "Show floating status messages",
            checked = showNotificationBanner,
            onCheckedChange = { viewModel.setShowNotificationBanner(it) }
        )

        HorizontalDivider(color = ColorBorder.copy(alpha = 0.2f))
        
        Button(
            onClick = { viewModel.selectTab(NavigationTab.TOWER) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(LocalizationManager.getString(activeLang, "ui.btn_go_back"))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = Dimens.LetterSpacingWide),
        color = ColorOnSurfaceMuted,
        modifier = Modifier.padding(top = Dimens.SpacingS)
    )
}

@Composable
fun ThemeRadioButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Dimens.SpacingXs)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(Dimens.SpacingS))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SettingSwitchRow(label: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = ColorOnSurfaceMuted)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
