package com.whomade.kycarrots.setting

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Switch
import android.widget.EditText
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.BaseDrawerActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.loginout.AuthManager
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class SettingActivity : BaseDrawerActivity() {
    
    private lateinit var profileImageView: ImageView
    private lateinit var etUserNm: EditText
    private lateinit var etUserTelno: EditText
    
    private var selectedCityValue = ""
    private var selectedTownValue = ""
    private var currentUserNo: Long = 0L

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            saveProfileImageLocally(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val btnLogout = findViewById<Button>(R.id.btn_logout)

        btnLogout.setOnClickListener {
            // 로그아웃 처리
            AuthManager.logout(this)
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            title = "설정"
        }

        loadUserInfo()

        // 프로필 이미지 설정
        profileImageView = findViewById(R.id.profile_image)
        loadLocalProfileImage()
        findViewById<android.view.View>(R.id.profile_image_container).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // 푸시 알림 스위치 설정
        val switchPush = findViewById<Switch>(R.id.switch_push)
        val pushPrefs = getSharedPreferences("PushSettings", Context.MODE_PRIVATE)
        switchPush.isChecked = pushPrefs.getBoolean("push_enabled", true)

        switchPush.setOnCheckedChangeListener { _, isChecked ->
            pushPrefs.edit().putBoolean("push_enabled", isChecked).apply()
        }

        // 사용자 정보 수정 UI 초기화
        etUserNm = findViewById(R.id.et_user_nm)
        etUserTelno = findViewById(R.id.et_user_telno)
        
        findViewById<Button>(R.id.btn_save_info).setOnClickListener {
            saveUserInfo()
        }

        // 비밀번호 변경 레이아웃 토글
        val tvChangePassword = findViewById<TextView>(R.id.tv_change_password)
        val layoutPasswordChange = findViewById<View>(R.id.layout_password_change)
        tvChangePassword.setOnClickListener {
            layoutPasswordChange.visibility = if (layoutPasswordChange.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        findViewById<Button>(R.id.btn_execute_pw_change).setOnClickListener {
            executePasswordChange()
        }
    }

    private fun executePasswordChange() {
        val currentPw = findViewById<EditText>(R.id.et_current_password).text.toString()
        val newPw = findViewById<EditText>(R.id.et_new_password).text.toString()
        val confirmPw = findViewById<EditText>(R.id.et_confirm_password).text.toString()

        if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            Toast.makeText(this, "비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPw != confirmPw) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("TokenInfo", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: return

        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            val request = com.whomade.kycarrots.data.model.PasswordChangeRequest(currentPw, newPw, confirmPw)
            val result = appService.changePassword(token, request)
            if (result.first) {
                Toast.makeText(this@SettingActivity, result.second, Toast.LENGTH_SHORT).show()
                // 입력 필드 초기화 및 레이아웃 숨기기
                findViewById<EditText>(R.id.et_current_password).setText("")
                findViewById<EditText>(R.id.et_new_password).setText("")
                findViewById<EditText>(R.id.et_confirm_password).setText("")
                findViewById<View>(R.id.layout_password_change).visibility = View.GONE
            } else {
                Toast.makeText(this@SettingActivity, result.second, Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun saveUserInfo() {
        val prefs = getSharedPreferences("TokenInfo", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: return
        
        val name = etUserNm.text.toString().trim()
        val telno = etUserTelno.text.toString().trim()
        
        if (name.isEmpty() || telno.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            val userVo = com.whomade.kycarrots.data.model.OpUserVO(
                userNo = currentUserNo,
                userNm = name,
                cttpc = telno,
                areaCode = selectedCityValue,
                areaSeCodeS = selectedTownValue
            )
            val success = appService.updateUser(token, userVo)
            if (success) {
                Toast.makeText(this@SettingActivity, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SettingActivity, "수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfileImageLocally(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val file = File(filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            // 로컬 경로 저장
            getSharedPreferences("ProfileSettings", Context.MODE_PRIVATE)
                .edit()
                .putString("local_profile_path", file.absolutePath)
                .apply()

            // 즉시 반영
            Glide.with(this).load(file).circleCrop().into(profileImageView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadLocalProfileImage() {
        val path = getSharedPreferences("ProfileSettings", Context.MODE_PRIVATE)
            .getString("local_profile_path", null)
        
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                Glide.with(this).load(file).circleCrop().into(profileImageView)
            }
        }
    }

    private fun loadUserInfo() {
        val prefs = getSharedPreferences("TokenInfo", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: return

        val tv_user_id = findViewById<TextView>(R.id.tv_user_id)
        val appService = AppServiceProvider.getService()

        lifecycleScope.launch {
            try {
                val userInfo = appService.getUserInfo(token)
                userInfo?.let {
                    currentUserNo = it.userNo
                    tv_user_id.text = "아이디: ${it.userId ?: ""}"
                    etUserNm.setText(it.userNm ?: "")
                    etUserTelno.setText(it.cttpc ?: "")
                    
                    // 지역 정보 설정
                    if (!it.areaCode.isNullOrEmpty()) {
                        selectedCityValue = it.areaCode
                        selectedTownValue = it.areaSeCodeS ?: ""
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
