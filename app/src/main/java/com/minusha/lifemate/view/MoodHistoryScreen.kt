package com.minusha.lifemate.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.minusha.lifemate.viewmodel.MoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: MoodViewModel = viewModel()
) {
    val moodHistory = remember { mutableStateListOf<DocumentSnapshot>() }
    val isLoading = remember { mutableStateOf(true) }

    // Load mood history
    LaunchedEffect(key1 = true) {
        viewModel.getMoodHistory().get()
            .addOnSuccessListener { documents ->
                moodHistory.clear()
                moodHistory.addAll(documents.documents)
                isLoading.value = false
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (moodHistory.isEmpty()) {
                Text(
                    text = "No mood entries yet. Start recording your mood!",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(moodHistory) { document ->
                        MoodHistoryItem(document)
                    }
                }
            }
        }
    }
}

@Composable
fun MoodHistoryItem(document: DocumentSnapshot) {
    val dateFormat = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())

    val timestamp = document.getLong("timestamp")
    val rating = document.getLong("rating")?.toInt() ?: 3
    val note = document.getString("note") ?: ""

    val formattedDate = if (timestamp != null) {
        dateFormat.format(Date(timestamp))
    } else {
        "Unknown date"
    }

    val moodText = when (rating) {
        1 -> "Very Bad"
        2 -> "Bad"
        3 -> "Neutral"
        4 -> "Good"
        5 -> "Very Good"
        else -> "Unknown"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mood: $moodText",
                style = MaterialTheme.typography.bodyLarge
            )

            if (note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}