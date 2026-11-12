package com.example.counter.ui

import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import com.example.counter.R
import com.example.counter.data.CounterItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CounterApp(viewModel: CounterViewModel) {
    val counters = viewModel.counters
    val pinned = counters.filter { it.pinned }
    val rest = counters.filterNot { it.pinned }
    val compact = viewModel.compactLayout
    val anyValue = counters.any { it.value != 0 }
    // The bracketed count is only meaningful once there is something to count.
    val pinnedCountText = if (pinned.isEmpty()) "" else " (${pinned.size}个)"
    val totalCountText = if (counters.isEmpty()) "" else " (${counters.size}个)"
    // Nothing pinned means the pinned tally has nothing to add - drop it and its separator.
    val subtitle = buildString {
        if (pinned.isNotEmpty()) {
            append("置顶计数和: ${viewModel.pinnedSum()}$pinnedCountText│")
        }
        append("总计数和: ${viewModel.totalSum()}$totalCountText")
    }
    var showAddSheet by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<CounterItem?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            letterSpacing = 0.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleLayout) {
                        Box(modifier = Modifier.size(24.dp)) {
                            LayoutGlyph(
                                showCompact = !compact,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(
                        onClick = viewModel::resetAll,
                        enabled = anyValue,
                    ) {
                        Box(modifier = Modifier.size(24.dp)) {
                            ResetGlyph(
                                color = if (anyValue) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                },
                            )
                        }
                    }
                    IconButton(
                        onClick = { confirmDeleteAll = true },
                        enabled = counters.isNotEmpty(),
                    ) {
                        Box(modifier = Modifier.size(24.dp)) {
                            TrashGlyph(
                                color = if (counters.isNotEmpty()) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                shape = RoundedCornerShape(16.dp),
            ) {
                StepGlyph(
                    plus = true,
                    color = LocalContentColor.current,
                    size = 24.dp,
                    strokeRatio = 0.085f,
                )
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = counters.isEmpty(),
            transitionSpec = {
                fadeIn(tween(durationMillis = 180)) togetherWith fadeOut(tween(durationMillis = 120))
            },
            label = "content",
        ) { isEmpty ->
        if (isEmpty) {
            EmptyState(
                onCreate = { showAddSheet = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 4.dp,
                    bottom = innerPadding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 12.dp),
            ) {
                if (pinned.isEmpty()) {
                    counterItems(rest, compact, viewModel, onRename = { renaming = it })
                } else {
                    item(key = "section-pinned") { SectionHeader("置顶") }
                    counterItems(pinned, compact, viewModel, onRename = { renaming = it })
                    if (rest.isNotEmpty()) {
                        item(key = "section-rest") { SectionHeader("未置顶") }
                        counterItems(rest, compact, viewModel, onRename = { renaming = it })
                    }
                }
            }
        }
        }
    }

    if (showAddSheet) {
        AddCounterSheet(
            history = viewModel.history,
            onDismiss = { showAddSheet = false },
            onSubmit = { label -> viewModel.add(label) },
            onForgetHistory = { entry -> viewModel.forgetName(entry) },
        )
    }

    renaming?.let { target ->
        RenameDialog(
            current = target,
            onDismiss = { renaming = null },
            onConfirm = { label ->
                viewModel.rename(target.id, label)
                renaming = null
            },
        )
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("全部删除?") },
            text = { Text("将删除 ${counters.size} 个 Counter 及其计数值, 此操作不可撤销.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDeleteAll = false
                        viewModel.removeAll()
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.counterItems(
    items: List<CounterItem>,
    compact: Boolean,
    viewModel: CounterViewModel,
    onRename: (CounterItem) -> Unit,
) {
    items(items = items, key = { it.id }) { item ->
        SwipeableCounter(
            item = item,
            compact = compact,
            accent = accentFor(item.id),
            onStep = { delta -> viewModel.step(item.id, delta) },
            onReset = { viewModel.reset(item.id) },
            onRename = { onRename(item) },
            onDelete = { viewModel.remove(item.id) },
            onTogglePin = { viewModel.togglePin(item.id) },
            modifier = Modifier.animateItem(
                fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                fadeOutSpec = tween(durationMillis = 140),
                placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            ),
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 0.dp),
    )
}

/**
 * Swipe right to pin (or unpin), swipe left to delete. Only the side being dragged paints a
 * background, so the opposite colour never leaks out at the midline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION") // confirmValueChange is the only way to veto a dismissal and snap back
@Composable
private fun SwipeableCounter(
    item: CounterItem,
    compact: Boolean,
    accent: CounterAccent,
    onStep: (Int) -> Unit,
    onReset: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape: Shape = if (compact) MaterialTheme.shapes.large else MaterialTheme.shapes.extraLarge

    // Pinning must happen once per gesture: re-sorting the list re-settles the drag, which would
    // otherwise toggle the pin back and forth while the finger is still down.
    var pinHandled by remember(item.id) { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * SWIPE_THRESHOLD },
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (!pinHandled) {
                        pinHandled = true
                        onTogglePin()
                    }
                    false // snap back: the row is not going anywhere, it just changes section
                }

                SwipeToDismissBoxValue.EndToStart -> true
                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.pointerInput(item.id) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pinHandled = false
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
        },
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> PinSwipeBackground(pinned = item.pinned, shape = shape)
                SwipeToDismissBoxValue.EndToStart -> DeleteSwipeBackground(shape = shape)
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
    ) {
        CounterCard(
            item = item,
            accent = accent,
            compact = compact,
            onStep = onStep,
            onReset = onReset,
            onRename = onRename,
            onDelete = onDelete,
        )
    }
}

/** Fraction of the row width a drag has to cover before it counts. */
private const val SWIPE_THRESHOLD = 0.85f

@Composable
private fun PinSwipeBackground(pinned: Boolean, shape: Shape) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(scheme.secondaryContainer)
            .padding(start = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PinGlyph(color = scheme.onSecondaryContainer, size = 18.dp)
        Text(
            text = if (pinned) "取消置顶" else "置顶",
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun DeleteSwipeBackground(shape: Shape) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(scheme.errorContainer)
            .padding(end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        Text(
            text = "删除",
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onErrorContainer,
        )
        TrashGlyph(color = scheme.onErrorContainer, size = 18.dp)
    }
}

@Composable
private fun EmptyState(onCreate: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(140.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("0", style = MaterialTheme.typography.displayLarge)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "还没有 Counter",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "可同时新建多个互不影响的 Counter.\n右滑 Counter 可将其置顶, 左滑删除.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onCreate, shape = MaterialTheme.shapes.large) {
                Text("新建 Counter")
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
private fun AddCounterSheet(
    history: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    onForgetHistory: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var editingHistory by remember { mutableStateOf(false) }

    // Slide the sheet down first, then drop it from the composition - removing it straight away
    // is what made closing look like it had no animation.
    fun close(then: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            then()
            onDismiss()
        }
    }

    LaunchedEffect(history.isEmpty()) {
        if (history.isEmpty()) editingHistory = false
    }

    ModalBottomSheet(
        onDismissRequest = { close() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        PinSheetWindowToBottom()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                // A tap on the sheet body (but not on a control) leaves history edit mode.
                .pointerInput(Unit) {
                    detectTapGestures { editingHistory = false }
                }
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            Text("新建 Counter", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名称") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            )
            if (history.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    history.forEach { entry ->
                        HistoryChip(
                            label = entry,
                            editing = editingHistory,
                            onPick = { name = entry },
                            onLongPress = { editingHistory = true },
                            onDelete = { onForgetHistory(entry) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = { close() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("取消")
                }
                Button(
                    onClick = { close { onSubmit(name) } },
                    enabled = name.isNotBlank(),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("添加")
                }
            }
        }
    }
}

/**
 * Stops the sheet's own window from being resized by the keyboard, so the sheet stays anchored to
 * the bottom of the screen and only its content (via imePadding) moves up.
 */
@Composable
private fun PinSheetWindowToBottom() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? DialogWindowProvider)?.window
        val previous = window?.attributes?.softInputMode
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
        onDispose {
            if (window != null && previous != null) {
                window.setSoftInputMode(previous)
            }
        }
    }
}

/** History suggestion: tap to reuse, long press to switch the whole row into delete mode. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryChip(
    label: String,
    editing: Boolean,
    onPick: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(if (editing) scheme.errorContainer else scheme.surfaceContainerHighest)
            .combinedClickable(
                onClick = { if (editing) onDelete() else onPick() },
                onLongClick = onLongPress,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (editing) {
                CloseGlyph(color = scheme.onErrorContainer, size = 14.dp)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (editing) scheme.onErrorContainer else scheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RenameDialog(current: CounterItem, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember(current.id) { mutableStateOf(current.label) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text("重命名") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("名称") },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

/** Stable per counter, so pinning (which reorders the list) does not repaint the colours. */
@Composable
private fun accentFor(id: String): CounterAccent {
    val scheme = MaterialTheme.colorScheme
    return when ((id.hashCode() and 0x7fffffff) % 4) {
        0 -> CounterAccent(
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
            strong = scheme.primary,
            onStrong = scheme.onPrimary,
        )

        1 -> CounterAccent(
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            strong = scheme.secondary,
            onStrong = scheme.onSecondary,
        )

        2 -> CounterAccent(
            container = scheme.tertiaryContainer,
            content = scheme.onTertiaryContainer,
            strong = scheme.tertiary,
            onStrong = scheme.onTertiary,
        )

        else -> CounterAccent(
            container = scheme.surfaceContainerHighest,
            content = scheme.onSurface,
            strong = scheme.primary,
            onStrong = scheme.onPrimary,
        )
    }
}