package com.whomade.kycarrots.loginout

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.whomade.kycarrots.CheckLoginService
import com.whomade.kycarrots.MainTitleBar
import com.whomade.kycarrots.R
import com.whomade.kycarrots.TitleBar
import com.whomade.kycarrots.data.model.LinkSocialRequest
import com.whomade.kycarrots.data.model.OpUserVO
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.loginout.LoginActivity
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBirth: EditText
    private lateinit var rgSex: RadioGroup
    private lateinit var btnRegister: Button
    private lateinit var btnCheckEmail: Button
    private lateinit var spinnerCity: MaterialAutoCompleteTextView
    private lateinit var spinnerTown: MaterialAutoCompleteTextView
    private lateinit var tvEmailStatus: TextView
    private lateinit var groupMore: View

    private var isEmailChecked = false

    private var selectedCityName = ""
    private var selectedCityValue = ""

    private var selectedTownName = ""
    private var selectedTownValue = ""

    val roleMap = mapOf(
        "판매자" to "ROLE_SELL",
        "센터관리" to "ROLE_PROJ",
        "구매자" to "ROLE_PUB"
    )
    private val provider by lazy { intent.getStringExtra("provider") ?: "KAKAO" }
    private val providerUserId by lazy { intent.getStringExtra("providerUserId").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        CheckLoginService.mActivityList.add(this)


        findViewById<MainTitleBar>(R.id.main_title_bar).apply {
            findViewById<ImageButton>(R.id.ib_refresh).visibility = View.GONE
            findViewById<ImageButton>(R.id.ib_home).visibility = View.GONE
        }
        findViewById<TitleBar>(R.id.title_bar).setTitle(getString(R.string.str_onboard))

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_pwd)
        etName = findViewById(R.id.et_name)
        etPhone = findViewById(R.id.et_phone)
        etBirth = findViewById(R.id.et_birth)
        rgSex = findViewById(R.id.rg_sex)
        btnRegister = findViewById(R.id.btn_register)
        btnCheckEmail = findViewById(R.id.btn_check_email)
        spinnerCity = findViewById(R.id.spinner_city)
        spinnerTown = findViewById(R.id.spinner_town)
        tvEmailStatus = findViewById(R.id.tv_email_status)
        groupMore     = findViewById(R.id.group_more)
        // 초기: 나머지 폼 감춤
        groupMore.visibility = View.GONE

        val roles = roleMap.keys.toList()
        val adapter = ArrayAdapter(this, R.layout.list_txt_item, roles)
        findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_role).setAdapter(adapter)

        val dropdown = findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_role)
        dropdown.setOnClickListener {
            dropdown.showDropDown()
        }

        dropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) dropdown.showDropDown()
        }

        loadCityList()

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isEmailChecked = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })



        btnCheckEmail.setOnClickListener { checkEmailDuplicate() }
        btnRegister.setOnClickListener { registerUser() }
    }

    private fun checkEmailDuplicate() {
        val email  = etEmail.text.toString().trim()
        var userNo =""
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("유효한 이메일을 입력하세요.")
            return
        }

        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            showLoading(true)
            try {
                val response = appService.checkEmailDuplicate(email)

                userNo = response.message
                showLoading(false)
                if (response.result == true) {
                    //showToast("사용 가능한 이메일입니다.")
                    openFullOnboarding()
                    isEmailChecked = true
                } else  {
                    tvEmailStatus.visibility = View.VISIBLE
                    tvEmailStatus.text = "이미 가입된 이메일입니다. 계정 연결을 진행합니다."
                    //showToast("이미 사용 중인 이메일입니다.")
                    isEmailChecked = false

                    val body = appService.linkSocial(
                        LinkSocialRequest(
                            userNo = userNo,
                            userId = email,
                            provider = provider,
                            providerUserId = providerUserId
                        )
                    )
                    if (body == null) {
                        showToast("연결 응답이 비어 있습니다.")
                        return@launch
                    }
                    when (body.resultCode) {
                        200 -> {
                            // 토큰 저장 후 메인 이동
                            /*
                            val jwt = body.token
                            if (!jwt.isNullOrBlank()) {
                                appService.saveJwt(jwt)
                                goMain()
                                return@launch
                            } else {
                                showToast("토큰이 없습니다. (code=200)")
                            }
                             */
                            MainNavigation.goMain(this@OnboardingActivity, body)
                            showToast("소셜계정 링크 성공!!!")
                            //goMain()
                        }
                        409 -> {
                            // 이미 다른 유저에 매핑된 경우
                            showToast("이미 다른 사용자에 연결된 소셜 계정입니다. (409)")
                            // 필요 시 추가 가이드 처리
                        }
                        400 -> {
                            showToast("요청이 올바르지 않습니다. (400)")
                        }
                        601 -> {
                            showToast("사용자를 찾을 수 없습니다. (601)")
                        }
                        500 -> {
                            showToast( "서버 오류가 발생했습니다. (500)")
                        }
                        else -> {
                            showToast("연결 실패)")
                        }
                    }


                }
            } catch (e: Exception) {
                Log.e("MembershipActivity", "이메일 중복 확인 오류", e)
                showToast("네트워크 오류 발생")
                showLoading(false)
                isEmailChecked = false
            }
        }
    }

    private fun openFullOnboarding() {
        tvEmailStatus.visibility = View.VISIBLE
        tvEmailStatus.text = "신규 가입입니다. 추가 정보를 입력해 주세요."
        if (groupMore.visibility != View.VISIBLE) {
            groupMore.visibility = View.VISIBLE
        }
        // 여기서부터는 너가 만들어둔 인증코드 발송/검증 + postOnboarding 흐름 그대로 진행
        // (postOnboarding 성공 시 서버가 TB_USER 생성 & TB_SOCIAL_ACCOUNT insert까지 처리하도록 해두면 베스트)
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val birth = etBirth.text.toString().trim()
        val genderId = rgSex.checkedRadioButtonId
        val gender = if (genderId == R.id.rb_man) "1" else if (genderId == R.id.rb_woman) "2" else ""

        val selectedText = findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_role).text.toString()
        val selectedCode = roleMap[selectedText] ?: ""

        if (name.isEmpty()) {
            showToast("이름을 입력하세요.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("유효한 이메일을 입력하세요.")
            return
        }
        if (!isEmailChecked) {
            showToast("이메일 중복 확인을 해주세요.")
            return
        }
        if (password.length < 4) {
            showToast("비밀번호는 최소 4자 이상이어야 합니다.")
            return
        }
        if (!birth.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            showToast("생년월일은 YYYY-MM-DD 형식으로 입력하세요.")
            return
        }
        if (!phone.matches(Regex("^01[016789]-\\d{3,4}-\\d{4}$"))) {
            showToast("유효한 전화번호를 입력하세요.")
            return
        }
        if (gender.isEmpty()) {
            showToast("성별을 선택하세요.")
            return
        }
        if (selectedCityValue.isEmpty() || selectedTownValue.isEmpty()) {
            showToast("지역을 모두 선택하세요.")
            return
        }

        val user = OpUserVO(
            userNm = name,
            email = email,
            userId = email,
            password = password,
            cttpc = phone,
            gender = gender.toInt(),
            userAge = "",
            birthDate = birth,
            areaCode    =selectedCityValue,
            areaSeCodeS = selectedTownValue,
            areaSeCodeD = "",
            referrerId = "",
            userSttusCode = "10",
            memberCode = selectedCode,
            provider = provider,
            providerUserId = providerUserId
        )

        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            try {
                val response = appService.registerUser(user)
                if (response!!.resultCode == 200) {
                    showToast("회원가입 성공!")
                    MainNavigation.goMain(this@OnboardingActivity, response)
                    //startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
                    //finish()
                } else {
                    showToast("회원가입 실패")
                }
            } catch (e: Exception) {
                Log.e("MembershipActivity", "회원가입 오류", e)
                showToast("네트워크 오류 발생")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun loadCityList() {
        val cityDropdown = findViewById<MaterialAutoCompleteTextView>(R.id.spinner_city)
        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            try {
                val codeList = appService.getCodeList("R010070") // "AREA1" = 시/도 그룹 ID
                val cityNames = codeList.map { it.strMsg}

                val adapter = ArrayAdapter(this@OnboardingActivity, R.layout.list_txt_item, cityNames)
                cityDropdown.setAdapter(adapter)

                cityDropdown.setOnClickListener {
                    cityDropdown.showDropDown()
                }

                cityDropdown.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) cityDropdown.showDropDown()
                }

                // 선택된 시/도 코드 저장 가능하도록 설정
                cityDropdown.setOnItemClickListener { _, _, position, _ ->
                    val selectedCityCode = codeList[position].strIdx
                    selectedCityName = codeList[position].strMsg
                    selectedCityValue = selectedCityCode
                    loadTownList()
                    Log.d("지역 선택", "선택된 지역: $selectedCityName ($selectedCityCode)")
                }

            } catch (e: Exception) {
                Log.e("MembershipActivity", "지역 코드 조회 실패", e)
                Toast.makeText(this@OnboardingActivity, "지역 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTownList() {
        val cityTowndown = findViewById<MaterialAutoCompleteTextView>(R.id.spinner_town)
        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            try {
                val codeList = appService.getSCodeList("R010070",selectedCityValue) // "AREA1" = 시/도 그룹 ID
                val cityNames = codeList.map { it.strMsg}

                val adapter = ArrayAdapter(this@OnboardingActivity, R.layout.list_txt_item, cityNames)
                cityTowndown.setAdapter(adapter)

                cityTowndown.setOnClickListener {
                    cityTowndown.showDropDown()
                }

                cityTowndown.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) cityTowndown.showDropDown()
                }

                // 선택된 시/도 코드 저장 가능하도록 설정
                cityTowndown.setOnItemClickListener { _, _, position, _ ->
                    val selectedTownCode = codeList[position].strIdx
                    selectedTownName = codeList[position].strMsg
                    selectedTownValue = selectedTownCode
                    Log.d("지역 선택", "선택된 지역: $selectedTownName ($selectedTownCode)")
                }

            } catch (e: Exception) {
                Log.e("MembershipActivity", "지역 코드 조회 실패", e)
                Toast.makeText(this@OnboardingActivity, "지역 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        findViewById<View>(R.id.ll_progress_circle).visibility =
            if (show) View.VISIBLE else View.GONE
    }

}
