// view/TaskListScreen.kt
package com.minusha.lifemate.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.minusha.lifemate.ui.theme.LightBlue
import com.minusha.lifemate.ui.theme.LightGreen
import com.minusha.lifemate.ui.theme.Orange
import com.minusha.lifemate.ui.theme.TaskGreen
import com.minusha.lifemate.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onAddTask: () -> Unit,
    onTaskClick: (String) -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    val tasksState by viewModel.tasksState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Manager") },
                actions = {
                    IconButton(onClick = { /* Edit mode */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Tasks")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Today's Tasks",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            when (val state = tasksState) {
                is TaskViewModel.TasksState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is TaskViewModel.TasksState.Success -> {
                    if (state.tasks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tasks available")
                        }
                    } else {
                        LazyColumn {
                            // Today's tasks
                            state.tasks.take(2).forEach { task ->
                                item {
                                    TaskDetailItem(
                                        task = task,
                                        onToggleDone = { viewModel.toggleTaskDone(task.id) },
                                        onClick = { onTaskClick(task.id) }
                                    )
                                }
                            }

                            item {
                                Text(
                                    text = "Upcoming Tasks",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }

                            // Upcoming tasks
                            state.tasks.drop(2).take(2).forEach { task ->
                                item {
                                    TaskDetailItem(
                                        task = task,
                                        onToggleDone = { viewModel.toggleTaskDone(task.id) },
                                        onClick = { onTaskClick(task.id) },
                                        showActions = true
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Category",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Categories
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    CategoryItem(
                                        name = "Work",
                                        color = LightBlue
                                    )

                                    CategoryItem(
                                        name = "Personal",
                                        color = Orange
                                    )

                                    CategoryItem(
                                        name = "Fitness",
                                        color = LightGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                            }
                        }
                    }
                }

                is TaskViewModel.TasksState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailItem(
    task: Task,
    onToggleDone: () -> Unit,
    onClick: () -> Unit,
    showActions: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (showActions) Color(0xFFF9FBE7) else Color.White
        )
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
                    tint = if (task.isDone) TaskGreen else Color.LightGray
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

                // Show due date for upcoming tasks
                if (showActions && task.dueDate != null) {
                    Text(
                        text = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(task.dueDate)),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Action buttons for upcoming tasks
            if (showActions) {
                Row {
                    IconButton(
                        onClick = { /* Edit task */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Task",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { /* Delete task */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    name: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when(name) {
                    "Work" -> Icons.Default.Work
                    "Personal" -> Icons.Default.Person
                    "Fitness" -> Icons.Default.FitnessCenter
                    else -> Icons.Default.Star
                },
                contentDescription = name,
                tint = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            fontSize = 12.sp
        )
    }
}