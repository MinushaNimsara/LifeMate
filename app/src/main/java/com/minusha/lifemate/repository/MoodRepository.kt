// MoodRepository.kt - Update interface with new methods

package com.minusha.lifemate.repository

import com.minusha.lifemate.model.Mood
import com.minusha.lifemate.model.WeeklyMoodData
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    val moods: Flow<List<Mood>>
    suspend fun addMood(mood: Mood)
    suspend fun updateMood(mood: Mood)
    suspend fun deleteMood(moodId: String)
    suspend fun getMoodById(moodId: String): Mood?
    suspend fun getMoodsForDay(timestamp: Long): List<Mood>
    suspend fun getMoodsForRange(startTimestamp: Long, endTimestamp: Long): List<Mood>
    suspend fun getAverageRatingForRange(startTimestamp: Long, endTimestamp: Long): Float

    // NEW methods for analytics
    suspend fun getWeeklyMoodAverages(userId: String, weeksCount: Int = 4): List<WeeklyMoodData>
    suspend fun getMoodFactorFrequency(userId: String, daysBack: Int = 30): Map<String, Int>
}