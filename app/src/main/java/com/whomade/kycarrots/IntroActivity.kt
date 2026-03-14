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
import com.whomade.kycarrots.ui.dialog.DlgBtnActivity
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
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.printKakaoKeyHash
import com.whomade.kycarrots.OrderDetailActivity
import com.whomade.kycarrots.OrderMgtDetailActivity


class IntroActivity : AppCompatActivity() {
    private var pushTargetId: String? = null
    private var pushType: String? = null
    private var pushMsg: String? = null
    private var pushTitle: String? = null
    private var pushBody: String? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        savePushIntentData(intent)
    }
    private fun savePushIntentData(intent: Intent?) {
        pushTargetId = intent?.getStringExtra("targetId") ?: intent?.getStringExtra("roomId") ?: intent?.getStringExtra("productId") ?: intent?.getStringExtra("orderId")
        pushType = intent?.getStringExtra("type")
        pushMsg = intent?.getStringExtra("msg")
        pushTitle = intent?.getStringExtra("title")
        pushBody = intent?.getStringExtra("body")
        Log.d("PushIntent", "savePushIntentData - type: $pushType, targetId: $pushTargetId, msg: $pushMsg, title: $pushTitle")

        // 알림 클릭으로 진입한 경우 로컬 DB 저장 시도 (백그라운드 수신 시 저장 누락 대비)
        if (!pushType.isNullOrBlank() && !pushTitle.isNullOrBlank()) {
            handlePushStorage()
        }
    }

    private fun handlePushStorage() {
        val type = pushType ?: return
        val title = pushTitle ?: return
        val body = pushBody
        val targetId = pushTargetId

        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val userId = prefs.getString("LogIn_ID", "") ?: ""
        if (userId.isEmpty()) return

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val repo = com.whomade.kycarrots.data.local.PushRepositoryProvider.get(applicationContext)
                
                // 간단한 중복 체크 (최근 5초 이내 동일한 제목/내용이 있으면 스킵)
                // (onMessageReceived에서 이미 저장했을 가능성이 높음)
                val items = repo.list(userId, false, 1, 0)
                if (items.isNotEmpty()) {
                    val last = items[0]
                    if (last.title == title && last.body == body && (System.currentTimeMillis() - last.createdAt < 5000)) {
                        Log.d("PushIntent", "중복 저장 방지: 이미 저장된 푸시입니다.")
                        return@launch
                    }
                }

                val entity = com.whomade.kycarrots.data.local.PushNotificationEntity(
                    userId = userId,
                    type = type,
                    title = title,
                    body = body,
                    targetId = targetId,
                    deeplink = when(type) {
                        "product" -> "app://product/$targetId"
                        "chat" -> "app://chat/room/$targetId"
                        "order" -> "app://order/$targetId"
                        else -> null
                    },
                    isRead = true // 클릭해서 들어왔으므로 읽음 처리
                )
                repo.save(entity)
                Log.d("PushIntent", "알림 클릭 -> 로컬 DB 저장 완료")
            } catch (e: Exception) {
                Log.e("PushIntent", "로컬 DB 저장 실패", e)
            }
        }
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
        const val REQUEST_ERR = 999
    }

    private lateinit var mThisAppVersion: String
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
        //printKakaoKeyHash()
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

            runAutoLoginCheck()
        }

    }

    private fun runAutoLoginCheck() {
        mRunnable = Runnable {
            autoLoginCheck()
        }
        mHandler.postDelayed(mRunnable!!, 1000)
    }


    private fun autoLoginCheck() {
        mHandler.removeCallbacks(mRunnable ?: return)

        val sUID            = LoginInfoUtil.getUserId(this)
        val sPWD            = LoginInfoUtil.getUserPassword(this)
        val sLoginCd        = LoginInfoUtil.getUserLoginCd(this)
        val sProviderUserId = LoginInfoUtil.getUserSocialId(this)
        Log.d("FCM", "autoLoginCheck: true")

        if (sUID.isNotBlank() &&  sLoginCd.isNotBlank() && mThisAppVersion.isNotEmpty()) {
            val appService = AppServiceProvider.getService()

            lifecycleScope.launch {
                val resultCode = LoginInfo(this@IntroActivity, sUID, sPWD, sLoginCd, mThisAppVersion,sProviderUserId, appService).login()
                if (resultCode == StaticDataInfo.RESULT_CODE_200) {
                    PushTokenUtil.ensureTokenRegistered(this@IntroActivity)
                    nextPage(true, LoginInfoUtil.getMemberCode(this@IntroActivity))
                } else {
                    when (resultCode) {
                        StaticDataInfo.RESULT_NO_USER,
                        StaticDataInfo.RESULT_NO_DATA -> {
                            Toast.makeText(this@IntroActivity, getString(R.string.str_find_email_err), Toast.LENGTH_SHORT).show()
                        }

                        StaticDataInfo.RESULT_MEMBER_CODE_ERR -> {
                            Toast.makeText(this@IntroActivity, getString(R.string.str_member_code_err), Toast.LENGTH_SHORT).show()
                        }

                        StaticDataInfo.RESULT_NO_SOCAIL_DATA -> {
                            Toast.makeText(this@IntroActivity, getString(R.string.str_social_code_err), Toast.LENGTH_SHORT).show()
                        }

                        StaticDataInfo.RESULT_PWD_ERR -> {
                            Toast.makeText(this@IntroActivity, getString(R.string.str_pwd_err), Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            Toast.makeText(this@IntroActivity, getString(R.string.str_http_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                    nextPage(false, LoginInfoUtil.getMemberCode(this@IntroActivity))
                }
            }
        } else {
            // sUSERTYPE 이 비어 있을 수 있지만, nextPage 에 빈 문자열이라도 넘겨서 NPE 방지
            nextPage(false, LoginInfoUtil.getMemberCode(this@IntroActivity))
        }
    }

    private fun nextPage(isLogin: Boolean, memberCode: String) {

        // ROLE_PUB 푸시 토픽 구독
        FirebaseMessaging.getInstance().subscribeToTopic(memberCode)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", memberCode+" 토픽 구독 성공")
                } else {
                    Log.e("FCM", memberCode+" 토픽 구독 실패", task.exception)
                }
        }

        if (isLogin) {
            val intents = createIntentsForPushNavigation(memberCode)
            startActivities(intents)
        } else {
            val intent = Intent(this, LoginActivity::class.java).apply {
                // 로그인 후 다시 채팅 or 상품 상세로 이동할 수 있도록 push 데이터 전달
                putExtra("targetId", pushTargetId)
                putExtra("type", pushType)
                putExtra("msg", pushMsg)
                putExtra("title", pushTitle)
                putExtra("body", pushBody)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
        finish()
    }

    private fun createIntentsForPushNavigation(memberCode: String): Array<Intent> {
        val defaultIntent = defaultIntentForMember(memberCode).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        return when (pushType) {
            "chat" -> {
                if (!pushTargetId.isNullOrBlank()) {
                    val parts = pushTargetId?.split("_") ?: emptyList()
                    var productId = ""
                    var buyerId = ""
                    var branchId = ""
                    if (parts.size >= 3) {
                        productId = parts[0]
                        buyerId = parts[1]
                        branchId = parts[2]
                    }
                    val chatIntent = Intent(this, ChatActivity::class.java).apply {
                        putExtra("roomId", pushTargetId)
                        putExtra("buyerId", buyerId)
                        putExtra("branchId", branchId)
                        putExtra("productId", productId)
                        putExtra("type", pushType)
                        putExtra("msg", pushMsg)
                    }
                    arrayOf(defaultIntent, chatIntent)
                } else {
                    arrayOf(defaultIntent)
                }
            }

            "product" -> {
                if (!pushTargetId.isNullOrBlank()) {
                    val productIntent = Intent(this, AdDetailActivity::class.java).apply {
                        putExtra(AdDetailActivity.EXTRA_PRODUCT_ID, pushTargetId)
                        putExtra("type", pushType)
                        putExtra("msg", pushMsg)
                    }
                    arrayOf(defaultIntent, productIntent)
                } else {
                    arrayOf(defaultIntent)
                }
            }

            "order" -> {
                if (!pushTargetId.isNullOrBlank()) {
                    val targetActivity = if (memberCode == Constants.ROLE_SELL || memberCode == Constants.ROLE_PROJ || memberCode == Constants.ROLE_ADMIN) {
                        OrderMgtDetailActivity::class.java
                    } else {
                        OrderDetailActivity::class.java
                    }
                    val orderIntent = Intent(this, targetActivity).apply {
                        putExtra("orderId", pushTargetId)
                        putExtra("type", pushType)
                        putExtra("msg", pushMsg)
                    }
                    arrayOf(defaultIntent, orderIntent)
                } else {
                    arrayOf(defaultIntent)
                }
            }

            else -> {
                arrayOf(defaultIntent)
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


}
