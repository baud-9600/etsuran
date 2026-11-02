package de.asdfghasdfgh.etsuran.activity.delegates

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import de.asdfghasdfgh.etsuran.activity.BrowserState
import de.asdfghasdfgh.etsuran.view.dialog.TtsLanguageDialog
import de.asdfghasdfgh.etsuran.view.dialog.compose.TtsSettingDialogFragment
import de.asdfghasdfgh.etsuran.view.viewControllers.ComposeToolbarViewController
import de.asdfghasdfgh.etsuran.viewmodel.TtsViewModel
import kotlinx.coroutines.launch

class TtsButtonDelegate(
    private val activity: FragmentActivity,
    private val state: BrowserState,
    private val ttsViewModel: TtsViewModel,
    private val composeToolbarViewControllerProvider: () -> ComposeToolbarViewController,
) {
    fun init() {
        activity.lifecycleScope.launch {
            ttsViewModel.readingState.collect { composeToolbarViewControllerProvider().updateIcons() }
        }
    }

    fun handleTtsButton() {
        if (ttsViewModel.isReading()) {
            TtsSettingDialogFragment().show(activity.supportFragmentManager, "TtsSettingDialog")
        } else {
            readArticle()
        }
    }

    fun showTtsLanguageDialog() {
        TtsLanguageDialog(activity).show(ttsViewModel.getAvailableLanguages())
    }

    fun readArticle() {
        activity.lifecycleScope.launch {
            ttsViewModel.readArticle(state.ebWebView.getRawText(), state.ebWebView.title.orEmpty())
        }
    }
}
