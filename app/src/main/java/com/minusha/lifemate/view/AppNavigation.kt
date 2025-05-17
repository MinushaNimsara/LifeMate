package com.minusha.lifemate.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.minusha.lifemate.viewmodel.AuthViewModel
import com.minusha.lifemate.viewmodel.TaskViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val taskViewModel: TaskViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthViewModel.AuthState.Authenticated -> "home"
            else -> "login"
        }
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.signIn(email, password)
                },
                onSignUpClick = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterClick = { name, email, password ->
                    authViewModel.signUp(name, email, password)
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                onSignOut = {
                    authViewModel.signOut()
                },
                onTasksClick = {
                    navController.navigate("tasks")
                },
                onAddTaskClick = {
                    navController.navigate("add_task")
                },
                // Add these new parameters for mood tracking features
                onMoodTrackerClick = {
                    navController.navigate("mood_tracker")
                },
                onMoodHistoryClick = {
                    navController.navigate("mood_history")
                },
                onAddMoodClick = {
                    navController.navigate("add_mood")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable("tasks") {
            TaskListScreen(
                onAddTask = {
                    navController.navigate("add_task")
                },
                onTaskClick = { taskId ->
                    // In the future, you can navigate to task detail screen
                    // For now, we don't have task detail screen
                }
            )
        }

        composable("add_task") {
            AddTaskScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Add these new routes for mood tracking features
        composable("mood_tracker") {
            // Temporary placeholder until MoodTrackerScreen is implemented
            Text("Mood Tracker Screen - Coming Soon")
        }

        composable("mood_history") {
            // Temporary placeholder until MoodHistoryScreen is implemented
            Text("Mood History Screen - Coming Soon")
        }

        composable("add_mood") {
            // Temporary placeholder until AddMoodScreen is implemented
            Text("Add Mood Screen - Coming Soon")
        }

        composable("settings") {
            // Temporary placeholder until SettingsScreen is implemented
            Text("Settings Screen - Coming Soon")
        }
    }
}