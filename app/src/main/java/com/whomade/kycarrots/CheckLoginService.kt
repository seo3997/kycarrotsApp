package com.whomade.kycarrots

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.IBinder

class CheckLoginService : Service() {

    override fun onCreate() {
        super.onCreate()
        start_service = true
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        var start_service: Boolean = false
        val mActivityList: MutableList<Activity> = mutableListOf()

        // 전체 액티비티 종료
        fun closeAll() {
            for (activity in mActivityList) {
                try {
                    activity.finish()
                } catch (e: Exception) {
                    // 예외 무시
                }
            }
        }

        // MainActivity 제외하고 종료
        fun closeActivity() {
            for (activity in mActivityList) {
                if (!activity.toString().contains("MainActivity")) {
                    activity.finish()
                }
            }
        }
    }
}
