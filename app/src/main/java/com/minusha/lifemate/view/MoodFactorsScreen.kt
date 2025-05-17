// MoodFactorsScreen.kt - Create this new file

package com.minusha.lifemate.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minusha.lifemate.viewmodel.MoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodFactorsScreen(
    onBackClick: () -> Unit,
    viewModel: MoodViewModel = viewModel()
) {
    val selectedFactors by viewModel.selectedFactors.collectAsState()
    val availableFactors = viewModel.getAvailableMoodFactors()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("What affected your mood?") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
        ) {
            Text(
                "Select factors that affected your mood today:",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableFactors) { factor ->
                    FactorChip(
                        factor = factor,
                        isSelected = selectedFactors.contains(factor),
                        onToggle = { viewModel.toggleFactor(factor) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
fun FactorChip(
    factor: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() },
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = factor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}