package com.whomade.kycarrots

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class SoftKeyboardLinear @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var isKeyboardShown = false
    private val listeners = mutableListOf<SoftKeyboardListener>()
    private var layoutMaxH = 0f
    private val DETECT_ON_SIZE_PERCENT = 0.8f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newH = MeasureSpec.getSize(heightMeasureSpec)
        if (newH > layoutMaxH) {
            layoutMaxH = newH.toFloat()
        }
        if (layoutMaxH != 0f) {
            val sizePercent = newH / layoutMaxH
            if (!isKeyboardShown && sizePercent <= DETECT_ON_SIZE_PERCENT) {
                isKeyboardShown = true
                listeners.forEach { it.onSoftKeyboardShow() }
            } else if (isKeyboardShown && sizePercent > DETECT_ON_SIZE_PERCENT) {
                isKeyboardShown = false
                listeners.forEach { it.onSoftKeyboardHide() }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun addSoftKeyboardListener(listener: SoftKeyboardListener) {
        listeners.add(listener)
    }

    fun removeSoftKeyboardListener(listener: SoftKeyboardListener) {
        listeners.remove(listener)
    }

    interface SoftKeyboardListener {
        fun onSoftKeyboardShow()
        fun onSoftKeyboardHide()
    }
}
