package com.whomade.kycarrots

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.whomade.kycarrots.databinding.ActivityPaymentWebviewBinding
import java.net.URISyntaxException

class PaymentWebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderNo = intent.getStringExtra("orderNo") ?: ""
        val amount = intent.getIntExtra("amount", 0)
        var productName = intent.getStringExtra("productName") ?: ""
        val clientKey = "test_ck_yL0qZ4G1VOdympo01WQYroWb2MQY"

        android.util.Log.d("PaymentWebView", "orderNo: $orderNo, amount: $amount, productName: $productName")

        if (productName.isEmpty()) {
            productName = "상품 결제" // Fallback if server provides empty name
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "결제하기"

        setupWebView(clientKey, orderNo, amount, productName)
        WebView.setWebContentsDebuggingEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupWebView(clientKey: String, orderNo: String, amount: Int, productName: String) {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    android.util.Log.d("PaymentWebView", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url.toString()

                    // Handle success/fail redirects
                    if (url.contains("payment-success")) {
                        val paymentKey = request?.url?.getQueryParameter("paymentKey")
                        val orderId = request?.url?.getQueryParameter("orderId")
                        val amountVal = request?.url?.getQueryParameter("amount")?.toIntOrNull() ?: amount
                        
                        val resultIntent = Intent().apply {
                            putExtra("status", "SUCCESS")
                            putExtra("paymentKey", paymentKey)
                            putExtra("orderId", orderId)
                            putExtra("amount", amountVal)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                        return true
                    } else if (url.contains("payment-fail")) {
                        val message = request?.url?.getQueryParameter("message")
                        val resultIntent = Intent().apply {
                            putExtra("status", "FAIL")
                            putExtra("message", message)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                        return true
                    }

                    // Handle intent schemes for banking/card apps
                    if (url.startsWith("intent:") || url.startsWith("market:") || !url.startsWith("http")) {
                        try {
                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                                return true
                            }
                            // If app is not installed, go to Play Store
                            val packageName = intent.`package`
                            if (packageName != null) {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                                return true
                            }
                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                        }
                    }
                    return false
                }
            }

            // Load Toss Payments Checkout HTML
            val successUrl = "http://localhost/payment-success"
            val failUrl = "http://localhost/payment-fail"
            
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <script src="https://js.tosspayments.com/v1"></script>
                </head>
                <body>
                    <script>
                        document.addEventListener("DOMContentLoaded", function() {
                            try {
                                var tossPayments = TossPayments("$clientKey");
                                tossPayments.requestPayment('CARD', {
                                    amount: $amount,
                                    orderId: '${orderNo.replace("'", "\\'")}',
                                    orderName: '${productName.replace("'", "\\'")}',
                                    successUrl: '$successUrl',
                                    failUrl: '$failUrl',
                                });
                            } catch (e) {
                                console.error("TossPayments error:", e);
                            }
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()

            loadDataWithBaseURL("https://tosspayments.com", html, "text/html", "UTF-8", null)
        }
    }
}
