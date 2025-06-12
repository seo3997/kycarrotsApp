package com.whomade.kycarrots.setting

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.BaseDrawerActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.loginout.AuthManager
import kotlinx.coroutines.launch

class SettingActivity : BaseDrawerActivity() {

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
