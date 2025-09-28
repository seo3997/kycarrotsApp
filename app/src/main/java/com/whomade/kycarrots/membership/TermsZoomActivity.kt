package com.whomade.kycarrots.membership

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.MailTo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.whomade.kycarrots.R

class TermsZoomActivity : AppCompatActivity() {

    companion object {
        private const val EX_TITLE = "ex_title"
        private const val EX_URL   = "ex_url"
        private const val EX_HTML  = "ex_html"
        private const val EX_ZOOM  = "ex_zoom"

        /** 호출 헬퍼 */
        fun start(
            context: Context,
            title: String,
            url: String? = null,
            html: String? = null,
            textZoom: Int = 140
        ) {
            val i = Intent(context, TermsZoomActivity::class.java).apply {
                putExtra(EX_TITLE, title)
                putExtra(EX_URL,   url)
                putExtra(EX_HTML,  html)
                putExtra(EX_ZOOM,  textZoom)
            }
            context.startActivity(i)
        }
    }

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_zoom)

        val toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
            title = intent.getStringExtra(EX_TITLE).orEmpty()
            setNavigationOnClickListener { finish() }
        }
        setSupportActionBar(toolbar)

        // 1) Up 버튼 활성화 (액션바 기준)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)

        webView = findViewById(R.id.webView)
        progress = findViewById(R.id.progress)

        // 배경 충돌 방지
        webView.setBackgroundColor(Color.WHITE)

        with(webView.settings) {
            javaScriptEnabled = false
            domStorageEnabled = true

            // 확대 기능
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // 화면 맞춤
            useWideViewPort = true
            loadWithOverviewMode = true

            // 텍스트 시작 배율
            textZoom = intent.getIntExtra(EX_ZOOM, 140)

            // 보안/호환
            loadsImagesAutomatically = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) safeBrowsingEnabled = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) forceDark = WebSettings.FORCE_DARK_OFF
            // 혼합콘텐츠(필요 시 허용)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progress.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                progress.visibility = View.GONE
            }

            // 새 창/외부 스킴 처리
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                return handleExternalSchemes(uri)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val uri = url?.let { Uri.parse(it) } ?: return false
                return handleExternalSchemes(uri)
            }
        }

        // 뒤로가기: WebView 내 히스토리 우선
        onBackPressedDispatcher.addCallback(this) {
            if (this@TermsZoomActivity::webView.isInitialized && webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }

        // 로드
        val url = intent.getStringExtra(EX_URL)
        val html = intent.getStringExtra(EX_HTML)
        when {
            !url.isNullOrBlank() -> webView.loadUrl(url)
            !html.isNullOrBlank() -> webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
            else -> {
                Toast.makeText(this, "표시할 약관이 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /** tel:, mailto:, 외부 앱 스킴 처리 + http/https는 내부 로드 */
    private fun handleExternalSchemes(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase().orEmpty()

        // mailto:
        if (scheme == "mailto") {
            val mt = MailTo.parse(uri.toString())
            val email = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${mt.to}")).apply {
                putExtra(Intent.EXTRA_SUBJECT, mt.subject)
                putExtra(Intent.EXTRA_TEXT, mt.body)
                putExtra(Intent.EXTRA_CC, mt.cc)
            }
            startActivity(Intent.createChooser(email, "이메일 보내기"))
            return true
        }

        // tel:, sms:, market:, intent:, etc. → 외부 앱으로
        if (scheme in listOf("tel", "sms", "smsto", "mms", "mmsto", "market", "intent")) {
            runCatching {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            return true
        }

        // http/https: 내부에서 그대로 로드
        return false
    }

    override fun onDestroy() {
        // 메모리 누수 방지
        runCatching {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.removeAllViews()
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { onBackPressedDispatcher.onBackPressed(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
