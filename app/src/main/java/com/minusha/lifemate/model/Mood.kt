package com.minusha.lifemate.model

import java.util.UUID

data class Mood(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val rating: Int = 3, // 1-5 rating (1=very bad, 5=very good)
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val factors: List<String> = emptyList() // Factors that affected mood
)

// List of common mood factors
val COMMON_MOOD_FACTORS = listOf(
    "Sleep", "Work", "Exercise", "Food",
    "Social", "Weather", "Health", "Family",
    "Stress", "Relaxation", "Achievement", "Entertainment"
)