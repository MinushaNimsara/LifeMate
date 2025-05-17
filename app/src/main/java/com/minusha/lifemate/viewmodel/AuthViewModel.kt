package com.minusha.lifemate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minusha.lifemate.model.User
import com.minusha.lifemate.repository.AuthRepository
import com.minusha.lifemate.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository: AuthRepository by lazy { FirebaseAuthRepository() }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeAuthChanges()
    }

    private fun observeAuthChanges() {
        viewModelScope.launch {
            try {
                repository.currentUser
                    .catch { e ->
                        Log.e("AuthViewModel", "Error collecting user: ${e.message}")
                        _authState.value = AuthState.Unauthenticated
                    }
                    .collectLatest { user ->
                        if (user != null) {
                            Log.d("AuthViewModel", "User authenticated: ${user.email}")
                            _authState.value = AuthState.Authenticated(user)
                        } else {
                            Log.d("AuthViewModel", "User unauthenticated")
                            _authState.value = AuthState.Unauthenticated
                        }
                    }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception in auth observation: ${e.message}")
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthViewModel", "Attempting sign in with email: $email")
                val success = repository.signIn(email, password)

                if (!success) {
                    Log.e("AuthViewModel", "Sign in failed")
                    _authState.value = AuthState.Error("Invalid email or password")
                } else {
                    Log.d("AuthViewModel", "Sign in successful")
                    // observeAuthChanges will update the state
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign in exception: ${e.message}")
                _authState.value = AuthState.Error("Sign in error: ${e.message}")
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthViewModel", "Attempting sign up: $email, $name")
                val success = repository.signUp(name, email, password)

                if (!success) {
                    Log.e("AuthViewModel", "Sign up failed")
                    _authState.value = AuthState.Error("Failed to create account")
                } else {
                    Log.d("AuthViewModel", "Sign up successful")
                    // observeAuthChanges will update the state
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign up exception: ${e.message}")
                _authState.value = AuthState.Error("Sign up error: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Signing out")
                repository.signOut()
                // observeAuthChanges will update state
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign out exception: ${e.message}")
            }
        }
    }

    sealed class AuthState {
        object Loading : AuthState()
        data class Authenticated(val user: User) : AuthState()
        object Unauthenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }
}