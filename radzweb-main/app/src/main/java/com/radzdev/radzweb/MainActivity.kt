package com.radzdev.radzexoplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.radzdev.radzexoplayer.ui.theme.RadzExoPlayerTheme
import com.radzdev.radzweb.radzweb

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, radzweb::class.java)
        intent.putExtra("url", "https://strm.great-site.net/")
        startActivity(intent)
        finish()
    }
}
