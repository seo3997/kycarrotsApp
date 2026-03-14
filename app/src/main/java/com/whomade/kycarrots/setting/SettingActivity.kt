package com.whomade.kycarrots.setting

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Switch
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
        val tv_user_nm = findViewById<TextView>(R.id.tv_user_nm)
        val tv_user_telno = findViewById<TextView>(R.id.tv_user_telno)
        val tv_user_addr = findViewById<TextView>(R.id.tv_user_addr)

        val appService = AppServiceProvider.getService()

        lifecycleScope.launch {
            try {
                val userInfo = appService.getUserInfo(token)
                userInfo?.let {
                    tv_user_id.text = "아이디: ${it.userId ?: ""}"
                    tv_user_nm.text = "이름: ${it.userNm ?: ""}"
                    tv_user_telno.text = "연락처: ${it.cttpc ?: ""}"
                    tv_user_addr.text = "주소: ${it.areaCodeNm} ${it.areaSeCodeSNm}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
