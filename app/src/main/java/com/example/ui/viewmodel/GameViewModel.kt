package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.GameDatabase
import com.example.data.engine.*
import com.example.data.model.JournalEntry
import com.example.data.model.PlayerProfile
import com.example.data.model.EnemyFaction
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val playerProfile: StateFlow<PlayerProfile?>
    val journalEntries: StateFlow<List<JournalEntry>>

    // Display and notification preferences state
    private val _themeSelection = MutableStateFlow("ALIGNMENT")
    val themeSelection: StateFlow<String> = _themeSelection.asStateFlow()

    private val _showNotificationBanner = MutableStateFlow(true)
    val showNotificationBanner: StateFlow<Boolean> = _showNotificationBanner.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _showTitlePrefix = MutableStateFlow(true)
    val showTitlePrefix: StateFlow<Boolean> = _showTitlePrefix.asStateFlow()

    // Combined activeThemeSide flow
    val activeThemeSide: StateFlow<String>

    // Game interface states
    private val _activeLanguage = MutableStateFlow("EN") // "TR" or "EN"
    val activeLanguage: StateFlow<String> = _activeLanguage.asStateFlow()

    private val _currentScenario = MutableStateFlow<FloorScenario?>(null)
    val currentScenario: StateFlow<FloorScenario?> = _currentScenario.asStateFlow()

    private val _lastActionMessageEn = MutableStateFlow("Welcome, hero. Your ascent begins.")
    val lastActionMessageEn: StateFlow<String> = _lastActionMessageEn.asStateFlow()

    private val _lastActionMessageTr = MutableStateFlow("Hoş geldin, kahraman. Yükselişin başlıyor.")
    val lastActionMessageTr: StateFlow<String> = _lastActionMessageTr.asStateFlow()

    private val _currentTab = MutableStateFlow("TOWER") // "TOWER", "OUTER_WORLD", "CHAR_SHEET", "JOURNAL"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Interactive adventure and combat nodes
    private val _currentFloorNodes = MutableStateFlow<List<AdventureNode>>(emptyList())
    val currentFloorNodes: StateFlow<List<AdventureNode>> = _currentFloorNodes.asStateFlow()

    private val _activeEnemyHp = MutableStateFlow<Int?>(null)
    val activeEnemyHp: StateFlow<Int?> = _activeEnemyHp.asStateFlow()

    private val _combatLog = MutableStateFlow<List<String>>(emptyList())
    val combatLog: StateFlow<List<String>> = _combatLog.asStateFlow()

    private val _isAdWatching = MutableStateFlow(false)
    val isAdWatching: StateFlow<Boolean> = _isAdWatching.asStateFlow()

    private val _adCooldownSeconds = MutableStateFlow(0)
    val adCooldownSeconds: StateFlow<Int> = _adCooldownSeconds.asStateFlow()

    private val _isPurchaseDialogShown = MutableStateFlow(false)
    val isPurchaseDialogShown: StateFlow<Boolean> = _isPurchaseDialogShown.asStateFlow()

    private val _completedEvents = MutableStateFlow<Set<String>>(emptySet())
    val completedEvents: StateFlow<Set<String>> = _completedEvents.asStateFlow()

    private val _slainSecretBosses = MutableStateFlow<Set<String>>(emptySet())
    val slainSecretBosses: StateFlow<Set<String>> = _slainSecretBosses.asStateFlow()

    private val _activeNarrativeEvent = MutableStateFlow<NarrativeEvent?>(null)
    val activeNarrativeEvent: StateFlow<NarrativeEvent?> = _activeNarrativeEvent.asStateFlow()

    private val _activeSecretBossCombat = MutableStateFlow<SecretBossEncounter?>(null)
    val activeSecretBossCombat: StateFlow<SecretBossEncounter?> = _activeSecretBossCombat.asStateFlow()

    private val _activeSecretBossHp = MutableStateFlow<Int?>(null)
    val activeSecretBossHp: StateFlow<Int?> = _activeSecretBossHp.asStateFlow()

    private val _scoutedNodeIndices = MutableStateFlow<Set<Int>>(emptySet())
    val scoutedNodeIndices: StateFlow<Set<Int>> = _scoutedNodeIndices.asStateFlow()

    init {
        LocalizationManager.init(application)
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())

        // Load persisted settings from SharedPreferences
        val prefs = application.getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        _themeSelection.value = prefs.getString("themeSelection", "ALIGNMENT") ?: "ALIGNMENT"
        _showNotificationBanner.value = prefs.getBoolean("showNotificationBanner", true)
        _soundEnabled.value = prefs.getBoolean("soundEnabled", true)
        _showTitlePrefix.value = prefs.getBoolean("showTitlePrefix", true)

        playerProfile = repository.playerProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        journalEntries = repository.journalEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeThemeSide = combine(
            repository.playerProfile,
            _themeSelection
        ) { profile, themeSel ->
            when (themeSel) {
                "LIGHT" -> "SANCTUM"
                "ABYSS" -> "COVENANT"
                else -> { // "ALIGNMENT"
                    val alignment = profile?.alignment ?: 0
                    if (alignment >= 0) "SANCTUM" else "COVENANT"
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "SANCTUM"
        )

        // Automatically load procedurally generated floor nodes when player profile loads
        viewModelScope.launch {
            var lastLoadedFloor = -1
            repository.playerProfile.collect { profile ->
                if (profile != null) {
                    if (profile.currentFloor != lastLoadedFloor) {
                        lastLoadedFloor = profile.currentFloor
                        _scoutedNodeIndices.value = emptySet()
                    }
                    // Automatically run title checks if any status preconditions are met
                    val checkedProfile = checkAndUnlockTitles(profile)
                    if (checkedProfile.titlesEncoded != profile.titlesEncoded) {
                        repository.savePlayerProfile(checkedProfile)
                        return@collect
                    }

                    val nodes = AdventureEngine.generateNodesForFloor(profile.currentFloor, profile)
                    _currentFloorNodes.value = nodes
                    
                    // Initialize enemy HP if on an incomplete combat/boss node
                    if (profile.currentNodeIndex in nodes.indices) {
                        val activeNode = nodes[profile.currentNodeIndex]
                        if ((activeNode.type == NodeType.COMBAT || activeNode.type == NodeType.BOSS) && !profile.currentNodeCompleted && _activeEnemyHp.value == null) {
                            _activeEnemyHp.value = activeNode.enemyHp
                            _combatLog.value = listOf(
                                "TR" to "⚔️ ${activeNode.enemyNameTr} ile kule yolunda savaşa girdiniz! Can: ${activeNode.enemyHp}",
                                "EN" to "⚔️ Engaged in battle with ${activeNode.enemyNameEn}! HP: ${activeNode.enemyHp}"
                            ).map { if (_activeLanguage.value == "TR") it.first else it.second }
                        }
                    }
                }
            }
        }

        // Ensure default player exists on launch
        viewModelScope.launch {
            val direct = repository.getPlayerProfileDirect()
            val initialLang = _activeLanguage.value
            if (direct == null) {
                val newProfile = PlayerProfile(
                    playerName = "Lord Alistair",
                    currentFloor = 1,
                    currentHp = 100,
                    maxHp = 100,
                    gold = 150,
                    side = "NEUTRAL",
                    level = 1,
                    exp = 0,
                    maxExp = 100,
                    currentWill = 10,
                    maxWill = 10,
                    itemsEncoded = "",
                    titlesEncoded = "",
                    currentNodeIndex = 0,
                    currentNodeCompleted = false
                )
                repository.savePlayerProfile(newProfile)
                _currentScenario.value = LocalizationManager.getScenarioForFloor(initialLang, 1)
            } else {
                _currentScenario.value = LocalizationManager.getScenarioForFloor(initialLang, direct.currentFloor)
            }
        }
    }

    fun changeLanguage(lang: String) {
        _activeLanguage.value = lang
        // Automatically refresh scenario with newly loaded language strings
        viewModelScope.launch {
            val direct = repository.getPlayerProfileDirect()
            if (direct != null) {
                _currentScenario.value = LocalizationManager.getScenarioForFloor(lang, direct.currentFloor)
            }
        }
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun setPlayerName(name: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: PlayerProfile()
            val updated = profile.copy(playerName = name, lastUpdated = System.currentTimeMillis())
            repository.savePlayerProfile(updated)
        }
    }

    fun selectFaction(faction: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: PlayerProfile()
            val updated = profile.copy(
                side = faction,
                chosenClass = calculatePlayerClass(faction, profile.alignment),
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            _lastActionMessageEn.value = "You swore allegiance to the $faction faction!"
            _lastActionMessageTr.value = "Yeni bağlılık andın: $faction!"
        }
    }

    fun handleRpgChoice(option: GameOption) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            
            // Calculate status adjustments
            val newAlignment = (profile.alignment + option.alignmentShift).coerceIn(-100, 100)
            val newGold = (profile.gold + option.goldChange).coerceAtLeast(0)
            val newGleam = (profile.gleam + option.gleamChange).coerceAtLeast(0)
            val newPyre = (profile.pyre + option.pyreChange).coerceAtLeast(0)
            var newHp = profile.currentHp + option.hpChange
            
            val activeFactionSide = if (profile.side == "NEUTRAL" && option.alignmentShift != 0) {
                // Automagic alignment detection
                if (option.alignmentShift > 0 && profile.alignment > 20) "SANCTUM"
                else if (option.alignmentShift < 0 && profile.alignment < -20) "COVENANT"
                else "NEUTRAL"
            } else {
                profile.side
            }

            // Write into our localized log
            val logEntry = JournalEntry(
                floor = profile.currentFloor,
                actionTakenEs = option.journalEn,
                actionTakenTr = option.journalTr,
                sideAlignmentShift = if (option.alignmentShift > 0) "SANCTUM" else if (option.alignmentShift < 0) "COVENANT" else "NEUTRAL",
                alignmentImpact = option.alignmentShift
            )
            repository.insertJournalEntry(logEntry)

            _lastActionMessageEn.value = option.journalEn
            _lastActionMessageTr.value = option.journalTr

            // Check for death (Spirit Fracture!)
            if (newHp <= 0) {
                // Spirit Fracture occurs
                val fractureCount = profile.totalFractures + 1
                val rollbackFloor = profile.savedFloorCheckpoint // Back to outer checkpoint
                
                val updatedProfile = profile.copy(
                    alignment = newAlignment,
                    gold = newGold,
                    gleam = (newGleam / 2), // Lose half current faction currencies due to void shatter
                    pyre = (newPyre / 2),
                    currentHp = 50, // Reincarnate with 50% HP
                    currentFloor = rollbackFloor,
                    totalFractures = fractureCount,
                    chosenClass = calculatePlayerClass(activeFactionSide, newAlignment),
                    rank = calculateRank(rollbackFloor),
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updatedProfile)
                _currentScenario.value = LocalizationManager.getScenarioForFloor(_activeLanguage.value, rollbackFloor)
                
                _lastActionMessageEn.value = "💀 Spirit Fracture triggered! Slipped from Tower back to checkpoint Floor $rollbackFloor."
                _lastActionMessageTr.value = "💀 Ruh Kırılması Yaşandı! Kule'den güvenli koridor kontrol noktası Kat $rollbackFloor seviyesine savruldun."
                _currentTab.value = "OUTER_WORLD" // Go to outer world to rest/heal
            } else {
                // Clean progression
                val nextFloor = profile.currentFloor + 1
                val isCheckpoint = (nextFloor % 10 == 0)
                val newCheckpoint = if (isCheckpoint) nextFloor else profile.savedFloorCheckpoint

                val nextRank = calculateRank(nextFloor)
                val updatedProfile = profile.copy(
                    alignment = newAlignment,
                    gold = newGold,
                    gleam = newGleam,
                    pyre = newPyre,
                    currentHp = newHp.coerceAtMost(profile.maxHp),
                    currentFloor = nextFloor,
                    savedFloorCheckpoint = newCheckpoint,
                    chosenClass = calculatePlayerClass(activeFactionSide, newAlignment),
                    rank = nextRank,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updatedProfile)
                
                if (nextFloor <= 100) {
                    _currentScenario.value = LocalizationManager.getScenarioForFloor(_activeLanguage.value, nextFloor)
                } else {
                    _currentScenario.value = null // Ultimate completion!
                    _lastActionMessageEn.value = "🎉 Absolute Ascent complete! You have conquered the Sovereign Throne!"
                    _lastActionMessageTr.value = "🎉 Mutlak Yükseliş Tamamlandı! Hükümdar Tahtını fethettiniz!"
                }
            }
        }
    }

    fun healAndRest(cost: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            if (profile.gold >= cost) {
                val updated = profile.copy(
                    gold = profile.gold - cost,
                    currentHp = profile.maxHp,
                    currentWill = profile.maxWill, // Rest completely restores Daily Will limit too!
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                _lastActionMessageEn.value = "💖 RESTORED: Completely recovered life force and Will power in the Outer Haven."
                _lastActionMessageTr.value = "💖 DOYUM: Dış Sığınak kaplıcalarında yaşam gücü ve İrade tamamen yenilendi."
            } else {
                _lastActionMessageEn.value = "❌ INSUFFICIENT GOLD: Cannot afford clean medicine."
                _lastActionMessageTr.value = "❌ YETERSİZ ALTIN: Temiz tıbbi bakım için bütçeniz yetersiz."
            }
        }
    }

    fun performAbyssScouting() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            if (profile.currentHp > 15) {
                val reward = (20..50).random()
                val damage = (5..15).random()
                val isVoid = (0..1).random() == 1
                
                val updated = profile.copy(
                    gold = profile.gold + reward,
                    currentHp = profile.currentHp - damage,
                    alignment = (profile.alignment + if (isVoid) -2 else 2).coerceIn(-100, 100),
                    chosenClass = calculatePlayerClass(profile.side, (profile.alignment + if (isVoid) -2 else 2).coerceIn(-100, 100)),
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                
                _lastActionMessageEn.value = "⚔️ SCOUT: Explored Outer blighted areas. Earned +$reward Gold, took -$damage HP damage."
                _lastActionMessageTr.value = "⚔️ KEŞİF: Dış musibetli bölgeleri temizlediniz. +$reward Altın kazandınız, -$damage Hasar aldınız."
            } else {
                _lastActionMessageEn.value = "❌ SPIRIT WEAKNESS: Rest before scouting. Your current HP is dangerously low."
                _lastActionMessageTr.value = "❌ RUH ZAYIFLIĞI: Misyona çıkmadan önce dinlenin. Canınız çok düşük."
            }
        }
    }

    fun tradeCurrency(type: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            when (type) {
                "GOLD_TO_GLEAM" -> {
                    if (profile.gold >= 50) {
                        val updated = profile.copy(
                            gold = profile.gold - 50,
                            gleam = profile.gleam + 15,
                            lastUpdated = System.currentTimeMillis()
                        )
                        repository.savePlayerProfile(updated)
                        _lastActionMessageEn.value = "Gleam exchange complete (+15 Gleam, -50 Gold)."
                        _lastActionMessageTr.value = "Işıltı takası tamamlandı (+15 Işıltı, -50 Altın)."
                    } else {
                        _lastActionMessageEn.value = "Insufficient gold to secure Gleam."
                        _lastActionMessageTr.value = "Işıltı satın almak için yetersiz altın."
                    }
                }
                "GOLD_TO_PYRE" -> {
                    if (profile.gold >= 50) {
                        val updated = profile.copy(
                            gold = profile.gold - 50,
                            pyre = profile.pyre + 15,
                            lastUpdated = System.currentTimeMillis()
                        )
                        repository.savePlayerProfile(updated)
                        _lastActionMessageEn.value = "Pyre exchange complete (+15 Pyre, -50 Gold)."
                        _lastActionMessageTr.value = "Ateş takası tamamlandı (+15 Ateş, -50 Altın)."
                    } else {
                        _lastActionMessageEn.value = "Insufficient gold to secure Void Pyre."
                        _lastActionMessageTr.value = "Kara Ateş satın almak için yetersiz altın."
                    }
                }
            }
        }
    }

    fun renounceAllegiance() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            if (profile.side != "NEUTRAL") {
                val updated = profile.copy(
                    side = "NEUTRAL",
                    chosenClass = "Exiled Sovereign / Outcast",
                    alignment = 0,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                
                _lastActionMessageEn.value = "⚠️ REBEL! Renounced all factions. You are now an Outcast!"
                _lastActionMessageTr.value = "⚠️ HAİN! Tüm rütbeler ve taraflar feshedildi. Artık Sürgünsünüz!"
            }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            repository.clearJournal()
            val newProfile = PlayerProfile(
                playerName = "Lord Alistair",
                currentFloor = 1,
                currentHp = 100,
                maxHp = 100,
                gold = 150,
                side = "NEUTRAL",
                alignment = 0,
                gleam = 0,
                pyre = 0,
                rank = "EMISSARY",
                chosenClass = "Initiate",
                totalFractures = 0,
                savedFloorCheckpoint = 1,
                level = 1,
                exp = 0,
                maxExp = 100,
                currentWill = 10,
                maxWill = 10,
                itemsEncoded = "",
                titlesEncoded = "",
                currentNodeIndex = 0,
                currentNodeCompleted = false
            )
            repository.savePlayerProfile(newProfile)
            _activeEnemyHp.value = null
            _combatLog.value = emptyList()
            _completedEvents.value = emptySet()
            _slainSecretBosses.value = emptySet()
            _activeNarrativeEvent.value = null
            _activeSecretBossCombat.value = null
            _activeSecretBossHp.value = null
            _currentScenario.value = LocalizationManager.getScenarioForFloor(_activeLanguage.value, 1)
            _lastActionMessageEn.value = "Game restarted. Chronology reset."
            _lastActionMessageTr.value = "Zaman döngüsü sıfırlandı. Macera yeniden başlıyor."
            _currentTab.value = "TOWER"
        }
    }

    // Interacting with Adventure Nodes
    fun selectNodeChoice(choice: NodeChoice) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            
            val hasPass = profile.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }
            val resolvedWillChange = if (hasPass && choice.willChange < 0) 0 else choice.willChange

            // Validate Will Cost
            if (!hasPass && choice.willChange < 0 && profile.currentWill < -choice.willChange) {
                _lastActionMessageEn.value = "❌ Insufficient Willpower to make this choice!"
                _lastActionMessageTr.value = "❌ Bu kararı seçmek için yeterli İradeniz yok!"
                return@launch
            }

            val newAlignment = (profile.alignment + choice.alignmentShift).coerceIn(-100, 100)
            val newGold = (profile.gold + choice.goldChange).coerceAtLeast(0)
            val newGleam = (profile.gleam + choice.gleamChange).coerceAtLeast(0)
            val newPyre = (profile.pyre + choice.pyreChange).coerceAtLeast(0)
            val newWill = (profile.currentWill + resolvedWillChange).coerceIn(0, profile.maxWill)
            var newHp = profile.currentHp + choice.hpChange
            
            // Build item catalog list
            var currentItems = if (profile.itemsEncoded.isEmpty()) emptyList() else profile.itemsEncoded.split(",")
            if (choice.rewardItem.isNotEmpty()) {
                currentItems = currentItems + choice.rewardItem
            }
            val newItemsEncoded = currentItems.joinToString(",")

            // Build titles catalog list
            var currentTitles = if (profile.titlesEncoded.isEmpty()) emptyList() else profile.titlesEncoded.split(",")
            if (choice.rewardTitle.isNotEmpty()) {
                currentTitles = currentTitles + choice.rewardTitle
            }
            val newTitlesEncoded = currentTitles.joinToString(",")

            // Leveling Up logic
            var newExp = profile.exp + choice.expChange
            var newLevel = profile.level
            var newMaxExp = profile.maxExp
            var newMaxHp = profile.maxHp
            while (newExp >= newMaxExp && newLevel < 100) {
                newExp -= newMaxExp
                newLevel++
                newMaxExp = newLevel * 100
                newMaxHp += 20
                newHp += 20 // full level up restoration
            }

            val activeFactionSide = if (profile.side == "NEUTRAL" && choice.alignmentShift != 0) {
                if (choice.alignmentShift > 0 && profile.alignment > 20) "SANCTUM"
                else if (choice.alignmentShift < 0 && profile.alignment < -20) "COVENANT"
                else "NEUTRAL"
            } else {
                profile.side
            }

            // Record story ledger
            val logEntry = JournalEntry(
                floor = profile.currentFloor,
                actionTakenEs = choice.journalEn,
                actionTakenTr = choice.journalTr,
                sideAlignmentShift = if (choice.alignmentShift > 0) "SANCTUM" else if (choice.alignmentShift < 0) "COVENANT" else "NEUTRAL",
                alignmentImpact = choice.alignmentShift,
                nodeIndex = profile.currentNodeIndex
            )
            repository.insertJournalEntry(logEntry)

            _lastActionMessageEn.value = choice.journalEn
            _lastActionMessageTr.value = choice.journalTr

            if (newHp <= 0) {
                triggerSpiritFracture(profile, newAlignment, newGold, newGleam, newPyre, activeFactionSide)
            } else {
                var targetFloor = profile.currentFloor
                var targetNodeIndex = profile.currentNodeIndex
                var completedState = true
                var targetCheckpoint = profile.savedFloorCheckpoint
                var targetRank = profile.rank

                if (choice.skipToNextFloor) {
                    targetFloor = (profile.currentFloor + 1).coerceAtMost(100)
                    targetNodeIndex = 0
                    completedState = false
                    targetCheckpoint = if (targetFloor % 10 == 1) targetFloor else profile.savedFloorCheckpoint
                    targetRank = calculateRank(targetFloor)
                    
                    // Regenerate the floor nodes
                    val nodes = AdventureEngine.generateNodesForFloor(targetFloor, profile)
                    _currentFloorNodes.value = nodes
                    _currentScenario.value = LocalizationManager.getScenarioForFloor(_activeLanguage.value, targetFloor)

                    if (nodes.isNotEmpty()) {
                        val firstNode = nodes[0]
                        if (firstNode.type == NodeType.COMBAT || firstNode.type == NodeType.BOSS) {
                            _activeEnemyHp.value = firstNode.enemyHp
                            _combatLog.value = listOf(
                                "TR" to "⚔️ ${firstNode.enemyNameTr} ile kule yolunda savaşa girdiniz! Can: ${firstNode.enemyHp}",
                                "EN" to "⚔️ Engaged in battle with ${firstNode.enemyNameEn}! HP: ${firstNode.enemyHp}"
                            ).map { if (_activeLanguage.value == "TR") it.first else it.second }
                        } else {
                            _activeEnemyHp.value = null
                            _combatLog.value = emptyList()
                        }
                    }
                } else if (choice.skipToBoss) {
                    val nodes = _currentFloorNodes.value
                    if (nodes.isNotEmpty()) {
                        targetNodeIndex = nodes.size - 1
                        completedState = false
                        
                        val bossNode = nodes[targetNodeIndex]
                        _activeEnemyHp.value = bossNode.enemyHp
                        _combatLog.value = listOf(
                            "TR" to "⚔️ MEKAN KISAYOLU! Doğrudan kat patronu ${bossNode.enemyNameTr} ile savaşa girdiniz! Can: ${bossNode.enemyHp}",
                            "EN" to "⚔️ SPATIAL SHORTCUT! Teleported straight to battle with floor boss ${bossNode.enemyNameEn}! HP: ${bossNode.enemyHp}"
                        ).map { if (_activeLanguage.value == "TR") it.first else it.second }
                    }
                }

                val updated = profile.copy(
                    currentFloor = targetFloor,
                    currentNodeIndex = targetNodeIndex,
                    currentNodeCompleted = completedState,
                    savedFloorCheckpoint = targetCheckpoint,
                    rank = targetRank,
                    alignment = newAlignment,
                    gold = newGold,
                    gleam = newGleam,
                    pyre = newPyre,
                    currentHp = newHp.coerceAtMost(newMaxHp),
                    maxHp = newMaxHp,
                    currentWill = newWill,
                    level = newLevel,
                    exp = newExp,
                    maxExp = newMaxExp,
                    itemsEncoded = newItemsEncoded,
                    titlesEncoded = newTitlesEncoded,
                    chosenClass = calculatePlayerClass(activeFactionSide, newAlignment),
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
            }
        }
    }

    fun advanceToNextNode() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val nodes = _currentFloorNodes.value
            
            val hasPass = profile.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }

            // Sinking into a new node in sequence costs 1 Will
            if (!hasPass && profile.currentWill < 1) {
                _lastActionMessageEn.value = "❌ No Willpower! Spend 50 Gold at Outer Haven sığınağı to restore life and Will."
                _lastActionMessageTr.value = "❌ İrade Tükendi! İradenizi yenilemek için Dış Sığınak Kaplıcasında 50 Altına dinlenin."
                return@launch
            }

            val nextIndex = profile.currentNodeIndex + 1
            if (nextIndex in nodes.indices) {
                val nextNode = nodes[nextIndex]
                if (nextNode.type == NodeType.COMBAT || nextNode.type == NodeType.BOSS) {
                    _activeEnemyHp.value = nextNode.enemyHp
                    _combatLog.value = listOf(
                        "TR" to "⚔️ ${nextNode.enemyNameTr} ile kule yolunda savaşa girdiniz! Can: ${nextNode.enemyHp}",
                        "EN" to "⚔️ Engaged in battle with ${nextNode.enemyNameEn}! HP: ${nextNode.enemyHp}"
                    ).map { if (_activeLanguage.value == "TR") it.first else it.second }
                } else {
                    _activeEnemyHp.value = null
                    _combatLog.value = emptyList()
                }

                val updated = profile.copy(
                    currentNodeIndex = nextIndex,
                    currentNodeCompleted = false,
                    currentWill = if (hasPass) profile.currentWill else profile.currentWill - 1, // Deduct Will
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                _lastActionMessageEn.value = "Advanced deeper into Floor ${profile.currentFloor}. Sektor: ${nextNode.title}."
                _lastActionMessageTr.value = "${profile.currentFloor}. Katta yeni bir sektöre adım attınız: ${nextNode.titleTr}."
            }
        }
    }

    fun showActionMessage(messageEn: String, messageTr: String) {
        _lastActionMessageEn.value = messageEn
        _lastActionMessageTr.value = messageTr
    }

    fun performScouting() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val nodes = _currentFloorNodes.value
            if (nodes.isEmpty()) return@launch

            val hasPass = profile.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }
            val scoutCost = 1

            if (!hasPass && profile.currentWill < scoutCost) {
                _lastActionMessageEn.value = "❌ Insufficient Willpower to Scout (Costs $scoutCost Will)!"
                _lastActionMessageTr.value = "❌ Gözlem yapmak için yetersiz İrade (Maliyet $scoutCost İrade)!"
                return@launch
            }

            val currentIdx = profile.currentNodeIndex
            val unscoutedIndices = nodes.indices.filter { idx ->
                idx > currentIdx && !scoutedNodeIndices.value.contains(idx)
            }

            if (unscoutedIndices.isEmpty()) {
                _lastActionMessageEn.value = "🔍 All future sectors are already scouted or cleared on this floor!"
                _lastActionMessageTr.value = "🔍 Bu kattaki tüm gelecek sektörler zaten gözlendi veya temizlendi!"
                return@launch
            }

            val newlyScouted = unscoutedIndices.shuffled().take(2)
            val updatedScouted = scoutedNodeIndices.value + newlyScouted
            _scoutedNodeIndices.value = updatedScouted

            val updatedProfile = profile.copy(
                currentWill = if (hasPass) profile.currentWill else (profile.currentWill - scoutCost).coerceAtLeast(0),
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updatedProfile)

            val sectorNumsEn = newlyScouted.map { it + 1 }.joinToString(" and ") { "Sector $it" }
            val sectorNumsTr = newlyScouted.map { it + 1 }.joinToString(" ve ") { "$it. Sektör" }
            _lastActionMessageEn.value = "🔍 Scouted map! Revealed the node types for $sectorNumsEn."
            _lastActionMessageTr.value = "🔍 Harita gözetlendi! $sectorNumsTr tipi açığa çıkarıldı."
        }
    }

    fun ascendToNextFloor() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            
            val hasPass = profile.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }

            // Leaving floor costs 2 Will
            if (!hasPass && profile.currentWill < 2) {
                _lastActionMessageEn.value = "❌ Climbing to another floor costs 2 Will. Please rest first."
                _lastActionMessageTr.value = "❌ Başka bir kata geçmek için 2 İrade gücü gerekir. Kaplıcalarda dinlenin."
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
                _lastActionMessageEn.value = "Ultimate Sovereign Conquered the Cosmic Tower!"
                _lastActionMessageTr.value = "Büyük Hükümdar Kozmik Kuleyi tamamen fethetti!"
                return@launch
            }

            val isCheckpoint = (nextFloor % 10 == 0)
            val newCheckpoint = if (isCheckpoint) nextFloor else profile.savedFloorCheckpoint
            val nextRank = calculateRank(nextFloor)

            val updated = profile.copy(
                currentFloor = nextFloor,
                currentNodeIndex = 0,
                currentNodeCompleted = false,
                currentWill = if (hasPass) profile.currentWill else profile.currentWill - 2, // Deduct transit fee
                savedFloorCheckpoint = newCheckpoint,
                rank = nextRank,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            
            _activeEnemyHp.value = null
            _combatLog.value = emptyList()
            
            _lastActionMessageEn.value = "Ascended towers! Welcome to Floor $nextFloor."
            _lastActionMessageTr.value = "Boyutsal sınırları aşıp $nextFloor. Kata geçtiniz."
        }
    }

    fun initiateTransitionToFloor(targetFloor: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            when (val res = com.example.data.engine.FloorStateManager.attemptFloorTransition(profile, targetFloor)) {
                is com.example.data.engine.FloorStateManager.TransitionResult.Success -> {
                    repository.savePlayerProfile(res.updatedProfile)
                    
                    _activeEnemyHp.value = null
                    _combatLog.value = emptyList()
                    
                    val logEntry = JournalEntry(
                        floor = profile.currentFloor,
                        actionTakenEs = res.journalEn,
                        actionTakenTr = res.journalTr,
                        sideAlignmentShift = profile.side,
                        alignmentImpact = 0
                    )
                    repository.insertJournalEntry(logEntry)
                    
                    _lastActionMessageEn.value = res.messageEn
                    _lastActionMessageTr.value = res.messageTr
                }
                is com.example.data.engine.FloorStateManager.TransitionResult.Failure -> {
                    _lastActionMessageEn.value = "❌ " + res.reasonEn
                    _lastActionMessageTr.value = "❌ " + res.reasonTr
                }
            }
        }
    }

    fun executeCombatTurn(action: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val nodes = _currentFloorNodes.value
            if (profile.currentNodeIndex !in nodes.indices) return@launch
            val activeNode = nodes[profile.currentNodeIndex]
            
            val currentEnemyHp = _activeEnemyHp.value ?: activeNode.enemyHp

            // 1. Analyze Factions & Multipliers
            val enemyFaction = EnemyFaction.fromName(activeNode.enemyNameEn)
            var alignmentModEn = ""
            var alignmentModTr = ""
            var factionMod = 1.0f

            // Alignment / Side modifiers
            val isSanctum = profile.side == "SANCTUM" || profile.alignment > 20
            val isCovenant = profile.side == "COVENANT" || profile.alignment < -20

            if (isSanctum) {
                when (enemyFaction) {
                    EnemyFaction.VOID_CORRUPTION -> {
                        val sanctumBonus = 0.20f + (profile.alignment / 400.0f).coerceIn(0.0f, 0.25f)
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
                if (profile.gleam > 0) {
                    val gleamBonus = ((profile.gleam / 50) * 0.05f).coerceAtMost(0.15f)
                    if (gleamBonus > 0f) {
                        factionMod += gleamBonus
                        val pct = (gleamBonus * 100).toInt()
                        alignmentModEn = if (alignmentModEn.isEmpty()) "[✨ Gleam Amplification: +$pct%]" else alignmentModEn + " [✨ +$pct% Gleam]"
                        alignmentModTr = if (alignmentModTr.isEmpty()) "[✨ Cevher Güçlendirmesi: +$pct%]" else alignmentModTr + " [✨ +$pct% Cevher]"
                    }
                }
            } else if (isCovenant) {
                when (enemyFaction) {
                    EnemyFaction.SANCTUM_WRATH -> {
                        val voidBonus = 0.20f + (Math.abs(profile.alignment) / 400.0f).coerceIn(0.0f, 0.25f)
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
                if (profile.pyre > 0) {
                    val pyreBonus = ((profile.pyre / 50) * 0.05f).coerceAtMost(0.15f)
                    if (pyreBonus > 0f) {
                        factionMod += pyreBonus
                        val pct = (pyreBonus * 100).toInt()
                        alignmentModEn = if (alignmentModEn.isEmpty()) "[🔥 Pyre Immolation: +$pct%]" else alignmentModEn + " [🔥 +$pct% Pyre]"
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

            // Stat Modifiers: Willpower Inspired or Fatigue
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

            // Low HP Adrenaline Burst
            var adrenalineMod = 1.0f
            var enemyRetaliationMod = 1.0f
            val hpPercentage = profile.currentHp.toFloat() / profile.maxHp.toFloat()
            if (hpPercentage < 0.3f && hpPercentage > 0.0f) {
                adrenalineMod = 1.30f
                enemyRetaliationMod = 1.25f
                statModEn += if (statModEn.isEmpty()) "[🩸 Adrenaline Siege: +30% Survival Fury!]" else " [🩸 +30% Adrenaline]"
                statModTr += if (statModTr.isEmpty()) "[🩸 Can Havli: Seferberlikte +30% Hasar!]" else " [🩸 +30% Can Havli]"
            }

            // Calculate Base Strike Dmg
            val baseDmg = 12 + profile.level * 2
            var actionDescriptionEn = ""
            var actionDescriptionTr = ""
            var selfHpDmg = 0

            // Critical hit calculation based on Willpower stat
            val critChance = (10 + profile.currentWill * 4).coerceIn(10, 50)
            val isCrit = (1..100).random() <= critChance
            val criticalMultiplier = if (isCrit) 1.5f else 1.0f

            var computedDmg = 0

            when (action) {
                "STRIKE" -> {
                    val rawDmg = (baseDmg * 0.9).toInt() + (0..6).random()
                    computedDmg = (rawDmg * factionMod * statDamageMod * adrenalineMod * criticalMultiplier).toInt().coerceAtLeast(1)
                    val actualEnemyHp = (currentEnemyHp - computedDmg).coerceAtLeast(0)
                    _activeEnemyHp.value = actualEnemyHp

                    actionDescriptionEn = "You swung your weapon at ${activeNode.enemyNameEn}, dealing $computedDmg damage."
                    actionDescriptionTr = "${activeNode.enemyNameTr} üzerine hücum edip silahınızla $computedDmg hasar verdiniz."
                }
                "SKILL" -> {
                    val multiplier = if (profile.side != "NEUTRAL") 2.5f else 1.8f
                    val rawDmg = (baseDmg * multiplier).toInt() + (0..10).random()
                    computedDmg = (rawDmg * factionMod * statDamageMod * adrenalineMod * criticalMultiplier).toInt().coerceAtLeast(1)
                    val actualEnemyHp = (currentEnemyHp - computedDmg).coerceAtLeast(0)
                    _activeEnemyHp.value = actualEnemyHp

                    selfHpDmg = 10
                    val skillNames = getSkillNameForClass(profile.chosenClass)

                    actionDescriptionEn = "Unleashed Sınıf Yeteneği [${skillNames.first}], evoking $computedDmg cosmic damage (Fatigued: -$selfHpDmg HP)."
                    actionDescriptionTr = "Sınıf Yeteneği [${skillNames.second}] fırlatarak $computedDmg kozmik hasar verdiniz (Yorgunluk: -$selfHpDmg HP)."
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

                        actionDescriptionEn = "Swallowed a quick Sığınak Flask (-20 Gold), recovering +$healedValue HP."
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

            val newEnemyHp = _activeEnemyHp.value ?: 0
            val logs = ArrayList<String>()
            
            // Log core action
            if (_activeLanguage.value == "TR") {
                logs.add("👤 $actionDescriptionTr")
                if (isCrit && action != "POTION") {
                    logs.add("💥 KRİTİK DOĞRUDAN DARBE! (%${critChance} şans ile x1.5 hasar!)")
                }
                if (alignmentModTr.isNotEmpty() && action != "POTION") {
                    logs.add("🛡️ Faksiyon Mod: $alignmentModTr")
                }
                if (statModTr.isNotEmpty() && action != "POTION") {
                    logs.add("🏋️ Stat Mod: $statModTr")
                }
            } else {
                logs.add("👤 $actionDescriptionEn")
                if (isCrit && action != "POTION") {
                    logs.add("💥 CRITICAL DIRECT BURST! (Chance: $critChance%, dealt x1.5 damage!)")
                }
                if (alignmentModEn.isNotEmpty() && action != "POTION") {
                    logs.add("🛡️ Faction Mod: $alignmentModEn")
                }
                if (statModEn.isNotEmpty() && action != "POTION") {
                    logs.add("🏋️ Stat Mod: $statModEn")
                }
            }

            var playerHp = (profile.currentHp - selfHpDmg).coerceAtLeast(0)

            if (newEnemyHp <= 0) {
                // Victory! Compute drops and EXP scaling via our brand new RewardGenerator
                val rewards = RewardGenerator.generateRewards(
                    player = profile.copy(currentHp = playerHp),
                    isBoss = activeNode.type == NodeType.BOSS
                )

                _activeEnemyHp.value = null
                
                // Append drops to profile serialization
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

                val updatedProfile = profile.copy(
                    currentHp = rewards.finalHp,
                    maxHp = rewards.newMaxHp,
                    level = rewards.newLevel,
                    exp = rewards.newExp,
                    maxExp = rewards.newMaxExp,
                    gold = profile.gold + rewards.goldGained,
                    itemsEncoded = newItemsEncoded,
                    titlesEncoded = newTitlesEncoded,
                    currentNodeCompleted = true,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updatedProfile)

                if (_activeLanguage.value == "TR") {
                    logs.add("🎉 ZAFER! Düşman katledildi. +${rewards.expGained} Deneyim, +${rewards.goldGained} Altın kazanıldı.")
                    if (rewards.itemAwarded != null) logs.add("🎁 Hazine: [${rewards.itemAwarded}] toplandı teçhizat torbana konuldu!")
                    if (rewards.titleAwarded != null) logs.add("👑 Yeni Unvan Kazandınız: [${rewards.titleAwarded}]!")
                    if (rewards.didLevelUp) logs.add("⚡ SEVİYE ATLADINIZ! Seviye ${rewards.newLevel} oldunuz! Maksimum Canınız arttı.")
                } else {
                    logs.add("🎉 VICTORY! Enemy defeated. Won +${rewards.expGained} EXP, +${rewards.goldGained} Gold.")
                    if (rewards.itemAwarded != null) logs.add("🎁 Loot Pick: [${rewards.itemAwarded}] added to inventory!")
                    if (rewards.titleAwarded != null) logs.add("👑 Achieved Epic Title: [${rewards.titleAwarded}]!")
                    if (rewards.didLevelUp) logs.add("⚡ LEVEL UP! Reached Level ${rewards.newLevel}! Max HP increased.")
                }

                // Insert into chronology db
                val journalEntry = JournalEntry(
                    floor = profile.currentFloor,
                    actionTakenEs = "Defeated ${activeNode.enemyNameEn} in combat on Floor ${profile.currentFloor}.",
                    actionTakenTr = "${profile.currentFloor}. Katta ${activeNode.enemyNameTr} karşısındaki düelloyu kazandınız.",
                    sideAlignmentShift = "NEUTRAL",
                    alignmentImpact = 0,
                    nodeIndex = profile.currentNodeIndex
                )
                repository.insertJournalEntry(journalEntry)

                _combatLog.value = _combatLog.value + logs
                _lastActionMessageEn.value = "Defeated ${activeNode.enemyNameEn}!"
                _lastActionMessageTr.value = "${activeNode.enemyNameTr} yok edildi!"
            } else {
                // Enemy retaliates
                val enemyDmg = ((activeNode.enemyAtk * 0.85).toInt() + (0..4).random() * enemyRetaliationMod).toInt()
                playerHp = (playerHp - enemyDmg).coerceAtLeast(0)

                if (_activeLanguage.value == "TR") {
                    logs.add("👿 Canavar saldırdı ve size $enemyDmg hasar verdi!")
                } else {
                    logs.add("👿 Enemy struck back, dealing $enemyDmg damage to you!")
                }

                if (playerHp <= 0) {
                    triggerSpiritFracture(profile, profile.alignment, profile.gold, profile.gleam, profile.pyre, profile.side)
                } else {
                    val updatedProfile = profile.copy(
                        currentHp = playerHp,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.savePlayerProfile(updatedProfile)
                    _combatLog.value = _combatLog.value + logs
                }
            }
        }
    }

    private suspend fun triggerSpiritFracture(
        profile: PlayerProfile,
        newAlignment: Int,
        newGold: Int,
        newGleam: Int,
        newPyre: Int,
        factionSide: String
    ) {
        val fractureCount = profile.totalFractures + 1
        val rollbackFloor = profile.savedFloorCheckpoint

        val updated = profile.copy(
            alignment = newAlignment,
            gold = newGold,
            gleam = newGleam / 2,
            pyre = newPyre / 2,
            currentHp = profile.maxHp / 2, // Reincarnate with 50% HP
            currentFloor = rollbackFloor,
            currentNodeIndex = 0, // Reset progression to beginning of safety checkpoint
            currentNodeCompleted = false,
            totalFractures = fractureCount,
            chosenClass = calculatePlayerClass(factionSide, newAlignment),
            rank = calculateRank(rollbackFloor),
            lastUpdated = System.currentTimeMillis()
        )
        repository.savePlayerProfile(updated)

        _lastActionMessageEn.value = "💀 Spirit Fracture triggered! Slipped from Tower back to checkpoint Floor $rollbackFloor."
        _lastActionMessageTr.value = "💀 Ruh Kırılması Yaşandı! Kule'den güvenli koridor kontrol noktası Kat $rollbackFloor seviyesine savruldun."
        _currentTab.value = "OUTER_WORLD"
        _activeEnemyHp.value = null
        _combatLog.value = emptyList()
    }

    fun setPurchaseDialogShown(shown: Boolean) {
        _isPurchaseDialogShown.value = shown
    }

    fun setThemeSelection(selection: String) {
        _themeSelection.value = selection
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("themeSelection", selection).apply()
    }

    fun setShowNotificationBanner(show: Boolean) {
        _showNotificationBanner.value = show
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("showNotificationBanner", show).apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("soundEnabled", enabled).apply()
    }

    fun setShowTitlePrefix(show: Boolean) {
        _showTitlePrefix.value = show
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("showTitlePrefix", show).apply()
    }

    private var cooldownJob: kotlinx.coroutines.Job? = null

    fun watchRewardedAd() {
        val currentCd = _adCooldownSeconds.value
        if (currentCd > 0 || _isAdWatching.value) return
        
        viewModelScope.launch {
            _isAdWatching.value = true
            _lastActionMessageEn.value = "🎬 [Simulating Rewarded Video Ad] Enjoy the sponsor preview for 5 seconds..."
            _lastActionMessageTr.value = "🎬 [Ödüllü Reklam Simüle Ediliyor] Kule sponsorlu geçişini 5 saniye izleyin..."
            
            kotlinx.coroutines.delay(5000)
            
            val profile = repository.getPlayerProfileDirect()
            if (profile != null) {
                val newWill = (profile.currentWill + 5).coerceAtMost(profile.maxWill)
                val updated = profile.copy(
                    currentWill = newWill,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                _lastActionMessageEn.value = "🎬 Ad Completed! Restored +5 Willpower ⚡. Ready to climb the spire!"
                _lastActionMessageTr.value = "🎬 Reklam Tamamlandı! +5 İrade Gücü Yenilendi ⚡. Kulvarda tırmanmaya hazırsın!"
            }
            _isAdWatching.value = false
            
            // Start cooldown
            cooldownJob?.cancel()
            _adCooldownSeconds.value = 60
            cooldownJob = viewModelScope.launch {
                while (_adCooldownSeconds.value > 0) {
                    kotlinx.coroutines.delay(1000)
                    _adCooldownSeconds.value -= 1
                }
            }
        }
    }

    fun purchaseProduct(skuId: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            var updated = profile
            when (skuId) {
                "pack_elixir" -> {
                    // Small Willpower elixir (+10 Will, can overflow max for premium)
                    val newWill = (profile.currentWill + 10).coerceAtMost(99)
                    updated = profile.copy(
                        currentWill = newWill,
                        gold = profile.gold + 50, // bonus gold
                        lastUpdated = System.currentTimeMillis()
                    )
                    _lastActionMessageEn.value = "💳 Purchased Willpower Elixir Pack ($1.99)! Added +10 Willpower ⚡ & +50 Gold 🪙"
                    _lastActionMessageTr.value = "💳 İrade İksiri Paketi ($1.99) Alındı! +10 İrade ⚡ ve +50 Altın 🪙 eklendi."
                }
                "pack_chest" -> {
                    // Large Willpower chest (+40 Will, can overflow up to 99)
                    val newWill = (profile.currentWill + 40).coerceAtMost(99)
                    updated = profile.copy(
                        currentWill = newWill,
                        gold = profile.gold + 200, // bonus gold
                        lastUpdated = System.currentTimeMillis()
                    )
                    _lastActionMessageEn.value = "💳 Purchased Chest of Sovereign Will ($3.99)! Added +40 Willpower ⚡ & +200 Gold 🪙"
                    _lastActionMessageTr.value = "💳 Büyük Hükümdar İrade Sandığı ($3.99) Alındı! +40 İrade ⚡ ve +200 Altın 🪙 eklendi."
                }
                "season_pass" -> {
                    // UNLIMITED WILLPOWER! Unlocks persistent Item "Seasonal Sovereign Pass"
                    val items = profile.itemsEncoded.split(",").filter { it.isNotBlank() }.toMutableList()
                    if (!items.contains("Seasonal Sovereign Pass")) {
                        items.add("Seasonal Sovereign Pass")
                    }
                    val newItems = items.joinToString(",")
                    updated = profile.copy(
                        itemsEncoded = newItems,
                        currentWill = 99,
                        maxWill = 99,
                        lastUpdated = System.currentTimeMillis()
                    )
                    _lastActionMessageEn.value = "👑 UNLOCKED: Seasonal Sovereign Pass ($4.99)! Infinite Willpower Active! Spire costs are completely 0!"
                    _lastActionMessageTr.value = "👑 AKTİF: Sezonluk Hükümdar Kartı ($4.99)! Sınırsız İrade Aktif! Kule tırmanış ücreti artık Sıfır (0)!"
                }
            }
            repository.savePlayerProfile(updated)
            _isPurchaseDialogShown.value = false
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

    private fun calculatePlayerClass(side: String, alignment: Int): String {
        return getPlayerClassString(side, alignment)
    }

    private fun calculateRank(floor: Int): String {
        return when {
            floor >= 100 -> "SOVEREIGN"
            floor >= 25 -> "EXARCH"
            floor >= 10 -> "ARBITER"
            else -> "EMISSARY"
        }
    }

    /**
     * Checks all titles for qualification and automatically registers them to the player's profile if unlocked.
     */
    fun checkAndUnlockTitles(profile: PlayerProfile): PlayerProfile {
        val currentUnlocked = profile.titlesEncoded.split(",").filter { it.isNotBlank() }.toMutableSet()
        var changed = false
        
        com.example.data.engine.QuestTitleSystem.titles.forEach { title ->
            if (!currentUnlocked.contains(title.id) && title.meetsPreconditions(profile)) {
                currentUnlocked.add(title.id)
                changed = true
                viewModelScope.launch {
                    _lastActionMessageEn.value = "👑 UNLOCKED TITLE: ${title.nameEn}!"
                    _lastActionMessageTr.value = "👑 YENİ UNVAN KAZANILDI: ${title.nameTr}!"
                }
            }
        }
        
        return if (changed) {
            profile.copy(
                titlesEncoded = currentUnlocked.joinToString(","),
                lastUpdated = System.currentTimeMillis()
            )
        } else {
            profile
        }
    }

    /**
     * Equips a specific title that the user has currently unlocked.
     */
    fun equipTitle(titleId: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val unlocked = profile.titlesEncoded.split(",").filter { it.isNotBlank() }.toSet()
            if (titleId.isEmpty() || unlocked.contains(titleId)) {
                val updated = profile.copy(
                    equippedTitle = titleId,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                _lastActionMessageEn.value = if (titleId.isEmpty()) "Unequipped title." else "Equipped: ${com.example.data.engine.QuestTitleSystem.getTitleDef(titleId)?.nameEn}"
                _lastActionMessageTr.value = if (titleId.isEmpty()) "Unvan çıkarıldı." else "Mistik unvan kuşanıldı: ${com.example.data.engine.QuestTitleSystem.getTitleDef(titleId)?.nameTr}"
            }
        }
    }

    /**
     * Claims the rewards for a completed quest, awarding Gold, Experience, Items, and/or Titles.
     */
    fun claimQuestReward(questId: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val quest = com.example.data.engine.QuestTitleSystem.quests.find { it.id == questId } ?: return@launch
            
            val completedSet = profile.completedQuestsEncoded.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (completedSet.contains(questId)) return@launch
            
            if (!quest.checkProgress(profile)) return@launch
            
            completedSet.add(questId)
            
            // Calculate base currency rewards
            val newGold = profile.gold + quest.rewardGold
            val newGleam = profile.gleam + quest.rewardGleam
            val newPyre = profile.pyre + quest.rewardPyre
            
            // Experience and levels
            var newExp = profile.exp + quest.rewardExp
            var newLevel = profile.level
            var newMaxExp = profile.maxExp
            var newMaxHp = profile.maxHp
            
            while (newExp >= newMaxExp && newLevel < 100) {
                newExp -= newMaxExp
                newLevel++
                newMaxExp += 25
                newMaxHp += 15
            }
            
            // Appends items
            val currentItems = profile.itemsEncoded.split(",").filter { it.isNotBlank() }.toMutableList()
            quest.rewardItem?.let { item ->
                if (!currentItems.contains(item)) {
                    currentItems.add(item)
                }
            }
            
            // Appends titles
            val currentTitles = profile.titlesEncoded.split(",").filter { it.isNotBlank() }.toMutableList()
            quest.rewardTitle?.let { title ->
                if (!currentTitles.contains(title)) {
                    currentTitles.add(title)
                }
            }
            
            var updated = profile.copy(
                completedQuestsEncoded = completedSet.joinToString(","),
                gold = newGold,
                gleam = newGleam,
                pyre = newPyre,
                level = newLevel,
                exp = newExp,
                maxExp = newMaxExp,
                maxHp = newMaxHp,
                itemsEncoded = currentItems.joinToString(","),
                titlesEncoded = currentTitles.joinToString(","),
                lastUpdated = System.currentTimeMillis()
            )
            
            // Run automatic checks on newly unlocked titles
            updated = checkAndUnlockTitles(updated)
            
            repository.savePlayerProfile(updated)
            
            _lastActionMessageEn.value = "Quest '${quest.titleEn}' claimed! Rewards received."
            _lastActionMessageTr.value = "'${quest.titleTr}' görevi tamamlandı! Ödülleri aldınız."
        }
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
            
            // Process choice using custom NarrativeEventProcessor
            val updated = NarrativeEventProcessor.processNarrativeChoice(profile, choice)
            
            // Unlocked titles auto-evaluation
            val titleChecked = checkAndUnlockTitles(updated)
            
            repository.savePlayerProfile(titleChecked)
            
            // Insert journal entry
            val entry = JournalEntry(
                floor = profile.currentFloor,
                actionTakenEs = "Decided '${choice.textEn}' inside scenario '${event.titleEn}'. Outcome: ${choice.outcomeEn}",
                actionTakenTr = "'${event.titleTr}' içindeki kararın: '${choice.textTr}'. Sonuç: ${choice.outcomeTr}",
                sideAlignmentShift = if (choice.alignmentImpact > 0) "SANCTUM" else if (choice.alignmentImpact < 0) "COVENANT" else "NEUTRAL",
                alignmentImpact = choice.alignmentImpact
            )
            repository.insertJournalEntry(entry)

            _completedEvents.value = _completedEvents.value + event.id
            _lastActionMessageEn.value = choice.outcomeEn
            _lastActionMessageTr.value = choice.outcomeTr
            
            _activeNarrativeEvent.value = null
        }
    }

    fun startSecretBossEncounter(boss: SecretBossEncounter) {
        _activeSecretBossCombat.value = boss
        _activeSecretBossHp.value = boss.hp
        _combatLog.value = listOf(
            "TR" to "⚔️ GİZLİ TEHLİKE! Kadim ${boss.nameTr} ile kutsal savaşa tutuştunuz! Can: ${boss.hp}",
            "EN" to "⚔️ SECRET THREAT! Engaged in legendary trial with ${boss.nameEn}! HP: ${boss.hp}"
        ).map { if (_activeLanguage.value == "TR") it.first else it.second }
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

            // 1. Analyze Factions & Multipliers
            val enemyFaction = EnemyFaction.fromName(boss.nameEn)
            var alignmentModEn = ""
            var alignmentModTr = ""
            var factionMod = 1.0f

            // Alignment / Side modifiers
            val isSanctum = profile.side == "SANCTUM" || profile.alignment > 20
            val isCovenant = profile.side == "COVENANT" || profile.alignment < -20

            if (isSanctum) {
                when (enemyFaction) {
                    EnemyFaction.VOID_CORRUPTION -> {
                        val sanctumBonus = 0.20f + (profile.alignment / 400.0f).coerceIn(0.0f, 0.25f)
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
                if (profile.gleam > 0) {
                    val gleamBonus = ((profile.gleam / 50) * 0.05f).coerceAtMost(0.15f)
                    if (gleamBonus > 0f) {
                        factionMod += gleamBonus
                        val pct = (gleamBonus * 100).toInt()
                        alignmentModEn = if (alignmentModEn.isEmpty()) "[✨ Gleam Amplification: +$pct%]" else alignmentModEn + " [✨ +$pct% Gleam]"
                        alignmentModTr = if (alignmentModTr.isEmpty()) "[✨ Cevher Güçlendirmesi: +$pct%]" else alignmentModTr + " [✨ +$pct% Cevher]"
                    }
                }
            } else if (isCovenant) {
                when (enemyFaction) {
                    EnemyFaction.SANCTUM_WRATH -> {
                        val voidBonus = 0.20f + (Math.abs(profile.alignment) / 400.0f).coerceIn(0.0f, 0.25f)
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
                if (profile.pyre > 0) {
                    val pyreBonus = ((profile.pyre / 50) * 0.05f).coerceAtMost(0.15f)
                    if (pyreBonus > 0f) {
                        factionMod += pyreBonus
                        val pct = (pyreBonus * 100).toInt()
                        alignmentModEn = if (alignmentModEn.isEmpty()) "[🔥 Pyre Immolation: +$pct%]" else alignmentModEn + " [🔥 +$pct% Pyre]"
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

            // Stat Modifiers: Willpower Inspired or Fatigue
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

            // Low HP Adrenaline Burst
            var adrenalineMod = 1.0f
            var enemyRetaliationMod = 1.0f
            val hpPercentage = profile.currentHp.toFloat() / profile.maxHp.toFloat()
            if (hpPercentage < 0.3f && hpPercentage > 0.0f) {
                adrenalineMod = 1.30f
                enemyRetaliationMod = 1.25f
                statModEn += if (statModEn.isEmpty()) "[🩸 Adrenaline Siege: +30% Survival Fury!]" else " [🩸 +30% Adrenaline]"
                statModTr += if (statModTr.isEmpty()) "[🩸 Can Havli: Seferberlikte +30% Hasar!]" else " [🩸 +30% Can Havli]"
            }

            // Calculate Base Strike Dmg
            val baseDmg = 12 + profile.level * 2
            var actionDescriptionEn = ""
            var actionDescriptionTr = ""
            var selfHpDmg = 0

            // Critical hit calculation based on Willpower stat
            val critChance = (10 + profile.currentWill * 4).coerceIn(10, 50)
            val isCrit = (1..100).random() <= critChance
            val criticalMultiplier = if (isCrit) 1.5f else 1.0f

            var computedDmg = 0

            when (action) {
                "STRIKE" -> {
                    val rawDmg = (baseDmg * 0.9).toInt() + (0..6).random()
                    computedDmg = (rawDmg * factionMod * statDamageMod * adrenalineMod * criticalMultiplier).toInt().coerceAtLeast(1)
                    val actualEnemyHp = (currentEnemyHp - computedDmg).coerceAtLeast(0)
                    _activeSecretBossHp.value = actualEnemyHp

                    actionDescriptionEn = "You swung your weapon at secret boss ${boss.nameEn}, dealing $computedDmg damage."
                    actionDescriptionTr = "Gizli patron ${boss.nameTr} üzerine kılıç savurup $computedDmg hasar verdiniz."
                }
                "SKILL" -> {
                    val multiplier = if (profile.side != "NEUTRAL") 2.5f else 1.8f
                    val rawDmg = (baseDmg * multiplier).toInt() + (0..10).random()
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

                        actionDescriptionEn = "Swallowed a quick Sığınak Flask (-20 Gold), recovering +$healedValue HP."
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
            val logs = ArrayList<String>()
            
            // Log core action
            if (_activeLanguage.value == "TR") {
                logs.add("👤 $actionDescriptionTr")
                if (isCrit && action != "POTION") {
                    logs.add("💥 KRİTİK DOĞRUDAN DARBE! (%${critChance} şans ile x1.5 hasar!)")
                }
                if (alignmentModTr.isNotEmpty() && action != "POTION") {
                    logs.add("🛡️ Faksiyon Mod: $alignmentModTr")
                }
                if (statModTr.isNotEmpty() && action != "POTION") {
                    logs.add("🏋️ Stat Mod: $statModTr")
                }
            } else {
                logs.add("👤 $actionDescriptionEn")
                if (isCrit && action != "POTION") {
                    logs.add("💥 CRITICAL DIRECT BURST! (Chance: $critChance%, dealt x1.5 damage!)")
                }
                if (alignmentModEn.isNotEmpty() && action != "POTION") {
                    logs.add("🛡️ Faction Mod: $alignmentModEn")
                }
                if (statModEn.isNotEmpty() && action != "POTION") {
                    logs.add("🏋️ Stat Mod: $statModEn")
                }
            }

            var playerHp = (profile.currentHp - selfHpDmg).coerceAtLeast(0)

            if (newEnemyHp <= 0) {
                // Secret Boss Defeated!
                val rewardGold = boss.rewardGold
                var currentItems = if (profile.itemsEncoded.isEmpty()) emptyList() else profile.itemsEncoded.split(",")
                if (boss.rewardItem.isNotEmpty() && !currentItems.contains(boss.rewardItem)) {
                    currentItems = currentItems + boss.rewardItem
                }
                val newItemsEncoded = currentItems.filter { it.isNotBlank() }.joinToString(",")

                val updated = profile.copy(
                    gold = (profile.gold + rewardGold),
                    gleam = (profile.gleam + boss.rewardGleam),
                    pyre = (profile.pyre + boss.rewardPyre),
                    currentHp = playerHp.coerceAtMost(profile.maxHp),
                    itemsEncoded = newItemsEncoded,
                    lastUpdated = System.currentTimeMillis()
                )
                
                // Title triggers check integration
                val titleChecked = checkAndUnlockTitles(updated)
                repository.savePlayerProfile(titleChecked)

                // Journal entry
                val entry = JournalEntry(
                    floor = profile.currentFloor,
                    actionTakenEs = "🔥 COGNITIVE SANCTUARY TRIUMPH: Slid the Secret Trial Overlord ${boss.nameEn}. Earned +$rewardGold Gold, rare item '${boss.rewardItem}'.",
                    actionTakenTr = "🔥 GİZLİ MEKAN MUZAFFERİ: Esrarengiz düşman ${boss.nameTr} bertaraf edildi. +$rewardGold Altın, nadide '${boss.rewardItem}' hazinesi kazanıldı.",
                    sideAlignmentShift = "NEUTRAL",
                    alignmentImpact = 0
                )
                repository.insertJournalEntry(entry)

                _slainSecretBosses.value = _slainSecretBosses.value + boss.id
                _lastActionMessageEn.value = "🎉 Defeated the Trial Overlord ${boss.nameEn}! Rare artifacts claimed."
                _lastActionMessageTr.value = "🎉 İmtihan Derebeyi ${boss.nameTr} mağlup edildi! Kadim ganimetler alındı."

                _activeSecretBossCombat.value = null
                _activeSecretBossHp.value = null
                _combatLog.value = emptyList()
            } else {
                // Boss retaliation turn
                val bossAtk = boss.atk + (0..6).random()
                playerHp = (playerHp - bossAtk).coerceAtLeast(0)

                if (_activeLanguage.value == "TR") {
                    logs.add("👹 ${boss.nameTr} sarsıcı bir darbe vurdu: -$bossAtk Hasar!")
                } else {
                    logs.add("👹 ${boss.nameEn} retaliates with a crushing blow: -$bossAtk DMG!")
                }

                _combatLog.value = logs

                if (playerHp <= 0) {
                    // Spirit Fracture (Death)
                    val fractureCount = profile.totalFractures + 1
                    val rollbackFloor = profile.savedFloorCheckpoint
                    
                    val updated = profile.copy(
                        currentHp = 50,
                        currentFloor = rollbackFloor,
                        totalFractures = fractureCount,
                        gleam = (profile.gleam / 2),
                        pyre = (profile.pyre / 2),
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.savePlayerProfile(updated)

                    val entry = JournalEntry(
                        floor = profile.currentFloor,
                        actionTakenEs = "💀 SPIRIT FRACTURE: Obliterated in trial fight against ${boss.nameEn}. Shattered back to Floor $rollbackFloor.",
                        actionTakenTr = "💀 RUH KIRILMASI: '${boss.nameTr}' imtihanı vahşi bitti. Kat $rollbackFloor seviyesine savruldunuz.",
                        sideAlignmentShift = "NEUTRAL",
                        alignmentImpact = 0
                    )
                    repository.insertJournalEntry(entry)

                    _lastActionMessageEn.value = "💀 Obliterated by ${boss.nameEn}. Slipped back to checkpoint Floor $rollbackFloor."
                    _lastActionMessageTr.value = "💀 '${boss.nameTr}' tarafından alt edildiniz. Kat $rollbackFloor sığınağına ışınlandınız."

                    _activeSecretBossCombat.value = null
                    _activeSecretBossHp.value = null
                    _combatLog.value = emptyList()
                    _currentTab.value = "OUTER_WORLD"
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

    companion object {
        fun getPlayerClassString(side: String, alignment: Int): String {
            val isLightBand = alignment >= 15
            val isDarkBand = alignment <= -15
            val isNeutralBand = alignment in -14..14

            return when (side) {
                "SANCTUM" -> {
                    when {
                        alignment >= 40 -> "Holy Aegis (Kutsal Siper)"
                        isLightBand -> "Codifier (Kanon Büyücüsü)"
                        else -> "Iron Throne (Acımasız İnfazcı)"
                    }
                }
                "COVENANT" -> {
                    when {
                        alignment <= -40 -> "Death Herald (Ölüm Habercisi)"
                        isDarkBand -> "Wildhand (Kaotik Gölge Ustası)"
                        else -> "Tempest (Özgürlük Savaşçısı)"
                    }
                }
                else -> {
                    when {
                        alignment >= 30 -> "Acolyte of Seal"
                        alignment <= -30 -> "Abyss Renegade"
                        else -> "Initiate (Yansıma Adayı)"
                    }
                }
            }
        }
    }
}
