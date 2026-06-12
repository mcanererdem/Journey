package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.PlayerProfile

/**
 * Definition of a Title with preconditions, descriptions and stats indicators.
 */
data class TitleDef(
    val id: String,
    val nameKey: String,
    val descKey: String,
    val isHidden: Boolean,
    val requirementDescKey: String,
    val hpBonus: Int = 0,
    val goldBonusPercent: Int = 0,
    val meetsPreconditions: (PlayerProfile) -> Boolean
)

/**
 * Quest Category types mapped to requested categories.
 */
enum class QuestType {
    MAIN,
    SIDE,
    NORMAL,
    SPECIAL,
    CHAIN,
    HIDDEN,
    EVENT
}

/**
 * Status tracking values for Quests as defined in requested specifications.
 */
enum class QuestStatusType {
    LOCKED,     // Prerequisites or preconditions are not met
    ACTIVE,     // Preconditions met, task is currently active/in-progress
    COMPLETED,  // Completed and rewards have been claimed
    HIDDEN      // Completely mystified from the quest log boards until unlocked
}

/**
 * Definition of a Quest with prerequisite check, preconditions met check, state verification, and rewards.
 */
data class QuestDef(
    val id: String,
    val type: QuestType,
    val titleKey: String,
    val descKey: String,
    val requirementKey: String,
    val prerequisiteQuestId: String? = null,
    val meetsPreconditions: (PlayerProfile) -> Boolean = { true },
    val checkProgress: (PlayerProfile) -> Boolean,
    val rewardGold: Int = 0,
    val rewardExp: Int = 0,
    val rewardItem: String? = null,
    val rewardTitle: String? = null,
    val rewardAether: Int = 0
)

/**
 * Engine Registry tracking all available Titles & Quests in Light & Darkness.
 */
object QuestTitleSystem {

    val titles = listOf(
        TitleDef(
            id = "lightseeker",
            nameKey = "title.lightseeker.name",
            descKey = "title.lightseeker.desc",
            isHidden = false,
            requirementDescKey = "title.lightseeker.req",
            hpBonus = 15,
            meetsPreconditions = { it.level >= 3 && it.momentum >= 70 }
        ),
        TitleDef(
            id = "void_reaper",
            nameKey = "title.void_reaper.name",
            descKey = "title.void_reaper.desc",
            isHidden = false,
            requirementDescKey = "title.void_reaper.req",
            hpBonus = 25,
            meetsPreconditions = { it.level >= 5 && it.momentum <= 15 }
        ),
        TitleDef(
            id = "cosmic_observer",
            nameKey = "title.cosmic_observer.name",
            descKey = "title.cosmic_observer.desc",
            isHidden = true,
            requirementDescKey = "title.cosmic_observer.req",
            hpBonus = 30,
            meetsPreconditions = { it.currentFloor >= 10 && it.momentum == 50 }
        ),
        TitleDef(
            id = "immortal_phantom",
            nameKey = "title.immortal_phantom.name",
            descKey = "title.immortal_phantom.desc",
            isHidden = true,
            requirementDescKey = "title.immortal_phantom.req",
            hpBonus = 40,
            meetsPreconditions = { it.totalFractures >= 5 }
        ),
        TitleDef(
            id = "spire_conqueror",
            nameKey = "title.spire_conqueror.name",
            descKey = "title.spire_conqueror.desc",
            isHidden = false,
            requirementDescKey = "title.spire_conqueror.req",
            hpBonus = 50,
            meetsPreconditions = { it.currentFloor >= 50 }
        ),
        TitleDef(
            id = "gold_hoarder",
            nameKey = "title.gold_hoarder.name",
            descKey = "title.gold_hoarder.desc",
            isHidden = false,
            requirementDescKey = "title.gold_hoarder.req",
            hpBonus = 10,
            meetsPreconditions = { it.gold >= 500 }
        ),
        TitleDef(
            id = "doomsday_bringer",
            nameKey = "title.doomsday_bringer.name",
            descKey = "title.doomsday_bringer.desc",
            isHidden = true,
            requirementDescKey = "title.doomsday_bringer.req",
            hpBonus = 60,
            meetsPreconditions = { it.side == "COVENANT" && it.level >= 15 && it.momentum <= 15 }
        ),
        TitleDef(
            id = "archon_sage",
            nameKey = "title.archon_sage.name",
            descKey = "title.archon_sage.desc",
            isHidden = false,
            requirementDescKey = "title.archon_sage.req",
            hpBonus = 60,
            meetsPreconditions = { it.side == "SANCTUM" && it.level >= 15 && it.momentum >= 85 }
        ),
        TitleDef(
            id = "plague_vanquisher",
            nameKey = "title.plague_vanquisher.name",
            descKey = "title.plague_vanquisher.desc",
            isHidden = false,
            requirementDescKey = "title.plague_vanquisher.req",
            hpBonus = 15,
            meetsPreconditions = { it.currentFloor > 1 }
        ),
        TitleDef(
            id = "shard_bearer",
            nameKey = "title.shard_bearer.name",
            descKey = "title.shard_bearer.desc",
            isHidden = false,
            requirementDescKey = "title.shard_bearer.req",
            hpBonus = 25,
            meetsPreconditions = { it.currentFloor > 2 }
        ),
        TitleDef(
            id = "crypt_breaker",
            nameKey = "title.crypt_breaker.name",
            descKey = "title.crypt_breaker.desc",
            isHidden = false,
            requirementDescKey = "title.crypt_breaker.req",
            hpBonus = 35,
            meetsPreconditions = { it.currentFloor > 3 }
        )
    )

