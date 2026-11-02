package de.asdfghasdfgh.etsuran.activity.delegates

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.activity.BrowserState
import de.asdfghasdfgh.etsuran.database.Bookmark
import de.asdfghasdfgh.etsuran.unit.BrowserUnit
import de.asdfghasdfgh.etsuran.unit.HelperUnit
import de.asdfghasdfgh.etsuran.unit.IntentUnit
import de.asdfghasdfgh.etsuran.unit.ViewUnit
import de.asdfghasdfgh.etsuran.unit.pruneWebTitle
import de.asdfghasdfgh.etsuran.view.EBToast
import de.asdfghasdfgh.etsuran.view.EBWebView
import de.asdfghasdfgh.etsuran.view.dialog.BookmarkEditDialog
import de.asdfghasdfgh.etsuran.view.dialog.compose.BookmarksDialogFragment
import de.asdfghasdfgh.etsuran.view.viewControllers.OverviewDialogController
import de.asdfghasdfgh.etsuran.viewmodel.BookmarkViewModel
import kotlinx.coroutines.launch

class BookmarkActionsDelegate(
    private val activity: FragmentActivity,
    private val state: BrowserState,
    private val bookmarkViewModel: BookmarkViewModel,
    private val overviewDialogControllerProvider: () -> OverviewDialogController,
    private val updateAlbum: (String?) -> Unit,
    private val addAlbum: (String, String, Boolean) -> Unit,
    private val toggleSplitScreen: (String?) -> Unit,
) {
    fun saveBookmark(url: String?, title: String?) {
        val currentUrl = url ?: state.ebWebView.url ?: return
        val nonNullTitle = title ?: HelperUnit.secString(state.ebWebView.title)
        try {
            BookmarkEditDialog(
                bookmarkViewModel,
                Bookmark(
                    nonNullTitle.pruneWebTitle(),
                    currentUrl,
                    order = if (ViewUnit.isWideLayout(activity)) 999 else 0,
                ),
                { ViewUnit.hideKeyboard(activity); EBToast.show(activity, R.string.toast_edit_successful) },
                { ViewUnit.hideKeyboard(activity) },
            ).show(activity.supportFragmentManager, "bookmark_edit")
        } catch (e: Exception) {
            e.printStackTrace()
            EBToast.show(activity, R.string.toast_error)
        }
    }

    fun createShortcut() = BrowserUnit.createShortcut(activity, state.ebWebView)

    fun shareLink() = IntentUnit.share(activity, state.ebWebView.title, state.ebWebView.url)

    fun openHistoryPage(amount: Int) = overviewDialogControllerProvider().openHistoryPage(amount)

    fun openBookmarkPage() {
        activity.lifecycleScope.launch {
            // Wait for the list so the dialog opens at its final size; showing it
            // empty and letting it grow once the query finishes made the window
            // visibly resize and shift on screen.
            bookmarkViewModel.awaitLoaded()
            if (activity.isFinishing || activity.supportFragmentManager.isStateSaved) return@launch
            BookmarksDialogFragment(
                activity.lifecycleScope,
                bookmarkViewModel,
                gotoUrlAction = { url -> updateAlbum(url) },
                bookmarkIconClickAction = { title, url, isForeground -> addAlbum(title, url, isForeground) },
                splitScreenAction = { url -> toggleSplitScreen(url) },
            ).show(activity.supportFragmentManager, "bookmarks dialog")
        }
    }

    fun prepareRecord(): Boolean {
        val webView = state.currentAlbumController as EBWebView
        val title = webView.title
        val url = webView.url
        return (title.isNullOrEmpty() || url.isNullOrEmpty()
                || url.startsWith(BrowserUnit.URL_SCHEME_ABOUT)
                || url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)
                || url.startsWith(BrowserUnit.URL_SCHEME_INTENT))
    }
}
