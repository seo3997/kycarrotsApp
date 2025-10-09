package com.whomade.kycarrots.loginout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.isGone
import androidx.core.view.isVisible
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


    companion object {
        private const val REQUEST_CODE_EMAIL_PWD = 888
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fr_find_pwd_new, container, false)

        etEmail = view.findViewById(R.id.et_email)

        view.findViewById<Button>(R.id.btn_find_pwd_ok).setOnClickListener(this)
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

        if (strEmail.isBlank() || strEmail.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.str_input_email_err), Toast.LENGTH_SHORT).show()
            return
        }

        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            setLoading(true)
            val resultCode = FindPassword(requireContext(), strEmail, appService).find()
            setLoading(false)

            when (resultCode) {
                StaticDataInfo.RESULT_CODE_ERR -> {
                    Toast.makeText(requireContext(), getString(R.string.str_http_error), Toast.LENGTH_SHORT).show()
                }

                StaticDataInfo.RESULT_NO_USER -> {
                    Toast.makeText(requireContext(), getString(R.string.str_no_data), Toast.LENGTH_SHORT).show()
                }

                StaticDataInfo.RESULT_CODE_200 -> {
                    val intent = Intent(requireContext(), DlgBtnActivity::class.java).apply {
                        putExtra("DlgTitle", getString(R.string.str_setting_alrim))
                        putExtra("BtnDlgMsg", getString(R.string.str_init_pwd, strEmail))
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivityForResult(intent, REQUEST_CODE_EMAIL_PWD)
                }
            }
        }
    }
    fun setLoading(show: Boolean) {
        val v = view?.findViewById<View>(R.id.ll_progress_circle) ?: return
        if (show) {
            v.isVisible = true
            activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            v.isGone = true
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

}
