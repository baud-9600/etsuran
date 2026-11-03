package de.asdfghasdfgh.etsuran.view.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Form pieces shared by every settings dialog.
 *
 * MMD asks for 56dp text fields with an 8dp corner, 2dp outlines and filled,
 * black-on-paper actions. Material 2's defaults are the opposite (grey
 * hairline, 4dp corner, tinted text buttons), so the colours and geometry are
 * pinned here once instead of in each dialog.
 */
@Composable
fun MmdTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val ink = MaterialTheme.colors.onBackground
    val paper = MaterialTheme.colors.background
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        textStyle = MaterialTheme.typography.body1,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = themedItemShape(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = ink,
            backgroundColor = paper,
            cursorColor = ink,
            focusedBorderColor = ink,
            unfocusedBorderColor = ink,
            focusedLabelColor = ink,
            unfocusedLabelColor = ink,
            placeholderColor = ink.copy(alpha = 0.6f),
        ),
    )
}

/** MMD's dialog actions: a filled primary button and an outlined cancel button. */
@Composable
fun MmdDialogButtons(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    confirmLabel: String = stringResource(android.R.string.ok),
    cancelLabel: String = stringResource(android.R.string.cancel),
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        OutlinedButton(
            onClick = onCancel,
            shape = themedItemShape(),
            border = themedButtonBorder(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colors.onBackground,
            ),
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(cancelLabel, style = MaterialTheme.typography.button)
        }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onConfirm,
            enabled = confirmEnabled,
            shape = themedItemShape(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.onBackground,
                contentColor = MaterialTheme.colors.background,
                disabledBackgroundColor = MaterialTheme.colors.onBackground.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colors.background.copy(alpha = 0.8f),
            ),
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(confirmLabel, style = MaterialTheme.typography.button)
        }
    }
}

/** Title, optional explanation, caller-supplied fields and the action row. */
@Composable
fun MmdDialogContent(
    title: String,
    message: String = "",
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h3,
            color = MaterialTheme.colors.onBackground,
        )
        if (message.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

/** A dialog body with a single text field and confirm/cancel actions. */
@Composable
fun MmdTextInputContent(
    title: String,
    message: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    confirmEnabled: Boolean = true,
    fieldModifier: Modifier = Modifier,
) {
    MmdDialogContent(title = title, message = message) {
        Column {
            MmdTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = fieldModifier,
                label = label,
                placeholder = placeholder,
                singleLine = singleLine,
                keyboardType = keyboardType,
            )
            Spacer(Modifier.height(16.dp))
            MmdDialogButtons(
                onConfirm = onConfirm,
                onCancel = onCancel,
                confirmEnabled = confirmEnabled,
            )
        }
    }
}
