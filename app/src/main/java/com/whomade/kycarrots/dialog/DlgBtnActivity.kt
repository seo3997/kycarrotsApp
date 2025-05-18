package com.whomade.kycarrots.dialog

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.whomade.CheckLoginService
import com.whomade.kycarrots.R


class DlgBtnActivity : Activity(), View.OnTouchListener {

    private var isCallMode: Boolean = false
    private var sViewUrl: String = ""

    private lateinit var btn1: Button
    private var btn2: Button? = null

    companion object {
        const val DIALOG_MODE_TWO = "Two"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dlg_btn_layout)

        CheckLoginService.mActivityList.add(this)

        // 다이얼로그 전체화면 설정
        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = params

        val intent = intent

        val sTitle = intent.getStringExtra("DlgTitle")
        val sMode = intent.getStringExtra("DlgMode")
        val sMsg = intent.getStringExtra("BtnDlgMsg")
        val sBtn1 = intent.getStringExtra("BtnDlgOneText")
        val sBtn2 = intent.getStringExtra("BtnDlgCancelText")

        isCallMode = intent.getBooleanExtra("CallMode", false)
        if (isCallMode) {
            sViewUrl = intent.getStringExtra("ViewUrl") ?: ""
        }

        findViewById<TextView>(R.id.txt_dlg_title)?.apply {
            if (!sTitle.isNullOrEmpty()) text = sTitle
        }

        findViewById<TextView>(R.id.txt_dlg_msg)?.text = sMsg

        val ll1 = findViewById<LinearLayout>(R.id.ll1)
        btn1 = findViewById(R.id.btn1)
        val txt1 = findViewById<TextView>(R.id.txt1)
        if (!sBtn1.isNullOrEmpty()) txt1.text = sBtn1
        ll1.setOnTouchListener(this)
        btn1.setOnTouchListener(this)

        if (sMode == DIALOG_MODE_TWO) {
            findViewById<LinearLayout>(R.id.ll_btn_divider).visibility = View.VISIBLE
            val ll2 = findViewById<LinearLayout>(R.id.ll2)
            btn2 = findViewById(R.id.btn2)
            val txt2 = findViewById<TextView>(R.id.txt2)
            if (!sBtn2.isNullOrEmpty()) txt2.text = sBtn2
            ll2.setOnTouchListener(this)
            ll2.visibility = View.VISIBLE
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            R.id.ll1, R.id.btn1 -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        btn1.background = getDrawable(R.drawable.dlg_chk_press)
                    }

                    MotionEvent.ACTION_UP -> {
                        if (isCallMode && sViewUrl.isNotEmpty()) {
                            val callIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sViewUrl))
                            callIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(callIntent)
                        } else {
                            setResult(RESULT_OK)
                        }
                        finish()
                    }
                }
                return true
            }

            R.id.ll2 -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        btn2?.background = getDrawable(R.drawable.dlg_chk_press)
                    }

                    MotionEvent.ACTION_UP -> finish()
                }
                return true
            }
        }
        return false
    }
}
