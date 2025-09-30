package com.whomade.kycarrots.webview

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.snackbar.Snackbar
import com.whomade.kycarrots.BaseDrawerActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.databinding.ActivityWebViewBinding
import com.whomade.kycarrots.ui.Noti.NotificationListActivity
import com.whomade.kycarrots.ui.common.NotificationBadgeHelper

class WebViewActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private var badge: BadgeDrawable? = null

    // <input type="file"> 콜백
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    // Android 13 미만 저장 권한
    private lateinit var requestPermission: ActivityResultLauncher<String>
    private var scrollListener: ViewTreeObserver.OnScrollChangedListener? = null

    // 파일 선택 런처 (Activity Result API)
    private val pickFiles = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris: Array<Uri>? = if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            when {
                data?.clipData != null -> {
                    val clip: ClipData = data.clipData!!
                    Array(clip.itemCount) { i -> clip.getItemAt(i).uri }
                }
                data?.data != null -> arrayOf(data.data!!)
                else -> null
            }
        } else null
        fileChooserCallback?.onReceiveValue(uris)
        fileChooserCallback = null
    }

    private val boardUrl by lazy {
        // 사용자가 말한대로 슬래시 없이 사용
        intent.getStringExtra("url") ?: (Constants.BASE_URL + "front/board/selectPageListBoard.do")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        //binding = ActivityWebViewBinding.inflate(layoutInflater)
        binding = ActivityWebViewBinding.bind(findViewById(R.id.main_content))
        //setContentView(binding.root)

        initToolbar()
        initPermissionLauncher()
        setupWebView()
        setupSwipeRefresh()

        // 뒤로가기: WebView history 우선
        onBackPressedDispatcher.addCallback(this) {
            if (binding.webView.canGoBack()) binding.webView.goBack() else finish()
        }

        // 최초 로드
        binding.webView.loadUrl(boardUrl)
    }

    /** DashboardActivity와 동일한 스타일의 Toolbar/메뉴 구조 */
    private fun initToolbar() {
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu) // 햄버거 아이콘
            title = initialTitle ?: "공지사항"   // 넘겨받은 타이틀이 있으면 사용
        }
        // (DashboardActivity도 홈 클릭을 따로 처리하지 않으므로 동일하게 둠)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // DashboardActivity와 동일한 뱃지 부착
        val toolbar = binding.toolbar
        badge = NotificationBadgeHelper.attach(
            activity = this,
            menu = menu,
            toolbar = toolbar,
            menuItemId = R.id.action_notifications
        )
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
        return true
    }

    override fun onResume() {
        super.onResume()
        // DashboardActivity와 동일: 돌아올 때 뱃지 갱신
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                startActivity(Intent(this, NotificationListActivity::class.java))
                true
            }
            // (DashboardActivity도 홈 처리 안 하므로 동일하게 둠. 필요하면 android.R.id.home 처리 추가)
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initPermissionLauncher() {
        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Snackbar.make(binding.root, "저장 권한이 거부되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() = with(binding.webView) {
        // ===== WebSettings =====
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.userAgentString = settings.userAgentString + " KyCarrotsApp/Android"
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        }
        CookieManager.getInstance().setAcceptCookie(true)

        // ===== ChromeClient (프로그레스/파일선택/새창) =====
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                binding.progress.visibility = View.VISIBLE
                binding.progress.setProgressCompat(newProgress, true)
                if (newProgress >= 100) binding.progress.visibility = View.GONE
            }
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (initialTitle == null && !title.isNullOrBlank()) {
                    supportActionBar?.title = title
                }
            }
            // target="_blank" 새창 -> 같은 WebView에서 처리
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message?
            ): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                transport.webView = this@WebViewActivity.binding.webView
                resultMsg.sendToTarget()
                return true
            }

            // 파일 선택
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = filePathCallback

                val intent = fileChooserParams?.createIntent()
                    ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                return try {
                    pickFiles.launch(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = null
                    Snackbar.make(binding.root, "파일 선택기를 열 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                    false
                }
            }
        }

        // ===== WebViewClient (내부 네비/에러/외부스킴) =====
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                if (request == null) return false
                val url = request.url.toString()
                val scheme = request.url.scheme ?: ""
                return handleUrl(url, scheme)
            }

            // 구형 대응
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                val scheme = try { Uri.parse(url).scheme ?: "" } catch (_: Exception) { "" }
                return handleUrl(url, scheme)
            }

            private fun handleUrl(url: String, scheme: String): Boolean {
                when {
                    url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:") -> {
                        return try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); true }
                        catch (_: Exception) { false }
                    }
                    url.startsWith("intent://") -> {
                        return try { startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME)); true }
                        catch (_: ActivityNotFoundException) { false }
                    }
                    !scheme.equals("http", true) && !scheme.equals("https", true) -> {
                        return try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); true }
                        catch (_: ActivityNotFoundException) { false }
                    }
                }
                return false // http(s)는 WebView가 처리
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.swipe.isRefreshing = false
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    Snackbar.make(binding.root, "페이지를 불러오지 못했습니다.", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        // ===== 다운로드 처리 =====
        setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val req = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                allowScanningByMediaScanner()
            }
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
            Snackbar.make(binding.root, "다운로드 시작: $fileName", Snackbar.LENGTH_SHORT).show()
        }

        // 앱 ↔ 웹 브리지
        addJavascriptInterface(BoardBridge(), "AndroidBridge")

        // 당겨서 새로고침: 최상단에서만 작동 (API 21 호환)
        scrollListener = ViewTreeObserver.OnScrollChangedListener {
            binding.swipe.isEnabled = (this.scrollY == 0)
        }
        viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    private fun setupSwipeRefresh() {
        binding.swipe.setOnRefreshListener { binding.webView.reload() }
    }

    // CLEAR_TOP | SINGLE_TOP 진입 시 새 인텐트 수신
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getStringExtra(EXTRA_URL)?.let { binding.webView.loadUrl(it) }
        intent?.getStringExtra(EXTRA_TITLE)?.let { newTitle ->
            if (!newTitle.isNullOrBlank()) {
                supportActionBar?.title = newTitle
            }
        }
    }

    override fun onDestroy() {
        // 스크롤 리스너 해제
        scrollListener?.let { l ->
            val vto = binding.webView.viewTreeObserver
            if (vto.isAlive) vto.removeOnScrollChangedListener(l)
        }
        scrollListener = null

        // WebView 메모리 릭 방지
        (binding.webView.parent as? ViewGroup)?.removeView(binding.webView)
        binding.webView.apply {
            stopLoading()
            loadUrl("about:blank")
            removeJavascriptInterface("AndroidBridge")
            webViewClient = object : WebViewClient() {}
            webChromeClient = object : WebChromeClient() {}
            clearHistory()
            clearCache(true)
            onPause()
            destroy()
        }
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null

        super.onDestroy()
    }

    inner class BoardBridge {
        @JavascriptInterface
        fun showToast(msg: String) {
            runOnUiThread {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun refresh() {
            runOnUiThread { binding.webView.reload() }
        }
    }

    companion object {
        private const val EXTRA_URL = "url"
        private const val EXTRA_TITLE = "title"

        fun start(
            context: Context,
            url: String,
            title: String? = null,
            reuseTop: Boolean = true
        ) {
            val intent = Intent(context, WebViewActivity::class.java)
                .putExtra(EXTRA_URL, url)
                .putExtra(EXTRA_TITLE, title)
                .apply {
                    if (reuseTop) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(intent)
        }
    }

    private val initialUrl by lazy {
        intent.getStringExtra(EXTRA_URL) ?: (Constants.BASE_URL + "front/board/selectPageListBoard.do")
    }
    private val initialTitle by lazy {
        intent.getStringExtra(EXTRA_TITLE) // null이면 페이지 타이틀로 대체
    }
}
