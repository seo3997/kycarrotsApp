package com.whomade.kycarrots.loginout

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.whomade.kycarrots.CheckLoginService
import com.whomade.kycarrots.MainTitleBar
import com.whomade.kycarrots.R
import com.whomade.kycarrots.TitleBar


class FindEmailPwdActivity : FragmentActivity(), View.OnClickListener {

    private lateinit var txtFindEmail: TextView
    private lateinit var llFindEmailUnder: LinearLayout

    private lateinit var txtFindPwd: TextView
    private lateinit var llFindPwdUnder: LinearLayout

    private lateinit var mTitle: TitleBar
    private lateinit var txtInfo: TextView

    private val BOOL_FIND_MODE_EMAIL = true
    private val BOOL_FIND_MODE_PWD = false

    private val REQUEST_CODE_FIRST_NUM = 999
    private val REQUEST_CODE_FIND_EMAIL = 777
    private val REQUEST_CODE_EMAIL_PWD = 888
    private val REQUEST_PHONE_NUM = 666

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_find_id_pwd)
        CheckLoginService.mActivityList.add(this)

        val layoutBG = findViewById<LinearLayout>(R.id.ll_bg)
        layoutBG.background = BitmapDrawable(resources, BitmapFactory.decodeResource(resources,
            R.drawable.display_bg
        ))

        val mMainTitleBar = findViewById<MainTitleBar>(R.id.main_title_bar)
        mMainTitleBar.findViewById<ImageButton>(R.id.ib_refresh).visibility = View.GONE
        mMainTitleBar.findViewById<ImageButton>(R.id.ib_home).visibility = View.GONE

        mTitle = findViewById(R.id.title_bar)
        mTitle.setTitle(getString(R.string.str_find_email_title))

        txtFindEmail = findViewById(R.id.txt_find_email)
        txtFindEmail.setOnClickListener(this)
        llFindEmailUnder = findViewById(R.id.ll_find_email_under)

        txtFindPwd = findViewById(R.id.txt_find_pwd)
        txtFindPwd.setOnClickListener(this)
        llFindPwdUnder = findViewById(R.id.ll_find_pwd_under)

        txtInfo = findViewById(R.id.txt_info)
        txtInfo.text = getString(R.string.str_find_email_info)

        switchFragment(BOOL_FIND_MODE_EMAIL, "")
    }

    override fun onDestroy() {
        super.onDestroy()
        recycleView(findViewById(R.id.ll_bg))
    }

    private fun recycleView(view: View?) {
        view?.background?.let { bg ->
            bg.callback = null
            (bg as? BitmapDrawable)?.bitmap?.recycle()
            view.background = null
        }
    }

    fun switchFragment(findMode: Boolean, pPhoneNum: String) {
        val fr: Fragment = if (findMode) {
            FrFindEmail()
        } else {
            val args = Bundle().apply { putString("PhoneNum", pPhoneNum) }
            FrFindPwd().apply { arguments = args }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.ll_find_fr, fr)
            .commit()
    }

    fun setFindPwdInit(phoneNum: String) {
        mTitle.setTitle(getString(R.string.str_find_pwd_title))
        txtInfo.text = getString(R.string.str_find_pwd_info)
        llFindEmailUnder.visibility = View.GONE
        llFindPwdUnder.visibility = View.VISIBLE
        txtFindEmail.setBackgroundColor(
            ContextCompat.getColor(this, R.color.color_white)
        )
        txtFindPwd.setBackgroundColor(
            ContextCompat.getColor(this, R.color.colorRAccent)
        )
        switchFragment(BOOL_FIND_MODE_PWD, phoneNum)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.txt_find_email -> {
                mTitle.setTitle(getString(R.string.str_find_email_title))
                txtInfo.text = getString(R.string.str_find_email_info)
                llFindEmailUnder.visibility = View.VISIBLE
                llFindPwdUnder.visibility = View.GONE
                txtFindEmail.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.colorRAccent)
                )
                txtFindPwd.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.color_white)
                )

                switchFragment(BOOL_FIND_MODE_EMAIL, "")
            }

            R.id.txt_find_pwd -> {
                setFindPwdInit("")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_FIRST_NUM -> {
                    val phone = data?.getStringExtra("FirstPhoneNum") ?: ""
                    FrFindEmail.getActivityResult(phone)
                }

                REQUEST_PHONE_NUM -> {
                    val phoneNum = data?.getStringExtra("PHONE_NUM") ?: ""
                    setFindPwdInit(phoneNum)
                }

                REQUEST_CODE_FIND_EMAIL,
                REQUEST_CODE_EMAIL_PWD -> {
                    finish()
                }
            }
        }
    }
}
