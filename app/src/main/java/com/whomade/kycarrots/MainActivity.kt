package com.whomade.kycarrots

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.whomade.kycarrots.ui.ad.AdListFragment
import com.whomade.kycarrots.ui.ad.makead.MakeADMainActivity
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.let { ab ->
            ab.setHomeAsUpIndicator(R.drawable.ic_menu)
            ab.setDisplayHomeAsUpEnabled(true)
        }

        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        setupDrawerContent(navigationView)

        val viewPager: ViewPager = findViewById(R.id.viewpager)
        setupViewPager(viewPager)

        val floatingActionButton: FloatingActionButton = findViewById(R.id.fab)
        floatingActionButton.setOnClickListener { view ->
            //Snackbar.make(view, "광고 데이터를 불러옵니다.", Snackbar.LENGTH_LONG).setAction("닫기", null).show()
            val context = view.context
            val intent = Intent(context, MakeADMainActivity::class.java)
            context.startActivity(intent)
        }

        val tabLayout: TabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)
    }

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.menu_night_mode_system -> {
                setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            R.id.menu_night_mode_day -> {
                setNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            R.id.menu_night_mode_night -> {
                setNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            R.id.menu_night_mode_auto -> {
                setNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setNightMode(@NightMode nightMode: Int) {
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        viewPager.adapter = Adapter(supportFragmentManager).apply {
            addFragment(AdListFragment(), "광고 A")
            addFragment(AdListFragment(), "광고 B")
            addFragment(AdListFragment(), "광고 C")
        }
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            true
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

        override fun getItem(position: Int): Fragment = fragments[position]
        override fun getCount(): Int = fragments.size
        override fun getPageTitle(position: Int): CharSequence? = titles[position]
    }
}
