package de.asdfghasdfgh.etsuran.view.dialog

import android.content.Context
import android.app.AlertDialog
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale
import de.asdfghasdfgh.etsuran.view.withThemedFrame

class TtsLanguageDialog(val context: Context) : KoinComponent {
    private val config: ConfigManager by inject()

    fun show(locales: List<Locale>) {
        val availableLocales = locales.sortedBy { it.displayName }
        val availableLocalDisplayNames = availableLocales.map { it.displayName }.toTypedArray()

        AlertDialog.Builder(context, R.style.TouchAreaDialog).apply {
            setTitle(context.getString(R.string.tts_language_title))
            setSingleChoiceItems(
                availableLocalDisplayNames, availableLocales.indexOf(config.tts.ttsLocale)
            ) { dialog, selectedIndex ->
                val locale = availableLocales[selectedIndex]
                config.tts.ttsLocale = locale
                dialog.dismiss()
            }
        }.create().withThemedFrame().show()
    }
}
