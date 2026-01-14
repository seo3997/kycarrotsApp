package com.whomade.kycarrots.ui.common

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest

fun Context.printKakaoKeyHash() {
    try {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        }

        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo!!.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        }

        signatures!!.forEach { sig ->
            val md = MessageDigest.getInstance("SHA")
            md.update(sig.toByteArray())
            val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
            Log.d("KAKAO_KEYHASH", "keyHash=$keyHash")
        }
    } catch (e: Exception) {
        Log.e("KAKAO_KEYHASH", "printKakaoKeyHash failed", e)
    }
}
