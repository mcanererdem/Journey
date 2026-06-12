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
    private val onMessage: (String, String) -> Unit,
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

    private val _activeSecretBossCombat = MutableStateFlow<SecretBossEncounter?>(null)
    val activeSecretBossCombat: StateFlow<SecretBossEncounter?> = _activeSecretBossCombat.asStateFlow()

    private val _activeSecretBossHp = MutableStateFlow<Int?>(null)
    val activeSecretBossHp: StateFlow<Int?> = _activeSecretBossHp.asStateFlow()

    private val _completedEvents = MutableStateFlow<Set<String>>(emptySet())
    val completedEvents: StateFlow<Set<String>> = _completedEvents.asStateFlow()

    private val _slainSecretBosses = MutableStateFlow<Set<String>>(emptySet())
    val slainSecretBosses: StateFlow<Set<String>> = _slainSecretBosses.asStateFlow()

    private var hasTriggeredPhase2 = false

    fun clearCombat() {
        _activeEnemyHp.value = null
        _combatLog.value = emptyList()
        _playerStatuses.value = emptyList()
        _enemyStatuses.value = emptyList()
        hasTriggeredPhase2 = false
        _activeSecretBossCombat.value = null
        _activeSecretBossHp.value = null
    }

    fun clearCompletedEventsAndSlainBosses() {
        _completedEvents.value = emptySet()
        _slainSecretBosses.value = emptySet()
    }

    fun addCompletedEvent(eventId: String) {
        _completedEvents.value = _completedEvents.value + eventId
    }

    fun addSlainSecretBoss(bossId: String) {
        _slainSecretBosses.value = _slainSecretBosses.value + bossId
    }

    fun checkAndInitCombat(profile: PlayerProfile, nodes: List<AdventureNode>, lang: String) {
        if (profile.currentNodeIndex in nodes.indices) {
            val activeNode = nodes[profile.currentNodeIndex]
            if ((activeNode.type == NodeType.COMBAT || activeNode.type == NodeType.BOSS) && !profile.currentNodeCompleted && _activeEnemyHp.value == null) {
                val enemyHp = activeNode.enemy?.overrideHp ?: 50
                _activeEnemyHp.value = enemyHp
                _playerStatuses.value = emptyList()
                _enemyStatuses.value = emptyList()
                _currentEnemyIntent.value = EnemyIntent.random()
                hasTriggeredPhase2 = false
                
                val enemyNameKey = activeNode.enemy?.enemyId?.let { "enemy.$it.name" } ?: "ui.label_unknown_enemy"
                _combatLog.value = listOf(
                    CombatLogEntry(
                        key = "combat_log_initiated",
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
                logs.add(CombatLogEntry(key = "combat_log_player_poison"))
                if (playerHp <= 0) {
                    val updated = profile.copy(currentHp = 0)
                    repository.savePlayerProfile(updated)
                    onTriggerSpiritFracture(updated, profile.currentFloor, 1, 0, "Died from poison in combat")
                    return@launch
                }
            }
 
            val playerStunned = updatedPlayerStatuses.any { it.type == StatusType.STUNNED }
            if (playerStunned) {
                logs.add(CombatLogEntry(key = "combat_log_player_stunned"))
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
                            key = "combat_log_player_strike",
                            args = mapOf("damage" to damageDealt.toString())
                        )
                    )
                }
                "HEAVY_BLOW" -> {
                    if (profile.aether < 15) {
                        logs.add(CombatLogEntry(key = "combat_log_insufficient_aether"))
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
                            key = "combat_log_player_barrier",
                            args = mapOf("heal" to healAmt.toString())
                        )
                    )
                }
                "ESCAPE" -> {
                    clearCombat()
                    onMessage("🏃 Escaped from combat.", "🏃 Dövüşten kaçtınız.")
                    return@launch
                }
            }
 
            if (isCrit && action != "BARRIER") {
                logs.add(CombatLogEntry(key = "combat_log_crit"))
            }
 
            if (currentEnemyHp <= 0) {
                handleVictory(profile, activeNode, playerHp, logs)
                return@launch
            }
 
            if (activeNode.type == NodeType.BOSS && currentEnemyHp < (maxEnemyHp / 2) && !hasTriggeredPhase2) {
                hasTriggeredPhase2 = true
                logs.add(CombatLogEntry(key = "combat_log_boss_enraged"))
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
            logs.add(CombatLogEntry(key = "combat_log_enemy_poison"))
            if (currentEnemyHp <= 0) {
                handleVictory(profile, activeNode, playerHp, logs)
                return
            }
        }
 
        val enemyStunned = enemyStatuses.any { it.type == StatusType.STUNNED }
        if (enemyStunned) {
            logs.add(CombatLogEntry(key = "combat_log_enemy_stunned"))
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
                    logs.add(CombatLogEntry(key = "combat_log_shield_reduced"))
                }
                playerHp = (playerHp - dmg).coerceAtLeast(0)
                logs.add(
                    CombatLogEntry(
                        key = "combat_log_enemy_attack",
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
                logs.add(CombatLogEntry(key = "combat_log_enemy_defend"))
            }
            EnemyIntent.DEBUFF -> {
                val existingPoison = playerStatuses.find { it.type == StatusType.POISONED }
                if (existingPoison != null) {
                    existingPoison.durationTurns = 3
                } else {
                    playerStatuses.add(CombatStatus(StatusType.POISONED, 3))
                }
                logs.add(CombatLogEntry(key = "combat_log_enemy_poison_debuff"))
            }
            EnemyIntent.BUFF -> {
                val existingBless = enemyStatuses.find { it.type == StatusType.BLESSED }
                if (existingBless != null) {
                    existingBless.durationTurns = 3
                } else {
                    enemyStatuses.add(CombatStatus(StatusType.BLESSED, 3))
                }
                logs.add(CombatLogEntry(key = "combat_log_enemy_buff"))
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
        val completedState = !hasNextNode
 
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
            currentNodeIndex = if (hasNextNode) nextNodeIndex else profile.currentNodeIndex,
            currentNodeCompleted = completedState,
            lastUpdated = System.currentTimeMillis()
        )
        val checkedProfile = checkAndUnlockTitles(updatedProfile)
        repository.savePlayerProfile(checkedProfile)
 
        logs.add(
            CombatLogEntry(
                key = "combat_log_victory",
                args = mapOf("exp" to expGained.toString(), "gold" to scaledGoldGained.toString())
            )
        )
        rewards.itemAwarded?.let { drop ->
            logs.add(CombatLogEntry(key = "combat_log_loot", args = mapOf("item" to drop)))
        }
        rewards.titleAwarded?.let { drop ->
            logs.add(CombatLogEntry(key = "combat_log_title", args = mapOf("title" to drop)))
        }
        if (didLevelUp) {
            logs.add(CombatLogEntry(key = "combat_log_level_up", args = mapOf("level" to totalLevel.toString())))
        }
 
        val enemyNameKey = activeNode.enemy?.enemyId?.let { "enemy.$it.name" } ?: "ui.label_unknown_enemy"
        val enemyNameEn = LocalizationManager.getString("EN", enemyNameKey)
        val enemyNameTr = LocalizationManager.getString("TR", enemyNameKey)
 
        val journalEntry = JournalEntry(
            floor = profile.currentFloor,
            actionTakenEs = "Defeated $enemyNameEn in combat on Floor ${profile.currentFloor}.",
            actionTakenTr = "${profile.currentFloor}. Katta $enemyNameTr karşısındaki düelloyu kazandınız.",
            sideAlignmentShift = "NEUTRAL",
            alignmentImpact = 0,
            nodeIndex = profile.currentNodeIndex
        )
        repository.insertJournalEntry(journalEntry)
 
        _combatLog.value = _combatLog.value + logs
        onMessage("Defeated $enemyNameEn!", "$enemyNameTr yok edildi!")
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
            
            val textEn = LocalizationManager.getString("EN", choice.textKey)
            val titleEn = LocalizationManager.getString("EN", event.titleKey)
            val outcomeEn = LocalizationManager.getString("EN", choice.outcomeKey)
            val textTr = LocalizationManager.getString("TR", choice.textKey)
            val titleTr = LocalizationManager.getString("TR", event.titleKey)
            val outcomeTr = LocalizationManager.getString("TR", choice.outcomeKey)

            val entry = JournalEntry(
                floor = profile.currentFloor,
                actionTakenEs = "Decided '$textEn' inside scenario '$titleEn'. Outcome: $outcomeEn",
                actionTakenTr = "'$titleTr' içindeki kararın: '$textTr'. Sonuç: $outcomeTr",
                sideAlignmentShift = if (choice.alignmentImpact > 0) "SANCTUM" else if (choice.alignmentImpact < 0) "COVENANT" else "NEUTRAL",
                alignmentImpact = choice.alignmentImpact
            )
            repository.insertJournalEntry(entry)

            _completedEvents.value = _completedEvents.value + event.id
            onMessage(outcomeEn, outcomeTr)
            _activeNarrativeEvent.value = null
        }
    }

    fun startSecretBossEncounter(boss: SecretBossEncounter) {
        _activeSecretBossCombat.value = boss
        _activeSecretBossHp.value = boss.hp
        
        _combatLog.value = listOf(
            CombatLogEntry(
                key = "combat_log_secret_boss_initiated",
                args = mapOf("boss" to boss.nameKey, "hp" to boss.hp.toString())
            )
        )
    }

    fun escapeSecretBoss() {
        _activeSecretBossCombat.value = null
        _activeSecretBossHp.value = null
        _combatLog.value = emptyList()
    }

    fun executeSecretBossTurn(action: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val boss = _activeSecretBossCombat.value ?: return@launch
            val currentEnemyHp = _activeSecretBossHp.value ?: boss.hp

            val bossNameTr = LocalizationManager.getString("TR", boss.nameKey)
            val bossNameEn = LocalizationManager.getString("EN", boss.nameKey)
            val enemyFaction = EnemyFaction.fromName(bossNameEn)
            var alignmentModEn = ""
            var alignmentModTr = ""
            var factionMod = 1.0f

            val isSanctum = profile.side == "SANCTUM" || profile.momentum > 70
            val isCovenant = profile.side == "COVENANT" || profile.momentum < 30

            if (isSanctum) {
                when (enemyFaction) {
                    EnemyFaction.VOID_CORRUPTION -> {
                        val sanctumBonus = 0.20f + ((profile.momentum - 50) / 200.0f).coerceIn(0.0f, 0.25f)
                        factionMod += sanctumBonus
                        val pct = (sanctumBonus * 100).toInt()
                        alignmentModEn = "[✨ Radiant Resonance: +$pct% Holy Radiant Damage!]"
                        alignmentModTr = "[✨ Işıltılı Rezonans: +$pct% Ek Kutsal Hasar!]"
                    }
                    EnemyFaction.SANCTUM_WRATH -> {
                        factionMod -= 0.15f
                        alignmentModEn = "[⚠️ Celestial Affinity: -15% kinship resistance]"
                        alignmentModTr = "[⚠️ Gök Bağı: Kutsal direnç nedeniyle -15% Azalmış Hasar]"
                    }
                    else -> {}
                }
                if (profile.aether > 0) {
                    val aetherBonus = ((profile.aether / 50) * 0.05f).coerceAtMost(0.15f)
                    if (aetherBonus > 0f) {
                        factionMod += aetherBonus
                        val pct = (aetherBonus * 100).toInt()
                        alignmentModEn = if (alignmentModEn.isEmpty()) "[✨ Aether Amplification: +$pct%]" else alignmentModEn + " [✨ +$pct% Aether]"
                        alignmentModTr = if (alignmentModTr.isEmpty()) "[✨ Cevher Güçlendirmesi: +$pct%]" else alignmentModTr + " [✨ +$pct% Cevher]"
                    }
                }
            } else if (isCovenant) {
                when (enemyFaction) {
                    EnemyFaction.SANCTUM_WRATH -> {
                        val voidBonus = 0.20f + (Math.abs(profile.momentum - 50) / 200.0f).coerceIn(0.0f, 0.25f)
                        factionMod += voidBonus
                        val pct = (voidBonus * 100).toInt()
                        alignmentModEn = "[🔮 Ruinous Eclipse: +$pct% Chaos Abyss Damage!]"
                        alignmentModTr = "[🔮 Yıkıcı Tutulma: +$pct% Ek Boşluk Hasarı!]"
                    }
                    EnemyFaction.VOID_CORRUPTION -> {
                        factionMod -= 0.15f
                        alignmentModEn = "[⚠️ Shadow Affinity: -15% void resistance]"
                        alignmentModTr = "[⚠️ Gölgelerin Bağı: Karanlık direnci nedeniyle -15% Azalmış Hasar]"
                    }
                    else -> {}
                }
                if (profile.aether > 0) {
                    val aetherBonus = ((profile.aether / 50) * 0.05f).coerceAtMost(0.15f)
                    if (aetherBonus > 0f) {
                        factionMod += aetherBonus
                        val pct = (aetherBonus * 100).toInt()
                        alignmentModEn = if (alignmentModEn.isEmpty()) "[🔥 Aether Immolation: +$pct%]" else alignmentModEn + " [🔥 +$pct% Aether]"
                        alignmentModTr = if (alignmentModTr.isEmpty()) "[🔥 Ateş Yıkımı: +$pct%]" else alignmentModTr + " [🔥 +$pct% Ateş]"
                    }
                }
            } else {
                if (enemyFaction == EnemyFaction.BLIGHTED_AMALGAM) {
                    factionMod += 0.20f
                    alignmentModEn = "[⚖️ Indomitable Balance: +20% focus damage]"
                    alignmentModTr = "[⚖️ Sarsılmaz Denge: Canavarlara odaklanma ile +20% Hasar]"
                }
            }

            var statModEn = ""
            var statModTr = ""
            var statDamageMod = 1.0f

            if (profile.currentWill == 0) {
                statDamageMod -= 0.25f
                statModEn = "[⚠️ Mind Fatigue: -25% weapon efficiency due to 0 Willpower]"
                statModTr = "[⚠️ Zihinsel Sürsaj: 0 İrade gücüyle -25% Azalmış Savaş Gücü]"
            } else if (profile.currentWill >= 7) {
                statDamageMod += 0.15f
                statModEn = "[⚡ High Clarity: +15% damage from steady focus]"
                statModTr = "[⚡ Berrak Zihin: Yüksek İrade gücüyle +15% Hasar]"
            }

            var adrenalineMod = 1.0f
            val hpPercentage = profile.currentHp.toFloat() / profile.maxHp.toFloat()
            if (hpPercentage < 0.3f && hpPercentage > 0.0f) {
                adrenalineMod = 1.30f
                statModEn += if (statModEn.isEmpty()) "[🩸 Adrenaline Siege: +30% Survival Fury!]" else " [🩸 +30% Adrenaline]"
                statModTr += if (statModTr.isEmpty()) "[🩸 Can Havli: Seferberlikte +30% Hasar!]" else " [🩸 +30% Can Havli]"
            }

            val baseDmg = 12 + profile.level * 2
            var actionDescriptionEn = ""
            var actionDescriptionTr = ""
            var selfHpDmg = 0

            val critChance = (10 + profile.currentWill * 4).coerceIn(10, 50)
            val isCrit = Random.nextInt(100) < critChance
            val criticalMultiplier = if (isCrit) 1.5f else 1.0f

            var computedDmg = 0

            when (action) {
                "STRIKE" -> {
                    val rawDmg = (baseDmg * 0.9).toInt() + Random.nextInt(7)
                    computedDmg = (rawDmg * factionMod * statDamageMod * adrenalineMod * criticalMultiplier).toInt().coerceAtLeast(1)
                    val actualEnemyHp = (currentEnemyHp - computedDmg).coerceAtLeast(0)
                    _activeSecretBossHp.value = actualEnemyHp

                    actionDescriptionEn = "You swung your weapon at secret boss $bossNameEn, dealing $computedDmg damage."
                    actionDescriptionTr = "Gizli patron $bossNameTr üzerine kılıç savurup $computedDmg hasar verdiniz."
                }
                "SKILL" -> {
                    val multiplier = if (profile.side != "NEUTRAL") 2.5f else 1.8f
                    val rawDmg = (baseDmg * multiplier).toInt() + Random.nextInt(11)
                    computedDmg = (rawDmg * factionMod * statDamageMod * adrenalineMod * criticalMultiplier).toInt().coerceAtLeast(1)
                    val actualEnemyHp = (currentEnemyHp - computedDmg).coerceAtLeast(0)
                    _activeSecretBossHp.value = actualEnemyHp

                    selfHpDmg = 10
                    val skillNames = getSkillNameForClass(profile.chosenClass)

                    actionDescriptionEn = "Unleashed Reflection Skill [${skillNames.first}], evoking $computedDmg celestial damage (Fatigued: -$selfHpDmg HP)."
                    actionDescriptionTr = "Yansıma Yeteneği [${skillNames.second}] fırlatarak $computedDmg kozmik hasar verdiniz (Yorgunluk: -$selfHpDmg HP)."
                }
                "POTION" -> {
                    if (profile.gold >= 20) {
                        val healedValue = 40
                        val actualHp = (profile.currentHp + healedValue).coerceAtMost(profile.maxHp)
                        val updated = profile.copy(
                            gold = profile.gold - 20,
                            currentHp = actualHp,
                            lastUpdated = System.currentTimeMillis()
                        )
                        repository.savePlayerProfile(updated)

                        actionDescriptionEn = "Swallowed a quick Sanctuary Flask (-20 Gold), recovering +$healedValue HP."
                        actionDescriptionTr = "Kesenizden -20 Altına Şifa İksiri içip +$healedValue Can kazandınız."
                    } else {
                        val healedValue = 10
                        val actualHp = (profile.currentHp + healedValue).coerceAtMost(profile.maxHp)
                        val updated = profile.copy(
                            currentHp = actualHp,
                            lastUpdated = System.currentTimeMillis()
                        )
                        repository.savePlayerProfile(updated)

                        actionDescriptionEn = "No gold! Channeled focus to reconstruct +$healedValue HP vital cells."
                        actionDescriptionTr = "Yeterli altınınız yok! Odaklanarak +$healedValue Can dokusunu onardınız."
                    }
                }
            }

            val newEnemyHp = _activeSecretBossHp.value ?: 0
            val logs = ArrayList<CombatLogEntry>()
            
            val directMsg = if (activeLanguage.value == "TR") actionDescriptionTr else actionDescriptionEn
            logs.add(CombatLogEntry(key = "combat_log_direct", args = mapOf("text" to directMsg)))
            
            if (isCrit && action != "POTION") {
                val critMsg = if (activeLanguage.value == "TR") {
                    "💥 KRİTİK DOĞRUDAN DARBE! (%${critChance} şans ile x1.5 hasar!)"
                } else {
                    "💥 CRITICAL DIRECT BURST! (Chance: $critChance%, dealt x1.5 damage!)"
                }
                logs.add(CombatLogEntry(key = "combat_log_direct", args = mapOf("text" to critMsg)))
            }
            if (action != "POTION") {
                val alignMod = if (activeLanguage.value == "TR") alignmentModTr else alignmentModEn
                if (alignMod.isNotEmpty()) {
                    val label = if (activeLanguage.value == "TR") "🛡️ Faksiyon Mod: $alignMod" else "🛡️ Faction Mod: $alignMod"
                    logs.add(CombatLogEntry(key = "combat_log_direct", args = mapOf("text" to label)))
                }
                val statMod = if (activeLanguage.value == "TR") statModTr else statModEn
                if (statMod.isNotEmpty()) {
                    val label = if (activeLanguage.value == "TR") "🏋️ Stat Mod: $statMod" else "🏋️ Stat Mod: $statMod"
                    logs.add(CombatLogEntry(key = "combat_log_direct", args = mapOf("text" to label)))
                }
            }
 
            var playerHp = (profile.currentHp - selfHpDmg).coerceAtLeast(0)
 
            if (newEnemyHp <= 0) {
                val greedLvl = LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, LegacyUpgradeType.GREED)
                val greedMultiplier = 1.0f + (greedLvl * 0.20f)
                val rewardGold = (boss.rewardGold * greedMultiplier).toInt()
                
                var currentItems = if (profile.itemsEncoded.isEmpty()) emptyList() else profile.itemsEncoded.split(",")
                if (boss.rewardItem.isNotEmpty() && !currentItems.contains(boss.rewardItem)) {
                    currentItems = currentItems + boss.rewardItem
                }
                val newItemsEncoded = currentItems.filter { it.isNotBlank() }.joinToString(",")
 
                val updated = profile.copy(
                    gold = (profile.gold + rewardGold),
                    aether = (profile.aether + boss.rewardAether),
                    currentHp = playerHp.coerceAtMost(profile.maxHp),
                    itemsEncoded = newItemsEncoded,
                    lastUpdated = System.currentTimeMillis()
                )
                
                val titleChecked = checkAndUnlockTitles(updated)
                repository.savePlayerProfile(titleChecked)
 
                val entry = JournalEntry(
                    floor = profile.currentFloor,
                    actionTakenEs = "🔥 COGNITIVE SANCTUARY TRIUMPH: Slid the Secret Trial Overlord $bossNameEn. Earned +$rewardGold Gold, rare item '${boss.rewardItem}'.",
                    actionTakenTr = "🔥 GİZLİ MEKAN MUZAFFERİ: Esrarengiz düşman $bossNameTr bertaraf edildi. +$rewardGold Altın, nadide '${boss.rewardItem}' hazinesi kazanıldı.",
                    sideAlignmentShift = "NEUTRAL",
                    alignmentImpact = 0
                )
                repository.insertJournalEntry(entry)
 
                _slainSecretBosses.value = _slainSecretBosses.value + boss.id
                onMessage(
                    "🎉 Defeated the Trial Overlord $bossNameEn! Rare artifacts claimed.",
                    "🎉 İmtihan Derebeyi $bossNameTr mağlup edildi! Kadim ganimetler alındı."
                )
 
                _activeSecretBossCombat.value = null
                _activeSecretBossHp.value = null
                _combatLog.value = emptyList()
            } else {
                val bossAtk = boss.atk + Random.nextInt(7)
                playerHp = (playerHp - bossAtk).coerceAtLeast(0)
 
                logs.add(
                    CombatLogEntry(
                        key = "combat_log_boss_attack",
                        args = mapOf("boss" to boss.nameKey, "damage" to bossAtk.toString())
                    )
                )

                _combatLog.value = logs

                if (playerHp <= 0) {
                    onTriggerSpiritFracture(profile, profile.momentum, profile.gold, profile.aether, profile.side)
                } else {
                    val updated = profile.copy(
                        currentHp = playerHp,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.savePlayerProfile(updated)
                }
            }
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
