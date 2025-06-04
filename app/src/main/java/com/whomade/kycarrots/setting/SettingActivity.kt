package com.whomade.kycarrots.setting

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.whomade.kycarrots.BaseDrawerActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.loginout.AuthManager

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

    }

}
