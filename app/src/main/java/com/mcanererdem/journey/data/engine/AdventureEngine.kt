package com.mcanererdem.journey.data.engine

import kotlin.random.Random
import com.mcanererdem.journey.data.model.*

object AdventureEngine {

    fun generateNodesForFloor(floor: Int, player: PlayerProfile? = null): List<AdventureNode> {
        val blueprint = FloorBlueprintSystem.getBlueprintForFloor(floor, player)
        return blueprint.nodes
    }

    // New key-based generators for the refactored architecture

    private fun generateChestNode(id: String, floor: Int, index: Int): AdventureNode {
        return AdventureNode(
            id = id,
            type = NodeType.CHEST,
            titleKey = "floor.$floor.node.$index.title",
            descriptionKey = "floor.$floor.node.$index.description",
            depth = index,
            choices = listOf(
                NodeChoice(
                    id = "${id}_choice_a",
                    labelKey = "floor.$floor.node.$index.choice_a",
                    journalKey = "floor.$floor.node.$index.choice_a.journal",
                    effects = ChoiceEffects(goldChange = 50, expChange = 15, hpChange = -5)
                ),
                NodeChoice(
                    id = "${id}_choice_b",
                    labelKey = "floor.$floor.node.$index.choice_b",
                    journalKey = "floor.$floor.node.$index.choice_b.journal",
                    effects = ChoiceEffects(aetherChange = 20, expChange = 25, rewardItemId = "random_item_tier_1", momentumShift = 5)
                )
            )
        )
    }

    private fun generateNarrativeNode(id: String, floor: Int, index: Int): AdventureNode {
        return AdventureNode(
            id = id,
            type = NodeType.NARRATIVE,
            titleKey = "floor.$floor.node.$index.title",
            descriptionKey = "floor.$floor.node.$index.description",
            depth = index,
            choices = listOf(
                NodeChoice(
                    id = "${id}_choice_a",
                    labelKey = "floor.$floor.node.$index.choice_a",
                    journalKey = "floor.$floor.node.$index.choice_a.journal",
                    effects = ChoiceEffects(hpChange = -10, aetherChange = 40, momentumShift = 6, expChange = 25)
                ),
                NodeChoice(
                    id = "${id}_choice_b",
                    labelKey = "floor.$floor.node.$index.choice_b",
                    journalKey = "floor.$floor.node.$index.choice_b.journal",
                    effects = ChoiceEffects(goldChange = 40, momentumShift = -6, expChange = 20)
                )
            )
        )
    }

    private fun generateCombatNode(id: String, floor: Int, index: Int, enemyId: String, isBoss: Boolean = false): AdventureNode {
        return AdventureNode(
            id = id,
            type = if (isBoss) NodeType.BOSS else NodeType.COMBAT,
            titleKey = if (isBoss) "enemy.$enemyId.boss_title" else "enemy.$enemyId.name",
            descriptionKey = "enemy.$enemyId.description",
            depth = index,
            enemy = EnemyRef(enemyId = enemyId, isBoss = isBoss)
        )
    }

    fun getRandomLootItem(floor: Int, random: Random): String {
        val items = when {
            floor <= 30 -> listOf("rustblade_dagger", "sporeplated_helm", "scouts_sigil")
            floor <= 70 -> listOf("blightslayer_sabret", "vanguard_aegis", "sanctified_greatsword")
            else -> listOf("sovereign_pentacle", "cosmic_will_band", "blight_purifier")
        }
        return items[random.nextInt(items.size)]
    }
}
