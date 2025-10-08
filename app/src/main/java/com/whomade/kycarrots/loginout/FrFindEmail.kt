package com.whomade.kycarrots.loginout

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.R
import com.whomade.kycarrots.StaticDataInfo
import com.whomade.kycarrots.dialog.DlgBtnActivity
import com.whomade.kycarrots.dialog.DlgFirstPhoneNumActivity
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.launch
import java.util.*

class FrFindEmail : Fragment(), View.OnClickListener {

    private lateinit var etName: EditText
    private lateinit var etEmailMiddleNum: EditText
    private lateinit var etEmailLastNum: EditText

    private var strName = ""
    private var strEmailPhoneNum = ""

    private val REQUEST_CODE_FIRST_NUM = 999
    private val REQUEST_CODE_FIND_EMAIL = 777

    companion object {
        private lateinit var txtEmailFirstNum: TextView

        fun getActivityResult(date: String) {
            txtEmailFirstNum.text = date
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fr_find_email, container, false)

        etName = view.findViewById(R.id.et_name)
        txtEmailFirstNum = view.findViewById(R.id.txt_first_num)
        etEmailMiddleNum = view.findViewById(R.id.et_middle_num)
        etEmailLastNum = view.findViewById(R.id.et_last_num)

        txtEmailFirstNum.setOnClickListener(this)
        view.findViewById<Button>(R.id.btn_find_ok).setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_find_ok -> chkInputData()
            R.id.txt_first_num -> {
                val intent = Intent(requireContext(), DlgFirstPhoneNumActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivityForResult(intent, REQUEST_CODE_FIRST_NUM)
            }
        }
    }



    private fun chkInputData() {
        strName = etName.text.toString()
        val mid = etEmailMiddleNum.text.toString()
        val last = etEmailLastNum.text.toString()
        strEmailPhoneNum = "${txtEmailFirstNum.text}-$mid-$last"

        when {
            strName.isBlank() -> Toast.makeText(requireContext(), getString(R.string.str_input_name), Toast.LENGTH_SHORT).show()
            mid.isBlank() || last.isBlank() -> Toast.makeText(requireContext(), getString(R.string.str_empty_phone_num), Toast.LENGTH_SHORT).show()
            else -> {
                val appService = AppServiceProvider.getService()
                setLoading(true)
                lifecycleScope.launch {
                    try {
                        val email = appService.findEmail(strName, strEmailPhoneNum)
                        setLoading(false)
                        if (!email.isNullOrEmpty()) {
                            val intent = Intent(requireContext(), DlgBtnActivity::class.java).apply {
                                putExtra("DlgTitle", getString(R.string.str_setting_alrim))
                                putExtra("BtnDlgMsg", getString(R.string.str_find_email_ok) + email)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            startActivityForResult(intent, REQUEST_CODE_FIND_EMAIL)
                        } else {
                            showError(getString(R.string.str_find_no_email))
                        }
                    } catch (e: Exception) {
                        setLoading(false)
                        showError(getString(R.string.str_http_error))
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        val intent = Intent(requireContext(), DlgBtnActivity::class.java).apply {
            putExtra("BtnDlgMsg", message)
        }
        startActivity(intent)
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
