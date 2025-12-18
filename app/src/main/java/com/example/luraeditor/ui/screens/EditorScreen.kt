package com.example.luraeditor.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luraeditor.data.model.Filter
import com.example.luraeditor.domain.export.ImageExporter
import com.example.luraeditor.ui.components.AdjustmentSlider
import com.example.luraeditor.ui.components.FilterItem
import com.example.luraeditor.ui.theme.Blue500
import com.example.luraeditor.ui.theme.Pink500
import com.example.luraeditor.ui.theme.Purple500
import com.example.luraeditor.ui.theme.Slate100
import com.example.luraeditor.ui.theme.Slate200
import com.example.luraeditor.ui.theme.Slate300
import com.example.luraeditor.ui.theme.Slate600
import com.example.luraeditor.ui.theme.Slate700
import com.example.luraeditor.ui.theme.Slate800
import com.example.luraeditor.ui.theme.Slate900
import com.example.luraeditor.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    onSave: (Bitmap) -> Unit
) {
    val editState by viewModel.editState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showComparison by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showAdvancedControls by remember { mutableStateOf(true) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Purple500,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        containerColor = Slate900,
        topBar = {
            EditorTopBar(
                onBack = onBack,
                onUndo = viewModel::undo,
                canUndo = viewModel.canUndo(),
                onReset = viewModel::reset,
                onComparePress = { showComparison = true },
                onCompareRelease = { showComparison = false },
                onSave = { showSaveDialog = true },
                onToggleControls = { showAdvancedControls = !showAdvancedControls },
                showAdvancedControls = showAdvancedControls
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ═══════════════════════════════════════════════════
            // 🖼️ IMAGE DISPLAY AREA - كبيرة وفخمة
            // ═══════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.45f) // تم تقليل النسبة لتظهر الاعدادات
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Slate900,
                                Slate800
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                val displayBitmap = if (showComparison) {
                    editState.originalBitmap
                } else {
                    editState.editedBitmap
                }

                displayBitmap?.let { bitmap ->
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = Purple500.copy(alpha = 0.3f)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Slate800
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Editing Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    )
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(0.5f, 3f)

                                            val maxOffsetX = (size.width * (scale - 1)) / 2
                                            val maxOffsetY = (size.height * (scale - 1)) / 2

                                            offsetX =
                                                (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                            offsetY =
                                                (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                        }
                                    },
                                contentScale = ContentScale.Fit
                            )

                            // Zoom Indicator
                            if (scale != 1f) {
                                Surface(
                                    color = Slate800.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${(scale * 100).toInt()}%",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Loading Indicator
                if (editState.isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = Purple500,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري المعالجة...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Comparison Banner
                androidx.compose.animation.AnimatedVisibility(
                    visible = showComparison,
                    enter = fadeIn(animationSpec = tween(300)) +
                            slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut(animationSpec = tween(300)) +
                            slideOutVertically(targetOffsetY = { -it })
                ) {
                    Surface(
                        color = Blue500,
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "الصورة الأصلية",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════
            // 🎛️ CONTROLS PANEL - منظمة وقابلة للتمرير
            // ═══════════════════════════════════════════════════
            AnimatedVisibility(
                visible = showAdvancedControls,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Slate800
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Quick Actions Bar
                        QuickActionsBar(
                            brightness = editState.brightness,
                            contrast = editState.contrast,
                            saturation = editState.saturation,
                            onBrightnessChange = viewModel::updateBrightness,
                            onContrastChange = viewModel::updateContrast,
                            onSaturationChange = viewModel::updateSaturation
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Filters Section
                        FiltersSection(
                            filters = Filter.FILTERS,
                            selectedFilter = editState.selectedFilter,
                            originalBitmap = editState.originalBitmap,
                            onFilterSelected = viewModel::selectFilter,
                            filterIntensity = editState.filterIntensity,
                            onIntensityChange = viewModel::updateFilterIntensity
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(
                            color = Slate700,
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Advanced Adjustments Section
                        AdvancedAdjustmentsSection(
                            brightness = editState.brightness,
                            contrast = editState.contrast,
                            saturation = editState.saturation,
                            warmth = editState.warmth,
                            sharpen = editState.sharpen,
                            onBrightnessChange = viewModel::updateBrightness,
                            onContrastChange = viewModel::updateContrast,
                            onSaturationChange = viewModel::updateSaturation,
                            onWarmthChange = viewModel::updateWarmth,
                            onSharpenChange = viewModel::updateSharpen
                        )
                    }
                }
            }

            // Mini Controls when collapsed
            AnimatedVisibility(
                visible = !showAdvancedControls,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = Slate800,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "إظهار التحكم",
                            tint = Slate300,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "المزيد من الإعدادات",
                            color = Slate300,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "اضغط على زر الإعدادات في الأعلى",
                            color = Slate600,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        AdvancedSaveDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { format, quality ->
                editState.editedBitmap?.let { bitmap ->
                    onSave(bitmap)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "✅ تم حفظ الصورة بنجاح!",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                showSaveDialog = false
            }
        )
    }
}

// ═══════════════════════════════════════════════════
// 📱 TOP BAR - شريط علوي فخم
// ═══════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
    onBack: () -> Unit,
    onUndo: () -> Unit,
    canUndo: Boolean,
    onReset: () -> Unit,
    onComparePress: () -> Unit,
    onCompareRelease: () -> Unit,
    onSave: () -> Unit,
    onToggleControls: () -> Unit,
    showAdvancedControls: Boolean
) {
    var isComparing by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "تعديل الصورة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(4.dp)
            ) {
                Surface(
                    color = Slate700.copy(alpha = 0.5f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "رجوع",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        actions = {
            // Toggle Controls Button
            IconButton(
                onClick = onToggleControls,
                modifier = Modifier.padding(4.dp)
            ) {
                Surface(
                    color = if (showAdvancedControls) Purple500.copy(alpha = 0.2f) else Slate700.copy(
                        alpha = 0.5f
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (showAdvancedControls) Icons.Default.Close else Icons.Default.Tune,
                            contentDescription = "تبديل التحكم",
                            tint = if (showAdvancedControls) Purple500 else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Undo Button
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.padding(4.dp)
            ) {
                Surface(
                    color = if (canUndo) Slate700.copy(alpha = 0.5f) else Slate700.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تراجع",
                            tint = if (canUndo) Color.White else Slate600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Compare Button
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(4.dp)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { change ->
                                    if (change.pressed && !isComparing) {
                                        isComparing = true
                                        onComparePress()
                                    } else if (!change.pressed && isComparing) {
                                        isComparing = false
                                        onCompareRelease()
                                    }
                                }
                            }
                        }
                    }
            ) {
                Surface(
                    color = if (isComparing) Blue500.copy(alpha = 0.2f) else Slate700.copy(alpha = 0.5f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "مقارنة",
                            tint = if (isComparing) Blue500 else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Save Button
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(40.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Purple500, Pink500)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "حفظ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Slate800
        )
    )
}

// ═══════════════════════════════════════════════════
// ⚡ QUICK ACTIONS BAR - تحكم سريع
// ═══════════════════════════════════════════════════
@Composable
fun QuickActionsBar(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate700.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "تحكم سريع",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Filter,
                    contentDescription = null,
                    tint = Slate300,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickSlider(
                    label = "سطوع",
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    valueRange = 0f..2f,
                    modifier = Modifier.weight(1f)
                )

                QuickSlider(
                    label = "تباين",
                    value = contrast,
                    onValueChange = onContrastChange,
                    valueRange = 0f..2f,
                    modifier = Modifier.weight(1f)
                )

                QuickSlider(
                    label = "تشبع",
                    value = saturation,
                    onValueChange = onSaturationChange,
                    valueRange = 0f..2f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Slate300,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${((value - valueRange.start) * 50).toInt()}%",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Purple500,
                inactiveTrackColor = Slate600
            ),
            modifier = Modifier.height(32.dp)
        )
    }
}

// ═══════════════════════════════════════════════════
// 🎨 FILTERS SECTION - قسم الفلاتر الفخم
// ═══════════════════════════════════════════════════
@Composable
fun FiltersSection(
    filters: List<Filter>,
    selectedFilter: Filter,
    originalBitmap: Bitmap?,
    onFilterSelected: (Filter) -> Unit,
    filterIntensity: Float,
    onIntensityChange: (Float) -> Unit
) {
    Column {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Surface(
                color = Purple500.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Purple500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "الفلاتر",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${filters.size} فلتر",
                color = Slate300,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Filters Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(filters) { filter ->
                FilterItem(
                    filter = filter,
                    originalBitmap = originalBitmap,
                    isSelected = selectedFilter.id == filter.id,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }

        // Filter Intensity Slider
        AnimatedVisibility(
            visible = selectedFilter.id != "none",
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Slate700.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = Slate300,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "شدة الفلتر: ${selectedFilter.name}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Slate300,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                color = Purple500,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${(filterIntensity * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Slider(
                            value = filterIntensity,
                            onValueChange = onIntensityChange,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Purple500,
                                inactiveTrackColor = Slate600
                            ),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// ⚙️ ADVANCED ADJUSTMENTS SECTION - قسم التعديلات المتقدمة
// ═══════════════════════════════════════════════════
@Composable
fun AdvancedAdjustmentsSection(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    warmth: Float,
    sharpen: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onWarmthChange: (Float) -> Unit,
    onSharpenChange: (Float) -> Unit
) {
    Column {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Surface(
                color = Pink500.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Pink500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "التعديلات المتقدمة",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Sliders Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AdjustmentSlider(
                label = "السطوع",
                value = brightness,
                onValueChange = onBrightnessChange,
                valueRange = 0f..2f,
                displayValue = "${((brightness - 1) * 100).toInt()}",
                icon = Icons.Default.Star,
                iconColor = Purple500
            )

            AdjustmentSlider(
                label = "التباين",
                value = contrast,
                onValueChange = onContrastChange,
                valueRange = 0f..2f,
                displayValue = "${((contrast - 1) * 100).toInt()}",
                icon = Icons.Default.Settings,
                iconColor = Blue500
            )

            AdjustmentSlider(
                label = "التشبع",
                value = saturation,
                onValueChange = onSaturationChange,
                valueRange = 0f..2f,
                displayValue = "${((saturation - 1) * 100).toInt()}",
                icon = Icons.Default.Filter,
                iconColor = Pink500
            )

            AdjustmentSlider(
                label = "الدفء",
                value = warmth,
                onValueChange = onWarmthChange,
                valueRange = -50f..50f,
                displayValue = "${warmth.toInt()}°",
                icon = Icons.Default.Face,
                iconColor = Color(0xFFFF6B35),
            )

            AdjustmentSlider(
                label = "الحدة",
                value = sharpen,
                onValueChange = onSharpenChange,
                valueRange = 0f..1f,
                displayValue = "${(sharpen * 100).toInt()}%",
                icon = Icons.Default.Search,
                iconColor = Color(0xFF00D4AA)
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// 💾 SAVE DIALOG - فخم ومتقدم
// ═══════════════════════════════════════════════════
@Composable
fun AdvancedSaveDialog(
    onDismiss: () -> Unit,
    onSave: (ImageExporter.ExportFormat, Int) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ImageExporter.ExportFormat.JPEG) }
    var selectedQuality by remember { mutableStateOf(95) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Purple500.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            tint = Purple500,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "حفظ الصورة",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "اختر الإعدادات المناسبة",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            Column {
                // Format Selection
                Text(
                    text = "صيغة الملف",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate800,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "اختر صيغة الملف المناسبة للصورة",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ImageExporter.ExportFormat.values().forEach { format ->
                        FormatChip(
                            format = format,
                            isSelected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Quality Selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "جودة الصورة",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate800,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تؤثر الجودة على حجم الملف",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600,
                            fontSize = 13.sp
                        )
                    }

                    Surface(
                        color = Purple500.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${selectedQuality}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = Purple500,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = selectedQuality.toFloat(),
                    onValueChange = { selectedQuality = it.toInt() },
                    valueRange = 50f..100f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = Purple500,
                        activeTrackColor = Purple500,
                        inactiveTrackColor = Slate200
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quality presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "اقتصادية" to 60,
                        "متوسطة" to 80,
                        "عالية" to 95,
                        "الأفضل" to 100
                    ).forEach { (label, quality) ->
                        QualityChip(
                            label = label,
                            quality = quality,
                            isSelected = selectedQuality == quality,
                            onClick = { selectedQuality = quality }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedFormat, selectedQuality) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Purple500, Pink500)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "حفظ الصورة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Slate600
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إلغاء", fontWeight = FontWeight.Medium)
            }
        },
        containerColor = Color.White,
        tonalElevation = 24.dp,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun FormatChip(
    format: ImageExporter.ExportFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(70.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Purple500 else Slate100
        ),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            Slate200
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = format.extension.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) Color.White else Slate900,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (format) {
                    ImageExporter.ExportFormat.JPEG -> "للصور"
                    ImageExporter.ExportFormat.PNG -> "شفافية"
                    ImageExporter.ExportFormat.WEBP -> "حديثة"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else Slate600,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun QualityChip(
    label: String,
    quality: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Purple500.copy(alpha = 0.1f) else Slate100,
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            1.dp,
            Purple500
        ) else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Purple500 else Slate700,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}