package com.minusha.lifemate.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val isDone: Boolean = false,
    val dueDate: Long? = null,
    val priority: Int = 0, // 0: low, 1: medium, 2: high
    val createdAt: Long = System.currentTimeMillis()
)