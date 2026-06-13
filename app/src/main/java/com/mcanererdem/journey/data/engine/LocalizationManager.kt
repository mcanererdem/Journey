package com.mcanererdem.journey.data.engine

import android.content.Context
import org.json.JSONObject
import android.util.Log
import com.mcanererdem.journey.data.model.*
import java.io.InputStream
import java.nio.charset.Charset

object LocalizationManager {
    private const val TAG = "LocalizationManager"
    private var enJson: JSONObject? = null
    private var trJson: JSONObject? = null
    private var appContext: Context? = null
    private val stringCache = mutableMapOf<String, String>()

    fun init(context: Context) {
        appContext = context.applicationContext
        enJson = loadJsonFromAsset(context, "locales/en.json")
        trJson = loadJsonFromAsset(context, "locales/tr.json")
        stringCache.clear()
        Log.d(TAG, "Localization initialized. EN loaded: ${enJson != null}, TR loaded: ${trJson != null}")
    }

    fun loadFloorBlueprint(floor: Int): JSONObject? {
        return appContext?.let { loadJsonFromAsset(it, "blueprints/floor_$floor.json") }
    }

    fun loadGlobalEnemies(): JSONObject? {
        return appContext?.let { loadJsonFromAsset(it, "blueprints/global_enemies.json") }
    }

    fun getEnemyStats(enemyId: String): JSONObject? {
        return loadGlobalEnemies()?.optJSONObject(enemyId)
    }

    fun loadGlobalItems(): JSONObject? {
        return appContext?.let { loadJsonFromAsset(it, "blueprints/global_items.json") }
    }

    private fun loadJsonFromAsset(context: Context, fileName: String): JSONObject? {
        return try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charset.forName("UTF-8"))
            JSONObject(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading locale $fileName", e)
            null
        }
    }

    /**
     * Supports nested keys like "ui.app_name" or "floor.1.title"
     */
    fun getString(lang: String, key: String): String {
        val cacheKey = "${lang}_$key"
        stringCache[cacheKey]?.let { return it }

        val root = if (lang.uppercase() == "TR") trJson else enJson
        if (root == null) return key

        val result = try {
            val parts = key.split(".")
            var current: JSONObject = root
            for (i in 0 until parts.size - 1) {
                current = current.getJSONObject(parts[i])
            }
            current.optString(parts.last(), key)
        } catch (e: Exception) {
            key
        }

        stringCache[cacheKey] = result
        return result
    }

    fun formatString(lang: String, key: String, vararg args: Any): String {
        val template = getString(lang, key)
        return try {
            String.format(template, *args)
        } catch (e: Exception) {
            template
        }
    }

    /**
     * Helper specifically for floor content
     */
    fun getFloorString(lang: String, floor: Int, path: String): String {
        return getString(lang, "floor.$floor.$path")
    }
}

/**
 * Extension for ActionMessage to support easy UI formatting
 */
fun ActionMessage.getFormattedText(lang: String): String {
    if (key.isBlank()) return ""
    return try {
        val template = LocalizationManager.getString(lang, key)
        if (args.isEmpty()) template else String.format(template, *args.toTypedArray())
    } catch (e: Exception) {
        key
    }
}

/**
 * Extension for CombatLogEntry to support easy UI formatting
 */
fun CombatLogEntry.getFormattedText(lang: String): String {
    if (key.isBlank()) return ""
    return try {
        val template = LocalizationManager.getString(lang, key)
        if (args.isEmpty()) {
            template
        } else {
            var result = template
            args.forEach { (name, value) ->
                val localizedValue = if (value.startsWith("ui.") || value.startsWith("enemy.") || value.startsWith("skill.")) {
                    LocalizationManager.getString(lang, value)
                } else {
                    value
                }
                result = result.replace("{$name}", localizedValue)
            }
            result
        }
    } catch (e: Exception) {
        key
    }
}
