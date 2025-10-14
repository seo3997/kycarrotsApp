package com.whomade.kycarrots

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class KycarrotsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "7cfedd989e5e9c768c1f70d85ece26e2")
    }
}