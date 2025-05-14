package com.minusha.lifemate.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val joinDate: Long = System.currentTimeMillis()
)