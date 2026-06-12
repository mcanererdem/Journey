package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.model.LegacyUpgradeType

/**
 * Branching Choice representation in the Narrative Event Processor.
 */
data class NarrativeBranchOption(
    val id: String,
    val textKey: String,
    val alignmentImpact: Int = 0,
    val goldChange: Int = 0,
    val expReward: Int = 0,
    val aetherChange: Int = 0,
    val hpChange: Int = 0,
    val itemReward: String = "",
    val titleReward: String = "",
    val outcomeKey: String,
    val nextBranchId: String? = null,
    val triggersSecretBossId: String? = null
)

/**
 * Story narrative event with pre-requisite conditions.
 */
data class NarrativeEvent(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val preconditionDescKey: String,
    val checkPreconditions: (PlayerProfile) -> Boolean,
    val options: List<NarrativeBranchOption>
)

/**
 * Secret Boss Encounter with distinct unlock rules and high reward payouts.
 */
data class SecretBossEncounter(
    val id: String,
    val nameKey: String,
    val hp: Int,
    val atk: Int,
    val descriptionKey: String,
    val unlockRequirementKey: String,
    val checkUnlock: (PlayerProfile) -> Boolean,
    val rewardGold: Int,
    val rewardAether: Int,
    val rewardItem: String = ""
)

object NarrativeEventProcessor {

    val events = listOf(
        NarrativeEvent(
            id = "obelisk_whispers",
            titleKey = "narrative.obelisk_whispers.title",
            descriptionKey = "narrative.obelisk_whispers.desc",
            preconditionDescKey = "narrative.obelisk_whispers.req",
            checkPreconditions = { it.level >= 2 && it.momentum in 30..70 },
            options = listOf(
                NarrativeBranchOption(
                    id = "obelisk_opt_light",
                    textKey = "narrative.obelisk_opt_light.text",
                    alignmentImpact = 15,
                    aetherChange = 30,
                    expReward = 50,
                    outcomeKey = "narrative.obelisk_opt_light.outcome"
                ),
                NarrativeBranchOption(
                    id = "obelisk_opt_void",
                    textKey = "narrative.obelisk_opt_void.text",
                    alignmentImpact = -15,
                    aetherChange = 30,
                    hpChange = -5,
                    expReward = 55,
                    outcomeKey = "narrative.obelisk_opt_void.outcome"
                ),
                NarrativeBranchOption(
                    id = "obelisk_opt_scholarly",
                    textKey = "narrative.obelisk_opt_scholarly.text",
                    alignmentImpact = 0,
                    goldChange = 15,
                    expReward = 80,
                    outcomeKey = "narrative.obelisk_opt_scholarly.outcome"
                )
            )
        ),
        NarrativeEvent(
            id = "shadow_bazaar",
            titleKey = "narrative.shadow_bazaar.title",
            descriptionKey = "narrative.shadow_bazaar.desc",
            preconditionDescKey = "narrative.shadow_bazaar.req",
            checkPreconditions = { it.gold >= 150 && it.momentum <= 35 },
            options = listOf(
                NarrativeBranchOption(
                    id = "bazaar_opt_buy",
                    textKey = "narrative.bazaar_opt_buy.text",
                    goldChange = -100,
                    hpChange = -15,
                    itemReward = "Cursed Abyssal Eye",
                    outcomeKey = "narrative.bazaar_opt_buy.outcome"
                ),
                NarrativeBranchOption(
                    id = "bazaar_opt_betray",
                    textKey = "narrative.bazaar_opt_betray.text",
                    alignmentImpact = 20,
                    aetherChange = 40,
                    expReward = 60,
                    outcomeKey = "narrative.bazaar_opt_betray.outcome"
                )
            )
        ),
        NarrativeEvent(
            id = "celestial_solstice",
            titleKey = "narrative.celestial_solstice.title",
            descriptionKey = "narrative.celestial_solstice.desc",
            preconditionDescKey = "narrative.celestial_solstice.req",
            checkPreconditions = { it.momentum >= 80 && it.level >= 3 },
            options = listOf(
                NarrativeBranchOption(
                    id = "sun_opt_blood",
                    textKey = "narrative.sun_opt_blood.text",
                    hpChange = -20,
                    expReward = 120,
                    titleReward = "Sunforged Harbinger",
                    outcomeKey = "narrative.sun_opt_blood.outcome"
                ),
                NarrativeBranchOption(
                    id = "sun_opt_meditate",
                    textKey = "narrative.sun_opt_meditate.text",
                    alignmentImpact = 10,
                    outcomeKey = "narrative.sun_opt_meditate.outcome"
                )
            )
        )
    )

