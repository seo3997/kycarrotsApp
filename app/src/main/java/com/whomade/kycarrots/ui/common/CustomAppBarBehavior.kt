package com.whomade.kycarrots.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

class CustomAppBarBehavior @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppBarLayout.Behavior(context, attrs) {

    // fling 속도가 너무 빠르면 무시
    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return if (abs(velocityY) > 6000) {
            // fling 무시
            true
        } else {
            super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
        }
    }

    // 필요할 경우 시작을 명확히 허용
    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == View.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(parent, child, directTargetChild, target, axes, type)
    }
}
