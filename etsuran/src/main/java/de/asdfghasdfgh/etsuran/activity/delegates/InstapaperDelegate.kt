package de.asdfghasdfgh.etsuran.activity.delegates

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.activity.BrowserState
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.view.EBToast
import de.asdfghasdfgh.etsuran.view.dialog.DialogManager
import de.asdfghasdfgh.etsuran.viewmodel.InstapaperViewModel
import kotlinx.coroutines.launch

class InstapaperDelegate(
    private val activity: FragmentActivity,
    private val state: BrowserState,
    private val config: ConfigManager,
    private val instapaperViewModel: InstapaperViewModel,
    private val dialogManager: DialogManager,
) {
    fun init() {
        activity.lifecycleScope.launch {
            instapaperViewModel.uiState.collect { uiState ->
                if (uiState.showConfigureDialog) configureInstapaper()
                else if (uiState.successMessage != null) EBToast.show(activity, uiState.successMessage)
                else if (uiState.errorMessageRes != null) EBToast.show(activity, uiState.errorMessageRes)
                else if (uiState.errorMessage != null) EBToast.show(activity, uiState.errorMessage)
                instapaperViewModel.resetState()
            }
        }
    }

    fun addToInstapaper() {
        val url = state.ebWebView.url.orEmpty()
        if (url.isEmpty()) {
            EBToast.show(activity, R.string.url_empty)
            return
        }
        instapaperViewModel.addUrl(
            url = url,
            username = config.instapaperUsername,
            password = config.instapaperPassword,
            title = state.ebWebView.title,
        )
    }

    fun configureInstapaper() {
        dialogManager.showInstapaperCredentialsDialog { _, _ -> Unit }
    }
}
