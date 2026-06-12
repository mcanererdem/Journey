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
