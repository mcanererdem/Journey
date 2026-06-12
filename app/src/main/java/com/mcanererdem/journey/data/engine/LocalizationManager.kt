package com.mcanererdem.journey.data.engine

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.InputStream
import java.nio.charset.Charset

object LocalizationManager {
    private const val TAG = "LocalizationManager"
    private var enJson: JSONObject? = null
    private var trJson: JSONObject? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        if (enJson == null) {
            enJson = loadJsonFromAsset(context, "locales/en.json")
        }
        if (trJson == null) {
            trJson = loadJsonFromAsset(context, "locales/tr.json")
        }
    }

    fun loadFloorBlueprint(floor: Int): JSONObject? {
        val ctx = appContext ?: return null
        return loadJsonFromAsset(ctx, "blueprints/floor_$floor.json")
    }

    fun loadGlobalEnemies(): JSONObject? {
        val ctx = appContext ?: return null
        return loadJsonFromAsset(ctx, "blueprints/global_enemies.json")
    }

    fun loadGlobalItems(): JSONObject? {
        val ctx = appContext ?: return null
        return loadJsonFromAsset(ctx, "blueprints/global_items.json")
    }

    private fun loadJsonFromAsset(context: Context, fileName: String): JSONObject? {
        return try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonStr = String(buffer, Charset.forName("UTF-8"))
            JSONObject(jsonStr)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading locale file: $fileName", e)
            null
        }
    }

    fun getString(lang: String, key: String): String {
        val root = if (lang.lowercase() == "tr") trJson else enJson
        return try {
            root?.getJSONObject("ui")?.optString(key, null)
                ?: enJson?.getJSONObject("ui")?.optString(key, key)
                ?: key
        } catch (e: Exception) {
            key
        }
    }

    fun formatString(lang: String, key: String, vararg args: Any): String {
        val template = getString(lang, key)
        return try {
            String.format(template, *args)
        } catch (e: Exception) {
            template
        }
    }

    // Dynamic floor scenario builder directly from JSON
    fun getScenarioForFloor(lang: String, floor: Int): FloorScenario {
        if (floor in 1..3) {
            val blueprint = FloorBlueprintSystem.getBlueprintForFloor(floor)
            return blueprint.introScenario
        }
        val root = if (lang.lowercase() == "tr") trJson else enJson
        val fallbackRoot = enJson

        // FloorScenario details
        val (scenarioKey, formatFloorArg) = when {
            floor == 100 -> Pair("floor_100", false)
            floor % 25 == 0 -> Pair("exarch_council", true)
            floor % 10 == 0 -> Pair("arbiter_threshold", true)
            else -> Pair("bracket_${((floor - 1) / 10) + 1}", true)
        }

        try {
            val scenarioObj = root?.getJSONObject("scenarios")?.optJSONObject(scenarioKey)
                ?: fallbackRoot?.getJSONObject("scenarios")?.getJSONObject(scenarioKey)
                ?: throw Exception("Scenario key not found")

            // Titles
            val rawTitle = scenarioObj.optString("title", "")
            val title = if (formatFloorArg) String.format(rawTitle, floor) else rawTitle

            val trTitle = trJson?.getJSONObject("scenarios")?.optJSONObject(scenarioKey)?.let {
                val trRaw = it.optString("title", "")
                if (formatFloorArg) String.format(trRaw, floor) else trRaw
            } ?: title

            val enTitle = enJson?.getJSONObject("scenarios")?.optJSONObject(scenarioKey)?.let {
                val enRaw = it.optString("title", "")
                if (formatFloorArg) String.format(enRaw, floor) else enRaw
            } ?: title

            // Descriptions
            val rawDesc = scenarioObj.optString("description", "")
            val description = if (formatFloorArg) String.format(rawDesc, floor) else rawDesc

            val trDesc = trJson?.getJSONObject("scenarios")?.optJSONObject(scenarioKey)?.let {
                val trRaw = it.optString("description", "")
                if (formatFloorArg) String.format(trRaw, floor) else trRaw
            } ?: description

            val enDesc = enJson?.getJSONObject("scenarios")?.optJSONObject(scenarioKey)?.let {
                val enRaw = it.optString("description", "")
                if (formatFloorArg) String.format(enRaw, floor) else enRaw
            } ?: description

            // Build Options
            fun buildOption(optId: String, alignmentShift: Int, goldChange: Int, aetherChange: Int, hpChange: Int): GameOption {
                val trOptText = trJson?.getJSONObject("scenarios")?.getJSONObject(scenarioKey)?.optString("${optId}_text", "") ?: ""
                val enOptText = enJson?.getJSONObject("scenarios")?.getJSONObject(scenarioKey)?.optString("${optId}_text", "") ?: ""

                val rawTrJournal = trJson?.getJSONObject("scenarios")?.getJSONObject(scenarioKey)?.optString("${optId}_journal", "") ?: ""
                val trJournal = if (formatFloorArg) String.format(rawTrJournal, floor) else rawTrJournal

                val rawEnJournal = enJson?.getJSONObject("scenarios")?.getJSONObject(scenarioKey)?.optString("${optId}_journal", "") ?: ""
                val enJournal = if (formatFloorArg) String.format(rawEnJournal, floor) else rawEnJournal

                return GameOption(
                    textEn = enOptText,
                    textTr = trOptText,
                    alignmentShift = alignmentShift,
                    goldChange = goldChange,
                    aetherChange = aetherChange,
                    hpChange = hpChange,
                    journalEn = enJournal,
                    journalTr = trJournal
                )
            }

            // Statically define changes to match narrative rules
            val optA: GameOption
            val optB: GameOption
            val optC: GameOption

            val bracketIdx = (floor - 1) / 10
            when {
                floor == 100 -> {
                    optA = buildOption("optA", 40, 0, 500, -20)
                    optB = buildOption("optB", -40, 0, 500, -20)
                    optC = buildOption("optC", 0, 1000, 0, -80)
                }
                floor % 25 == 0 -> {
                    optA = buildOption("optA", 15, -100, 150, 0)
                    optB = buildOption("optB", -15, -100, 150, 0)
                    optC = buildOption("optC", 0, 300, 0, 0)
                }
                floor % 10 == 0 -> {
                    optA = buildOption("optA", 20, 0, 100, -20)
                    optB = buildOption("optB", -20, 0, 120, -10)
                    optC = buildOption("optC", 0, -150, 0, 0)
                }
                else -> {
                    // Standard dynamic floors based on bracket
                    when (bracketIdx) {
                        0 -> { // 1-10
                            optA = buildOption("optA", 8, 0, 25, 0)
                            optB = buildOption("optB", -8, 0, 30, 0)
                            optC = buildOption("optC", 0, 50, 0, 0)
                        }
                        1 -> { // 11-20
                            optA = buildOption("optA", 10, 0, 40, -10)
                            optB = buildOption("optB", -10, 0, 45, -10)
                            optC = buildOption("optC", 0, 0, 0, 20)
                        }
                        2 -> { // 21-30
                            optA = buildOption("optA", 12, 0, 50, -15)
                            optB = buildOption("optB", -12, 0, 55, 0)
                            optC = buildOption("optC", 0, 60, 0, 0)
                        }
                        3 -> { // 31-40
                            optA = buildOption("optA", 10, -80, 80, 0)
                            optB = buildOption("optB", -10, -80, 85, 0)
                            optC = buildOption("optC", 0, 70, 0, 10)
                        }
                        4 -> { // 41-50
                            optA = buildOption("optA", 10, 0, 40, 0)
                            optB = buildOption("optB", -14, 0, 60, 0)
                            optC = buildOption("optC", 0, 80, 0, 0)
                        }
                        5 -> { // 51-60
                            optA = buildOption("optA", 10, 0, 50, -10)
                            optB = buildOption("optB", -10, 0, 50, -10)
                            optC = buildOption("optC", 0, -20, 0, 30)
                        }
                        6 -> { // 61-70
                            optA = buildOption("optA", 15, 0, 60, -10)
                            optB = buildOption("optB", -15, 0, 60, -10)
                            optC = buildOption("optC", 0, 0, 30, 0)
                        }
                        7 -> { // 71-80
                            optA = buildOption("optA", 12, -100, 100, 0)
                            optB = buildOption("optB", -12, -100, 100, 0)
                            optC = buildOption("optC", 0, 150, 0, -10)
                        }
                        8 -> { // 81-90
                            optA = buildOption("optA", 20, 0, 80, -20)
                            optB = buildOption("optB", -20, 0, 80, -20)
                            optC = buildOption("optC", 0, 0, 0, 40)
                        }
                        else -> { // 91-99
                            optA = buildOption("optA", 10, 0, 40, -15)
                            optB = buildOption("optB", -10, 0, 40, -15)
                            optC = buildOption("optC", 0, -50, 0, 0)
                        }
                    }
                }
            }

            return FloorScenario(
                floor = floor,
                titleEn = enTitle,
                titleTr = trTitle,
                descriptionEn = enDesc,
                descriptionTr = trDesc,
                optionA = optA,
                optionB = optB,
                optionC = optC
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error building FloorScenario: $floor for lang $lang", e)
            return NarrativeEngine.getScenarioForFloor(floor)
        }
    }
}
