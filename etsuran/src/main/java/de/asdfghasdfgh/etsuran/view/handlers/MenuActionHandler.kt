package de.asdfghasdfgh.etsuran.view.handlers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.activity.EpubReaderActivity
import de.asdfghasdfgh.etsuran.activity.ToolbarConfigActivity
import de.asdfghasdfgh.etsuran.browser.BrowserAction
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.preference.toggle
import de.asdfghasdfgh.etsuran.unit.BrowserUnit
import de.asdfghasdfgh.etsuran.unit.HelperUnit
import de.asdfghasdfgh.etsuran.unit.IntentUnit
import de.asdfghasdfgh.etsuran.unit.ShareUtil
import de.asdfghasdfgh.etsuran.view.EBToast
import de.asdfghasdfgh.etsuran.view.EBWebView
import de.asdfghasdfgh.etsuran.view.dialog.ReceiveDataDialog
import de.asdfghasdfgh.etsuran.view.dialog.compose.MenuItemType
import de.asdfghasdfgh.etsuran.view.dialog.compose.TtsSettingDialogFragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MenuActionHandler(
    private val activity: FragmentActivity,
    private val dispatch: (BrowserAction) -> Unit,
    private val currentWebView: () -> EBWebView,
) : KoinComponent {
    private val config: ConfigManager by inject()

    fun handleLongClick(menuItemType: MenuItemType) =
        when (menuItemType) {
            MenuItemType.Translate -> dispatch(BrowserAction.ShowTranslationConfigDialog(true))
            MenuItemType.ReceiveData -> dispatch(BrowserAction.ToggleReceiveTextSearch)
            MenuItemType.SendLink -> dispatch(BrowserAction.ToggleTextSearch)
            MenuItemType.ShareLink -> dispatch(BrowserAction.ShareLinkLongPress)
            MenuItemType.TouchSetting -> dispatch(BrowserAction.ToggleTouchPagination)
            MenuItemType.BoldFont -> dispatch(BrowserAction.ShowFontBoldnessDialog)
            MenuItemType.ReaderMode -> dispatch(BrowserAction.ShowReaderSettingsDialog)
            MenuItemType.Settings -> IntentUnit.gotoSettings(activity)
            MenuItemType.Tts -> TtsSettingDialogFragment()
                .show(activity.supportFragmentManager, "TtsSettingDialog")

            MenuItemType.Instapaper -> dispatch(BrowserAction.ConfigureInstapaper)

            MenuItemType.SaveArchive -> dispatch(BrowserAction.ShowSavedPages)

            else -> Unit
        }

    fun handle(menuItemType: MenuItemType): Any? {
        val ebWebView = currentWebView()
        return when (menuItemType) {
            MenuItemType.Tts -> dispatch(BrowserAction.HandleTtsButton)
            MenuItemType.QuickToggle -> dispatch(BrowserAction.ShowFastToggleDialog)
            MenuItemType.OpenHome -> dispatch(BrowserAction.UpdateAlbum(config.favoriteUrl))
            MenuItemType.CloseTab -> dispatch(BrowserAction.RemoveAlbum)
            MenuItemType.Quit -> activity.finishAndRemoveTask()

            MenuItemType.SplitScreen -> dispatch(BrowserAction.ToggleSplitScreen())
            MenuItemType.Translate -> dispatch(BrowserAction.ShowTranslation)
            MenuItemType.VerticalRead -> dispatch(BrowserAction.ToggleVerticalRead)
            MenuItemType.ReaderMode -> dispatch(BrowserAction.ToggleReaderMode)
            MenuItemType.TouchSetting -> dispatch(BrowserAction.ShowTouchAreaDialog)
            MenuItemType.ToolbarSetting -> {
                activity.startActivity(Intent(activity, ToolbarConfigActivity::class.java).apply {
                    putExtra(ToolbarConfigActivity.EXTRA_IS_READER_MODE, activity is EpubReaderActivity)
                })
            }

            MenuItemType.ReceiveData -> dispatch(BrowserAction.ToggleReceiveLink)
            MenuItemType.SendLink -> dispatch(BrowserAction.SendToRemote(ebWebView.url.orEmpty()))

            MenuItemType.ShareLink -> dispatch(BrowserAction.ShareLink)

            MenuItemType.OpenWith -> HelperUnit.showBrowserChooser(
                activity,
                ebWebView.url,
                activity.getString(R.string.menu_open_with)
            )

            MenuItemType.Shortcut -> dispatch(BrowserAction.CreateShortcut)

            MenuItemType.Highlights -> IntentUnit.gotoHighlights(activity)
            MenuItemType.SetHome -> config.favoriteUrl = ebWebView.url.orEmpty()
            MenuItemType.SaveBookmark -> dispatch(BrowserAction.SaveBookmark())
            MenuItemType.Epub -> dispatch(BrowserAction.ShowEpubDialog)
            MenuItemType.SavePdf -> dispatch(BrowserAction.SavePdf)

            MenuItemType.FontSize -> dispatch(BrowserAction.ShowFontSizeChangeDialog)
            MenuItemType.InvertColor -> dispatch(BrowserAction.InvertColors)

            MenuItemType.WhiteBknd -> {
                config.toggleWhiteBackground(ebWebView.url.orEmpty())
                ebWebView.updateCssStyle()
            }

            MenuItemType.BoldFont -> config.display::boldFontStyle.toggle()
            MenuItemType.BlackFont -> config.display::blackFontStyle.toggle()
            MenuItemType.Search -> dispatch(BrowserAction.ShowSearchPanel)
            MenuItemType.Download -> BrowserUnit.openDownloadFolder(activity)
            MenuItemType.SaveArchive -> dispatch(BrowserAction.SavePageForLater)
            MenuItemType.SaveMht -> dispatch(BrowserAction.SaveWebArchive)
            MenuItemType.Settings -> IntentUnit.gotoSettings(activity)

            MenuItemType.PageAiActions -> dispatch(BrowserAction.ShowPageAiActionMenu)
            MenuItemType.Instapaper -> dispatch(BrowserAction.AddToInstapaper)
            MenuItemType.AudioOnly -> ebWebView.toggleAudioOnlyMode()
            MenuItemType.SiteSettings -> dispatch(BrowserAction.ShowSiteSettingsDialog)
        }
    }


    private fun showReceiveDataDialog(ebWebView: EBWebView) {
        ReceiveDataDialog(activity, activity.lifecycleScope).show { text ->
            if (text.startsWith("http")) ebWebView.loadUrl(text)
            else {
                val clip = ClipData.newPlainText("Copied Text", text)
                (activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                    .setPrimaryClip(clip)
                EBToast.show(activity, R.string.toast_copied_to_clipboard)
            }
        }
    }

}
