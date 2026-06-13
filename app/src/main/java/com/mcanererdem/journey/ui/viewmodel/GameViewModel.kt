package com.mcanererdem.journey.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mcanererdem.journey.data.database.GameDatabase
import com.mcanererdem.journey.data.engine.*
import com.mcanererdem.journey.data.model.*
import com.mcanererdem.journey.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val playerProfile: StateFlow<PlayerProfile?>
    val journalEntries: StateFlow<List<JournalEntry>>

    private val _firebaseSyncState = MutableStateFlow("IDLE") // "IDLE", "SYNCING", "SUCCESS", "FAILURE"
    val firebaseSyncState: StateFlow<String> = _firebaseSyncState.asStateFlow()

    private val _firebaseLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val firebaseLeaderboard: StateFlow<List<LeaderboardEntry>> = _firebaseLeaderboard.asStateFlow()

    private val _themeSelection = MutableStateFlow("ALIGNMENT")
    val themeSelection: StateFlow<String> = _themeSelection.asStateFlow()

    private val _showNotificationBanner = MutableStateFlow(true)
    val showNotificationBanner: StateFlow<Boolean> = _showNotificationBanner.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _showTitlePrefix = MutableStateFlow(true)
    val showTitlePrefix: StateFlow<Boolean> = _showTitlePrefix.asStateFlow()

    val activeThemeSide: StateFlow<String>

    private val _activeLanguage = MutableStateFlow("EN") // "TR" or "EN"
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

    // Delegates
    val profileViewModel: ProfileViewModel
    val combatViewModel: CombatViewModel
    val floorViewModel: FloorViewModel

    // Expose sub-ViewModel States
    val currentScenario: StateFlow<FloorScenario?>
    val currentFloorNodes: StateFlow<List<AdventureNode>>

    val activeEnemyHp: StateFlow<Int?>
    val combatLog: StateFlow<List<CombatLogEntry>>
    val playerStatuses: StateFlow<List<CombatStatus>>
    val enemyStatuses: StateFlow<List<CombatStatus>>
    val currentEnemyIntent: StateFlow<EnemyIntent>
    val activeNarrativeEvent: StateFlow<NarrativeEvent?>

    val completedEvents: StateFlow<Set<String>>

    init {
        LocalizationManager.init(application)
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())

        // Load persisted settings
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
                else -> {
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

        // Instantiate delegates
        profileViewModel = ProfileViewModel(
            repository = repository,
            application = application,
            onMessage = { msg -> showActionMessage(msg) },
            activeLanguage = activeLanguage
        )

        floorViewModel = FloorViewModel(
            repository = repository,
            application = application,
            onClearCombat = { combatViewModel.clearCombat() },
            onMessage = { msg -> showActionMessage(msg) },
            activeLanguage = activeLanguage,
            onTriggerSpiritFracture = { profile, momentum, gold, aether, side ->
                triggerSpiritFracture(profile, momentum, gold, aether, side)
            },
            calculatePlayerClass = { side, momentum -> profileViewModel.getPlayerClassString(side, momentum, activeLanguage.value) },
            updateDailyQuestProgress = { profile, idx, amt -> profileViewModel.updateDailyQuestProgress(profile, idx, amt) }
        )

        combatViewModel = CombatViewModel(
            repository = repository,
            application = application,
            activeLanguage = activeLanguage,
            currentFloorNodes = floorViewModel.currentFloorNodes,
            onMessage = { msg -> showActionMessage(msg) },
            onNavigateToTab = { tab -> selectTab(tab) },
            onTriggerSpiritFracture = { profile, momentum, gold, aether, side ->
                triggerSpiritFracture(profile, momentum, gold, aether, side)
            },
            checkAndUnlockTitles = { profile -> profileViewModel.checkAndUnlockTitles(profile) },
            calculatePlayerClass = { side, momentum -> profileViewModel.getPlayerClassString(side, momentum, activeLanguage.value) },
            updateDailyQuestProgress = { profile, idx, amt -> profileViewModel.updateDailyQuestProgress(profile, idx, amt) }
        )

        // Connect delegate states
        currentScenario = floorViewModel.currentScenario
        currentFloorNodes = floorViewModel.currentFloorNodes

        activeEnemyHp = combatViewModel.activeEnemyHp
        combatLog = combatViewModel.combatLog
        playerStatuses = combatViewModel.playerStatuses
        enemyStatuses = combatViewModel.enemyStatuses
        currentEnemyIntent = combatViewModel.currentEnemyIntent
        activeNarrativeEvent = combatViewModel.activeNarrativeEvent

        completedEvents = combatViewModel.completedEvents

        // Automatic orchestration logic
        viewModelScope.launch {
            var lastFloor = -1
            var lastNodeIndex = -1
            var lastNodesList: List<FloorNode> = emptyList()

            // Initial profile bootstrap
            if (repository.getPlayerProfileDirect() == null) {
                val initialProfile = PlayerProfile(
                    playerName = "Lord Alistair",
                    currentFloor = 1,
                    currentHp = 100,
                    maxHp = 100,
                    gold = 150,
                    side = "NEUTRAL",
                    momentum = 50,
                    rank = "EMISSARY",
                    chosenClass = "Initiate",
                    currentWill = 10,
                    maxWill = 10,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(initialProfile)
            }

            repository.playerProfile.collect { profile ->
                if (profile != null) {
                    // 1. Update floor nodes if floor changed
                    if (profile.currentFloor != lastFloor) {
                        lastFloor = profile.currentFloor
                        val blueprint = FloorBlueprintSystem.getBlueprintForFloor(profile.currentFloor, profile)
                        lastNodesList = blueprint.allNodes
                        floorViewModel.updateNodes(lastNodesList)
                        
                        // We reset node index tracking on floor change
                        lastNodeIndex = -1 
                    }

                    // 2. Check combat if node index changed or if combat state needs sync
                    if (profile.currentNodeIndex != lastNodeIndex) {
                        lastNodeIndex = profile.currentNodeIndex
                        if (lastNodesList.isNotEmpty()) {
                            combatViewModel.checkAndInitCombat(profile, lastNodesList, activeLanguage.value)
                        }
                    }

                    // 3. Periodic Title check (still can be done on change, but maybe less frequent?)
                    // For now, keep it simple but avoid loops
                    val checkedProfile = profileViewModel.checkAndUnlockTitles(profile)
                    if (checkedProfile.titlesEncoded != profile.titlesEncoded) {
                        repository.savePlayerProfile(checkedProfile)
                    }
                }
            }
        }
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
        
        val earnedLegacyPoints = (profile.currentFloor / 10) + 1
        val newLegacyPoints = profile.legacyPoints + earnedLegacyPoints

        val updated = profile.copy(
            momentum = newMomentum,
            gold = newGold,
            aether = newAether / 2,
            currentHp = profile.maxHp / 2,
            currentFloor = rollbackFloor,
            currentNodeIndex = 0,
            currentNodeCompleted = false,
            totalFractures = fractureCount,
            legacyPoints = newLegacyPoints,
            chosenClass = profileViewModel.getPlayerClassString(factionSide, newMomentum, activeLanguage.value),
            rank = when {
                rollbackFloor >= 100 -> "SOVEREIGN"
                rollbackFloor >= 25 -> "EXARCH"
                rollbackFloor >= 10 -> "ARBITER"
                else -> "EMISSARY"
            },
            lastUpdated = System.currentTimeMillis()
        )
        repository.savePlayerProfile(updated)

        showActionMessage(ActionMessage("ui.msg_spirit_fracture", listOf(rollbackFloor)))
        _currentTab.value = NavigationTab.TOWER
        combatViewModel.clearCombat()
    }

    fun showActionMessage(message: ActionMessage) {
        _lastActionMessage.value = message
        _showNotificationBanner.value = true
    }

    fun showActionMessage(key: String, args: List<Any> = emptyList()) {
        showActionMessage(ActionMessage(key, args))
    }

    fun changeLanguage(lang: String) {
        _activeLanguage.value = lang
        viewModelScope.launch {
            repository.getPlayerProfileDirect()?.let { profile ->
                val updated = profile.copy(
                    chosenClass = profileViewModel.getPlayerClassString(profile.side, profile.momentum, lang),
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setPlayerName(name: String) {
        profileViewModel.setPlayerName(name)
    }

    fun selectFaction(faction: String) {
        profileViewModel.selectFaction(faction)
    }

    fun renounceAllegiance() {
        profileViewModel.renounceAllegiance()
    }

    fun equipTitle(titleId: String) {
        profileViewModel.equipTitle(titleId)
    }

    fun claimQuestReward(questId: String) {
        profileViewModel.claimQuestReward(questId)
    }

    fun claimDailyQuestReward(typeIndex: Int) {
        profileViewModel.claimDailyQuestReward(typeIndex)
    }

    fun purchaseUpgrade(upgradeKey: String) {
        profileViewModel.purchaseUpgrade(upgradeKey)
    }

    fun initiateTransitionToFloor(targetFloor: Int) {
        floorViewModel.initiateTransitionToFloor(targetFloor)
    }

    fun ascendToNextFloor() {
        floorViewModel.ascendToNextFloor()
    }

    fun selectNodeChoice(choice: NodeChoice) {
        floorViewModel.selectNodeChoice(choice)
    }

    fun selectNodeAt(depth: Int, column: Int) {
        floorViewModel.selectNodeAt(depth, column)
    }

    fun performScouting() {
        // Removed as per request
    }

    fun performAbyssScouting() {
        floorViewModel.performAbyssScouting()
    }

    fun healAndRest(cost: Int) {
        floorViewModel.healAndRest(cost)
    }

    fun tradeCurrency(type: String) {
        floorViewModel.tradeCurrency(type)
    }

    fun handleRpgChoice(option: GameOption) {
        floorViewModel.handleRpgChoice(option)
    }

    fun executeCombatTurn(action: String) {
        combatViewModel.executeCombatTurn(action)
    }

    fun startNarrativeEvent(event: NarrativeEvent) {
        combatViewModel.startNarrativeEvent(event)
    }

    fun cancelNarrativeEvent() {
        combatViewModel.cancelNarrativeEvent()
    }

    fun selectNarrativeEventOption(event: NarrativeEvent, choice: NarrativeBranchOption) {
        combatViewModel.selectNarrativeEventOption(event, choice)
    }



    fun resetGame() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect()
            val oldLegacyPoints = profile?.legacyPoints ?: 0
            val currentFloor = profile?.currentFloor ?: 1
            val currentLevel = profile?.level ?: 1
            
            val earnedPoints = (currentFloor * 2) + (currentLevel * 5)
            val newLegacyPoints = oldLegacyPoints + earnedPoints
            
            val upgradesEncoded = profile?.upgradesEncoded ?: ""
            
            val vitLvl = LegacyUpgradeType.getUpgradeLevel(upgradesEncoded, LegacyUpgradeType.VITALITY)
            val focusLvl = LegacyUpgradeType.getUpgradeLevel(upgradesEncoded, LegacyUpgradeType.AETHER_FOCUS)
            val fortLvl = LegacyUpgradeType.getUpgradeLevel(upgradesEncoded, LegacyUpgradeType.FORTITUDE)
            
            val startMaxHp = 100 + (vitLvl * 10)
            val startAether = focusLvl * 15
            val startMaxWill = 10 + fortLvl
            
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
            
            combatViewModel.clearCombat()
            combatViewModel.clearCompletedEventsAndSlainBosses()
            _currentTab.value = NavigationTab.TOWER
            
            showActionMessage(ActionMessage("ui.msg_reset_success", listOf(earnedPoints)))
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
            val updated = profile.copy(
                legacyPoints = profile.legacyPoints + 5,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            
            showActionMessage(ActionMessage("ui.msg_ad_watched_success"))
            
            // Set cooldown
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
            if (skuId == "remove_ads") {
                val updated = profile.copy(
                    itemsEncoded = if (profile.itemsEncoded.isEmpty()) "Ad-Free Certificate" else "${profile.itemsEncoded},Ad-Free Certificate",
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                showActionMessage(ActionMessage("msg_purchase_adfree_success"))
            } else if (skuId == "seasonal_sovereign_pass") {
                val updated = profile.copy(
                    itemsEncoded = if (profile.itemsEncoded.isEmpty()) "Seasonal Sovereign Pass" else "${profile.itemsEncoded},Seasonal Sovereign Pass",
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                showActionMessage(ActionMessage("msg_purchase_pass_success"))
            }
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
                if (success) {
                    showActionMessage(ActionMessage("ui.msg_sync_success"))
                } else {
                    showActionMessage(ActionMessage("ui.msg_sync_failed"))
                }
            } else {
                _firebaseSyncState.value = "FAILURE"
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
            val board = FirebaseManager.fetchLeaderboard()
            _firebaseLeaderboard.value = board
        }
    }
}
