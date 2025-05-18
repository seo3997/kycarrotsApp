package com.whomade.kycarrots.loginout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.R
import com.whomade.kycarrots.StaticDataInfo
import com.whomade.kycarrots.common.FindPassword
import com.whomade.kycarrots.dialog.DlgBtnActivity
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class FrFindPwd : Fragment(), View.OnClickListener {

    private lateinit var etEmail: EditText
    private lateinit var txtFirstNum: TextView
    private lateinit var etMiddleNum: TextView
    private lateinit var etLastNum: TextView

    private var mPhoneNum: String = ""
    private var mArrPhoneNum = arrayOf("", "", "")

    companion object {
        private const val REQUEST_CODE_EMAIL_PWD = 888
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fr_find_pwd_new, container, false)

        etEmail = view.findViewById(R.id.et_email)
        txtFirstNum = view.findViewById(R.id.txt_first_num)
        etMiddleNum = view.findViewById(R.id.et_middle_num)
        etLastNum = view.findViewById(R.id.et_last_num)

        view.findViewById<Button>(R.id.btn_find_pwd_ok).setOnClickListener(this)

        mPhoneNum = arguments?.getString("PhoneNum") ?: ""
        Log.d("FrFindPwd", "mPhoneNum = [$mPhoneNum]")

        if (mPhoneNum.isNotEmpty()) {
            mArrPhoneNum = mPhoneNum.split("-").toTypedArray()
        }

        txtFirstNum.text = mArrPhoneNum.getOrNull(0) ?: ""
        etMiddleNum.text = mArrPhoneNum.getOrNull(1) ?: ""
        etLastNum.text = mArrPhoneNum.getOrNull(2) ?: ""

        return view
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_find_pwd_ok) {
            if (chkInputEmail()) {
                chkInputData()
            }
        }
    }

    private fun chkInputEmail(): Boolean {
        val email = etEmail.text.toString()
        return if (email.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.str_input_email_err), Toast.LENGTH_SHORT).show()
            false
        } else {
            chkEmailType(email)
        }
    }

    private fun chkEmailType(email: String): Boolean {
        return if (checkEmail(email)) {
            true
        } else {
            Toast.makeText(requireContext(), R.string.str_email_type_err, Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun checkEmail(email: String): Boolean {
        val regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$"
        return Pattern.compile(regex).matcher(email).matches()
    }

    private fun chkInputData() {
        val strEmail = etEmail.text.toString()
        val strPhoneNum = "${txtFirstNum.text}-${etMiddleNum.text}-${etLastNum.text}"

        if (etMiddleNum.text.toString().isBlank() || etLastNum.text.toString().isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.str_empty_phone_num), Toast.LENGTH_SHORT).show()
            return
        }

        val appService = AppServiceProvider.getService()

        lifecycleScope.launch {
            val resultCode = FindPassword(requireContext(), strPhoneNum, strEmail, appService).find()

            when (resultCode) {
                StaticDataInfo.RESULT_CODE_ERR -> {
                    Toast.makeText(requireContext(), getString(R.string.str_http_error), Toast.LENGTH_SHORT).show()
                }

                StaticDataInfo.RESULT_NO_USER -> {
                    Toast.makeText(requireContext(), getString(R.string.str_no_data), Toast.LENGTH_SHORT).show()
                }

                StaticDataInfo.RESULT_CODE_200 -> {
                    val tempPwd = requireContext()
                        .getSharedPreferences("TempPasswordInfo", android.content.Context.MODE_PRIVATE)
                        .getString("TempPassword", "") ?: ""

                    val intent = Intent(requireContext(), DlgBtnActivity::class.java).apply {
                        putExtra("DlgTitle", getString(R.string.str_setting_alrim))
                        putExtra("BtnDlgMsg", getString(R.string.str_init_pwd, tempPwd))
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivityForResult(intent, REQUEST_CODE_EMAIL_PWD)
                }
            }
        }
    }
}