    val quests = listOf(
        // --- FLOOR PROGRESSION QUESTS ---
        QuestDef(
            id = "floor_1_rat_king",
            type = QuestType.MAIN,
            titleKey = "quest.floor_1_rat_king.title",
            descKey = "quest.floor_1_rat_king.desc",
            requirementKey = "quest.floor_1_rat_king.req",
            checkProgress = { it.currentFloor > 1 },
            rewardGold = 100,
            rewardExp = 150,
            rewardItem = "Familiar: Pet Ember Kitten",
            rewardTitle = "title.plague_vanquisher.name"
        ),
        QuestDef(
            id = "floor_2_crystal_heart",
            type = QuestType.MAIN,
            titleKey = "quest.floor_2_crystal_heart.title",
            descKey = "quest.floor_2_crystal_heart.desc",
            requirementKey = "quest.floor_2_crystal_heart.req",
            prerequisiteQuestId = "floor_1_rat_king",
            checkProgress = { it.currentFloor > 2 },
            rewardGold = 150,
            rewardExp = 200,
            rewardItem = "Companion: Holographic Pixie",
            rewardTitle = "title.shard_bearer.name"
        ),
        QuestDef(
            id = "floor_3_oracle_scourge",
            type = QuestType.MAIN,
            titleKey = "quest.floor_3_oracle_scourge.title",
            descKey = "quest.floor_3_oracle_scourge.desc",
            requirementKey = "quest.floor_3_oracle_scourge.req",
            prerequisiteQuestId = "floor_2_crystal_heart",
            checkProgress = { it.currentFloor > 3 },
            rewardGold = 200,
            rewardExp = 300,
            rewardItem = "Familiar: Petite Void Drake",
            rewardTitle = "title.crypt_breaker.name"
        ),

        // --- MAIN QUESTS ---
        QuestDef(
            id = "main_foothold",
            type = QuestType.MAIN,
            titleKey = "quest.main_foothold.title",
            descKey = "quest.main_foothold.desc",
            requirementKey = "quest.main_foothold.req",
            checkProgress = { it.currentFloor >= 5 },
            rewardGold = 80,
            rewardExp = 120
        ),
        QuestDef(
            id = "main_midpoint",
            type = QuestType.MAIN,
            titleKey = "quest.main_midpoint.title",
            descKey = "quest.main_midpoint.desc",
            requirementKey = "quest.main_midpoint.req",
            checkProgress = { it.currentFloor >= 15 },
            rewardGold = 180,
            rewardExp = 250,
            rewardItem = "Aegis Shard of Eternity"
        ),

        // --- SIDE QUESTS ---
        QuestDef(
            id = "side_wealth",
            type = QuestType.SIDE,
            titleKey = "quest.side_wealth.title",
            descKey = "quest.side_wealth.desc",
            requirementKey = "quest.side_wealth.req",
            checkProgress = { it.gold >= 300 },
            rewardExp = 200,
            rewardItem = "Aetherweave Cloak of Sanctum"
        ),
        QuestDef(
            id = "side_sanctum_purity",
            type = QuestType.SIDE,
            titleKey = "quest.side_sanctum_purity.title",
            descKey = "quest.side_sanctum_purity.desc",
            requirementKey = "quest.side_sanctum_purity.req",
            checkProgress = { it.side == "SANCTUM" && it.momentum >= 65 },
            rewardExp = 250,
            rewardGold = 80,
            rewardAether = 60
        ),
        QuestDef(
            id = "side_void_alliance",
            type = QuestType.SIDE,
            titleKey = "quest.side_void_alliance.title",
            descKey = "quest.side_void_alliance.desc",
            requirementKey = "quest.side_void_alliance.req",
            checkProgress = { it.side == "COVENANT" && it.momentum <= 35 },
            rewardExp = 250,
            rewardGold = 80,
            rewardAether = 60
        ),

        // --- CHAIN QUESTS ---
        QuestDef(
            id = "chain_ascension_1",
            type = QuestType.CHAIN,
            titleKey = "quest.chain_ascension_1.title",
            descKey = "quest.chain_ascension_1.desc",
            requirementKey = "quest.chain_ascension_1.req",
            checkProgress = { it.level >= 4 },
            rewardExp = 100,
            rewardGold = 40,
            rewardTitle = "Tower Novice Apprentice"
        ),
        QuestDef(
            id = "chain_ascension_2",
            type = QuestType.CHAIN,
            titleKey = "quest.chain_ascension_2.title",
            descKey = "quest.chain_ascension_2.desc",
            requirementKey = "quest.chain_ascension_2.req",
            prerequisiteQuestId = "chain_ascension_1",
            checkProgress = { it.level >= 12 },
            rewardExp = 300,
            rewardGold = 150,
            rewardItem = "Celesta Dawnbreaker Sword"
        ),
        QuestDef(
            id = "chain_ascension_3",
            type = QuestType.CHAIN,
            titleKey = "quest.chain_ascension_3.title",
            descKey = "quest.chain_ascension_3.desc",
            requirementKey = "quest.chain_ascension_3.req",
            prerequisiteQuestId = "chain_ascension_2",
            checkProgress = { it.level >= 20 },
            rewardExp = 800,
            rewardGold = 400,
            rewardTitle = "Apex Ascendant Sovereign"
        ),

        // --- HIDDEN QUESTS ---
        QuestDef(
            id = "hidden_fractured_soul",
            type = QuestType.HIDDEN,
            titleKey = "quest.hidden_fractured_soul.title",
            descKey = "quest.hidden_fractured_soul.desc",
            requirementKey = "quest.hidden_fractured_soul.req",
            checkProgress = { it.totalFractures >= 4 },
            rewardExp = 400,
            rewardItem = "Shattered Skull of Resiliency",
            rewardTitle = "Fracture Vanguard Hero"
        ),
        QuestDef(
            id = "hidden_perfect_balance",
            type = QuestType.HIDDEN,
            titleKey = "quest.hidden_perfect_balance.title",
            descKey = "quest.hidden_perfect_balance.desc",
            requirementKey = "quest.hidden_perfect_balance.req",
            checkProgress = { it.currentFloor >= 15 && it.momentum == 50 },
            rewardExp = 500,
            rewardGold = 250,
            rewardTitle = "Guardian of Cosmic Neutrality"
        ),

        // --- NORMAL QUESTS ---
        QuestDef(
            id = "normal_climb_3",
            type = QuestType.NORMAL,
            titleKey = "quest.normal_climb_3.title",
            descKey = "quest.normal_climb_3.desc",
            requirementKey = "quest.normal_climb_3.req",
            checkProgress = { it.currentFloor >= 3 },
            rewardGold = 50,
            rewardExp = 80
        ),

        // --- SPECIAL QUESTS ---
        QuestDef(
            id = "special_alignment_pioneer",
            type = QuestType.SPECIAL,
            titleKey = "quest.special_alignment_pioneer.title",
            descKey = "quest.special_alignment_pioneer.desc",
            requirementKey = "quest.special_alignment_pioneer.req",
            checkProgress = { it.momentum >= 75 || it.momentum <= 25 },
            rewardGold = 150,
            rewardExp = 200,
            rewardItem = "Scroll of Conviction"
        ),

        // --- EVENT QUESTS ---
        QuestDef(
            id = "event_solar_zenith",
            type = QuestType.EVENT,
            titleKey = "quest.event_solar_zenith.title",
            descKey = "quest.event_solar_zenith.desc",
            requirementKey = "quest.event_solar_zenith.req",
            checkProgress = { it.level >= 6 },
            rewardGold = 120,
            rewardExp = 150,
            rewardAether = 40
        )
    )

