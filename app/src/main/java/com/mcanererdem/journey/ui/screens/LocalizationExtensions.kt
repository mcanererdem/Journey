package com.mcanererdem.journey.ui.screens

import com.mcanererdem.journey.data.engine.LocalizationManager
import com.mcanererdem.journey.data.engine.NarrativeBranchOption
import com.mcanererdem.journey.data.engine.NarrativeEvent
import com.mcanererdem.journey.data.engine.SecretBossEncounter
import com.mcanererdem.journey.data.engine.TitleDef
import com.mcanererdem.journey.data.engine.QuestDef
import com.mcanererdem.journey.data.model.LegacyUpgradeType
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.engine.FloorStateManager
import com.mcanererdem.journey.data.engine.FloorStateManager.FloorObjective

import com.mcanererdem.journey.data.model.CombatLogEntry
import com.mcanererdem.journey.data.model.ActionMessage

fun NarrativeEvent.getTitle(lang: String): String = LocalizationManager.getString(lang, titleKey)
fun NarrativeEvent.getDescription(lang: String): String = LocalizationManager.getString(lang, descriptionKey)
fun NarrativeEvent.getPreconditionDesc(lang: String): String = LocalizationManager.getString(lang, preconditionDescKey)
fun NarrativeBranchOption.getText(lang: String): String = LocalizationManager.getString(lang, textKey)
fun NarrativeBranchOption.getOutcome(lang: String): String = LocalizationManager.getString(lang, outcomeKey)
fun SecretBossEncounter.getName(lang: String): String = LocalizationManager.getString(lang, nameKey)
fun SecretBossEncounter.getDescription(lang: String): String = LocalizationManager.getString(lang, descriptionKey)
fun SecretBossEncounter.getUnlockRequirement(lang: String): String = LocalizationManager.getString(lang, unlockRequirementKey)
fun TitleDef.getName(lang: String): String = LocalizationManager.getString(lang, nameKey)
fun TitleDef.getDescription(lang: String): String = LocalizationManager.getString(lang, descKey)
fun TitleDef.getRequirementDesc(lang: String): String = LocalizationManager.getString(lang, requirementDescKey)
fun QuestDef.getTitle(lang: String): String = LocalizationManager.getString(lang, titleKey)
fun QuestDef.getDescription(lang: String): String = LocalizationManager.getString(lang, descKey)
fun QuestDef.getRequirement(lang: String): String = LocalizationManager.getString(lang, requirementKey)
fun LegacyUpgradeType.getName(lang: String): String = LocalizationManager.getString(lang, nameKey)
fun LegacyUpgradeType.getDescription(lang: String): String = LocalizationManager.getString(lang, descriptionKey)
fun JournalEntry.getActionTaken(lang: String): String = if (lang == "TR") actionTakenTr else actionTakenEs
fun FloorObjective.getText(lang: String): String {
    // Delegate to the companion object's extension method
    return with(FloorStateManager) { this@getText.getText(lang) }
}

fun CombatLogEntry.getFormattedText(lang: String): String {
    val template = LocalizationManager.getString(lang, key)
    val resolvedArgs = args.map { (k, v) ->
        if (v.contains(".")) LocalizationManager.getString(lang, v) else v
    }
    
    return try {
        when (key) {
            "combat_log_initiated" -> String.format(template, resolvedArgs.getOrNull(0) ?: "")
            "combat_log_player_strike" -> String.format(template, args["damage"] ?: "")
            "combat_log_player_heavy_blow" -> String.format(template, args["damage"] ?: "")
            "combat_log_player_barrier" -> String.format(template, args["heal"] ?: "")
            "combat_log_enemy_attack" -> String.format(template, args["damage"] ?: "")
            "combat_log_victory" -> String.format(template, args["exp"] ?: "", args["gold"] ?: "")
            "combat_log_loot" -> {
                val item = args["item"] ?: ""
                val localizedItem = if (item.contains(".")) LocalizationManager.getString(lang, item) else item
                String.format(template, localizedItem)
            }
            "combat_log_title" -> {
                val title = args["title"] ?: ""
                val localizedTitle = if (title.contains(".")) LocalizationManager.getString(lang, title) else title
                String.format(template, localizedTitle)
            }
            "combat_log_level_up" -> String.format(template, args["level"] ?: "")
            "combat_log_secret_boss_initiated" -> {
                val boss = args["boss"] ?: ""
                val localizedBoss = if (boss.contains(".")) LocalizationManager.getString(lang, boss) else boss
                String.format(template, localizedBoss, args["hp"] ?: "")
            }
            "combat_log_boss_attack" -> {
                val boss = args["boss"] ?: ""
                val localizedBoss = if (boss.contains(".")) LocalizationManager.getString(lang, boss) else boss
                String.format(template, localizedBoss, args["damage"] ?: "")
            }
            "combat_log_direct" -> String.format(template, args["text"] ?: "")
            else -> {
                if (args.isEmpty()) {
                    template
                } else {
                    String.format(template, *resolvedArgs.toTypedArray())
                }
            }
        }
    } catch (e: Exception) {
        template
    }
}

fun ActionMessage.getFormattedText(lang: String): String {
    val template = LocalizationManager.getString(lang, key)
    return try {
        if (args.isEmpty()) {
            template
        } else {
            val resolvedArgs = args.map { arg ->
                if (arg is String && arg.contains(".")) {
                    LocalizationManager.getString(lang, arg)
                } else {
                    arg
                }
            }
            String.format(template, *resolvedArgs.toTypedArray())
        }
    } catch (e: Exception) {
        template
    }
}


