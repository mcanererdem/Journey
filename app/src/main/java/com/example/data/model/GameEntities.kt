package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EnemyFaction {
    SANCTUM_WRATH,
    VOID_CORRUPTION,
    BLIGHTED_AMALGAM;

    companion object {
        fun fromName(nameEn: String): EnemyFaction {
            val nameLower = nameEn.lowercase()
            return when {
                nameLower.contains("celestial") || nameLower.contains("arbiter") || nameLower.contains("auriel") || 
                nameLower.contains("angel") || nameLower.contains("purifier") || nameLower.contains("templar") || 
                nameLower.contains("order") || nameLower.contains("sentinel") || nameLower.contains("guardian") ||
                nameLower.contains("centurion") || nameLower.contains("paladin") -> SANCTUM_WRATH
                
                nameLower.contains("void") || nameLower.contains("abyss") || nameLower.contains("shadow") || 
                nameLower.contains("stalker") || nameLower.contains("ghoul") || nameLower.contains("necromancer") || 
                nameLower.contains("terror") || nameLower.contains("sorrow") || nameLower.contains("spectre") || 
                nameLower.contains("wraith") || nameLower.contains("lord") || nameLower.contains("overlord") ||
                nameLower.contains("blight") || nameLower.contains("covenant") || nameLower.contains("reaper") -> VOID_CORRUPTION
                
                else -> BLIGHTED_AMALGAM
            }
        }
    }
}

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1, // Single active player slot
    val playerName: String = "Seraphine",
    val side: String = "NEUTRAL", // "SANCTUM" (Celestial Sanctum), "COVENANT" (Void Covenant), "NEUTRAL"
    val momentum: Int = 50, // 0 to 100 indicator (0 void / monstrous, 100 light / saintly)
    val currentFloor: Int = 1, // 1 to 100 floors
    val currentHp: Int = 100,
    val maxHp: Int = 100,
    val gold: Int = 120,
    val aether: Int = 0, // Unified currency
    val rank: String = "EMISSARY", // "EMISSARY", "ARBITER", "EXARCH", "SOVEREIGN"
    val chosenClass: String = "Initiate", // Determined by alignment and side
    val totalFractures: Int = 0, // Count of spirit fractures
    val savedFloorCheckpoint: Int = 1, // Last checkpoint saved
    val level: Int = 1,
    val exp: Int = 0,
    val maxExp: Int = 100,
    val currentWill: Int = 10,
    val maxWill: Int = 10,
    val itemsEncoded: String = "",
    val titlesEncoded: String = "",
    val equippedTitle: String = "", // Currently active title equipped by user
    val completedQuestsEncoded: String = "", // Comma separated quest IDs that are finished
    val currentNodeIndex: Int = 0,
    val currentNodeColumn: Int = 0,
    val currentNodeCompleted: Boolean = false,
    val storyFlagsEncoded: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_entry")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val floor: Int,
    val actionTakenEs: String, // English action description
    val actionTakenTr: String, // Turkish action description
    val sideAlignmentShift: String, // SANCTUM / COVENANT / NEUTRAL
    val alignmentImpact: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val nodeIndex: Int = -1
)
