package com.mcanererdem.journey.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.PlayerProfile
import com.mcanererdem.journey.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: GameRepository) : ViewModel() {

    val playerProfile: StateFlow<PlayerProfile?> = repository.playerProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val journalEntries: StateFlow<List<JournalEntry>> = repository.journalEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveProfile(profile: PlayerProfile) {
        viewModelScope.launch {
            repository.savePlayerProfile(profile)
        }
    }

    fun insertJournal(entry: JournalEntry) {
        viewModelScope.launch {
            repository.insertJournalEntry(entry)
        }
    }

    fun clearJournal() {
        viewModelScope.launch {
            repository.clearJournal()
        }
    }
}
