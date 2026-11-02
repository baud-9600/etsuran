package de.asdfghasdfgh.etsuran.browser

import android.app.Activity
import android.content.Context
import android.net.http.SslError
import android.security.KeyChain
import android.util.Log
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import de.asdfghasdfgh.etsuran.R
import de.asdfghasdfgh.etsuran.preference.ConfigManager
import de.asdfghasdfgh.etsuran.view.dialog.DialogManager
import de.asdfghasdfgh.etsuran.view.dialog.compose.AuthenticationDialogFragment

class WebViewSslHandler(
    private val context: Context,
    private val config: ConfigManager,
) {
    private val dialogManager: DialogManager by lazy { DialogManager(context as Activity) }

    fun onReceivedHttpAuthRequest(handler: HttpAuthHandler?) {
        AuthenticationDialogFragment { username, password ->
            handler?.proceed(username, password)
        }.show((context as FragmentActivity).supportFragmentManager, "AuthenticationDialog")
    }

    // return true means it's processed
    private fun handlePrivateKeyAlias(request: ClientCertRequest, alias: String?): Boolean {
        val keyAlias = alias ?: return false
        val holder = context as? Activity ?: return false
        try {
            val certChain = KeyChain.getCertificateChain(holder, keyAlias) ?: return false
            val privateKey = KeyChain.getPrivateKey(holder, keyAlias) ?: return false
            request.proceed(privateKey, certChain)
            return true
        } catch (e: Exception) {
            Log.e(
                "ebWebViewClient",
                "Error when getting CertificateChain or PrivateKey for alias '${alias}'",
                e
            )
        }
        return false
    }

    fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest, fallback: () -> Unit) {
        val holder = view.context as? Activity ?: return
        KeyChain.choosePrivateKeyAlias(
            holder,
            { alias ->
                if (!handlePrivateKeyAlias(request, alias)) {
                    fallback()
                }
            },
            request.keyTypes, request.principals, request.host, request.port, null
        )
    }

    fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        val reason = when (error.primaryError) {
            SslError.SSL_UNTRUSTED -> context.getString(R.string.ssl_error_reason_untrusted)
            SslError.SSL_EXPIRED -> context.getString(R.string.ssl_error_reason_expired)
            SslError.SSL_IDMISMATCH -> context.getString(R.string.ssl_error_reason_hostname_mismatch)
            SslError.SSL_NOTYETVALID -> context.getString(R.string.ssl_error_reason_not_yet_valid)
            SslError.SSL_DATE_INVALID -> context.getString(R.string.ssl_error_reason_date_invalid)
            SslError.SSL_INVALID -> context.getString(R.string.ssl_error_reason_invalid)
            else -> ""
        }
        val title = context.getString(R.string.ssl_error_title)
        val message = context.getString(R.string.ssl_error_message, reason)

        Log.e(TAG, "onReceivedSslError: $message")
        if (config.browser.enableCertificateErrorDialog) {
            dialogManager.showOkCancelDialog(
                title = title,
                message = message,
                showInCenter = true,
                okAction = { handler.proceed() },
                cancelAction = { handler.cancel() }
            )
        } else {
            handler.proceed()
        }
    }

    companion object {
        private const val TAG = "ebWebViewClient"
    }
}
