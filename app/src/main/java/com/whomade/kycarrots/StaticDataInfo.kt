package com.whomade.kycarrots

import android.app.Activity
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object StaticDataInfo {
    var currentActivity: Activity? = null

    const val TIME_OUT = 5000

    const val RESULT_NO_USER = 601
    const val RESULT_PWD_ERR = 602
    const val RESULT_MEMBER_CODE_ERR = 603
    const val RESULT_CODE_200 = 200
    const val RESULT_NO_DATA = 604
    const val RESULT_NO_SOCAIL_DATA = 605
    const val RESULT_CODE_ERR = 0


    /**
     * 현재 시간 구하는 함수
     * @return 현재 시간
     */
    fun nowTime(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdfNow = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return sdfNow.format(date)
    }

    /**
     * 천 단위 콤마 포맷팅
     */
    fun makeStringComma(str: String?): String {
        if (str.isNullOrEmpty()) return ""

        if (str.contains(",")) return str

        var valueStr = str
        var fractional = ""
        if (str.contains(".")) {
            val parts = str.split(".")
            valueStr = parts[0]
            if (parts.size > 1) {
                fractional = "." + parts[1]
            }
        }

        return try {
            val value = valueStr.toLong()
            val format = DecimalFormat("###,###")
            format.format(value) + fractional
        } catch (e: NumberFormatException) {
            str
        }
    }

    /**
     * 디렉토리 내 모든 파일/폴더 삭제
     */
    fun delDir(path: String) {
        val file = File(path)
        val childFiles = file.listFiles()
        if (childFiles != null && childFiles.isNotEmpty()) {
            for (child in childFiles) {
                if (child.isDirectory) {
                    delDir(child.absolutePath)
                } else {
                    child.delete()
                }
            }
        }
        file.delete()
    }
}
