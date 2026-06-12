package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.*
import kotlin.random.Random
import java.util.UUID
import org.json.JSONObject
import org.json.JSONArray


object FloorBlueprintSystem {

    fun getBlueprintForFloor(floor: Int, player: PlayerProfile? = null): FloorBlueprint {
        val jsonBlueprint = loadBlueprintFromJson(floor)
        if (jsonBlueprint != null) {
            return jsonBlueprint
        }
        return generateProceduralBlueprint(floor, player)
    }

    private fun generateProceduralBlueprint(floor: Int, player: PlayerProfile?): FloorBlueprint {
        val random = Random(floor.toLong() + (player?.playerName?.hashCode()?.toLong() ?: 0L))
        val nodes = ArrayList<AdventureNode>()
        
        // Simplified procedural generation for the refactor
        for (i in 0 until 10) {
            val type = when (i) {
                0 -> NodeType.NARRATIVE
                9 -> NodeType.BOSS
                else -> if (random.nextBoolean()) NodeType.COMBAT else NodeType.CHEST
            }
            nodes.add(generateProceduralNode(floor, i, type, random, i, 0))
        }

        return FloorBlueprint(
            floor = floor,
            titleKey = "floor.$floor.title",
            descriptionKey = "floor.$floor.description",
            introScenario = buildNormalScenario(floor, (floor / 10) % 10),
            nodes = nodes
        )
    }

    private fun generateProceduralNode(floor: Int, index: Int, type: NodeType, random: Random, depth: Int, column: Int): AdventureNode {
        val id = "floor_${floor}_node_$index"
        return when (type) {
            NodeType.COMBAT -> {
                AdventureNode(
                    id = id,
                    type = type,
                    titleKey = "ui.label_skirmish",
                    descriptionKey = "ui.desc_skirmish",
                    depth = depth,
                    column = column,
                    enemy = EnemyRef("infested_rat") // Placeholder for now
                )
            }
            NodeType.CHEST -> {
                AdventureNode(
                    id = id,
                    type = type,
                    titleKey = "ui.label_chest",
                    descriptionKey = "ui.desc_chest",
                    depth = depth,
                    column = column,
                    choices = listOf(
                        NodeChoice(
                            id = "${id}_a",
                            labelKey = "ui.btn_open",
                            journalKey = "ui.journal_chest_open",
                            effects = ChoiceEffects(goldChange = 30)
                        )
                    )
                )
            }
            else -> {
                AdventureNode(
                    id = id,
                    type = NodeType.NARRATIVE,
                    titleKey = "ui.label_ruins",
                    descriptionKey = "ui.desc_ruins",
                    depth = depth,
                    column = column,
                    choices = listOf(
                        NodeChoice(
                            id = "${id}_a",
                            labelKey = "ui.btn_explore",
                            journalKey = "ui.journal_explore",
                            effects = ChoiceEffects(expChange = 20)
                        )
                    )
                )
            }
        }
    }

    private fun buildNormalScenario(floor: Int, bracketIndex: Int): FloorScenario {
        val themeId = when {
            floor == 100 -> "floor_100"
            floor % 25 == 0 -> "exarch_council"
            floor % 10 == 0 -> "arbiter_threshold"
            else -> "bracket_${bracketIndex + 1}"
        }

        return FloorScenario(
            floor = floor,
            titleKey = "scenarios.$themeId.title",
            descriptionKey = "scenarios.$themeId.description",
            options = listOf(
                GameOption(
                    id = "opt_a",
                    labelKey = "scenarios.$themeId.optA_text",
                    journalKey = "scenarios.$themeId.optA_journal",
                    effects = ChoiceEffects(momentumShift = 5)
                ),
                GameOption(
                    id = "opt_b",
                    labelKey = "scenarios.$themeId.optB_text",
                    journalKey = "scenarios.$themeId.optB_journal",
                    effects = ChoiceEffects(momentumShift = -5)
                ),
                GameOption(
                    id = "opt_c",
                    labelKey = "scenarios.$themeId.optC_text",
                    journalKey = "scenarios.$themeId.optC_journal",
                    effects = ChoiceEffects(goldChange = 20)
                )
            )
        )
    }