    val secretBosses = listOf(
        SecretBossEncounter(
            id = "abyssal_beast_boss",
            nameKey = "boss.abyssal_beast.name",
            hp = 550,
            atk = 32,
            descriptionKey = "boss.abyssal_beast.desc",
            unlockRequirementKey = "boss.abyssal_beast.req",
            checkUnlock = { it.momentum <= 10 && it.totalFractures >= 2 },
            rewardGold = 250,
            rewardAether = 100,
            rewardItem = "Voidreaver Edge Plate"
        ),
        SecretBossEncounter(
            id = "celestial_avatar_boss",
            nameKey = "boss.celestial_avatar.name",
            hp = 600,
            atk = 28,
            descriptionKey = "boss.celestial_avatar.desc",
            unlockRequirementKey = "boss.celestial_avatar.req",
            checkUnlock = { it.momentum >= 100 && it.level >= 5 },
            rewardGold = 200,
            rewardAether = 100,
            rewardItem = "Sunspire Crest Seal"
        ),
        SecretBossEncounter(
            id = "sentinel_relic_boss",
            nameKey = "boss.sentinel_relic.name",
            hp = 700,
            atk = 24,
            descriptionKey = "boss.sentinel_relic.desc",
            unlockRequirementKey = "boss.sentinel_relic.req",
            checkUnlock = { it.gold >= 250 && it.itemsEncoded.split(",").filter { i -> i.isNotBlank() }.size >= 2 },
            rewardGold = 400,
            rewardAether = 100,
            rewardItem = "Chrono-Core Fragment"
        )
    )

    /**
     * Evaluates active events based on player's current progression profile.
     */
    fun getAvailableEvents(player: PlayerProfile): List<NarrativeEvent> {
        return events.filter { it.checkPreconditions(player) }
    }

    /**
     * Evaluates active secret bosses based on player's current progression profile.
     */
    fun getAvailableSecretBosses(player: PlayerProfile): List<SecretBossEncounter> {
        return secretBosses.filter { it.checkUnlock(player) }
    }

    /**
     * Process selection choice inside a Narrative Branch and award immediate payouts.
     */
    fun processNarrativeChoice(player: PlayerProfile, choice: NarrativeBranchOption): PlayerProfile {
        val greedLvl = LegacyUpgradeType.getUpgradeLevel(player.upgradesEncoded, LegacyUpgradeType.GREED)
        val greedMultiplier = 1.0f + (greedLvl * 0.20f)
        val scaledGoldChange = if (choice.goldChange > 0) (choice.goldChange * greedMultiplier).toInt() else choice.goldChange

        val recoveryLvl = LegacyUpgradeType.getUpgradeLevel(player.upgradesEncoded, LegacyUpgradeType.RECOVERY)
        val recoveryMultiplier = 1.0f + (recoveryLvl * 0.15f)
        val scaledHpChange = if (choice.hpChange > 0) (choice.hpChange * recoveryMultiplier).toInt() else choice.hpChange

        val newMomentum = (player.momentum + choice.alignmentImpact).coerceIn(0, 100)
        val newGold = (player.gold + scaledGoldChange).coerceAtLeast(0)
        val newAether = (player.aether + choice.aetherChange).coerceAtLeast(0)
        
        var newHp = player.currentHp + scaledHpChange
        if (newHp < 1 && scaledHpChange < 0) {
            newHp = 1 // Prevent narrative death from simple choices, leave at critical 1 HP
        }

        // Apply item reward
        var currentItems = if (player.itemsEncoded.isEmpty()) emptyList() else player.itemsEncoded.split(",")
        if (choice.itemReward.isNotEmpty() && !currentItems.contains(choice.itemReward)) {
            currentItems = currentItems + choice.itemReward
        }
        val newItemsEncoded = currentItems.filter { it.isNotBlank() }.joinToString(",")

        // Apply title reward
        var currentTitles = if (player.titlesEncoded.isEmpty()) emptyList() else player.titlesEncoded.split(",")
        if (choice.titleReward.isNotEmpty() && !currentTitles.contains(choice.titleReward)) {
            currentTitles = currentTitles + choice.titleReward
        }
        val newTitlesEncoded = currentTitles.filter { it.isNotBlank() }.joinToString(",")

        // Apply EXP & Leveling up logic
        var newExp = player.exp + choice.expReward
        var newLevel = player.level
        var newMaxExp = player.maxExp
        var newMaxHp = player.maxHp
        while (newExp >= newMaxExp && newLevel < 100) {
            newExp -= newMaxExp
            newLevel++
            newMaxExp = newLevel * 100
            newMaxHp += 20
            newHp += 20
        }

        val activeFactionSide = if (player.side == "NEUTRAL" && choice.alignmentImpact != 0) {
            if (choice.alignmentImpact > 0 && newMomentum > 70) "SANCTUM"
            else if (choice.alignmentImpact < 0 && newMomentum < 30) "COVENANT"
            else "NEUTRAL"
        } else {
            player.side
        }

        return player.copy(
            momentum = newMomentum,
            gold = newGold,
            aether = newAether,
            currentHp = newHp.coerceAtMost(newMaxHp),
            maxHp = newMaxHp,
            itemsEncoded = newItemsEncoded,
            titlesEncoded = newTitlesEncoded,
            level = newLevel,
            exp = newExp,
            maxExp = newMaxExp,
            side = activeFactionSide,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
