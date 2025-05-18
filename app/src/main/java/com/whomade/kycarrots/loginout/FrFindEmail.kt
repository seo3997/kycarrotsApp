package com.whomade.kycarrots.loginout

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
    private lateinit var txtYear: TextView
    private lateinit var txtMonth: TextView
    private lateinit var txtDay: TextView

    private var strName = ""
    private var strBirthDate = ""
    private var strEmailPhoneNum = ""

    private val REQUEST_CODE_FIRST_NUM = 999
    private val REQUEST_CODE_FIND_EMAIL = 777

    companion object {
        private const val SET_YEAR = 0
        private const val SET_MONTH = 1
        private const val SET_DAY = 2
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
        txtYear = view.findViewById(R.id.txt_year)
        txtMonth = view.findViewById(R.id.txt_month)
        txtDay = view.findViewById(R.id.txt_day)

        txtEmailFirstNum.setOnClickListener(this)
        txtYear.setOnClickListener(this)
        txtMonth.setOnClickListener(this)
        txtDay.setOnClickListener(this)
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

            R.id.txt_year -> setDate(SET_YEAR)
            R.id.txt_month -> setDate(SET_MONTH)
            R.id.txt_day -> setDate(SET_DAY)
        }
    }

    private fun setDate(mode: Int) {
        val cal = Calendar.getInstance()
        val dates = mutableListOf<String>()
        when (mode) {
            SET_YEAR -> {
                val currentYear = cal.get(Calendar.YEAR)
                for (i in currentYear downTo currentYear - 110) {
                    dates.add(i.toString())
                }
            }

            SET_MONTH -> {
                for (i in 1..12) {
                    dates.add(i.toString().padStart(2, '0'))
                }
            }

            SET_DAY -> {
                if (txtMonth.text.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.str_sel_month_err), Toast.LENGTH_SHORT).show()
                    return
                }
                cal.set(Calendar.MONTH, txtMonth.text.toString().toInt() - 1)
                val maxDay = cal.getActualMaximum(Calendar.DATE)
                for (i in 1..maxDay) {
                    dates.add(i.toString().padStart(2, '0'))
                }
            }
        }
        openDateDialog(mode, dates)
    }

    private fun openDateDialog(mode: Int, list: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        AlertDialog.Builder(requireContext())
            .setAdapter(adapter) { _, which ->
                when (mode) {
                    SET_YEAR -> txtYear.text = adapter.getItem(which)
                    SET_MONTH -> txtMonth.text = adapter.getItem(which)
                    SET_DAY -> txtDay.text = adapter.getItem(which)
                }
            }.show()
    }

    private fun chkInputData() {
        strName = etName.text.toString()
        val year = txtYear.text.toString()
        val month = txtMonth.text.toString()
        val day = txtDay.text.toString()
        val mid = etEmailMiddleNum.text.toString()
        val last = etEmailLastNum.text.toString()

        strBirthDate = "$year-$month-$day"
        strEmailPhoneNum = "${txtEmailFirstNum.text}-$mid-$last"

        when {
            strName.isBlank() -> Toast.makeText(requireContext(), getString(R.string.str_input_name), Toast.LENGTH_SHORT).show()
            year.isBlank() || month.isBlank() || day.isBlank() -> Toast.makeText(requireContext(), getString(R.string.str_input_birthdate), Toast.LENGTH_SHORT).show()
            mid.isBlank() || last.isBlank() -> Toast.makeText(requireContext(), getString(R.string.str_empty_phone_num), Toast.LENGTH_SHORT).show()
            else -> {
                val appService = AppServiceProvider.getService()
                lifecycleScope.launch {
                    try {
                        val email = appService.findEmail(strName, strBirthDate, strEmailPhoneNum)
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
}
