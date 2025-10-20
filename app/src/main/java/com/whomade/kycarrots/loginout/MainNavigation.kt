package com.whomade.kycarrots.loginout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.whomade.kycarrots.IntroActivity
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.ui.common.LoginInfoUtil

object MainNavigation {
    fun goMain(activity: AppCompatActivity, login: LoginResponse? = null) {
        LoginInfoUtil.saveLoginInfo(activity,login?.login_id!!,login?.login_idx ?: "", login?.login_social_id ?: "",login?.member_code!!,login?.login_nm ?: "",login.login_cd,login.login_social_id)

        activity.startActivity(
            Intent(activity, IntroActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        activity.finish()
    }
}