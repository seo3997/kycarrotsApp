package com.whomade.kycarrots

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.common.Constants.SYSTEM_TYPE
import com.whomade.kycarrots.data.model.OpUserVO
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.Noti.NotificationListActivity
import com.whomade.kycarrots.ui.ad.AdListFragment
import com.whomade.kycarrots.ui.ad.makead.KtMakeADMainActivity
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.NotificationBadgeHelper
import com.whomade.kycarrots.ui.dialog.BottomDto
import com.whomade.kycarrots.ui.dialog.BottomDtoPickerSheet
import kotlinx.coroutines.launch
import java.util.ArrayList

class MainActivity : BaseDrawerActivity() {
    private var badge: BadgeDrawable? = null
    private lateinit var toolbar: Toolbar

    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: Adapter

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




        viewPager = findViewById(R.id.viewpager)
        setupViewPager(viewPager)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                // 필요 시 탭 전환 때 재조회 (원치 않으면 주석)
                val tag = "android:switcher:${R.id.viewpager}:$position"
                (supportFragmentManager.findFragmentByTag(tag) as? AdListFragment)
                    ?.fetchAdvertiseList(isRefresh = true)
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
            //registerLauncher.launch(Intent(this, MakeADMainActivity::class.java))
            if (SYSTEM_TYPE == 1) {
                moveToMakeAD()
            } else {
                launchMakeAdWithCenterGuard()
            }

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

    private fun launchMakeAdWithCenterGuard() {
        showProgressBar()
        val appService = AppServiceProvider.getService();
        val userId = LoginInfoUtil.getUserId(this)
        lifecycleScope.launch {
            try {


                // 1) 기본 중간센터 있는지 확인
                val defaultNo = appService.getDefaultWholesaler(userId)
                if (defaultNo != null) {
                    moveToMakeAD()
                    return@launch
                }

                // 2) 없으면 도매상 목록 → 선택 다이얼로그
                val wholesalers = appService.getWholesalers(Constants.ROLE_PROJ)
                val centers = wholesalers.map { it.toBottomDto() }
                if (centers.isEmpty()) {
                    Toast.makeText(this@MainActivity, "선택 가능한 중간센터가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                BottomDtoPickerSheet.new(
                    centers = centers,
                    title = "중간센터 선택",
                    onPicked = { picked ->
                        lifecycleScope.launch {
                            showProgressBar()
                            try {
                                val ok = appService.setDefaultWholesaler(userId, picked.code)
                                if (ok) {
                                    Toast.makeText(this@MainActivity, "기본 중간센터 지정 완료", Toast.LENGTH_SHORT).show()
                                    moveToMakeAD()
                                } else {
                                    Toast.makeText(this@MainActivity, "센터 지정 실패", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(this@MainActivity, "센터 지정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            } finally {
                                hideProgressBar()
                            }
                        }
                    }
                ).show(supportFragmentManager, "center_picker")

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                hideProgressBar()
            }
        }
    }
    private fun moveToMakeAD() {
        registerLauncher.launch(Intent(this, KtMakeADMainActivity::class.java))
    }

    private fun showProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.GONE
    }
    private fun OpUserVO.toBottomDto(): BottomDto =
        BottomDto(
            code = this.userNo?.toString() ?: "", // Long → String
            name = this.userNm ?: (this.userId ?: ""),
            text1 = null, text2 = null, text3 = null, text4 = null
        )

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
        /*
        val pos = viewPager.currentItem
        (pagerAdapter.getFragment(pos) as? Refreshable)?.refresh()
         */
        /*
        for (i in 0 until pagerAdapter.count) {
            (pagerAdapter.getFragment(i) as? Refreshable)?.refresh()
        }
         */
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
        pagerAdapter  = buildPagerAdapter()
        viewPager.adapter = pagerAdapter

        // ✅ 모든 프래그먼트를 미리 생성해서 onViewCreated/fetchAdvertiseList가 즉시 실행되도록
        viewPager.offscreenPageLimit = pagerAdapter.count
    }

    private fun buildPagerAdapter(): Adapter {
        val items = if (SYSTEM_TYPE == 2) {
            listOf(
                "0" to "승인반려",
                "1" to "판매중",
                "2" to "예약중",
                "3" to "판매완료"
            )
        } else {
            listOf(
                "1" to "판매중",
                "2" to "예약중",
                "3" to "판매완료"
            )
        }

        return Adapter(supportFragmentManager).apply {
            items.forEach { (status, title) ->
                addFragment(AdListFragment.newInstance(status), title)
            }
        }
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
        fun getFragment(position: Int): Fragment = fragments[position]
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
                // (선택) 판매중 탭만 확실히 한 번 더
                refreshTabBySaleStatus("1")
            }
        }
    }

    fun refreshTabBySaleStatus(status: String?) {
        val position = when (status) {
            "0"  -> 0  // 판매중 탭
            "1"  -> 1  // 판매중 탭
            "10" -> 2  // 예약중 탭
            "99" -> 3  // 판매완료 탭
            else -> return
        }
        val tag = "android:switcher:${R.id.viewpager}:$position"
        (supportFragmentManager.findFragmentByTag(tag) as? AdListFragment)
            ?.fetchAdvertiseList(isRefresh = true)
    }
}
