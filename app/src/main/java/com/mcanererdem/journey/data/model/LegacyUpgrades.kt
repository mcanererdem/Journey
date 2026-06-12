package com.mcanererdem.journey.data.model

enum class LegacyUpgradeType(
    val key: String,
    val nameKey: String,
    val descriptionKey: String,
    val baseCost: Int,
    val costMultiplier: Int,
    val maxLevel: Int = 5
) {
    VITALITY(
        key = "VITALITY",
        nameKey = "legacy.VITALITY.name",
        descriptionKey = "legacy.VITALITY.desc",
        baseCost = 5,
        costMultiplier = 5
    ),
    AETHER_FOCUS(
        key = "AETHER_FOCUS",
        nameKey = "legacy.AETHER_FOCUS.name",
        descriptionKey = "legacy.AETHER_FOCUS.desc",
        baseCost = 5,
        costMultiplier = 5
    ),
    FORTITUDE(
        key = "FORTITUDE",
        nameKey = "legacy.FORTITUDE.name",
        descriptionKey = "legacy.FORTITUDE.desc",
        baseCost = 10,
        costMultiplier = 10
    ),
    GREED(
        key = "GREED",
        nameKey = "legacy.GREED.name",
        descriptionKey = "legacy.GREED.desc",
        baseCost = 8,
        costMultiplier = 8
    ),
    RECOVERY(
        key = "RECOVERY",
        nameKey = "legacy.RECOVERY.name",
        descriptionKey = "legacy.RECOVERY.desc",
        baseCost = 6,
        costMultiplier = 6
    );

    fun getCostForLevel(level: Int): Int {
        return baseCost + (level * costMultiplier)
    }

    companion object {
        fun getUpgradeLevel(upgradesEncoded: String, type: LegacyUpgradeType): Int {
            if (upgradesEncoded.isEmpty()) return 0
            val upgradeMap = upgradesEncoded.split(",").associate {
                val parts = it.split("_")
                val key = parts.firstOrNull() ?: ""
                val lvl = parts.getOrNull(1)?.toIntOrNull() ?: 0
                key to lvl
            }
            return upgradeMap[type.key] ?: 0
        }

        fun encodeUpgrades(upgradesMap: Map<String, Int>): String {
            return upgradesMap.map { "${it.key}_${it.value}" }.joinToString(",")
        }

        fun getUpgradesMap(upgradesEncoded: String): Map<String, Int> {
            if (upgradesEncoded.isEmpty()) return emptyMap()
            return upgradesEncoded.split(",").mapNotNull {
                val parts = it.split("_")
                val key = parts.firstOrNull() ?: return@mapNotNull null
                val lvl = parts.getOrNull(1)?.toIntOrNull() ?: 0
                key to lvl
            }.toMap()
        }
    }
}
