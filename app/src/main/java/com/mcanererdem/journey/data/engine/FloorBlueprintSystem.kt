package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.*
import kotlin.random.Random
import java.util.UUID
import org.json.JSONObject
import org.json.JSONArray
import android.util.Log

object FloorBlueprintSystem {

    private const val TAG = "FloorBlueprintSystem"

    fun getBlueprintForFloor(floor: Int, player: PlayerProfile? = null): FloorBlueprint {
        return loadBlueprintFromJson(floor)
            ?: generateProceduralBlueprint(floor, player)
    }

    private fun loadBlueprintFromJson(floorNum: Int): FloorBlueprint? {
        val json = LocalizationManager.loadFloorBlueprint(floorNum) ?: return null
        return try {
            parseFloorBlueprint(json, floorNum)
        } catch (e: Exception) {
            Log.e(TAG, "Parse error floor $floorNum", e)
            null
        }
    }

    private fun parseFloorBlueprint(json: JSONObject, floor: Int): FloorBlueprint {
        val intro = parseNode(json.getJSONObject("intro"), floor)
        val pathLight = parseNodeList(json.optJSONArray("path_light"), floor)
        val pathDark  = parseNodeList(json.optJSONArray("path_dark"), floor)
        val shared    = parseNodeList(json.optJSONArray("shared"), floor)
        val chains    = parseChainList(json.optJSONArray("chains"), floor)
        val boss      = json.optJSONObject("boss")?.let { parseEnemyRef(it) }

        return FloorBlueprint(
            floor = floor,
            region = json.optString("region", "unknown"),
            type = try { FloorType.valueOf(json.optString("type", "NORMAL")) } catch(e: Exception) { FloorType.NORMAL },
            titleKey = json.optString("titleKey", "floor.$floor.title"),
            descriptionKey = json.optString("descriptionKey", "floor.$floor.description"),
            minSecondsOnFloor = json.optInt("minSecondsOnFloor", 0),
            intro = intro,
            pathLight = pathLight,
            pathDark = pathDark,
            shared = shared,
            chains = chains,
            boss = boss
        )
    }

    private fun parseNode(obj: JSONObject, floor: Int): FloorNode {
        val idx = obj.optInt("index", -1)
        val typeStr = obj.optString("type", "NARRATIVE")
        val type = try { NodeType.valueOf(typeStr) } catch(e: Exception) { NodeType.NARRATIVE }
        
        return AdventureNode(
            id = obj.optString("id", UUID.randomUUID().toString()),
            type = type,
            titleKey = obj.optString("titleKey", if (idx != -1) "floor.$floor.nodes.$idx.title" else ""),
            descriptionKey = obj.optString("descriptionKey", if (idx != -1) "floor.$floor.nodes.$idx.description" else ""),
            depth = obj.optInt("depth", idx),
            column = obj.optInt("column", 0),
            enemy = obj.optJSONObject("enemy")?.let { parseEnemyRef(it) } 
                   ?: obj.optString("enemyId").let { if (it.isNotEmpty()) EnemyRef(it, type == NodeType.BOSS) else null },
            choices = parseChoiceList(obj.optJSONArray("choices")),
            willCost = obj.optInt("willCost", 0),
            path = try { NodePath.valueOf(obj.optString("path", "SHARED")) } catch(e: Exception) { NodePath.SHARED },
            chainId = obj.optString("chainId").ifBlank { null },
            chainNext = obj.optString("chainNext").ifBlank { null },
            chainExit = obj.optBoolean("chainExit", false),
            prereq = obj.optJSONObject("prereq")?.let { parseNodePrereq(it) },
            merchantRef = obj.optJSONObject("merchantRef")?.let { parseMerchantRef(it) },
            campRef = obj.optJSONObject("campRef")?.let { parseCampRef(it) },
            secretCondition = obj.optJSONObject("secretCondition")?.let { parseSecretCondition(it) }
        )
    }

    private fun parseNodeList(arr: JSONArray?, floor: Int): List<FloorNode> {
        if (arr == null) return emptyList()
        val list = mutableListOf<FloorNode>()
        for (i in 0 until arr.length()) {
            list.add(parseNode(arr.getJSONObject(i), floor))
        }
        return list
    }

