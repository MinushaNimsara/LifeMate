// FirebaseMoodRepository.kt - Update with these additional methods

package com.minusha.lifemate.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.minusha.lifemate.model.Mood
import com.minusha.lifemate.model.WeeklyMoodData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class FirebaseMoodRepository : MoodRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    override val moods: Flow<List<Mood>> = callbackFlow {
        if (currentUserId.isBlank()) {
            trySend(emptyList())
            awaitClose { /* No listener to remove */ }
            return@callbackFlow
        }

        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = firestore.collection("users")
                .document(currentUserId)
                .collection("moods")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseMoodRepo", "Error getting moods: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val moodList = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Mood::class.java)
                    } ?: emptyList()

                    trySend(moodList)
                }
        } catch (e: Exception) {
            Log.e("FirebaseMoodRepo", "Error setting up mood listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    override suspend fun addMood(mood: Mood) {
        if (currentUserId.isBlank()) return

        try {
            val moodWithUserId = mood.copy(userId = currentUserId)

            firestore.collection("users")
                .document(currentUserId)
                .collection("moods")
                .document(moodWithUserId.id)
                .set(moodWithUserId)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseMoodRepo", "Error adding mood: ${e.message}")
            throw e
        }
    }

    override suspend fun updateMood(mood: Mood) {
        if (currentUserId.isBlank()) return

        try {
            firestore.collection("users")
                .document(currentUserId)
                .collection("moods")
                .document(mood.id)
                .set(mood)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseMoodRepo", "Error updating mood: ${e.message}")
            throw e
        }
    }

    override suspend fun deleteMood(moodId: String) {
        if (currentUserId.isBlank()) return

        try {
            firestore.collection("users")
                .document(currentUserId)
                .collection("moods")
                .document(moodId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseMoodRepo", "Error deleting mood: ${e.message}")
            throw e
        }
    }

    override suspend fun getMoodById(moodId: String): Mood? {
        if (currentUserId.isBlank()) return null

        try {
            val doc = firestore.collection("users")
                .document(currentUserId)
                .collection("moods")
                .document(moodId)
                .get()
                .await()

            return doc.toObject(Mood::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseMoodRepo", "Error getting mood by ID: ${e.message}")
            throw e
        }
    }

    override suspend fun getMoodsForDay(timestamp: Long): List<Mood> {
        if (currentUserId.isBlank()) return emptyList()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return getMoodsForRange(startOfDay, endOfDay)
    }

    override suspend fun getMoodsForRange(startTimestamp: Long, endTimestamp: Long): List<Mood> {
        if (currentUserId.isBlank()) return emptyList()

        try {
            val snapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("moods")
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThanOrEqualTo("timestamp", endTimestamp)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            return snapshot.documents.mapNotNull { doc ->
                doc.toObject(Mood::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseMoodRepo", "Error getting moods for range: ${e.message}")
            return emptyList()
        }
    }

    override suspend fun getAverageRatingForRange(startTimestamp: Long, endTimestamp: Long): Float {
        val moods = getMoodsForRange(startTimestamp, endTimestamp)
        if (moods.isEmpty()) return 0f

        val sum = moods.sumOf { it.rating }
        return sum.toFloat() / moods.size
    }

    // NEW METHODS FOR ANALYTICS

    override suspend fun getWeeklyMoodAverages(userId: String, weeksCount: Int): List<WeeklyMoodData> {
        if (currentUserId.isBlank()) return emptyList()

        val result = mutableListOf<WeeklyMoodData>()
        val calendar = Calendar.getInstance()

        // Set to current time
        calendar.timeInMillis = System.currentTimeMillis()

        // End date is now
        val endDate = calendar.timeInMillis

        // Start date is weeksCount weeks ago
        calendar.add(Calendar.WEEK_OF_YEAR, -weeksCount)
        val startDate = calendar.timeInMillis

        try {
            // Get all moods in the range
            val moods = getMoodsForRange(startDate, endDate)

            // Reset calendar to start date
            calendar.timeInMillis = startDate

            // Process each week
            for (week in 0 until weeksCount) {
                // Set to start of week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val weekStartDate = calendar.timeInMillis

                // Move to end of week
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                val weekEndDate = calendar.timeInMillis

                // Filter moods for this week
                val weekMoods = moods.filter {
                    it.timestamp in weekStartDate..weekEndDate
                }

                if (weekMoods.isNotEmpty()) {
                    // Calculate average rating
                    val avgRating = weekMoods.map { it.rating }.average().toFloat()

                    // Count occurrences of each factor
                    val factorCounts = mutableMapOf<String, Int>()
                    weekMoods.forEach { mood ->
                        mood.factors.forEach { factor ->
                            factorCounts[factor] = (factorCounts[factor] ?: 0) + 1
                        }
                    }

                    // Get top factors
                    val topFactors = factorCounts.entries
                        .sortedByDescending { it.value }
                        .take(3)
                        .map { it.key }

                    result.add(
                        WeeklyMoodData(
                            weekStartDate = weekStartDate,
                            weekEndDate = weekEndDate,
                            averageRating = avgRating,
                            moodCount = weekMoods.size,
                            topFactors = topFactors
                        )
                    )
                } else {
                    // Add empty data for weeks with no mood entries
                    result.add(
                        WeeklyMoodData(
                            weekStartDate = weekStartDate,
                            weekEndDate = weekEndDate,
                            averageRating = 0f,
                            moodCount = 0,
                            topFactors = emptyList()
                        )
                    )
                }

                // Move to next week
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

        } catch (e: Exception) {
            Log.e("FirebaseMoodRepo", "Error getting weekly mood averages", e)
        }

        return result.sortedBy { it.weekStartDate }
    }

    override suspend fun getMoodFactorFrequency(userId: String, daysBack: Int): Map<String, Int> {
        if (currentUserId.isBlank()) return emptyMap()

        val factorMap = mutableMapOf<String, Int>()

        try {
            val calendar = Calendar.getInstance()

            // End date is now
            val endDate = calendar.timeInMillis

            // Start date is daysBack days ago
            calendar.add(Calendar.DAY_OF_YEAR, -daysBack)
            val startDate = calendar.timeInMillis

            val moods = getMoodsForRange(startDate, endDate)

            moods.forEach { mood ->
                mood.factors.forEach { factor ->
                    factorMap[factor] = (factorMap[factor] ?: 0) + 1
                }
            }

        } catch (e: Exception) {
            Log.e("FirebaseMoodRepo", "Error getting factor frequency", e)
        }

        return factorMap
    }
}