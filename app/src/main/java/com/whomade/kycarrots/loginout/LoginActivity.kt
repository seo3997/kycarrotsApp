package com.whomade.kycarrots.loginout

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.whomade.kycarrots.CheckLoginService
import com.whomade.kycarrots.IntroActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.StaticDataInfo
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.data.model.SocialAuthRequest
import com.whomade.kycarrots.membership.TermsAgreeActivity
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener {

    private lateinit var etEmail: EditText
    private lateinit var etPwd: EditText
    private lateinit var llProgress: LinearLayout
    private var selectedUserType: String = "1" // 기본값: 판매자

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        CheckLoginService.mActivityList.add(this)

        findViewById<Button>(R.id.btn_membership).setOnClickListener(this)
        findViewById<Button>(R.id.btn_login).setOnClickListener(this)
        findViewById<Button>(R.id.btn_kakaologin).setOnClickListener(this)
        findViewById<Button>(R.id.btn_kakaounlink).setOnClickListener(this)
        findViewById<Button>(R.id.btn_find_id_pwd).setOnClickListener(this)

        etEmail = findViewById(R.id.et_email)
        etPwd = findViewById(R.id.et_pwd)
        llProgress = findViewById(R.id.ll_progress_circle)

        etEmail.onFocusChangeListener = this
        etPwd.onFocusChangeListener = this


        /*
        spinner = findViewById<Spinner>(R.id.spinner_user_type)
        val userTypeList = listOf("판매자", "구매자", "센터")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypeList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // 선택 시 값 얻기 (1,2,3)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedUserType = when (position) {
                    0 -> Constants.ROLE_SELL // 판매자
                    1 -> Constants.ROLE_PUB // 구매자
                    2 -> Constants.ROLE_PROJ // 센터
                    else -> ""
                }
                // TODO: selectedValue 사용
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
         */
    }

    override fun onClick(v: View?) {
        val intent = when (v?.id) {
            R.id.btn_membership -> {
                //etEmail.setText("")
                //etPwd.setText("")
                Intent(this, TermsAgreeActivity::class.java)
            }

            R.id.btn_find_id_pwd -> {
                //etEmail.setText("")
                //etPwd.setText("")
                Intent(this, FindEmailPwdActivity::class.java)
            }

            R.id.btn_login -> {
                chkLoginCondition()
                null
            }
            R.id.btn_kakaologin -> {
                startKakaoLogin()
                null
            }
            R.id.btn_kakaounlink -> {
                UserApiClient.instance.unlink { error ->
                    if (error != null) Log.e("KAKAO", "unlink 실패", error)
                    else {
                        // 옵션: 카카오 로그아웃도
                        UserApiClient.instance.logout { /* ignore */ }
                        Toast.makeText(this, "카카오 연결 해제 완료. 다음 로그인 시 재동의 필요", Toast.LENGTH_SHORT).show()
                    }
                }
                null
            }

            else -> null
        }

        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(it)
        }
    }
    private fun startKakaoLogin() {
        showLoading(true)
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> {
                    // 카카오톡 취소 시 계정 로그인으로 폴백
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        // 사용자가 취소한 경우
                        Toast.makeText(this,"로그인이 취소되었습니다.",Toast.LENGTH_SHORT).show()

                    } else {
                        // 기타 에러면 계정 로그인으로 폴백
                        loginWithKakaoAccount()
                    }
                }
                token != null -> {
                    // 로그인 성공 → 사용자 정보 조회
                    fetchKakaoUserAndGo(token)
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            loginWithKakaoAccount()
        }
    }
    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Toast.makeText(this,"카카오 계정 로그인 실패: ${error.message}",Toast.LENGTH_SHORT).show()
                return@loginWithKakaoAccount
            }
            if (token != null) {
                fetchKakaoUserAndGo(token)
            }
        }
    }
    private fun fetchKakaoUserAndGo(token: OAuthToken) {
        UserApiClient.instance.me { user, error ->
            if (error != null || user == null) {
                Toast.makeText(this, "카카오 사용자 정보 조회 실패", Toast.LENGTH_SHORT).show()
                return@me
            }

            val kakaoUserId = user.id?.toString().orEmpty()      // provider_user_id
            val nickname    = user.kakaoAccount?.profile?.nickname.orEmpty()
            val email       = user.kakaoAccount?.email           // null 가능
            val profileUrl  = user.kakaoAccount?.profile?.profileImageUrl

            // 안전장치: id 없으면 온보딩으로 보냄
            if (kakaoUserId.isBlank()) {
                Toast.makeText(this, "카카오 ID를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                return@me
            }
            Toast.makeText(this, "카카오 로그인 성공: ${token.accessToken.take(10)}...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                showLoading(true)
                try {
                    val appService = AppServiceProvider.getService()

                    // 공용 소셜 인증 요청 (서버는 LoginResponse 반환)
                    val req = SocialAuthRequest(
                        provider = SocialProvider.KAKAO.name,   // "KAKAO"
                        providerUserId = kakaoUserId,
                        accessToken = token.accessToken,        // 카카오는 accessToken으로 검증
                        idToken = null
                    )

                    val auth: LoginResponse? = appService.authSocial(req)
                    // 서버 표준: code=200 성공 / 604 온보딩 필요 (token은 성공시에만 존재)
                    when {
                        auth == null -> {
                            Toast.makeText(this@LoginActivity, "로그인 응답이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        auth.resultCode == 200 && !auth.token.isNullOrBlank() -> {
                            appService.saveJwt(auth.token!!)
                            showLoading(false)
                            //goMain()
                            MainNavigation.goMain(this@LoginActivity, auth)
                            return@launch
                        }
                        auth.resultCode == 604 -> {
                            // 매핑 없음 → 온보딩 필요
                            startActivity(Intent(this@LoginActivity, OnboardingActivity::class.java).apply {
                                putExtra("provider", SocialProvider.KAKAO.name)
                                putExtra("providerUserId", kakaoUserId)
                                putExtra("nickname", nickname)
                                // 서버 정책상 이메일 필수 → 카카오가 안 준 경우 null로 넘겨 온보딩에서 입력/인증
                                putExtra("email", email)
                                putExtra("profileUrl", profileUrl)
                            })
                            finish()
                        }
                        else -> {
                            // 그 외 코드(400/500 등) 공통 처리
                            Toast.makeText(
                                this@LoginActivity,
                                "로그인 실패(code=${auth.resultCode})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    showLoading(false)
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (etEmail.text.toString().trim().isEmpty()) {
            etEmail.hint = getString(R.string.str_email_en)
        }
        if (etPwd.text.toString().trim().isEmpty()) {
            etPwd.hint = getString(R.string.str_pwd_en)
        }
        etEmail.requestFocus()
    }

    private fun chkLoginCondition() {
        val strEmail = etEmail.text.toString()
        val strPwd = etPwd.text.toString()
        val strMemberCode = selectedUserType

        when {
            strEmail.trim().isEmpty() -> {
                Toast.makeText(this, getString(R.string.str_input_id_err), Toast.LENGTH_SHORT).show()
            }

            !chkEmailType(strEmail) -> {
                // 오류 메시지는 chkEmailType 내부에서 출력
            }

            strPwd.trim().isEmpty() -> {
                Toast.makeText(this, getString(R.string.str_input_pwd_err), Toast.LENGTH_SHORT).show()
            }

            strMemberCode == Constants.ROLE_PROJ && Constants.SYSTEM_TYPE.toString() == "1" -> {
                Toast.makeText(this, "직거래앱은 센터로 로그인 할수 없습니다.", Toast.LENGTH_SHORT).show()
            }

            else -> {
                if (!llProgress.isShown) llProgress.visibility = View.VISIBLE

                val appVersion = getAppVersion(this)
                val appService = AppServiceProvider.getService()

                lifecycleScope.launch {
                    try {
                        val resultCode = LoginInfo(this@LoginActivity, strEmail, strPwd, "PWD", appVersion ,"", appService).login()
                        when (resultCode) {
                            StaticDataInfo.RESULT_CODE_200 -> {
                                /*
                                if (strMemberCode == "ROLE_SELL") {
                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            DashboardActivity::class.java
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        })
                                } else {
                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            ItemSelectionActivity::class.java
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        })
                                }
                                */
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        IntroActivity::class.java
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    })

                                finish()
                            }

                            StaticDataInfo.RESULT_NO_USER,
                            StaticDataInfo.RESULT_NO_DATA -> {
                                Toast.makeText(this@LoginActivity, getString(R.string.str_find_email_err), Toast.LENGTH_SHORT).show()
                            }

                            StaticDataInfo.RESULT_MEMBER_CODE_ERR -> {
                                Toast.makeText(this@LoginActivity, getString(R.string.str_member_code_err), Toast.LENGTH_SHORT).show()
                            }

                            StaticDataInfo.RESULT_NO_SOCAIL_DATA -> {
                                Toast.makeText(this@LoginActivity, getString(R.string.str_social_code_err), Toast.LENGTH_SHORT).show()
                            }

                            StaticDataInfo.RESULT_PWD_ERR -> {
                                Toast.makeText(this@LoginActivity, getString(R.string.str_pwd_err), Toast.LENGTH_SHORT).show()
                            }

                            else -> {
                                Toast.makeText(this@LoginActivity, getString(R.string.str_http_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, getString(R.string.str_http_error), Toast.LENGTH_SHORT).show()
                    } finally {
                        llProgress.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (llProgress.isShown) llProgress.visibility = View.GONE
    }

    private fun checkEmail(email: String): Boolean {
        val mailRegex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$"
        return Pattern.compile(mailRegex).matcher(email).matches()
    }

    private fun chkEmailType(email: String): Boolean {
       /*
        return if (checkEmail(email)) {
            true
        } else {
            Toast.makeText(this, R.string.str_email_type_err, Toast.LENGTH_SHORT).show()
            false
        }
        */
        return true
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        when (v?.id) {
            R.id.et_email -> {
                if (hasFocus) {
                    etEmail.hint = ""
                    if (etPwd.text.toString().trim().isEmpty()) {
                        etPwd.hint = getString(R.string.str_pwd_en)
                    }
                }
            }

            R.id.et_pwd -> {
                if (hasFocus) {
                    if (etEmail.text.toString().trim().isEmpty()) {
                        etEmail.hint = getString(R.string.str_email_en)
                    }
                    etPwd.hint = ""
                }
            }
        }
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }
    }
    private fun showLoading(show: Boolean) {
        findViewById<View>(R.id.ll_progress_circle).visibility =
            if (show) View.VISIBLE else View.GONE
    }

}