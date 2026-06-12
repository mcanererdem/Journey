package com.mcanererdem.journey.data.model

enum class LegacyUpgradeType(
    val key: String,
    val nameEn: String,
    val nameTr: String,
    val descriptionEn: String,
    val descriptionTr: String,
    val baseCost: Int,
    val costMultiplier: Int,
    val maxLevel: Int = 5
) {
    VITALITY(
        key = "VITALITY",
        nameEn = "Celestial Vitality",
        nameTr = "Semavi Hayatiyet",
        descriptionEn = "Increase starting HP by +10 per level.",
        descriptionTr = "Başlangıç HP limitini seviye başına +10 artırır.",
        baseCost = 5,
        costMultiplier = 5
    ),
    AETHER_FOCUS(
        key = "AETHER_FOCUS",
        nameEn = "Aetheric Focus",
        nameTr = "Eterik Odak",
        descriptionEn = "Increase starting Aether by +15 per level.",
        descriptionTr = "Başlangıç Eter değerini seviye başına +15 artırır.",
        baseCost = 5,
        costMultiplier = 5
    ),
    FORTITUDE(
        key = "FORTITUDE",
        nameEn = "Willpower Fortitude",
        nameTr = "İrade Direnci",
        descriptionEn = "Increase maximum Willpower by +1 per level.",
        descriptionTr = "Maksimum İrade sınırını seviye başına +1 artırır.",
        baseCost = 10,
        costMultiplier = 10
    ),
    GREED(
        key = "GREED",
        nameEn = "Sovereign's Greed",
        nameTr = "Hükümdar Açgözlülüğü",
        descriptionEn = "Increase gold rewards by +20% per level.",
        descriptionTr = "Kazanılan altın miktarını seviye başına %20 artırır.",
        baseCost = 8,
        costMultiplier = 8
    ),
    RECOVERY(
        key = "RECOVERY",
        nameEn = "Soul Recovery",
        nameTr = "Ruhsal Yenilenme",
        descriptionEn = "Increase HP recovery from Camp and items by +15% per level.",
        descriptionTr = "Kamp ve eşyalardan gelen HP yenilenmesini seviye başına %15 artırır.",
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
