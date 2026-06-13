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

    fun clearCombat() {
        _activeEnemyHp.value = null
        _combatLog.value = emptyList()
        _playerStatuses.value = emptyList()
        _enemyStatuses.value = emptyList()
        hasTriggeredPhase2 = false
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
            if ((activeNode.type == NodeType.COMBAT || activeNode.type == NodeType.BOSS) && !profile.currentNodeCompleted && _activeEnemyHp.value == null) {
                val enemyRef = activeNode.enemy
                val enemyId = enemyRef?.enemyId ?: return
                
                val stats = LocalizationManager.getEnemyStats(enemyId)
                val enemyHp = enemyRef.overrideHp ?: stats?.optInt("hp") ?: 50
                
                _activeEnemyHp.value = enemyHp
                _playerStatuses.value = emptyList()
                _enemyStatuses.value = emptyList()
                _currentEnemyIntent.value = EnemyIntent.random()
                hasTriggeredPhase2 = false
                
                val enemyNameKey = stats?.optString("nameKey") ?: "enemy.$enemyId.name"
                _combatLog.value = listOf(
                    CombatLogEntry(
                        key = "ui.combat_log_initiated",
                        args = mapOf("enemy" to enemyNameKey)
                    )
                )
            }
        }
    }

    fun executeCombatTurn(action: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val nodes = currentFloorNodes.value
            val nodeIndex = profile.currentNodeIndex
            if (nodeIndex !in nodes.indices) return@launch
            val activeNode = nodes[nodeIndex]
            
            val maxEnemyHp = activeNode.enemy?.overrideHp ?: 50
            var currentEnemyHp = _activeEnemyHp.value ?: maxEnemyHp
 
            val logs = mutableListOf<CombatLogEntry>()
 
            var playerHp = profile.currentHp
            val updatedPlayerStatuses = _playerStatuses.value.map { it.copy() }.toMutableList()
            val updatedEnemyStatuses = _enemyStatuses.value.map { it.copy() }.toMutableList()
 
            if (updatedPlayerStatuses.any { it.type == StatusType.POISONED }) {
                playerHp = (playerHp - 5).coerceAtLeast(0)
                logs.add(CombatLogEntry(key = "ui.combat_log_player_poison"))
                if (playerHp <= 0) {
                    val updated = profile.copy(currentHp = 0)
                    repository.savePlayerProfile(updated)
                    onMessage(ActionMessage("ui.msg_spirit_fracture", listOf(profile.savedFloorCheckpoint)))
                    return@launch
                }
            }
 
            val playerStunned = updatedPlayerStatuses.any { it.type == StatusType.STUNNED }
            if (playerStunned) {
                logs.add(CombatLogEntry(key = "ui.combat_log_player_stunned"))
                decrementStatuses(updatedPlayerStatuses)
                _playerStatuses.value = updatedPlayerStatuses.filter { it.durationTurns > 0 }
                
                executeEnemyTurn(profile, playerHp, currentEnemyHp, activeNode, updatedPlayerStatuses, updatedEnemyStatuses, logs)
                return@launch
            }
 
            var damageDealt = 0
            var isCrit = false
            val critChance = (10 + profile.currentWill * 4).coerceIn(10, 50)
            val isBlessed = updatedPlayerStatuses.any { it.type == StatusType.BLESSED }
 
            when (action) {
                "LIGHT_STRIKE" -> {
                    val baseDmg = 10 + profile.level * 2
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
                    if (profile.aether < 15) {
                        logs.add(CombatLogEntry(key = "ui.combat_log_insufficient_aether"))
                        _combatLog.value = logs
                        return@launch
                    }
                    val baseDmg = 25 + profile.level * 3
                    val rawDmg = (baseDmg * 0.9).toInt() + Random.nextInt(10)
                    var finalDmg = rawDmg
                    if (isBlessed) finalDmg = (finalDmg * 1.25f).toInt()
 
                    isCrit = Random.nextInt(100) < critChance
                    if (isCrit) finalDmg = (finalDmg * 1.5f).toInt()
 
                    damageDealt = finalDmg
                    currentEnemyHp = (currentEnemyHp - damageDealt).coerceAtLeast(0)
                    _activeEnemyHp.value = currentEnemyHp
 
                    val newAether = (profile.aether - 15).coerceAtLeast(0)
                    val updatedProfile = profile.copy(aether = newAether, lastUpdated = System.currentTimeMillis())
                    repository.savePlayerProfile(updatedProfile)
 
                    logs.add(
                        CombatLogEntry(
                            key = "combat_log_player_heavy_blow",
                            args = mapOf("damage" to damageDealt.toString())
                        )
                    )
                }
                "BARRIER" -> {
                    val healAmt = 20
                    playerHp = (playerHp + healAmt).coerceAtMost(profile.maxHp)
                    val existingShield = updatedPlayerStatuses.find { it.type == StatusType.SHIELDED }
                    if (existingShield != null) {
                        existingShield.durationTurns = 2
                    } else {
                        updatedPlayerStatuses.add(CombatStatus(StatusType.SHIELDED, 2))
                    }
                    val updatedProfile = profile.copy(currentHp = playerHp, lastUpdated = System.currentTimeMillis())
                    repository.savePlayerProfile(updatedProfile)
 
                    logs.add(
                        CombatLogEntry(
                            key = "ui.combat_log_player_barrier",
                            args = mapOf("heal" to healAmt.toString())
                        )
                    )
                }
                "ESCAPE" -> {
                    onMessage(ActionMessage("ui.msg_escaped_combat"))
                    return@launch
                }
            }
 
            if (isCrit && action != "BARRIER") {
                logs.add(CombatLogEntry(key = "ui.combat_log_crit"))
            }
 
            if (currentEnemyHp <= 0) {
                handleVictory(profile, activeNode, playerHp, logs)
                return@launch
            }
 
            if (activeNode.type == NodeType.BOSS && currentEnemyHp < (maxEnemyHp / 2) && !hasTriggeredPhase2) {
                hasTriggeredPhase2 = true
                logs.add(CombatLogEntry(key = "ui.combat_log_boss_enraged"))
            }
 
            decrementStatuses(updatedPlayerStatuses)
            _playerStatuses.value = updatedPlayerStatuses.filter { it.durationTurns > 0 }
 
            executeEnemyTurn(profile, playerHp, currentEnemyHp, activeNode, updatedPlayerStatuses, updatedEnemyStatuses, logs)
        }
    }

    private suspend fun executeEnemyTurn(
        profile: PlayerProfile,
        initialPlayerHp: Int,
        enemyHp: Int,
        activeNode: AdventureNode,
        playerStatuses: MutableList<CombatStatus>,
        enemyStatuses: MutableList<CombatStatus>,
        logs: MutableList<CombatLogEntry>
    ) {
        var playerHp = initialPlayerHp
        var currentEnemyHp = enemyHp
 
        if (enemyStatuses.any { it.type == StatusType.POISONED }) {
            currentEnemyHp = (currentEnemyHp - 5).coerceAtLeast(0)
            _activeEnemyHp.value = currentEnemyHp
            logs.add(CombatLogEntry(key = "ui.combat_log_enemy_poison"))
            if (currentEnemyHp <= 0) {
                handleVictory(profile, activeNode, playerHp, logs)
                return
            }
        }
 
        val enemyStunned = enemyStatuses.any { it.type == StatusType.STUNNED }
        if (enemyStunned) {
            logs.add(CombatLogEntry(key = "ui.combat_log_enemy_stunned"))
            decrementStatuses(enemyStatuses)
            _enemyStatuses.value = enemyStatuses.filter { it.durationTurns > 0 }
            _combatLog.value = _combatLog.value + logs
            return
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
        } else {
            val updatedProfile = profile.copy(currentHp = playerHp, lastUpdated = System.currentTimeMillis())
            repository.savePlayerProfile(updatedProfile)
            _combatLog.value = _combatLog.value + logs
        }
    }
 
    private suspend fun handleVictory(profile: PlayerProfile, activeNode: AdventureNode, playerHp: Int, logs: MutableList<CombatLogEntry>) {
        val rewards = RewardGenerator.generateRewards(
            player = profile.copy(currentHp = playerHp),
            isBoss = activeNode.type == NodeType.BOSS
        )
 
        _activeEnemyHp.value = null
        _playerStatuses.value = emptyList()
        _enemyStatuses.value = emptyList()
        
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
        
        // AUTO-PROGRESS: If not a boss and there's a next node, move immediately.
        // If it's a boss, we set completedState = true so the UI shows the "Ascend" button.
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
        repository.savePlayerProfile(checkedProfile)
 
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
 
        val enemyId = activeNode.enemy?.enemyId ?: "unknown"
        val stats = LocalizationManager.getEnemyStats(enemyId)
        val enemyNameKey = stats?.optString("nameKey") ?: "enemy.$enemyId.name"
 
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
        onMessage(ActionMessage("ui.msg_defeated_enemy", listOf(enemyNameKey)))
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





    private fun getSkillNameForClass(chosenClass: String): Pair<String, String> {
        return when {
            chosenClass.contains("Holy Aegis") -> Pair("Sanctified Shieldbash", "Kutsal Siper Vuruşu")
            chosenClass.contains("Death Herald") -> Pair("Decay Scythe Reave", "Çürük Tırpan Reaksiyonu")
            chosenClass.contains("Codifier") -> Pair("Celestial Scribe Ray", "Semavi Yazıt Işını")
            chosenClass.contains("Wildhand") -> Pair("Eclipse Shade Claw", "Kaotik Gölge Pençesi")
            chosenClass.contains("Iron Throne") -> Pair("Ruthless Execution Blade", "İnsafsız İnfaz Baltası")
            chosenClass.contains("Tempest") -> Pair("Raging Thunder Sunder", "Kuduz Fırtına Darbesi")
            else -> Pair("Adrenaline Reflection Strike", "Adrenalin Yansıma Saldırısı")
        }
    }
}
