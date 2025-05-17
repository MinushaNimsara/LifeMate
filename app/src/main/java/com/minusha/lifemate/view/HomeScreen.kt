package com.minusha.lifemate.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minusha.lifemate.model.Task
import com.minusha.lifemate.ui.theme.LifeMateTheme
import com.minusha.lifemate.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import androidx.compose.ui.tooling.preview.Preview

import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onTasksClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onMoodHistoryClick: () -> Unit,
    onAddMoodClick: () -> Unit,
    onSettingsClick: () -> Unit,
    taskViewModel: TaskViewModel = viewModel(),
    onMoodTrackerClick: () -> Unit
) {
    val tasksState by taskViewModel.tasksState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with greeting and profile
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile image
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M", // First letter of your name
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Greeting
                Column {
                    Text(
                        text = "Good Morning, Minusha",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Notification icon
            IconButton(onClick = { /* Open notifications */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search bar
        OutlinedTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Your Task...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(50),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Today's Tasks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Tasks",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            TextButton(onClick = { onTasksClick() }) {
                Text(
                    text = "See More",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Tasks list
        when (val state = tasksState) {
            is TaskViewModel.TasksState.Loading -> {
                Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is TaskViewModel.TasksState.Success -> {
                if (state.tasks.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No tasks for today")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onAddTaskClick) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Task")
                            }
                        }
                    }
                } else {
                    // Show only first 3 tasks
                    val topTasks = state.tasks.take(3)
                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(topTasks) { task ->
                            HomeTaskItem(
                                task = task,
                                onToggleDone = { taskViewModel.toggleTaskDone(task.id) }
                            )
                        }
                    }
                }
            }

            is TaskViewModel.TasksState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Add more features
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onAddMoodClick,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Mood, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Record Mood")
            }

            Button(
                onClick = onMoodHistoryClick,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mood History")
            }
        }
    }
}

@Composable
fun HomeTaskItem(
    task: Task,
    onToggleDone: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task status checkbox
            IconButton(
                onClick = onToggleDone,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (task.isDone) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Toggle task status",
                    tint = if (task.isDone) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            }

            // Task content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LifeMateTheme {
        HomeScreen(
            onSignOut = {},
            onTasksClick = {},
            onAddTaskClick = {},
            onMoodHistoryClick = {},
            onAddMoodClick = {},
            onSettingsClick = {},
            onMoodTrackerClick = {}
        )
    }
}