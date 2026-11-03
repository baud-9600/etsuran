package de.asdfghasdfgh.etsuran.search

import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.activity.BaseWhiteListType
import de.asdfghasdfgh.etsuran.activity.WhiteListEntry
import de.asdfghasdfgh.etsuran.browser.DomainInterface
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.preference.SplitSearchItemInfo
import de.asdfghasdfgh.etsuran.view.dialog.DialogManager
import de.asdfghasdfgh.etsuran.view.dialog.MmdInputField
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SplitSearchListType : BaseWhiteListType() {
    override val titleId: Int
        get() = R.string.split_search_settings
    override val emptyHintResId: Int = R.string.split_search_empty_hint
    override val domainHandler: DomainInterface by lazy { SplitSearchHandler() }

    override suspend fun getEntries(): List<WhiteListEntry> =
        (domainHandler as SplitSearchHandler).getItems().map {
            WhiteListEntry(
                value = it.title,
                title = it.title,
                subtitle = it.stringPattern,
            )
        }

    override suspend fun addEntry(dialogManager: DialogManager): WhiteListEntry? {
        val values = dialogManager.getMultiFieldInput(
            titleId,
            R.string.setting_summary_split_search_setting,
            listOf(
                MmdInputField(dialogManager.text(R.string.split_search_name_label)),
                MmdInputField(dialogManager.text(R.string.split_search_url_label)),
            ),
        ) ?: return null
        val title = values[0].trim()
        val pattern = values[1].trim()
        if (title.isBlank() || pattern.isBlank()) return null
        val item = SplitSearchItemInfo(title, pattern, true)
        (domainHandler as SplitSearchHandler).addSplitSearchItem(item)
        return WhiteListEntry(value = title, title = title, subtitle = pattern)
    }

    override suspend fun editEntry(
        entry: WhiteListEntry,
        dialogManager: DialogManager,
    ): WhiteListEntry? {
        val values = dialogManager.getMultiFieldInput(
            R.string.menu_edit,
            R.string.setting_summary_split_search_setting,
            listOf(
                MmdInputField(
                    dialogManager.text(R.string.split_search_name_label),
                    entry.title,
                ),
                MmdInputField(
                    dialogManager.text(R.string.split_search_url_label),
                    entry.subtitle.orEmpty(),
                ),
            ),
        ) ?: return null
        val title = values[0].trim()
        val pattern = values[1].trim()
        if (title.isBlank() || pattern.isBlank()) return null
        (domainHandler as SplitSearchHandler).updateSplitSearchItem(
            oldTitle = entry.value,
            item = SplitSearchItemInfo(title, pattern, true),
        )
        return WhiteListEntry(value = title, title = title, subtitle = pattern)
    }
}

private class SplitSearchHandler : DomainInterface, KoinComponent {
    private val configManager: ConfigManager by inject()

    override suspend fun getDomains(): List<String> = getItems().map { it.title }

    fun getItems(): List<SplitSearchItemInfo> = configManager.splitSearchItemInfoList

    override suspend fun addDomain(domain: String) { /* use addSplitSearchItem instead */
    }

    fun addSplitSearchItem(splitSearchItemInfo: SplitSearchItemInfo) {
        configManager.addSplitSearchItem(splitSearchItemInfo)
    }

    fun updateSplitSearchItem(oldTitle: String, item: SplitSearchItemInfo) {
        configManager.updateSplitSearchItem(oldTitle, item)
    }

    override suspend fun deleteDomain(domain: String) {
        configManager.deleteSplitSearchItem(SplitSearchItemInfo(domain, domain, true))
    }

    override suspend fun deleteAllDomains() = configManager.deleteAllSplitSearchItems()
}
