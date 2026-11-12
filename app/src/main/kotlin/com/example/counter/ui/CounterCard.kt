package com.example.counter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.counter.data.CounterItem
import kotlinx.coroutines.delay

/** Per-card Monet accent, cycled so several counters stay visually distinct. */
internal data class CounterAccent(
    val container: Color,
    val content: Color,
    val strong: Color,
    val onStrong: Color,
)

@Composable
internal fun CounterCard(
    item: CounterItem,
    accent: CounterAccent,
    compact: Boolean,
    onStep: (Int) -> Unit,
    onReset: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (compact) {
        CompactCounterCard(item, accent, onStep, onRename, modifier)
    } else {
        ComfortableCounterCard(item, accent, onStep, onReset, onRename, onDelete, modifier)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ComfortableCounterCard(
    item: CounterItem,
    accent: CounterAccent,
    onStep: (Int) -> Unit,
    onReset: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    var menuOpen by remember { mutableStateOf(false) }

    // The tally pops when it changes, then settles back on the theme's spatial spring.
    val pop = remember { Animatable(1f) }
    var primed by remember { mutableStateOf(false) }
    LaunchedEffect(item.value) {
        if (!primed) {
            primed = true
            return@LaunchedEffect
        }
        pop.snapTo(0.86f)
        pop.animateTo(1f, motion.defaultSpatialSpec())
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = accent.container, contentColor = accent.content),
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.pinned) {
                    PinGlyph(color = accent.content.copy(alpha = 0.7f))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = accent.content.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        // keep the row-wide ink well, just give it rounded corners
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = onRename)
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                )
                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { menuOpen = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        DotsGlyph(color = accent.content.copy(alpha = 0.7f))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("重命名") },
                            onClick = {
                                menuOpen = false
                                onRename()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("归零") },
                            onClick = {
                                menuOpen = false
                                onReset()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            },
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = item.value,
                transitionSpec = {
                    val spatial = motion.defaultSpatialSpec<IntOffset>()
                    val effects = motion.defaultEffectsSpec<Float>()
                    val rising = targetState > initialState
                    (slideInVertically(spatial) { height -> if (rising) height / 3 else -height / 3 } +
                        fadeIn(effects)) togetherWith
                        (slideOutVertically(spatial) { height -> if (rising) -height / 3 else height / 3 } +
                            fadeOut(effects))
                },
                label = "count",
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = pop.value
                        scaleY = pop.value
                    },
            ) { value ->
                Text(
                    text = value.toString(),
                    style = countStyle(value),
                    fontWeight = FontWeight.Medium,
                    color = accent.content,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepButton(
                    plus = false,
                    containerColor = accent.content.copy(alpha = 0.14f),
                    glyphColor = accent.content,
                    onClick = { onStep(-1) },
                    modifier = Modifier.weight(1f),
                )
                StepButton(
                    plus = true,
                    containerColor = accent.strong,
                    glyphColor = accent.onStrong,
                    onClick = { onStep(1) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** One dense row: label, value, then the step buttons at the end of that same row. */
@Composable
private fun CompactCounterCard(
    item: CounterItem,
    accent: CounterAccent,
    onStep: (Int) -> Unit,
    onRename: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = accent.container, contentColor = accent.content),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (item.pinned) {
                PinGlyph(color = accent.content.copy(alpha = 0.7f))
            }
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleSmall,
                color = accent.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onRename)
                    .padding(vertical = 12.dp, horizontal = 8.dp),
            )
            Text(
                text = item.value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = accent.content,
                maxLines = 1,
                softWrap = false,
            )
            StepButton(
                plus = false,
                containerColor = accent.content.copy(alpha = 0.14f),
                glyphColor = accent.content,
                onClick = { onStep(-1) },
                height = 44.dp,
                glyphSize = 18.dp,
                modifier = Modifier.width(52.dp),
            )
            StepButton(
                plus = true,
                containerColor = accent.strong,
                glyphColor = accent.onStrong,
                onClick = { onStep(1) },
                height = 44.dp,
                glyphSize = 18.dp,
                modifier = Modifier.width(52.dp),
            )
        }
    }
}

/** Keeps big tallies inside the card instead of letting them run off the edge. */
@Composable
private fun countStyle(value: Int) = with(MaterialTheme.typography) {
    when (value.toString().length) {
        0, 1, 2, 3 -> displayMedium
        4, 5 -> displaySmall
        6, 7 -> headlineLarge
        else -> headlineMedium
    }
}

/**
 * Rounded step target. A tap steps once, holding keeps stepping (that is how you actually
 * count 200 reps without 200 taps).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StepButton(
    plus: Boolean,
    containerColor: Color,
    glyphColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    glyphSize: Dp = 26.dp,
) {
    val motion = MaterialTheme.motionScheme
    val haptics = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = motion.defaultSpatialSpec(),
        label = "stepScale",
    )
    val tint by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "stepTint",
    )

    LaunchedEffect(pressed) {
        if (!pressed) return@LaunchedEffect
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
        delay(400) // hold before the repeat kicks in
        while (true) {
            onClick()
            delay(80)
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(containerColor)
            .background(glyphColor.copy(alpha = 0.12f * tint))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    try {
                        waitForUpOrCancellation()
                    } finally {
                        pressed = false
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        StepGlyph(plus = plus, color = glyphColor, size = glyphSize)
    }
}