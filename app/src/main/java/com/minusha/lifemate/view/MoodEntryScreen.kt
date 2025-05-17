// MoodEntryScreen.kt - Update existing file or create new

package com.minusha.lifemate.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minusha.lifemate.viewmodel.MoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodEntryScreen(
    onNavigateBack: () -> Unit, // Already exists
    onNavigateToFactors: () -> Unit, // NEW: Add this parameter
    viewModel: MoodViewModel = viewModel()
) {
    var moodRating by remember { mutableStateOf(3) }
    var note by remember { mutableStateOf("") }
    val message by viewModel.message.collectAsState()
    val selectedFactors by viewModel.selectedFactors.collectAsState()

    LaunchedEffect(message) {
        if (message != null) {
            // You could show a snackbar here
            // Reset fields if mood was saved successfully
            if (message?.contains("successfully") == true) {
                moodRating = 3
                note = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Your Mood") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How are you feeling today?",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mood rating selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (rating in 1..5) {
                    MoodRatingOption(
                        rating = rating,
                        selected = rating == moodRating,
                        onSelect = { moodRating = rating },
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Note input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Add a note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Factors selection
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToFactors() }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "What affected your mood today?",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedFactors.isEmpty()) {
                        Text(
                            text = "Tap to select factors",
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Text(
                            text = selectedFactors.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    viewModel.saveMood(moodRating, note)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            if (message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message ?: "",
                    color = if (message?.contains("Error") == true)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MoodRatingOption(
    rating: Int,
    selected: Boolean,
    onSelect: () -> Unit,
    viewModel: MoodViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onSelect() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (selected) viewModel.getMoodColor(rating)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = viewModel.getMoodEmoji(rating),
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when(rating) {
                1 -> "Very bad"
                2 -> "Bad"
                3 -> "Neutral"
                4 -> "Good"
                5 -> "Very good"
                else -> ""
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}