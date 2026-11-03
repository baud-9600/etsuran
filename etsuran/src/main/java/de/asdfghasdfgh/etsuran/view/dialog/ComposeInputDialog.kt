package de.asdfghasdfgh.etsuran.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.view.ThemedBorders
import de.asdfghasdfgh.etsuran.view.compose.MyTheme
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Hosts a Compose dialog in a plain [Dialog] so settings prompts are not
 * bound to AlertDialog's button bar (whose positive button is black on black
 * on this device).
 */
internal class ComposeInputDialog<T>(
    context: Context,
    private val content: @Composable (finish: (T?) -> Unit) -> Unit,
) {
    private val activity = context.findActivity() ?: context

    suspend fun show(): T? = suspendCoroutine { continuation ->
        var finished = false
        lateinit var dialog: Dialog
        fun finish(value: T?) {
            if (finished) return
            finished = true
            dialog.dismiss()
            continuation.resume(value)
        }

        val composeView = ComposeView(activity).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            (activity as? LifecycleOwner)?.let { setViewTreeLifecycleOwner(it) }
            (activity as? SavedStateRegistryOwner)?.let { setViewTreeSavedStateRegistryOwner(it) }
            setContent {
                MyTheme {
                    content { finish(it) }
                }
            }
        }

        dialog = Dialog(activity, R.style.TouchAreaDialog).apply {
            setContentView(
                composeView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
            setCanceledOnTouchOutside(true)
            setOnCancelListener { finish(null) }
            window?.apply {
                setBackgroundDrawable(ThemedBorders.dialogFrame(activity))
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                // A raw Dialog with a ComposeView reports no text editor, so
                // AlertController would never target the IME for it.
                clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }
        dialog.setOnDismissListener {
            if (!finished) {
                finished = true
                continuation.resume(null)
            }
        }
        dialog.show()
        dialog.window?.setBackgroundDrawable(ThemedBorders.dialogFrame(activity))
    }
}

private tailrec fun Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
