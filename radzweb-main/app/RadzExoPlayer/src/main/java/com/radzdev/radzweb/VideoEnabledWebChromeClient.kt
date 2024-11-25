package com.radzdev.radzweb

import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import android.widget.VideoView

/**
 * This class serves as a WebChromeClient to be set to a WebView, allowing it to play video.
 * Video will play differently depending on target API level (in-line, fullscreen, or both).
 *
 * It has been tested with the following video classes:
 * - android.widget.VideoView (typically API level <11)
 * - android.webkit.HTML5VideoFullScreen$VideoSurfaceView/VideoTextureView (typically API level 11-18)
 * - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView (typically API level 19+)
 *
 * Important notes:
 * - For API level 11+, android:hardwareAccelerated="true" must be set in the application manifest.
 * - The invoking activity must call VideoEnabledWebChromeClient's onBackPressed() inside of its own onBackPressed().
 * - Tested in Android API levels 8-19. Only tested on http://m.youtube.com.
 *
 * @author Cristian Perez (http://cpr.name)
 */
class VideoEnabledWebChromeClient : WebChromeClient, OnPreparedListener, OnCompletionListener,
    MediaPlayer.OnErrorListener {
    private var getScrollY = 0

    interface ToggledFullscreenCallback {
        fun toggledFullscreen(fullscreen: Boolean)
    }

    private var activityNonVideoView: View? = null
    private var activityVideoView: ViewGroup? = null
    private var loadingView: View? = null
    private var webView: VideoEnabledWebView? = null

    /**
     * Indicates if the video is being displayed using a custom view (typically full-screen)
     * @return true it the video is being displayed using a custom view (typically full-screen)
     */
    var isVideoFullscreen: Boolean =
        false // Indicates if the video is being displayed using a custom view (typically full-screen)
        private set
    private var videoViewContainer: FrameLayout? = null
    private var videoViewCallback: CustomViewCallback? = null

    private var toggledFullscreenCallback: ToggledFullscreenCallback? = null

    /**
     * Never use this constructor alone.
     * This constructor allows this class to be defined as an inline inner class in which the user can override methods
     */
    @Suppress("unused")
    constructor()

    /**
     * Builds a video enabled WebChromeClient.
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     * @param activityVideoView A ViewGroup in the activity's layout that will display the video. Typically you would like this to fill the whole layout.
     */
    @Suppress("unused")
    constructor(activityNonVideoView: View?, activityVideoView: ViewGroup?) {
        this.activityNonVideoView = activityNonVideoView
        this.activityVideoView = activityVideoView
        this.loadingView = null
        this.webView = null
        this.isVideoFullscreen = false
    }

    /**
     * Builds a video enabled WebChromeClient.
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     * @param activityVideoView A ViewGroup in the activity's layout that will display the video. Typically you would like this to fill the whole layout.
     * @param loadingView A View to be shown while the video is loading (typically only used in API level <11). Must be already inflated and not attached to a parent view.
     */
    @Suppress("unused")
    constructor(activityNonVideoView: View?, activityVideoView: ViewGroup?, loadingView: View?) {
        this.activityNonVideoView = activityNonVideoView
        this.activityVideoView = activityVideoView
        this.loadingView = loadingView
        this.webView = null
        this.isVideoFullscreen = false
    }

    /**
     * Builds a video enabled WebChromeClient.
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     * @param activityVideoView A ViewGroup in the activity's layout that will display the video. Typically you would like this to fill the whole layout.
     * @param loadingView A View to be shown while the video is loading (typically only used in API level <11). Must be already inflated and not attached to a parent view.
     * @param webView The owner VideoEnabledWebView. Passing it will enable the VideoEnabledWebChromeClient to detect the HTML5 video ended event and exit full-screen.
     * Note: The web page must only contain one video tag in order for the HTML5 video ended event to work. This could be improved if needed (see Javascript code).
     */
    @Suppress("unused")
    constructor(
        activityNonVideoView: View?,
        activityVideoView: ViewGroup?,
        loadingView: View?,
        webView: VideoEnabledWebView?
    ) {
        this.activityNonVideoView = activityNonVideoView
        this.activityVideoView = activityVideoView
        this.loadingView = loadingView
        this.webView = webView
        this.isVideoFullscreen = false
    }

    /**
     * Set a callback that will be fired when the video starts or finishes displaying using a custom view (typically full-screen)
     * @param callback A VideoEnabledWebChromeClient.ToggledFullscreenCallback callback
     */
    @Suppress("unused")
    fun setOnToggledFullscreen(callback: ToggledFullscreenCallback?) {
        this.toggledFullscreenCallback = callback
    }

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        if (view is FrameLayout) {
            // A video wants to be shown
            val frameLayout = view
            val focusedChild = frameLayout.focusedChild

            // Save video related variables
            this.isVideoFullscreen = true
            this.videoViewContainer = frameLayout
            this.videoViewCallback = callback

            // Hide the non-video view, add the video view, and show it
            activityNonVideoView!!.visibility = View.INVISIBLE
            activityVideoView!!.addView(
                videoViewContainer,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            activityVideoView!!.visibility = View.VISIBLE

            if (focusedChild is VideoView) {
                // android.widget.VideoView (typically API level <11)
                val videoView = focusedChild

                // Handle all the required events
                videoView.setOnPreparedListener(this)
                videoView.setOnCompletionListener(this)
                videoView.setOnErrorListener(this)
            } else {
                // Other classes, including:
                // - android.webkit.HTML5VideoFullScreen$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 11-18)
                // - android.webkit.HTML5VideoFullScreen$VideoTextureView, which inherits from android.view.TextureView (typically API level 11-18)
                // - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 19+)

                // Handle HTML5 video ended event only if the class is a SurfaceView
                // Test case: TextureView of Sony Xperia T API level 16 doesn't work fullscreen when loading the javascript below

                if (webView != null && webView!!.settings.javaScriptEnabled && focusedChild is SurfaceView) {
                    // Run javascript code that detects the video end and notifies the Javascript interface
                    var js = "javascript:"
                    js += "var _ytrp_html5_video_last;"
                    js += "var _ytrp_html5_video = document.getElementsByTagName('video')[0];"
                    js += "if (_ytrp_html5_video != undefined && _ytrp_html5_video != _ytrp_html5_video_last) {"
                    run {
                        js += "_ytrp_html5_video_last = _ytrp_html5_video;"
                        js += "function _ytrp_html5_video_ended() {"
                        run {
                            js += "_VideoEnabledWebView.notifyVideoEnd();" // Must match Javascript interface name and method of VideoEnableWebView
                        }
                        js += "}"
                        js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);"
                    }
                    js += "}"
                    webView!!.loadUrl(js)
                }
            }

            // Notify full-screen change
            if (toggledFullscreenCallback != null) {
                getScrollY = webView!!.scrollY
                toggledFullscreenCallback!!.toggledFullscreen(true)
            }
        }
    }

    @Suppress("deprecation")
    override fun onShowCustomView(
        view: View,
        requestedOrientation: Int,
        callback: CustomViewCallback
    ) // Available in API level 14+, deprecated in API level 18+
    {
        onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        // This method should be manually called on video end in all cases because it's not always called automatically.
        // This method must be manually called on back key press (from this class' onBackPressed() method).

        if (isVideoFullscreen) {
            // Hide the video view, remove it, and show the non-video view
            activityVideoView!!.visibility = View.INVISIBLE
            activityVideoView!!.removeView(videoViewContainer)
            activityNonVideoView!!.visibility = View.VISIBLE

            // Call back (only in API level <19, because in API level 19+ with chromium webview it crashes)
            if (videoViewCallback != null && !videoViewCallback!!.javaClass.name.contains(".chromium.")) {
                videoViewCallback!!.onCustomViewHidden()
            }

            // Reset video related variables
            isVideoFullscreen = false
            videoViewContainer = null
            videoViewCallback = null

            // Notify full-screen change
            if (toggledFullscreenCallback != null) {
                webView!!.postDelayed({ webView!!.scrollTo(0, getScrollY) }, 100)
                toggledFullscreenCallback!!.toggledFullscreen(false)
            }
        }
    }

    override fun getVideoLoadingProgressView(): View? // Video will start loading
    {
        if (loadingView != null) {
            loadingView!!.visibility = View.VISIBLE
            return loadingView
        } else {
            return super.getVideoLoadingProgressView()
        }
    }

    override fun onPrepared(mp: MediaPlayer) // Video will start playing, only called in the case of android.widget.VideoView (typically API level <11)
    {
        if (loadingView != null) {
            loadingView!!.visibility = View.GONE
        }
    }

    override fun onCompletion(mp: MediaPlayer) // Video finished playing, only called in the case of android.widget.VideoView (typically API level <11)
    {
        onHideCustomView()
    }

    override fun onError(
        mp: MediaPlayer,
        what: Int,
        extra: Int
    ): Boolean // Error while playing video, only called in the case of android.widget.VideoView (typically API level <11)
    {
        return false // By returning false, onCompletion() will be called
    }

    /**
     * Notifies the class that the back key has been pressed by the user.
     * This must be called from the Activity's onBackPressed(), and if it returns false, the activity itself should handle it. Otherwise don't do anything.
     * @return Returns true if the event was handled, and false if was not (video view is not visible)
     */
    @Suppress("unused")
    fun onBackPressed(): Boolean {
        if (isVideoFullscreen) {
            onHideCustomView()
            return true
        } else {
            return false
        }
    }
}
