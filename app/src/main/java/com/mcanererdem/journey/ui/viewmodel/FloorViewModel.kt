package com.mcanererdem.journey.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mcanererdem.journey.data.model.AdventureNode
import com.mcanererdem.journey.data.model.FloorScenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FloorViewModel : ViewModel() {
    private val _currentScenario = MutableStateFlow<FloorScenario?>(null)
    val currentScenario: StateFlow<FloorScenario?> = _currentScenario.asStateFlow()

    private val _currentFloorNodes = MutableStateFlow<List<AdventureNode>>(emptyList())
    val currentFloorNodes: StateFlow<List<AdventureNode>> = _currentFloorNodes.asStateFlow()

    private val _scoutedNodeIndices = MutableStateFlow<Set<Int>>(emptySet())
    val scoutedNodeIndices: StateFlow<Set<Int>> = _scoutedNodeIndices.asStateFlow()

    fun updateScenario(scenario: FloorScenario?) {
        _currentScenario.value = scenario
    }

    fun updateNodes(nodes: List<AdventureNode>) {
        _currentFloorNodes.value = nodes
    }

    fun setScoutedNodes(indices: Set<Int>) {
        _scoutedNodeIndices.value = indices
    }
}
