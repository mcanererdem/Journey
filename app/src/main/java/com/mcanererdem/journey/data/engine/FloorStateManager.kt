package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.*

object FloorStateManager {

    /**
     * Represents a discrete floor objective.
     */
    data class FloorObjective(
        val id: String,
        val textKey: String,
        val textArgs: List<Any> = emptyList(),
        val isCompleted: Boolean,
        val nodeIndex: Int? = null // if tied to a specific node index
    )

    fun FloorObjective.getText(lang: String): String {
        val translatedArgs = textArgs.map { arg ->
            if (arg is String && arg.contains(".")) {
                LocalizationManager.getString(lang, arg)
            } else {
                arg.toString()
            }
        }.toTypedArray()
        
        val formatString = LocalizationManager.getString(lang, textKey)
        return try {
            String.format(formatString, *translatedArgs)
        } catch (e: Exception) {
            formatString
        }
    }

    /**
     * Gets progress stats (completed nodes, total nodes) for an active floor.
     */
    fun getFloorProgress(floor: Int, player: PlayerProfile): Pair<Int, Int> {
        val blueprint = FloorBlueprintSystem.getBlueprintForFloor(floor, player)
        val totalNodes = blueprint.allNodes.size
        
        return when {
            player.currentFloor > floor -> Pair(totalNodes, totalNodes)
            player.currentFloor < floor -> Pair(0, totalNodes)
            else -> {
                // Same floor
                var completedCount = player.currentNodeIndex
                if (player.currentNodeCompleted) {
                    completedCount++
                }
                Pair(completedCount.coerceAtMost(totalNodes), totalNodes)
            }
        }
    }

    /**
     * Returns the list of objectives for the specified floor and their completion status.
     */
    fun getObjectivesForFloor(floor: Int, player: PlayerProfile): List<FloorObjective> {
        if (floor !in 1..3) return emptyList()
        val blueprint = FloorBlueprintSystem.getBlueprintForFloor(floor, player)
        val objectives = mutableListOf<FloorObjective>()

        // 1. Floor Entry Scenario objective
        val introScenarioCompleted = player.currentFloor > floor || 
                (player.currentFloor == floor && (player.currentNodeIndex > 0 || player.currentNodeCompleted))
                
        objectives.add(
            FloorObjective(
                id = "intro_scenario_floor_$floor",
                textKey = "ui.objective_intro_format",
                textArgs = listOf(blueprint.intro.titleKey),
                isCompleted = introScenarioCompleted
            )
        )

        // 2. Node objectives
        blueprint.allNodes.forEachIndexed { index, node ->
            val nodeCompleted = player.currentFloor > floor || 
                    (player.currentFloor == floor && (player.currentNodeIndex > index || (player.currentNodeIndex == index && player.currentNodeCompleted)))
            
            val nodeTypePrefixKey = when (node.type) {
                NodeType.COMBAT -> "ui.node_prefix_combat"
                NodeType.BOSS -> "ui.node_prefix_boss"
                NodeType.CHEST -> "ui.node_prefix_chest"
                NodeType.SHRINE -> "ui.node_prefix_shrine"
                NodeType.MERCHANT -> "ui.node_prefix_merchant"
                NodeType.NARRATIVE -> "ui.node_prefix_narrative"
                NodeType.CAMP -> "ui.node_prefix_camp"
                NodeType.EVENT -> "ui.node_prefix_event"
                NodeType.SECRET -> "ui.node_prefix_secret"
            }

            objectives.add(
                FloorObjective(
                    id = "node_obj_${floor}_$index",
                    textKey = "ui.objective_node_format",
                    textArgs = listOf(nodeTypePrefixKey, node.titleKey),
                    isCompleted = nodeCompleted,
                    nodeIndex = index
                )
            )
        }

        return objectives
    }

