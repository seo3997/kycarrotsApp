package com.whomade.kycarrots

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.whomade.kycarrots.setting.SettingActivity
import com.whomade.kycarrots.ui.buy.ItemSelectionActivity
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.TokenUtil

open class BaseDrawerActivity : AppCompatActivity() {
    protected lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun setContentView(layoutResID: Int) {
        val fullView = layoutInflater.inflate(R.layout.activity_base_drawer, null)
        val activityContainer = fullView.findViewById<FrameLayout>(R.id.activity_content)
        layoutInflater.inflate(layoutResID, activityContainer, true)
        super.setContentView(fullView)


        drawerLayout = fullView.findViewById(R.id.drawer_layout)


        navView = fullView.findViewById(R.id.nav_view)

        val headerView = navView.getHeaderView(0) // 헤더 레이아웃의 첫 번째 뷰
        val navUserIdTextView = headerView.findViewById<TextView>(R.id.nav_userid)

        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val userId = prefs.getString("LogIn_ID", "") ?: ""
        navUserIdTextView.text = userId

        val userRole: String? = LoginInfoUtil.getMemberCode(this) // 예: ROLE_PUB / ROLE_SELL / ROLE_PROJ
        applyMenuForRole(userRole)

        navView.setNavigationItemSelectedListener { menuItem ->
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

    private fun applyMenuForRole(role: String?) {
        navView.menu.clear()
        val menuRes = when (role) {
            "ROLE_SELL" -> R.menu.drawer_view_seller
            "ROLE_PROJ" -> R.menu.drawer_view_wholesaler
            else        -> R.menu.drawer_view_buyer
        }
        navView.inflateMenu(menuRes)
    }
}
