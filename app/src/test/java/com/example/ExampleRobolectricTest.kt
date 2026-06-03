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

    // survival quest requirements: climb to floor 5. Fresh player is on floor 1.
    val survivalQuestProgress = progressList.find { it.quest.id == "normal_survival" }
    org.junit.Assert.assertNotNull(survivalQuestProgress)
    org.junit.Assert.assertFalse(survivalQuestProgress!!.isCompleted)
    org.junit.Assert.assertFalse(survivalQuestProgress.requirementMet)
  }
}
