package com.minusha.lifemate.model

data class WeeklyMoodData(
    val weekStartDate: Long,
    val weekEndDate: Long,
    val averageRating: Float,
    val moodCount: Int,
    val topFactors: List<String> = emptyList()
)