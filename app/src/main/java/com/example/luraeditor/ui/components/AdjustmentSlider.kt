package com.example.luraeditor.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luraeditor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..2f,
    modifier: Modifier = Modifier,
    displayValue: String = "${(value * 100).toInt()}%",
    icon: ImageVector? = null,
    iconColor: Color = Purple500,
    showTrackGradient: Boolean = false,
    trackColor: Color = Purple500
) {
    val normalizedValue = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val animatedNormalizedValue by animateFloatAsState(
        targetValue = normalizedValue,
        label = "sliderAnimation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Slate800.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate800.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with icon and label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Icon Container
                    icon?.let {
                        Surface(
                            color = iconColor.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp),
                            contentColor = iconColor
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = it,
                                    contentDescription = label,
                                    modifier = Modifier.size(16.dp),
                                    tint = iconColor
                                )
                            }
                        }
                    }

                    // Label
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Value Display
                Surface(
                    color = Slate700,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp))
                ) {
                    AnimatedContent(
                        targetState = displayValue,
                        label = "valueAnimation"
                    ) { currentValue ->
                        Text(
                            text = currentValue,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Slider with Enhanced Design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Slate700)
                )

                // Active Track (with optional gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedNormalizedValue)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                ) {
                    if (showTrackGradient) {
                        when (label) {
                            "الدفء" -> Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF4A90E2), // Cool Blue
                                                Color(0xFFFFD166), // Warm Yellow
                                                Color(0xFFFF6B35)  // Hot Orange
                                            )
                                        )
                                    )
                            )
                            "السطوع" -> Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Slate900,
                                                Color.White
                                            )
                                        )
                                    )
                            )
                            "التشبع" -> Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Slate700,
                                                Pink500
                                            )
                                        )
                                    )
                            )
                            else -> Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(trackColor)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(trackColor)
                        )
                    }
                }

                // Custom Slider
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    thumb = {
                        // Custom Thumb Design
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .shadow(8.dp, CircleShape, spotColor = trackColor.copy(alpha = 0.5f))
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(trackColor, CircleShape)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Min/Max Indicators (for warmth specifically)
                if (label == "الدفء") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = "بارد",
                                tint = Color(0xFF4A90E2),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "بارد",
                                color = Slate300,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "حار",
                                color = Slate300,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "حار",
                                tint = Color(0xFFFF6B35),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Preset Quick Actions (for warmth)
            if (label == "الدفء") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WarmthPresetChip(
                        text = "بارد",
                        isSelected = value == -25f,
                        color = Color(0xFF4A90E2),
                        modifier = Modifier.weight(1f),
                        onClick = { onValueChange(-25f) }
                    )

                    WarmthPresetChip(
                        text = "محايد",
                        isSelected = value == 0f,
                        color = Slate300,
                        modifier = Modifier.weight(1f),
                        onClick = { onValueChange(0f) }
                    )

                    WarmthPresetChip(
                        text = "دافئ",
                        isSelected = value == 25f,
                        color = Color(0xFFFFD166),
                        modifier = Modifier.weight(1f),
                        onClick = { onValueChange(25f) }
                    )

                    WarmthPresetChip(
                        text = "حار",
                        isSelected = value == 50f,
                        color = Color(0xFFFF6B35),
                        modifier = Modifier.weight(1f),
                        onClick = { onValueChange(50f) }
                    )
                }
            }
        }
    }
}

@Composable
fun WarmthPresetChip(
    text: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else Slate700.copy(alpha = 0.3f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            1.5.dp,
            color
        ) else null,
        modifier = modifier
            .height(32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = if (isSelected) color else Slate300,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// Companion object for common icons
object AdjustmentIcons {
    val Brightness = Icons.Default.WbSunny
    val Contrast = Icons.Default.Tonality
    val Saturation = Icons.Default.ColorLens
    val Warmth = Icons.Default.Whatshot
    val Sharpen = Icons.Default.FilterCenterFocus
    val Vignette = Icons.Default.Circle
    val Exposure = Icons.Default.Exposure
    val Highlights = Icons.Default.LightMode
    val Shadows = Icons.Default.DarkMode
    val Clarity = Icons.Default.AutoAwesome
}