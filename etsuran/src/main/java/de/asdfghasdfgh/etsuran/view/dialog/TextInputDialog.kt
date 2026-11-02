package de.asdfghasdfgh.etsuran.view.dialog

import android.app.AlertDialog
import android.content.Context

import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import de.asdfghasdfgh.etsuran.R
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import de.asdfghasdfgh.etsuran.view.withThemedFrame


class TextInputDialog(
   private val context: Context,
   private val title: String,
   private val message: String,
   private val defaultText: String = "",
) {
    suspend fun show() = suspendCoroutine<String?> { continuation ->
        val density = context.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        val editText = EditText(context).apply {
            setText(defaultText)
            setSingleLine()
            textSize = 20f
            minHeight = dp(48)
            setPadding(0, dp(8), 0, dp(8))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
        // A full-width field with MMD's 16dp inset. The framework AlertDialog
        // otherwise measures EditText at its hint width and left-aligns it.
        val field = FrameLayout(context).apply {
            setPadding(dp(20), dp(4), dp(20), dp(4))
            addView(editText)
        }

        AlertDialog.Builder(context, R.style.TouchAreaDialog)
                .setTitle(title)
                .setMessage(message)
                .setView(field)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    val text = editText.text.toString()
                    continuation.resume(text)
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                    continuation.resume(null)
                }.show().withThemedFrame()
    }
}