package com.example.ui.screens

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
import com.example.data.model.JournalEntry
import com.example.ui.theme.*

// Helper function to format timestamp in relative time
fun formatTimestamp(timestamp: Long, lang: String): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
         seconds < 60 -> if (lang == "TR") "Az önce" else "Just now"
         minutes < 60 -> if (lang == "TR") "Göz açıp kapayana dek ($minutes dk önce)" else "Just moments ago ($minutes m ago)"
         hours < 24 -> if (lang == "TR") "$hours saat önce" else "$hours hours ago"
         else -> {
             val days = hours / 24
             if (lang == "TR") "$days gün önce" else "$days days ago"
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
        val textMatches = if (activeLang == "TR") {
            entry.actionTakenTr.contains(searchQuery, ignoreCase = true)
        } else {
            entry.actionTakenEs.contains(searchQuery, ignoreCase = true)
        } || "Floor ${entry.floor}".contains(searchQuery, ignoreCase = true) || "Kat ${entry.floor}".contains(searchQuery, ignoreCase = true)

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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (activeLang == "TR") "EBEDİ KRONOLOJİ GÜNCESİ" else "THE ETERNAL CHRONOLOGY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (activeLang == "TR") {
                        "Tırmanışınız sırasında verdiğiniz kararların ve kader mühürlerinizin kutsal kaydı."
                    } else {
                        "The immutable history of your choices made across the 100 floors of the tower."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
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
                    contentDescription = if (activeLang == "TR") "Geçmişi Sıfırla" else "Clear History",
                    tint = BlightDamageColor
                )
            }
        }

        // Stats Summary Dashboard Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Decisions
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (activeLang == "TR") "Kararlar" else "Decisions",
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
                        text = if (activeLang == "TR") "Kutsal / Boşluk" else "Light / Abyss",
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
                        text = if (activeLang == "TR") "Net Odak" else "Net Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val netColor = when {
                        totalNet > 0 -> SanctumGold
                        totalNet < 0 -> VoidNeonPurple
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
            placeholder = { Text(if (activeLang == "TR") "Macerada ara..." else "Search chronicles...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
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
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filters = listOf(
                "ALL" to (if (activeLang == "TR") "Hepsi" else "All"),
                "SANCTUM" to "✨ Sanctum",
                "COVENANT" to "🔮 Covenant",
                "NEUTRAL" to "⚖️ Neutral"
            )

            filters.forEach { (filterVal, label) ->
                val isSelected = selectedFilter == filterVal
                val chipBg = if (isSelected) {
                    when (filterVal) {
                        "SANCTUM" -> SanctumGold.copy(alpha = 0.2f)
                        "COVENANT" -> VoidNeonPurple.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    }
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }

                val chipBorder = if (isSelected) {
                    when (filterVal) {
                        "SANCTUM" -> SanctumGold
                        "COVENANT" -> VoidNeonPurple
                        else -> MaterialTheme.colorScheme.primary
                    }
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }

                val chipTextColor = if (isSelected) {
                    when (filterVal) {
                        "SANCTUM" -> SanctumGold
                        "COVENANT" -> VoidNeonPurple
                        else -> MaterialTheme.colorScheme.primary
                    }
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .background(chipBg, RoundedCornerShape(16.dp))
                        .border(1.dp, chipBorder, RoundedCornerShape(16.dp))
                        .clickable { selectedFilter = filterVal }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
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
                    Text("📜", fontSize = 48.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        text = if (activeLang == "TR") {
                            if (searchQuery.isNotEmpty() || selectedFilter != "ALL") "Aranan kriterlere uygun kayıt bulunamadı." else "Hala tırmanışa devam ediyorsun. Kronolojide bir kayıt yok."
                        } else {
                            if (searchQuery.isNotEmpty() || selectedFilter != "ALL") "No ledger logs match your criteria." else "Your Chronology registry is empty. Begin climbing."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
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
                        entry.alignmentImpact > 0 -> SanctumGold
                        entry.alignmentImpact < 0 -> VoidNeonPurple
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                // Draw continuous connect track to the next node if we're not the last index
                                if (index < filteredJournal.lastIndex) {
                                    val lineX = with(density) { 16.dp.toPx() } // Align exactly under our timeline dot
                                    val stemWidth = with(density) { 2.dp.toPx() }
                                    val startY = with(density) { 24.dp.toPx() }
                                    drawLine(
                                        color = nodeColor.copy(alpha = 0.25f),
                                        start = androidx.compose.ui.geometry.Offset(lineX, startY),
                                        end = androidx.compose.ui.geometry.Offset(lineX, size.height),
                                        strokeWidth = stemWidth
                                    )
                                }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Timeline Connector Left Panel
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .padding(top = 10.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            val nodeSymbol = when {
                                entry.alignmentImpact > 0 -> "✦"
                                entry.alignmentImpact < 0 -> "✧"
                                else -> "•"
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(nodeColor.copy(alpha = 0.15f), CircleShape)
                                    .border(2.2.dp, nodeColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = nodeSymbol,
                                    color = nodeColor,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
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
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    entry.alignmentImpact > 0 -> SanctumGold.copy(alpha = 0.35f)
                                    entry.alignmentImpact < 0 -> VoidNeonPurple.copy(alpha = 0.35f)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (activeLang == "TR") "Kat ${entry.floor} Güncesi" else "Floor ${entry.floor} History",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    val shiftSymbol = when {
                                         entry.alignmentImpact > 0 -> "✨ +${entry.alignmentImpact} Momentum"
                                         entry.alignmentImpact < 0 -> "🔮 ${entry.alignmentImpact} Momentum"
                                        else -> "⚖️ Neutral"
                                    }
                                    val shiftSymbolTr = when {
                                         entry.alignmentImpact > 0 -> "✨ +${entry.alignmentImpact} Momentum"
                                         entry.alignmentImpact < 0 -> "🔮 ${entry.alignmentImpact} Momentum"
                                        else -> "⚖️ Nötr"
                                    }
                                    Text(
                                        text = if (activeLang == "TR") shiftSymbolTr else shiftSymbol,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (entry.alignmentImpact > 0) SanctumGold else if (entry.alignmentImpact < 0) VoidNeonPurple else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (activeLang == "TR") entry.actionTakenTr else entry.actionTakenEs,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif, lineHeight = 16.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Localized elapsed/formatted timestamp indicator
                                Text(
                                    text = formatTimestamp(entry.timestamp, activeLang),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Light),
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
