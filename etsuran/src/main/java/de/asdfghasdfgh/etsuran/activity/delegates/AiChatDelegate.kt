package de.asdfghasdfgh.etsuran.activity.delegates

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.activity.BrowserState
import de.asdfghasdfgh.etsuran.preference.ChatGPTActionInfo
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.preference.GptActionDisplay
import de.asdfghasdfgh.etsuran.preference.GptActionScope
import de.asdfghasdfgh.etsuran.task.InitialPageSnapshot
import de.asdfghasdfgh.etsuran.view.EBToast
import de.asdfghasdfgh.etsuran.view.EBWebView
import de.asdfghasdfgh.etsuran.view.dialog.compose.PageAiActionDialogFragment
import de.asdfghasdfgh.etsuran.view.dialog.compose.ShowEditGptActionDialogFragment
import de.asdfghasdfgh.etsuran.view.viewControllers.TwoPaneController
import de.asdfghasdfgh.etsuran.viewmodel.TranslationViewModel
import de.asdfghasdfgh.etsuran.viewmodel.TtsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiChatDelegate(
    private val activity: FragmentActivity,
    private val config: ConfigManager,
    private val state: BrowserState,
    private val translationViewModel: TranslationViewModel,
    private val ttsViewModel: TtsViewModel,
    private val twoPaneControllerProvider: () -> TwoPaneController,
    private val isTwoPaneControllerInitialized: () -> Boolean,
    private val maybeInitTwoPaneController: () -> Unit,
    private val addAlbum: (title: String, url: String) -> Unit,
    private val showTranslationDialog: (isWholePageMode: Boolean) -> Unit,
    private val showTaskMenu: () -> Unit,
) {
    private val linkContentWebView: EBWebView by lazy {
        EBWebView(activity, activity as de.asdfghasdfgh.etsuran.browser.WebViewCallback).apply {
            setOnPageFinishedAction {
                activity.lifecycleScope.launch {
                    if (!translationViewModel.hasOpenAiApiKey()) {
                        loadUrl("about:blank")
                        EBToast.show(activity, R.string.gpt_api_key_not_set)
                        return@launch
                    }
                    val content = linkContentWebView.getRawText(onGeminiTranscribe = ::showTranscribingNote)
                    loadUrl("about:blank")
                    if (content.isNotEmpty()) {
                        val isSuccess = translationViewModel.setupTextSummary(content)
                        if (!isSuccess) {
                            EBToast.show(activity, R.string.gpt_api_key_not_set)
                            return@launch
                        }
                        showTranslationDialog(false)
                    }
                }
            }
        }
    }

    fun summarizeLinkContent(url: String) {
        if (!translationViewModel.hasOpenAiApiKey()) {
            EBToast.show(activity, R.string.gpt_api_key_not_set)
            return
        }
        translationViewModel.url = url
        linkContentWebView.loadUrl(url)
    }

    fun summarizeContent() {
        if (!translationViewModel.hasOpenAiApiKey()) {
            EBToast.show(activity, R.string.gpt_api_key_not_set)
            return
        }
        activity.lifecycleScope.launch {
            val ebWebView = state.ebWebView
            translationViewModel.url = ebWebView.url.orEmpty()
            val rawText = ebWebView.getRawText(onGeminiTranscribe = ::showTranscribingNote)
            val isSuccess = translationViewModel.setupTextSummary(rawText)

            if (!isSuccess) {
                EBToast.show(activity, R.string.gpt_api_key_not_set)
                return@launch
            }

            showTranslationDialog(false)
        }
    }

    fun chatWithWeb(useSplitScreen: Boolean, content: String?, runWithAction: ChatGPTActionInfo?) {
        activity.lifecycleScope.launch {
            val ebWebView = state.ebWebView
            val rawText = content ?: ebWebView.getRawText()
            // Opening a chat with no page content silently looks like the content was
            // lost (e.g. video transcription failed and the watch page has no
            // readable text) — surface it instead.
            if (rawText.isBlank()) {
                withContext(Dispatchers.Main) {
                    EBToast.show(activity, R.string.chat_no_web_content)
                }
                return@launch
            }
            withContext(Dispatchers.Main) {
                val scope = activity.lifecycleScope
                if (useSplitScreen) {
                    maybeInitTwoPaneController()
                    val webTitle = ebWebView.title ?: activity.getString(R.string.page_untitled)
                    val webUrl = ebWebView.url.orEmpty()
                    twoPaneControllerProvider().showSecondPaneAsAi(rawText, webTitle, webUrl)
                    runWithAction?.let { twoPaneControllerProvider().runGptAction(it) }
                } else {
                    val webTitle = ebWebView.title ?: activity.getString(R.string.page_untitled)
                    val webUrl = ebWebView.url.orEmpty()
                    addAlbum(activity.getString(R.string.chat_with_web), "")
                    val newWebView = state.ebWebView
                    runWithAction?.let { action ->
                        newWebView.setOnPageFinishedAction {
                            newWebView.setOnPageFinishedAction {}
                            newWebView.runGptAction(action)
                        }
                    }
                    newWebView.setupAiPage(scope, rawText, webTitle, webUrl)
                }
            }
        }
    }

    /**
     * Open a new chat tab that runs the free-form LLM agent with tool access. The chat
     * tab's [ChatWebInterface] is configured in agent mode and the user's [prompt] is
     * fired as the first message once `chat.html` finishes loading.
     *
     * The [snapshot] captures the page the user was viewing at the moment they triggered
     * the task — it's exposed to the agent as the "originating page" so the user can say
     * things like "summarize this" or "open the top 3 stories" without extra fetches.
     */
    fun chatWithWebAgent(prompt: String, snapshot: InitialPageSnapshot) {
        activity.lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                val scope = activity.lifecycleScope
                addAlbum(activity.getString(R.string.ai_agent_chat), "")
                val newWebView = state.ebWebView
                newWebView.setOnPageFinishedAction {
                    newWebView.setOnPageFinishedAction {}
                    newWebView.runAgentPrompt(prompt)
                }
                newWebView.setupAiPage(
                    lifecycleScope = scope,
                    webContent = snapshot.text,
                    webTitle = snapshot.title.ifBlank { activity.getString(R.string.ai_agent_chat) },
                    webUrl = snapshot.url,
                    agentMode = true,
                    initialSnapshot = snapshot,
                    agentContext = activity,
                    agentBrowserState = state,
                    agentTtsViewModel = ttsViewModel,
                )
            }
        }
    }

    // Opens the translation popup with a "transcribing with Gemini" note before the
    // slow video transcription starts, so a minutes-long wait doesn't look like the
    // AI action silently failed. The action's own setup later replaces the note.
    private fun showTranscribingNote() {
        translationViewModel.setupStatusMessage(
            activity.getString(R.string.gemini_transcribing_note)
        )
        showTranslationDialog(false)
    }

    fun showPageAiActionMenu() {
        val pageActions =
            config.ai.gptActionList.filter { it.scope == GptActionScope.WholePage }

        PageAiActionDialogFragment(
            actions = pageActions,
            onActionClicked = { runPageAiAction(it) },
            onActionLongClicked = { action ->
                val index = config.ai.gptActionList.indexOf(action)
                if (index >= 0) {
                    ShowEditGptActionDialogFragment(index)
                        .showNow(activity.supportFragmentManager, "editGptAction")
                }
            },
            onChatWithWebClicked = { chatWithWeb(useSplitScreen = false, content = null, runWithAction = null) },
            onChatWithWebLongClicked = { chatWithWeb(useSplitScreen = true, content = null, runWithAction = null) },
            onTaskRunnerClicked = { showTaskMenu() },
        ).showNow(activity.supportFragmentManager, "pageAiAction")
    }

    fun runPageAiAction(action: ChatGPTActionInfo) {
        activity.lifecycleScope.launch {
            if (!translationViewModel.hasOpenAiApiKey()) {
                EBToast.show(activity, R.string.gpt_api_key_not_set)
                return@launch
            }

            val ebWebView = state.ebWebView
            val content = ebWebView.getRawText(
                onGeminiTranscribe = if (action.display == GptActionDisplay.Popup) {
                    ::showTranscribingNote
                } else null // EBWebView's default toast covers the non-popup displays
            )
            translationViewModel.setupGptAction(action)
            translationViewModel.url = ebWebView.url.orEmpty()
            translationViewModel.pageTitle = ebWebView.title.orEmpty()

            when (action.display) {
                GptActionDisplay.Popup -> {
                    translationViewModel.updateInputMessage(content)
                    translationViewModel.updateMessageWithContext(content)
                    showTranslationDialog(true)
                }

                GptActionDisplay.NewTab -> chatWithWeb(false, content, action)
                GptActionDisplay.SplitScreen -> chatWithWeb(true, content, action)
            }
        }
    }
}
