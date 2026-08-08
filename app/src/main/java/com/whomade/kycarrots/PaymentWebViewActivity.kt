package com.whomade.kycarrots

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.PaymentConfirmRequest
import com.whomade.kycarrots.databinding.ActivityPaymentWebviewBinding
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import kotlinx.coroutines.launch
import java.net.URISyntaxException

class PaymentWebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentWebviewBinding
    private var orderId: String = ""
    private var orderNo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getStringExtra("orderId") ?: ""
        orderNo = intent.getStringExtra("orderNo") ?: ""
        val amount = intent.getIntExtra("amount", 0)
        var productName = intent.getStringExtra("productName") ?: ""
        val clientKey = LoginInfoUtil.getTossClientKey(this)

        android.util.Log.d("PaymentWebView", "orderId: $orderId, orderNo: $orderNo, amount: $amount, productName: $productName")

        if (productName.isEmpty()) {
            productName = "상품 결제" // Fallback if server provides empty name
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "결제하기"

        setupWebView(clientKey, orderNo, amount, productName)
        WebView.setWebContentsDebuggingEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //menu?.add(0, 1, 0, "성공확인")?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            binding.webView.url?.let {
                if (!checkAndHandleRedirect(it)) {
                    Toast.makeText(this, "결제 완료 후 눌러주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupWebView(clientKey: String, paymentOrderId: String, amount: Int, productName: String) {
        binding.webView.apply {
            settings.run {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                loadWithOverviewMode = true
                useWideViewPort = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                setSupportMultipleWindows(false)
            }
            
            // Allow cookies for session persistence
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    android.util.Log.d("PaymentWebView", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    url?.let { checkAndHandleRedirect(it) }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return checkAndHandleRedirect(request?.url.toString())
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    request?.url?.toString()?.let { checkAndHandleRedirect(it) }
                }
            }

            // Load Toss Payments Checkout HTML
            // val successUrl = "https://example.com/payment-success"
            // val failUrl = "https://example.com/payment-fail"
            val successUrl = "https://www.asagong.com/api/payment/payment-success"
            val failUrl = "https://www.asagong.com/api/payment/payment-fail"
            
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
                                    orderId: '${paymentOrderId.replace("'", "\\'")}',
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

    private fun checkAndHandleRedirect(url: String): Boolean {
        android.util.Log.d("PaymentWebView", "Checking URL: $url")

        // Handle success/fail redirects
        if (url.contains("payment-success")) {
            val uri = Uri.parse(url)
            val paymentKey = uri.getQueryParameter("paymentKey") ?: ""
            val orderNoFromUrl = uri.getQueryParameter("orderId") ?: orderNo
            val amount = intent.getIntExtra("amount", 0) // get default from intent
            val amountVal = uri.getQueryParameter("amount")?.toIntOrNull() ?: amount

            confirmPayment(paymentKey, orderNoFromUrl, amountVal)
            return true
        } else if (url.contains("payment-fail")) {
            val uri = Uri.parse(url)
            val message = uri.getQueryParameter("message") ?: "결제에 실패했습니다."
            showErrorAndFinish(message)
            return true
        }

        // Handle intent schemes for banking/card apps
        if (!url.startsWith("http")) {
            if (url.startsWith("intent://")) {
                val parsed = parseIntentUrl(url)
                if (parsed != null) {
                    val schemeUrl = parsed["schemeUrl"] ?: ""
                    val packageName = parsed["package"] ?: ""

                    if (schemeUrl.isNotEmpty()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(schemeUrl))
                            startActivity(intent)
                            return true
                        } catch (e: Exception) {
                            // ignore and try fallback
                        }
                    }

                    if (packageName.isNotEmpty()) {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                            return true
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
                return true
            }

            // Normal custom schemes (wooripay://, kakaotalk://, etc.)
            try {
                val decodedUrl = Uri.decode(url)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(decodedUrl))
                startActivity(intent)
            } catch (e: Exception) {
                // ignore
            }
            return true
        }
        return false
    }

    private fun parseIntentUrl(url: String): Map<String, String>? {
        try {
            if (!url.startsWith("intent://")) return null
            val intentIndex = url.indexOf("#Intent;")
            if (intentIndex == -1) return null

            val uriPath = url.substring(9, intentIndex)
            val paramsStr = url.substring(intentIndex + 8)
            val params = paramsStr.split(";")

            var scheme = ""
            var packageName = ""
            for (param in params) {
                if (param.startsWith("scheme=")) {
                    scheme = param.substring(7)
                } else if (param.startsWith("package=")) {
                    packageName = param.substring(8)
                }
            }

            return mapOf(
                "schemeUrl" to if (scheme.isNotEmpty()) "$scheme://$uriPath" else "",
                "package" to packageName
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun confirmPayment(paymentKey: String, paymentOrderId: String, amount: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.webView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val userNo = LoginInfoUtil.getUserNo(this@PaymentWebViewActivity).toLongOrNull()
                val api = RetrofitProvider.retrofit.create(AdApi::class.java)
                val request = PaymentConfirmRequest(
                    paymentKey = paymentKey,
                    orderNo = paymentOrderId,
                    amount = amount,
                    userNo = userNo
                )
                val response = api.confirmPayment(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val resultIntent = Intent().apply {
                        putExtra("status", "SUCCESS")
                        putExtra("paymentKey", paymentKey)
                        putExtra("orderId", orderId) // PK
                        putExtra("orderNo", orderNo)
                        putExtra("amount", amount)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    val message = response.body()?.message ?: "결제 승인에 실패했습니다."
                    showErrorAndFinish(message)
                }
            } catch (e: Exception) {
                showErrorAndFinish("결제 승인 중 오류: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showErrorAndFinish(message: String) {
        val resultIntent = Intent().apply {
            putExtra("status", "FAIL")
            putExtra("message", message)
        }
        setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}
