package de.asdfghasdfgh.etsuran.view.dialog

import android.app.Activity
import android.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.database.BookmarkManager
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.preference.StartPageItem
import de.asdfghasdfgh.etsuran.unit.BookmarkRenderer
import de.asdfghasdfgh.etsuran.view.EBWebView
import de.asdfghasdfgh.etsuran.unit.ViewUnit.dp
import de.asdfghasdfgh.etsuran.view.compose.MyTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import de.asdfghasdfgh.etsuran.view.withThemedFrame

/**
 * Flow behind the "+" tile on the built-in start page: add an item picked from
 * the existing bookmarks or entered manually, or delete a current item.
 */
class StartPageItemDialog(private val ebWebView: EBWebView) : KoinComponent {
    private val config: ConfigManager by inject()
    private val bookmarkManager: BookmarkManager by inject()
    private val activity: Activity = ebWebView.context as Activity

    suspend fun show() {
        val options = mutableListOf(
            activity.getString(R.string.start_page_pick_bookmark),
            activity.getString(R.string.start_page_enter_manually),
        )
        if (config.startPageItems.isNotEmpty()) {
            options.add(activity.getString(R.string.menu_delete))
        }

        when (activity.showPlainListDialog(null, options)) {
            0 -> pickFromBookmarks()
            1 -> enterManually()
            2 -> removeItem()
        }
    }

    private suspend fun pickFromBookmarks() {
        val bookmarks = bookmarkManager.getAllBookmarksOnly()
        if (bookmarks.isEmpty()) return
        val index = activity.showPlainListDialog(
            R.string.start_page_pick_bookmark,
            bookmarks.map { it.title },
        ) ?: return
        val bookmark = bookmarks.getOrNull(index) ?: return
        config.addStartPageItem(StartPageItem(bookmark.title, bookmark.url))
        BookmarkRenderer.loadStartPage(ebWebView)
    }

    private fun enterManually() {
        val titleState = mutableStateOf("")
        val urlState = mutableStateOf("")
        val composeView = ComposeView(activity).apply {
            setViewTreeLifecycleOwner(activity as LifecycleOwner)
            setViewTreeSavedStateRegistryOwner(activity as SavedStateRegistryOwner)
            setContent {
                MyTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 15.dp)
                    ) {
                        OutlinedTextField(
                            value = titleState.value,
                            onValueChange = { titleState.value = it },
                            label = { Text(stringResource(R.string.dialog_title_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = MaterialTheme.colors.onBackground,
                                cursorColor = MaterialTheme.colors.onBackground,
                            ),
                        )
                        OutlinedTextField(
                            value = urlState.value,
                            onValueChange = { urlState.value = it },
                            label = { Text(stringResource(R.string.dialog_url_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = MaterialTheme.colors.onBackground,
                                cursorColor = MaterialTheme.colors.onBackground,
                            ),
                        )
                    }
                }
            }
        }

        DialogManager(activity).showOkCancelDialog(
            title = activity.getString(R.string.whitelist_add),
            view = composeView,
            okAction = {
                val rawUrl = urlState.value.trim()
                if (rawUrl.isEmpty()) return@showOkCancelDialog
                val url = if (rawUrl.contains("://")) rawUrl else "https://$rawUrl"
                val title = titleState.value.trim().ifEmpty {
                    runCatching { java.net.URI(url).host }.getOrNull() ?: url
                }
                config.addStartPageItem(StartPageItem(title, url))
                BookmarkRenderer.loadStartPage(ebWebView)
            },
        ).allowImeForComposeContent()
    }

    private suspend fun removeItem() {
        val items = config.startPageItems
        val index = activity.showPlainListDialog(
            R.string.menu_delete,
            items.map { it.title },
        ) ?: return
        val item = items.getOrNull(index) ?: return
        config.removeStartPageItem(item.url)
        BookmarkRenderer.loadStartPage(ebWebView)
    }
}

// A compact MMD menu: 2dp outline, 20dp corners, 56dp rows. Compose rather than
// AlertDialog's item list so the panel, type and dividers match the gear menu.
internal suspend fun Activity.showPlainListDialog(titleId: Int?, names: List<String>): Int? =
    suspendCoroutine { continuation ->
        val dialog = android.app.Dialog(this)
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@showPlainListDialog as LifecycleOwner)
            setViewTreeSavedStateRegistryOwner(this@showPlainListDialog as SavedStateRegistryOwner)
            setContent {
                MyTheme {
                    Column(
                        modifier = Modifier
                            .width(320.dp)
                            .heightIn(max = 480.dp)
                            .background(
                                MaterialTheme.colors.background,
                                androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            )
                            .border(
                                2.dp,
                                MaterialTheme.colors.onBackground,
                                androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            )
                            .verticalScroll(rememberScrollState()),
                    ) {
                        if (titleId != null) {
                            Text(
                                text = stringResource(titleId),
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onBackground,
                            )
                        }
                        names.forEachIndexed { index, name ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .clickable {
                                        continuation.resume(index)
                                        dialog.dismiss()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.subtitle1,
                                    color = MaterialTheme.colors.onBackground,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                            }
                            if (index != names.lastIndex) {
                                androidx.compose.material.Divider(
                                    modifier = Modifier.padding(start = 16.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colors.onBackground,
                                )
                            }
                        }
                    }
                }
            }
        }
        dialog.setContentView(composeView)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.3f)
        dialog.setOnCancelListener { continuation.resume(null) }
        dialog.show()
    }
