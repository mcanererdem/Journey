package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1, // Single active player slot
    val playerName: String = "Seraphine",
    val side: String = "NEUTRAL", // "SANCTUM" (Celestial Sanctum), "COVENANT" (Void Covenant), "NEUTRAL"
    val alignment: Int = 0, // -100 to +100 indicator (-100 void / monstrous, +100 light / saintly)
    val currentFloor: Int = 1, // 1 to 100 floors
    val currentHp: Int = 100,
    val maxHp: Int = 100,
    val gold: Int = 120,
    val gleam: Int = 0, // Celestial Sanctum currency
    val pyre: Int = 0, // Void Covenant currency
    val rank: String = "EMISSARY", // "EMISSARY", "ARBITER", "EXARCH", "SOVEREIGN"
    val chosenClass: String = "Initiate", // Determined by alignment and side
    val totalFractures: Int = 0, // Count of spirit fractures
    val savedFloorCheckpoint: Int = 1, // Last checkpoint saved
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
    val timestamp: Long = System.currentTimeMillis()
)
