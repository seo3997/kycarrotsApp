package com.whomade.kycarrots.loginout

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.CheckLoginService
import com.whomade.kycarrots.MainActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.StaticDataInfo
import com.whomade.kycarrots.membership.TermsAgreeActivity
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener {

    private lateinit var etEmail: EditText
    private lateinit var etPwd: EditText
    private lateinit var llProgress: LinearLayout
    private lateinit var spinner: Spinner
    private var selectedUserType: String = "1" // 기본값: 판매자

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        CheckLoginService.mActivityList.add(this)

        findViewById<Button>(R.id.btn_membership).setOnClickListener(this)
        findViewById<Button>(R.id.btn_login).setOnClickListener(this)
        findViewById<Button>(R.id.btn_find_id_pwd).setOnClickListener(this)

        etEmail = findViewById(R.id.et_email)
        etPwd = findViewById(R.id.et_pwd)
        llProgress = findViewById(R.id.ll_progress_circle)

        etEmail.onFocusChangeListener = this
        etPwd.onFocusChangeListener = this


        spinner = findViewById<Spinner>(R.id.spinner_user_type)
        val userTypeList = listOf("판매자", "구매자", "센터")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypeList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // 선택 시 값 얻기 (1,2,3)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedUserType = when (position) {
                    0 -> "1" // 판매자
                    1 -> "2" // 구매자
                    2 -> "3" // 센터
                    else -> ""
                }
                // TODO: selectedValue 사용
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onClick(v: View?) {
        val intent = when (v?.id) {
            R.id.btn_membership -> {
                etEmail.setText("")
                etPwd.setText("")
                Intent(this, TermsAgreeActivity::class.java)
            }

            R.id.btn_find_id_pwd -> {
                etEmail.setText("")
                etPwd.setText("")
                Intent(this, FindEmailPwdActivity::class.java)
            }

            R.id.btn_login -> {
                chkLoginCondition()
                null
            }

            else -> null
        }

        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(it)
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
        val strUserType = selectedUserType

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

            else -> {
                if (!llProgress.isShown) llProgress.visibility = View.VISIBLE

                val appVersion = getAppVersion(this)
                val appService = AppServiceProvider.getService()

                lifecycleScope.launch {
                    try {
                        val resultCode = LoginInfo(this@LoginActivity, strEmail, strPwd, appVersion ,strUserType, appService).login()
                        when (resultCode) {
                            StaticDataInfo.RESULT_CODE_200 -> {
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                })
                                finish()
                            }

                            StaticDataInfo.RESULT_NO_USER,
                            StaticDataInfo.RESULT_NO_DATA -> {
                                Toast.makeText(this@LoginActivity, getString(R.string.str_find_email_err), Toast.LENGTH_SHORT).show()
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
}