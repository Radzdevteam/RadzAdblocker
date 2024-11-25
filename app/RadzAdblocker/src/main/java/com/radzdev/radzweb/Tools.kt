package com.radzdev.radzweb

import android.content.Context
import android.os.Build
import android.webkit.WebSettings

object Tools {
    fun getUserAgent(c: Context?, desktopMode: Boolean): String {
        val mobilePrefix = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + ")"
        val desktopPrefix = "Mozilla/5.0 (X11; Linux " + System.getProperty("os.arch") + ")"

        var newUserAgent = WebSettings.getDefaultUserAgent(c)
        val prefix = newUserAgent.substring(0, newUserAgent.indexOf(")") + 1)

        if (desktopMode) {
            try {
                newUserAgent = newUserAgent.replace(prefix, desktopPrefix)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                newUserAgent = newUserAgent.replace(prefix, mobilePrefix)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return newUserAgent
    }

}
