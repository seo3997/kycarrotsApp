// NotificationBadgeHelper.kt
package com.whomade.kycarrots.ui.common

import android.app.Activity
import android.view.Menu
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.local.PushRepositoryProvider
import kotlinx.coroutines.launch

object NotificationBadgeHelper {

    fun attach(
        activity: Activity,
        menu: Menu,
        toolbar: Toolbar,               // ✅ Toolbar로 명시
        @IdRes menuItemId: Int = R.id.action_notifications
    ): BadgeDrawable {
        val badge = BadgeDrawable.create(activity).apply {
            backgroundColor = ContextCompat.getColor(activity, android.R.color.holo_red_dark)
            badgeTextColor = ContextCompat.getColor(activity, android.R.color.white)
            number = 0
            isVisible = false
        }
        // ✅ Toolbar 오버로드 사용
        BadgeUtils.attachBadgeDrawable(badge, toolbar, menuItemId)
        return badge
    }

    fun refresh(
        activity: Activity,
        lifecycleScope: LifecycleCoroutineScope,
        badge: BadgeDrawable?
    ) {
        if (badge == null) return
        val userId = LoginInfoUtil.getUserId(activity)
        lifecycleScope.launch {
            val repo = PushRepositoryProvider.get(activity)
            val unread = repo.countUnread(userId)
            badge.number = unread
            badge.isVisible = unread > 0
        }
    }
}
