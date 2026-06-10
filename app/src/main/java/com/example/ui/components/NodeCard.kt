package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.data.engine.AdventureNode
import com.example.data.engine.NodeChoice
import com.example.data.engine.NodeType
import com.example.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
// Journey Dark Fantasy — Node Kartı Bileşeni
// Her adventure node için kart UI. Narratif, savaş dışı node'lar için.
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Tek bir adventure node'un içeriğini gösterir.
 * Tip'e göre renk ve stil alır. Seçim butonlarını gösterir.
 *
 * @param node Gösterilecek AdventureNode
 * @param lang "EN" veya "TR"
 * @param factionSide Aktif faction (buton rengini belirler)
 * @param onChoiceSelected Seçim yapıldığında çağrılır
 * @param isCompleted Bu node tamamlandı mı? (göre farklı UI)
 * @param pastDecisionLabel Tamamlanmışsa, geçmişte alınan karar metni
 */
@Composable
fun NodeCard(
    node: AdventureNode,
    lang: String,
    factionSide: String,
    onChoiceSelected: (NodeChoice) -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false,
    pastDecisionLabel: String? = null
) {
    val nodeColor = nodeTypeColor(node.type)
    val title = if (lang == "TR") node.titleTr else node.title
    val description = if (lang == "TR") node.descriptionTr else node.description

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(node.index) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(Dimens.AnimNormal)) + slideInVertically(
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            initialOffsetY = { it / 4 }
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.RadiusL))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ColorSurface,
                            nodeColor.copy(alpha = 0.06f)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        width = if (isCompleted) Dimens.BorderThin else Dimens.BorderNormal,
                        color = if (isCompleted) nodeColor.copy(alpha = 0.2f) else nodeColor.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(Dimens.RadiusL)
                )
                .padding(Dimens.SpacingL)
        ) {
            // ── Node Tipi Etiketi ──────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                NodeTypeTag(
                    label = nodeTypeLabel(node.type, lang),
                    color = nodeColor
                )
                if (isCompleted) {
                    NodeTypeTag(
                        label = if (lang == "TR") "TAMAMLANDI" else "DONE",
                        color = ColorHeal.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingM))

            // ── Başlık ────────────────────────────────────────────────
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = nodeColor,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(Dimens.SpacingS))

            // ── Açıklama ──────────────────────────────────────────────
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = ColorOnSurface,
                fontStyle = FontStyle.Italic
            )

            // ── Geçmiş Karar (tamamlanmış node) ──────────────────────
            if (isCompleted && pastDecisionLabel != null) {
                Spacer(Modifier.height(Dimens.SpacingM))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusS))
                        .background(ColorSurfaceHighlight)
                        .padding(Dimens.SpacingM)
                ) {
                    Text(
                        text = "${if (lang == "TR") "KARARIN" else "YOUR DECISION"}: $pastDecisionLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorOnSurfaceMuted,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // ── Seçenekler (tamamlanmamış node) ──────────────────────
            if (!isCompleted) {
                val choices = listOfNotNull(node.optionA, node.optionB, node.optionC)
                if (choices.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.SpacingL))
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
                        choices.forEach { choice ->
                            val choiceText = if (lang == "TR") choice.textTr else choice.textEn
                            NodeChoiceButton(
                                text = choiceText,
                                onClick = { onChoiceSelected(choice) },
                                accentColor = when {
                                    choice.alignmentShift > 0 -> ColorSanctumPrimary
                                    choice.alignmentShift < 0 -> ColorCovenantPrimary
                                    else -> nodeColor
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Yardımcı fonksiyonlar ──────────────────────────────────────────────────

fun nodeTypeColor(type: NodeType) = when (type) {
    NodeType.NARRATIVE -> ColorNodeNarrative
    NodeType.COMBAT    -> ColorNodeCombat
    NodeType.BOSS      -> ColorNodeBoss
    NodeType.CHEST     -> ColorNodeChest
    NodeType.SHRINE    -> ColorNodeShrine
    NodeType.MERCHANT  -> ColorNodeMerchant
}

fun nodeTypeLabel(type: NodeType, lang: String) = when (type) {
    NodeType.NARRATIVE -> if (lang == "TR") "ANLATI" else "NARRATIVE"
    NodeType.COMBAT    -> if (lang == "TR") "SAVAŞ"  else "COMBAT"
    NodeType.BOSS      -> if (lang == "TR") "PATRON" else "BOSS"
    NodeType.CHEST     -> if (lang == "TR") "HAZİNE" else "TREASURE"
    NodeType.SHRINE    -> if (lang == "TR") "SUNAK"  else "SHRINE"
    NodeType.MERCHANT  -> if (lang == "TR") "TÜCCAR" else "MERCHANT"
}