    private fun loadBlueprintFromJson(floorNum: Int): FloorBlueprint? {
        try {
            val floorObj = LocalizationManager.loadFloorBlueprint(floorNum) ?: return null
            
            val nodesArr = floorObj.optJSONArray("nodes") ?: return null
            val nodesList = ArrayList<AdventureNode>()
            for (j in 0 until nodesArr.length()) {
                val nodeObj = nodesArr.getJSONObject(j)
                val idx = nodeObj.optInt("index", 0)
                val id = "floor_${floorNum}_node_$idx"
                val typeStr = nodeObj.optString("type", "NARRATIVE")
                val type = try { NodeType.valueOf(typeStr) } catch(e: Exception) { NodeType.NARRATIVE }

                val enemyId = nodeObj.optString("enemyId", "")
                
                val nodeOptAObj = nodeObj.optJSONObject("optionA")
                val nodeOptBObj = nodeObj.optJSONObject("optionB")
                val nodeOptCObj = nodeObj.optJSONObject("optionC")

                val choices = mutableListOf<NodeChoice>()
                if (nodeOptAObj != null) choices.add(parseNodeChoice(nodeOptAObj, "floor.$floorNum.nodes.$idx.choice_a"))
                if (nodeOptBObj != null) choices.add(parseNodeChoice(nodeOptBObj, "floor.$floorNum.nodes.$idx.choice_b"))
                if (nodeOptCObj != null) choices.add(parseNodeChoice(nodeOptCObj, "floor.$floorNum.nodes.$idx.choice_c"))

                nodesList.add(
                    AdventureNode(
                        id = id,
                        type = type,
                        titleKey = nodeObj.optString("titleKey", "floor.$floorNum.nodes.$idx.title"),
                        descriptionKey = nodeObj.optString("descriptionKey", "floor.$floorNum.nodes.$idx.description"),
                        depth = nodeObj.optInt("depth", idx),
                        column = nodeObj.optInt("column", 0),
                        enemy = if (enemyId.isNotEmpty()) EnemyRef(enemyId, type == NodeType.BOSS) else null,
                        choices = choices,
                        willCost = nodeObj.optInt("willCost", 0)
                    )
                )
            }

            val introObj = floorObj.optJSONObject("introScenario") ?: floorObj.optJSONObject("intro") ?: return null
            
            return FloorBlueprint(
                floor = floorNum,
                titleKey = floorObj.optString("titleKey", "floor.$floorNum.title"),
                descriptionKey = floorObj.optString("descriptionKey", "floor.$floorNum.description"),
                introScenario = FloorScenario(
                    floor = floorNum,
                    titleKey = introObj.optString("titleKey", "floor.$floorNum.intro.title"),
                    descriptionKey = introObj.optString("descriptionKey", "floor.$floorNum.intro.description"),
                    options = listOf(
                        parseGameOption(introObj.optJSONObject("optionA") ?: JSONObject(), "floor.$floorNum.intro.choice_a"),
                        parseGameOption(introObj.optJSONObject("optionB") ?: JSONObject(), "floor.$floorNum.intro.choice_b"),
                        parseGameOption(introObj.optJSONObject("optionC") ?: JSONObject(), "floor.$floorNum.intro.choice_c")
                    )
                ),
                nodes = nodesList
            )
        } catch (e: Exception) {
            android.util.Log.e("FloorBlueprintSystem", "Error parsing floor JSON blueprint $floorNum", e)
        }
        return null
    }

    private fun parseGameOption(obj: JSONObject, defaultKey: String = ""): GameOption {
        val labelKey = when {
            obj.has("textKey") -> obj.getString("textKey")
            obj.has("labelKey") -> obj.getString("labelKey")
            else -> if (defaultKey.isNotEmpty()) "$defaultKey.text" else ""
        }
        val effectsObj = obj.optJSONObject("effects") ?: obj
        return GameOption(
            id = obj.optString("id", UUID.randomUUID().toString()),
            labelKey = labelKey,
            journalKey = obj.optString("journalKey", if (defaultKey.isNotEmpty()) "$defaultKey.journal" else ""),
            effects = parseChoiceEffects(effectsObj)
        )
    }

    private fun parseNodeChoice(obj: JSONObject, defaultKey: String = ""): NodeChoice {
        val labelKey = when {
            obj.has("textKey") -> obj.getString("textKey")
            obj.has("labelKey") -> obj.getString("labelKey")
            else -> if (defaultKey.isNotEmpty()) "$defaultKey.text" else ""
        }
        val journalKey = obj.optString("journalKey", if (defaultKey.isNotEmpty()) "$defaultKey.journal" else "")
        val effectsObj = obj.optJSONObject("effects") ?: obj
        
        return NodeChoice(
            id = obj.optString("id", UUID.randomUUID().toString()),
            labelKey = labelKey,
            journalKey = journalKey,
            effects = parseChoiceEffects(effectsObj),
            weight = parseChoiceWeight(obj.optString("weight", "")),
            isHidden = obj.optBoolean("isHidden", false),
            isIrreversible = obj.optBoolean("isIrreversible", false),
            nextChainNodeId = obj.optString("nextChainNodeId").ifBlank { null }
        )
    }

    private fun parseChoiceEffects(obj: JSONObject): ChoiceEffects {
        return ChoiceEffects(
            hpChange = obj.optInt("hpChange", 0),
            goldChange = obj.optInt("goldChange", 0),
            aetherChange = obj.optInt("aetherChange", 0),
            expChange = obj.optInt("expChange", 0),
            momentumShift = obj.optInt("momentumShift", obj.optInt("alignmentShift", 0)),
            willChange = obj.optInt("willChange", 0),
            rewardItemId = obj.optString("rewardItemId", obj.optString("rewardItem", "")),
            rewardTitleId = obj.optString("rewardTitleId", obj.optString("rewardTitle", "")),
            requiredFlag = obj.optString("requiredFlag", obj.optString("requiredStoryFlag", "")),
            setsFlag = obj.optString("setsFlag", obj.optString("addStoryFlag", "")),
            removesFlag = obj.optString("removesFlag", ""),
            consequenceRing = obj.optInt("consequenceRing", 0),
            consequenceKey = obj.optString("consequenceKey", ""),
            triggerChainId = obj.optString("triggerChainId", ""),
            skipToBoss = obj.optBoolean("skipToBoss", false),
            skipToNextFloor = obj.optBoolean("skipToNextFloor", false)
        )
    }

    private fun parseChoiceWeight(value: String): ChoiceWeight {
        return try {
            if (value.isBlank()) ChoiceWeight.MINOR else ChoiceWeight.valueOf(value.uppercase())
        } catch (_: IllegalArgumentException) {
            ChoiceWeight.MINOR
        }
    }
}
