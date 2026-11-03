package de.asdfghasdfgh.etsuran.view.dialog

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.asdfghasdfgh.etsuran.view.compose.MmdDialogContent
import de.asdfghasdfgh.etsuran.view.compose.MmdDialogButtons
import de.asdfghasdfgh.etsuran.view.compose.MmdTextField

data class MmdInputField(
    val label: String,
    val value: String = "",
    val keyboardType: KeyboardType = KeyboardType.Text,
)

/**
 * One MMD dialog with several labelled fields (used by the split-search
 * editor, which used to ask two questions in two separate dialogs).
 * Returns the entered values in field order, or null when cancelled.
 */
class MultiFieldInputDialog(
    private val context: Context,
    private val title: String,
    private val message: String = "",
    private val fields: List<MmdInputField>,
) {
    suspend fun show(): List<String>? =
        ComposeInputDialog<List<String>>(context) { finish ->
            val values = remember { mutableStateListOf(*fields.map { it.value }.toTypedArray()) }
            MmdDialogContent(title = title, message = message) {
                Column {
                    fields.forEachIndexed { index, field ->
                        MmdTextField(
                            value = values[index],
                            onValueChange = { values[index] = it },
                            label = field.label,
                            keyboardType = field.keyboardType,
                        )
                        if (index != fields.lastIndex) Spacer(Modifier.height(12.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    MmdDialogButtons(
                        onConfirm = { finish(values.toList()) },
                        onCancel = { finish(null) },
                    )
                }
            }
        }.show()
}
