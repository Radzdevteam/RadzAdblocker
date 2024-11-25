@file:Suppress("DEPRECATION")

package com.radzdev.radzweb

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.net.http.SslError
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.radzdev.radzexoplayer.R
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class radzadblocker : ComponentActivity() {

    private lateinit var webView: VideoEnabledWebView
    private lateinit var webChromeClient: VideoEnabledWebChromeClient
    private lateinit var loadingAnimationView: LottieAnimationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mainDomain: String
    private lateinit var wakeLock: PowerManager.WakeLock


    @SuppressLint("SetJavaScriptEnabled", "ResourceAsColor", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize WebView first
        webView = findViewById(R.id.webView)

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // Acquire WakeLock to keep the screen on
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "radzadblocker:WakeLock"
        )
        wakeLock.acquire(10*60*1000L /*10 minutes*/)

        // Now set the WebView to the CustomSwipeRefreshLayout
        if (swipeRefreshLayout is CustomSwipeRefreshLayout) {
            (swipeRefreshLayout as CustomSwipeRefreshLayout).setWebView(webView)
        }

        swipeRefreshLayout.setOnRefreshListener {
            webView.reload() // Reload the current page
        }

        // Initialize other views
        webView.setBackgroundColor(android.R.color.transparent)
        val nonVideoLayout: View = findViewById(R.id.nonVideoLayout)
        val videoLayout: ViewGroup = findViewById(R.id.videoLayout)
        loadingAnimationView = findViewById(R.id.loading_animation)

        showLoadingDialog()

        // Get the URL from the intent
        val url = intent.getStringExtra("url") ?: "https://google.com/"
        mainDomain = extractDomain(url)
        loadContent(url)

        // Set up the WebChromeClient for video and fullscreen support
        val loadingView: View = layoutInflater.inflate(R.layout.view_loading_video, null)
        webChromeClient = VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView)
        webChromeClient.setOnToggledFullscreen(object : VideoEnabledWebChromeClient.ToggledFullscreenCallback {
            override fun toggledFullscreen(fullscreen: Boolean) {
                val attrs = window.attributes
                if (fullscreen) {
                    // Keep the screen on during fullscreen
                    if (!wakeLock.isHeld) {
                        wakeLock.acquire(10*60*1000L /*10 minutes*/)
                    }
                    attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    window.attributes = attrs
                    adjustScreen(true)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    // Release the WakeLock when exiting fullscreen
                    if (wakeLock.isHeld) {
                        wakeLock.release()
                    }
                    attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
                    window.attributes = attrs
                    adjustScreen(false)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        })


        webView.webChromeClient = webChromeClient
        webView.webViewClient = InsideWebViewClient()

        // Configure WebSettings for the WebView
        val webSettings = webView.settings
        webSettings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = true
            builtInZoomControls = true
            setSupportZoom(true)
            displayZoomControls = false
            useWideViewPort = false
            loadWithOverviewMode = false
            userAgentString = Tools.getUserAgent(this@radzadblocker, false)
            webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = URL(url)
            uri.host
        } catch (e: Exception) {
            ""
        }
    }


    private fun loadContent(url: String) {
        // Load the URL in WebView
        webView.loadUrl(url)
    }

    private inner class InsideWebViewClient : WebViewClient() {

        // Declare a variable to hold the fetched ad hosts
        private var adHosts = mutableListOf<String>()

        private val redirectPattern = """(redirect|ref|click|url|go|jump|forward|redir)\?url=([a-zA-Z0-9\-._~:/?#\\@!$&'()*+,;%=]+)"""

        init {
            // Fetch ad hosts from the online source
            "https://raw.githubusercontent.com/Radzdevteam/test/refs/heads/main/adhost".fetchAdHosts()
        }

        // Method to fetch ad hosts from the URL
        private fun String.fetchAdHosts() {
            // Start a background thread to download the ad hosts
            Thread {
                try {
                    val url = URL(this)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000 // 5 seconds timeout
                    connection.readTimeout = 5000
                    connection.requestMethod = "GET"

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            adHosts.add(line!!.trim())
                        }
                        reader.close()
                        Log.d("WebView", "Ad hosts loaded: ${adHosts.size} entries")
                    } else {
                        Log.e("WebView", "Failed to fetch ad hosts: HTTP ${connection.responseCode}")
                    }
                } catch (e: Exception) {
                    Log.e("WebView", "Error fetching ad hosts: ${e.message}")
                }
            }.start()
        }

        // Handle errors like connection issues and show custom error page
        @Deprecated("Deprecated in Java")
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)

            // URL of the custom error page (404.html in assets)
            val errorPageUrl = "file:///android_asset/404/404.html"

            // Check for network error conditions
            if (description.contains("ERR_NAME_NOT_RESOLVED") ||
                description.contains("ERR_ADDRESS_UNREACHABLE") ||
                description.contains("ERR_CONNECTION_ABORTED") ||
                description.contains("ERR_TIMED_OUT")) {
                view?.loadUrl(errorPageUrl) // Load custom error page
            }
        }

        @Deprecated("Deprecated in Java")
        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
            Log.d("WebView", "Loading URL: $url")

            // Block URLs that start with 'intent://'
            if (url != null && url.startsWith("intent://")) {
                return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
            }

            // Regex to block unwanted popups and ad URLs
            val popupPattern1 = "^https?://(?:www\\.|[a-z0-9]{7,10}\\.)?[a-z0-9-]{5,}\\.(?:com|bid|link|live|online|top|club)//?(?:[a-z0-9]{2}/){2,3}[a-f0-9]{32}\\.js$"
            val popupPattern2 = "^https?://(?:[a-z]{2}\\.)?[0-9a-z]{5,16}\\.[a-z]{3,7}/[a-z](?=[a-z]{0,25}[0-9A-Z])[0-9a-zA-Z]{3,26}/\\d{4,5}(?:\\?[_v]=\\d+)?$"

            // Block popups based on patterns
            if (url != null && (url.matches(Regex(popupPattern1)) || url.matches(Regex(popupPattern2)))) {
                return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
            }

            // Block URLs based on known ad hosts (using the dynamically fetched list)
            if (url != null && adHosts.any { url.contains(it) }) {
                return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
            }

            // Block redirect ad URLs by pattern (directly use redirectPattern without extra `.*`)
            if (url != null && Regex(redirectPattern).containsMatchIn(url)) {
                return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
            }

            return super.shouldInterceptRequest(view, url)
        }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url != null) {
                val newDomain = extractDomain(url)

                // Block if the new domain does not match the main domain
                if (newDomain != mainDomain) {
                    return true
                }

                // Additional checks for intent URLs and ad hosts
                if (url.startsWith("intent://")) {
                    return true
                }

                if (adHosts.any { url.contains(it) } || Regex(redirectPattern).containsMatchIn(url)) {
                    return true
                }
            }
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
            super.onPageStarted(view, url, favicon)
            showLoadingDialog()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            // Ensure the page finishes loading only if it's not a blocked URL
            if (url != null && (adHosts.any { url.contains(it) } || Regex(redirectPattern).containsMatchIn(url))) {
                return
            }
            super.onPageFinished(view, url)
            Log.d("WebView", "Page finished loading: $url")
            hideLoadingDialog()
            swipeRefreshLayout.isRefreshing = false
        }

        // Handle SSL
        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            handler?.proceed()
        }
    }

    private fun showLoadingDialog() {
        loadingAnimationView.visibility = View.VISIBLE
        loadingAnimationView.playAnimation()
    }

    private fun hideLoadingDialog() {
        loadingAnimationView.visibility = View.GONE
    }

    private fun adjustScreen(isFullScreen: Boolean) {
        val uiOptions = if (isFullScreen) {
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }
        window.decorView.systemUiVisibility = uiOptions
    }

    @Override
    override fun onPause() {
        super.onPause()
        // Release the WakeLock when the app is paused
        if (this::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    @Override
    override fun onStop() {
        super.onStop()
        // Release the WakeLock when the app stops
        if (this::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            AlertDialog.Builder(this)
                .setMessage("Do you want to exit?")
                .setPositiveButton("Yes") { _, _ -> super.onBackPressed() }
                .setNegativeButton("No", null)
                .show()
        }
    }
}