    /**
     * Formats objective list for simple progression tracking
     */
    fun getFormattedObjectiveList(floor: Int, player: PlayerProfile, isTr: Boolean): List<String> {
        val lang = if (isTr) "TR" else "EN"
        val objs = getObjectivesForFloor(floor, player)
        return objs.map { obj ->
            val status = if (obj.isCompleted) "✅" else "⏳"
            val text = obj.getText(lang)
            "$status $text"
        }
    }

    /**
     * Checks if all objectives/boss on the current floor are finished, permitting ascension.
     */
    fun canAscendToNextFloor(player: PlayerProfile): Boolean {
        val currentFloor = player.currentFloor
        val blueprint = FloorBlueprintSystem.getBlueprintForFloor(currentFloor, player)
        val lastNodeIndex = blueprint.allNodes.size - 1
        
        // Player must have completed the last node of their current floor
        return player.currentNodeIndex >= lastNodeIndex && player.currentNodeCompleted
    }

    /**
     * Result of attempting to transition to a target floor.
     */
    sealed class TransitionResult {
        data class Success(
            val updatedProfile: PlayerProfile,
            val messageKey: String,
            val messageArgs: List<Any> = emptyList(),
            val journalKey: String,
            val journalArgs: List<Any> = emptyList()
        ) : TransitionResult()

        data class Failure(
            val reasonKey: String,
            val reasonArgs: List<Any> = emptyList()
        ) : TransitionResult()
    }

    /**
     * Centralized transition manager for Floor 1 to Floor 3, checking requirements,
     * deducting costs cleanly, and updating player properties.
     */
    fun attemptFloorTransition(player: PlayerProfile, targetFloor: Int): TransitionResult {
        if (targetFloor !in 1..3) {
            return TransitionResult.Failure(
                reasonKey = "ui.transition_fail_restricted"
            )
        }

        val currentFloor = player.currentFloor
        if (targetFloor == currentFloor) {
            return TransitionResult.Failure(
                reasonKey = "ui.transition_fail_already_there",
                reasonArgs = listOf(currentFloor)
            )
        }

        // Ascending logic
        if (targetFloor > currentFloor) {
            // Must have cleared previous floors sequentially
            if (targetFloor != currentFloor + 1) {
                return TransitionResult.Failure(
                    reasonKey = "ui.transition_fail_sequential"
                )
            }

            if (!canAscendToNextFloor(player)) {
                val blueprint = FloorBlueprintSystem.getBlueprintForFloor(currentFloor, player)
                val bossNode = blueprint.allNodes.lastOrNull()
                val bossNameKey = bossNode?.titleKey ?: "ui.label_boss"
                return TransitionResult.Failure(
                    reasonKey = "ui.transition_fail_defeat_boss",
                    reasonArgs = listOf(bossNameKey, currentFloor)
                )
            }
        }

        // Will cost calculation: transitioning floors costs 2 Will (unless holding "Seasonal Sovereign Pass")
        val hasPass = player.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }
        val transitionCost = if (hasPass) 0 else 2

        if (player.currentWill < transitionCost) {
            return TransitionResult.Failure(
                reasonKey = "ui.transition_fail_will",
                reasonArgs = listOf(transitionCost)
            )
        }

        val newWill = player.currentWill - transitionCost

        // Calculate new rank
        val nextRank = when {
            targetFloor >= 30 -> "SOVEREIGN"
            targetFloor >= 15 -> "EXARCH"
            targetFloor >= 5 -> "ARBITER"
            else -> "EMISSARY"
        }

        val updated = player.copy(
            currentFloor = targetFloor,
            currentNodeIndex = 0,
            currentNodeCompleted = false,
            currentWill = newWill,
            rank = nextRank,
            lastUpdated = System.currentTimeMillis()
        )

        val directionWordKey = if (targetFloor > currentFloor) "ui.direction_ascended" else "ui.direction_backtracked"

        return TransitionResult.Success(
            updatedProfile = updated,
            messageKey = "ui.transition_success_msg",
            messageArgs = listOf(targetFloor),
            journalKey = "ui.transition_success_journal",
            journalArgs = listOf(currentFloor, targetFloor, transitionCost, directionWordKey)
        )
    }
}