    /**
     * Finds a title by its id index.
     */
    fun getTitleDef(id: String): TitleDef? {
        return titles.find { it.id == id }
    }

    /**
     * Analyzes and returns all currently qualified titles for the player.
     */
    fun getEligibleTitles(player: PlayerProfile): List<TitleDef> {
        return titles.filter { it.meetsPreconditions(player) }
    }

    /**
     * Scans and returns quest definitions with statuses mapped for UI list binding.
     */
    fun getQuestProgress(player: PlayerProfile): List<QuestStatus> {
        val completedSet = player.completedQuestsEncoded.split(",")
            .filter { it.isNotBlank() }
            .toSet()

        return quests.map { q ->
            val isCompleted = completedSet.contains(q.id)
            val prerequisiteMet = q.prerequisiteQuestId == null || completedSet.contains(q.prerequisiteQuestId)
            val meetsPrecon = q.meetsPreconditions(player)
            val isUnlocked = prerequisiteMet && meetsPrecon
            val requirementMet = q.checkProgress(player)

            val statusType = when {
                isCompleted -> QuestStatusType.COMPLETED
                !isUnlocked -> if (q.type == QuestType.HIDDEN) QuestStatusType.HIDDEN else QuestStatusType.LOCKED
                else -> QuestStatusType.ACTIVE
            }

            QuestStatus(
                quest = q,
                status = statusType,
                isCompleted = isCompleted,
                isUnlocked = isUnlocked,
                requirementMet = requirementMet && isUnlocked && !isCompleted
            )
        }
    }
}

