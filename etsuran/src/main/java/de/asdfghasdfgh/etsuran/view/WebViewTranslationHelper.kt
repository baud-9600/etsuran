package de.asdfghasdfgh.etsuran.view

import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.viewmodel.TRANSLATE_API

class WebViewTranslationHelper(
    private val webView: EBWebView,
    private val config: ConfigManager,
) {
    var translateApi: TRANSLATE_API = TRANSLATE_API.GOOGLE
    var isTranslateByParagraph = false

    fun clearTranslationElements() = webView.jsBridge.clearTranslationElements()

    fun translateByParagraphInPlaceReplace() {
        webView.jsBridge.translateByParagraphInPlaceReplace()
        isTranslateByParagraph = true
    }

    fun translateByParagraphInPlace() {
        webView.jsBridge.translateByParagraphInPlace(config.translation.translationTextStyle)
        isTranslateByParagraph = true
    }

    fun addGoogleTranslation() =
        webView.jsBridge.addGoogleTranslation(config.translation.preferredTranslateLanguageString)

    fun hideTranslateContext() = webView.jsBridge.hideTranslateContext(config.translation.translationMode)
}
