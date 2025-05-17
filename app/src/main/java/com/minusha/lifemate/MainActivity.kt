package com.minusha.lifemate

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.minusha.lifemate.ui.theme.LifeMateTheme
import com.minusha.lifemate.view.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase and catch any initialization errors
        try {
            FirebaseApp.initializeApp(this)
            Log.d("MainActivity", "Firebase initialized successfully")

            // Test Firebase Auth connection
            val auth = FirebaseAuth.getInstance()
            Log.d("MainActivity", "FirebaseAuth instance created: ${auth != null}")

            // Show success toast
            Toast.makeText(this, "Firebase initialized", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase initialization failed: ${e.message}", e)
            Toast.makeText(this, "Firebase initialization error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        setContent {
            LifeMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use AppNavigation - no try-catch here
                    AppNavigation()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in when activity starts
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            Log.d("MainActivity", "Current user: ${currentUser?.email ?: "None"}")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking current user: ${e.message}", e)
        }
    }
}