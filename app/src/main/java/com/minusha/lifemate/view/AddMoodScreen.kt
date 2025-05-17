package com.minusha.lifemate.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minusha.lifemate.model.COMMON_MOOD_FACTORS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoodScreen(
    onBackClick: () -> Unit
) {
    var moodRating by remember { mutableStateOf(3) }
    var moodNote by remember { mutableStateOf("") }
    var selectedFactors by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Mood Entry") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mood rating slider
            Text(
                text = when(moodRating) {
                    1 -> "Very Bad"
                    2 -> "Bad"
                    3 -> "Neutral"
                    4 -> "Good"
                    5 -> "Very Good"
                    else -> "Neutral"
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Slider(
                value = moodRating.toFloat(),
                onValueChange = { moodRating = it.toInt() },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Note field
            OutlinedTextField(
                value = moodNote,
                onValueChange = { moodNote = it },
                label = { Text("Add a note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Factors that affected mood
            Text(
                text = "What factors affected your mood?",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Just showing a few factors for simplicity
            COMMON_MOOD_FACTORS.take(6).forEach { factor ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = selectedFactors.contains(factor),
                        onCheckedChange = { checked ->
                            selectedFactors = if (checked) {
                                selectedFactors + factor
                            } else {
                                selectedFactors - factor
                            }
                        }
                    )
                    Text(factor)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // Save mood entry
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}