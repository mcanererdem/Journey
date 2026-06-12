package com.mcanererdem.journey.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mcanererdem.journey.data.database.GameDatabase
import com.mcanererdem.journey.data.engine.*
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.model.EnemyFaction
import com.mcanererdem.journey.data.model.NavigationTab
import com.mcanererdem.journey.data.repository.GameRepository
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

    private val _firebaseSyncState = MutableStateFlow("IDLE") // "IDLE", "SYNCING", "SUCCESS", "FAILURE"
    val firebaseSyncState: StateFlow<String> = _firebaseSyncState.asStateFlow()

    private val _firebaseLeaderboard = MutableStateFlow<List<com.mcanererdem.journey.data.engine.LeaderboardEntry>>(emptyList())
    val firebaseLeaderboard: StateFlow<List<com.mcanererdem.journey.data.engine.LeaderboardEntry>> = _firebaseLeaderboard.asStateFlow()

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

    private val _currentTab = MutableStateFlow(NavigationTab.TOWER) // "TOWER", "OUTER_WORLD", "CHAR_SHEET", "JOURNAL"
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

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

    private val _playerStatuses = MutableStateFlow<List<CombatStatus>>(emptyList())
    val playerStatuses: StateFlow<List<CombatStatus>> = _playerStatuses.asStateFlow()

    private val _enemyStatuses = MutableStateFlow<List<CombatStatus>>(emptyList())
    val enemyStatuses: StateFlow<List<CombatStatus>> = _enemyStatuses.asStateFlow()

    private val _currentEnemyIntent = MutableStateFlow<EnemyIntent>(EnemyIntent.ATTACK)
    val currentEnemyIntent: StateFlow<EnemyIntent> = _currentEnemyIntent.asStateFlow()

    private var hasTriggeredPhase2 = false

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
                    val momentum = profile?.momentum ?: 50
                    when {
                        momentum > 55 -> "SANCTUM"
                        momentum < 45 -> "COVENANT"
                        else -> "NEUTRAL"
                    }
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
                            _playerStatuses.value = emptyList()
                            _enemyStatuses.value = emptyList()
                            _currentEnemyIntent.value = EnemyIntent.random()
                            hasTriggeredPhase2 = false
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
                val checked = checkDailyLogin(newProfile)
                repository.savePlayerProfile(checked)
                _currentScenario.value = LocalizationManager.getScenarioForFloor(initialLang, 1)
            } else {
                val checked = checkDailyLogin(direct)
                if (checked != direct) {
                    repository.savePlayerProfile(checked)
                }
                _currentScenario.value = LocalizationManager.getScenarioForFloor(initialLang, checked.currentFloor)
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

    fun selectTab(tab: NavigationTab) {
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
                chosenClass = calculatePlayerClass(faction, profile.momentum),
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
            
            val greedLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.GREED)
            val greedMultiplier = 1.0f + (greedLvl * 0.20f)
            val scaledGoldChange = if (option.goldChange > 0) (option.goldChange * greedMultiplier).toInt() else option.goldChange

            val recoveryLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.RECOVERY)
            val recoveryMultiplier = 1.0f + (recoveryLvl * 0.15f)
            val scaledHpChange = if (option.hpChange > 0) (option.hpChange * recoveryMultiplier).toInt() else option.hpChange

            // Calculate status adjustments
            val newMomentum = (profile.momentum + option.alignmentShift).coerceIn(-100, 100)
            val newGold = (profile.gold + scaledGoldChange).coerceAtLeast(0)
            val newAether = (profile.aether + option.aetherChange).coerceAtLeast(0)
            var newHp = profile.currentHp + scaledHpChange
            
            val activeFactionSide = if (profile.side == "NEUTRAL" && option.alignmentShift != 0) {
                // Automagic alignment detection
                if (option.alignmentShift > 0 && profile.momentum > 70) "SANCTUM"
                else if (option.alignmentShift < 0 && profile.momentum < 30) "COVENANT"
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
                    momentum = newMomentum,
                    gold = newGold,
                    aether = (newAether / 2), // Lose half current faction currencies due to void shatter
                    currentHp = 50, // Reincarnate with 50% HP
                    currentFloor = rollbackFloor,
                    totalFractures = fractureCount,
                    chosenClass = calculatePlayerClass(activeFactionSide, newMomentum),
                    rank = calculateRank(rollbackFloor),
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updatedProfile)
                _currentScenario.value = LocalizationManager.getScenarioForFloor(_activeLanguage.value, rollbackFloor)
                
                _lastActionMessageEn.value = "💀 Spirit Fracture triggered! Slipped from Tower back to checkpoint Floor $rollbackFloor."
                _lastActionMessageTr.value = "💀 Ruh Kırılması Yaşandı! Kule'den güvenli koridor kontrol noktası Kat $rollbackFloor seviyesine savruldun."
                _currentTab.value = NavigationTab.OUTER_WORLD // Go to outer world to rest/heal
            } else {
                // Clean progression
                val nextFloor = profile.currentFloor + 1
                val isCheckpoint = (nextFloor % 10 == 0)
                val newCheckpoint = if (isCheckpoint) nextFloor else profile.savedFloorCheckpoint

                val nextRank = calculateRank(nextFloor)
                val updatedProfile = profile.copy(
                    momentum = newMomentum,
                    gold = newGold,
                    aether = newAether,
                    currentHp = newHp.coerceAtMost(profile.maxHp),
                    currentFloor = nextFloor,
                    savedFloorCheckpoint = newCheckpoint,
                    chosenClass = calculatePlayerClass(activeFactionSide, newMomentum),
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
                val greedLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.GREED)
                val greedMultiplier = 1.0f + (greedLvl * 0.20f)
                val baseReward = (20..50).random()
                val reward = (baseReward * greedMultiplier).toInt()
                val damage = (5..15).random()
                val isVoid = (0..1).random() == 1
                
                val updated = profile.copy(
                    gold = profile.gold + reward,
                    currentHp = profile.currentHp - damage,
                    momentum = (profile.momentum + (if (isVoid) -2 else 2)).coerceIn(0, 100),
                    chosenClass = calculatePlayerClass(profile.side, (profile.momentum + (if (isVoid) -2 else 2)).coerceIn(0, 100)),
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
            when (type.uppercase()) {
                "GOLD_TO_AETHER" -> {
                    if (profile.gold >= 50) {
                        val updated = profile.copy(
                            gold = profile.gold - 50,
                            aether = profile.aether + 15,
                            lastUpdated = System.currentTimeMillis()
                        )
                        repository.savePlayerProfile(updated)
                        _lastActionMessageEn.value = "Aether exchange complete (+15 Aether, -50 Gold)."
                        _lastActionMessageTr.value = "Işıltı takası tamamlandı (+15 Işıltı, -50 Altın)."
                    } else {
                        _lastActionMessageEn.value = "Insufficient gold to secure Aether."
                        _lastActionMessageTr.value = "Işıltı satın almak için yetersiz altın."
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
                    momentum = 50,
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
            val profile = repository.getPlayerProfileDirect()
            val oldLegacyPoints = profile?.legacyPoints ?: 0
            val currentFloor = profile?.currentFloor ?: 1
            val currentLevel = profile?.level ?: 1
            
            // Calculate earned legacy points
            val earnedPoints = (currentFloor * 2) + (currentLevel * 5)
            val newLegacyPoints = oldLegacyPoints + earnedPoints
            
            val upgradesEncoded = profile?.upgradesEncoded ?: ""
            
            // Extract upgrade levels to apply starting bonuses
            val vitLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.VITALITY)
            val focusLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.AETHER_FOCUS)
            val fortLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.FORTITUDE)
            
            val startMaxHp = 100 + (vitLvl * 10)
            val startAether = focusLvl * 15
            val startMaxWill = 10 + fortLvl
            
            // Preserve daily login values
            val lastLogin = profile?.lastLoginTimestamp ?: 0L
            val streak = profile?.loginStreak ?: 0
            val dailyQuests = profile?.dailyQuestsEncoded ?: ""

            repository.clearJournal()
            val newProfile = PlayerProfile(
                playerName = profile?.playerName ?: "Lord Alistair",
                currentFloor = 1,
                currentHp = startMaxHp,
                maxHp = startMaxHp,
                gold = 150,
                side = "NEUTRAL",
                momentum = 50,
                aether = startAether,
                rank = "EMISSARY",
                chosenClass = "Initiate",
                totalFractures = 0,
                savedFloorCheckpoint = 1,
                level = 1,
                exp = 0,
                maxExp = 100,
                currentWill = startMaxWill,
                maxWill = startMaxWill,
                itemsEncoded = "",
                titlesEncoded = "",
                currentNodeIndex = 0,
                currentNodeCompleted = false,
                legacyPoints = newLegacyPoints,
                upgradesEncoded = upgradesEncoded,
                lastLoginTimestamp = lastLogin,
                loginStreak = streak,
                dailyQuestsEncoded = dailyQuests
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
            
            _lastActionMessageEn.value = "Game restarted. Gained +$earnedPoints Legacy Points! Upgrades retained."
            _lastActionMessageTr.value = "Zaman döngüsü sıfırlandı. +$earnedPoints Miras Puanı kazanıldı! Kalıcı yükseltmeler korundu."
            _currentTab.value = NavigationTab.TOWER
        }
    }

    // Interacting with Adventure Nodes
    fun selectNodeChoice(choice: NodeChoice) {
        viewModelScope.launch {
            var profile = repository.getPlayerProfileDirect() ?: return@launch
            
            val hasPass = profile.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }
            val resolvedWillChange = if (hasPass && choice.willChange < 0) 0 else choice.willChange

            // Validate Will Cost
            if (!hasPass && choice.willChange < 0 && profile.currentWill < -choice.willChange) {
                _lastActionMessageEn.value = "❌ Insufficient Willpower to make this choice!"
                _lastActionMessageTr.value = "❌ Bu kararı seçmek için yeterli İradeniz yok!"
                return@launch
            }

            // Calculate gold change with Greed
            val greedLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.GREED)
            val greedMultiplier = 1.0f + (greedLvl * 0.20f)
            val scaledGoldChange = if (choice.goldChange > 0) (choice.goldChange * greedMultiplier).toInt() else choice.goldChange

            val recoveryLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.RECOVERY)
            val recoveryMultiplier = 1.0f + (recoveryLvl * 0.15f)
            val scaledHpChange = if (choice.hpChange > 0) (choice.hpChange * recoveryMultiplier).toInt() else choice.hpChange

            // Process Quest progress updates
            val nodes = _currentFloorNodes.value
            val activeNode = nodes.getOrNull(profile.currentNodeIndex)
            if (activeNode != null && activeNode.type == NodeType.CHEST) {
                profile = updateDailyQuestProgress(profile, 1, 1)
            }
            val willSpent = if (resolvedWillChange < 0) -resolvedWillChange else 0
            if (willSpent > 0) {
                profile = updateDailyQuestProgress(profile, 2, willSpent)
            }

            val newMomentum = (profile.momentum + choice.alignmentShift).coerceIn(-100, 100)
            val newGold = (profile.gold + scaledGoldChange).coerceAtLeast(0)
            val newAether = (profile.aether + choice.aetherChange).coerceAtLeast(0)
            val newWill = (profile.currentWill + resolvedWillChange).coerceIn(0, profile.maxWill)
            var newHp = profile.currentHp + scaledHpChange
            
            // Build item catalog list
            var currentItems = if (profile.itemsEncoded.isEmpty()) emptyList() else profile.itemsEncoded.split(",")
            if (choice.rewardItem.isNotEmpty()) {
                currentItems = currentItems + choice.rewardItem
            }
            val newItemsEncoded = currentItems.joinToString(",")

            // Build story flags list
            var currentFlags = if (profile.storyFlagsEncoded.isEmpty()) emptyList() else profile.storyFlagsEncoded.split(",")
            if (choice.addStoryFlag.isNotEmpty() && !currentFlags.contains(choice.addStoryFlag)) {
                currentFlags = currentFlags + choice.addStoryFlag
            }
            val newStoryFlagsEncoded = currentFlags.joinToString(",")

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
                if (choice.alignmentShift > 0 && profile.momentum > 70) "SANCTUM"
                else if (choice.alignmentShift < 0 && profile.momentum < 30) "COVENANT"
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
                triggerSpiritFracture(profile, newMomentum, newGold, newAether, activeFactionSide)
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
                            _playerStatuses.value = emptyList()
                            _enemyStatuses.value = emptyList()
                            _currentEnemyIntent.value = EnemyIntent.random()
                            hasTriggeredPhase2 = false
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
                        _playerStatuses.value = emptyList()
                        _enemyStatuses.value = emptyList()
                        _currentEnemyIntent.value = EnemyIntent.random()
                        hasTriggeredPhase2 = false
                        _combatLog.value = listOf(
                            "TR" to "⚔️ MEKAN KISAYOLU! Doğrudan kat patronu ${bossNode.enemyNameTr} ile savaşa girdiniz! Can: ${bossNode.enemyHp}",
                            "EN" to "⚔️ SPATIAL SHORTCUT! Teleported straight to battle with floor boss ${bossNode.enemyNameEn}! HP: ${bossNode.enemyHp}"
                        ).map { if (_activeLanguage.value == "TR") it.first else it.second }
                    }
                }

                val updated = profile.copy(
                    currentFloor = targetFloor,
                    currentNodeIndex = targetNodeIndex,
                    currentNodeColumn = if (choice.skipToNextFloor) 0 else profile.currentNodeColumn,
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
            var profile = repository.getPlayerProfileDirect() ?: return@launch
            val nodes = _currentFloorNodes.value
            
            // Find the node at the selected depth and column
            val nextNode = nodes.find { it.depth == depth && it.column == column } ?: return@launch
            
            val hasPass = profile.itemsEncoded.split(",").any { it.trim() == "Seasonal Sovereign Pass" }

            // Validate Willpower
            if (!hasPass && profile.currentWill < 1) {
                _lastActionMessageEn.value = "❌ No Willpower! Spend 50 Gold at Outer Haven sığınağı to restore life and Will."
                _lastActionMessageTr.value = "❌ İrade Tükendi! İradenizi yenilemek için Dış Sığınak Kaplıcasında 50 Altına dinlenin."
                return@launch
            }

            if (nextNode.type == NodeType.COMBAT || nextNode.type == NodeType.BOSS) {
                _activeEnemyHp.value = nextNode.enemyHp
                _playerStatuses.value = emptyList()
                _enemyStatuses.value = emptyList()
                _currentEnemyIntent.value = EnemyIntent.random()
                hasTriggeredPhase2 = false
                _combatLog.value = listOf(
                    "TR" to "⚔️ ${nextNode.enemyNameTr} ile kule yolunda savaşa girdiniz! Can: ${nextNode.enemyHp}",
                    "EN" to "⚔️ Engaged in battle with ${nextNode.enemyNameEn}! HP: ${nextNode.enemyHp}"
                ).map { if (_activeLanguage.value == "TR") it.first else it.second }
            } else {
                _activeEnemyHp.value = null
                _combatLog.value = emptyList()
            }

            val willSpent = if (hasPass) 0 else 0 // Regular node advancement no longer costs Will
            if (willSpent > 0) {
                profile = updateDailyQuestProgress(profile, 2, willSpent)
            }

            val updated = profile.copy(
                currentNodeIndex = nextNode.index,
                currentNodeColumn = column,
                currentNodeCompleted = false,
                currentWill = if (hasPass) profile.currentWill else profile.currentWill - willSpent, // Deduct Will
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            _lastActionMessageEn.value = "Advanced deeper into Floor ${profile.currentFloor}. Sector: ${nextNode.title}."
            _lastActionMessageTr.value = "${profile.currentFloor}. Katta yeni bir sektöre adım attınız: ${nextNode.titleTr}."
        }
    }

    fun showActionMessage(messageEn: String, messageTr: String) {
        _lastActionMessageEn.value = messageEn
        _lastActionMessageTr.value = messageTr
    }

    fun performScouting() {
        viewModelScope.launch {
            var profile = repository.getPlayerProfileDirect() ?: return@launch
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

            val willSpent = if (hasPass) 0 else scoutCost
            if (willSpent > 0) {
                profile = updateDailyQuestProgress(profile, 2, willSpent)
            }

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
            var profile = repository.getPlayerProfileDirect() ?: return@launch
            
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

            val willSpent = if (hasPass) 0 else 2
            if (willSpent > 0) {
                profile = updateDailyQuestProgress(profile, 2, willSpent)
            }

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
            when (val res = com.mcanererdem.journey.data.engine.FloorStateManager.attemptFloorTransition(profile, targetFloor)) {
                is com.mcanererdem.journey.data.engine.FloorStateManager.TransitionResult.Success -> {
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
                is com.mcanererdem.journey.data.engine.FloorStateManager.TransitionResult.Failure -> {
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
            val nodeIndex = profile.currentNodeIndex
            if (nodeIndex !in nodes.indices) return@launch
            val node = nodes[nodeIndex]
            val maxEnemyHp = node.enemyHp
            var currentEnemyHp = _activeEnemyHp.value ?: maxEnemyHp

            val logs = mutableListOf<String>()

            // 1. Process turn start status effects on player
            var playerHp = profile.currentHp
            val updatedPlayerStatuses = _playerStatuses.value.map { it.copy() }.toMutableList()
            val updatedEnemyStatuses = _enemyStatuses.value.map { it.copy() }.toMutableList()

            // Player Poison
            if (updatedPlayerStatuses.any { it.type == StatusType.POISONED }) {
                playerHp = (playerHp - 5).coerceAtLeast(0)
                logs.add(if (_activeLanguage.value == "TR") "ğŸ§ª Zehirlendiniz! -5 Can aldÄ±nÄ±z." else "ğŸ§ª Poisoned! Took -5 HP damage.")
                if (playerHp <= 0) {
                    triggerSpiritFracture(profile, profile.momentum, profile.gold, profile.aether, profile.side)
                    return@launch
                }
            }

            // Player Stun check
            val playerStunned = updatedPlayerStatuses.any { it.type == StatusType.STUNNED }
            if (playerStunned) {
                logs.add(if (_activeLanguage.value == "TR") "ğŸŒ€ Sersemlediniz! SÄ±ranÄ±zÄ± geÃ§tiniz." else "ğŸŒ€ Stunned! Your turn is skipped.")
                decrementStatuses(updatedPlayerStatuses)
                _playerStatuses.value = updatedPlayerStatuses.filter { it.durationTurns > 0 }
                
                executeEnemyTurn(profile, playerHp, currentEnemyHp, node, updatedPlayerStatuses, updatedEnemyStatuses, logs)
                return@launch
            }

            // 2. Process player's action
            var damageDealt = 0
            var isCrit = false
            val critChance = (10 + profile.currentWill * 4).coerceIn(10, 50)
            val isBlessed = updatedPlayerStatuses.any { it.type == StatusType.BLESSED }

            when (action) {
                "LIGHT_STRIKE" -> {
                    val baseDmg = 10 + profile.level * 2
                    val rawDmg = (baseDmg * 0.9).toInt() + kotlin.random.Random.nextInt(5)
                    var finalDmg = rawDmg
                    if (isBlessed) finalDmg = (finalDmg * 1.25f).toInt()
                    
                    isCrit = kotlin.random.Random.nextInt(100) < critChance
                    if (isCrit) finalDmg = (finalDmg * 1.5f).toInt()

                    damageDealt = finalDmg
                    currentEnemyHp = (currentEnemyHp - damageDealt).coerceAtLeast(0)
                    _activeEnemyHp.value = currentEnemyHp

                    logs.add(
                        if (_activeLanguage.value == "TR") "⚔️ Hafif Vuruş yaptınız! Düşmana $damageDealt hasar verdiniz."
                        else "⚔️ Light Strike! Dealt $damageDealt damage to the enemy."
                    )
                }
                "HEAVY_BLOW" -> {
                    if (profile.aether < 15) {
                        logs.add(
                            if (_activeLanguage.value == "TR") "❌ Yetersiz Aether!"
                            else "❌ Insufficient Aether!"
                        )
                        _combatLog.value = logs
                        return@launch
                    }
                    val baseDmg = 25 + profile.level * 3
                    val rawDmg = (baseDmg * 0.9).toInt() + kotlin.random.Random.nextInt(10)
                    var finalDmg = rawDmg
                    if (isBlessed) finalDmg = (finalDmg * 1.25f).toInt()

                    isCrit = kotlin.random.Random.nextInt(100) < critChance
                    if (isCrit) finalDmg = (finalDmg * 1.5f).toInt()

                    damageDealt = finalDmg
                    currentEnemyHp = (currentEnemyHp - damageDealt).coerceAtLeast(0)
                    _activeEnemyHp.value = currentEnemyHp

                    val newAether = (profile.aether - 15).coerceAtLeast(0)
                    val updatedProfile = profile.copy(aether = newAether, lastUpdated = System.currentTimeMillis())
                    repository.savePlayerProfile(updatedProfile)

                    logs.add(
                        if (_activeLanguage.value == "TR") "🔥 Ağır Darbe! Düşmana $damageDealt hasar verdiniz. (-15 Aether)"
                        else "🔥 Heavy Blow! Dealt $damageDealt damage to the enemy. (-15 Aether)"
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
                        if (_activeLanguage.value == "TR") "🛡️ Bariyer kullandınız! +$healAmt Can yenilendi ve 2 tur Kalkan kazandınız."
                        else "🛡️ Activated Barrier! Recovered +$healAmt HP and gained Shielded status for 2 turns."
                    )
                }
                "ESCAPE" -> {
                    _activeEnemyHp.value = null
                    _combatLog.value = emptyList()
                    _playerStatuses.value = emptyList()
                    _enemyStatuses.value = emptyList()
                    _lastActionMessageEn.value = "🏃 Escaped from combat."
                    _lastActionMessageTr.value = "🏃 Dövüşten kaçtınız."
                    return@launch
                }
            }

            if (isCrit && action != "BARRIER") {
                logs.add(if (_activeLanguage.value == "TR") "ğŸ’¥ Kritik VuruÅŸ!" else "ğŸ’¥ Critical Hit!")
            }

            if (currentEnemyHp <= 0) {
                handleVictory(profile, node, playerHp, logs)
                return@launch
            }

            if (node.type == NodeType.BOSS && currentEnemyHp < (maxEnemyHp / 2) && !hasTriggeredPhase2) {
                hasTriggeredPhase2 = true
                logs.add(
                    if (_activeLanguage.value == "TR") "ğŸ˜¡ PATRON Ã–FKELENDÄ°! CanÄ± %50'nin altÄ±na indi, saldÄ±rÄ± gÃ¼cÃ¼ %50 arttÄ±!"
                    else "ğŸ˜¡ BOSS ENRAGED! HP fell below 50%, attack power increased by 50%!"
                )
            }

            decrementStatuses(updatedPlayerStatuses)
            _playerStatuses.value = updatedPlayerStatuses.filter { it.durationTurns > 0 }

            executeEnemyTurn(profile, playerHp, currentEnemyHp, node, updatedPlayerStatuses, updatedEnemyStatuses, logs)
        }
    }

    private suspend fun executeEnemyTurn(
        profile: PlayerProfile,
        initialPlayerHp: Int,
        enemyHp: Int,
        node: AdventureNode,
        playerStatuses: MutableList<CombatStatus>,
        enemyStatuses: MutableList<CombatStatus>,
        logs: MutableList<String>
    ) {
        var playerHp = initialPlayerHp
        var currentEnemyHp = enemyHp

        if (enemyStatuses.any { it.type == StatusType.POISONED }) {
            currentEnemyHp = (currentEnemyHp - 5).coerceAtLeast(0)
            _activeEnemyHp.value = currentEnemyHp
            logs.add(if (_activeLanguage.value == "TR") "ğŸ§ª DÃ¼ÅŸman zehirden -5 hasar aldÄ±." else "ğŸ§ª Enemy took -5 Poison damage.")
            if (currentEnemyHp <= 0) {
                handleVictory(profile, node, playerHp, logs)
                return
            }
        }

        val enemyStunned = enemyStatuses.any { it.type == StatusType.STUNNED }
        if (enemyStunned) {
            logs.add(if (_activeLanguage.value == "TR") "ğŸŒ€ DÃ¼ÅŸman sersemledi ve sÄ±rasÄ±nÄ± geÃ§ti." else "ğŸŒ€ Enemy is stunned and skips turn.")
            decrementStatuses(enemyStatuses)
            _enemyStatuses.value = enemyStatuses.filter { it.durationTurns > 0 }
            _combatLog.value = _combatLog.value + logs
            return
        }

        var enemyAtk = (8 + profile.currentFloor * 1.5).toInt()
        if (node.type == NodeType.BOSS && hasTriggeredPhase2) {
            enemyAtk = (enemyAtk * 1.5f).toInt()
        }

        val enemyBlessed = enemyStatuses.any { it.type == StatusType.BLESSED }
        if (enemyBlessed) {
            enemyAtk = (enemyAtk * 1.25f).toInt()
        }

        val playerShielded = playerStatuses.any { it.type == StatusType.SHIELDED }

        when (_currentEnemyIntent.value) {
            EnemyIntent.ATTACK -> {
                var dmg = enemyAtk + kotlin.random.Random.nextInt(4)
                if (playerShielded) {
                    dmg = (dmg * 0.5f).toInt()
                    logs.add(if (_activeLanguage.value == "TR") "ğŸ›¡ï¸ KalkanÄ±nÄ±z sayesinde hasar yarÄ±ya indi!" else "ğŸ›¡ï¸ Shield reduced incoming damage by 50%!")
                }
                playerHp = (playerHp - dmg).coerceAtLeast(0)
                logs.add(
                    if (_activeLanguage.value == "TR") "👺 Düşman saldırdı! $dmg hasar aldınız."
                    else "👺 Enemy attacked! Dealt $dmg damage to you."
                )
            }
            EnemyIntent.DEFEND -> {
                val existingShield = enemyStatuses.find { it.type == StatusType.SHIELDED }
                if (existingShield != null) {
                    existingShield.durationTurns = 2
                } else {
                    enemyStatuses.add(CombatStatus(StatusType.SHIELDED, 2))
                }
                logs.add(
                    if (_activeLanguage.value == "TR") "ğŸ›¡ï¸ DÃ¼ÅŸman savunmaya Ã§ekildi ve Kalkan kazandÄ±."
                    else "ğŸ›¡ï¸ Enemy defends and gains Shielded."
                )
            }
            EnemyIntent.DEBUFF -> {
                val existingPoison = playerStatuses.find { it.type == StatusType.POISONED }
                if (existingPoison != null) {
                    existingPoison.durationTurns = 3
                } else {
                    playerStatuses.add(CombatStatus(StatusType.POISONED, 3))
                }
                logs.add(
                    if (_activeLanguage.value == "TR") "ğŸ§ª DÃ¼ÅŸman sizi zehirledi! (3 tur zehir)"
                    else "ğŸ§ª Enemy poisoned you! (Poisoned for 3 turns)"
                )
            }
            EnemyIntent.BUFF -> {
                val existingBless = enemyStatuses.find { it.type == StatusType.BLESSED }
                if (existingBless != null) {
                    existingBless.durationTurns = 3
                } else {
                    enemyStatuses.add(CombatStatus(StatusType.BLESSED, 3))
                }
                logs.add(
                    if (_activeLanguage.value == "TR") "âœ¨ DÃ¼ÅŸman kendini kutsadÄ±! SaldÄ±rÄ± gÃ¼cÃ¼ arttÄ±."
                    else "âœ¨ Enemy blessed themselves! Attack power increased."
                )
            }
        }

        decrementStatuses(enemyStatuses)
        _enemyStatuses.value = enemyStatuses.filter { it.durationTurns > 0 }

        _currentEnemyIntent.value = EnemyIntent.random()

        if (playerHp <= 0) {
            triggerSpiritFracture(profile, profile.momentum, profile.gold, profile.aether, profile.side)
        } else {
            val updatedProfile = profile.copy(currentHp = playerHp, lastUpdated = System.currentTimeMillis())
            repository.savePlayerProfile(updatedProfile)
            _combatLog.value = _combatLog.value + logs
        }
    }

    private fun decrementStatuses(statuses: MutableList<CombatStatus>) {
        statuses.forEach { it.durationTurns-- }
    }

    private suspend fun handleVictory(profile: PlayerProfile, activeNode: AdventureNode, playerHp: Int, logs: MutableList<String>) {
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

        // Scale rewards if on elite path (column == 1)
        val isElite = activeNode.column == 1
        val greedLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.GREED)
        val greedMultiplier = 1.0f + (greedLvl * 0.20f)
        
        val goldGained = if (isElite) (rewards.goldGained * 1.5).toInt() else rewards.goldGained
        val scaledGoldGained = (goldGained * greedMultiplier).toInt()
        val expGained = if (isElite) (rewards.expGained * 1.5).toInt() else rewards.expGained

        // Recalculate leveling with scaled EXP
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

        // Process Combat Quest progress on profile
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
            currentNodeCompleted = true,
            lastUpdated = System.currentTimeMillis()
        )
        repository.savePlayerProfile(updatedProfile)

        if (_activeLanguage.value == "TR") {
            logs.add("🎉 ZAFER! Düşman katledildi. +$expGained Deneyim, +$scaledGoldGained Altın kazanıldı.")
            if (rewards.itemAwarded != null) logs.add("🎁 Hazine: [${rewards.itemAwarded}] toplandı teçhizat torbana konuldu!")
            if (rewards.titleAwarded != null) logs.add("👑 Yeni Unvan Kazandınız: [${rewards.titleAwarded}]!")
            if (didLevelUp) logs.add("⚡ SEVİYE ATLADINIZ! Seviye $totalLevel oldunuz! Maksimum Canınız arttı.")
        } else {
            logs.add("🎉 VICTORY! Enemy defeated. Won +$expGained EXP, +$scaledGoldGained Gold.")
            if (rewards.itemAwarded != null) logs.add("🎁 Loot Pick: [${rewards.itemAwarded}] added to inventory!")
            if (rewards.titleAwarded != null) logs.add("👑 Achieved Epic Title: [${rewards.titleAwarded}]!")
            if (didLevelUp) logs.add("⚡ LEVEL UP! Reached Level $totalLevel! Max HP increased.")
        }

        val journalEntry = JournalEntry(
            floor = profile.currentFloor,
            actionTakenEs = "Defeated $ in combat on Floor $.",
            actionTakenTr = "$. Katta $ karŞıŞındaki düelloyu kazandınız.",
            sideAlignmentShift = "NEUTRAL",
            alignmentImpact = 0,
            nodeIndex = profile.currentNodeIndex
        )
        repository.insertJournalEntry(journalEntry)

        _combatLog.value = _combatLog.value + logs
        _lastActionMessageEn.value = "Defeated $!"
        _lastActionMessageTr.value = "$ yok edildi!"
    }
    private suspend fun triggerSpiritFracture(
        profile: PlayerProfile,
        newMomentum: Int,
        newGold: Int,
        newAether: Int,
        factionSide: String
    ) {
        val fractureCount = profile.totalFractures + 1
        val rollbackFloor = profile.savedFloorCheckpoint
        
        // Award Legacy Points based on progress
        val earnedLegacyPoints = (profile.currentFloor / 10) + 1
        val newLegacyPoints = profile.legacyPoints + earnedLegacyPoints

        val updated = profile.copy(
            momentum = newMomentum,
            gold = newGold,
            aether = newAether / 2,
            currentHp = profile.maxHp / 2, // Reincarnate with 50% HP
            currentFloor = rollbackFloor,
            currentNodeIndex = 0, // Reset progression to beginning of safety checkpoint
            currentNodeCompleted = false,
            totalFractures = fractureCount,
            legacyPoints = newLegacyPoints,
            chosenClass = calculatePlayerClass(factionSide, newMomentum),
            rank = calculateRank(rollbackFloor),
            lastUpdated = System.currentTimeMillis()
        )
        repository.savePlayerProfile(updated)

        _lastActionMessageEn.value = "💀 Spirit Fracture triggered! Slipped from Tower back to checkpoint Floor $rollbackFloor."
        _lastActionMessageTr.value = "💀 Ruh Kırılması Yaşandı! Kule'den güvenli koridor kontrol noktası Kat $rollbackFloor seviyesine savruldun."
        _currentTab.value = NavigationTab.OUTER_WORLD
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

    private fun calculatePlayerClass(side: String, momentum: Int): String {
        return getPlayerClassString(side, momentum)
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
        
        com.mcanererdem.journey.data.engine.QuestTitleSystem.titles.forEach { title ->
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
                _lastActionMessageEn.value = if (titleId.isEmpty()) "Unequipped title." else "Equipped: ${com.mcanererdem.journey.data.engine.QuestTitleSystem.getTitleDef(titleId)?.nameEn}"
                _lastActionMessageTr.value = if (titleId.isEmpty()) "Unvan çıkarıldı." else "Mistik unvan kuşanıldı: ${com.mcanererdem.journey.data.engine.QuestTitleSystem.getTitleDef(titleId)?.nameTr}"
            }
        }
    }

    /**
     * Claims the rewards for a completed quest, awarding Gold, Experience, Items, and/or Titles.
     */
    fun claimQuestReward(questId: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val quest = com.mcanererdem.journey.data.engine.QuestTitleSystem.quests.find { it.id == questId } ?: return@launch
            
            val completedSet = profile.completedQuestsEncoded.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (completedSet.contains(questId)) return@launch
            
            if (!quest.checkProgress(profile)) return@launch
            
            completedSet.add(questId)
            
            // Calculate base currency rewards with Greed
            val greedLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.GREED)
            val greedMultiplier = 1.0f + (greedLvl * 0.20f)
            val scaledGoldReward = if (quest.rewardGold > 0) (quest.rewardGold * greedMultiplier).toInt() else quest.rewardGold

            val newGold = profile.gold + scaledGoldReward
            val newAether = profile.aether + quest.rewardAether
            
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
                aether = newAether,
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
                    val AetherBonus = ((profile.aether / 50) * 0.05f).coerceAtMost(0.15f)
                    if (AetherBonus > 0f) {
                        factionMod += AetherBonus
                        val pct = (AetherBonus * 100).toInt()
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
                    val AetherBonus = ((profile.aether / 50) * 0.05f).coerceAtMost(0.15f)
                    if (AetherBonus > 0f) {
                        factionMod += AetherBonus
                        val pct = (AetherBonus * 100).toInt()
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
                val greedLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.GREED)
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
                        aether = (profile.aether / 2),
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
                    _currentTab.value = NavigationTab.OUTER_WORLD
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

    private fun checkDailyLogin(profile: PlayerProfile): PlayerProfile {
        val now = System.currentTimeMillis()
        val diffMs = now - profile.lastLoginTimestamp
        val oneDayMs = 24 * 60 * 60 * 1000L
        val twoDaysMs = 48 * 60 * 60 * 1000L

        if (profile.lastLoginTimestamp == 0L || diffMs >= oneDayMs) {
            val newStreak = if (profile.lastLoginTimestamp == 0L) {
                1
            } else if (diffMs < twoDaysMs) {
                profile.loginStreak + 1
            } else {
                1
            }

            // Daily login reward: 20 gold + streak * 5 gold, 5 + streak aether (scaled by greed)
            val greedLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.GREED)
            val greedMultiplier = 1.0f + (greedLvl * 0.20f)
            val baseGoldReward = 20 + (newStreak * 5)
            val goldReward = (baseGoldReward * greedMultiplier).toInt()
            val aetherReward = 5 + newStreak

            viewModelScope.launch {
                _lastActionMessageEn.value = "📅 Daily Login! Streak: $newStreak day(s). Claimed login reward: +$goldReward Gold, +$aetherReward Aether."
                _lastActionMessageTr.value = "📅 Günlük Giriş! Seri: $newStreak gün. Giriş ödülü kazanıldı: +$goldReward Altın, +$aetherReward Işıltı."
            }

            return profile.copy(
                lastLoginTimestamp = now,
                loginStreak = newStreak,
                dailyQuestsEncoded = "0/3/0,0/2/0,0/5/0", // Reset quests: combat, chest, willpower
                gold = profile.gold + goldReward,
                aether = profile.aether + aetherReward,
                lastUpdated = now
            )
        }
        return profile
    }

    private fun updateDailyQuestProgress(profile: PlayerProfile, typeIndex: Int, amount: Int): PlayerProfile {
        if (profile.dailyQuestsEncoded.isEmpty()) return profile
        val quests = profile.dailyQuestsEncoded.split(",").toMutableList()
        if (quests.size <= typeIndex) return profile
        val parts = quests[typeIndex].split("/").toMutableList()
        if (parts.size < 3) return profile
        
        val currentProgress = parts[0].toIntOrNull() ?: 0
        val target = parts[1].toIntOrNull() ?: 0
        val claimed = parts[2].toIntOrNull() ?: 0
        
        if (claimed == 1 || currentProgress >= target) return profile
        
        val newProgress = (currentProgress + amount).coerceAtMost(target)
        parts[0] = newProgress.toString()
        quests[typeIndex] = parts.joinToString("/")
        
        val newQuestsEncoded = quests.joinToString(",")
        
        if (newProgress >= target) {
            viewModelScope.launch {
                _lastActionMessageEn.value = "✨ Daily Quest Completed! Claim rewards in Legacy tab."
                _lastActionMessageTr.value = "✨ Günlük Görev Tamamlandı! Miras sekmesinden ödülünüzü alın."
            }
        }
        
        return profile.copy(
            dailyQuestsEncoded = newQuestsEncoded,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun claimDailyQuestReward(typeIndex: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            if (profile.dailyQuestsEncoded.isEmpty()) return@launch
            val quests = profile.dailyQuestsEncoded.split(",").toMutableList()
            if (quests.size <= typeIndex) return@launch
            val parts = quests[typeIndex].split("/")
            if (parts.size < 3) return@launch
            
            val currentProgress = parts[0].toIntOrNull() ?: 0
            val target = parts[1].toIntOrNull() ?: 0
            val claimed = parts[2].toIntOrNull() ?: 0
            
            if (currentProgress < target) {
                _lastActionMessageEn.value = "❌ Quest not completed yet!"
                _lastActionMessageTr.value = "❌ Görev henüz tamamlanmadı!"
                return@launch
            }
            if (claimed == 1) {
                _lastActionMessageEn.value = "❌ Quest reward already claimed!"
                _lastActionMessageTr.value = "❌ Görev ödülü zaten alındı!"
                return@launch
            }
            
            val newParts = parts.toMutableList()
            newParts[2] = "1"
            quests[typeIndex] = newParts.joinToString("/")
            val newQuestsEncoded = quests.joinToString(",")
            
            val baseGold = 50
            val baseAether = 15
            val greedLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, com.mcanererdem.journey.data.model.LegacyUpgradeType.GREED)
            val greedMultiplier = 1.0f + (greedLvl * 0.20f)
            val finalGold = (baseGold * greedMultiplier).toInt()
            
            val updated = profile.copy(
                dailyQuestsEncoded = newQuestsEncoded,
                gold = profile.gold + finalGold,
                aether = profile.aether + baseAether,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            
            _lastActionMessageEn.value = "🎁 Claimed Daily Quest Reward: +$finalGold Gold, +$baseAether Aether!"
            _lastActionMessageTr.value = "🎁 Günlük Görev Ödülü Alındı: +$finalGold Altın, +$baseAether Işıltı!"
        }
    }

    fun purchaseUpgrade(upgradeKey: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val type = com.mcanererdem.journey.data.model.LegacyUpgradeType.values().find { it.key == upgradeKey } ?: return@launch
            val currentLvl = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, type)
            
            if (currentLvl >= type.maxLevel) {
                _lastActionMessageEn.value = "❌ Already at maximum level for this upgrade!"
                _lastActionMessageTr.value = "❌ Bu yükseltme için zaten maksimum seviyedesiniz!"
                return@launch
            }
            
            val cost = type.getCostForLevel(currentLvl)
            if (profile.legacyPoints < cost) {
                _lastActionMessageEn.value = "❌ Insufficient Legacy Points!"
                _lastActionMessageTr.value = "❌ Yetersiz Miras Puanı!"
                return@launch
            }
            
            val upgradesMap = com.mcanererdem.journey.data.model.LegacyUpgradeType.getUpgradesMap(profile.upgradesEncoded).toMutableMap()
            upgradesMap[type.key] = currentLvl + 1
            val newUpgradesEncoded = com.mcanererdem.journey.data.model.LegacyUpgradeType.encodeUpgrades(upgradesMap)
            
            val updated = profile.copy(
                legacyPoints = profile.legacyPoints - cost,
                upgradesEncoded = newUpgradesEncoded,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            
            _lastActionMessageEn.value = "✔️ Upgraded ${type.nameEn} to Level ${currentLvl + 1}!"
            _lastActionMessageTr.value = "✔️ ${type.nameTr} Seviye ${currentLvl + 1} düzeyine yükseltildi!"
        }
    }

    fun syncProfileToFirebase() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            _firebaseSyncState.value = "SYNCING"
            val success = FirebaseManager.syncProfileToCloud(profile)
            if (success) {
                _firebaseSyncState.value = "SUCCESS"
                _lastActionMessageEn.value = "✔️ Progress successfully backed up to cloud save!"
                _lastActionMessageTr.value = "✔️ Karakter ilerlemesi başarıyla buluta yedeklendi!"
            } else {
                _firebaseSyncState.value = "FAILURE"
                _lastActionMessageEn.value = "❌ Cloud save sync failed. Verify network connection."
                _lastActionMessageTr.value = "❌ Bulut yedeklemesi başarısız. Bağlantınızı kontrol edin."
            }
        }
    }

    fun restoreProfileFromFirebase() {
        viewModelScope.launch {
            _firebaseSyncState.value = "SYNCING"
            val cloudProfile = FirebaseManager.fetchProfileFromCloud()
            if (cloudProfile != null) {
                repository.savePlayerProfile(cloudProfile)
                _firebaseSyncState.value = "SUCCESS"
                _lastActionMessageEn.value = "✔️ Progress successfully restored from cloud save!"
                _lastActionMessageTr.value = "✔️ İlerleme buluttan başarıyla geri yüklendi!"
            } else {
                _firebaseSyncState.value = "FAILURE"
                _lastActionMessageEn.value = "❌ No cloud save found for this user."
                _lastActionMessageTr.value = "❌ Bu kullanıcı için bulut kaydı bulunamadı."
            }
        }
    }

    fun loadFirebaseLeaderboard() {
        viewModelScope.launch {
            val board = FirebaseManager.fetchLeaderboard()
            _firebaseLeaderboard.value = board
        }
    }

    companion object {
        fun getPlayerClassString(side: String, momentum: Int): String {
            val isLightBand = momentum >= 65
            val isDarkBand = momentum <= 35
            

            return when (side) {
                "SANCTUM" -> {
                    when {
                        momentum >= 90 -> "Holy Aegis (Kutsal Siper)"
                        isLightBand -> "Codifier (Kanon Büyücüsü)"
                        else -> "Iron Throne (Acımasız İnfazcı)"
                    }
                }
                "COVENANT" -> {
                    when {
                        momentum <= 10 -> "Death Herald (Ölüm Habercisi)"
                        isDarkBand -> "Wildhand (Kaotik Gölge Ustası)"
                        else -> "Tempest (Özgürlük Savaşçısı)"
                    }
                }
                else -> {
                    when {
                        momentum >= 80 -> "Acolyte of Seal"
                        momentum <= 20 -> "Abyss Renegade"
                        else -> "Initiate (Yansıma Adayı)"
                    }
                }
            }
        }
    }
}



enum class StatusType {
    POISONED,
    STUNNED,
    BLESSED,
    SHIELDED
}

data class CombatStatus(
    val type: StatusType,
    var durationTurns: Int
)

enum class EnemyIntent {
    ATTACK,
    DEFEND,
    DEBUFF,
    BUFF;

    companion object {
        fun random(random: kotlin.random.Random = kotlin.random.Random): EnemyIntent {
            val values = values()
            return values[random.nextInt(values.size)]
        }
    }
}
