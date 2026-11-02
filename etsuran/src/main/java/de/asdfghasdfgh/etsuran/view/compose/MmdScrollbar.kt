package de.asdfghasdfgh.etsuran.view.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * MMD's vertical scrollbar: a 24dp chevron at each end, and between them an
 * 8dp outlined track with a black thumb. Tap the track to jump; the chevrons
 * jump to the first/last row. Drawn here rather than taken from LazyColumnMMD
 * because the settings lists are Material 2 and use a plain LazyColumn.
 */
@Composable
fun MmdScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
) {
    val info by remember { derivedStateOf { state.layoutInfo } }
    val total = info.totalItemsCount
    if (total == 0) return

    val ink = MaterialTheme.colors.onBackground
    val paper = MaterialTheme.colors.background
    val scope = rememberCoroutineScope()
    val trackWidth = 8.dp
    val corner = RoundedCornerShape(4.dp)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(40.dp)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(
            onClick = { scope.launch { state.scrollToItem(0) } },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = ink)
        }

        Canvas(
            modifier = Modifier
                .width(trackWidth)
                .weight(1f)
                .border(1.dp, ink, corner)
                .pointerInput(total, info.visibleItemsInfo.size) {
                    detectTapGestures { offset ->
                        val viewport = size.height.toFloat().coerceAtLeast(1f)
                        val index = (offset.y / viewport * (total - 1)).toInt()
                            .coerceIn(0, total - 1)
                        scope.launch { state.scrollToItem(index) }
                    }
                },
        ) {
            val visible = info.visibleItemsInfo.size.coerceAtLeast(1)
            val trackHeight = size.height
            val thumbHeight = max(
                trackHeight * visible / total.toFloat(),
                trackHeight * 0.05f,
            ).coerceIn(0f, trackHeight)
            val maxOffset = (trackHeight - thumbHeight).coerceAtLeast(0f)
            val first = info.visibleItemsInfo.firstOrNull()?.index ?: 0
            val denominator = (total - visible).coerceAtLeast(1)
            val offset = (first.toFloat() / denominator * maxOffset).coerceIn(0f, maxOffset)

            // Track: paper inside the 1dp ink border.
            drawRoundRect(
                color = paper,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(size.width / 2f),
            )
            // Thumb: solid ink while there is somewhere to go, otherwise the track.
            drawRoundRect(
                color = if (visible >= total) paper else ink,
                topLeft = Offset(0f, offset),
                size = Size(size.width, thumbHeight),
                cornerRadius = CornerRadius(size.width / 2f),
            )
        }

        IconButton(
            onClick = { scope.launch { state.scrollToItem(total - 1) } },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = ink)
        }
    }
}
