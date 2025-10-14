package com.whomade.kycarrots

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.whomade.kycarrots.dialog.DlgBtnActivity
import com.whomade.kycarrots.loginout.LoginActivity
import com.whomade.kycarrots.loginout.LoginInfo
import com.whomade.kycarrots.network.NetworkCheck
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.buy.ItemSelectionActivity
import kotlinx.coroutines.launch
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.kakao.sdk.common.util.Utility
import com.whomade.kycarrots.chatting.ChatActivity
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.message.PushTokenUtil


class IntroActivity : AppCompatActivity() {
    private var pushRoomId: String? = null
    private var pushBuyerId: String? = null
    private var pushSellerId: String? = null
    private var pushProductId: String? = null
    private var pushType: String? = null
    private var pushMsg: String? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        savePushIntentData(intent)
    }
    private fun savePushIntentData(intent: Intent?) {
        pushRoomId = intent?.getStringExtra("roomId")
        pushBuyerId = intent?.getStringExtra("buyerId")
        pushSellerId = intent?.getStringExtra("sellerId")
        pushProductId = intent?.getStringExtra("productId")
        pushType = intent?.getStringExtra("type")
        pushMsg = intent?.getStringExtra("msg")
    }

    // (1) Activity의 멤버 변수(필드)로 선언!
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("알림", "알림 권한 허용됨")
            } else {
                Log.d("알림", "알림 권한 거부됨")
            }
        }

    companion object {
        const val ACTION_GCM_REGISTRATION = "com.cashcuk.intent.GCM_REGISTRATION"
        const val REQUEST_ERR = 999
    }

    private lateinit var mThisAppVersion: String
    private var regstrationId: String? = null
    private lateinit var mHandler: Handler
    private var mRunnable: Runnable? = null
    private var mRegHandler: Handler? = null
    private var mRegRunnable: Runnable? = null

    private lateinit var ad: AlertDialog.Builder
    private var isVersionMatch = true

    private lateinit var mIntroLoadingView: ImageView
    private lateinit var mLoadingAnimation: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_intro)

        val keyHash = Utility.getKeyHash(this)   // this = Activity/Context
        Log.d("KAKAO", "keyHash=$keyHash")

        // 1. 인텐트에서 푸시 데이터 추출 (항상 먼저 호출)
        savePushIntentData(intent)

        CheckLoginService.mActivityList.add(this)

        mHandler = Handler(Looper.getMainLooper())
        ad = AlertDialog.Builder(this)

        mIntroLoadingView = findViewById(R.id.iv_intro_loading)
        mLoadingAnimation = mIntroLoadingView.background as AnimationDrawable
        mIntroLoadingView.visibility = View.VISIBLE
        mLoadingAnimation.start()

        if (NetworkCheck.getConnectivityStatus(this) == NetworkCheck.TYPE_NOT_CONNECTED) {
            startActivityForResult(Intent(this, DlgBtnActivity::class.java).apply {
                putExtra("BtnDlgMsg", getString(R.string.str_network_error))
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }, REQUEST_ERR)
            return
        } else {
            val prefs = getSharedPreferences("SaveAppVersion", MODE_PRIVATE)
            val savedVersion = prefs.getString("AppVersion", "")
            mThisAppVersion = getAppVersion()

            if (savedVersion.isNullOrEmpty() || savedVersion != mThisAppVersion) {
                saveAppVersion(mThisAppVersion)
                isVersionMatch = false
            }

            checkMarketVersion()
        }

        checkAndRequestNotificationPermission()
    }

    private fun checkMarketVersion() {
        checkRegId()
    }

    private fun checkRegId() {
        val prefs = getSharedPreferences("SaveRegId", MODE_PRIVATE)
        val regId = prefs.getString("setRegId", "")
        Log.d("FCM", "checkRegId r**************egId: $regId")  // <- 로그로 출력!

        if (regId.isNullOrEmpty() || !isVersionMatch) {
            registerToken()
        } else {
            mRunnable = Runnable {
                autoLoginCheck()
            }
            mHandler.postDelayed(mRunnable!!, 1000)
        }
    }

    private fun autoLoginCheck() {
        mHandler.removeCallbacks(mRunnable ?: return)

        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val sUID       = prefs.getString("LogIn_ID",    "").orEmpty()
        val sPWD       = prefs.getString("LogIn_PWD",   "").orEmpty()
        val sMEMBERCODE= prefs.getString("LogIn_MEMBERCODE", "").orEmpty()
        Log.d("FCM", "autoLoginCheck: true")

        if (sMEMBERCODE == "ROLE_PROJ" && Constants.SYSTEM_TYPE.toString() == "1" ) {
            Toast.makeText(this, "직거래앱은 센터로 로그인 할수 없습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
            finish()
            return
        }
        if (sUID.isNotBlank() && sPWD.isNotBlank() && sMEMBERCODE.isNotBlank() && mThisAppVersion.isNotEmpty()) {
            val appService = AppServiceProvider.getService()

            lifecycleScope.launch {
                val resultCode = LoginInfo(this@IntroActivity, sUID, sPWD, sMEMBERCODE, mThisAppVersion, appService).login()
                if (resultCode == StaticDataInfo.RESULT_CODE_200) {
                    val regPrefs = getSharedPreferences("SaveRegId", MODE_PRIVATE)
                    val token = regPrefs.getString("setRegId", "") ?: ""
                    if (token.isNotBlank()) {
                        PushTokenUtil.sendTokenToServer(this@IntroActivity, token)
                    }
                    nextPage(true, sMEMBERCODE)
                } else {
                    nextPage(false, sMEMBERCODE)
                }
            }
        } else {
            // sUSERTYPE 이 비어 있을 수 있지만, nextPage 에 빈 문자열이라도 넘겨서 NPE 방지
            nextPage(false, sMEMBERCODE)
        }
    }

    private fun nextPage(isLogin: Boolean, memberCode: String) {

        // ROLE_PUB 푸시 토픽 구독
        if (memberCode == Constants.ROLE_PUB) {
            FirebaseMessaging.getInstance().subscribeToTopic(Constants.ROLE_PUB)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM", "ROLE_PUB 토픽 구독 성공")
                    } else {
                        Log.e("FCM", "ROLE_PUB 토픽 구독 실패", task.exception)
                    }
                }
        }

        val intent = when {
            isLogin -> createIntentForPushNavigation(memberCode, isLogin)
            else -> Intent(this, LoginActivity::class.java).apply {
                // 로그인 후 다시 채팅 or 상품 상세로 이동할 수 있도록 push 데이터 전달
                putExtra("roomId", pushRoomId)
                putExtra("buyerId", pushBuyerId)
                putExtra("sellerId", pushSellerId)
                putExtra("productId", pushProductId)
                putExtra("type", pushType)
                putExtra("msg", pushMsg)
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun createIntentForPushNavigation(memberCode: String, isLogin: Boolean): Intent {
        return when (pushType) {
            "chat" -> {
                // 채팅 데이터가 모두 존재해야 채팅으로 이동
                if (!pushRoomId.isNullOrBlank() && !pushBuyerId.isNullOrBlank()
                    && !pushSellerId.isNullOrBlank() && !pushProductId.isNullOrBlank()) {
                    Intent(this, ChatActivity::class.java).apply {
                        putExtra("roomId", pushRoomId)
                        putExtra("buyerId", pushBuyerId)
                        putExtra("sellerId", pushSellerId)
                        putExtra("productId", pushProductId)
                        putExtra("type", pushType)
                        putExtra("msg", pushMsg)
                    }
                } else {
                    defaultIntentForMember(memberCode)
                }
            }

            "product" -> {
                // productId만 있어도 상품 상세로 이동
                if (!pushProductId.isNullOrBlank()) {
                    Intent(this, AdDetailActivity::class.java).apply {
                        putExtra(AdDetailActivity.EXTRA_PRODUCT_ID, pushProductId)
                        putExtra(AdDetailActivity.EXTRA_USER_ID, pushSellerId)
                        putExtra("type", pushType)
                        putExtra("msg", pushMsg)
                    }
                } else {
                    defaultIntentForMember(memberCode)
                }
            }

            else -> {
                defaultIntentForMember(memberCode)
            }
        }
    }

    private fun defaultIntentForMember(memberCode: String): Intent {
        return if (memberCode == Constants.ROLE_SELL || memberCode == Constants.ROLE_PROJ) {
            Intent(this, DashboardActivity::class.java)
        } else {
            Intent(this, ItemSelectionActivity::class.java)
        }
    }
    private fun saveAppVersion(appVer: String) {
        val prefs = getSharedPreferences("SaveAppVersion", MODE_PRIVATE)
        prefs.edit().putString("AppVersion", appVer).apply()
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun showUpdateAlert() {
        mHandler.postDelayed({
            ad.setMessage(R.string.str_update_message)
                .setCancelable(false)
                .setPositiveButton(R.string.str_yes) { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=$packageName")
                    })
                    finish()
                }
                .setNegativeButton(R.string.str_no) { _, _ ->
                    finish()
                }
            ad.create().apply {
                setTitle(R.string.str_update_title)
                show()
            }
        }, 1000)
    }

    private fun registerToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "토큰: $token")  // <- 로그로 출력!
                // 1. 토큰을 서버로 전송 (원한다면)
                // 2. 로컬에 저장
                //PushTokenUtil.sendTokenToServer(this, token)
                val prefs = getSharedPreferences("SaveRegId", MODE_PRIVATE)
                prefs.edit().putString("setRegId", token).apply()
                // 3. 자동 로그인 체크
                mRunnable = Runnable {
                    autoLoginCheck()
                }
                mHandler.postDelayed(mRunnable!!, 1000)
            } else {
                // 토큰 발급 실패 시 재시도 또는 에러 처리
                // 예: 다시 registerToken() 호출 (주의: 무한루프 주의)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_ERR) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRegHandler?.removeCallbacks(mRegRunnable ?: return)
        mHandler.removeCallbacks(mRunnable ?: return)
        mLoadingAnimation.stop()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED -> {
                    // 이미 권한 허용됨
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // 사용자가 이전에 권한을 거부한 경우 설명 필요
                    // Dialog 등으로 설명 후 다시 요청 가능
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // 권한 요청
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
