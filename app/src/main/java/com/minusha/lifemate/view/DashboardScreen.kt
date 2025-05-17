package com.minusha.lifemate.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minusha.lifemate.model.Task
import com.minusha.lifemate.viewmodel.AuthViewModel
import com.minusha.lifemate.viewmodel.MoodViewModel
import com.minusha.lifemate.viewmodel.TaskViewModel
import com.minusha.lifemate.viewmodel.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onTasksClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onMoodHistoryClick: () -> Unit,
    onAddMoodClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    moodViewModel: MoodViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val tasksState by taskViewModel.tasksState.collectAsState()

    val user = (authState as? AuthViewModel.AuthState.Authenticated)?.user

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("LifeMate")
                },
                actions = {
                    // Theme toggle button
                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (themeViewModel.isDarkMode)
                                Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }

                    // Settings button
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }

                    // Profile/Sign out
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Welcome section with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    // Date
                    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())

                    Text(
                        text = currentDate,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Greeting
                    Text(
                        text = "Welcome, ${user?.name ?: "User"}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick mood recording
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "How are you today?",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onAddMoodClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Record Mood")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tasks heading with "See All" link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Tasks",
                    style = MaterialTheme.typography.titleLarge
                )

                TextButton(onClick = onTasksClick) {
                    Text("See All")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task list preview
            when (val state = tasksState) {
                is TaskViewModel.TasksState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                is TaskViewModel.TasksState.Success -> {
                    if (state.tasks.isEmpty()) {
                        EmptyTasksCard(onAddTaskClick)
                    } else {
                        // Show first 3 tasks only
                        state.tasks.take(3).forEach { task ->
                            TaskCardPreview(task, taskViewModel)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                is TaskViewModel.TasksState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Add,
                    title = "New Task",
                    onClick = onAddTaskClick,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primaryContainer
                )

                QuickActionCard(
                    icon = Icons.Default.Face,
                    title = "Record Mood",
                    onClick = onAddMoodClick,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Analytics,
                    title = "Mood History",
                    onClick = onMoodHistoryClick,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondaryContainer
                )

                QuickActionCard(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    onClick = onSettingsClick,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}

@Composable
fun EmptyTasksCard(onAddTaskClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No tasks for today",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onAddTaskClick) {
                Text("Add Task")
            }
        }
    }
}

@Composable
fun TaskCardPreview(
    task: Task,
    taskViewModel: TaskViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isDone)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { taskViewModel.toggleTaskDone(task.id) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                task.dueDate?.let { dueDate ->
                    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    Text(
                        text = "Due: ${dateFormat.format(Date(dueDate))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}