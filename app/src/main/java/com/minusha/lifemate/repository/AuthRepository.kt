package com.minusha.lifemate.repository

import com.minusha.lifemate.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signIn(email: String, password: String): Boolean
    suspend fun signUp(name: String, email: String, password: String): Boolean
    suspend fun signOut()
}