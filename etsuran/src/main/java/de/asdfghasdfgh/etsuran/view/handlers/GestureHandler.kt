package de.asdfghasdfgh.etsuran.view.handlers

import de.asdfghasdfgh.etsuran.browser.BrowserAction

class GestureHandler(
    private val dispatch: (BrowserAction) -> Unit,
) {
    fun handle(action: BrowserAction) {
        if (action is BrowserAction.Noop) return
        dispatch(action)
    }
}
