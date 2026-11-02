@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package de.asdfghasdfgh.etsuran.view.compose

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Point
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.database.BookmarkManager
import de.asdfghasdfgh.etsuran.database.Record
import de.asdfghasdfgh.etsuran.view.Album
import de.asdfghasdfgh.etsuran.view.dialog.compose.ActionIcon
import de.asdfghasdfgh.etsuran.view.dialog.compose.HorizontalSeparator
import kotlin.math.max

@Composable
fun HistoryAndTabs(
    bookmarkManager: BookmarkManager? = null,
    isHistoryOpen: Boolean = false,
    shouldShowTwoColumns: Boolean = false,
    shouldReverseHistory: Boolean = false,

    albumList: MutableState<List<Album>>,
    albumFocusIndex: MutableState<Int>,
    onTabIconClick: () -> Unit,
    onTabClick: (Album) -> Unit,
    onTabLongClick: (Album) -> Unit,

    records: List<Record>,
    onHistoryIconClick: () -> Unit,
    onHistoryItemClick: (Record) -> Unit,
    onHistoryItemLongClick: (Record, Point) -> Unit,

    addIncognitoTab: () -> Unit,
    addTab: () -> Unit,
    closePanel: () -> Unit,
    onDeleteAction: () -> Unit,
    onCloseAllTabs: () -> Unit,
    launchNewBrowserAction: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isBarOnTop = !shouldReverseHistory
    Column(
        Modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { closePanel() },
    ) {
        MmdTopAppBar(
            title = stringResource(if (isHistoryOpen) R.string.history else R.string.tabs_title),
            navigationIcon = {
                IconButton(onClick = closePanel) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colors.onBackground,
                    )
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = if (isBarOnTop) Arrangement.Top else Arrangement.Bottom,
        ) {
        if (isBarOnTop) {
            ButtonBarLayout(
                isHistoryOpen = isHistoryOpen,
                addIncognitoTab = addIncognitoTab,
                addTab = addTab,
                closePanel = closePanel,
                toggleHistory = { onHistoryIconClick() },
                togglePreview = { onTabIconClick() },
                onDeleteAction = onDeleteAction,
                onCloseAllTabs = onCloseAllTabs,
                launchNewBrowserAction = launchNewBrowserAction,
            )
            HorizontalSeparator()
        }
        MainContent(
            modifier = Modifier.Companion
                .weight(1f, false)
                .background(MaterialTheme.colors.background)
                .horizontalBorder(
                    drawTop = !isBarOnTop,
                    drawBottom = isBarOnTop,
                    MaterialTheme.colors.primary
                ),
            isHistoryOpen,
            shouldShowTwoColumns,
            shouldReverseHistory,
            albumList,
            albumFocusIndex,
            onTabClick,
            onTabLongClick,
            bookmarkManager,
            records,
            onHistoryItemClick,
            onHistoryItemLongClick
        )
        if (!isBarOnTop) {
            HorizontalSeparator()
            ButtonBarLayout(
                isHistoryOpen = isHistoryOpen,
                addIncognitoTab = addIncognitoTab,
                addTab = addTab,
                closePanel = closePanel,
                toggleHistory = { onHistoryIconClick() },
                togglePreview = { onTabIconClick() },
                onDeleteAction = onDeleteAction,
                onCloseAllTabs = onCloseAllTabs,
                launchNewBrowserAction = launchNewBrowserAction,
            )
        }
        }
    }
}

