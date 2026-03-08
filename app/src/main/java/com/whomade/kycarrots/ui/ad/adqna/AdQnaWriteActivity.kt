package com.whomade.kycarrots.ui.ad.adqna

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.model.QnaVo
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import kotlinx.coroutines.launch

class AdQnaWriteActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etContents: TextInputEditText
    private lateinit var cbSecret: CheckBox
    private lateinit var btnSubmit: Button
    
    private var productId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_qna_write)

        productId = intent.getLongExtra("productId", 0L)
        if (productId == 0L) {
            Toast.makeText(this, "상품 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "상품 문의 작성"

        etTitle = findViewById(R.id.et_title)
        etContents = findViewById(R.id.et_contents)
        cbSecret = findViewById(R.id.cb_secret)
        btnSubmit = findViewById(R.id.btn_submit)

        btnSubmit.setOnClickListener {
            submitQna()
        }
    }

    private fun submitQna() {
        val title = etTitle.text.toString().trim()
        val contents = etContents.text.toString().trim()
        val isSecret = cbSecret.isChecked
        
        if (title.isEmpty() || contents.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = LoginInfoUtil.getUserId(this)
        val branchId = LoginInfoUtil.getBranchId(this)

        lifecycleScope.launch {
            try {
                val success = AppServiceProvider.getService().insertQna(
                    productId.toString(),
                    title,
                    contents,
                    if (isSecret) "Y" else "N"
                )
                if (success) {
                    Toast.makeText(this@AdQnaWriteActivity, "문의가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AdQnaWriteActivity, "등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdQnaWriteActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
