package com.mcanererdem.journey.data.model

enum class NodeType {
    NARRATIVE,
    COMBAT,
    BOSS,
    CHEST,
    SHRINE,
    MERCHANT,
    CAMP,
    EVENT,
    SECRET
}

enum class NodePath {
    LIGHT,
    DARK,
    SHARED
}

enum class ChoiceWeight {
    TRIVIAL,
    MINOR,
    MODERATE,
    MAJOR,
    HEAVY
}

enum class EnemyForm {
    NEUTRAL,
    LIGHT_FORM,
    DARK_FORM
}

enum class SecretConditionType {
    HAS_FLAG,
    HAS_TITLE,
    HAS_ITEM,
    MOMENTUM_RANGE,
    FLOOR_CLEARED,
    BOSS_DEFEATED,
    STAT_CHECK
}

enum class FloorType {
    NORMAL,
    SPECIAL,
    HUB
}

data class NodePrereq(
    val requiredPath: NodePath? = null,
    val minMomentum: Int? = null,
    val maxMomentum: Int? = null,
    val minLevel: Int? = null,
    val requiredTitleId: String = "",
    val requiredItemId: String = "",
    val requiredFlag: String = "",
    val excludesFlag: String = ""
)

data class ChoicePrereq(
    val minMomentum: Int? = null,
    val maxMomentum: Int? = null,
    val minLevel: Int? = null,
    val minHp: Int? = null,
    val requiredTitleId: String = "",
    val requiredItemId: String = "",
    val requiredFlag: String = "",
    val excludesFlag: String = ""
)

data class SecretCondition(
    val type: SecretConditionType,
    val value: String = "",
    val minValue: Int = 0,
    val successNodeId: String = "",
    val failNodeId: String = ""
)

data class MerchantStockEntry(
    val itemId: String,
    val baseCost: Int,
    val currency: String = "GOLD",
    val minMomentum: Int? = null,
    val maxMomentum: Int? = null,
    val requiredTitleId: String = "",
    val requiredItemId: String = "",
    val discountPercent: Int = 0,
    val premiumPercent: Int = 0
)

data class MerchantRef(
    val merchantId: String,
    val stock: List<MerchantStockEntry> = emptyList()
)

data class CampRef(
    val campId: String,
    val freeHealAmount: Int = 20,
    val paidHealAmount: Int = 40,
    val paidHealCost: Int = 30,
    val willRestoreAmount: Int = 2,
    val hasMiniMerchant: Boolean = false,
    val miniMerchantId: String = ""
)

data class AdventureNode(
    val id: String,              // e.g., "floor_1_node_0"
    val type: NodeType,
    val titleKey: String,        // e.g., "floor.1.node.0.title"
    val descriptionKey: String,  // e.g., "floor.1.node.0.description"
    val depth: Int = 0,
    val column: Int = 0,
    val enemy: EnemyRef? = null, // Reference only, no text
    val choices: List<NodeChoice> = emptyList(),
    val willCost: Int = 0,
    val path: NodePath = NodePath.SHARED,
    val chainId: String? = null,
    val chainNext: String? = null,
    val chainExit: Boolean = false,
    val prereq: NodePrereq? = null,
    val merchantRef: MerchantRef? = null,
    val campRef: CampRef? = null,
    val secretCondition: SecretCondition? = null
)

data class NodeChoice(
    val id: String,
    val labelKey: String,        // e.g., "floor.1.node.0.choice_a"
    val journalKey: String,      // e.g., "floor.1.node.0.choice_a.journal"
    val effects: ChoiceEffects,
    val prereq: ChoicePrereq? = null,
    val isHidden: Boolean = false,
    val isIrreversible: Boolean = false,
    val weight: ChoiceWeight = ChoiceWeight.MINOR,
    val nextChainNodeId: String? = null
)

data class ChoiceEffects(
    val hpChange: Int = 0,
    val goldChange: Int = 0,
    val aetherChange: Int = 0,
    val expChange: Int = 0,
    val momentumShift: Int = 0,
    val willChange: Int = 0,
    val rewardItemId: String = "",
    val rewardTitleId: String = "",
    val requiredFlag: String = "",
    val setsFlag: String = "",
    val removesFlag: String = "",
    val consequenceRing: Int = 0,
    val consequenceKey: String = "",
    val triggerChainId: String = "",
    val skipToBoss: Boolean = false,
    val skipToNextFloor: Boolean = false
)

data class EnemyRef(
    val enemyId: String,     // Lookup from global_enemies.json
    val isBoss: Boolean = false,
    val form: EnemyForm = EnemyForm.NEUTRAL,
    val scaleFactor: Float = 1.0f,
    val overrideHp: Int? = null,
    val overrideAtk: Int? = null
)

typealias FloorNode = AdventureNode

data class NodeChain(
    val chainId: String,
    val nodes: List<FloorNode>,
    val exitToPath: Boolean = true
)

data class FloorScenario(
    val floor: Int,
    val titleKey: String,
    val descriptionKey: String,
    val options: List<GameOption>
)

data class GameOption(
    val id: String,
    val labelKey: String,
    val journalKey: String,
    val effects: ChoiceEffects
)

data class FloorBlueprint(
    val floor: Int,
    val region: String,
    val type: FloorType,
    val titleKey: String,
    val descriptionKey: String,
    val minSecondsOnFloor: Int = 0,
    val intro: FloorNode,
    val pathLight: List<FloorNode> = emptyList(),
    val pathDark: List<FloorNode> = emptyList(),
    val shared: List<FloorNode> = emptyList(),
    val chains: List<NodeChain> = emptyList(),
    val boss: EnemyRef? = null
) {
    // Computed property for backward compatibility with systems expecting a single list
    // Now including intro as the first node
    val allNodes: List<FloorNode> get() = listOf(intro) + shared + pathLight + pathDark
}
