package com.example.luraeditor.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.luraeditor.data.model.Filter
import com.example.luraeditor.domain.processor.FilterProcessor
import com.example.luraeditor.domain.processor.ImageProcessor
import com.example.luraeditor.ui.theme.Purple500
import com.example.luraeditor.ui.theme.Slate600
import kotlinx.coroutines.launch

@Composable
fun FilterItem(
    filter: Filter,
    originalBitmap: Bitmap?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(originalBitmap, filter) {
        originalBitmap?.let { bitmap ->
            scope.launch {
                // Create small thumbnail first
                val small = ImageProcessor.createThumbnail(bitmap, 100)

                // Apply filter to thumbnail
                val filtered = if (filter.id != "none") {
                    FilterProcessor.applyFilter(small, filter, 1f)
                } else {
                    small
                }

                thumbnail = filtered
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .shadow(
                    elevation = if (isSelected) 8.dp else 2.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) Purple500 else Slate600,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick)
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = filter.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Slate600),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                }
            }

            // Selected indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Purple500.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = filter.name,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Purple500 else MaterialTheme.colorScheme.onSurface
        )
    }
}