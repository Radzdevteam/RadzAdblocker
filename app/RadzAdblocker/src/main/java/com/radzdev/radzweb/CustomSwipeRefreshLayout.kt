package com.radzdev.radzweb

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class CustomSwipeRefreshLayout(context: Context, attrs: AttributeSet) : SwipeRefreshLayout(context, attrs) {

    // Reference to the WebView to check if it's at the top
    private var webView: WebView? = null

    // Set the WebView when it's available
    fun setWebView(webView: WebView) {
        this.webView = webView
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Only allow swipe refresh if the WebView is at the top
        if (isWebViewAtTop()) {
            // Check if the user is pressing near the center of the screen
            val centerY = height / 2
            val touchY = ev.y

            // Define the area where pull-to-refresh should be disabled (near the center)
            val centerAreaThreshold = 0.4f // Adjust this value to control the center area (40% of the height)

            // Prevent refresh if touch is near the center of the screen
            if (touchY in (centerY - (centerAreaThreshold * centerY))..(centerY + (centerAreaThreshold * centerY))) {
                return false
            }
            return super.onInterceptTouchEvent(ev)
        }
        return false // Block the gesture if WebView is not at the top
    }

    // Function to check if the WebView is at the top of the page
    private fun isWebViewAtTop(): Boolean {
        return webView?.scrollY == 0
    }
}