@Composable
private fun MainContent(
    modifier: Modifier,
    isHistoryOpen: Boolean,
    shouldShowTwoColumns: Boolean,
    shouldReverse: Boolean,
    albumList: MutableState<List<Album>>,
    albumFocusIndex: MutableState<Int>,
    onTabClick: (Album) -> Unit,
    onTabLongClick: (Album) -> Unit,
    bookmarkManager: BookmarkManager?,
    records: List<Record>,
    onHistoryItemClick: (Record) -> Unit,
    onHistoryItemLongClick: (Record, Point) -> Unit,
) {
    if (!isHistoryOpen) {
        PreviewTabs(
            modifier = modifier,
            shouldShowTwoColumns = shouldShowTwoColumns,
            albumList = albumList.value,
            albumFocusIndex = albumFocusIndex,
            onClick = onTabClick,
            closeAction = {
                onTabLongClick.invoke(it)
            }
        )
    }
    if (isHistoryOpen) {
        BrowseHistoryList(
            modifier = modifier,
            bookmarkManager = bookmarkManager,
            records = records,
            shouldReverse = shouldReverse,
            shouldShowTwoColumns = shouldShowTwoColumns,
            onClick = onHistoryItemClick,
            onLongClick = onHistoryItemLongClick,
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewTabs(
    modifier: Modifier = Modifier,
    shouldShowTwoColumns: Boolean = false,
    albumList: List<Album>,
    albumFocusIndex: MutableState<Int>,
    onClick: (Album) -> Unit,
    closeAction: (Album) -> Unit,
    showHorizontal: Boolean = false,
) {
    if (showHorizontal) {
        val maxItemWidth = 200
        // Real constraints, not Configuration screen dims: at targetSdk 35+
        // screenWidthDp includes the system bars and overshoots the bar width.
        BoxWithConstraints(modifier = modifier) {
            val barWidth = maxWidth.value.toInt() - 50 // 50 is the plus button width
            val itemWidth =
                if (albumList.size * 200 > barWidth) max(
                    barWidth / albumList.size,
                    80
                )
                else maxItemWidth

            val listState = rememberLazyListState()
            LazyRow(state = listState) {
                items(albumList.size, key = { albumList[it].id }) { index ->
                    val album = albumList[index]
                    val interactionSource = remember { MutableInteractionSource() }
                    // derivedStateOf: a focus change invalidates only the two
                    // affected items instead of every visible tab.
                    val isFocused by remember(index) {
                        derivedStateOf { index == albumFocusIndex.value }
                    }
                    TabItem(
                        modifier = Modifier
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onClick(album) },
                                onLongClick = { closeAction(album) }
                            )
                            .width(itemWidth.dp),
                        showCloseButton = false,
                        isFocused = isFocused,
                        album = album
                    ) { closeAction(album) }
                }
            }
            // snapshotFlow keeps the focus-index read out of composition scope.
            LaunchedEffect(Unit) {
                snapshotFlow { albumFocusIndex.value }.collect { focusIndex ->
                    if (listState.layoutInfo.visibleItemsInfo.none { it.index == focusIndex }) {
                        listState.scrollToItem(focusIndex)
                    }
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        Row(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
            ) {
                items(albumList.size, key = { albumList[it].id }) { index ->
                    val album = albumList[index]
                    val isFocused by remember(index) {
                        derivedStateOf { index == albumFocusIndex.value }
                    }
                    TabListItem(
                        album = album,
                        isFocused = isFocused,
                        onClick = { onClick(album) },
                        onLongClick = { closeAction(album) },
                        closeAction = { closeAction(album) },
                    )
                }
            }
            MmdScrollbar(listState)
        }
    }
}

/** One MMD list row in the tab overview: icon, title, URL, close. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabListItem(
    album: Album,
    isFocused: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    closeAction: () -> Unit,
) {
    val tabInfo = album.toTabInfo()
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (tabInfo.favicon != null) {
                Image(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 12.dp),
                    bitmap = tabInfo.favicon.asImageBitmap(),
                    contentDescription = null,
                )
            } else {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 12.dp),
                    imageVector = if (tabInfo.isTranslatePage) Icons.Outlined.Translate
                    else Icons.Outlined.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tabInfo.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colors.onBackground,
                )
                Text(
                    text = tabInfo.url,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.65f),
                )
            }
            IconButton(onClick = closeAction, modifier = Modifier.size(40.dp)) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.menu_closeTab),
                    tint = MaterialTheme.colors.onBackground,
                )
            }
        }
        Divider(
            modifier = Modifier.padding(start = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colors.onBackground,
        )
    }
}

@Composable
private fun TabItem(
    modifier: Modifier,
    album: Album,
    showCloseButton: Boolean = true,
    isFocused: Boolean = false,
    closeAction: () -> Unit,
) {
    val tabInfo = album.toTabInfo()
    val borderWidth = if (isFocused) 1.dp else -1.dp

    Row(
        modifier = modifier
            .height(54.dp)
            .padding(4.dp)
            .ebItemFrame(borderWidth)
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (tabInfo.isTranslatePage) {
            Icon(
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 5.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_translate),
                contentDescription = null,
                tint = MaterialTheme.colors.onBackground
            )
        } else if (tabInfo.favicon != null) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(36.dp)
                    .padding(start = 2.dp, end = 5.dp),
                bitmap = tabInfo.favicon.asImageBitmap(),
                contentDescription = null,
            )
        } else {
            Icon(
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 5.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_earth),
                contentDescription = null,
                tint = MaterialTheme.colors.onBackground
            )
        }

        Text(
            modifier = Modifier
                .weight(1F)
                .align(Alignment.CenterVertically),
            text = tabInfo.title,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = MaterialTheme.colors.onBackground,
        )

        if (showCloseButton || isFocused) {
            ActionIcon(
                modifier = Modifier.align(Alignment.CenterVertically),
                iconResId = R.drawable.icon_close,
                action = closeAction,
            )
        }
    }
}

data class TabInfo(
    val url: String,
    val title: String,
    val favicon: Bitmap? = null,
    val isTranslatePage: Boolean,
)

@Composable
fun ButtonBarLayout(
    isHistoryOpen: Boolean = true,
    addIncognitoTab: () -> Unit,
    toggleHistory: () -> Unit,
    togglePreview: () -> Unit,
    addTab: () -> Unit,
    closePanel: () -> Unit,
    onDeleteAction: () -> Unit,
    onCloseAllTabs: () -> Unit,
    launchNewBrowserAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colors.background)
            .clickable(enabled = false) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (isHistoryOpen) {
            ButtonIcon(Icons.Outlined.Delete, onDeleteAction, modifier = Modifier.weight(1f))
        } else {
            ButtonIcon(Icons.Outlined.DeleteSweep, onCloseAllTabs, modifier = Modifier.weight(1f))
        }
        ButtonIcon(Icons.Outlined.VisibilityOff, addIncognitoTab, modifier = Modifier.weight(1f))
        ButtonIcon(
            imageVector = if (isHistoryOpen) Icons.Filled.History else Icons.Outlined.History,
            onClick = toggleHistory,
            modifier = Modifier.weight(1f),
        )
        ButtonIcon(
            imageVector = if (!isHistoryOpen) Icons.Filled.Tab else Icons.Outlined.Tab,
            onClick = togglePreview,
            modifier = Modifier.weight(1f),
        )
        ButtonIcon(
            imageVector = Icons.Outlined.Add,
            onClick = addTab,
            onLongClick = launchNewBrowserAction,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ButtonIcon(
    imageVector: ImageVector,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier.width(46.dp),
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colors.onBackground,
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun PreviewHistoryAndTabs() {
    val recordList = listOf(
        Record(
            title = "Hello aaa aaa aaa aa aa aaa aa a aa a a a aa a a a a a a a a a aa a a ",
            url = "123",
            time = System.currentTimeMillis()
        ),
        Record(title = "Hello 2", url = "123", time = System.currentTimeMillis()),
        Record(title = "Hello 3", url = "123", time = System.currentTimeMillis()),
    )

    val albumList = mutableStateOf(listOf<Album>())

    HistoryAndTabs(
        isHistoryOpen = true,
        shouldShowTwoColumns = false,
        shouldReverseHistory = false,
        albumList = albumList,
        albumFocusIndex = mutableStateOf(0),
        onTabIconClick = {},
        onTabClick = {},
        onTabLongClick = {},
        records = recordList,
        onHistoryIconClick = {},
        onHistoryItemClick = {},
        onHistoryItemLongClick = { _, _ -> },

        addIncognitoTab = {},
        addTab = {},
        closePanel = {},
        onDeleteAction = {},
        launchNewBrowserAction = {},
        onCloseAllTabs = {},
    )
}

private fun Album.toTabInfo(): TabInfo =
    TabInfo(
        title = this.albumTitle,
        url = this.getUrl(),
        favicon = this.bitmap,
        isTranslatePage = this.isTranslatePage
    )

