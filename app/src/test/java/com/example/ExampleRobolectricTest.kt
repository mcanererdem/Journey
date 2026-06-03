package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Light & Darkness", appName)
  }

  @Test
  fun `verify lightseeker title preconditions`() {
    val uneligiblePlayer = com.example.data.model.PlayerProfile(level = 1, alignment = 0)
    val eligiblePlayer = com.example.data.model.PlayerProfile(level = 4, alignment = 30)

    val lightseekerTitle = com.example.data.engine.QuestTitleSystem.getTitleDef("lightseeker")
    org.junit.Assert.assertNotNull(lightseekerTitle)
    org.junit.Assert.assertFalse(lightseekerTitle!!.meetsPreconditions(uneligiblePlayer))
    org.junit.Assert.assertTrue(lightseekerTitle.meetsPreconditions(eligiblePlayer))
  }

  @Test
  fun `verify secret titles are marked hidden`() {
    val observerTitle = com.example.data.engine.QuestTitleSystem.getTitleDef("cosmic_observer")
    org.junit.Assert.assertNotNull(observerTitle)
    org.junit.Assert.assertTrue(observerTitle!!.isHidden)

    val lightseekerTitle = com.example.data.engine.QuestTitleSystem.getTitleDef("lightseeker")
    org.junit.Assert.assertNotNull(lightseekerTitle)
    org.junit.Assert.assertFalse(lightseekerTitle!!.isHidden)
  }

  @Test
  fun `verify quests eligible progress mapping`() {
    val freshPlayer = com.example.data.model.PlayerProfile()
    val progressList = com.example.data.engine.QuestTitleSystem.getQuestProgress(freshPlayer)

    // climb to floor 5. Fresh player is on floor 1.
    val survivalQuestProgress = progressList.find { it.quest.id == "main_foothold" }
    org.junit.Assert.assertNotNull(survivalQuestProgress)
    org.junit.Assert.assertFalse(survivalQuestProgress!!.isCompleted)
    org.junit.Assert.assertFalse(survivalQuestProgress.requirementMet)
  }

  @Test
  fun `verify custom quest categories like main side chain and hidden and rewards`() {
    val player = com.example.data.model.PlayerProfile(level = 4, gold = 100)
    val progress = com.example.data.engine.QuestTitleSystem.getQuestProgress(player)

    val mainQuest = progress.find { it.quest.type == com.example.data.engine.QuestType.MAIN }
    val sideQuest = progress.find { it.quest.type == com.example.data.engine.QuestType.SIDE }
    val chainQuest = progress.find { it.quest.type == com.example.data.engine.QuestType.CHAIN }
    val hiddenQuest = progress.find { it.quest.type == com.example.data.engine.QuestType.HIDDEN }

    org.junit.Assert.assertNotNull(mainQuest)
    org.junit.Assert.assertNotNull(sideQuest)
    org.junit.Assert.assertNotNull(chainQuest)
    org.junit.Assert.assertNotNull(hiddenQuest)

    // Check progress on completed or active state tracking (level 4 meets chain_ascension_1)
    val chain1 = progress.find { it.quest.id == "chain_ascension_1" }
    org.junit.Assert.assertNotNull(chain1)
    org.junit.Assert.assertTrue(chain1!!.requirementMet)
  }
}
