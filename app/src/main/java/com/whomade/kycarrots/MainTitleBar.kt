package com.whomade.kycarrots

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * 최상단 title bar (MainTitleBar Kotlin 버전)
 */
class MainTitleBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val ibHome: ImageButton
    private val ivLogo: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.main_title_bar, this, true)

        ibHome = findViewById(R.id.ib_home)
        ivLogo = findViewById(R.id.iv_logo)

        ibHome.setOnClickListener(this)
        ivLogo.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        // Java 원본에 onClick 내용이 축약(...)되어 있어 기본 hook만 제공
        when (v?.id) {
            R.id.ib_home -> onHomeClickListener?.invoke()
            R.id.iv_logo -> onLogoClickListener?.invoke()
        }
    }

    private var onHomeClickListener: (() -> Unit)? = null
    private var onLogoClickListener: (() -> Unit)? = null

    fun setOnHomeClickListener(listener: (() -> Unit)?) {
        onHomeClickListener = listener
    }

    fun setOnLogoClickListener(listener: (() -> Unit)?) {
        onLogoClickListener = listener
    }
}
