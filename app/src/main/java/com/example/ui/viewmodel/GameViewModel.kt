package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.GameDatabase
import com.example.data.engine.FloorScenario
import com.example.data.engine.GameOption
import com.example.data.engine.NarrativeEngine
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

    init {
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

        // Ensure default player exists on launch
        viewModelScope.launch {
            val direct = repository.getPlayerProfileDirect()
            if (direct == null) {
                val newProfile = PlayerProfile(
                    playerName = "Lord Alistair",
                    currentFloor = 1,
                    currentHp = 100,
                    maxHp = 100,
                    gold = 150,
                    side = "NEUTRAL"
                )
                repository.savePlayerProfile(newProfile)
                _currentScenario.value = NarrativeEngine.getScenarioForFloor(1)
            } else {
                _currentScenario.value = NarrativeEngine.getScenarioForFloor(direct.currentFloor)
            }
        }
    }

    fun changeLanguage(lang: String) {
        _activeLanguage.value = lang
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
                _currentScenario.value = NarrativeEngine.getScenarioForFloor(rollbackFloor)
                
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
                    _currentScenario.value = NarrativeEngine.getScenarioForFloor(nextFloor)
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
                    lastUpdated = System.currentTimeMillis()
                )
                repository.savePlayerProfile(updated)
                _lastActionMessageEn.value = "💖 RESTORED: Completely recovered life force in the Outer Haven."
                _lastActionMessageTr.value = "💖 DOYUM: Dış Sığınak kaplıcalarında yaşam gücü tamamen yenilendi."
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
                savedFloorCheckpoint = 1
            )
            repository.savePlayerProfile(newProfile)
            _currentScenario.value = NarrativeEngine.getScenarioForFloor(1)
            _lastActionMessageEn.value = "Game restarted. Chronology reset."
            _lastActionMessageTr.value = "Zaman döngüsü sıfırlandı. Macera yeniden başlıyor."
            _currentTab.value = "TOWER"
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
