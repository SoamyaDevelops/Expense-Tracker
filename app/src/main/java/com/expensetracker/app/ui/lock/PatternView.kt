package com.expensetracker.app.ui.lock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * A 3x3 pattern-unlock grid. Reports the finished pattern (list of node indices 0-8)
 * once the finger is lifted.
 */
@Composable
fun PatternView(
    modifier: Modifier = Modifier,
    onPatternComplete: (List<Int>) -> Unit
) {
    var nodeCenters by remember { mutableStateOf(listOf<Offset>()) }
    var selected by remember { mutableStateOf(listOf<Int>()) }
    var currentPos by remember { mutableStateOf<Offset?>(null) }

    val primary = MaterialTheme.colorScheme.primary
    val idle = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(24.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selected = listOf()
                        currentPos = offset
                        nodeCenters.forEachIndexed { index, center ->
                            if ((center - offset).getDistance() < 60f) {
                                selected = listOf(index)
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        currentPos = change.position
                        nodeCenters.forEachIndexed { index, center ->
                            if ((center - change.position).getDistance() < 60f && !selected.contains(index)) {
                                selected = selected + index
                            }
                        }
                    },
                    onDragEnd = {
                        if (selected.isNotEmpty()) onPatternComplete(selected)
                        selected = listOf()
                        currentPos = null
                    }
                )
            }
    ) {
        val cell = size.minDimension / 3f
        val centers = (0 until 9).map { i ->
            val row = i / 3
            val col = i % 3
            Offset(cell * col + cell / 2f, cell * row + cell / 2f)
        }
        nodeCenters = centers

        // connecting lines
        if (selected.size > 1) {
            for (i in 0 until selected.size - 1) {
                drawLine(
                    color = primary,
                    start = centers[selected[i]],
                    end = centers[selected[i + 1]],
                    strokeWidth = 8f
                )
            }
        }
        if (selected.isNotEmpty() && currentPos != null) {
            drawLine(
                color = primary.copy(alpha = 0.5f),
                start = centers[selected.last()],
                end = currentPos!!,
                strokeWidth = 8f
            )
        }

        centers.forEachIndexed { index, center ->
            val isSelected = selected.contains(index)
            drawCircle(
                color = if (isSelected) primary else idle,
                radius = if (isSelected) 22f else 16f,
                center = center,
                style = if (isSelected) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = 4f)
            )
            if (isSelected) {
                drawCircle(color = primary, radius = 8f, center = center)
            }
        }
    }
}