/**
 * Transient state carrying status metrics for quests.
 */
data class QuestStatus(
    val quest: QuestDef,
    val status: QuestStatusType,
    val isCompleted: Boolean,
    val isUnlocked: Boolean,
    val requirementMet: Boolean
) {
    fun getProgressLabelAndFraction(player: PlayerProfile, lang: String): Pair<String, Float> {
        if (isCompleted) return Pair(LocalizationManager.getString(lang, "ui.progress_completed"), 1f)
        if (!isUnlocked) return Pair(LocalizationManager.getString(lang, "ui.progress_locked"), 0f)
        val q = quest
        return when (q.id) {
            "main_foothold" -> {
                val curr = player.currentFloor
                val target = 5
                val label = LocalizationManager.formatString(lang, "ui.progress_floor_format", curr, target)
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "main_midpoint" -> {
                val curr = player.currentFloor
                val target = 15
                val label = LocalizationManager.formatString(lang, "ui.progress_floor_format", curr, target)
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "side_wealth" -> {
                val curr = player.gold
                val target = 300
                val label = LocalizationManager.formatString(lang, "ui.progress_gold_format", curr, target)
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "side_sanctum_purity" -> {
                if (player.side != "SANCTUM") {
                    Pair(LocalizationManager.getString(lang, "ui.progress_not_sanctum"), 0f)
                } else {
                    val curr = player.momentum
                    val target = 65
                    val label = LocalizationManager.formatString(lang, "ui.progress_momentum_format", curr, target)
                    Pair(label, ((curr - 50).toFloat() / 15f).coerceIn(0f, 1f))
                }
            }
            "side_void_alliance" -> {
                if (player.side != "COVENANT") {
                    Pair(LocalizationManager.getString(lang, "ui.progress_not_covenant"), 0f)
                } else {
                    val curr = (50 - player.momentum).coerceAtLeast(0)
                    val target = 15
                    val label = LocalizationManager.formatString(lang, "ui.progress_void_momentum_format", curr, target)
                    Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
                }
            }
            "chain_ascension_1" -> {
                val curr = player.level
                val target = 4
                val label = LocalizationManager.formatString(lang, "ui.progress_level_format", curr, target)
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "chain_ascension_2" -> {
                val curr = player.level
                val target = 12
                val label = LocalizationManager.formatString(lang, "ui.progress_level_format", curr, target)
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "chain_ascension_3" -> {
                val curr = player.level
                val target = 20
                val label = LocalizationManager.formatString(lang, "ui.progress_level_format", curr, target)
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "hidden_fractured_soul" -> {
                val curr = player.totalFractures
                val target = 4
                val label = LocalizationManager.formatString(lang, "ui.progress_fractures_format", curr, target)
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "hidden_perfect_balance" -> {
                val currFl = player.currentFloor
                val align = player.momentum
                val isBalanced = align == 50
                if (!isUnlocked) {
                    Pair("???", 0f)
                } else if (!isBalanced) {
                    val displayVal = align - 50
                    Pair(LocalizationManager.formatString(lang, "ui.progress_imbalanced", displayVal), 0f)
                } else {
                    val label = LocalizationManager.formatString(lang, "ui.progress_balanced_floor", currFl, 15)
                    Pair(label, (currFl.toFloat() / 15).coerceIn(0f, 1f))
                }
            }
            "floor_1_rat_king" -> {
                val curr = player.currentFloor
                val label = LocalizationManager.formatString(lang, "ui.progress_floor_format", curr, 2)
                Pair(label, (if (curr >= 2) 1f else 0.5f))
            }
            "floor_2_crystal_heart" -> {
                val curr = player.currentFloor
                val label = LocalizationManager.formatString(lang, "ui.progress_floor_format", curr, 3)
                Pair(label, (if (curr >= 3) 1f else (if (curr == 2) 0.5f else 0f)))
            }
            "floor_3_oracle_scourge" -> {
                val curr = player.currentFloor
                val label = LocalizationManager.formatString(lang, "ui.progress_floor_format", curr, 4)
                Pair(label, (if (curr >= 4) 1f else (if (curr == 3) 0.5f else 0f)))
            }
            "normal_climb_3" -> {
                val curr = player.currentFloor
                val target = 3
                val label = LocalizationManager.formatString(lang, "ui.progress_floor_format", curr, target)
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "special_alignment_pioneer" -> {
                val dist = Math.abs(player.momentum - 50)
                val target = 25
                val label = LocalizationManager.formatString(lang, "ui.progress_momentum_diff_format", dist, target)
                Pair(label, (dist.toFloat() / target).coerceIn(0f, 1f))
            }
            "event_solar_zenith" -> {
                val curr = player.level
                val target = 6
                val label = LocalizationManager.formatString(lang, "ui.progress_level_format", curr, target)
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            else -> Pair("", 1f)
        }
    }
}
