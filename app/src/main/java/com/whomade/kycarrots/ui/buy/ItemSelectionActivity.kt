package com.whomade.kycarrots.ui.buy

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.whomade.kycarrots.ui.common.NotificationBadgeHelper
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.BaseDrawerActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.ui.Noti.NotificationListActivity

class ItemSelectionActivity : BaseDrawerActivity() {

    private var badge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_selection)

        // 툴바/드로어 유지
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            title = ""
        }
        toolbar.setNavigationOnClickListener {
            val drawer = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
            if (!drawer.isDrawerOpen(GravityCompat.START)) drawer.openDrawer(GravityCompat.START)
            else drawer.closeDrawer(GravityCompat.START)
        }

        // ✅ NavHostFragment 안전하게 가져오기 (없으면 생성)
        val navHostFragment = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)
            ?: run {
                val created = NavHostFragment.create(R.navigation.nav_graph)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, created)
                    .setPrimaryNavigationFragment(created)
                    .commitNow()
                created
            }
        val navController = navHostFragment.navController

        // BottomNavigation 연결
        val bottom = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottom.setupWithNavController(navController)

        // (선택) 툴바 타이틀 sync
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label
        }
    }

    // 배지 유지용: 기존 메뉴/헬퍼 그대로 사용
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        badge = NotificationBadgeHelper.attach(
            activity = this,
            menu = menu,
            toolbar = toolbar,
            menuItemId = R.id.action_notifications
        )
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
        return true
    }

    override fun onResume() {
        super.onResume()
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 여기서 햄버거를 처리하지 않아도, 위의 toolbar.setNavigationOnClickListener에서 Drawer 열림
        when (item.itemId) {
            R.id.action_notifications -> {
                startActivity(Intent(this, NotificationListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)

    }
}
