package com.minusha.lifemate.view

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
                }
            )
        }

        composable("tasks") {
            TaskListScreen(
                onAddTask = {
                    navController.navigate("add_task")
                },
                onTaskClick = { taskId ->
                    // TODO: Navigate to task detail screen
                    // For now, just a placeholder
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
    }
}