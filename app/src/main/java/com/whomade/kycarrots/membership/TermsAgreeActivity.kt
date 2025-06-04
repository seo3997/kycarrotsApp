package com.whomade.kycarrots.membership

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.*
import com.whomade.kycarrots.CheckLoginService
import com.whomade.kycarrots.MainTitleBar
import com.whomade.kycarrots.R
import com.whomade.kycarrots.TitleBar
import com.whomade.kycarrots.dialog.DlgBtnActivity

class TermsAgreeActivity : Activity(), View.OnClickListener {

    private lateinit var chkAgreeAll: CheckBox
    private lateinit var chkAgree1: CheckBox
    private lateinit var chkAgree2: CheckBox

    private val NOT_TERMS_AGREE = 0
    private val MOBILE_AUTHENTICATION = 1
    private val REQUEST_OK = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_agree)
        CheckLoginService.mActivityList.add(this)

        val layoutBG = findViewById<FrameLayout>(R.id.fl_bg)
        layoutBG.background = BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.display_bg))

        val mMainTitleBar = findViewById<MainTitleBar>(R.id.main_title_bar)
        mMainTitleBar.findViewById<ImageButton>(R.id.ib_refresh).visibility = View.GONE
        mMainTitleBar.findViewById<ImageButton>(R.id.ib_home).visibility = View.GONE
        findViewById<TitleBar>(R.id.title_bar).setTitle(getString(R.string.str_terms_agree_title))

        chkAgreeAll = findViewById(R.id.chk_agree_all)
        chkAgree1 = findViewById(R.id.chk_agree1)
        chkAgree2 = findViewById(R.id.chk_agree2)

        chkAgreeAll.setOnClickListener(this)
        chkAgree1.setOnClickListener(this)
        chkAgree2.setOnClickListener(this)

        findViewById<TextView>(R.id.txt_agree1).setOnClickListener(this)
        findViewById<TextView>(R.id.txt_agree2).setOnClickListener(this)

        findViewById<Button>(R.id.btn_agree_ok).setOnClickListener(this)
        findViewById<Button>(R.id.btn_agree_cancel).setOnClickListener(this)

        val wvTerms1 = findViewById<WebView>(R.id.wv_terms_1)
        val wvTerms2 = findViewById<WebView>(R.id.wv_terms_2)
        wvTerms1.loadUrl(getString(R.string.str_url_join_terms1))
        wvTerms2.loadUrl(getString(R.string.str_url_join_terms2))
        wvTerms1.setBackgroundColor(0)
        wvTerms2.setBackgroundColor(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        recycleView(findViewById(R.id.fl_bg))
    }

    private fun recycleView(view: View?) {
        view?.background?.let { bg ->
            bg.callback = null
            (bg as? BitmapDrawable)?.bitmap?.recycle()
            view.background = null
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.chk_agree_all -> {
                val checked = chkAgreeAll.isChecked
                chkAgree1.isChecked = checked
                chkAgree2.isChecked = checked
            }

            R.id.txt_agree1, R.id.txt_agree2, R.id.chk_agree1, R.id.chk_agree2 -> {
                chkAgreeAll.isChecked = chkAgree1.isChecked && chkAgree2.isChecked
            }

            R.id.btn_agree_ok -> {
                if (chkAgreeAll.isChecked) {
                    dialogShow(MOBILE_AUTHENTICATION)
                } else {
                    dialogShow(NOT_TERMS_AGREE)
                }
            }

            R.id.btn_agree_cancel -> {
                finish()
            }
        }
    }

    /**
     * 약관 비동의, 휴대폰 인증 팝업
     */
    private fun dialogShow(type: Int) {
        val i = Intent(this, DlgBtnActivity::class.java)
        when (type) {
            NOT_TERMS_AGREE -> {
                i.putExtra("DlgTitle", getString(R.string.str_terms_agree_title))
                i.putExtra("BtnDlgMsg", getString(R.string.str_agree_err))
                startActivity(i)
            }

            MOBILE_AUTHENTICATION -> {
                i.putExtra("DlgTitle", getString(R.string.str_mobile_authentication_title))
                i.putExtra("BtnDlgMsg", getString(R.string.str_mobile_authentication_msg))
                i.putExtra("DlgMode", "Two")
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivityForResult(i, REQUEST_OK)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*
        if (resultCode == RESULT_OK && requestCode == REQUEST_OK) {
            val intent = Intent(this, MobileAuthenticationWebActivity::class.java)
            intent.putExtra("ReturnCd", 1)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }*/
    }
}
