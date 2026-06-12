package com.mcanererdem.journey.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mcanererdem.journey.data.model.EnemyIntent
import com.mcanererdem.journey.data.model.CombatStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CombatViewModel : ViewModel() {
    private val _activeEnemyHp = MutableStateFlow<Int?>(null)
    val activeEnemyHp: StateFlow<Int?> = _activeEnemyHp.asStateFlow()

    private val _combatLog = MutableStateFlow<List<String>>(emptyList())
    val combatLog: StateFlow<List<String>> = _combatLog.asStateFlow()

    private val _playerStatuses = MutableStateFlow<List<CombatStatus>>(emptyList())
    val playerStatuses: StateFlow<List<CombatStatus>> = _playerStatuses.asStateFlow()

    private val _enemyStatuses = MutableStateFlow<List<CombatStatus>>(emptyList())
    val enemyStatuses: StateFlow<List<CombatStatus>> = _enemyStatuses.asStateFlow()

    private val _currentEnemyIntent = MutableStateFlow<EnemyIntent>(EnemyIntent.ATTACK)
    val currentEnemyIntent: StateFlow<EnemyIntent> = _currentEnemyIntent.asStateFlow()

    fun updateEnemyHp(hp: Int?) {
        _activeEnemyHp.value = hp
    }

    fun addLog(message: String) {
        _combatLog.value = _combatLog.value + message
    }

    fun clearLogs() {
        _combatLog.value = emptyList()
    }

    fun setPlayerStatuses(statuses: List<CombatStatus>) {
        _playerStatuses.value = statuses
    }

    fun setEnemyStatuses(statuses: List<CombatStatus>) {
        _enemyStatuses.value = statuses
    }

    fun setEnemyIntent(intent: EnemyIntent) {
        _currentEnemyIntent.value = intent
    }
}
