package com.example.luraeditor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun CropOverlay(
    imageSize: Size,
    cropRect: Rect,
    onCropRectChange: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentRect by remember { mutableStateOf(cropRect) }
    var dragPosition by remember { mutableStateOf<DragPosition?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragPosition = detectDragPosition(offset, currentRect)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragPosition?.let { position ->
                            currentRect = updateCropRect(
                                currentRect,
                                position,
                                dragAmount,
                                imageSize
                            )
                        }
                    },
                    onDragEnd = {
                        onCropRectChange(currentRect)
                        dragPosition = null
                    }
                )
            }
    ) {
        // Draw overlay
        val overlayPath = Path().apply {
            addRect(Rect(Offset.Zero, size))
            addRect(currentRect)
            fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
        }

        drawPath(
            path = overlayPath,
            color = Color.Black.copy(alpha = 0.5f)
        )

        // Draw crop rect border
        drawRect(
            color = Color.White,
            topLeft = currentRect.topLeft,
            size = currentRect.size,
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(10f, 10f)
                )
            )
        )

        // Draw corner handles
        val handleSize = 30f
        val corners = listOf(
            currentRect.topLeft,
            Offset(currentRect.right, currentRect.top),
            currentRect.bottomRight,
            Offset(currentRect.left, currentRect.bottom)
        )

        corners.forEach { corner ->
            drawCircle(
                color = Color.White,
                radius = handleSize / 2,
                center = corner
            )
            drawCircle(
                color = Color(0xFF8B5CF6),
                radius = handleSize / 3,
                center = corner
            )
        }

        // Draw grid lines
        val gridColor = Color.White.copy(alpha = 0.5f)

        // Vertical lines
        for (i in 1..2) {
            val x = currentRect.left + (currentRect.width / 3) * i
            drawLine(
                color = gridColor,
                start = Offset(x, currentRect.top),
                end = Offset(x, currentRect.bottom),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Horizontal lines
        for (i in 1..2) {
            val y = currentRect.top + (currentRect.height / 3) * i
            drawLine(
                color = gridColor,
                start = Offset(currentRect.left, y),
                end = Offset(currentRect.right, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

private enum class DragPosition {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
    TOP, BOTTOM, LEFT, RIGHT, CENTER
}

private fun detectDragPosition(offset: Offset, rect: Rect): DragPosition {
    val handleSize = 50f

    return when {
        (offset - rect.topLeft).getDistance() < handleSize -> DragPosition.TOP_LEFT
        (offset - Offset(rect.right, rect.top)).getDistance() < handleSize -> DragPosition.TOP_RIGHT
        (offset - rect.bottomRight).getDistance() < handleSize -> DragPosition.BOTTOM_RIGHT
        (offset - Offset(
            rect.left,
            rect.bottom
        )).getDistance() < handleSize -> DragPosition.BOTTOM_LEFT

        offset.y < rect.top + handleSize && offset.x in rect.left..rect.right -> DragPosition.TOP
        offset.y > rect.bottom - handleSize && offset.x in rect.left..rect.right -> DragPosition.BOTTOM
        offset.x < rect.left + handleSize && offset.y in rect.top..rect.bottom -> DragPosition.LEFT
        offset.x > rect.right - handleSize && offset.y in rect.top..rect.bottom -> DragPosition.RIGHT
        rect.contains(offset) -> DragPosition.CENTER
        else -> DragPosition.CENTER
    }
}

private fun updateCropRect(
    rect: Rect,
    position: DragPosition,
    dragAmount: Offset,
    imageSize: Size
): Rect {
    val minSize = 100f

    return when (position) {
        DragPosition.TOP_LEFT -> Rect(
            left = (rect.left + dragAmount.x).coerceIn(0f, rect.right - minSize),
            top = (rect.top + dragAmount.y).coerceIn(0f, rect.bottom - minSize),
            right = rect.right,
            bottom = rect.bottom
        )

        DragPosition.TOP_RIGHT -> Rect(
            left = rect.left,
            top = (rect.top + dragAmount.y).coerceIn(0f, rect.bottom - minSize),
            right = (rect.right + dragAmount.x).coerceIn(rect.left + minSize, imageSize.width),
            bottom = rect.bottom
        )

        DragPosition.BOTTOM_LEFT -> Rect(
            left = (rect.left + dragAmount.x).coerceIn(0f, rect.right - minSize),
            top = rect.top,
            right = rect.right,
            bottom = (rect.bottom + dragAmount.y).coerceIn(rect.top + minSize, imageSize.height)
        )

        DragPosition.BOTTOM_RIGHT -> Rect(
            left = rect.left,
            top = rect.top,
            right = (rect.right + dragAmount.x).coerceIn(rect.left + minSize, imageSize.width),
            bottom = (rect.bottom + dragAmount.y).coerceIn(rect.top + minSize, imageSize.height)
        )

        DragPosition.CENTER -> {
            val newLeft = (rect.left + dragAmount.x).coerceIn(0f, imageSize.width - rect.width)
            val newTop = (rect.top + dragAmount.y).coerceIn(0f, imageSize.height - rect.height)
            Rect(
                offset = Offset(newLeft, newTop),
                size = rect.size
            )
        }

        else -> rect
    }
}

private fun Offset.coerceIn(min: Offset, max: Offset): Offset {
    return Offset(
        x.coerceIn(min.x, max.x),
        y.coerceIn(min.y, max.y)
    )
}