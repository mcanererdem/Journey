package com.example.data.engine

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object SoundManager {
    private const val TAG = "SoundManager"
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            // Initialize ToneGenerator on STREAM_MUSIC at 80% volume
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ToneGenerator", e)
        }
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play tone: $toneType", e)
        }
    }

    fun playStrike() {
        playTone(ToneGenerator.TONE_PROP_BEEP, 80)
    }

    fun playSkill() {
        playTone(ToneGenerator.TONE_PROP_ACK, 180)
    }

    fun playHeal() {
        playTone(ToneGenerator.TONE_PROP_PROMPT, 150)
    }

    fun playLevelUp() {
        playTone(ToneGenerator.TONE_DTMF_0, 250)
    }

    fun playDeath() {
        playTone(ToneGenerator.TONE_SUP_ERROR, 450)
    }

    fun playChime() {
        playTone(ToneGenerator.TONE_PROP_BEEP2, 150)
    }
}
