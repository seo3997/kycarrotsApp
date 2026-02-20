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
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no">
                    <script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
                    <style>
                        html, body, #layer { width:100%; height:100%; margin:0; padding:0; }
                    </style>
                </head>
                <body>
                    <div id="layer"></div>
                    <script>
                        var element_layer = document.getElementById('layer');
                        new daum.Postcode({
                            oncomplete: function(data) {
                                if (window.Android) {
                                    window.Android.processAddress(data.zonecode, data.address);
                                }
                            },
                            width : '100%',
                            height : '100%'
                        }).embed(element_layer);
                    </script>
                </body>
                </html>
            """.trimIndent()
            
            loadDataWithBaseURL("https://daum.net", html, "text/html", "UTF-8", null)
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
