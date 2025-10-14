package com.whomade.kycarrots.loginout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.data.model.EmailSendReq
import com.whomade.kycarrots.data.model.EmailVerifyReq
import com.whomade.kycarrots.data.model.EmailVerifyResp
import com.whomade.kycarrots.data.model.OnboardingRequest
import com.whomade.kycarrots.data.model.OnboardingResponse
import com.whomade.kycarrots.databinding.ActivityOnboardingBinding
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    private lateinit var b: ActivityOnboardingBinding
    private val appService by lazy { AppServiceProvider.getService() }

    private var emailVerified = false
    private var lastVerifiedEmail: String? = null

    // 화면 진입 파라미터 (카카오에서 넘긴 값들)
    private val nicknameFromKakao by lazy { intent.getStringExtra("nickname").orEmpty() }
    private val emailFromKakao by lazy { intent.getStringExtra("email") } // null 가능
    private val profileUrl by lazy { intent.getStringExtra("profileUrl") }

    private val roleMap = mapOf(
        "구매자" to "ROLE_PUB",
        "판매자" to "ROLE_SELL",
        "도매상" to "ROLE_PROJ"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(b.root)

        // 1) 기본 값 채우기
        b.etNickname.setText(nicknameFromKakao)
        emailFromKakao?.let {
            b.etEmail.setText(it)
            emailVerified = true
            lastVerifiedEmail = it
        }
        // (원하면 Glide/Coil로 b.ivProfile 에 profileUrl 로드)

        // 2) 역할 드롭다운
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, roleMap.keys.toList())
        b.ddRole.setAdapter(roleAdapter)

        // 3) 지역 스피너 (placeholder → 후에 코드리스트로 교체)
        b.spAreaGroup.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("선택","서울","경기","부산"))
        b.spAreaMid.adapter   = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("선택"))
        b.spAreaScls.adapter  = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("선택"))

        // 이메일이 바뀌면 인증 플래그 리셋
        b.etEmail.addTextChangedListener(simpleWatcher {
            val cur = b.etEmail.text?.toString()?.trim().orEmpty()
            if (cur != lastVerifiedEmail) {
                emailVerified = false
            }
            updateSubmitEnabled()
        })

        // 4) 인증코드 보내기
        b.btnSendCode.setOnClickListener {
            val email = b.etEmail.text?.toString()?.trim().orEmpty()
            if (!isEmail(email)) {
                b.etEmail.error = "유효한 이메일을 입력하세요"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                showLoading(true)
                try {
                    val ok = appService.sendEmailCode(EmailSendReq(email))
                    if (ok) Toast.makeText(this@OnboardingActivity, "인증코드를 전송했습니다.", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this@OnboardingActivity, "코드 전송 실패", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@OnboardingActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    showLoading(false)
                }
            }
        }

        // 5) 인증코드 확인
        b.btnVerifyCode.setOnClickListener {
            val email = b.etEmail.text?.toString()?.trim().orEmpty()
            val code  = b.etCode.text?.toString()?.trim().orEmpty()
            if (!isEmail(email) || code.isBlank()) {
                Toast.makeText(this, "이메일과 인증코드를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                showLoading(true)
                try {
                    val resp: EmailVerifyResp? = appService.verifyEmailCode(EmailVerifyReq(email, code))
                    if (resp?.verified == true) {
                        emailVerified = true
                        lastVerifiedEmail = email
                        Toast.makeText(this@OnboardingActivity, "이메일 인증 완료", Toast.LENGTH_SHORT).show()
                    } else {
                        emailVerified = false
                        Toast.makeText(this@OnboardingActivity, "인증 실패", Toast.LENGTH_SHORT).show()
                    }
                    updateSubmitEnabled()
                } catch (e: Exception) {
                    Toast.makeText(this@OnboardingActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    showLoading(false)
                }
            }
        }

        // 6) 약관 링크(예시)
        b.tvTermsLinks.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://your.domain/terms")))
        }

        // 7) 제출
        b.btnSubmit.setOnClickListener { submit() }

        // 8) 버튼 활성화 조건 연결
        attachCommonWatchers()
        updateSubmitEnabled()
    }

    private fun submit() {
        val email = b.etEmail.text?.toString()?.trim().orEmpty()
        val roleDisp = b.ddRole.text?.toString().orEmpty()
        val role = roleMap[roleDisp] ?: "ROLE_PUB"

        if (!isEmail(email)) {
            b.etEmail.error = "유효한 이메일을 입력하세요"
            return
        }
        if (!emailVerified || email != lastVerifiedEmail) {
            Toast.makeText(this, "이메일 인증이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val req = OnboardingRequest(
            nickname = b.etNickname.text!!.toString().trim(),
            email = email, // 카카오 제공 or 사용자 입력(인증 완료)
            role = role,
            areaGroup = sel(b.spAreaGroup),
            areaMid   = sel(b.spAreaMid),
            areaScls  = sel(b.spAreaScls),
            marketingPush = b.swMarketingPush.isChecked,
            marketingEmail = b.swMarketingEmail.isChecked,
            tosAgreed = b.cbTos.isChecked,
            privacyAgreed = b.cbPrivacy.isChecked
        )

        lifecycleScope.launch {
            showLoading(true)
            try {
                val resp: OnboardingResponse? = appService.postOnboarding(req)
                if (resp != null) {
                    Toast.makeText(this@OnboardingActivity, "환영합니다!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK, Intent().apply {
                        putExtra("userId", resp.userId)
                        putExtra("role", resp.role)
                    })
                    finish()
                } else {
                    Toast.makeText(this@OnboardingActivity, "저장 실패(서버 오류)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OnboardingActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    // ===== 유틸 =====

    private fun attachCommonWatchers() {
        listOf<TextView>(b.etNickname, b.ddRole).forEach {
            it.addTextChangedListener(simpleWatcher { updateSubmitEnabled() })
        }
        listOf<CheckBox>(b.cbTos, b.cbPrivacy).forEach {
            it.setOnCheckedChangeListener { _, _ -> updateSubmitEnabled() }
        }
    }

    private fun updateSubmitEnabled() {
        val nickOk = b.etNickname.text?.isNotBlank() == true
        val roleOk = !b.ddRole.text.isNullOrBlank()
        val tosOk = b.cbTos.isChecked && b.cbPrivacy.isChecked
        val email = b.etEmail.text?.toString()?.trim().orEmpty()
        val emailOk = isEmail(email)
        val verifiedOk = emailVerified && (email == lastVerifiedEmail)
        b.btnSubmit.isEnabled = nickOk && roleOk && tosOk && emailOk && verifiedOk
    }

    private fun isEmail(v: String) = Patterns.EMAIL_ADDRESS.matcher(v).matches()

    private fun sel(spinner: Spinner): String? =
        spinner.selectedItem?.toString()?.takeIf { it.isNotBlank() && it != "선택" }

    private fun simpleWatcher(onChange: () -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = onChange()
        override fun afterTextChanged(s: Editable?) {}
    }

    /** 프로젝트에 이미 있는 showLoading(true/false)를 그대로 사용 */
    private fun showLoading(show: Boolean) {
        // Activity/Fragment 공통 로딩 표시 메서드가 있으면 그걸 호출하세요.
        // 여기선 너의 패턴에 맞춰 그냥 그대로 둠.
    }
}
