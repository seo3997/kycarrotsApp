package com.whomade.kycarrots

import android.app.Activity
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object StaticDataInfo {
    var currentActivity: Activity? = null

    const val TIME_OUT = 5000
    const val STRING_Y = "Y"
    const val STRING_N = "N"

    const val STRING_M = "M"
    const val STRING_P = "P"

    const val FALSE = 0
    const val TRUE = 1
    const val CHARAGE_REJECT = 2

    const val RESULT_CODE_MY_POINT = 1000

    const val RESULT_NO_USER = 601
    const val RESULT_PWD_ERR = 602
    const val RESULT_CODE_200 = 200
    const val RESULT_CODE_ADVERTISER = 201
    const val RESULT_CODE_NO_ADVERTISER = 202
    const val RESULT_CODE_WAIT_ADVERTISER = 203
    const val RESULT_CODE_REJECT_ADVERTISER = 204
    const val RESULT_CODE_AUTHORIZATION_CODE_ERR = 503
    const val RESULT_OVER_SAVE_POINT = 504
    const val RESULT_NO_SAVE_POINT = 100
    const val RESULT_OVERLAP_ERR = 603
    const val RESULT_NO_DATA = 604
    const val RESULT_NO_RECOMMEND = 605
    const val RESULT_NO_DI = 606
    const val RESULT_OVERLAP_DI = 607
    const val RESULT_NO_SIGUN = 608
    const val RESULT_NO_INPUT_RECOMMEND = 609
    const val RESULT_CODE_ERR = 0

    const val STR_P = "p"
    const val TAG_ITEM = "item"
    const val TAG_LIST = "<list>"
    const val TAG_NO_LIST = "<list/>"
    const val SEND_URL = 0
    const val SEND_TOKEN = 1

    const val COMMON_CODE_TYPE_AD = "ad"
    const val COMMON_CODE_TYPE_DS = "ds"
    const val COMMON_CODE_TYPE_BK = "bk"
    const val COMMON_CODE_TYPE_JD = "jd"
    const val COMMON_CODE_TYPE_CH = "ch"

    const val RESULT_SI_DO = 5555
    const val RESULT_SI_GUN_GU = 6666
    const val RESULT_SEND_AD_INFO = 7777
    const val RESULT_AD_INFO = 8888

    const val MODE_POINT_LIST_ACCRUE = "Acc"
    const val MODE_POINT_LIST_USE = "Use"

    const val MODE_MY_AD = "My"

    const val ADVER_INTEREST = 111

    const val SEX_MEN = "M"
    const val SEX_FEMALE = "F"

    const val BELL_AD_CODE_AGE_10 = "73"
    const val BELL_AD_CODE_AGE_20 = "74"
    const val BELL_AD_CODE_AGE_30 = "75"
    const val BELL_AD_CODE_AGE_40 = "95"
    const val BELL_AD_CODE_AGE_50 = "96"
    const val BELL_AD_CODE_AGE_60 = "97"
    const val BELL_AD_CODE_AGE_70 = "99"
    const val BELL_AD_CODE_SEX_MEN = "116"
    const val BELL_AD_CODE_SEX_FEMALE = "117"

    const val BELL_AD_NO_TIME = "NoTime"

    var STR_CHARATER_DOWN_OTHER = "DownOther"
    var STR_CHARATER_DOWN = "Down"
    var STR_CHARATER_OTHER = "Other"

    var STR_CHARGE_CODE_TAKE = "TAKE"

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
