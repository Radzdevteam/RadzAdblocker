package com.radzdev.radzweb

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView

/**
 * This class serves as a WebView to be used in conjunction with a VideoEnabledWebChromeClient.
 * It makes possible:
 * - To detect the HTML5 video ended event so that the VideoEnabledWebChromeClient can exit full-screen.
 *
 * Important notes:
 * - Javascript is enabled by default and must not be disabled with getSettings().setJavaScriptEnabled(false).
 * - setWebChromeClient() must be called before any loadData(), loadDataWithBaseURL() or loadUrl() method.
 *
 * @author Cristian Perez (http://cpr.name)
 */
class VideoEnabledWebView : WebView {
    inner class JavascriptInterface {
        @android.webkit.JavascriptInterface
        @Suppress("unused")
        fun notifyVideoEnd() // Must match Javascript interface method of VideoEnabledWebChromeClient
        {
            Log.d("___", "GOT IT")
            // This code is not executed in the UI thread, so we must force that to happen
            Handler(Looper.getMainLooper()).post {
                if (videoEnabledWebChromeClient != null) {
                    videoEnabledWebChromeClient!!.onHideCustomView()
                }
            }
        }
    }

    private var videoEnabledWebChromeClient: VideoEnabledWebChromeClient? = null
    private var addedJavascriptInterface: Boolean

    @Suppress("unused")
    constructor(context: Context) : super(context) {
        addedJavascriptInterface = false
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        addedJavascriptInterface = false
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        addedJavascriptInterface = false
    }

    @get:Suppress("unused")
    val isVideoFullscreen: Boolean
        /**
         * Indicates if the video is being displayed using a custom view (typically full-screen)
         * @return true it the video is being displayed using a custom view (typically full-screen)
         */
        get() = videoEnabledWebChromeClient != null && videoEnabledWebChromeClient!!.isVideoFullscreen

    /**
     * Pass only a VideoEnabledWebChromeClient instance.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun setWebChromeClient(client: WebChromeClient?) {
        settings.javaScriptEnabled = true
        if (client is VideoEnabledWebChromeClient) {
            this.videoEnabledWebChromeClient = client
        }
        super.setWebChromeClient(client)
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        addJavascriptInterface()
        super.loadData(data, mimeType, encoding)
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        addJavascriptInterface()
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun loadUrl(url: String) {
        addJavascriptInterface()
        super.loadUrl(url)
    }

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        addJavascriptInterface()
        super.loadUrl(url, additionalHttpHeaders)
    }

    private fun addJavascriptInterface() {
        if (!addedJavascriptInterface) {
            // Add javascript interface to be called when the video ends (must be done before page load)
            addJavascriptInterface(
                JavascriptInterface(),
                "_VideoEnabledWebView"
            ) // Must match Javascript interface name of VideoEnabledWebChromeClient

            addedJavascriptInterface = true
        }
    }
}
