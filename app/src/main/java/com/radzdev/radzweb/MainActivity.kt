package com.radzdev.radzexoplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.radzdev.radzweb.radzadblocker

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, radzadblocker::class.java)
        intent.putExtra("url", "https://strm.great-site.net/")
        startActivity(intent)
        finish()
    }
}
