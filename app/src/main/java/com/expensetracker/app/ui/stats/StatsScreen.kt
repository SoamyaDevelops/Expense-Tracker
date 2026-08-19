package com.expensetracker.app.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.app.util.DateUtils
import com.expensetracker.app.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::previousMonth) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous") }
                    Text(DateUtils.monthLabel(state.monthsAgo), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = viewModel::nextMonth, enabled = state.monthsAgo > 0) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next")
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text("Income", style = MaterialTheme.typography.labelLarge, color = Color(0xFF2FBF71))
                                Text(formatAmount(state.totalIncome, state.currencySymbol), style = MaterialTheme.typography.titleLarge)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Spent", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                                Text(formatAmount(state.totalExpenses, state.currencySymbol), style = MaterialTheme.typography.titleLarge)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Avg ${formatAmount(state.average, state.currencySymbol)} / day spent",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.dailyTotals.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Daily spending", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(24.dp))
                            BarChart(values = state.dailyTotals)
                        }
                    }
                }
            }

            if (state.categoryTotals.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("By category", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(16.dp))
                            DonutChart(
                                segments = state.categoryTotals.map { (cat, total) ->
                                    val color = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(Color.Gray)
                                    Triple(cat.name, total, color)
                                }
                            )
                        }
                    }
                }

                items(state.categoryTotals) { (category, total) ->
                    val color = runCatching { Color(android.graphics.Color.parseColor(category.colorHex)) }.getOrDefault(Color.Gray)
                    val pct = if (state.totalExpenses > 0) (total / state.totalExpenses * 100) else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(category.name, style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(
                                progress = { (pct / 100).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(formatAmount(total, state.currencySymbol), fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                item {
                    Text(
                        "No expenses this month yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun BarChart(values: List<Double>) {
    val maxVal = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val barColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.outlineVariant
    
    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(top = 20.dp, bottom = 20.dp)) {
            val barWidth = size.width / (values.size * 1.4f)
            val gap = (size.width - (barWidth * values.size)) / (values.size - 1).coerceAtLeast(1)
            
            // Draw horizontal guide lines
            val lines = 4
            for (i in 0..lines) {
                val y = size.height * (i.toFloat() / lines)
                drawLine(
                    color = secondaryColor.copy(alpha = 0.3f),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
            
            values.forEachIndexed { index, value ->
                val barHeight = (value / maxVal * size.height).toFloat()
                val x = index * (barWidth + gap)
                drawRoundRect(
                    color = if (value > 0) barColor else barColor.copy(alpha = 0.1f),
                    topLeft = androidx.compose.ui.geometry.Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }
        Text(
            formatAmount(maxVal, ""),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Composable
private fun DonutChart(segments: List<Triple<String, Double, Color>>) {
    val total = segments.sumOf { it.second }.coerceAtLeast(0.01)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(180.dp)) {
        Canvas(modifier = Modifier.size(160.dp)) {
            var startAngle = -90f
            segments.forEach { (_, value, color) ->
                val sweep = (value / total * 360.0).toFloat()
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 34f, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        Text(formatAmount(total, ""), style = MaterialTheme.typography.titleMedium)
    }
}
