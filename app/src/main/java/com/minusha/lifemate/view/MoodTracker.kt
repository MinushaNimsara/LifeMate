package com.minusha.lifemate.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minusha.lifemate.ui.theme.LifeMateTheme
import com.minusha.lifemate.viewmodel.MoodViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast

enum class Mood(
    val description: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    GREAT("Great", Color(0xFF4CAF50), Icons.Filled.Favorite),
    GOOD("Good", Color(0xFF8BC34A), Icons.Filled.Favorite),
    NEUTRAL("Neutral", Color(0xFFFFC107), Icons.Filled.Star),
    BAD("Bad", Color(0xFFFF9800), Icons.Filled.FavoriteBorder),
    TERRIBLE("Terrible", Color(0xFFF44336), Icons.Filled.FavoriteBorder)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodTrackerScreen(
    onBackClick: () -> Unit,
    viewModel: MoodViewModel = viewModel()
) {
    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var notes by remember { mutableStateOf("") }

    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    // Display message as Toast
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Your Mood") },
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
            // Date and time
            val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            Text(
                text = currentDate,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "How are you feeling today?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mood selection row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MoodOption(
                    mood = Mood.GREAT,
                    isSelected = selectedMood == Mood.GREAT,
                    onSelect = { selectedMood = Mood.GREAT }
                )

                MoodOption(
                    mood = Mood.GOOD,
                    isSelected = selectedMood == Mood.GOOD,
                    onSelect = { selectedMood = Mood.GOOD }
                )

                MoodOption(
                    mood = Mood.NEUTRAL,
                    isSelected = selectedMood == Mood.NEUTRAL,
                    onSelect = { selectedMood = Mood.NEUTRAL }
                )

                MoodOption(
                    mood = Mood.BAD,
                    isSelected = selectedMood == Mood.BAD,
                    onSelect = { selectedMood = Mood.BAD }
                )

                MoodOption(
                    mood = Mood.TERRIBLE,
                    isSelected = selectedMood == Mood.TERRIBLE,
                    onSelect = { selectedMood = Mood.TERRIBLE }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // Get numeric rating based on mood
                    val rating = when(selectedMood) {
                        Mood.TERRIBLE -> 1
                        Mood.BAD -> 2
                        Mood.NEUTRAL -> 3
                        Mood.GOOD -> 4
                        Mood.GREAT -> 5
                        null -> 3
                    }
                    viewModel.saveMood(rating, notes)
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMood != null
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun MoodOption(
    mood: Mood,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(64.dp)
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            ),
            onClick = onSelect
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mood.icon,
                    contentDescription = mood.description,
                    tint = mood.color
                )
            }
        }

        Text(
            text = mood.description,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MoodTrackerScreenPreview() {
    LifeMateTheme {
        MoodTrackerScreen(
            onBackClick = {}
        )
    }
}