    private fun parseChoiceList(arr: JSONArray?): List<NodeChoice> {
        if (arr == null) return emptyList()
        val list = mutableListOf<NodeChoice>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(NodeChoice(
                id = obj.optString("id", UUID.randomUUID().toString()),
                labelKey = obj.optString("labelKey"),
                journalKey = obj.optString("journalKey"),
                effects = parseChoiceEffects(obj.getJSONObject("effects")),
                prereq = obj.optJSONObject("prereq")?.let { parseChoicePrereq(it) },
                isHidden = obj.optBoolean("isHidden", false),
                isIrreversible = obj.optBoolean("isIrreversible", false),
                weight = try { ChoiceWeight.valueOf(obj.optString("weight", "MINOR")) } catch(e: Exception) { ChoiceWeight.MINOR },
                nextChainNodeId = obj.optString("nextChainNodeId").ifBlank { null }
            ))
        }
        return list
    }

    private fun parseChainList(arr: JSONArray?, floor: Int): List<NodeChain> {
        if (arr == null) return emptyList()
        val list = mutableListOf<NodeChain>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(NodeChain(
                chainId = obj.getString("chainId"),
                nodes = parseNodeList(obj.getJSONArray("nodes"), floor),
                exitToPath = obj.optBoolean("exitToPath", true)
            ))
        }
        return list
    }

    private fun parseEnemyRef(obj: JSONObject): EnemyRef {
        return EnemyRef(
            enemyId = obj.getString("enemyId"),
            isBoss = obj.optBoolean("isBoss", false),
            form = try { EnemyForm.valueOf(obj.optString("form", "NEUTRAL")) } catch(e: Exception) { EnemyForm.NEUTRAL },
            scaleFactor = obj.optDouble("scaleFactor", 1.0).toFloat(),
            overrideHp = if (obj.has("overrideHp")) obj.getInt("overrideHp") else null,
            overrideAtk = if (obj.has("overrideAtk")) obj.getInt("overrideAtk") else null
        )
    }

    private fun parseMerchantRef(obj: JSONObject): MerchantRef {
        val stockArr = obj.optJSONArray("stock")
        val stockList = mutableListOf<MerchantStockEntry>()
        if (stockArr != null) {
            for (i in 0 until stockArr.length()) {
                val s = stockArr.getJSONObject(i)
                stockList.add(MerchantStockEntry(
                    itemId = s.getString("itemId"),
                    baseCost = s.getInt("baseCost"),
                    currency = s.optString("currency", "GOLD"),
                    minMomentum = if (s.has("minMomentum")) s.getInt("minMomentum") else null,
                    maxMomentum = if (s.has("maxMomentum")) s.getInt("maxMomentum") else null,
                    requiredTitleId = s.optString("requiredTitleId"),
                    requiredItemId = s.optString("requiredItemId"),
                    discountPercent = s.optInt("discountPercent", 0),
                    premiumPercent = s.optInt("premiumPercent", 0)
                ))
            }
        }
        return MerchantRef(obj.getString("merchantId"), stockList)
    }

    private fun parseCampRef(obj: JSONObject): CampRef {
        return CampRef(
            campId = obj.getString("campId"),
            freeHealAmount = obj.optInt("freeHealAmount", 20),
            paidHealAmount = obj.optInt("paidHealAmount", 40),
            paidHealCost = obj.optInt("paidHealCost", 30),
            willRestoreAmount = obj.optInt("willRestoreAmount", 2),
            hasMiniMerchant = obj.optBoolean("hasMiniMerchant", false),
            miniMerchantId = obj.optString("miniMerchantId")
        )
    }

    private fun parseSecretCondition(obj: JSONObject): SecretCondition {
        return SecretCondition(
            type = SecretConditionType.valueOf(obj.getString("type")),
            value = obj.optString("value"),
            minValue = obj.optInt("minValue", 0),
            successNodeId = obj.optString("successNodeId"),
            failNodeId = obj.optString("failNodeId")
        )
    }

    private fun parseChoiceEffects(obj: JSONObject): ChoiceEffects {
        return ChoiceEffects(
            hpChange = obj.optInt("hpChange", 0),
            goldChange = obj.optInt("goldChange", 0),
            aetherChange = obj.optInt("aetherChange", 0),
            expChange = obj.optInt("expChange", 0),
            momentumShift = obj.optInt("momentumShift", 0),
            willChange = obj.optInt("willChange", 0),
            rewardItemId = obj.optString("rewardItemId"),
            rewardTitleId = obj.optString("rewardTitleId"),
            requiredFlag = obj.optString("requiredFlag"),
            setsFlag = obj.optString("setsFlag"),
            removesFlag = obj.optString("removesFlag"),
            consequenceRing = obj.optInt("consequenceRing", 0),
            consequenceKey = obj.optString("consequenceKey"),
            triggerChainId = obj.optString("triggerChainId"),
            skipToBoss = obj.optBoolean("skipToBoss", false),
            skipToNextFloor = obj.optBoolean("skipToNextFloor", false)
        )
    }

    private fun parseNodePrereq(obj: JSONObject): NodePrereq {
        return NodePrereq(
            requiredPath = obj.optString("requiredPath").let { if (it.isNotEmpty()) NodePath.valueOf(it) else null },
            minMomentum = if (obj.has("minMomentum")) obj.getInt("minMomentum") else null,
            maxMomentum = if (obj.has("maxMomentum")) obj.getInt("maxMomentum") else null,
            minLevel = if (obj.has("minLevel")) obj.getInt("minLevel") else null,
            requiredTitleId = obj.optString("requiredTitleId"),
            requiredItemId = obj.optString("requiredItemId"),
            requiredFlag = obj.optString("requiredFlag"),
            excludesFlag = obj.optString("excludesFlag")
        )
    }

    private fun parseChoicePrereq(obj: JSONObject): ChoicePrereq {
        return ChoicePrereq(
            minMomentum = if (obj.has("minMomentum")) obj.getInt("minMomentum") else null,
            maxMomentum = if (obj.has("maxMomentum")) obj.getInt("maxMomentum") else null,
            minLevel = if (obj.has("minLevel")) obj.getInt("minLevel") else null,
            minHp = if (obj.has("minHp")) obj.getInt("minHp") else null,
            requiredTitleId = obj.optString("requiredTitleId"),
            requiredItemId = obj.optString("requiredItemId"),
            requiredFlag = obj.optString("requiredFlag"),
            excludesFlag = obj.optString("excludesFlag")
        )
    }

    private fun generateProceduralBlueprint(floor: Int, player: PlayerProfile?): FloorBlueprint {
        val random = Random(floor.toLong() + (player?.playerName?.hashCode()?.toLong() ?: 0L))
        val shared = ArrayList<FloorNode>()
        
        for (i in 0 until 10) {
            val type = when (i) {
                0 -> NodeType.NARRATIVE
                9 -> NodeType.BOSS
                else -> if (random.nextBoolean()) NodeType.COMBAT else NodeType.CHEST
            }
            shared.add(generateProceduralNode(floor, i, type, random, i, 0))
        }

        return FloorBlueprint(
            floor = floor,
            region = "procedural_depths",
            type = FloorType.NORMAL,
            titleKey = "floor.$floor.title",
            descriptionKey = "floor.$floor.description",
            intro = generateProceduralNode(floor, -1, NodeType.NARRATIVE, random, -1, 0),
            shared = shared,
            boss = EnemyRef("infested_rat", isBoss = true)
        )
    }

    private fun generateProceduralNode(floor: Int, index: Int, type: NodeType, random: Random, depth: Int, column: Int): FloorNode {
        val id = "floor_${floor}_node_$index"
        return AdventureNode(
            id = id,
            type = type,
            titleKey = "ui.label_skirmish",
            descriptionKey = "ui.desc_skirmish",
            depth = depth,
            column = column,
            enemy = if (type == NodeType.COMBAT || type == NodeType.BOSS) EnemyRef("infested_rat") else null,
            choices = if (type == NodeType.CHEST) listOf(
                NodeChoice(
                    id = "${id}_a",
                    labelKey = "ui.btn_open",
                    journalKey = "ui.journal_chest_open",
                    effects = ChoiceEffects(goldChange = 30)
                )
            ) else if (type == NodeType.NARRATIVE) listOf(
                NodeChoice(
                    id = "${id}_a",
                    labelKey = "ui.btn_explore",
                    journalKey = "ui.journal_explore",
                    effects = ChoiceEffects(expChange = 20)
                )
            ) else emptyList()
        )
    }
}
