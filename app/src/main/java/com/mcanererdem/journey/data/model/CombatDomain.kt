package com.mcanererdem.journey.data.model

enum class StatusType {
    POISONED,
    STUNNED,
    BLESSED,
    SHIELDED
}

data class CombatStatus(
    val type: StatusType,
    var durationTurns: Int
)

data class SkillDef(
    val id: String,
    val nameKey: String,
    val descriptionKey: String,
    val cost: Int,         // Aether cost
    val damage: Int,
    val effect: StatusType? = null,
    val effectDuration: Int = 0
)

data class CombatLogEntry(
    val key: String,
    val args: Map<String, String> = emptyMap()
)

enum class EnemyIntent {
    ATTACK,
    DEFEND,
    DEBUFF,
    BUFF;

    companion object {
        fun random(random: kotlin.random.Random = kotlin.random.Random): EnemyIntent {
            val values = values()
            return values[random.nextInt(values.size)]
        }
    }
}
