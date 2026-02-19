package com.whomade.kycarrots

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AddressSearchActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_search)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "주소 검색"

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.run {
            settings.javaScriptCanOpenWindowsAutomatically = true
            addJavascriptInterface(AndroidBridge(), "Android")
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            // Daum post code api requires a static HTML hosted on a server.
            // For demonstration, we use a public one or a local bridge.
            loadUrl("https://toss-payments-postcode.vercel.app/") // Sample public bridge
        }
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun processAddress(zipCode: String, address: String) {
            val intent = Intent().apply {
                putExtra("zipCode", zipCode)
                putExtra("address", address)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
