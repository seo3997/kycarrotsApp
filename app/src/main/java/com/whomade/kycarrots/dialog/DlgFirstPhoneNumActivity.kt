package com.whomade.kycarrots.dialog

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.*
import com.whomade.CheckLoginService
import com.whomade.kycarrots.R

/**
 * 휴대폰 번호 첫번호 선택 Dialog
 */
class DlgFirstPhoneNumActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Dialog 사이즈 조절
        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        params.gravity = Gravity.CENTER
        window.attributes = params

        setContentView(R.layout.dlg_txt_list)
        CheckLoginService.mActivityList.add(this)

        findViewById<TextView>(R.id.txt_dlg_title).text = getString(R.string.str_phone_number)

        val resultIntent = Intent()
        val phonePrefixList = resources.getStringArray(R.array.first_phone_num)

        val listView = findViewById<ListView>(R.id.lv_txt)
        val adapter = ArrayAdapter(this, R.layout.list_txt_item, phonePrefixList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            resultIntent.putExtra("FirstPhoneNum", phonePrefixList[position])
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
