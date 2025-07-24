package com.whomade.kycarrots

import android.content.Intent
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.whomade.kycarrots.setting.SettingActivity
import com.whomade.kycarrots.ui.buy.ItemSelectionActivity

open class BaseDrawerActivity : AppCompatActivity() {
    protected lateinit var drawerLayout: DrawerLayout

    override fun setContentView(layoutResID: Int) {
        val fullView = layoutInflater.inflate(R.layout.activity_base_drawer, null)
        val activityContainer = fullView.findViewById<FrameLayout>(R.id.activity_content)
        layoutInflater.inflate(layoutResID, activityContainer, true)
        super.setContentView(fullView)




        drawerLayout = fullView.findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = fullView.findViewById(R.id.nav_view)

        val headerView = navigationView.getHeaderView(0) // 헤더 레이아웃의 첫 번째 뷰
        val navUserIdTextView = headerView.findViewById<TextView>(R.id.nav_userid)

        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val userId = prefs.getString("LogIn_ID", "") ?: ""
        navUserIdTextView.text = userId



        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(intent)
                }
                R.id.nav_messages -> {
                    // 다른 화면 연결 시 여기에 작성
                    val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
                    val sMEMBERCODE= prefs.getString("LogIn_MEMBERCODE", "").orEmpty()

                    if (sMEMBERCODE == "ROLE_SELL") {
                        var intent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(intent)
                    } else {
                        var intent = Intent(this, ItemSelectionActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(intent)
                    }

                }
                R.id.nav_setting -> {
                    // 설정 화면 연결 시 여기에 작성
                    val intent = Intent(this, SettingActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}
