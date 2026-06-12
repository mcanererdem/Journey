package com.mcanererdem.journey.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mcanererdem.journey.data.engine.AdventureEngine
import com.mcanererdem.journey.data.engine.FloorStateManager
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.model.*
import com.mcanererdem.journey.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FloorViewModel(
    private val repository: GameRepository,
    application: Application,
    private val onClearCombat: () -> Unit,
    private val onMessage: (ActionMessage) -> Unit,
    private val activeLanguage: StateFlow<String>,
    private val onTriggerSpiritFracture: suspend (PlayerProfile, Int, Int, Int, String) -> Unit,
    private val calculatePlayerClass: (String, Int) -> String,
    private val updateDailyQuestProgress: (PlayerProfile, Int, Int) -> PlayerProfile
) : AndroidViewModel(application) {

    private val _currentScenario = MutableStateFlow<FloorScenario?>(null)
    val currentScenario: StateFlow<FloorScenario?> = _currentScenario.asStateFlow()

    private val _currentFloorNodes = MutableStateFlow<List<AdventureNode>>(emptyList())
    val currentFloorNodes: StateFlow<List<AdventureNode>> = _currentFloorNodes.asStateFlow()

    private val _scoutedNodeIndices = MutableStateFlow<Set<Int>>(emptySet())
    val scoutedNodeIndices: StateFlow<Set<Int>> = _scoutedNodeIndices.asStateFlow()

    fun updateScenario(scenario: FloorScenario?) {
        _currentScenario.value = scenario
    }

    fun updateNodes(nodes: List<AdventureNode>) {
        _currentFloorNodes.value = nodes
    }

    fun clearScoutedNodes() {
        _scoutedNodeIndices.value = emptySet()
    }

    fun ascendToNextFloor() {
        viewModelScope.launch {
            var profile = repository.getPlayerProfileDirect() ?: return@launch
            
            val hasPass = profile.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }

            if (!hasPass && profile.currentWill < 2) {
                onMessage(ActionMessage("msg_climb_will_cost"))
                return@launch
            }

            val nextFloor = profile.currentFloor + 1
            if (nextFloor > 100) {
                val updated = profile.copy(
                    currentFloor = 101,
                    currentNodeIndex = 0,
                    currentNodeCompleted = true,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                onMessage(ActionMessage("msg_ascend_victory"))
                return@launch
            }

            val isCheckpoint = (nextFloor % 10 == 0)
            val newCheckpoint = if (isCheckpoint) nextFloor else profile.savedFloorCheckpoint
            val nextRank = calculateRank(nextFloor)

            val willSpent = if (hasPass) 0 else 2
            if (willSpent > 0) {
                profile = updateDailyQuestProgress(profile, 2, willSpent)
            }

            val updated = profile.copy(
                currentFloor = nextFloor,
                currentNodeIndex = 0,
                currentNodeCompleted = false,
                currentWill = if (hasPass) profile.currentWill else profile.currentWill - 2,
                savedFloorCheckpoint = newCheckpoint,
                rank = nextRank,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            
            onClearCombat()
            
            onMessage(ActionMessage("msg_ascend_success", listOf(nextFloor)))
        }
    }

    fun initiateTransitionToFloor(targetFloor: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            when (val res = FloorStateManager.attemptFloorTransition(profile, targetFloor)) {
                is FloorStateManager.TransitionResult.Success -> {
                    repository.savePlayerProfile(res.updatedProfile)
                    onClearCombat()
                    
                    val journalEsArgs = res.journalArgs.map { arg ->
                        if (arg is String && arg.contains(".")) LocalizationManager.getString("EN", arg) else arg.toString()
                    }.toTypedArray()
                    val journalTrArgs = res.journalArgs.map { arg ->
                        if (arg is String && arg.contains(".")) LocalizationManager.getString("TR", arg) else arg.toString()
                    }.toTypedArray()
                    
                    val actionEs = try {
                        String.format(LocalizationManager.getString("EN", res.journalKey), *journalEsArgs)
                    } catch (e: Exception) {
                        LocalizationManager.getString("EN", res.journalKey)
                    }
                    val actionTr = try {
                        String.format(LocalizationManager.getString("TR", res.journalKey), *journalTrArgs)
                    } catch (e: Exception) {
                        LocalizationManager.getString("TR", res.journalKey)
                    }

                    val logEntry = JournalEntry(
                        floor = profile.currentFloor,
                        actionTakenEs = actionEs,
                        actionTakenTr = actionTr,
                        sideAlignmentShift = profile.side,
                        alignmentImpact = 0
                    )
                    repository.insertJournalEntry(logEntry)
                    
                    val msgEsArgs = res.messageArgs.map { arg ->
                        if (arg is String && arg.contains(".")) LocalizationManager.getString("EN", arg) else arg.toString()
                    }.toTypedArray()
                    val msgTrArgs = res.messageArgs.map { arg ->
                        if (arg is String && arg.contains(".")) LocalizationManager.getString("TR", arg) else arg.toString()
                    }.toTypedArray()
                    
                    val messageEs = try {
                        String.format(LocalizationManager.getString("EN", res.messageKey), *msgEsArgs)
                    } catch (e: Exception) {
                        LocalizationManager.getString("EN", res.messageKey)
                    }
                    val messageTr = try {
                        String.format(LocalizationManager.getString("TR", res.messageKey), *msgTrArgs)
                    } catch (e: Exception) {
                        LocalizationManager.getString("TR", res.messageKey)
                    }

                    onMessage(ActionMessage(res.messageKey, res.messageArgs))
                }
                is FloorStateManager.TransitionResult.Failure -> {
                    val reasonEsArgs = res.reasonArgs.map { arg ->
                        if (arg is String && arg.contains(".")) LocalizationManager.getString("EN", arg) else arg.toString()
                    }.toTypedArray()
                    val reasonTrArgs = res.reasonArgs.map { arg ->
                        if (arg is String && arg.contains(".")) LocalizationManager.getString("TR", arg) else arg.toString()
                    }.toTypedArray()
                    
                    val reasonEs = try {
                        String.format(LocalizationManager.getString("EN", res.reasonKey), *reasonEsArgs)
                    } catch (e: Exception) {
                        LocalizationManager.getString("EN", res.reasonKey)
                    }
                    val reasonTr = try {
                        String.format(LocalizationManager.getString("TR", res.reasonKey), *reasonTrArgs)
                    } catch (e: Exception) {
                        LocalizationManager.getString("TR", res.reasonKey)
                    }
                    
                    onMessage(ActionMessage(res.reasonKey, res.reasonArgs))
                }
            }
        }
    }

    fun selectNodeChoice(choice: NodeChoice) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val effects = choice.effects
            
            val hasPass = profile.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }
            val resolvedWillChange = if (hasPass && effects.willChange < 0) 0 else effects.willChange

            if (!hasPass && resolvedWillChange < 0 && profile.currentWill < -resolvedWillChange) {
                onMessage(ActionMessage("msg_climb_will_cost"))
                return@launch
            }

            val greedLvl = LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, LegacyUpgradeType.GREED)
            val greedMultiplier = 1.0f + (greedLvl * 0.20f)
            val scaledGoldChange = if (effects.goldChange > 0) (effects.goldChange * greedMultiplier).toInt() else effects.goldChange

            val recoveryLvl = LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, LegacyUpgradeType.RECOVERY)
            val recoveryMultiplier = 1.0f + (recoveryLvl * 0.15f)
            val scaledHpChange = if (effects.hpChange > 0) (effects.hpChange * recoveryMultiplier).toInt() else effects.hpChange

            val newMomentum = (profile.momentum + effects.alignmentShift).coerceIn(0, 100)
            val newGold = (profile.gold + scaledGoldChange).coerceAtLeast(0)
            val newAether = (profile.aether + effects.aetherChange).coerceAtLeast(0)
            val newWill = (profile.currentWill + resolvedWillChange).coerceIn(0, profile.maxWill)
            var newHp = profile.currentHp + scaledHpChange
            
            var currentItems = if (profile.itemsEncoded.isEmpty()) emptyList() else profile.itemsEncoded.split(",")
            if (effects.rewardItemId.isNotEmpty()) {
                currentItems = currentItems + effects.rewardItemId
            }
            val newItemsEncoded = currentItems.joinToString(",")

            var currentFlags = if (profile.storyFlagsEncoded.isEmpty()) emptyList() else profile.storyFlagsEncoded.split(",")
            if (effects.setsFlag.isNotEmpty() && !currentFlags.contains(effects.setsFlag)) {
                currentFlags = currentFlags + effects.setsFlag
            }
            val newStoryFlagsEncoded = currentFlags.joinToString(",")

            var currentTitles = if (profile.titlesEncoded.isEmpty()) emptyList() else profile.titlesEncoded.split(",")
            if (effects.rewardTitleId.isNotEmpty()) {
                currentTitles = currentTitles + effects.rewardTitleId
            }
            val newTitlesEncoded = currentTitles.joinToString(",")

            var newExp = profile.exp + effects.expChange
            var newLevel = profile.level
            var newMaxExp = profile.maxExp
            var newMaxHp = profile.maxHp
            while (newExp >= newMaxExp && newLevel < 100) {
                newExp -= newMaxExp
                newLevel++
                newMaxExp = newLevel * 100
                newMaxHp += 20
                newHp += 20
            }

            val activeFactionSide = if (profile.side == "NEUTRAL" && effects.alignmentShift != 0) {
                if (effects.alignmentShift > 0 && newMomentum > 70) "SANCTUM"
                else if (effects.alignmentShift < 0 && newMomentum < 30) "COVENANT"
                else "NEUTRAL"
            } else {
                profile.side
            }

            val logEntry = JournalEntry(
                floor = profile.currentFloor,
                actionTakenEs = LocalizationManager.getString("EN", choice.journalKey),
                actionTakenTr = LocalizationManager.getString("TR", choice.journalKey),
                sideAlignmentShift = if (effects.alignmentShift > 0) "SANCTUM" else if (effects.alignmentShift < 0) "COVENANT" else "NEUTRAL",
                alignmentImpact = effects.alignmentShift,
                nodeIndex = profile.currentNodeIndex
            )
            repository.insertJournalEntry(logEntry)

            onMessage(ActionMessage(choice.journalKey))

            val willSpent = if (hasPass) 0 else -resolvedWillChange
            val profileWithQuest = if (willSpent > 0) updateDailyQuestProgress(profile, 2, willSpent) else profile

            if (newHp <= 0) {
                onTriggerSpiritFracture(profileWithQuest, newMomentum, newGold, newAether, activeFactionSide)
            } else {
                var targetFloor = profileWithQuest.currentFloor
                var targetNodeIndex = profileWithQuest.currentNodeIndex
                var completedState = true
                var targetCheckpoint = profileWithQuest.savedFloorCheckpoint
                var targetRank = profileWithQuest.rank

                if (effects.skipToNextFloor) {
                    targetFloor = (profileWithQuest.currentFloor + 1).coerceAtMost(100)
                    targetNodeIndex = 0
                    completedState = false
                    targetCheckpoint = if (targetFloor % 10 == 1) targetFloor else profileWithQuest.savedFloorCheckpoint
                    targetRank = calculateRank(targetFloor)
                    
                    val nodes = AdventureEngine.generateNodesForFloor(targetFloor, profileWithQuest)
                    _currentFloorNodes.value = nodes
                } else if (effects.skipToBoss) {
                    val nodes = _currentFloorNodes.value
                    if (nodes.isNotEmpty()) {
                        targetNodeIndex = nodes.size - 1
                        completedState = false
                    }
                } else {
                    val nodes = _currentFloorNodes.value
                    if (profileWithQuest.currentNodeIndex + 1 < nodes.size) {
                        targetNodeIndex = profileWithQuest.currentNodeIndex + 1
                        completedState = false
                    } else {
                        completedState = true
                    }
                }

                val updated = profileWithQuest.copy(
                    currentFloor = targetFloor,
                    currentNodeIndex = targetNodeIndex,
                    currentNodeColumn = if (effects.skipToNextFloor) 0 else profileWithQuest.currentNodeColumn,
                    currentNodeCompleted = completedState,
                    savedFloorCheckpoint = targetCheckpoint,
                    rank = targetRank,
                    momentum = newMomentum,
                    gold = newGold,
                    aether = newAether,
                    currentHp = newHp.coerceAtMost(newMaxHp),
                    maxHp = newMaxHp,
                    currentWill = newWill,
                    level = newLevel,
                    exp = newExp,
                    maxExp = newMaxExp,
                    itemsEncoded = newItemsEncoded,
                    titlesEncoded = newTitlesEncoded,
                    storyFlagsEncoded = newStoryFlagsEncoded,
                    chosenClass = calculatePlayerClass(activeFactionSide, newMomentum),
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
            }
        }
    }

    fun selectNodeAt(depth: Int, column: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val nodes = _currentFloorNodes.value
            if (depth !in nodes.indices) return@launch
            val targetNode = nodes[depth]
            
            if (targetNode.willCost > profile.currentWill) {
                onMessage(ActionMessage("msg_scout_no_will"))
                return@launch
            }
            
            val willSpent = targetNode.willCost
            val profileWithQuest = if (willSpent > 0) updateDailyQuestProgress(profile, 2, willSpent) else profile

            val updated = profileWithQuest.copy(
                currentNodeIndex = depth,
                currentNodeColumn = column,
                currentNodeCompleted = false,
                currentWill = (profileWithQuest.currentWill - targetNode.willCost).coerceAtLeast(0),
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            onClearCombat()
        }
    }

    fun performScouting() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val scoutCost = 1
            if (profile.currentWill < scoutCost) {
                onMessage(ActionMessage("msg_scout_no_will"))
                return@launch
            }

            val unScouted = mutableListOf<Int>()
            _currentFloorNodes.value.forEachIndexed { idx, node ->
                if (!_scoutedNodeIndices.value.contains(idx) && idx > profile.currentNodeIndex) {
                    unScouted.add(idx)
                }
            }

            if (unScouted.isEmpty()) {
                onMessage(ActionMessage("msg_all_sectors_scouted"))
                return@launch
            }

            val revealIndex = unScouted.random()
            _scoutedNodeIndices.value = _scoutedNodeIndices.value + revealIndex

            val profileWithQuest = updateDailyQuestProgress(profile, 2, scoutCost)
            val updated = profileWithQuest.copy(
                currentWill = (profileWithQuest.currentWill - scoutCost).coerceAtLeast(0),
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)

            val revealedNode = _currentFloorNodes.value[revealIndex]
            val typeDesc = when (revealedNode.type) {
                NodeType.COMBAT -> "Combat Skirmish"
                NodeType.BOSS -> "Apex Floor Guardian"
                NodeType.CHEST -> "Treasure Chest Cache"
                NodeType.SHRINE -> "Aetherial Shrine"
                NodeType.MERCHANT -> "Wandering Dealer"
                NodeType.CAMP -> "Safety Camp Haven"
                NodeType.EVENT -> "Mystic Event Portal"
                NodeType.SECRET -> "Veiled Anomaly"
                else -> "Narrative Ruins"
            }
            val typeDescTr = when (revealedNode.type) {
                NodeType.COMBAT -> "Düşman Pususu"
                NodeType.BOSS -> "Kat Muhafızı Canavar"
                NodeType.CHEST -> "Hazine Sandığı"
                NodeType.SHRINE -> "Mistik Sunak"
                NodeType.MERCHANT -> "Gezgin Satıcı"
                NodeType.CAMP -> "Güvenli Dinlenme Kampı"
                NodeType.EVENT -> "Gizemli Etkinlik Geçidi"
                NodeType.SECRET -> "Örtülü Anomali"
                else -> "Tarihi Kalıntılar"
            }

            onMessage(
                ActionMessage(
                    "msg_scout_success",
                    listOf(revealIndex, "ui.node_type_${revealedNode.type.name.lowercase()}")
                )
            )
        }
    }

    fun performAbyssScouting() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val cost = 20
            if (profile.aether < cost) {
                onMessage(ActionMessage("msg_scout_no_aether"))
                return@launch
            }

            val updated = profile.copy(
                aether = profile.aether - cost,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)

            val roll = (1..100).random()
            if (roll > 50) {
                val foundGold = 45 + (1..15).random()
                val innerUpdated = updated.copy(gold = updated.gold + foundGold)
                repository.savePlayerProfile(innerUpdated)
                onMessage(ActionMessage("msg_scout_success_gold", listOf(foundGold)))
            } else {
                onMessage(ActionMessage("msg_scout_failed_mist"))
            }
        }
    }

    fun healAndRest(cost: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            if (profile.gold < cost) {
                onMessage(ActionMessage("msg_rest_no_gold"))
                return@launch
            }
            val healAmount = profile.maxHp / 2
            val updated = profile.copy(
                gold = profile.gold - cost,
                currentHp = (profile.currentHp + healAmount).coerceAtMost(profile.maxHp),
                currentWill = (profile.currentWill + 5).coerceAtMost(profile.maxWill),
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            onMessage(ActionMessage("msg_rest_success", listOf(healAmount, 5)))
        }
    }

    fun tradeCurrency(type: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            if (type == "BUY_AETHER") {
                val cost = 30
                if (profile.gold < cost) {
                    onMessage(ActionMessage("msg_trade_no_gold"))
                    return@launch
                }
                val updated = profile.copy(
                    gold = profile.gold - cost,
                    aether = profile.aether + 10,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                onMessage(ActionMessage("msg_trade_success_aether"))
            }
        }
    }

    fun handleRpgChoice(option: GameOption) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val effects = option.effects

            val greedLvl = LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, LegacyUpgradeType.GREED)
            val greedMultiplier = 1.0f + (greedLvl * 0.20f)
            val scaledGoldChange = if (effects.goldChange > 0) (effects.goldChange * greedMultiplier).toInt() else effects.goldChange

            val recoveryLvl = LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, LegacyUpgradeType.RECOVERY)
            val recoveryMultiplier = 1.0f + (recoveryLvl * 0.15f)
            val scaledHpChange = if (effects.hpChange > 0) (effects.hpChange * recoveryMultiplier).toInt() else effects.hpChange

            val newMomentum = (profile.momentum + effects.alignmentShift).coerceIn(0, 100)
            val newGold = (profile.gold + scaledGoldChange).coerceAtLeast(0)
            val newAether = (profile.aether + effects.aetherChange).coerceAtLeast(0)
            var newHp = profile.currentHp + scaledHpChange

            var targetLevel = profile.level
            var targetExp = profile.exp + effects.expChange
            var targetMaxExp = profile.maxExp
            var targetMaxHp = profile.maxHp

            while (targetExp >= targetMaxExp && targetLevel < 100) {
                targetExp -= targetMaxExp
                targetLevel++
                targetMaxExp = targetLevel * 100
                targetMaxHp += 20
                newHp += 20
            }

            val activeFactionSide = if (profile.side == "NEUTRAL" && effects.alignmentShift != 0) {
                if (effects.alignmentShift > 0 && newMomentum > 70) "SANCTUM"
                else if (effects.alignmentShift < 0 && newMomentum < 30) "COVENANT"
                else "NEUTRAL"
            } else {
                profile.side
            }

            val logEntry = JournalEntry(
                floor = profile.currentFloor,
                actionTakenEs = LocalizationManager.getString("EN", option.journalKey),
                actionTakenTr = LocalizationManager.getString("TR", option.journalKey),
                sideAlignmentShift = if (effects.alignmentShift > 0) "SANCTUM" else if (effects.alignmentShift < 0) "COVENANT" else "NEUTRAL",
                alignmentImpact = effects.alignmentShift,
                nodeIndex = -1
            )
            repository.insertJournalEntry(logEntry)

            onMessage(ActionMessage(option.journalKey))

            if (newHp <= 0) {
                onTriggerSpiritFracture(profile, newMomentum, newGold, newAether, activeFactionSide)
            } else {
                val updated = profile.copy(
                    currentNodeCompleted = true, // Solves scenario phase, unlocking floor nodes
                    momentum = newMomentum,
                    gold = newGold,
                    aether = newAether,
                    currentHp = newHp.coerceAtMost(targetMaxHp),
                    maxHp = targetMaxHp,
                    level = targetLevel,
                    exp = targetExp,
                    maxExp = targetMaxExp,
                    chosenClass = calculatePlayerClass(activeFactionSide, newMomentum),
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
            }
            _currentScenario.value = null
        }
    }

    private fun calculateRank(floor: Int): String {
        return when {
            floor >= 100 -> "SOVEREIGN"
            floor >= 25 -> "EXARCH"
            floor >= 10 -> "ARBITER"
            else -> "EMISSARY"
        }
    }
}
