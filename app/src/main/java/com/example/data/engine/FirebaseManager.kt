package com.example.data.engine

import android.util.Log
import com.example.data.model.PlayerProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.gms.tasks.Task

// Self-contained extension to await Play Services tasks
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Task failed"))
        }
    }
}

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Check if user is signed in
    val currentUserId: String?
        get() = auth.currentUser?.uid

    // Sign in anonymously
    suspend fun signInAnonymously(): String? {
        return try {
            val result = auth.signInAnonymously().awaitTask()
            val userId = result.user?.uid
            Log.d(TAG, "Signed in anonymously: $userId")
            userId
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in anonymously", e)
            null
        }
    }

    // Save Player Profile to Firestore
    suspend fun syncProfileToCloud(profile: PlayerProfile): Boolean {
        val userId = currentUserId ?: signInAnonymously() ?: return false
        return try {
            val data = hashMapOf(
                "playerName" to profile.playerName,
                "chosenClass" to profile.chosenClass,
                "level" to profile.level,
                "exp" to profile.exp,
                "maxExp" to profile.maxExp,
                "currentHp" to profile.currentHp,
                "maxHp" to profile.maxHp,
                "currentWill" to profile.currentWill,
                "maxWill" to profile.maxWill,
                "gold" to profile.gold,
                "aether" to profile.aether,
                "currentFloor" to profile.currentFloor,
                "side" to profile.side,
                "momentum" to profile.momentum,
                "equippedTitle" to profile.equippedTitle,
                "titlesEncoded" to profile.titlesEncoded,
                "storyFlagsEncoded" to profile.storyFlagsEncoded,
                "itemsEncoded" to profile.itemsEncoded,
                "lastSyncTime" to System.currentTimeMillis()
            )
            firestore.collection("profiles").document(userId).set(data).awaitTask()
            Log.d(TAG, "Synced profile for user: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing profile", e)
            false
        }
    }

    // Load Player Profile from Firestore
    suspend fun fetchProfileFromCloud(): PlayerProfile? {
        val userId = currentUserId ?: return null
        return try {
            val doc = firestore.collection("profiles").document(userId).get().awaitTask()
            if (doc.exists()) {
                PlayerProfile(
                    id = 1,
                    playerName = doc.getString("playerName") ?: "Anonymous Conqueror",
                    chosenClass = doc.getString("chosenClass") ?: "Exile",
                    level = doc.getLong("level")?.toInt() ?: 1,
                    exp = doc.getLong("exp")?.toInt() ?: 0,
                    maxExp = doc.getLong("maxExp")?.toInt() ?: 100,
                    currentHp = doc.getLong("currentHp")?.toInt() ?: 100,
                    maxHp = doc.getLong("maxHp")?.toInt() ?: 100,
                    currentWill = doc.getLong("currentWill")?.toInt() ?: 10,
                    maxWill = doc.getLong("maxWill")?.toInt() ?: 10,
                    gold = doc.getLong("gold")?.toInt() ?: 0,
                    aether = doc.getLong("aether")?.toInt() ?: 0,
                    currentFloor = doc.getLong("currentFloor")?.toInt() ?: 1,
                    currentNodeIndex = 0,
                    currentNodeCompleted = false,
                    savedFloorCheckpoint = 1,
                    side = doc.getString("side") ?: "NEUTRAL",
                    momentum = doc.getLong("momentum")?.toInt() ?: 50,
                    equippedTitle = doc.getString("equippedTitle") ?: "",
                    titlesEncoded = doc.getString("titlesEncoded") ?: "",
                    storyFlagsEncoded = doc.getString("storyFlagsEncoded") ?: "",
                    itemsEncoded = doc.getString("itemsEncoded") ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile", e)
            null
        }
    }

    // Fetch Top Leaderboards (ranked by level, then currentFloor)
    suspend fun fetchLeaderboard(): List<LeaderboardEntry> {
        return try {
            val querySnapshot = firestore.collection("profiles")
                .orderBy("level", Query.Direction.DESCENDING)
                .orderBy("currentFloor", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .awaitTask()
            
            querySnapshot.documents.mapIndexed { index, doc ->
                LeaderboardEntry(
                    rank = index + 1,
                    name = doc.getString("playerName") ?: "Unknown Exile",
                    level = doc.getLong("level")?.toInt() ?: 1,
                    floor = doc.getLong("currentFloor")?.toInt() ?: 1,
                    side = doc.getString("side") ?: "NEUTRAL"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching leaderboard", e)
            emptyList()
        }
    }
}

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val level: Int,
    val floor: Int,
    val side: String
)
