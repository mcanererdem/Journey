package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.PlayerProfile

object FloorStateManager {

    /**
     * Represents a discrete floor objective.
     */
    data class FloorObjective(
        val id: String,
        val textEn: String,
        val textTr: String,
        val isCompleted: Boolean,
        val nodeIndex: Int? = null // if tied to a specific node index
    )

    /**
     * Gets progress stats (completed nodes, total nodes) for an active floor.
     */
    fun getFloorProgress(floor: Int, player: PlayerProfile): Pair<Int, Int> {
        val blueprint = FloorBlueprintSystem.getBlueprintForFloor(floor, player)
        val totalNodes = blueprint.nodes.size
        
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
                textEn = "Resolve the gateway crisis: '${blueprint.introScenario.titleEn}'",
                textTr = "Yükseliş geçidini çözümle: '${blueprint.introScenario.titleTr}'",
                isCompleted = introScenarioCompleted
            )
        )

        // 2. Node objectives from the JSON blueprint
        blueprint.nodes.forEach { node ->
            val nodeCompleted = player.currentFloor > floor || 
                    (player.currentFloor == floor && (player.currentNodeIndex > node.index || (player.currentNodeIndex == node.index && player.currentNodeCompleted)))
            
            val nodeTypePrefixEn = when (node.type) {
                NodeType.COMBAT -> "Combat: Conquer"
                NodeType.BOSS -> "Apex Boss: Defeat"
                NodeType.CHEST -> "Exploration: Loot"
                NodeType.SHRINE -> "Sacrament: Pay homage at"
                NodeType.MERCHANT -> "Trade: Interact with"
                NodeType.NARRATIVE -> "Narrative: Witness"
                NodeType.CAMP -> "Camp: Rest at"
                NodeType.EVENT -> "Event: Participate in"
                NodeType.SECRET -> "Secret: Discover"
            }
            val nodeTypePrefixTr = when (node.type) {
                NodeType.COMBAT -> "Dövüş: Alt Et"
                NodeType.BOSS -> "Zirve Canavarı: Yen"
                NodeType.CHEST -> "Keşif: Yağmala"
                NodeType.SHRINE -> "Kutsal Alan: Bağ kur"
                NodeType.MERCHANT -> "Ticaret: Pazarlık yap"
                NodeType.NARRATIVE -> "Hikaye: Şahit ol"
                NodeType.CAMP -> "Kamp: Dinlen"
                NodeType.EVENT -> "Etkinlik: Katıl"
                NodeType.SECRET -> "Gizem: Keşfet"
            }

            objectives.add(
                FloorObjective(
                    id = "node_obj_${floor}_${node.index}",
                    textEn = "$nodeTypePrefixEn '${node.title}'",
                    textTr = "$nodeTypePrefixTr '${node.titleTr}'",
                    isCompleted = nodeCompleted,
                    nodeIndex = node.index
                )
            )
        }

        return objectives
    }

    /**
     * Formats objective list for simple progression tracking
     */
    fun getFormattedObjectiveList(floor: Int, player: PlayerProfile, isTr: Boolean): List<String> {
        val objs = getObjectivesForFloor(floor, player)
        return objs.map { obj ->
            val status = if (obj.isCompleted) "✅" else "⏳"
            val text = if (isTr) obj.textTr else obj.textEn
            "$status $text"
        }
    }

    /**
     * Checks if all objectives/boss on the current floor are finished, permitting ascension.
     */
    fun canAscendToNextFloor(player: PlayerProfile): Boolean {
        val currentFloor = player.currentFloor
        val blueprint = FloorBlueprintSystem.getBlueprintForFloor(currentFloor, player)
        val lastNodeIndex = blueprint.nodes.size - 1
        
        // Player must have completed the last node of their current floor
        return player.currentNodeIndex >= lastNodeIndex && player.currentNodeCompleted
    }

    /**
     * Result of attempting to transition to a target floor.
     */
    sealed class TransitionResult {
        data class Success(
            val updatedProfile: PlayerProfile,
            val messageEn: String,
            val messageTr: String,
            val journalEn: String,
            val journalTr: String
        ) : TransitionResult()

        data class Failure(
            val reasonEn: String,
            val reasonTr: String
        ) : TransitionResult()
    }

    /**
     * Centralized transition manager for Floor 1 to Floor 3, checking requirements,
     * deducting costs cleanly, and updating player properties.
     */
    fun attemptFloorTransition(player: PlayerProfile, targetFloor: Int): TransitionResult {
        if (targetFloor !in 1..3) {
            return TransitionResult.Failure(
                reasonEn = "Direct state transitions restricted to Handcrafted Floors 1-3.",
                reasonTr = "Doğrudan bölge geçişleri el yapımı 1-3. Katlar arasında sınırlandırılmıştır."
            )
        }

        val currentFloor = player.currentFloor
        if (targetFloor == currentFloor) {
            return TransitionResult.Failure(
                reasonEn = "Already on Floor $currentFloor.",
                reasonTr = "$currentFloor. Katta bulunuyorsunuz."
            )
        }

        // Ascending logic
        if (targetFloor > currentFloor) {
            // Must have cleared previous floors sequentially
            if (targetFloor != currentFloor + 1) {
                return TransitionResult.Failure(
                    reasonEn = "Sequentially ascend floor by floor. Clear current floor first.",
                    reasonTr = "Sırayla, kat kat yükselmelisiniz. Önce mevcut katı tamamlayın."
                )
            }

            if (!canAscendToNextFloor(player)) {
                val blueprint = FloorBlueprintSystem.getBlueprintForFloor(currentFloor, player)
                val bossNode = blueprint.nodes.lastOrNull()
                val bossName = bossNode?.title ?: "Boss"
                val bossNameTr = bossNode?.titleTr ?: "Lider"
                return TransitionResult.Failure(
                    reasonEn = "Defeat the Apex Boss '$bossName' of Floor $currentFloor before ascending.",
                    reasonTr = "Bir üst kata geçmeden önce $currentFloor. Katın Zirve Canavarı '$bossNameTr' liderini yenmelisiniz."
                )
            }
        }

        // Will cost calculation: transitioning floors costs 2 Will (unless holding "Seasonal Sovereign Pass")
        val hasPass = player.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }
        val transitionCost = if (hasPass) 0 else 2

        if (player.currentWill < transitionCost) {
            return TransitionResult.Failure(
                reasonEn = "Insufficient Will. Requires $transitionCost Will to transcend floor boundaries.",
                reasonTr = "Yetersiz İrade gücü. Katlar arası geçiş yapmak için $transitionCost İrade gerekir."
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

        val directionWordEn = if (targetFloor > currentFloor) "ascended" else "backtracked"
        val directionWordTr = if (targetFloor > currentFloor) "yükseldi" else "geri döndü"

        val msgEn = "Successfully transitioned to Floor $targetFloor."
        val msgTr = "Başarıyla $targetFloor. Kata geçiş yaptınız."

        val journalEn = "Transitioned floors from $currentFloor to $targetFloor. Will cost: $transitionCost. Direction: $directionWordEn."
        val journalTr = "$currentFloor. Kattan $targetFloor. Kata geçiş yapıldı. İrade bedeli: $transitionCost. Hareket: $directionWordTr."

        return TransitionResult.Success(
            updatedProfile = updated,
            messageEn = msgEn,
            messageTr = msgTr,
            journalEn = journalEn,
            journalTr = journalTr
        )
    }
}
