package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.GameDatabase
import com.example.data.engine.*
import com.example.data.model.JournalEntry
import com.example.data.model.PlayerProfile
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val playerProfile: StateFlow<PlayerProfile?>
    val journalEntries: StateFlow<List<JournalEntry>>

    // Game interface states
    private val _activeLanguage = MutableStateFlow("TR") // "TR" or "EN"
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

    init {
        LocalizationManager.init(application)
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())

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

        // Automatically load procedurally generated floor nodes when player profile loads
        viewModelScope.launch {
            repository.playerProfile.collect { profile ->
                if (profile != null) {
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
            
            // Validate Will Cost
            if (choice.willChange < 0 && profile.currentWill < -choice.willChange) {
                _lastActionMessageEn.value = "❌ Insufficient Willpower to make this choice!"
                _lastActionMessageTr.value = "❌ Bu kararı seçmek için yeterli İradeniz yok!"
                return@launch
            }

            val newAlignment = (profile.alignment + choice.alignmentShift).coerceIn(-100, 100)
            val newGold = (profile.gold + choice.goldChange).coerceAtLeast(0)
            val newGleam = (profile.gleam + choice.gleamChange).coerceAtLeast(0)
            val newPyre = (profile.pyre + choice.pyreChange).coerceAtLeast(0)
            val newWill = (profile.currentWill + choice.willChange).coerceIn(0, profile.maxWill)
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
                alignmentImpact = choice.alignmentShift
            )
            repository.insertJournalEntry(logEntry)

            _lastActionMessageEn.value = choice.journalEn
            _lastActionMessageTr.value = choice.journalTr

            if (newHp <= 0) {
                triggerSpiritFracture(profile, newAlignment, newGold, newGleam, newPyre, activeFactionSide)
            } else {
                val updated = profile.copy(
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
                    currentNodeCompleted = true, // complete active node
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
            
            // Sinking into a new node in sequence costs 1 Will
            if (profile.currentWill < 1) {
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
                    currentWill = profile.currentWill - 1, // Deduct Will
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                _lastActionMessageEn.value = "Advanced deeper into Floor ${profile.currentFloor}. Sektor: ${nextNode.title}."
                _lastActionMessageTr.value = "${profile.currentFloor}. Katta yeni bir sektöre adım attınız: ${nextNode.titleTr}."
            }
        }
    }

    fun ascendToNextFloor() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            
            // Leaving floor costs 2 Will
            if (profile.currentWill < 2) {
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
                currentWill = profile.currentWill - 2, // Deduct transit fee
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

    fun executeCombatTurn(action: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val nodes = _currentFloorNodes.value
            if (profile.currentNodeIndex !in nodes.indices) return@launch
            val activeNode = nodes[profile.currentNodeIndex]
            
            val currentEnemyHp = _activeEnemyHp.value ?: activeNode.enemyHp

            var baseDmg = 12 + profile.level * 2
            var actionDescriptionEn = ""
            var actionDescriptionTr = ""
            var selfHpDmg = 0

            when (action) {
                "STRIKE" -> {
                    val actualDmg = (baseDmg * 0.9).toInt() + (0..6).random()
                    val actualEnemyHp = (currentEnemyHp - actualDmg).coerceAtLeast(0)
                    _activeEnemyHp.value = actualEnemyHp

                    actionDescriptionEn = "You swung your weapon at ${activeNode.enemyNameEn}, dealing $actualDmg damage."
                    actionDescriptionTr = "${activeNode.enemyNameTr} üzerine hücum edip silahınızla $actualDmg hasar verdiniz."
                }
                "SKILL" -> {
                    val multiplier = if (profile.side != "NEUTRAL") 2.5f else 1.8f
                    val actualDmg = (baseDmg * multiplier).toInt() + (0..10).random()
                    val actualEnemyHp = (currentEnemyHp - actualDmg).coerceAtLeast(0)
                    _activeEnemyHp.value = actualEnemyHp

                    selfHpDmg = 10
                    val skillNames = getSkillNameForClass(profile.chosenClass)

                    actionDescriptionEn = "Unleashed Sınıf Yeteneği [${skillNames.first}], evoking $actualDmg cosmic damage (Fatigued: -$selfHpDmg HP)."
                    actionDescriptionTr = "Sınıf Yeteneği [${skillNames.second}] fırlatarak $actualDmg kozmik hasar verdiniz (Yorgunluk: -$selfHpDmg HP)."
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
            
            if (_activeLanguage.value == "TR") {
                logs.add("👤 $actionDescriptionTr")
            } else {
                logs.add("👤 $actionDescriptionEn")
            }

            var playerHp = (profile.currentHp - selfHpDmg).coerceAtLeast(0)

            if (newEnemyHp <= 0) {
                // Victory! Compute drops and EXP scaling
                val expReward = if (activeNode.type == NodeType.BOSS) (90 + profile.currentFloor * 5) else (20 + profile.currentFloor * 2)
                val goldReward = if (activeNode.type == NodeType.BOSS) (70 + (10..30).random()) else (12 + (4..12).random())
                
                var dropItem = ""
                var dropTitle = ""
                
                val roll = (1..100).random()
                if (roll <= 45 || activeNode.type == NodeType.BOSS) {
                    dropItem = AdventureEngine.getRandomLootItem(profile.currentFloor, Random)
                }
                if (activeNode.type == NodeType.BOSS && roll <= 50) {
                    dropTitle = AdventureEngine.getRandomCombatTitle(profile.currentFloor, Random)
                }

                _activeEnemyHp.value = null
                
                var newExp = profile.exp + expReward
                var newLevel = profile.level
                var newMaxExp = profile.maxExp
                var newMaxHp = profile.maxHp

                while (newExp >= newMaxExp && newLevel < 100) {
                    newExp -= newMaxExp
                    newLevel++
                    newMaxExp = newLevel * 100
                    newMaxHp += 20
                    playerHp += 20 // full heal on level up bounds
                }

                // Append drops to profile serialization
                var currentItems = if (profile.itemsEncoded.isEmpty()) emptyList() else profile.itemsEncoded.split(",")
                if (dropItem.isNotEmpty()) currentItems = currentItems + dropItem
                val newItemsEncoded = currentItems.joinToString(",")

                var currentTitles = if (profile.titlesEncoded.isEmpty()) emptyList() else profile.titlesEncoded.split(",")
                if (dropTitle.isNotEmpty()) currentTitles = currentTitles + dropTitle
                val newTitlesEncoded = currentTitles.joinToString(",")

                val updatedProfile = profile.copy(
                    currentHp = playerHp.coerceAtMost(newMaxHp),
                    maxHp = newMaxHp,
                    level = newLevel,
                    exp = newExp,
                    maxExp = newMaxExp,
                    gold = profile.gold + goldReward,
                    itemsEncoded = newItemsEncoded,
                    titlesEncoded = newTitlesEncoded,
                    currentNodeCompleted = true,
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updatedProfile)

                if (_activeLanguage.value == "TR") {
                    logs.add("🎉 ZAFER! Düşman katledildi. +$expReward Deneyim, +$goldReward Altın kazanıldı.")
                    if (dropItem.isNotEmpty()) logs.add("🎁 Hazine: [$dropItem] toplandı teçhizat torbana konuldu!")
                    if (dropTitle.isNotEmpty()) logs.add("👑 Yeni Unvan Kazandınız: [$dropTitle]!")
                } else {
                    logs.add("🎉 VICTORY! Enemy defeated. Won +$expReward EXP, +$goldReward Gold.")
                    if (dropItem.isNotEmpty()) logs.add("🎁 Loot Pick: [$dropItem] added to inventory!")
                    if (dropTitle.isNotEmpty()) logs.add("👑 Achieved Epic Title: [$dropTitle]!")
                }

                // Insert into chronology db
                val journalEntry = JournalEntry(
                    floor = profile.currentFloor,
                    actionTakenEs = "Defeated ${activeNode.enemyNameEn} in combat on Floor ${profile.currentFloor}.",
                    actionTakenTr = "${profile.currentFloor}. Katta ${activeNode.enemyNameTr} karşısındaki düelloyu kazandınız.",
                    sideAlignmentShift = "NEUTRAL",
                    alignmentImpact = 0
                )
                repository.insertJournalEntry(journalEntry)

                _combatLog.value = _combatLog.value + logs
                _lastActionMessageEn.value = "Defeated ${activeNode.enemyNameEn}!"
                _lastActionMessageTr.value = "${activeNode.enemyNameTr} yok edildi!"
            } else {
                // Enemy retaliates
                val enemyDmg = (activeNode.enemyAtk * 0.85).toInt() + (0..4).random()
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
