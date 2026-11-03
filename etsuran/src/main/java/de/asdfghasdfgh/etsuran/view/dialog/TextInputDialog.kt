package de.asdfghasdfgh.etsuran.view.dialog

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import de.asdfghasdfgh.etsuran.view.compose.MmdTextInputContent

/**
 * A settings-sized text prompt that follows MMD instead of the framework
 * dialog: 56dp outlined field, filled primary action, outlined cancel.
 *
 * The old implementation used an AlertDialog's own positive/negative buttons.
 * On this device the framework renders the positive button as a black slab
 * under black text, so "OK" was invisible.
 */
class TextInputDialog(
    private val context: Context,
    private val title: String,
    private val message: String,
    private val defaultText: String = "",
    private val singleLine: Boolean = true,
    private val hint: String = "",
) {
    suspend fun show(): String? =
        ComposeInputDialog<String>(context) { finish ->
            var text by remember { mutableStateOf(defaultText) }
            val focusRequester = remember { FocusRequester() }
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboard?.show()
            }
            MmdTextInputContent(
                title = title,
                message = message,
                value = text,
                onValueChange = { text = it },
                onConfirm = { finish(text) },
                onCancel = { finish(null) },
                placeholder = hint.ifBlank { null },
                singleLine = singleLine,
                fieldModifier = Modifier.focusRequester(focusRequester),
            )
        }.show()
}
