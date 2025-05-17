package com.minusha.lifemate.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minusha.lifemate.model.WeeklyMoodData
import com.minusha.lifemate.viewmodel.MoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodAnalyticsScreen(
    onBackClick: () -> Unit,
    viewModel: MoodViewModel = viewModel()
) {
    val weeklyMoodData by viewModel.weeklyMoodData.collectAsState()
    val moodFactors by viewModel.moodFactors.collectAsState()
    val insights = viewModel.getInsights()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Analytics") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Weekly mood chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Weekly Mood Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (weeklyMoodData.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No mood data available yet.\nStart recording your daily mood!",
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        WeeklyMoodChart(
                            weeklyData = weeklyMoodData,
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Top mood factors
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Top Mood Factors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (moodFactors.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No factor data available yet.\nAdd factors when recording your mood!",
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        MoodFactorsView(moodFactors)
                    }
                }
            }

            // Mood insights
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Mood Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (insights.isEmpty()) {
                        Text("Record more moods to see personalized insights.")
                    } else {
                        MoodInsightsView(insights)
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyMoodChart(
    weeklyData: List<WeeklyMoodData>,
    viewModel: MoodViewModel
) {
    // Filter to only include data points with mood count > 0
    val filteredData = weeklyData.filter { it.moodCount > 0 }

    if (filteredData.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // Simple bar chart using Row and Box
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                filteredData.forEach { weekData ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.getMoodEmoji(weekData.averageRating.toInt()),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height((weekData.averageRating * 25).dp)
                                .background(
                                    color = viewModel.getMoodColor(weekData.averageRating.toInt()),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                }
            }

            // Line connecting points
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (filteredData.size > 1) {
                    val barWidth = size.width / filteredData.size
                    val points = filteredData.mapIndexed { index, data ->
                        val x = barWidth * (index + 0.5f)
                        val y = size.height - (data.averageRating * 25)
                        Offset(x, y)
                    }

                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = Color.Gray,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 2f
                        )

                        // Draw circles at points
                        drawCircle(
                            color = viewModel.getMoodColor(filteredData[i].averageRating.toInt()),
                            radius = 5f,
                            center = points[i],
                            style = Stroke(width = 2f)
                        )
                    }

                    // Draw last point
                    drawCircle(
                        color = viewModel.getMoodColor(filteredData.last().averageRating.toInt()),
                        radius = 5f,
                        center = points.last(),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }

        // X axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            filteredData.forEach { weekData ->
                Text(
                    text = viewModel.formatDate(weekData.weekStartDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MoodFactorsView(factors: Map<String, Int>) {
    val sortedFactors = factors.entries.sortedByDescending { it.value }.take(5)
    val maxValue = sortedFactors.maxOfOrNull { it.value } ?: 1

    Column {
        sortedFactors.forEach { (factor, count) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = factor,
                    modifier = Modifier.width(80.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                LinearProgressIndicator(
                    progress = count.toFloat() / maxValue.toFloat(),
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MoodInsightsView(insights: List<String>) {
    Column {
        insights.forEach { insight ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Insight",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}