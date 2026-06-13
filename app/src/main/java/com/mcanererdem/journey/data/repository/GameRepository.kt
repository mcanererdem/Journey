package com.mcanererdem.journey.data.repository

import com.mcanererdem.journey.data.dao.GameDao
import com.mcanererdem.journey.data.database.GameDatabase
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.PlayerProfile
import android.content.Context
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    val playerProfile: Flow<PlayerProfile?> = gameDao.getPlayerProfileFlow()
    val journalEntries: Flow<List<JournalEntry>> = gameDao.getJournalEntriesFlow()

    suspend fun getPlayerProfileDirect(): PlayerProfile? {
        return gameDao.getPlayerProfileDirect()
    }

    suspend fun savePlayerProfile(profile: PlayerProfile) {
        gameDao.savePlayerProfile(profile)
    }

    suspend fun insertJournalEntry(entry: JournalEntry) {
        gameDao.insertJournalEntry(entry)
    }

    suspend fun clearJournal() {
        gameDao.clearJournal()
    }

    companion object {
        @Volatile
        private var INSTANCE: GameRepository? = null

        fun getInstance(context: Context): GameRepository {
            return INSTANCE ?: synchronized(this) {
                val database = GameDatabase.getDatabase(context)
                val instance = GameRepository(database.gameDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
