package com.mcanererdem.journey.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mcanererdem.journey.data.engine.*
import com.mcanererdem.journey.data.model.*
import com.mcanererdem.journey.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class CombatViewModel(
    private val repository: GameRepository,
    application: Application,
    private val activeLanguage: StateFlow<String>,
    private val currentFloorNodes: StateFlow<List<AdventureNode>>,
    private val onMessage: (ActionMessage) -> Unit,
    private val onNavigateToTab: (NavigationTab) -> Unit,
    private val onTriggerSpiritFracture: suspend (PlayerProfile, Int, Int, Int, String) -> Unit,
    private val checkAndUnlockTitles: (PlayerProfile) -> PlayerProfile,
    private val calculatePlayerClass: (String, Int) -> String,
    private val updateDailyQuestProgress: (PlayerProfile, Int, Int) -> PlayerProfile
) : AndroidViewModel(application) {

    private val _activeEnemyHp = MutableStateFlow<Int?>(null)
    val activeEnemyHp: StateFlow<Int?> = _activeEnemyHp.asStateFlow()

    private val _combatLog = MutableStateFlow<List<CombatLogEntry>>(emptyList())
    val combatLog: StateFlow<List<CombatLogEntry>> = _combatLog.asStateFlow()

    private val _playerStatuses = MutableStateFlow<List<CombatStatus>>(emptyList())
    val playerStatuses: StateFlow<List<CombatStatus>> = _playerStatuses.asStateFlow()

    private val _enemyStatuses = MutableStateFlow<List<CombatStatus>>(emptyList())
    val enemyStatuses: StateFlow<List<CombatStatus>> = _enemyStatuses.asStateFlow()

    private val _currentEnemyIntent = MutableStateFlow<EnemyIntent>(EnemyIntent.ATTACK)
    val currentEnemyIntent: StateFlow<EnemyIntent> = _currentEnemyIntent.asStateFlow()

    private val _activeNarrativeEvent = MutableStateFlow<NarrativeEvent?>(null)
    val activeNarrativeEvent: StateFlow<NarrativeEvent?> = _activeNarrativeEvent.asStateFlow()

    private val _completedEvents = MutableStateFlow<Set<String>>(emptySet())
    val completedEvents: StateFlow<Set<String>> = _completedEvents.asStateFlow()

    private var hasTriggeredPhase2 = false
    private var lastCombatNodeIndex = -1

    fun clearCombat() {
        _activeEnemyHp.value = null
        _combatLog.value = emptyList()
        _playerStatuses.value = emptyList()
        _enemyStatuses.value = emptyList()
        hasTriggeredPhase2 = false
        lastCombatNodeIndex = -1
    }

    fun clearCompletedEventsAndSlainBosses() {
        _completedEvents.value = emptySet()
    }

    fun addCompletedEvent(eventId: String) {
        _completedEvents.value = _completedEvents.value + eventId
    }

    fun checkAndInitCombat(profile: PlayerProfile, nodes: List<AdventureNode>, lang: String) {
        if (profile.currentNodeIndex in nodes.indices) {
            val activeNode = nodes[profile.currentNodeIndex]
            val isInCombatNode = activeNode.type == NodeType.COMBAT || activeNode.type == NodeType.BOSS
            
            // Force re-init if we changed to a different combat node, or if it's not initialized
            val isNewCombatNode = profile.currentNodeIndex != lastCombatNodeIndex
            val needsInit = isNewCombatNode || _activeEnemyHp.value == null
            
            if (isInCombatNode && !profile.currentNodeCompleted && needsInit) {
                val enemyRef = activeNode.enemy
                val enemyId = enemyRef?.enemyId ?: return
                
                val stats = LocalizationManager.getEnemyStats(enemyId)
                val enemyHp = enemyRef.overrideHp ?: stats?.optInt("hp") ?: 50
                
                _activeEnemyHp.value = enemyHp
                _playerStatuses.value = emptyList()
                _enemyStatuses.value = emptyList()
                _combatLog.value = emptyList() 
                _currentEnemyIntent.value = EnemyIntent.random()
                hasTriggeredPhase2 = false
                lastCombatNodeIndex = profile.currentNodeIndex
                
                val enemyNameKey = stats?.optString("nameKey") ?: "enemy.$enemyId.name"
                val initialLog = CombatLogEntry(
                    key = "ui.combat_log_initiated",
                    args = mapOf("enemy" to enemyNameKey)
                )
                _combatLog.value = listOf(initialLog)
            } else if (!isInCombatNode || profile.currentNodeCompleted) {
                if (_activeEnemyHp.value != null && !isNewCombatNode) {
                     // only clear if we are NOT on a new combat node (to avoid flickering)
                     // or if we just completed it
                     if (profile.currentNodeCompleted) {
                         clearCombat()
                     }
                }
            }
        }
    }

    suspend fun processCombatTurn(profile: PlayerProfile, action: String): PlayerProfile {
        if (action == "NOT_ENOUGH_AETHER") {
            _combatLog.value = _combatLog.value + CombatLogEntry(key = "ui.msg_combat_no_aether")
            return profile
        }
        return executeCombatTurnSync(profile, action)
    }

    private suspend fun executeCombatTurnSync(initialProfile: PlayerProfile, action: String): PlayerProfile {
        val nodes = currentFloorNodes.value
        val nodeIndex = initialProfile.currentNodeIndex
        if (nodeIndex !in nodes.indices) return initialProfile
        val activeNode = nodes[nodeIndex]
        
        val maxEnemyHp = activeNode.enemy?.overrideHp ?: 50
        var currentEnemyHp = _activeEnemyHp.value ?: maxEnemyHp

        val logs = mutableListOf<CombatLogEntry>()

        var playerHp = initialProfile.currentHp
        var playerAether = initialProfile.aether
        val updatedPlayerStatuses = _playerStatuses.value.map { it.copy() }.toMutableList()
        val updatedEnemyStatuses = _enemyStatuses.value.map { it.copy() }.toMutableList()

        if (updatedPlayerStatuses.any { it.type == StatusType.POISONED }) {
            playerHp = (playerHp - 5).coerceAtLeast(0)
            logs.add(CombatLogEntry(key = "ui.combat_log_player_poison"))
            if (playerHp <= 0) {
                onMessage(ActionMessage("ui.msg_spirit_fracture", listOf(initialProfile.savedFloorCheckpoint)))
                return initialProfile.copy(currentHp = 0)
            }
        }

        val playerStunned = updatedPlayerStatuses.any { it.type == StatusType.STUNNED }
        if (playerStunned) {
            logs.add(CombatLogEntry(key = "ui.combat_log_player_stunned"))
            decrementStatuses(updatedPlayerStatuses)
            _playerStatuses.value = updatedPlayerStatuses.filter { it.durationTurns > 0 }
            
            return executeEnemyTurnSync(initialProfile.copy(currentHp = playerHp), currentEnemyHp, activeNode, updatedPlayerStatuses, updatedEnemyStatuses, logs)
        }

        var damageDealt = 0
        var isCrit = false
        val critChance = (10 + initialProfile.currentWill * 4).coerceIn(10, 50)
        val isBlessed = updatedPlayerStatuses.any { it.type == StatusType.BLESSED }

        when (action) {
            "LIGHT_STRIKE" -> {
                val baseDmg = 10 + initialProfile.level * 2
                val rawDmg = (baseDmg * 0.9).toInt() + Random.nextInt(5)
                var finalDmg = rawDmg
                if (isBlessed) finalDmg = (finalDmg * 1.25f).toInt()
                
                isCrit = Random.nextInt(100) < critChance
                if (isCrit) finalDmg = (finalDmg * 1.5f).toInt()

                damageDealt = finalDmg
                currentEnemyHp = (currentEnemyHp - damageDealt).coerceAtLeast(0)
                _activeEnemyHp.value = currentEnemyHp

                logs.add(
                    CombatLogEntry(
                        key = "ui.combat_log_player_strike",
                        args = mapOf("damage" to damageDealt.toString())
                    )
                )
            }
            "HEAVY_BLOW" -> {
                if (playerAether < 15) {
                    logs.add(CombatLogEntry(key = "ui.combat_log_insufficient_aether"))
                    _combatLog.value = _combatLog.value + logs
                    return initialProfile
                }
                val baseDmg = 25 + initialProfile.level * 3
                val rawDmg = (baseDmg * 0.9).toInt() + Random.nextInt(10)
                var finalDmg = rawDmg
                if (isBlessed) finalDmg = (finalDmg * 1.25f).toInt()

                isCrit = Random.nextInt(100) < critChance
                if (isCrit) finalDmg = (finalDmg * 1.5f).toInt()

                damageDealt = finalDmg
                currentEnemyHp = (currentEnemyHp - damageDealt).coerceAtLeast(0)
                _activeEnemyHp.value = currentEnemyHp

                playerAether = (playerAether - 15).coerceAtLeast(0)

                logs.add(
                    CombatLogEntry(
                        key = "ui.combat_log_player_heavy_blow",
                        args = mapOf("damage" to damageDealt.toString())
                    )
                )
            }
            "BARRIER" -> {
                val healAmt = 20
                playerHp = (playerHp + healAmt).coerceAtMost(initialProfile.maxHp)
                val existingShield = updatedPlayerStatuses.find { it.type == StatusType.SHIELDED }
                if (existingShield != null) {
                    existingShield.durationTurns = 2
                } else {
                    updatedPlayerStatuses.add(CombatStatus(StatusType.SHIELDED, 2))
                }

                logs.add(
                    CombatLogEntry(
                        key = "ui.combat_log_player_barrier",
                        args = mapOf("heal" to healAmt.toString())
                    )
                )
            }
            "ESCAPE" -> {
                onMessage(ActionMessage("ui.msg_escaped_combat"))
                return initialProfile // Or handle escape properly
            }
        }

        if (isCrit && action != "BARRIER") {
            logs.add(CombatLogEntry(key = "ui.combat_log_crit"))
        }

        if (currentEnemyHp <= 0) {
            return handleVictorySync(initialProfile.copy(currentHp = playerHp, aether = playerAether), activeNode, playerHp, logs)
        }

        if (activeNode.type == NodeType.BOSS && currentEnemyHp < (maxEnemyHp / 2) && !hasTriggeredPhase2) {
            hasTriggeredPhase2 = true
            logs.add(CombatLogEntry(key = "ui.combat_log_boss_enraged"))
        }

        decrementStatuses(updatedPlayerStatuses)
        _playerStatuses.value = updatedPlayerStatuses.filter { it.durationTurns > 0 }

        return executeEnemyTurnSync(initialProfile.copy(currentHp = playerHp, aether = playerAether), currentEnemyHp, activeNode, updatedPlayerStatuses, updatedEnemyStatuses, logs)
    }

    private suspend fun executeEnemyTurnSync(
        profile: PlayerProfile,
        enemyHp: Int,
        activeNode: AdventureNode,
        playerStatuses: MutableList<CombatStatus>,
        enemyStatuses: MutableList<CombatStatus>,
        logs: MutableList<CombatLogEntry>
    ): PlayerProfile {
        var playerHp = profile.currentHp
        var currentEnemyHp = enemyHp

        if (enemyStatuses.any { it.type == StatusType.POISONED }) {
            currentEnemyHp = (currentEnemyHp - 5).coerceAtLeast(0)
            _activeEnemyHp.value = currentEnemyHp
            logs.add(CombatLogEntry(key = "ui.combat_log_enemy_poison"))
            if (currentEnemyHp <= 0) {
                return handleVictorySync(profile.copy(currentHp = playerHp), activeNode, playerHp, logs)
            }
        }

        val enemyStunned = enemyStatuses.any { it.type == StatusType.STUNNED }
        if (enemyStunned) {
            logs.add(CombatLogEntry(key = "ui.combat_log_enemy_stunned"))
            decrementStatuses(enemyStatuses)
            _enemyStatuses.value = enemyStatuses.filter { it.durationTurns > 0 }
            _combatLog.value = _combatLog.value + logs
            return profile.copy(currentHp = playerHp)
        }

        var enemyAtk = (8 + profile.currentFloor * 1.5).toInt()
        if (activeNode.type == NodeType.BOSS && hasTriggeredPhase2) {
            enemyAtk = (enemyAtk * 1.5f).toInt()
        }

        val enemyBlessed = enemyStatuses.any { it.type == StatusType.BLESSED }
        if (enemyBlessed) {
            enemyAtk = (enemyAtk * 1.25f).toInt()
        }

        val playerShielded = playerStatuses.any { it.type == StatusType.SHIELDED }

        when (_currentEnemyIntent.value) {
            EnemyIntent.ATTACK -> {
                var dmg = enemyAtk + Random.nextInt(4)
                if (playerShielded) {
                    dmg = (dmg * 0.5f).toInt()
                    logs.add(CombatLogEntry(key = "ui.combat_log_shield_reduced"))
                }
                playerHp = (playerHp - dmg).coerceAtLeast(0)
                logs.add(
                    CombatLogEntry(
                        key = "ui.combat_log_enemy_attack",
                        args = mapOf("damage" to dmg.toString())
                    )
                )
            }
            EnemyIntent.DEFEND -> {
                val existingShield = enemyStatuses.find { it.type == StatusType.SHIELDED }
                if (existingShield != null) {
                    existingShield.durationTurns = 2
                } else {
                    enemyStatuses.add(CombatStatus(StatusType.SHIELDED, 2))
                }
                logs.add(CombatLogEntry(key = "ui.combat_log_enemy_defend"))
            }
            EnemyIntent.DEBUFF -> {
                val existingPoison = playerStatuses.find { it.type == StatusType.POISONED }
                if (existingPoison != null) {
                    existingPoison.durationTurns = 3
                } else {
                    playerStatuses.add(CombatStatus(StatusType.POISONED, 3))
                }
                logs.add(CombatLogEntry(key = "ui.combat_log_enemy_poison_debuff"))
            }
            EnemyIntent.BUFF -> {
                val existingBless = enemyStatuses.find { it.type == StatusType.BLESSED }
                if (existingBless != null) {
                    existingBless.durationTurns = 3
                } else {
                    enemyStatuses.add(CombatStatus(StatusType.BLESSED, 3))
                }
                logs.add(CombatLogEntry(key = "ui.combat_log_enemy_buff"))
            }
        }

        decrementStatuses(enemyStatuses)
        _enemyStatuses.value = enemyStatuses.filter { it.durationTurns > 0 }

        _currentEnemyIntent.value = EnemyIntent.random()

        if (playerHp <= 0) {
            onTriggerSpiritFracture(profile, profile.momentum, profile.gold, profile.aether, profile.side)
            return profile.copy(currentHp = 0)
        } else {
            val updatedProfile = profile.copy(currentHp = playerHp, lastUpdated = System.currentTimeMillis())
            _combatLog.value = _combatLog.value + logs
            return updatedProfile
        }
    }

    private suspend fun handleVictorySync(profile: PlayerProfile, activeNode: AdventureNode, playerHp: Int, logs: MutableList<CombatLogEntry>): PlayerProfile {
        val rewards = RewardGenerator.generateRewards(
            player = profile.copy(currentHp = playerHp),
            isBoss = activeNode.type == NodeType.BOSS
        )

        _activeEnemyHp.value = 0 
        _playerStatuses.value = emptyList()
        _enemyStatuses.value = emptyList()
        
        val enemyId = activeNode.enemy?.enemyId ?: "unknown"
        val stats = LocalizationManager.getEnemyStats(enemyId)
        val enemyNameKey = stats?.optString("nameKey") ?: "enemy.$enemyId.name"

        onMessage(
            ActionMessage(
                "ui.msg_combat_victory_rewards", 
                listOf(enemyNameKey, rewards.expGained, rewards.goldGained, if (rewards.itemAwarded != null) "🎁 ${rewards.itemAwarded}" else "")
            )
        )

        var currentItems = if (profile.itemsEncoded.isEmpty()) emptyList() else profile.itemsEncoded.split(",")
        rewards.itemAwarded?.let { drop ->
            currentItems = currentItems + drop
        }
        val newItemsEncoded = currentItems.joinToString(",")

        var currentTitles = if (profile.titlesEncoded.isEmpty()) emptyList() else profile.titlesEncoded.split(",")
        rewards.titleAwarded?.let { drop ->
            currentTitles = currentTitles + drop
        }
        val newTitlesEncoded = currentTitles.joinToString(",")

        val isElite = activeNode.column == 1
        val greedLvl = LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, LegacyUpgradeType.GREED)
        val greedMultiplier = 1.0f + (greedLvl * 0.20f)
        
        val goldGained = if (isElite) (rewards.goldGained * 1.5).toInt() else rewards.goldGained
        val scaledGoldGained = (goldGained * greedMultiplier).toInt()
        val expGained = if (isElite) (rewards.expGained * 1.5).toInt() else rewards.expGained

        var totalExp = profile.exp + expGained
        var totalLevel = profile.level
        var totalMaxExp = profile.maxExp
        var totalMaxHp = profile.maxHp
        var newHp = rewards.finalHp
        var didLevelUp = false
        while (totalExp >= totalMaxExp && totalLevel < 100) {
            totalExp -= totalMaxExp
            totalLevel++
            totalMaxExp = totalLevel * 100
            totalMaxHp += 20
            newHp = totalMaxHp
            didLevelUp = true
        }

        val nodes = currentFloorNodes.value
        val nextNodeIndex = profile.currentNodeIndex + 1
        val hasNextNode = nextNodeIndex < nodes.size
        
        val isBoss = activeNode.type == NodeType.BOSS
        val completedState = isBoss || !hasNextNode

        val profileWithQuest = updateDailyQuestProgress(profile, 0, 1)
        val updatedProfile = profileWithQuest.copy(
            currentHp = newHp,
            maxHp = totalMaxHp,
            level = totalLevel,
            exp = totalExp,
            maxExp = totalMaxExp,
            gold = profileWithQuest.gold + scaledGoldGained,
            itemsEncoded = newItemsEncoded,
            titlesEncoded = newTitlesEncoded,
            currentNodeIndex = if (!completedState && hasNextNode) nextNodeIndex else profile.currentNodeIndex,
            currentNodeCompleted = completedState,
            lastUpdated = System.currentTimeMillis()
        )
        val checkedProfile = checkAndUnlockTitles(updatedProfile)

        logs.add(
            CombatLogEntry(
                key = "ui.combat_log_victory",
                args = mapOf("exp" to expGained.toString(), "gold" to scaledGoldGained.toString())
            )
        )
        rewards.itemAwarded?.let { drop ->
            logs.add(CombatLogEntry(key = "ui.combat_log_loot", args = mapOf("item" to drop)))
        }
        rewards.titleAwarded?.let { drop ->
            logs.add(CombatLogEntry(key = "ui.combat_log_title", args = mapOf("title" to drop)))
        }
        if (didLevelUp) {
            logs.add(CombatLogEntry(key = "ui.combat_log_level_up", args = mapOf("level" to totalLevel.toString())))
        }

        val journalEntry = JournalEntry(
            floor = profile.currentFloor,
            actionKey = "ui.journal_combat_victory",
            actionArgsEncoded = JournalEntry.encodeActionArgs(listOf(enemyNameKey, profile.currentFloor.toString())),
            sideAlignmentShift = "NEUTRAL",
            alignmentImpact = 0,
            nodeIndex = profile.currentNodeIndex
        )
        repository.insertJournalEntry(journalEntry)

        _combatLog.value = _combatLog.value + logs
        onMessage(
            ActionMessage(
                "ui.msg_combat_victory_rewards", 
                listOf(enemyNameKey, rewards.expGained, rewards.goldGained, if (rewards.itemAwarded != null) "🎁 ${rewards.itemAwarded}" else "")
            )
        )
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _activeEnemyHp.value = null
        }
        
        return checkedProfile
    }

    private fun decrementStatuses(statuses: MutableList<CombatStatus>) {
        statuses.forEach { it.durationTurns-- }
    }

    fun startNarrativeEvent(event: NarrativeEvent) {
        _activeNarrativeEvent.value = event
    }

    fun cancelNarrativeEvent() {
        _activeNarrativeEvent.value = null
    }

    fun selectNarrativeEventOption(event: NarrativeEvent, choice: NarrativeBranchOption) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val updated = NarrativeEventProcessor.processNarrativeChoice(profile, choice)
            val titleChecked = checkAndUnlockTitles(updated)
            repository.savePlayerProfile(titleChecked)
            
            val entry = JournalEntry(
                floor = profile.currentFloor,
                actionKey = "ui.journal_narrative_choice_result",
                actionArgsEncoded = JournalEntry.encodeActionArgs(listOf(choice.textKey, event.titleKey, choice.outcomeKey)),
                sideAlignmentShift = if (choice.alignmentImpact > 0) "SANCTUM" else if (choice.alignmentImpact < 0) "COVENANT" else "NEUTRAL",
                alignmentImpact = choice.alignmentImpact
            )
            repository.insertJournalEntry(entry)

            _completedEvents.value = _completedEvents.value + event.id
            onMessage(ActionMessage(choice.outcomeKey))
            _activeNarrativeEvent.value = null
        }
    }

    fun moveToNode(profile: PlayerProfile, depth: Int, column: Int): PlayerProfile? {
        val nodes = currentFloorNodes.value
        val targetNode = nodes.find { it.depth == depth && it.column == column } ?: return null
        val targetIndex = nodes.indexOf(targetNode)
        
        return profile.copy(
            currentNodeIndex = targetIndex,
            currentNodeCompleted = false,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun applyNodeChoice(profile: PlayerProfile, choice: NodeChoice): Pair<PlayerProfile, JournalEntry?> {
        val effects = choice.effects
        
        var currentItems = if (profile.itemsEncoded.isEmpty()) emptyList() else profile.itemsEncoded.split(",")
        if (effects.rewardItemId.isNotEmpty()) {
            currentItems = currentItems + effects.rewardItemId
        }
        val newItemsEncoded = currentItems.joinToString(",")

        val nodes = currentFloorNodes.value
        val nextNodeIndex = profile.currentNodeIndex + 1
        val hasNextNode = nextNodeIndex < nodes.size
        
        val isBoss = nodes.getOrNull(profile.currentNodeIndex)?.type == NodeType.BOSS
        val completedState = isBoss || !hasNextNode

        val updated = profile.copy(
            currentHp = (profile.currentHp + effects.hpChange).coerceIn(0, profile.maxHp),
            gold = (profile.gold + effects.goldChange).coerceAtLeast(0),
            aether = (profile.aether + effects.aetherChange).coerceIn(0, 100),
            exp = (profile.exp + effects.expChange),
            momentum = (profile.momentum + effects.momentumShift).coerceIn(0, 100),
            itemsEncoded = newItemsEncoded,
            currentNodeIndex = if (!completedState && hasNextNode) nextNodeIndex else profile.currentNodeIndex,
            currentNodeCompleted = completedState,
            lastUpdated = System.currentTimeMillis()
        )
        
        val journal = JournalEntry(
            floor = profile.currentFloor,
            actionKey = choice.journalKey,
            sideAlignmentShift = if (effects.momentumShift > 0) "SANCTUM" else if (effects.momentumShift < 0) "COVENANT" else "NEUTRAL",
            alignmentImpact = effects.momentumShift,
            nodeIndex = profile.currentNodeIndex
        )

        return updated to journal
    }

    fun ascendFloor(profile: PlayerProfile): PlayerProfile {
        return profile.copy(
            currentFloor = profile.currentFloor + 1,
            currentNodeIndex = 0,
            currentNodeCompleted = false,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun processScenarioChoice(profile: PlayerProfile, option: GameOption): Pair<PlayerProfile, List<JournalEntry>> {
        val effects = option.effects
        val updated = profile.copy(
            currentHp = (profile.currentHp + effects.hpChange).coerceIn(0, profile.maxHp),
            gold = (profile.gold + effects.goldChange).coerceAtLeast(0),
            aether = (profile.aether + effects.aetherChange).coerceIn(0, 100),
            exp = (profile.exp + effects.expChange),
            momentum = (profile.momentum + effects.momentumShift).coerceIn(0, 100),
            lastUpdated = System.currentTimeMillis()
        )
        val journal = JournalEntry(
            floor = profile.currentFloor,
            actionKey = option.journalKey,
            sideAlignmentShift = if (effects.momentumShift > 0) "SANCTUM" else if (effects.momentumShift < 0) "COVENANT" else "NEUTRAL",
            alignmentImpact = effects.momentumShift
        )
        return updated to listOf(journal)
    }
}
