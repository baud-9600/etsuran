package de.asdfghasdfgh.etsuran.view.viewControllers

import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.view.Album
import de.asdfghasdfgh.etsuran.view.compose.BrowserTopBar
import de.asdfghasdfgh.etsuran.view.compose.ComposedSideTabBar
import de.asdfghasdfgh.etsuran.view.compose.ComposedToolbar
import de.asdfghasdfgh.etsuran.view.compose.MyTheme
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.AudioOnly
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.BoldFont
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.Desktop
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.PageInfo
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.Refresh
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.Settings
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.ChatWithWeb
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.PageAi
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.Touch
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.ChatWithWeb
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.PageAi
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.TouchDirectionLeftRight
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.ChatWithWeb
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.PageAi
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.TouchDirectionUpDown
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.Title
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarAction.Tts
import de.asdfghasdfgh.etsuran.view.toolbaricons.ToolbarActionInfo
import de.asdfghasdfgh.etsuran.viewmodel.TtsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ComposeToolbarViewController(
    private val composeView: ComposeView,
    private val topBarView: ComposeView,
    private val sideTabBarView: ComposeView,
    private val albums: MutableState<List<Album>>,
    private val ttsViewModel: TtsViewModel,
    private val onIconClick: (ToolbarAction) -> Unit,
    private val onIconLongClick: (ToolbarAction) -> Unit,
    onTabClick: (Album) -> Unit,
    onTabLongClick: (Album) -> Unit,
    private val isAudioOnlyMode: () -> Boolean = { false },
) : KoinComponent {
    private val config: ConfigManager by inject()

    private var isLoading: Boolean = false

    var isReader: Boolean = false
        private set

    // State previously held by ToolbarComposeView
    var toolbarActionInfoList: List<ToolbarActionInfo> by mutableStateOf(emptyList())
    var shouldShowTabs by mutableStateOf(false)
    var title by mutableStateOf("")
    var address by mutableStateOf("")
    var tabCount by mutableStateOf("")
    var pageInfo by mutableStateOf("")
    var isIncognito by mutableStateOf(false)
    var isVertical by mutableStateOf(config.ui.isVerticalToolbar)
    var isToolbarOnRight by mutableStateOf(config.ui.toolbarPosition == de.asdfghasdfgh.etsuran.preference.ToolbarPosition.Right)
    var isSideTabBarOnTop by mutableStateOf(config.tab.sideTabBarOnTop)
    var albumFocusIndex = mutableStateOf(0)

    private val onTabClick: (Album) -> Unit = onTabClick
    private val onTabLongClick: (Album) -> Unit = onTabLongClick

    // Cached: updatePageInfo runs on every scroll event.
    private var hasPageInfoIcon = false

    init {
        val iconEnums = currentToolbarActions()
        toolbarActionInfoList = iconEnums.toToolbarActionInfoList()
        hasPageInfoIcon = iconEnums.contains(PageInfo)
        shouldShowTabs = config.tab.shouldShowTabBar

        composeView.setContent {
            MyTheme {
                ComposedToolbar(
                    showTabs = shouldShowTabs && !isVertical,
                    toolbarActionInfoList,
                    title = title,
                    tabCount = tabCount,
                    pageInfo = pageInfo,
                    isIncognito = isIncognito,
                    isVertical = isVertical,
                    isToolbarOnRight = isToolbarOnRight,
                    onIconClick = onIconClick,
                    onIconLongClick = onIconLongClick,
                    albumList = albums,
                    albumFocusIndex = albumFocusIndex,
                    onAlbumClick = this.onTabClick,
                    onAlbumLongClick = this.onTabLongClick,
                )
            }
        }

        topBarView.setContent {
            MyTheme {
                BrowserTopBar(
                    address = address,
                    onAddressClick = { onIconClick(Title) },
                    onMenuClick = { onIconClick(Settings) },
                )
            }
        }

        // The vertical toolbar column is too narrow for readable tab titles, so in that
        // mode the strip lives in its own view that the activity anchors across the top
        // of the content. Placement and visibility are ViewUnit's job; this only
        // supplies the content.
        sideTabBarView.setContent {
            MyTheme {
                ComposedSideTabBar(
                    albumList = albums,
                    albumFocusIndex = albumFocusIndex,
                    isOnTop = isSideTabBarOnTop,
                    onAlbumClick = this.onTabClick,
                    onAlbumLongClick = this.onTabLongClick,
                    onIconClick = onIconClick,
                    onIconLongClick = onIconLongClick,
                )
            }
        }
    }

    fun showTabbar(shouldShow: Boolean) {
        shouldShowTabs = shouldShow
    }

    fun isDisplayed(): Boolean = composeView.visibility == VISIBLE

    fun show() = toggleIconsOnOmnibox(true)

    fun hide() = toggleIconsOnOmnibox(false)

    fun updateTabCount(text: String) {
        tabCount = text
    }

    fun updatePageInfo(text: String) {
        if (hasPageInfoIcon) pageInfo = text
    }

    fun updateRefresh(isLoadingWeb: Boolean) {
        if (isLoadingWeb == isLoading) return

        isLoading = isLoadingWeb
        updateIcons()
    }

    fun setEpubReaderMode() {
        isReader = true
        updateIcons()
    }

    fun updateIcons(actionToUpdate: ToolbarAction? = null) {
        val iconEnums = currentToolbarActions()
        // only update specific icon is specified.
        if (actionToUpdate != null && !iconEnums.contains(actionToUpdate)) {
            return
        }

        toolbarActionInfoList = iconEnums.toToolbarActionInfoList()
        hasPageInfoIcon = iconEnums.contains(PageInfo)
        isIncognito = config.isIncognitoMode
        isVertical = config.ui.isVerticalToolbar
        isToolbarOnRight = config.ui.toolbarPosition == de.asdfghasdfgh.etsuran.preference.ToolbarPosition.Right
        isSideTabBarOnTop = config.tab.sideTabBarOnTop
    }

    /**
     * The bottom toolbar keeps navigation only. The page title and Settings gear
     * live in the MMD top bar, so they are not repeated below. When the user moves
     * the toolbar to the top or side, the top bar is hidden and both come back.
     */
    private fun currentToolbarActions(): List<ToolbarAction> {
        val raw = (if (isReader) config.ui.readerToolbarActions else config.ui.toolbarActions)
            .filterNot { it == ChatWithWeb || it == PageAi }
        if (isReader || config.ui.toolbarPosition != de.asdfghasdfgh.etsuran.preference.ToolbarPosition.Bottom) {
            return raw
        }
        return raw.filterNot { it == Title || it == Settings }
    }

    private fun List<ToolbarAction>.toToolbarActionInfoList(): List<ToolbarActionInfo> {
        return this.map { toolbarAction ->
            when (toolbarAction) {
                BoldFont -> ToolbarActionInfo(toolbarAction, config.display.boldFontStyle)
                Refresh -> ToolbarActionInfo(toolbarAction, isLoading)
                Desktop -> ToolbarActionInfo(toolbarAction, config.browser.desktop)
                Touch -> ToolbarActionInfo(toolbarAction, config.touch.enableTouchTurn)
                TouchDirectionUpDown -> ToolbarActionInfo(
                    toolbarAction,
                    config.touch.switchTouchAreaAction
                )

                TouchDirectionLeftRight -> ToolbarActionInfo(
                    toolbarAction,
                    config.touch.switchTouchAreaAction
                )

                Tts -> ToolbarActionInfo(toolbarAction, ttsViewModel.isReading())
                AudioOnly -> ToolbarActionInfo(toolbarAction, isAudioOnlyMode())
                else -> ToolbarActionInfo(toolbarAction, false)
            }
        }
    }

    private fun toggleIconsOnOmnibox(shouldShow: Boolean) {
        composeView.visibility = if (shouldShow) VISIBLE else INVISIBLE
    }

    fun updateTitle(title: String) {
        this.title = title
    }

    fun updateAddress(url: String) {
        address = url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .takeUnless { it.startsWith("etsuran://") }
            .orEmpty()
    }

    fun updateFocusIndex(index: Int) {
        albumFocusIndex.value = index
    }
}
