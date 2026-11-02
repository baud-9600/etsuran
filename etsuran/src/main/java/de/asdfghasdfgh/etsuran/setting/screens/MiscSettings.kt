package de.asdfghasdfgh.etsuran.setting.screens

import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.activity.SettingRoute.UserAgent
import de.asdfghasdfgh.etsuran.preference.HighlightStyle
import de.asdfghasdfgh.etsuran.preference.TranslationTextStyle
import de.asdfghasdfgh.etsuran.setting.ActionSettingItem
import de.asdfghasdfgh.etsuran.setting.DividerSettingItem
import de.asdfghasdfgh.etsuran.setting.ListSettingWithEnumItem
import de.asdfghasdfgh.etsuran.setting.NavigateSettingItem
import de.asdfghasdfgh.etsuran.setting.SettingItemInterface
import de.asdfghasdfgh.etsuran.setting.ValueSettingItem
import de.asdfghasdfgh.etsuran.view.dialog.PrinterDocumentPaperSizeDialog
import de.asdfghasdfgh.etsuran.view.dialog.TranslationLanguageDialog
import kotlinx.coroutines.launch

fun buildMiscSettingItems(deps: SettingScreenDeps): List<SettingItemInterface> {
    val config = deps.config
    return listOf(
        ListSettingWithEnumItem(
            R.string.setting_title_highlight_style,
            0,
            R.string.setting_summary_highlight_style,
            config = config.display::highlightStyle,
            options = HighlightStyle.entries
                .map { it.stringResId },
        ),
        ListSettingWithEnumItem(
            R.string.setting_title_translation_style,
            0,
            R.string.setting_summary_translation_style,
            config = config.translation::translationTextStyle,
            options = TranslationTextStyle.entries.map { it.stringResId },
        ),
        NavigateSettingItem(
            R.string.setting_title_userAgent,
            0,
            destination = UserAgent
        ),
        ValueSettingItem(
            R.string.setting_title_edit_homepage,
            0,
            config = config::favoriteUrl,
            showValue = false
        ),
        ActionSettingItem(R.string.setting_title_pdf_paper_size, 0) {
            PrinterDocumentPaperSizeDialog(
                deps.activity
            ).show()
        },
        DividerSettingItem(),
//        BooleanSettingItem(
//            R.string.setting_title_enable_inplace_translate,
//            0,
//            R.string.setting_summary_enable_inplace_translate,
//            config::enableInplaceParagraphTranslate
//        ),
        ValueSettingItem(
            R.string.setting_title_translated_langs,
            0,
            R.string.setting_summary_translated_langs,
            config.translation::preferredTranslateLanguageString
        ),
        ValueSettingItem(
            R.string.translate_image_key,
            0,
            R.string.translate_image_key_summary,
            config = config.ai::imageApiKey,
            showValue = false
        ),
        ActionSettingItem(
            R.string.setting_dual_caption,
            0,
            R.string.setting_summary_dual_caption,
        ) {
            deps.lifecycleScope.launch {
                TranslationLanguageDialog(deps.activity).showDualCaptionLocale()
            }
        },
    )
}
