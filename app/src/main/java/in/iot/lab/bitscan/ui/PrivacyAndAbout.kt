package `in`.iot.lab.bitscan.ui

import `in`.iot.lab.bitscan.R
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_privacy_and_about.*


class PrivacyAndAbout : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_and_about)

        webView.webViewClient = object : WebViewClient() {
            // Links clicked will be shown on the webview
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return super.shouldOverrideUrlLoading(view, url)
            }
        }



        val i = intent
        val page=i.getStringExtra("page")

        if (page=="privacy")
            webView.loadUrl("https://docs.google.com/document/d/1tzVfhWpSQFAN_lxBNyo3CwzI2SkLTTIcHEDbD1u3W7k/edit?usp=sharing")
        else
            webView.loadUrl("https://docs.google.com/document/d/1SHACDjyEA2hGt1iqrtElf50VNXjOju_NpRTOOHPiRUs/edit?usp=sharing")



    }
}