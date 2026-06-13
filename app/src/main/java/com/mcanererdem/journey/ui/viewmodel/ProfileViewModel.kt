package com.mcanererdem.journey.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.engine.QuestTitleSystem
import com.mcanererdem.journey.data.model.*
import com.mcanererdem.journey.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: GameRepository,
    application: Application,
    private val onMessage: (ActionMessage) -> Unit,
    private val activeLanguage: StateFlow<String>
) : AndroidViewModel(application) {

    val playerProfile: StateFlow<PlayerProfile?> = repository.playerProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val journalEntries: StateFlow<List<JournalEntry>> = repository.journalEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveProfile(profile: PlayerProfile) {
        viewModelScope.launch {
            repository.savePlayerProfile(profile)
        }
    }

    fun insertJournal(entry: JournalEntry) {
        viewModelScope.launch {
            repository.insertJournalEntry(entry)
        }
    }

    fun clearJournal() {
        viewModelScope.launch {
            repository.clearJournal()
        }
    }

    fun setPlayerName(name: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val updated = profile.copy(
                playerName = name,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            onMessage(ActionMessage("msg_name_update", listOf(name)))
        }
    }

    fun selectFaction(faction: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val side = when (faction) {
                "SANCTUM" -> "SANCTUM"
                "COVENANT" -> "COVENANT"
                else -> "NEUTRAL"
            }
            val newMomentum = when (side) {
                "SANCTUM" -> 70
                "COVENANT" -> 30
                else -> 50
            }
            val updated = profile.copy(
                side = side,
                momentum = newMomentum,
                chosenClass = calculatePlayerClass(side, newMomentum),
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            onMessage(ActionMessage("msg_faction_selected", listOf(faction)))
        }
    }

    fun renounceAllegiance() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val updated = profile.copy(
                side = "NEUTRAL",
                momentum = 50,
                chosenClass = calculatePlayerClass("NEUTRAL", 50),
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            onMessage(ActionMessage("msg_faction_renounce"))
        }
    }

    fun checkAndUnlockTitles(profile: PlayerProfile): PlayerProfile {
        val currentUnlocked = profile.titlesEncoded.split(",").filter { it.isNotBlank() }.toMutableSet()
        var changed = false
        
        QuestTitleSystem.titles.forEach { title ->
            if (!currentUnlocked.contains(title.id) && title.meetsPreconditions(profile)) {
                currentUnlocked.add(title.id)
                changed = true
                onMessage(ActionMessage("msg_title_unlocked", listOf(title.nameKey)))
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
                onMessage(
                    if (titleId.isEmpty()) ActionMessage("msg_title_unequipped")
                    else ActionMessage("msg_title_equipped", listOf(QuestTitleSystem.getTitleDef(titleId)!!.nameKey))
                )
            }
        }
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
            val scaledGoldReward = if (quest.rewardGold > 0) (quest.rewardGold * greedMultiplier).toInt() else quest.rewardGold

            val newGold = profile.gold + scaledGoldReward
            val newAether = profile.aether + quest.rewardAether
            
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
            
            val currentItems = profile.itemsEncoded.split(",").filter { it.isNotBlank() }.toMutableList()
            quest.rewardItem?.let { item ->
                if (!currentItems.contains(item)) {
                    currentItems.add(item)
                }
            }
            
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
            
            updated = checkAndUnlockTitles(updated)
            repository.savePlayerProfile(updated)
            
            val questTitleEn = LocalizationManager.getString("EN", quest.titleKey)
            val questTitleTr = LocalizationManager.getString("TR", quest.titleKey)
            onMessage(ActionMessage("msg_quest_claimed", listOf(quest.titleKey)))
        }
    }

    fun claimDailyQuestReward(typeIndex: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val quests = profile.dailyQuestsEncoded.split(",").toMutableList()
            if (typeIndex !in quests.indices) return@launch
            
            val parts = quests[typeIndex].split("/")
            if (parts.size < 3) return@launch
            
            val currentProgress = parts[0].toIntOrNull() ?: 0
            val target = parts[1].toIntOrNull() ?: 0
            val claimed = parts[2].toIntOrNull() ?: 0
            
            if (currentProgress < target || claimed == 1) return@launch
            
            val newParts = parts.toMutableList()
            newParts[2] = "1" // Mark claimed
            quests[typeIndex] = newParts.joinToString("/")
            
            val newQuestsEncoded = quests.joinToString(",")
            
            val baseGold = 50
            val baseAether = 15
            val greedLvl = LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, LegacyUpgradeType.GREED)
            val greedMultiplier = 1.0f + (greedLvl * 0.20f)
            val finalGold = (baseGold * greedMultiplier).toInt()
            
            val updated = profile.copy(
                dailyQuestsEncoded = newQuestsEncoded,
                gold = profile.gold + finalGold,
                aether = profile.aether + baseAether,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            
            onMessage(ActionMessage("msg_daily_reward_claimed", listOf(finalGold, baseAether)))
        }
    }

    fun purchaseUpgrade(upgradeKey: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileDirect() ?: return@launch
            val type = LegacyUpgradeType.values().find { it.key == upgradeKey } ?: return@launch
            val currentLvl = LegacyUpgradeType.getUpgradeLevel(profile.upgradesEncoded, type)
            
            if (currentLvl >= type.maxLevel) return@launch
            
            val cost = type.getCostForLevel(currentLvl)
            if (profile.legacyPoints < cost) return@launch
            
            val upgradesMap = LegacyUpgradeType.getUpgradesMap(profile.upgradesEncoded).toMutableMap()
            upgradesMap[type.key] = currentLvl + 1
            val newUpgradesEncoded = LegacyUpgradeType.encodeUpgrades(upgradesMap)
            
            val updated = profile.copy(
                legacyPoints = profile.legacyPoints - cost,
                upgradesEncoded = newUpgradesEncoded,
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePlayerProfile(updated)
            
            onMessage(ActionMessage("msg_upgrade_purchased", listOf(type.nameKey)))
        }
    }

    fun updateDailyQuestProgress(profile: PlayerProfile, typeIndex: Int, amount: Int): PlayerProfile {
        if (profile.dailyQuestsEncoded.isEmpty()) return profile
        val quests = profile.dailyQuestsEncoded.split(",").toMutableList()
        if (typeIndex !in quests.indices) return profile
        val parts = quests[typeIndex].split("/")
        if (parts.size < 3) return profile
        
        val currentProgress = parts[0].toIntOrNull() ?: 0
        val target = parts[1].toIntOrNull() ?: 0
        val claimed = parts[2].toIntOrNull() ?: 0
        
        if (claimed == 1 || currentProgress >= target) return profile
        
        val newProgress = (currentProgress + amount).coerceAtMost(target)
        val newParts = parts.toMutableList()
        newParts[0] = newProgress.toString()
        quests[typeIndex] = newParts.joinToString("/")
        
        val newQuestsEncoded = quests.joinToString(",")
        
        if (newProgress >= target) {
            onMessage(ActionMessage("msg_daily_quest_completed"))
        }
        
        return profile.copy(
            dailyQuestsEncoded = newQuestsEncoded,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun getPlayerClassString(side: String, momentum: Int, lang: String): String {
        val isLightBand = momentum >= 65
        val isDarkBand = momentum <= 35
        
        val key = when (side) {
            "SANCTUM" -> {
                when {
                    isLightBand -> "ui.class_sanctum_3"
                    isDarkBand -> "ui.class_sanctum_1"
                    else -> "ui.class_sanctum_2"
                }
            }
            "COVENANT" -> {
                when {
                    isDarkBand -> "ui.class_covenant_3"
                    isLightBand -> "ui.class_covenant_1"
                    else -> "ui.class_covenant_2"
                }
            }
            else -> { // NEUTRAL
                when {
                    isLightBand -> "ui.class_neutral_2"
                    isDarkBand -> "ui.class_neutral_1"
                    else -> "ui.class_neutral_0"
                }
            }
        }
        return LocalizationManager.getString(lang, key)
    }

    private fun calculatePlayerClass(side: String, momentum: Int): String {
        return getPlayerClassString(side, momentum, activeLanguage.value)
    }
}
