package de.asdfghasdfgh.etsuran.view.dialog

import android.content.Context
import android.widget.ProgressBar
import android.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.unit.ShareUtil
import de.asdfghasdfgh.etsuran.view.withThemedFrame

class SendLinkDialog(
    private val context: Context,
    private val lifecycleCoroutineScope: LifecycleCoroutineScope,
) {
    fun show(url: String) {
        ShareUtil.startBroadcastingUrl(lifecycleCoroutineScope, url)

        AlertDialog.Builder(context, R.style.TouchAreaDialog).apply {
            setView(ProgressBar(context))
            setNeutralButton(R.string.done) { _, _ -> ShareUtil.stopBroadcast() }
            setTitle(R.string.menu_send_link)
            setOnDismissListener { ShareUtil.stopBroadcast() }
        }.show().withThemedFrame()
    }
}