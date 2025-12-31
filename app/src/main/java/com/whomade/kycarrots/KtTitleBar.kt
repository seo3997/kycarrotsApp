package com.whomade.kycarrots.ui.common

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.whomade.kycarrots.R

class KtTitleBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val llBack: LinearLayout
    private val btnBack: ImageButton
    private val txtBack: TextView
    private val tvTitle: TextView
    private val ibMenu: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.title_bar, this, true)

        llBack = findViewById(R.id.ll_back)
        btnBack = findViewById(R.id.btn_back)
        txtBack = findViewById(R.id.txt_back)
        tvTitle = findViewById(R.id.txt_page_title)
        ibMenu = findViewById(R.id.ib_menu)

        // 기본: 뒤로가기
        llBack.setOnClickListener {
            (context as? Activity)?.onBackPressed()
        }
    }

    fun setTitle(title: String?) {
        tvTitle.text = title.orEmpty()
    }

    fun showMenu(show: Boolean) {
        ibMenu.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setOnMenuClick(listener: () -> Unit) {
        ibMenu.setOnClickListener { listener() }
    }

    fun setOnBackClick(listener: () -> Unit) {
        llBack.setOnClickListener { listener() }
    }
}
