package de.asdfghasdfgh.etsuran.view.compose

import android.graphics.Bitmap
import android.graphics.Point
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.database.BookmarkManager
import de.asdfghasdfgh.etsuran.database.Record
import de.asdfghasdfgh.etsuran.database.RecordType
import de.asdfghasdfgh.etsuran.view.dialog.compose.toScreenPoint
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowseHistoryList(
    modifier: Modifier,
    records: List<Record>,
    shouldReverse: Boolean,
    shouldShowTwoColumns: Boolean,
    bookmarkManager: BookmarkManager? = null,
    showThumbnailGrid: Boolean = false,
    onClick: (Record) -> Unit,
    onLongClick: (Record, Point) -> Unit,
    onAppendClick: (String) -> Unit = {},
) {
    if (showThumbnailGrid) {
        ThumbnailHistoryGrid(
            modifier = modifier,
            records = records,
            shouldReverse = shouldReverse,
            bookmarkManager = bookmarkManager,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        return
    }

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(if (shouldShowTwoColumns) 2 else 1),
        reverseLayout = shouldReverse
    ) {

        itemsIndexed(records) { index, record ->
            var boxPosition = remember { mutableStateOf(Offset.Zero) }

            RecordItem(
                record = record,
                bitmap = remember(record.url) { bookmarkManager?.findFaviconBitmapBy(record.url) },
                modifier = Modifier
                    .pointerInput(record) {
                        detectTapGestures(
                            onTap = { _ -> onClick(record) },
                            onLongPress = { it ->
                                onLongClick(record, it.toScreenPoint(boxPosition.value))
                            }
                        )
                    }
                    .onGloballyPositioned { boxPosition.value = it.positionOnScreen() },
                onAppendClick = onAppendClick,
            )
            Divider(
                modifier = Modifier.padding(start = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colors.onBackground,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThumbnailHistoryGrid(
    modifier: Modifier,
    records: List<Record>,
    shouldReverse: Boolean,
    bookmarkManager: BookmarkManager? = null,
    onClick: (Record) -> Unit,
    onLongClick: (Record, Point) -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 56.dp),
        reverseLayout = shouldReverse,
    ) {
        itemsIndexed(records) { _, record ->
            val boxPosition = remember { mutableStateOf(Offset.Zero) }
            ThumbnailHistoryItem(
                record = record,
                bitmap = remember(record.url) { bookmarkManager?.findFaviconBitmapBy(record.url) },
                modifier = Modifier
                    .pointerInput(record) {
                        detectTapGestures(
                            onTap = { onClick(record) },
                            onLongPress = { offset ->
                                onLongClick(record, offset.toScreenPoint(boxPosition.value))
                            }
                        )
                    }
                    .onGloballyPositioned { boxPosition.value = it.positionOnScreen() },
            )
        }
    }
}

@Composable
private fun ThumbnailHistoryItem(
    modifier: Modifier,
    record: Record,
    bitmap: Bitmap?,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                modifier = Modifier.size(32.dp),
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
            )
        } else {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_history),
                contentDescription = null,
                tint = MaterialTheme.colors.onBackground,
            )
        }
    }
}

@Composable
private fun RecordItem(
    modifier: Modifier,
    bitmap: Bitmap? = null,
    record: Record,
    onAppendClick: (String) -> Unit = {},
) {
    val timeString =
        if (record.type == RecordType.History) SimpleDateFormat(
            "MM/dd",
            Locale.getDefault()
        ).format(record.time)
        else ""

    val isTypeSuggestion = record.type == RecordType.Suggestion

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        when {
            record.type == RecordType.Bookmark -> {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp)
                        .padding(end = 5.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.icon_bookmark),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground
                )
            }

            isTypeSuggestion -> {
                IconButton(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .padding(0.dp),
                    onClick = { onAppendClick(record.title.orEmpty()) }
                ) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(20.dp),
                        imageVector = Icons.Outlined.ArrowDownward,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }

            bitmap != null -> {
                Image(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp)
                        .padding(end = 5.dp),
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                )
            }

            else -> {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp)
                        .padding(end = 5.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_history),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }
        Column(
            Modifier
                .weight(1F)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = record.title ?: stringResource(R.string.page_untitled),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!isTypeSuggestion) {
                Spacer(modifier = Modifier.height(1.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1F)
                            .align(Alignment.Top),
                        text = record.url,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // alight to end of row
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .align(Alignment.Top),
                        text = timeString,
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.65f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun previewItem() {
    MyTheme {
        RecordItem(
            modifier = Modifier,
            record = Record(
                title = "Hello",
                url = "123ddddddddddddddddddddddddd",
                time = System.currentTimeMillis()
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun previewHistoryList() {
    val list = listOf(
        Record(
            title = "Hello aaa aaa aaa aa aa aaa aa a aa a a a aa a a a a a a a a a aa a a ",
            url = "123",
            time = System.currentTimeMillis()
        ),
        Record(
            title = "Hello 2",
            url = "123 dddddddddddddddddddddddddddddddddddddddd",
            time = System.currentTimeMillis()
        ),
        Record(title = "Hello 3", url = "123", time = System.currentTimeMillis()),
    )
    MyTheme {
        BrowseHistoryList(
            modifier = Modifier,
            records = list,
            shouldReverse = true,
            shouldShowTwoColumns = true,
            onClick = {},
            onLongClick = { _, _ -> })
    }
}
