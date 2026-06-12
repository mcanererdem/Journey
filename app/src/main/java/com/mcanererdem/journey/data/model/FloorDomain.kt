package com.mcanererdem.journey.data.model

import com.mcanererdem.journey.data.engine.NodeType

data class AdventureNode(
    val id: String,              // e.g., "floor_1_node_0"
    val type: NodeType,
    val titleKey: String,        // e.g., "floor.1.node.0.title"
    val descriptionKey: String,  // e.g., "floor.1.node.0.description"
    val depth: Int = 0,
    val column: Int = 0,
    val enemy: EnemyRef? = null, // Reference only, no text
    val choices: List<NodeChoice> = emptyList(),
    val willCost: Int = 0
)

data class NodeChoice(
    val id: String,
    val labelKey: String,        // e.g., "floor.1.node.0.choice_a"
    val journalKey: String,      // e.g., "floor.1.node.0.choice_a.journal"
    val effects: ChoiceEffects
)

data class ChoiceEffects(
    val hpChange: Int = 0,
    val goldChange: Int = 0,
    val aetherChange: Int = 0,
    val expChange: Int = 0,
    val alignmentShift: Int = 0,
    val willChange: Int = 0,
    val rewardItemId: String = "",
    val rewardTitleId: String = "",
    val requiredFlag: String = "",
    val setsFlag: String = "",
    val skipToBoss: Boolean = false,
    val skipToNextFloor: Boolean = false
)

data class EnemyRef(
    val enemyId: String,     // Lookup from global_enemies.json
    val isBoss: Boolean = false
)
