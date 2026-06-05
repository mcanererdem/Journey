package com.example.data.engine

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Helper data structures for JSON parsing
    data class GeminiOptionJson(
        val textEn: String,
        val textTr: String,
        val alignmentShift: Int,
        val goldChange: Int = 0,
        val gleamChange: Int = 0,
        val pyreChange: Int = 0,
        val hpChange: Int = 0,
        val journalEn: String,
        val journalTr: String
    )

    data class GeminiScenarioJson(
        val titleEn: String,
        val titleTr: String,
        val descriptionEn: String,
        val descriptionTr: String,
        val optionA: GeminiOptionJson,
        val optionB: GeminiOptionJson,
        val optionC: GeminiOptionJson
    )

    /**
     * Checks if the Gemini API Key is configured and not a placeholder/empty.
     */
    fun isApiKeyAvailable(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return !apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "PLACEHOLDER"
    }

    /**
     * Queries the Gemini API to dynamically generate a FloorScenario for the given floor
     * based on the player's current alignment, chosen class, and level.
     */
    suspend fun generateDynamicScenario(
        floor: Int,
        alignment: Int,
        chosenClass: String,
        level: Int
    ): FloorScenario? {
        if (!isApiKeyAvailable()) {
            Log.d(TAG, "Gemini API key is not available, skipping dynamic generation.")
            return null
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val prompt = """
            Generate a dark fantasy text RPG scenario for Floor $floor of the Spires of Blight.
            Player details: Class: $chosenClass, Alignment: $alignment, Level: $level.
            
            The response MUST be a JSON object matching this schema exactly:
            {
              "titleEn": "...",
              "titleTr": "...",
              "descriptionEn": "...",
              "descriptionTr": "...",
              "optionA": {
                "textEn": "...",
                "textTr": "...",
                "alignmentShift": 10,  // Sanctum option: positive shift (+5 to +15)
                "goldChange": 0,
                "gleamChange": 30,
                "pyreChange": 0,
                "hpChange": -10,
                "journalEn": "...",
                "journalTr": "..."
              },
              "optionB": {
                "textEn": "...",
                "textTr": "...",
                "alignmentShift": -10, // Covenant option: negative shift (-5 to -15)
                "goldChange": 0,
                "gleamChange": 0,
                "pyreChange": 30,
                "hpChange": -10,
                "journalEn": "...",
                "journalTr": "..."
              },
              "optionC": {
                "textEn": "...",
                "textTr": "...",
                "alignmentShift": 0,   // Neutral option: 0 alignment shift
                "goldChange": 50,
                "gleamChange": 0,
                "pyreChange": 0,
                "hpChange": 15,
                "journalEn": "...",
                "journalTr": "..."
              }
            }
            Make sure the Turkish descriptions (Tr fields) are high-quality, atmospheric, and natural.
            Do not wrap JSON in markdown block. Return ONLY the raw JSON object.
        """.trimIndent()

        val requestBodyJson = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val requestBody = requestBodyJson.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API request failed: Code ${response.code}, Message: ${response.message}")
                return null
            }

            val bodyString = response.body?.string() ?: return null
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return null
            val candidate = candidates.optJSONObject(0) ?: return null
            val content = candidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val part = parts.optJSONObject(0) ?: return null
            val textResponse = part.optString("text") ?: return null

            val adapter = moshi.adapter(GeminiScenarioJson::class.java)
            val scenarioJson = adapter.fromJson(textResponse) ?: return null

            // Map parsed JSON to FloorScenario domain model
            FloorScenario(
                floor = floor,
                titleEn = scenarioJson.titleEn,
                titleTr = scenarioJson.titleTr,
                descriptionEn = scenarioJson.descriptionEn,
                descriptionTr = scenarioJson.descriptionTr,
                optionA = GameOption(
                    textEn = scenarioJson.optionA.textEn,
                    textTr = scenarioJson.optionA.textTr,
                    alignmentShift = scenarioJson.optionA.alignmentShift,
                    goldChange = scenarioJson.optionA.goldChange,
                    gleamChange = scenarioJson.optionA.gleamChange,
                    pyreChange = scenarioJson.optionA.pyreChange,
                    hpChange = scenarioJson.optionA.hpChange,
                    journalEn = scenarioJson.optionA.journalEn,
                    journalTr = scenarioJson.optionA.journalTr
                ),
                optionB = GameOption(
                    textEn = scenarioJson.optionB.textEn,
                    textTr = scenarioJson.optionB.textTr,
                    alignmentShift = scenarioJson.optionB.alignmentShift,
                    goldChange = scenarioJson.optionB.goldChange,
                    gleamChange = scenarioJson.optionB.gleamChange,
                    pyreChange = scenarioJson.optionB.pyreChange,
                    hpChange = scenarioJson.optionB.hpChange,
                    journalEn = scenarioJson.optionB.journalEn,
                    journalTr = scenarioJson.optionB.journalTr
                ),
                optionC = GameOption(
                    textEn = scenarioJson.optionC.textEn,
                    textTr = scenarioJson.optionC.textTr,
                    alignmentShift = scenarioJson.optionC.alignmentShift,
                    goldChange = scenarioJson.optionC.goldChange,
                    gleamChange = scenarioJson.optionC.gleamChange,
                    pyreChange = scenarioJson.optionC.pyreChange,
                    hpChange = scenarioJson.optionC.hpChange,
                    journalEn = scenarioJson.optionC.journalEn,
                    journalTr = scenarioJson.optionC.journalTr
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating or parsing dynamic scenario", e)
            null
        }
    }
}
