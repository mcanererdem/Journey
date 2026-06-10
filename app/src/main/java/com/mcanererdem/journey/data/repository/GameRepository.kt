package com.mcanererdem.journey.data.repository

import com.mcanererdem.journey.data.dao.GameDao
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.PlayerProfile
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
}
