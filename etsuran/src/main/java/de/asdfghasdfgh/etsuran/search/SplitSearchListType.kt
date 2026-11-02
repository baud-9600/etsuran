package de.asdfghasdfgh.etsuran.search

import androidx.lifecycle.LifecycleCoroutineScope
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.activity.BaseWhiteListType
import de.asdfghasdfgh.etsuran.browser.DomainInterface
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.preference.SplitSearchItemInfo
import de.asdfghasdfgh.etsuran.view.dialog.DialogManager
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SplitSearchListType : BaseWhiteListType() {
    override val titleId: Int
        get() = R.string.split_search_settings
    override val domainHandler: DomainInterface by lazy { SplitSearchHandler() }

    override fun addDomain(
        lifecycleScope: LifecycleCoroutineScope,
        dialogManager: DialogManager,
        postAction: (String) -> Unit
    ) {
        lifecycleScope.launch {
            val title = dialogManager.getTextInput(
                titleId, R.string.description_split_screen_title, ""
            )?.trim() ?: return@launch
            val stringPattern = dialogManager.getTextInput(
                titleId, R.string.description_split_screen_string_pattern, ""
            )?.trim() ?: return@launch
            if (title.isNotBlank() && stringPattern.isNotBlank()) {
                (domainHandler as SplitSearchHandler).addSplitSearchItem(
                    SplitSearchItemInfo(title, stringPattern, true)
                )
                postAction(title)
            }
        }
    }
}

private class SplitSearchHandler : DomainInterface, KoinComponent {
    private val configManager: ConfigManager by inject()
    override suspend fun getDomains(): List<String> =
        configManager.splitSearchItemInfoList.map { it.title }

    override suspend fun addDomain(domain: String) { /* use addSplitSearchItem instead */
    }

    fun addSplitSearchItem(splitSearchItemInfo: SplitSearchItemInfo) {
        configManager.addSplitSearchItem(splitSearchItemInfo)
    }

    override suspend fun deleteDomain(domain: String) {
        configManager.deleteSplitSearchItem(SplitSearchItemInfo(domain, domain, true))
    }

    override suspend fun deleteAllDomains() = configManager.deleteAllSplitSearchItems()
}