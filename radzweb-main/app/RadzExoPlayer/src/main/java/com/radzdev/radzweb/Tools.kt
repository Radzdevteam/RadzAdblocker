package com.radzdev.radzweb

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.os.Build
import android.text.ClipboardManager
import android.webkit.WebSettings
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

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

    @SuppressLint("NewApi")
    @Suppress("deprecation")
    @Throws(Exception::class)
    fun copyToClipboard(context: Context, text: String?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = text
        } else {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText("Message", text)
            clipboard.setPrimaryClip(clip)
        }
    }

    fun readRaw(c: Context, src: Int): Array<String> {
        var line: String? = ""
        val stringBuilder = StringBuilder()
        val is1 = c.resources.openRawResource(src)
        val br1 = BufferedReader(InputStreamReader(is1))
        if (is1 != null) {
            try {
                while ((br1.readLine().also { line = it }) != null) {
                    stringBuilder.append(line).append("\n")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return stringBuilder.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
    }
}
