package com.mcanererdem.journey

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
    val uneligiblePlayer = com.mcanererdem.journey.data.model.PlayerProfile(level = 1, momentum = 50)
    val eligiblePlayer = com.mcanererdem.journey.data.model.PlayerProfile(level = 4, momentum = 75)

    val lightseekerTitle = com.mcanererdem.journey.data.engine.QuestTitleSystem.getTitleDef("lightseeker")
    org.junit.Assert.assertNotNull(lightseekerTitle)
    org.junit.Assert.assertFalse(lightseekerTitle!!.meetsPreconditions(uneligiblePlayer))
    org.junit.Assert.assertTrue(lightseekerTitle.meetsPreconditions(eligiblePlayer))
  }

  @Test
  fun `verify secret titles are marked hidden`() {
    val observerTitle = com.mcanererdem.journey.data.engine.QuestTitleSystem.getTitleDef("cosmic_observer")
    org.junit.Assert.assertNotNull(observerTitle)
    org.junit.Assert.assertTrue(observerTitle!!.isHidden)

    val lightseekerTitle = com.mcanererdem.journey.data.engine.QuestTitleSystem.getTitleDef("lightseeker")
    org.junit.Assert.assertNotNull(lightseekerTitle)
    org.junit.Assert.assertFalse(lightseekerTitle!!.isHidden)
  }

  @Test
  fun `verify quests eligible progress mapping`() {
    val freshPlayer = com.mcanererdem.journey.data.model.PlayerProfile()
    val progressList = com.mcanererdem.journey.data.engine.QuestTitleSystem.getQuestProgress(freshPlayer)

    // climb to floor 5. Fresh player is on floor 1.
    val survivalQuestProgress = progressList.find { it.quest.id == "main_foothold" }
    org.junit.Assert.assertNotNull(survivalQuestProgress)
    org.junit.Assert.assertFalse(survivalQuestProgress!!.isCompleted)
    org.junit.Assert.assertFalse(survivalQuestProgress.requirementMet)
  }

  @Test
  fun `verify custom quest categories like main side chain and hidden and rewards`() {
    val player = com.mcanererdem.journey.data.model.PlayerProfile(level = 4, gold = 100)
    val progress = com.mcanererdem.journey.data.engine.QuestTitleSystem.getQuestProgress(player)

    val mainQuest = progress.find { it.quest.type == com.mcanererdem.journey.data.engine.QuestType.MAIN }
    val sideQuest = progress.find { it.quest.type == com.mcanererdem.journey.data.engine.QuestType.SIDE }
    val chainQuest = progress.find { it.quest.type == com.mcanererdem.journey.data.engine.QuestType.CHAIN }
    val hiddenQuest = progress.find { it.quest.type == com.mcanererdem.journey.data.engine.QuestType.HIDDEN }

    org.junit.Assert.assertNotNull(mainQuest)
    org.junit.Assert.assertNotNull(sideQuest)
    org.junit.Assert.assertNotNull(chainQuest)
    org.junit.Assert.assertNotNull(hiddenQuest)

    // Check progress on completed or active state tracking (level 4 meets chain_ascension_1)
    val chain1 = progress.find { it.quest.id == "chain_ascension_1" }
    org.junit.Assert.assertNotNull(chain1)
    org.junit.Assert.assertTrue(chain1!!.requirementMet)
  }

  @Test
  fun `verify quest status progress labels and fraction calculations`() {
    val player = com.mcanererdem.journey.data.model.PlayerProfile(currentFloor = 3, gold = 150)
    val progress = com.mcanererdem.journey.data.engine.QuestTitleSystem.getQuestProgress(player)

    val mainQuest = progress.find { it.quest.id == "main_foothold" }
    org.junit.Assert.assertNotNull(mainQuest)
    
    val (labelEn, fraction) = mainQuest!!.getProgressLabelAndFraction(player, isTr = false)
    org.junit.Assert.assertEquals("Floor 3 / 5", labelEn)
    org.junit.Assert.assertEquals(0.6f, fraction, 0.01f)

    val sideQuest = progress.find { it.quest.id == "side_wealth" }
    org.junit.Assert.assertNotNull(sideQuest)
    val (goldLabel, goldFraction) = sideQuest!!.getProgressLabelAndFraction(player, isTr = true)
    org.junit.Assert.assertEquals("Altın 150 / 300", goldLabel)
    org.junit.Assert.assertEquals(0.5f, goldFraction, 0.01f)
  }
}
