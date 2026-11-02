package de.asdfghasdfgh.etsuran.activity

import android.os.Bundle
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.view.EBToast
import de.asdfghasdfgh.etsuran.view.compose.ListScaffold
import de.asdfghasdfgh.etsuran.view.compose.MmdSwitch
import de.asdfghasdfgh.etsuran.view.compose.MyTheme
import de.asdfghasdfgh.etsuran.view.dialog.DialogManager
import io.github.edsuns.adfilter.AdFilter
import io.github.edsuns.adfilter.DownloadState
import io.github.edsuns.adfilter.Filter
import io.github.edsuns.adfilter.FilterViewModel
import java.text.SimpleDateFormat
import java.util.*

private val filterDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)

class AdBlockSettingActivity : LocaleAwareComponentActivity() {

    private val viewModel: FilterViewModel = AdFilter.get().viewModel
    private val dialogManager by lazy { DialogManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyTheme {
                val showAddDialog = remember { mutableStateOf(false) }
                SettingsScreen(
                    viewModel = viewModel,
                    addFilterDialog = { showAddDialog.value = true },
                    deleteFilterDialog = { filter -> showDeleteDialog(filter) },
                    onCloseAction = { finish() }
                )
                if (showAddDialog.value) {
                    AddFilterDialog(
                        onDismiss = { showAddDialog.value = false },
                        onAdd = { url ->
                            if (url.isNotBlank() && URLUtil.isNetworkUrl(url)) {
                                val filter = viewModel.addFilter("", url)
                                viewModel.download(filter.id)
                                showAddDialog.value = false
                            } else {
                                EBToast.show(this@AdBlockSettingActivity, R.string.adblock_invalid_url)
                            }
                        },
                    )
                }
            }
        }
    }

    private fun showDeleteDialog(filter: Filter) {
        dialogManager.showOkCancelDialog(
            title = getString(R.string.adblock_delete_filter),
            message = getString(R.string.adblock_confirm_delete_filter),
            okAction = {
                viewModel.removeFilter(filter.id)
            }
        )
    }
}

@Composable
private fun AddFilterDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        title = {
            Text(
                text = stringResource(R.string.adblock_add_filter),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onBackground,
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.adblock_add_filter)) },
                textStyle = MaterialTheme.typography.body1,
            )
        },
        confirmButton = {
            TextButton(onClick = { onAdd(text) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
fun SettingsScreen(
    viewModel: FilterViewModel,
    addFilterDialog: () -> Unit,
    deleteFilterDialog: (Filter) -> Unit,
    onCloseAction: () -> Unit = {},
) {
    val filters = viewModel.filters.collectAsState(initial = emptyMap())
    val context = LocalContext.current

    ListScaffold(
        title = stringResource(R.string.settings),
        onBack = { (context as ComponentActivity).onBackPressedDispatcher.onBackPressed() },
        actions = {
            IconButton(onClick = { filters.value.keys.forEach { viewModel.download(it) } }) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.cd_update))
            }
            IconButton(onClick = addFilterDialog) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.adblock_add_filter))
            }
            IconButton(onClick = { onCloseAction() }) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.cd_close))
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            LazyColumn {
                val filterList = filters.value.values.toList()
                items(filterList.size, key = { filterList[it].id }) { index ->
                    val filter = filterList[index]
                    Column {
                        FilterRow(filter, filterDateFormat,
                            onClick = {
                                viewModel.download(it.id)
                            },
                            onLongClick = { deleteFilterDialog(it) },
                            onToggled = { filter, enabled ->
                                viewModel.setFilterEnabled(filter.id, enabled)
                            }
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilterRow(
    filter: Filter,
    dateFormatter: SimpleDateFormat,
    onClick: (Filter) -> Unit,
    onLongClick: (Filter) -> Unit,
    onToggled: (Filter, Boolean) -> Unit,
) {
    // keyed so the switch reflects fresh data when the filters flow re-emits
    val isChecked = remember(filter.isEnabled) { mutableStateOf(filter.isEnabled) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = { onClick(filter) },
                onLongClick = { onLongClick(filter) }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (filter.name.isBlank()) filter.url else filter.name,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
            )
            Text(
                text = filter.url,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.65f)
            )
            Row {
                Text(
                    modifier = Modifier.weight(1f),
                    text = when (filter.downloadState) {
                        DownloadState.ENQUEUED -> stringResource(R.string.adblock_state_waiting)
                        DownloadState.DOWNLOADING -> stringResource(R.string.adblock_state_downloading)
                        DownloadState.INSTALLING -> stringResource(R.string.adblock_state_installing)
                        DownloadState.FAILED -> stringResource(R.string.adblock_state_failed)
                        DownloadState.CANCELLED -> stringResource(R.string.adblock_state_cancelled)
                        else -> {
                            if (filter.hasDownloaded())
                                dateFormatter.format(Date(filter.updateTime))
                            else stringResource(R.string.adblock_not_downloaded)
                        }
                    },
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.65f)
                )
                if (filter.hasDownloaded()) {
                    Text(
                        text = stringResource(R.string.adblock_filter_count, filter.filtersCount),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.65f)
                    )
                }
            }
        }
        MmdSwitch(
            checked = isChecked.value,
            onCheckedChange = {
                isChecked.value = it
                onToggled(filter, it)
            },
            enabled = filter.filtersCount > 0
        )
    }
}


