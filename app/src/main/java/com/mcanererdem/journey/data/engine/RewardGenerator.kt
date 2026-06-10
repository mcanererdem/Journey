package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.PlayerProfile
import kotlin.random.Random

/**
 * Data structure storing the generated combat reward output.
 */
data class RewardResult(
    val expGained: Int,
    val goldGained: Int,
    val itemAwarded: String?,
    val titleAwarded: String?,
    val didLevelUp: Boolean,
    val newLevel: Int,
    val newMaxHp: Int,
    val newExp: Int,
    val newMaxExp: Int,
    val finalHp: Int
)

/**
 * High-fidelity reward generation system for combat and boss completions.
 */
object RewardGenerator {

    /**
     * Main dispatch method to compute rewards for winning battles.
     * Generates standard or epic item/title loot drops dependent on floor tier and node type.
     */
    fun generateRewards(
        player: PlayerProfile,
        isBoss: Boolean,
        random: Random = Random
    ): RewardResult {
        // 1. Calculate EXP & Gold Based on Floor Scaling
        val baseExp = if (isBoss) 100 else 25
        val floorExpBonus = player.currentFloor * if (isBoss) 6 else 3
        val expGained = baseExp + floorExpBonus + random.nextInt(if (isBoss) 20 else 8)

        val baseGold = if (isBoss) 80 else 15
        val floorGoldBonus = player.currentFloor * if (isBoss) 3 else 1
        val goldGained = baseGold + floorGoldBonus + random.nextInt(if (isBoss) 30 else 10)

        // 2. Determine Item Drop
        var itemAwarded: String? = null
        val itemRoll = random.nextInt(100)
        // 40% chance of random item drop on normal combat, 100% on Boss combat
        if (isBoss || itemRoll < 40) {
            itemAwarded = getLootDrop(player.currentFloor, isBoss, random)
        }

        // 3. Determine Title Drop
        var titleAwarded: String? = null
        val titleRoll = random.nextInt(100)
        // 20% on normal combat, 75% on Boss combat
        if ((isBoss && titleRoll < 75) || (!isBoss && titleRoll < 20)) {
            titleAwarded = getUniqueTitle(player, isBoss, random)
        }

        // Prevent duplicate titles & items in simple display
        val currentItems = player.itemsEncoded.split(",").map { it.trim() }
        if (itemAwarded != null && currentItems.contains(itemAwarded)) {
            // Give fallback shiny pendant, gold or scrap if already exists
            itemAwarded = if (isBoss) "Pristine Radiant Pyrite" else "Polished Gleam Nugget"
        }

        val currentTitles = player.titlesEncoded.split(",").map { it.trim() }
        if (titleAwarded != null && currentTitles.contains(titleAwarded)) {
            titleAwarded = "Ascendant Warden VII"
        }

        // 4. Calculate Level Up Progression & Growth Scaling
        var tempExp = player.exp + expGained
        var tempLevel = player.level
        var tempMaxExp = player.maxExp
        var tempMaxHp = player.maxHp
        var tempCurrentHp = player.currentHp
        var didLevelUp = false

        while (tempExp >= tempMaxExp && tempLevel < 100) {
            tempExp -= tempMaxExp
            tempLevel++
            // Scaling next-level threshold dynamically
            tempMaxExp = tempLevel * 100
            // Grant +20 Max HP per level
            tempMaxHp += 20
            // Fully heal the difference on level up
            tempCurrentHp += 20
            didLevelUp = true
        }

        return RewardResult(
            expGained = expGained,
            goldGained = goldGained,
            itemAwarded = itemAwarded,
            titleAwarded = titleAwarded,
            didLevelUp = didLevelUp,
            newLevel = tempLevel,
            newMaxHp = tempMaxHp,
            newExp = tempExp,
            newMaxExp = tempMaxExp,
            finalHp = tempCurrentHp.coerceAtMost(tempMaxHp)
        )
    }

    /**
     * Returns a random item drop appropriate for the floor level tier and fight severity.
     */
    private fun getLootDrop(floor: Int, isBoss: Boolean, random: Random): String {
        return if (isBoss) {
            // Legendary/Epic loot drops
            when {
                floor <= 30 -> listOf(
                    "Grand Archon Crest", "Ironclad Ward-Plate", "Vesper's Razor Edge", 
                    "Sunder-Blade of the Vale", "Heartspire Brooch of Wisdom"
                )
                floor <= 70 -> listOf(
                    "Celestial Dawnbreaker", "Demonic Abyss Carver", "Tome of Bleeding Runes", 
                    "Spirelord's Gilded Signet", "Aetherweave Cloak of Sanctum"
                )
                else -> listOf(
                    "Sovereign Spire Crown", "Eldritch Void-Fanged Blade", "Infinite Cosmos Pentacle", 
                    "Radiant Bulwark of Eternity", "Doombringer's Catalyst Stone"
                )
            }.random(random)
        } else {
            // General loot drops
            when {
                floor <= 30 -> listOf(
                    "Rustblade Dagger", "Sporeplated Helm", "Scout's Sigil", "Iron Girdle", "Aether Coin Pendant"
                )
                floor <= 70 -> listOf(
                    "Blightslayer Sabret", "Vanguard Aegis Shield", "Sanctified Greatsword", "Voidweave Tunic", "Aether Signet Ring"
                )
                else -> listOf(
                    "Sovereign Pentacle Sceptre", "Cosmic Will Band", "Blight Purifier Plate", "Archon Void Fang", "Celestial Halo Crown"
                )
            }.random(random)
        }
    }

    /**
     * Yields a custom, high-flavor title for the character sheet.
     * Considers the player's faction ("SANCTUM", "COVENANT", "NEUTRAL") and player momentum indicators.
     */
    private fun getUniqueTitle(player: PlayerProfile, isBoss: Boolean, random: Random): String {
        val momentum = player.momentum
        val isSanctum = player.side == "SANCTUM"
        val isCovenant = player.side == "COVENANT"

        return if (isBoss) {
            when {
                isSanctum && momentum > 90 -> listOf("Saint of the Golden Spire", "Divine Arbiter of Light", "Exarch of Saintly Vigil")
                isCovenant && momentum < 10 -> listOf("Demon Emperor of the Abyss", "Malefic Scourge of Heavens", "Void Ascendant Sovereign")
                momentum > 75 -> listOf("Paragon of Cleansing Flame", "Warden of Untainted Dawn", "Radiant Spire Vanguard")
                momentum < 25 -> listOf("Eldritch Plague Carrier", "Harbinger of Nightfall", "Shattered Reaper")
                else -> listOf("Apex Spire Conqueror", "Blight's Ultimate Bane", "Keeper of the Cosmic Scales")
            }.random(random)
        } else {
            when {
                isSanctum -> listOf("Zealous Lightseeker", "Sanctified Shieldbearer", "Initiate of Grace")
                isCovenant -> listOf("Void Disciple", "Midnight Executioner", "Acolyte of Shadow")
                momentum > 80 -> listOf("Serene Pathfinder", "Aetherweaver", "Benevolent Sage")
                momentum < 20 -> listOf("Fierce Hellion", "Aether-Touched Outcast", "Blight Stalker")
                else -> listOf("Blight Survivor", "Iron Ascent Scout", "Spire Champion Apprentice")
            }.random(random)
        }
    }
}

