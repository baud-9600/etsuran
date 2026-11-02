package de.asdfghasdfgh.etsuran.view.dialog

import android.content.Context
import android.widget.ProgressBar
import android.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.unit.ShareUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import de.asdfghasdfgh.etsuran.view.withThemedFrame

class ReceiveDataDialog(
    private val context: Context,
    private val lifecycleCoroutineScope: LifecycleCoroutineScope,
) {
    fun show(afterAction: (String) -> Unit) {
        val dialog = AlertDialog.Builder(context, R.style.TouchAreaDialog).apply {
            setView(ProgressBar(context))
            setPositiveButton(R.string.done) { _, _ -> ShareUtil.stopBroadcast() }
            setTitle(R.string.menu_receive)
            setOnDismissListener {
                afterAction("")
            }
        }.show().withThemedFrame()

        ShareUtil.startReceiving(lifecycleCoroutineScope) {
            lifecycleCoroutineScope.launch(Dispatchers.Main) {
                dialog.dismiss()
                afterAction(it)
            }
            ShareUtil.stopBroadcast()
        }
    }
}