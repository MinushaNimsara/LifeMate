package com.minusha.lifemate.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.minusha.lifemate.util.NotificationHelper
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onThemeToggle: () -> Unit,
    isDarkMode: Boolean
) {
    // Local state for notification settings
    var dailyMoodReminder by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("2:51 AM") }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Reminder Time") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = timePickerState
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute

                        // Format time
                        val time = LocalTime.of(hour, minute)
                        val formatter = DateTimeFormatter.ofPattern("h:mm a")
                        reminderTime = time.format(formatter)

                        // Schedule notification
                        if (dailyMoodReminder) {
                            NotificationHelper.scheduleDailyReminder(context, hour, minute)
                        }

                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Theme switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Theme"
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Dark Theme",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Switch between light and dark theme",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onThemeToggle() }
                )
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            // Notifications section
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Daily mood reminder toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications"
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Daily Mood Reminder",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Get reminded to record your mood",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Switch(
                    checked = dailyMoodReminder,
                    onCheckedChange = {
                        dailyMoodReminder = it
                        if (it) {
                            // Parse current reminderTime string to get hour and minute
                            try {
                                val time = LocalTime.parse(reminderTime, DateTimeFormatter.ofPattern("h:mm a"))
                                NotificationHelper.scheduleDailyReminder(context, time.hour, time.minute)
                            } catch (e: Exception) {
                                // Use default time if parsing fails
                                NotificationHelper.scheduleDailyReminder(context, 9, 0) // 9:00 AM default
                            }
                        } else {
                            NotificationHelper.cancelDailyReminder(context)
                        }
                    }
                )
            }

            // Only show reminder time if daily mood reminder is enabled
            if (dailyMoodReminder) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reminder Time",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { showTimePicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(reminderTime)
                    }
                }
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            // About section
            Text(
                text = "About",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "LifeMate v1.0",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "© 2025 Minusha Nimsara",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}