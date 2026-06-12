package com.mcanererdem.journey.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.ui.theme.*

// Helper function to format timestamp in relative time
fun formatTimestamp(timestamp: Long, lang: String): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
         seconds < 60 -> LocalizationManager.getString(lang, "ui.journal_just_now")
         minutes < 60 -> LocalizationManager.formatString(lang, "ui.journal_moments_ago", minutes)
         hours < 24 -> LocalizationManager.formatString(lang, "ui.journal_hours_ago", hours)
         else -> {
             val days = hours / 24
             LocalizationManager.formatString(lang, "ui.journal_days_ago", days)
         }
    }
}

@Composable
fun JournalTab(
    journal: List<JournalEntry>?,
    activeLang: String,
    onClear: () -> Unit
) {
    val journalList = journal ?: emptyList()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "SANCTUM", "COVENANT", "NEUTRAL"

    val totalChoices = journalList.size
    val sanctumChoices = journalList.count { it.alignmentImpact > 0 }
    val covenantChoices = journalList.count { it.alignmentImpact < 0 }
    val totalNet = journalList.sumOf { it.alignmentImpact }

    val filteredJournal = journalList.filter { entry ->
        val textMatches = entry.getActionTaken(activeLang).contains(searchQuery, ignoreCase = true)
                || "Floor ${entry.floor}".contains(searchQuery, ignoreCase = true) || "Kat ${entry.floor}".contains(searchQuery, ignoreCase = true)

        val filterMatches = when (selectedFilter) {
            "SANCTUM" -> entry.alignmentImpact > 0
            "COVENANT" -> entry.alignmentImpact < 0
            "NEUTRAL" -> entry.alignmentImpact == 0
            else -> true
        }

        textMatches && filterMatches
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.SpacingL)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.chronology_title"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = LocalizationManager.getString(activeLang, "ui.chronology_desc"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = Dimens.SpacingXs, bottom = Dimens.SpacingM)
                )
            }

            // Clear Button
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("clear_journal_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = LocalizationManager.getString(activeLang, "ui.desc_dismiss"),
                    tint = ColorDanger
                )
            }
        }

        // Stats Summary Dashboard Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.SpacingM),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = BorderStroke(Dimens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpacingM),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Decisions
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.journal_stats_decisions"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalChoices",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.testTag("stats_total_choices")
                    )
                }

                // Split metrics
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.journal_stats_light_abyss"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "✨$sanctumChoices / 🔮$covenantChoices",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Net Alignment shift
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = LocalizationManager.getString(activeLang, "ui.journal_stats_net_balance"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val netColor = when {
                        totalNet > 0 -> ColorSanctumPrimary
                        totalNet < 0 -> ColorCovenantGlow
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Text(
                        text = if (totalNet >= 0) "+$totalNet" else "$totalNet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = netColor,
                        modifier = Modifier.testTag("stats_net_alignment")
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(LocalizationManager.getString(activeLang, "ui.journal_search_placeholder")) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.SpacingM)
                .testTag("journal_search_input"),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        // Custom Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.SpacingL),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filters = listOf(
                "ALL" to LocalizationManager.getString(activeLang, "ui.label_all"),
                "SANCTUM" to "✨ Sanctum",
                "COVENANT" to "🔮 Covenant",
                "NEUTRAL" to "⚖️ Neutral"
            )

            filters.forEach { (filterVal, label) ->
                val isSelected = selectedFilter == filterVal
                val chipBg = if (isSelected) {
                    when (filterVal) {
                        "SANCTUM" -> ColorSanctumPrimary.copy(alpha = 0.2f)
                        "COVENANT" -> ColorCovenantGlow.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    }
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }

                val chipBorder = if (isSelected) {
                    when (filterVal) {
                        "SANCTUM" -> ColorSanctumPrimary
                        "COVENANT" -> ColorCovenantGlow
                        else -> MaterialTheme.colorScheme.primary
                    }
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }

                val chipTextColor = if (isSelected) {
                    when (filterVal) {
                        "SANCTUM" -> ColorSanctumPrimary
                        "COVENANT" -> ColorCovenantGlow
                        else -> MaterialTheme.colorScheme.primary
                    }
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .background(chipBg, RoundedCornerShape(Dimens.SpacingL))
                        .border(Dimens.BorderThin, chipBorder, RoundedCornerShape(Dimens.SpacingL))
                        .clickable { selectedFilter = filterVal }
                        .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingS)
                        .testTag("filter_chip_$filterVal"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = chipTextColor
                    )
                }
            }
        }

        // Empty state versus Scrolling Timeline list
        if (filteredJournal.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📜", fontSize = 48.sp, modifier = Modifier.padding(bottom = Dimens.SpacingS))
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedFilter != "ALL") {
                            LocalizationManager.getString(activeLang, "ui.journal_no_match")
                        } else {
                            LocalizationManager.getString(activeLang, "ui.chronology_empty")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Dimens.SpacingXxl)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(filteredJournal) { index, entry ->
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    
                    val nodeColor = when {
                        entry.alignmentImpact > 0 -> ColorSanctumPrimary
                        entry.alignmentImpact < 0 -> ColorCovenantGlow
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                // Draw continuous connect track to the next node if we're not the last index
                                if (index < filteredJournal.lastIndex) {
                                    val lineX = with(density) { Dimens.SpacingL.toPx() } // Align exactly under our timeline dot
                                    val stemWidth = with(density) { Dimens.BorderThick.toPx() }
                                    val startY = with(density) { Dimens.SpacingXxl.toPx() }
                                    drawLine(
                                        color = nodeColor.copy(alpha = 0.25f),
                                        start = androidx.compose.ui.geometry.Offset(lineX, startY),
                                        end = androidx.compose.ui.geometry.Offset(lineX, size.height),
                                        strokeWidth = stemWidth
                                    )
                                }
                            }
                            .padding(vertical = Dimens.SpacingXs),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingL),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Timeline Connector Left Panel
                        Box(
                            modifier = Modifier
                                .width(Dimens.SpacingXxxl)
                                .padding(top = Dimens.SpacingM),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            val nodeSymbol = when {
                                entry.alignmentImpact > 0 -> "✦"
                                entry.alignmentImpact < 0 -> "✧"
                                else -> "•"
                            }
                            Box(
                                modifier = Modifier
                                    .size(Dimens.SpacingXxl)
                                    .background(nodeColor.copy(alpha = 0.15f), CircleShape)
                                    .border(Dimens.BorderThick, nodeColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = nodeSymbol,
                                    color = nodeColor,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = Dimens.TextS,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Chronology Event Info Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("journal_item_${entry.floor}"),
                            shape = RoundedCornerShape(Dimens.SpacingM),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                Dimens.BorderThin,
                                when {
                                    entry.alignmentImpact > 0 -> ColorSanctumPrimary.copy(alpha = 0.35f)
                                    entry.alignmentImpact < 0 -> ColorCovenantGlow.copy(alpha = 0.35f)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = LocalizationManager.formatString(activeLang, "ui.journal_entry_header", entry.floor),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    val momentumLabel = LocalizationManager.getString(activeLang, "ui.label_momentum")
                                    val neutralLabel = LocalizationManager.getString(activeLang, "ui.label_neutral")
                                    
                                    val shiftSymbol = when {
                                         entry.alignmentImpact > 0 -> "✨ +${entry.alignmentImpact} $momentumLabel"
                                         entry.alignmentImpact < 0 -> "🔮 ${entry.alignmentImpact} $momentumLabel"
                                        else -> "⚖️ $neutralLabel"
                                    }
                                    Text(
                                        text = shiftSymbol,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (entry.alignmentImpact > 0) ColorSanctumPrimary else if (entry.alignmentImpact < 0) ColorCovenantGlow else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(Dimens.SpacingS))
                                Text(
                                    text = entry.getActionTaken(activeLang),
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif, lineHeight = Dimens.TextL),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                                // Localized elapsed/formatted timestamp indicator
                                Text(
                                    text = formatTimestamp(entry.timestamp, activeLang),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = Dimens.TextXxs, fontWeight = FontWeight.Light),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
