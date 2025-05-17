package com.minusha.lifemate.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.minusha.lifemate.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        Log.d("FirebaseAuthRepository", "Initialized with auth: ${auth != null}")
    }

    override val currentUser: Flow<User?> = callbackFlow {
        Log.d("FirebaseAuthRepository", "Starting currentUser flow")

        // Initially send the current user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("FirebaseAuthRepository", "Current user is signed in: ${currentUser.email}")
            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val user = userDoc.toObject(User::class.java) ?: User(
                    id = currentUser.uid,
                    name = currentUser.displayName ?: "",
                    email = currentUser.email ?: "",
                    joinDate = 0
                )
                Log.d("FirebaseAuthRepository", "Emitting user: ${user.email}")
                trySend(user)
            } catch (e: Exception) {
                Log.e("FirebaseAuthRepository", "Error getting user doc: ${e.message}")
                val basicUser = User(
                    id = currentUser.uid,
                    name = currentUser.displayName ?: "",
                    email = currentUser.email ?: "",
                    joinDate = 0
                )
                trySend(basicUser)
            }
        } else {
            Log.d("FirebaseAuthRepository", "Current user is null")
            trySend(null)
        }

        // Listen for auth state changes
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            Log.d("FirebaseAuthRepository", "Auth state changed: ${firebaseUser?.email}")

            if (firebaseUser != null) {
                firestore.collection("users").document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java) ?: User(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: "",
                            joinDate = 0
                        )
                        Log.d("FirebaseAuthRepository", "AuthStateListener emitting user: ${user.email}")
                        trySend(user)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseAuthRepository", "AuthStateListener error: ${e.message}")
                        val basicUser = User(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: "",
                            joinDate = 0
                        )
                        trySend(basicUser)
                    }
            } else {
                Log.d("FirebaseAuthRepository", "AuthStateListener emitting null user")
                trySend(null)
            }
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            Log.d("FirebaseAuthRepository", "Removing auth state listener")
            auth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signIn(email: String, password: String): Boolean {
        return try {
            Log.d("FirebaseAuthRepository", "Signing in with: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("FirebaseAuthRepository", "Sign in result: ${result.user != null}")
            result.user != null
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Sign in error: ${e.message}")
            false
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): Boolean {
        return try {
            Log.d("FirebaseAuthRepository", "Creating account: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                Log.d("FirebaseAuthRepository", "Account created: ${firebaseUser.uid}")

                try {
                    // Set display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    firebaseUser.updateProfile(profileUpdates).await()
                    Log.d("FirebaseAuthRepository", "Profile updated with name: $name")

                    // Create user document in Firestore
                    val user = User(
                        id = firebaseUser.uid,
                        name = name,
                        email = email,
                        joinDate = System.currentTimeMillis()
                    )

                    firestore.collection("users").document(user.id).set(user).await()
                    Log.d("FirebaseAuthRepository", "User document created in Firestore")
                } catch (e: Exception) {
                    Log.e("FirebaseAuthRepository", "Error updating profile: ${e.message}")
                }

                true
            } else {
                Log.e("FirebaseAuthRepository", "Account created but user is null")
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Sign up error: ${e.message}")
            false
        }
    }

    override suspend fun signOut() {
        try {
            Log.d("FirebaseAuthRepository", "Signing out")
            auth.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Sign out error: ${e.message}")
        }
    }
}