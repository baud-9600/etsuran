@file:Suppress("DEPRECATION")

package de.asdfghasdfgh.etsuran

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import de.asdfghasdfgh.etsuran.activity.BrowserActivity
import de.asdfghasdfgh.etsuran.activity.SettingActivity
import de.asdfghasdfgh.etsuran.browser.AdBlock
import de.asdfghasdfgh.etsuran.view.compose.UiThemeState
import de.asdfghasdfgh.etsuran.browser.Cookie
import de.asdfghasdfgh.etsuran.browser.Javascript
import de.asdfghasdfgh.etsuran.database.BookmarkManager
import de.asdfghasdfgh.etsuran.database.RecordRepository
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.search.suggestion.SearchSuggestionViewModel
import de.asdfghasdfgh.etsuran.data.remote.GoogleDriveRepository
import de.asdfghasdfgh.etsuran.data.remote.InstapaperRepository
import de.asdfghasdfgh.etsuran.service.TtsManager
import de.asdfghasdfgh.etsuran.service.TtsNotificationManager
import de.asdfghasdfgh.etsuran.service.WebSpeechHandler
import de.asdfghasdfgh.etsuran.unit.EinkImageCache
import de.asdfghasdfgh.etsuran.unit.LocaleManager
import de.asdfghasdfgh.etsuran.util.WebViewUtil
import io.github.edsuns.adfilter.AdFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import de.asdfghasdfgh.etsuran.viewmodel.ActionModeMenuViewModel
import de.asdfghasdfgh.etsuran.viewmodel.ExternalSearchViewModel
import de.asdfghasdfgh.etsuran.viewmodel.GptQueryViewModel
import de.asdfghasdfgh.etsuran.viewmodel.HighlightViewModel
import de.asdfghasdfgh.etsuran.viewmodel.InstapaperViewModel
import de.asdfghasdfgh.etsuran.viewmodel.RemoteConnViewModel
import de.asdfghasdfgh.etsuran.viewmodel.SavedPageViewModel
import de.asdfghasdfgh.etsuran.viewmodel.TranslationViewModel
import de.asdfghasdfgh.etsuran.viewmodel.TtsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import timber.log.Timber

class EtsuranApplication : Application() {

    private val sp: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private val einkImageCache: EinkImageCache by lazy { EinkImageCache(this) }

    private val config: ConfigManager by lazy {
        ConfigManager(applicationContext, sp)
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val ttsManager: TtsManager = TtsManager(this, appScope)

    private val myModule = module {
        single<CoroutineScope> { appScope }
        single { config }
        single { sp }
        single { BookmarkManager(androidContext()) }
        single { de.asdfghasdfgh.etsuran.unit.FaviconFetcher(get()) }
        single { einkImageCache }
        single { de.asdfghasdfgh.etsuran.userscript.UserScriptManager(androidContext()) }
        single { RecordRepository() }
        single { AdBlock(androidContext()) }
        single { Javascript(androidContext()) }
        single { Cookie(androidContext()) }
        single { ttsManager }
        single { TtsNotificationManager(androidContext()) }
        single { WebSpeechHandler(androidContext()) }
        single { InstapaperRepository() }
        single { GoogleDriveRepository() }
        single { SearchSuggestionViewModel() }
        viewModel { TranslationViewModel(get(), get()) }
        viewModel { TtsViewModel(get(), get(), get()) }
        viewModel { ActionModeMenuViewModel(get()) }
        viewModel { ExternalSearchViewModel(get()) }
        viewModel { RemoteConnViewModel(get()) }
        viewModel { InstapaperViewModel(get()) }
        viewModel { GptQueryViewModel(get()) }
        viewModel { HighlightViewModel(get()) }
        viewModel { SavedPageViewModel(get()) }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@EtsuranApplication)
            modules(myModule)
        }


