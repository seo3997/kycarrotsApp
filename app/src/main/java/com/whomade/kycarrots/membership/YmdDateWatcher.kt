package com.whomade.kycarrots.membership

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Locale

// 1) 자동 포맷: YYYY-MM-DD
class YmdDateWatcher(private val edit: TextInputEditText) : TextWatcher {
    private var self = false
    override fun afterTextChanged(s: Editable?) {
        if (self) return
        val digits = s.toString().filter { it.isDigit() }.take(8) // yyyy mm dd
        val formatted = buildString {
            if (digits.length >= 4) append(digits.substring(0,4)) else append(digits)
            if (digits.length > 4) {
                append('-'); append(digits.substring(4, minOf(6, digits.length)))
            }
            if (digits.length > 6) {
                append('-'); append(digits.substring(6, minOf(8, digits.length)))
            }
        }
        if (formatted != s.toString()) {
            self = true
            edit.setText(formatted)
            edit.setSelection(formatted.length)
            self = false
        }
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

// 2) 유효성 검사 (존재하는 날짜인지)
fun isValidYmd(ymd: String): Boolean {
    if (!Regex("""^\d{4}-\d{2}-\d{2}$""").matches(ymd)) return false
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
    return try {
        sdf.parse(ymd) != null
    } catch (_: Exception) {
        false
    }
}