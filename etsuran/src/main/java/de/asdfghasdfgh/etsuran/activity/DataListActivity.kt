package de.asdfghasdfgh.etsuran.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.browser.AdBlock
import de.asdfghasdfgh.etsuran.browser.Cookie
import de.asdfghasdfgh.etsuran.browser.DomainInterface
import de.asdfghasdfgh.etsuran.browser.Javascript
import de.asdfghasdfgh.etsuran.search.SplitSearchListType
import de.asdfghasdfgh.etsuran.view.compose.EmptyListPlaceholder
import de.asdfghasdfgh.etsuran.view.compose.ListScaffold
import de.asdfghasdfgh.etsuran.view.dialog.DialogManager
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import de.asdfghasdfgh.etsuran.view.compose.onTopBar

class DataListActivity : LocaleAwareComponentActivity() {

    private lateinit var whitelistType: BaseWhiteListType
    private val dialogManager: DialogManager by lazy { DialogManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        whitelistType = getWhitelistType()
        setContent {
            val entries = remember { mutableStateOf(emptyList<WhiteListEntry>()) }
            LaunchedEffect(Unit) { entries.value = whitelistType.getEntries() }
            ListScaffold(
                title = stringResource(whitelistType.titleId),
                onBack = { finish() },
                actions = {
                    IconButton(onClick = {
                        lifecycleScope.launch {
                            whitelistType.deleteAllDomains()
                            entries.value = emptyList()
                        }
                    }) {
                        Icon(
                            tint = MaterialTheme.colors.onTopBar,
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.menu_delete)
                        )
                    }
                    IconButton(onClick = {
                        lifecycleScope.launch {
                            whitelistType.addEntry(dialogManager)?.let { entry ->
                                entries.value += entry
                            }
                        }
                    }) {
                        Icon(
                            tint = MaterialTheme.colors.onTopBar,
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.whitelist_add)
                        )
                    }
                },
            ) { innerPadding ->
                WhiteListContent(
                    modifier = Modifier.padding(innerPadding),
                    list = entries,
                    emptyHintResId = whitelistType.emptyHintResId,
                    editAction = { entry ->
                        lifecycleScope.launch {
                            val updated = whitelistType.editEntry(entry, dialogManager)
                                ?: return@launch
                            entries.value = entries.value.map {
                                if (it.value == entry.value) updated else it
                            }
                        }
                    },
                ) { entry ->
                    lifecycleScope.launch {
                        whitelistType.deleteEntry(entry)
                        entries.value = entries.value.filter { it.value != entry.value }
                    }
                }
            }
        }
    }

    private fun getWhitelistType(): BaseWhiteListType =
        @Suppress("DEPRECATION")
        when (intent.getSerializableExtra(TYPE) as WhiteListType) {
            WhiteListType.Adblock -> WhiteListTypeAdblock()
            WhiteListType.Javascript -> BaseWhiteListTypeJavascript()
            WhiteListType.Cookie -> BaseWhiteListTypeCookie()
            WhiteListType.SplitSearch -> SplitSearchListType()
        }

    companion object {
        private const val TYPE = "type"
        fun createIntent(context: Context, type: WhiteListType) = Intent(
            context,
            DataListActivity::class.java
        ).apply {
            putExtra(TYPE, type)
        }
    }
}

enum class WhiteListType {
    Adblock,
    Cookie,
    Javascript,
    SplitSearch
}

/** One editable row: the stored value plus optional second line. */
data class WhiteListEntry(
    val value: String,
    val title: String = value,
    val subtitle: String? = null,
)

@Composable
fun WhiteListContent(
    modifier: Modifier = Modifier,
    list: MutableState<List<WhiteListEntry>>,
    emptyHintResId: Int = R.string.empty_whitelist_hint,
    editAction: (WhiteListEntry) -> Unit = {},
    deleteAction: (WhiteListEntry) -> Unit = {},
) {
    if (list.value.isEmpty()) {
        EmptyListPlaceholder(
            stringResource(R.string.list_empty) + "\n" + stringResource(emptyHintResId)
        )
    } else {
        LazyColumn(modifier = modifier) {
            items(list.value.size, key = { list.value[it].value }) { index ->
                val entry = list.value[index]
                WhiteListRow(
                    entry = entry,
                    onEdit = { editAction(entry) },
                    onDelete = { deleteAction(entry) },
                )
                Divider(
                    modifier = Modifier.padding(start = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colors.onBackground,
                )
            }
        }
    }
}

/**
 * The same row geometry as Settings and the ad-block filter list: a 72dp
 * minimum, 20sp title, 15sp supporting line, full-width tap target and a
 * trailing delete action.
 */
@Composable
private fun WhiteListRow(
    entry: WhiteListEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onEdit)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            entry.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                tint = MaterialTheme.colors.onBackground,
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.menu_delete)
            )
        }
    }
}

abstract class BaseWhiteListType {
    abstract val titleId: Int
    open val emptyHintResId: Int = R.string.empty_whitelist_hint
    abstract val domainHandler: DomainInterface
    suspend fun getDomains(): List<String> = domainHandler.getDomains()

    open suspend fun getEntries(): List<WhiteListEntry> =
        getDomains().map { WhiteListEntry(it) }

    open suspend fun addEntry(dialogManager: DialogManager): WhiteListEntry? {
        val value = dialogManager.getTextInput(
            R.string.whitelist_add, titleId, ""
        )?.trim() ?: return null
        if (value.isBlank()) return null
        domainHandler.addDomain(value)
        return WhiteListEntry(value)
    }

    open suspend fun editEntry(
        entry: WhiteListEntry,
        dialogManager: DialogManager,
    ): WhiteListEntry? {
        val value = dialogManager.getTextInput(
            R.string.menu_edit, titleId, entry.value
        )?.trim() ?: return null
        if (value.isBlank() || value == entry.value) return null
        domainHandler.deleteDomain(entry.value)
        domainHandler.addDomain(value)
        return WhiteListEntry(value)
    }

    suspend fun deleteEntry(entry: WhiteListEntry) = domainHandler.deleteDomain(entry.value)
    suspend fun deleteDomain(domain: String) = domainHandler.deleteDomain(domain)
    suspend fun deleteAllDomains() = domainHandler.deleteAllDomains()
}

class WhiteListTypeAdblock : BaseWhiteListType(), KoinComponent {
    override val titleId: Int = R.string.setting_title_whitelist
    private val adBlock: AdBlock by inject()
    override val domainHandler: DomainInterface by lazy { adBlock }
}

class BaseWhiteListTypeJavascript : BaseWhiteListType(), KoinComponent {
    override val titleId: Int = R.string.setting_title_whitelistJS
    private val javascript: Javascript by inject()
    override val domainHandler: DomainInterface by lazy { javascript }
}

class BaseWhiteListTypeCookie : BaseWhiteListType(), KoinComponent {
    override val titleId: Int = R.string.setting_title_whitelistCookie
    private val cookie: Cookie by inject()
    override val domainHandler: DomainInterface by lazy { cookie }
}
