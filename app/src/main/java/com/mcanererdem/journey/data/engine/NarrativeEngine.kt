package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.*

object NarrativeEngine {

    fun getScenarioForFloor(floor: Int): FloorScenario {
        // This will eventually be fully key-based and procedural or loaded from JSON
        return FloorScenario(
            floor = floor,
            titleKey = "floor.$floor.scenario.title",
            descriptionKey = "floor.$floor.scenario.description",
            options = listOf(
                GameOption(
                    id = "opt_a",
                    labelKey = "floor.$floor.scenario.opt_a",
                    journalKey = "floor.$floor.scenario.opt_a.journal",
                    effects = ChoiceEffects(momentumShift = 5)
                ),
                GameOption(
                    id = "opt_b",
                    labelKey = "floor.$floor.scenario.opt_b",
                    journalKey = "floor.$floor.scenario.opt_b.journal",
                    effects = ChoiceEffects(momentumShift = -5)
                )
            )
        )
    }
}
