package de.asdfghasdfgh.etsuran.activity

import android.graphics.Point
import android.widget.FrameLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.asdfghasdfgh.etsuran.browser.AlbumController
import de.asdfghasdfgh.etsuran.view.CenterExpandProgressBar
import de.asdfghasdfgh.etsuran.view.EBWebView
import de.asdfghasdfgh.etsuran.view.MainActivityLayout
import de.asdfghasdfgh.etsuran.view.TranslationPanelView
import de.asdfghasdfgh.etsuran.view.viewControllers.ComposeToolbarViewController
import de.asdfghasdfgh.etsuran.view.viewControllers.FabImageViewController
import de.asdfghasdfgh.etsuran.view.viewControllers.StatusbarViewController

/**
 * Shared mutable state for BrowserActivity and its delegates.
 * Replaces the provider/setter lambdas that were passed to each delegate individually.
 */
class BrowserState {
    var isWebApp: Boolean = false
    lateinit var ebWebView: EBWebView
    val isWebViewInitialized: Boolean get() = ::ebWebView.isInitialized
    lateinit var binding: MainActivityLayout

    var currentAlbumController: AlbumController? = null
    var searchOnSite: Boolean = false
    var longPressPoint: Point = Point(0, 0)

    lateinit var mainContentLayout: FrameLayout
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var translationPanelView: TranslationPanelView
    lateinit var progressBar: CenterExpandProgressBar
    lateinit var progressBarVertical: CenterExpandProgressBar
    lateinit var composeToolbarViewController: ComposeToolbarViewController
    lateinit var fabImageViewController: FabImageViewController
    lateinit var statusbarViewController: StatusbarViewController
}
