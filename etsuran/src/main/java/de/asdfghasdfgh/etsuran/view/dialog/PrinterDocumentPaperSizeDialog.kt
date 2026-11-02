package de.asdfghasdfgh.etsuran.view.dialog

import android.content.Context
import android.app.AlertDialog
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.preference.PaperSize
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import de.asdfghasdfgh.etsuran.view.withThemedFrame

class PrinterDocumentPaperSizeDialog(val context: Context) : KoinComponent {
    private val config: ConfigManager by inject()

    fun show() {
        val paperSizes = PaperSize.values().map { it.sizeString }.toTypedArray()

        AlertDialog.Builder(context, R.style.TouchAreaDialog).apply {
            setTitle(context.getString(R.string.dialog_paper_size))
            setSingleChoiceItems(paperSizes, config.display.pdfPaperSize.ordinal) { dialog, selectedIndex ->
                config.display.pdfPaperSize = PaperSize.values()[selectedIndex]
                dialog.dismiss()
            }
        }.create().withThemedFrame().show()
    }
}