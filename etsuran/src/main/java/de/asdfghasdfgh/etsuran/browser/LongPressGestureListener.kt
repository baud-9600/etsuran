package de.asdfghasdfgh.etsuran.browser

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import de.asdfghasdfgh.etsuran.view.EBWebView

class LongPressGestureListener(private var webView: EBWebView) : SimpleOnGestureListener() {
    private var longPress = true


    override fun onContextClick(e: MotionEvent): Boolean {
        return super.onContextClick(e)
    }

    override fun onLongPress(e: MotionEvent) {
        if (longPress) {
            webView.onLongPress(e)
        }
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        longPress = false
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        longPress = true
    }
}