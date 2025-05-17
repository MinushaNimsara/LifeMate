// MoodViewModel.kt - Update with this code

package com.minusha.lifemate.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minusha.lifemate.model.COMMON_MOOD_FACTORS
import com.minusha.lifemate.model.Mood
import com.minusha.lifemate.model.WeeklyMoodData
import com.minusha.lifemate.repository.FirebaseMoodRepository
import com.minusha.lifemate.repository.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class MoodViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // NEW: Add repository
    private val repository: MoodRepository = FirebaseMoodRepository()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // NEW: Add state for analytics
    private val _weeklyMoodData = MutableStateFlow<List<WeeklyMoodData>>(emptyList())
    val weeklyMoodData: StateFlow<List<WeeklyMoodData>> = _weeklyMoodData

    private val _moodFactors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val moodFactors: StateFlow<Map<String, Int>> = _moodFactors

    // NEW: Selected factors for adding mood
    private val _selectedFactors = MutableStateFlow<List<String>>(emptyList())
    val selectedFactors: StateFlow<List<String>> = _selectedFactors

    init {
        // Load analytics data when ViewModel is created
        loadMoodAnalytics()
    }

    fun saveMood(rating: Int, note: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    val moodEntry = hashMapOf(
                        "rating" to rating,
                        "note" to note,
                        "timestamp" to System.currentTimeMillis(),
                        "date" to Date(),
                        "factors" to _selectedFactors.value // Add factors
                    )

                    try {
                        firestore.collection("users")
                            .document(userId)
                            .collection("moods")
                            .add(moodEntry)
                            .await()

                        _message.value = "Mood saved successfully"

                        // Clear selected factors after saving
                        _selectedFactors.value = emptyList()

                        // Reload analytics
                        loadMoodAnalytics()

                    } catch (e: Exception) {
                        _message.value = "Error saving mood: ${e.message}"
                        Log.e("MoodViewModel", "Firestore error: ${e.message}", e)
                    }
                } else {
                    _message.value = "Please sign in to save your mood"
                }
            } catch (e: Exception) {
                _message.value = "Error saving mood: ${e.message}"
                Log.e("MoodViewModel", "Save mood error: ${e.message}", e)
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    // Method for mood history
    fun getMoodHistory() = firestore.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("moods")
        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

    // NEW: Method to load analytics data
    private fun loadMoodAnalytics() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                // Load weekly mood data
                val weeklyData = repository.getWeeklyMoodAverages(userId, 4)
                _weeklyMoodData.value = weeklyData

                // Load factor frequency
                val factorData = repository.getMoodFactorFrequency(userId, 30)
                _moodFactors.value = factorData

            } catch (e: Exception) {
                Log.e("MoodViewModel", "Error loading analytics", e)
            }
        }
    }

    // NEW: Methods for handling mood factors
    fun toggleFactor(factor: String) {
        val currentFactors = _selectedFactors.value.toMutableList()

        if (currentFactors.contains(factor)) {
            currentFactors.remove(factor)
        } else {
            currentFactors.add(factor)
        }

        _selectedFactors.value = currentFactors
    }

    fun getAvailableMoodFactors(): List<String> {
        return COMMON_MOOD_FACTORS
    }

    // Helper function to get color for mood rating
    fun getMoodColor(rating: Int): Color {
        return when(rating) {
            1 -> Color(0xFFF44336) // Red - Very bad
            2 -> Color(0xFFFF9800) // Orange - Bad
            3 -> Color(0xFFFFEB3B) // Yellow - Neutral
            4 -> Color(0xFF4CAF50) // Green - Good
            5 -> Color(0xFF2196F3) // Blue - Very good
            else -> Color.Gray
        }
    }

    // Get emoji for mood rating
    fun getMoodEmoji(rating: Int): String {
        return when(rating) {
            1 -> "😢" // Very sad
            2 -> "😕" // Sad
            3 -> "😐" // Neutral
            4 -> "🙂" // Happy
            5 -> "😄" // Very happy
            else -> "❓"
        }
    }

    // Format date for display
    fun formatDate(timestamp: Long, pattern: String = "MMM d"): String {
        val date = Date(timestamp)
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    // Get insights based on mood data
    fun getInsights(): List<String> {
        val insights = mutableListOf<String>()
        val weeklyData = _weeklyMoodData.value

        if (weeklyData.size >= 2) {
            val currentWeek = weeklyData.lastOrNull { it.moodCount > 0 }
            val previousWeek = weeklyData
                .filter { it.moodCount > 0 }
                .sortedByDescending { it.weekStartDate }
                .drop(1)
                .firstOrNull()

            if (currentWeek != null && previousWeek != null) {
                if (currentWeek.averageRating > previousWeek.averageRating + 0.5f) {
                    insights.add("Your mood has improved significantly since last week!")
                } else if (currentWeek.averageRating > previousWeek.averageRating) {
                    insights.add("Your mood has slightly improved since last week.")
                } else if (currentWeek.averageRating < previousWeek.averageRating - 0.5f) {
                    insights.add("Your mood has declined since last week.")
                } else if (currentWeek.averageRating < previousWeek.averageRating) {
                    insights.add("Your mood has slightly declined since last week.")
                } else {
                    insights.add("Your mood has been stable over the past two weeks.")
                }
            }
        }

        // Factor-based insights
        val factorsData = _moodFactors.value
        if (factorsData.isNotEmpty()) {
            val topFactor = factorsData.entries.sortedByDescending { it.value }.firstOrNull()
            if (topFactor != null) {
                insights.add("'${topFactor.key}' seems to be a significant factor in your mood.")
            }
        }

        // Add generic insights if we don't have enough data
        if (insights.isEmpty()) {
            insights.add("Continue tracking your mood to see more personalized insights.")
        }

        return insights
    }
}