        UiThemeState.current.value = config.display.uiTheme
        UiThemeState.darkMode.value = config.display.darkMode
        UiThemeState.customColor.value = androidx.compose.ui.graphics.Color(config.display.customThemeColor)
        UiThemeState.uiBorder.value = config.display.uiBorder
        UiThemeState.uiFill.value = config.display.uiFill
        UiThemeState.inverted.value = config.display.uiThemeInverted
        UiThemeState.gradientAngle.value = config.display.gradientAngle
        UiThemeState.gradientLevel.value = config.display.gradientLevel

        if (config.uiLocaleLanguage.isNotEmpty()) {
            LocaleManager.setLocale(this, config.uiLocaleLanguage)
        }

        setupAdBlock()
    }

    private fun setupAdBlock() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val filter = AdFilter.create(this)
        // Enabling loads every processed filter blob (multi-MB file reads plus
        // native parsing), so it must not run on the main thread before the
        // first frame. Until it finishes the detector simply has no clients
        // and shouldIntercept blocks nothing.
        appScope.launch(Dispatchers.IO) {
            filter.setEnabled(config.browser.adBlock)
        }
        if (config.browser.adBlock) {
            appScope.launch {
                filter.viewModel.activeDownloads.collect { notifyDownloading(it.isEmpty()) }
            }
        }
    }

    // The image cache is process-wide, so it is trimmed from the Application
    // rather than from an Activity that may already be gone when the system
    // asks for memory back.
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        einkImageCache.trimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        einkImageCache.clearMemory()
    }

    override fun onTerminate() {
        super.onTerminate()
        ttsManager.release()
    }


    private var isDownloading = false
    private val channelId = "DOWNLOAD"
    private val notificationId = 1

    private fun notifyDownloading(finished: Boolean) {
        if (isDownloading != finished) {// only accept valid event
            return
        }

        val clazz = if (finished) BrowserActivity::class.java else SettingActivity::class.java
        val intent = Intent(this, clazz).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId).apply {
            setContentTitle(getString(R.string.notification_adblock_title))
            setContentIntent(pendingIntent)
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setVibrate(longArrayOf(0L))
            setSound(null)
            priority = NotificationCompat.PRIORITY_HIGH
        }
        if (finished) {
            isDownloading = false
            builder.apply {
                setContentText(getString(R.string.notification_download_complete))
                setSmallIcon(android.R.drawable.stat_sys_download_done)
                setProgress(0, 0, false)
                setOngoing(false)
            }
        } else {
            isDownloading = true
            builder.apply {
                setContentText(getString(R.string.notification_download_in_progress))
                setSmallIcon(android.R.drawable.stat_sys_download)
                setProgress(0, 0, true)
                setOngoing(true)// make the notification unable to be cleared
            }
        }
        NotificationManagerCompat.from(this).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name: CharSequence = getString(R.string.notification_channel_filter_download)
                val description: String = getString(R.string.notification_channel_filter_download_desc)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(channelId, name, importance)
                channel.setSound(null, null)
                channel.description = description

                // Add the channel
                createNotificationChannel(channel)
            }
            // Check if notification permission is granted
            if (ContextCompat.checkSelfPermission(
                    this@EtsuranApplication,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val notification = builder.build()
                cancel(notificationId)
                notify(notificationId, notification)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        (this@EtsuranApplication as BrowserActivity),
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1
                    )
                }
            }
        }
    }

    // This snippet borrow from mihon
    // https://github.com/mihonapp/mihon/blob/81871a34694c8e408d907731292b7266c5b993cc/app/src/main/java/eu/kanade/tachiyomi/App.kt#L231
    override fun getPackageName(): String {
        try {
            // Override the value passed as X-Requested-With in WebView requests
            val stackTrace = Looper.getMainLooper().thread.stackTrace
            val isChromiumCall = stackTrace.any { trace ->
                trace.className.lowercase() in setOf("org.chromium.base.buildinfo", "org.chromium.base.apkinfo") &&
                        trace.methodName.lowercase() in setOf("getall", "getpackagename", "<init>")
            }

            if (isChromiumCall) return WebViewUtil.spoofedPackageName(applicationContext)
        } catch (_: Exception) {
        }

        return super.getPackageName()
    }

}
