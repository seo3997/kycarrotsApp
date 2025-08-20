package com.whomade.kycarrots

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.whomade.kycarrots.ui.Noti.NotificationListActivity
import com.whomade.kycarrots.ui.ad.AdListFragment
import com.whomade.kycarrots.ui.ad.makead.MakeADMainActivity
import com.whomade.kycarrots.ui.common.NotificationBadgeHelper
import java.util.ArrayList

class MainActivity : BaseDrawerActivity() {
    private var badge: BadgeDrawable? = null
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.let { ab ->
            ab.setHomeAsUpIndicator(R.drawable.ic_menu)
            ab.setDisplayHomeAsUpEnabled(true)
            title = "내 등록 매물"
        }




        val viewPager: ViewPager = findViewById(R.id.viewpager)
        setupViewPager(viewPager)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                val fragment = supportFragmentManager.findFragmentByTag("android:switcher:${R.id.viewpager}:$position")
                if (fragment is AdListFragment) {
                    fragment.fetchAdvertiseList()
                }
            }
        })

        val floatingActionButton: FloatingActionButton = findViewById(R.id.fab)
        floatingActionButton.setOnClickListener { view ->
            //Snackbar.make(view, "광고 데이터를 불러옵니다.", Snackbar.LENGTH_LONG).setAction("닫기", null).show()
            /*
            val context = view.context
            val intent = Intent(context, MakeADMainActivity::class.java)
            context.startActivity(intent)
             */
            registerLauncher.launch(Intent(this, MakeADMainActivity::class.java))
        }

        val tabLayout: TabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)
        /*
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    floatingActionButton.show()
                } else {
                    floatingActionButton.hide()
                }
            }
        })
         */

    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sample_actions, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                menu.findItem(R.id.menu_night_mode_system).isChecked = true
            }
            AppCompatDelegate.MODE_NIGHT_AUTO -> {
                menu.findItem(R.id.menu_night_mode_auto).isChecked = true
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                menu.findItem(R.id.menu_night_mode_night).isChecked = true
            }
            AppCompatDelegate.MODE_NIGHT_NO -> {
                menu.findItem(R.id.menu_night_mode_day).isChecked = true
            }
        }
        return true
    }
     */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        badge = NotificationBadgeHelper.attach(
            activity = this,
            menu = menu,
            toolbar = toolbar,              // ✅ Toolbar 전달
            menuItemId = R.id.action_notifications
        )
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
        return true
    }

    override fun onResume() {
        super.onResume()
        // 돌아왔을 때 뱃지 다시 갱신
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_notifications -> {
                startActivity(Intent(this, NotificationListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = Adapter(supportFragmentManager).apply {
            addFragment(AdListFragment.newInstance("1"), "전체")
            addFragment(AdListFragment.newInstance("2"), "처리중")
            addFragment(AdListFragment.newInstance("3"), "완료")
        }
        viewPager.adapter = adapter

        // ✅ 모든 프래그먼트를 미리 생성해서 onViewCreated/fetchAdvertiseList가 즉시 실행되도록
        viewPager.offscreenPageLimit = adapter.count
    }

    internal class Adapter(
        fragmentManager: FragmentManager
    ) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val fragments: MutableList<Fragment> = ArrayList()
        private val titles: MutableList<String> = ArrayList()

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getItem(position: Int): Fragment = fragments[position]
        override fun getCount(): Int = fragments.size
        override fun getPageTitle(position: Int): CharSequence? = titles[position]
    }

    val registerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val isSuccess = result.data?.getBooleanExtra("register_result", false) ?: false
            if (isSuccess) {
                // FragmentManager를 통해 FragmentResult로 전달
                supportFragmentManager.setFragmentResult("register_result_key", Bundle().apply {
                    putBoolean("register_result", true)
                })
            }
        }
    }


}
