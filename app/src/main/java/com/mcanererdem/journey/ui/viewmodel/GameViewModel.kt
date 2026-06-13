package com.mcanererdem.journey.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mcanererdem.journey.data.engine.*
import com.mcanererdem.journey.data.model.*
import com.mcanererdem.journey.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val playerProfile: StateFlow<PlayerProfile?>
    val journalEntries: StateFlow<List<JournalEntry>>

    private val _firebaseSyncState = MutableStateFlow("IDLE")
    val firebaseSyncState: StateFlow<String> = _firebaseSyncState.asStateFlow()

    private val _firebaseLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val firebaseLeaderboard: StateFlow<List<LeaderboardEntry>> = _firebaseLeaderboard.asStateFlow()

    private val _themeSelection = MutableStateFlow("ALIGNMENT")
    val themeSelection: StateFlow<String> = _themeSelection.asStateFlow()

    private val _uiMode = MutableStateFlow("DARK") // "LIGHT", "DARK", "SYSTEM"
    val uiMode: StateFlow<String> = _uiMode.asStateFlow()

    private val _animationsEnabled = MutableStateFlow(true)
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    private val _glowEffectsEnabled = MutableStateFlow(true)
    val glowEffectsEnabled: StateFlow<Boolean> = _glowEffectsEnabled.asStateFlow()

    private val _showNotificationBanner = MutableStateFlow(true)
    val showNotificationBanner: StateFlow<Boolean> = _showNotificationBanner.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _showTitlePrefix = MutableStateFlow(true)
    val showTitlePrefix: StateFlow<Boolean> = _showTitlePrefix.asStateFlow()

    val activeThemeSide: StateFlow<String>

    private val _activeLanguage = MutableStateFlow("EN")
    val activeLanguage: StateFlow<String> = _activeLanguage.asStateFlow()

    private val _lastActionMessage = MutableStateFlow(ActionMessage("ui.msg_welcome"))
    val lastActionMessage: StateFlow<ActionMessage> = _lastActionMessage.asStateFlow()

    private val _currentTab = MutableStateFlow(NavigationTab.TOWER)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _isAdWatching = MutableStateFlow(false)
    val isAdWatching: StateFlow<Boolean> = _isAdWatching.asStateFlow()

    private val _adCooldownSeconds = MutableStateFlow(0)
    val adCooldownSeconds: StateFlow<Int> = _adCooldownSeconds.asStateFlow()

    private val _isPurchaseDialogShown = MutableStateFlow(false)
    val isPurchaseDialogShown: StateFlow<Boolean> = _isPurchaseDialogShown.asStateFlow()

    val combatViewModel: CombatViewModel
    val currentFloorNodes: StateFlow<List<AdventureNode>>
    val currentScenario: StateFlow<FloorScenario?>
    val activeEnemyHp: StateFlow<Int?>
    val combatLog: StateFlow<List<CombatLogEntry>>
    val playerStatuses: StateFlow<List<CombatStatus>>
    val enemyStatuses: StateFlow<List<CombatStatus>>
    val currentEnemyIntent: StateFlow<EnemyIntent>

    val completedEvents: StateFlow<Set<String>>
    val activeNarrativeEvent: StateFlow<NarrativeEvent?>

    init {
        repository = GameRepository.getInstance(application)
        playerProfile = repository.playerProfile
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
        journalEntries = repository.journalEntries
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        val nodesFlow = playerProfile.map { profile ->
            if (profile == null) emptyList()
            else AdventureEngine.generateNodesForFloor(profile.currentFloor, profile)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        
        currentFloorNodes = nodesFlow

        combatViewModel = CombatViewModel(
            repository = repository,
            application = application,
            activeLanguage = _activeLanguage,
            currentFloorNodes = nodesFlow,
            onMessage = { showActionMessage(it) },
            onNavigateToTab = { selectTab(it) },
            onTriggerSpiritFracture = { _, _, _, _, _ -> },
            checkAndUnlockTitles = { it },
            calculatePlayerClass = { _, _ -> "Outcast" },
            updateDailyQuestProgress = { p, _, _ -> p }
        )
        
        currentScenario = combatViewModel.activeNarrativeEvent.map { event ->
            if (event == null) null else {
                FloorScenario(
                    floor = playerProfile.value?.currentFloor ?: 1,
                    titleKey = event.titleKey,
                    descriptionKey = event.descriptionKey,
                    options = event.options.map { opt ->
                        GameOption(
                            id = opt.id,
                            labelKey = opt.textKey,
                            journalKey = opt.outcomeKey,
                            effects = ChoiceEffects(
                                hpChange = opt.hpChange,
                                goldChange = opt.goldChange,
                                aetherChange = opt.aetherChange,
                                expChange = opt.expReward,
                                momentumShift = opt.alignmentImpact,
                                rewardItemId = opt.itemReward,
                                rewardTitleId = opt.titleReward
                            )
                        )
                    }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
        
        activeEnemyHp = combatViewModel.activeEnemyHp
        combatLog = combatViewModel.combatLog
        playerStatuses = combatViewModel.playerStatuses
        enemyStatuses = combatViewModel.enemyStatuses
        currentEnemyIntent = combatViewModel.currentEnemyIntent
        completedEvents = combatViewModel.completedEvents
        activeNarrativeEvent = combatViewModel.activeNarrativeEvent

        // Load Settings
        val prefs = application.getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        _themeSelection.value = prefs.getString("themeSelection", "ALIGNMENT") ?: "ALIGNMENT"
        _uiMode.value = prefs.getString("uiMode", "DARK") ?: "DARK"
        _animationsEnabled.value = prefs.getBoolean("animationsEnabled", true)
        _glowEffectsEnabled.value = prefs.getBoolean("glowEffectsEnabled", true)
        _showNotificationBanner.value = prefs.getBoolean("showNotificationBanner", true)
        _soundEnabled.value = prefs.getBoolean("soundEnabled", true)
        _showTitlePrefix.value = prefs.getBoolean("showTitlePrefix", true)
        _activeLanguage.value = prefs.getString("language", "EN") ?: "EN"

        activeThemeSide = combine(playerProfile, _themeSelection) { profile, selection ->
            if (selection == "ALIGNMENT") {
                profile?.side ?: "NEUTRAL"
            } else {
                selection
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "NEUTRAL")

        LocalizationManager.init(application)

        // Initialize default profile if missing
        viewModelScope.launch {
            val existing = repository.getPlayerProfileDirect()
            if (existing == null) {
                val newProfile = PlayerProfile(
                    playerName = "New Climber",
                    currentHp = 100,
                    maxHp = 100,
                    aether = 100,
                    currentWill = 50,
                    maxWill = 100
                )
                repository.savePlayerProfile(newProfile)
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun changeLanguage(lang: String) {
        _activeLanguage.value = lang
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("language", lang).apply()
    }

    fun handleRpgChoice(option: GameOption) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val (updatedProfile, logs) = combatViewModel.processScenarioChoice(profile, option)
            repository.savePlayerProfile(updatedProfile)
            logs.forEach { repository.insertJournalEntry(it) }
        }
    }

    fun selectNodeAt(depth: Int, column: Int) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val updated = combatViewModel.moveToNode(profile, depth, column)
            if (updated != null) {
                repository.savePlayerProfile(updated)
            }
        }
    }

    fun selectNodeChoice(choice: NodeChoice) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val (updated, journal) = combatViewModel.applyNodeChoice(profile, choice)
            repository.savePlayerProfile(updated)
            if (journal != null) {
                repository.insertJournalEntry(journal)
            }
        }
    }

    fun executeCombatTurn(actionId: String) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val updated = combatViewModel.processCombatTurn(profile, actionId)
            repository.savePlayerProfile(updated)
        }
    }

    fun ascendToNextFloor() {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val updated = combatViewModel.ascendFloor(profile)
            repository.savePlayerProfile(updated)
        }
    }

    fun healAndRest(amount: Int) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val updated = profile.copy(currentHp = (profile.currentHp + amount).coerceAtMost(profile.maxHp))
            repository.savePlayerProfile(updated)
        }
    }

    fun performAbyssScouting() {
        // Obsolete but kept for now
    }

    fun tradeCurrency(type: String) {
        // Logic for trading
    }

    fun setPlayerName(name: String) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            repository.savePlayerProfile(profile.copy(playerName = name))
        }
    }

    fun selectFaction(faction: String) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            repository.savePlayerProfile(profile.copy(side = faction))
        }
    }

    fun renounceAllegiance() {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            repository.savePlayerProfile(profile.copy(side = "NEUTRAL", chosenClass = "Outcast"))
        }
    }

    fun purchaseUpgrade(upgradeId: String) {
        // Upgrade logic
    }

    fun claimDailyQuestReward(questId: String) {
        // Body handled by claimQuestReward if matched by ID
    }

    fun claimQuestReward(questId: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val quest = QuestTitleSystem.quests.find { it.id == questId } ?: return@launch
            
            val completedSet = profile.completedQuestsEncoded.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (completedSet.contains(questId)) return@launch
            if (!quest.checkProgress(profile)) return@launch
            
            completedSet.add(questId)
            val greedLvl = LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, LegacyUpgradeType.GREED)
            val greedMultiplier = 1.0f + (greedLvl * 0.20f)
            val scaledGoldReward = (quest.rewardGold * greedMultiplier).toInt()

            val updated = profile.copy(
                completedQuestsEncoded = completedSet.joinToString(","),
                gold = profile.gold + scaledGoldReward,
                aether = profile.aether + quest.rewardAether,
                exp = profile.exp + quest.rewardExp,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            showActionMessage(ActionMessage("ui.msg_quest_claimed", listOf(quest.titleKey)))
        }
    }

    fun equipTitle(titleId: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val unlocked = profile.titlesEncoded.split(",").filter { it.isNotBlank() }.toSet()
            if (titleId.isEmpty() || unlocked.contains(titleId)) {
                repository.savePlayerProfile(profile.copy(equippedTitle = titleId))
            }
        }
    }

    fun initiateTransitionToFloor(targetFloor: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            when (val res = FloorStateManager.attemptFloorTransition(profile, targetFloor)) {
                is FloorStateManager.TransitionResult.Success -> {
                    repository.savePlayerProfile(res.updatedProfile)
                    combatViewModel.clearCombat()
                    showActionMessage(ActionMessage(res.messageKey, res.messageArgs))
                }
                is FloorStateManager.TransitionResult.Failure -> {
                    showActionMessage(ActionMessage(res.reasonKey, res.reasonArgs))
                }
            }
        }
    }

    fun selectNarrativeEventOption(event: NarrativeEvent, choice: NarrativeBranchOption) {
        combatViewModel.selectNarrativeEventOption(event, choice)
    }

    fun cancelNarrativeEvent() {
        combatViewModel.cancelNarrativeEvent()
    }

    fun startNarrativeEvent(event: NarrativeEvent) {
        combatViewModel.startNarrativeEvent(event)
    }

    fun showActionMessage(message: ActionMessage) {
        _lastActionMessage.value = message
        // Only show if setting is enabled
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        if (prefs.getBoolean("showNotificationBanner", true)) {
            _showNotificationBanner.value = true
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val earnedPoints = (profile.currentFloor / 10).coerceAtLeast(0)
            
            val newProfile = PlayerProfile(
                playerName = profile.playerName,
                legacyPoints = profile.legacyPoints + earnedPoints,
                lastLoginTimestamp = System.currentTimeMillis()
            )
            repository.savePlayerProfile(newProfile)
            combatViewModel.clearCombat()
            _currentTab.value = NavigationTab.TOWER
        }
    }

    fun setPurchaseDialogShown(shown: Boolean) {
        _isPurchaseDialogShown.value = shown
    }

    fun setThemeSelection(selection: String) {
        _themeSelection.value = selection
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("themeSelection", selection).apply()
    }

    fun setUiMode(mode: String) {
        _uiMode.value = mode
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("uiMode", mode).apply()
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        _animationsEnabled.value = enabled
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("animationsEnabled", enabled).apply()
    }

    fun setGlowEffectsEnabled(enabled: Boolean) {
        _glowEffectsEnabled.value = enabled
        val prefs = getApplication<Application>().getSharedPreferences("rpg_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("glowEffectsEnabled", enabled).apply()
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

    fun watchRewardedAd() {
        viewModelScope.launch {
            if (_adCooldownSeconds.value > 0) return@launch
            _isAdWatching.value = true
            kotlinx.coroutines.delay(2000)
            _isAdWatching.value = false
            
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            repository.savePlayerProfile(profile.copy(legacyPoints = profile.legacyPoints + 5))
            showActionMessage(ActionMessage("ui.msg_ad_watched_success"))
            
            _adCooldownSeconds.value = 60
            while (_adCooldownSeconds.value > 0) {
                kotlinx.coroutines.delay(1000)
                _adCooldownSeconds.value--
            }
        }
    }

    fun purchaseProduct(skuId: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            // Purchase logic
            _isPurchaseDialogShown.value = false
        }
    }

    fun syncProfileToFirebase() {
        viewModelScope.launch {
            _firebaseSyncState.value = "SYNCING"
            val profile = repository.getPlayerProfileDirect()
            if (profile != null) {
                val success = FirebaseManager.syncProfileToCloud(profile)
                _firebaseSyncState.value = if (success) "SUCCESS" else "FAILURE"
                showActionMessage(ActionMessage(if (success) "ui.msg_sync_success" else "ui.msg_sync_failed"))
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
                showActionMessage(ActionMessage("ui.msg_restore_success"))
            } else {
                _firebaseSyncState.value = "FAILURE"
                showActionMessage(ActionMessage("ui.msg_restore_failed"))
            }
        }
    }

    fun loadFirebaseLeaderboard() {
        viewModelScope.launch {
            _firebaseLeaderboard.value = FirebaseManager.fetchLeaderboard()
        }
    }
}
