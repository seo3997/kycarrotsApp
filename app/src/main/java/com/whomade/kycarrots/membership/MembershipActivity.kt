package com.whomade.kycarrots.membership

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.whomade.kycarrots.CheckLoginService
import com.whomade.kycarrots.MainTitleBar
import com.whomade.kycarrots.R
import com.whomade.kycarrots.TitleBar
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.data.model.OpUserVO
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.loginout.MainNavigation
import kotlinx.coroutines.launch

class MembershipActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etName: EditText

    private lateinit var etPhoneFirst: MaterialAutoCompleteTextView
    private lateinit var etPhoneMid: EditText
    private lateinit var etPhoneLast: EditText

    private lateinit var etBirth: TextInputEditText
    private lateinit var rgSex: RadioGroup
    private lateinit var btnRegister: Button
    private lateinit var btnCheckEmail: Button
    private lateinit var etBranch: MaterialAutoCompleteTextView

    private var isEmailChecked = false
    private var branchList: List<com.whomade.kycarrots.data.model.BranchInfoVo> = emptyList()
    private var selectedBranchId: String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership)
        CheckLoginService.mActivityList.add(this)

        findViewById<MainTitleBar>(R.id.main_title_bar).apply {
            findViewById<ImageButton>(R.id.ib_refresh).visibility = View.GONE
            findViewById<ImageButton>(R.id.ib_home).visibility = View.GONE
        }
        findViewById<TitleBar>(R.id.title_bar).setTitle(getString(R.string.str_membership))

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_pwd)
        etName = findViewById(R.id.et_name)
        etBirth = findViewById(R.id.et_birth)
        rgSex = findViewById(R.id.rg_sex)
        btnRegister = findViewById(R.id.btn_register)
        btnCheckEmail = findViewById(R.id.btn_check_email)


        etPhoneFirst= findViewById<MaterialAutoCompleteTextView>(R.id.et_phone_first)
        etPhoneFirst.setAdapter(ArrayAdapter.createFromResource(this, R.array.first_phone_num, android.R.layout.simple_list_item_1))
        if (etPhoneFirst.text.isNullOrBlank()) etPhoneFirst.setText("010", false)
        etPhoneMid = findViewById(R.id.et_phone_mid)
        etPhoneLast = findViewById(R.id.et_phone_last)

        etBranch = findViewById(R.id.et_branch)
        fetchBranchList()





        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isEmailChecked = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        etBirth.addTextChangedListener(YmdDateWatcher(etBirth))


        btnCheckEmail.setOnClickListener { checkEmailDuplicate() }
        btnRegister.setOnClickListener { registerUser() }
    }

    private fun fetchBranchList() {
        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            try {
                branchList = appService.getBranchList()
                val branchNames = branchList.map { it.branchName ?: "" }
                val adapter = ArrayAdapter(this@MembershipActivity, android.R.layout.simple_dropdown_item_1line, branchNames)
                etBranch.setAdapter(adapter)
                etBranch.setOnItemClickListener { _, _, position, _ ->
                    selectedBranchId = branchList[position].branchId.toString()
                }
            } catch (e: Exception) {
                Log.e("MembershipActivity", "지점 목록 조회 오류", e)
            }
        }
    }

    private fun checkEmailDuplicate() {
        val email = etEmail.text.toString().trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("유효한 이메일을 입력하세요.")
            return
        }

        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            try {
                val response = appService.checkEmailDuplicate(email)
                if (response.result) {
                    showToast("사용 가능한 이메일입니다.")
                    isEmailChecked = true
                } else  {
                    showToast("이미 사용 중인 이메일입니다.")
                    isEmailChecked = false
                }
            } catch (e: Exception) {
                Log.e("MembershipActivity", "이메일 중복 확인 오류", e)
                showToast("네트워크 오류 발생")
                isEmailChecked = false
            }
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val phone = etPhoneFirst.text.toString().trim()+"-"+ etPhoneMid.text.toString().trim()+"-"+ etPhoneLast.text.toString().trim()
        val birth = etBirth.text.toString().trim()
        val genderId = rgSex.checkedRadioButtonId
        val gender = if (genderId == R.id.rb_man) "1" else if (genderId == R.id.rb_woman) "2" else ""



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
        if (selectedBranchId == null) {
            showToast(getString(R.string.str_branch_select_err))
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
            areaCode    = "",
            areaSeCodeS = "",
            areaSeCodeD = "",
            referrerId = "",
            userSttusCode = "10",
            memberCode = "ROLE_PUB",
            provider = "PWD",
            branchId = selectedBranchId ?: ""
        )
        showLoading(true)
        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            try {
                var response = appService.registerUser(user)
                showLoading(false)
                if (response!!.resultCode == 200) {
                    showToast("회원가입 성공!")
                    response.login_pwd=password
                    MainNavigation.goMain(this@MembershipActivity, response)
                    //startActivity(Intent(this@MembershipActivity, LoginActivity::class.java))
                    //finish()
                } else {
                    showToast("회원가입 실패")
                }
            } catch (e: Exception) {
                showLoading(false)
                Log.e("MembershipActivity", "회원가입 오류", e)
                showToast("네트워크 오류 발생")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun showLoading(show: Boolean) {
        findViewById<View>(R.id.ll_progress_circle).visibility =
            if (show) View.VISIBLE else View.GONE
    }

}
