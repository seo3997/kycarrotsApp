package com.whomade.kycarrots.ui.ad.makead

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.whomade.kycarrots.R
import com.whomade.kycarrots.ui.dialog.DlgBtnActivity
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class KtMakeADDetailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs), View.OnClickListener {

    companion object {
        const val STR_PUT_AD_IDX = "AD_IDX"
        const val STR_PUT_AD_NAME = "AD_NAME"
        const val STR_PUT_AD_QUANTITY = "AD_QUANTITY"
        const val STR_PUT_AD_UNIT_CODE = "AD_UNIT_CODE"
        const val STR_PUT_AD_AMOUNT = "AD_AMOUNT"
        const val STR_PUT_AD_DESIRED_SHIPPING_DATE = "AD_DESIRED_SHIPPING_DATE"
        const val STR_PUT_AD_DETAIL = "AD_DETAIL"
        const val STR_PUT_AD_CATEGORY_MID = "AD_CATEGORY_MID"
        const val STR_PUT_AD_CATEGORY_SCLS = "AD_CATEGORY_SCLS"
        const val STR_PUT_AD_AREA_MID = "AD_AREA_MID"
        const val STR_PUT_AD_AREA_SCLS = "AD_AREA_SCLS"
        const val STR_PUT_AD_UNIT_CODEMM = "AD_UNIT_CODENM"
        const val STR_PUT_AD_CATEGORY_MIDNM = "AD_CATEGORY_MIDNM"
        const val STR_PUT_AD_CATEGORY_SCLSNM = "AD_CATEGORY_SCLSNM"
        const val STR_PUT_AD_AREA_MIDNM = "AD_AREA_MIDNM"
        const val STR_PUT_AD_AREA_SCLSNM = "AD_AREA_SCLSNM"
    }

    interface OnGetInfoData {
        fun onGetInfoData(arrData: ArrayList<String>, arrCategory: String)
    }

    interface OnCategorySelectedListener {
        fun onCategorySelected(code: String)
    }

    interface OnCitySelectedListener {
        fun onCitySelected(code: String)
    }

    private var mGetInfoData: OnGetInfoData? = null
    private var categorySelectedListener: OnCategorySelectedListener? = null
    private var citySelectedListener: OnCitySelectedListener? = null

    private val cal = Calendar.getInstance()
    private val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val etName: TextInputEditText
    private val etQuantity: TextInputEditText
    private val etAmount: TextInputEditText
    private val etDesiredShippingDate: TextInputEditText
    private val etDetail: TextInputEditText

    private val dropdownUnit: AutoCompleteTextView
    private val dropdownCategory: AutoCompleteTextView
    private val dropdownSubCategory: AutoCompleteTextView
    private val dropdownCity: AutoCompleteTextView
    private val dropdownDistrict: AutoCompleteTextView

    private val btnNext: Button

    private var unitList: List<TxtListDataInfo> = emptyList()
    private var categoryList: List<TxtListDataInfo> = emptyList()
    private var subCategoryList: List<TxtListDataInfo> = emptyList()
    private var cityList: List<TxtListDataInfo> = emptyList()
    private var districtList: List<TxtListDataInfo> = emptyList()

    private var categoryCode: String = ""
    private var categoryName: String = ""
    private var subCategoryCode: String = ""
    private var subCategoryName: String = ""

    private var cityCode: String = ""
    private var cityName: String = ""
    private var districtCode: String = ""
    private var districtName: String = ""

    private var unitCode: String = ""
    private var unitName: String = ""

    // ✅ 수정 진입 pending
    private var pendingCategoryCode: String = ""
    private var pendingSubCategoryCode: String = ""
    private var pendingCityCode: String = ""
    private var pendingDistrictCode: String = ""
    private var pendingUnitCode: String = ""

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.advertiser_make_ad_detail, this, true)

        etName = findViewById(R.id.et_input_ad_name)
        etQuantity = findViewById(R.id.et_quantity)
        etAmount = findViewById(R.id.et_input_ad_amount)
        etDesiredShippingDate = findViewById(R.id.et_desired_shipping_date)
        etDetail = findViewById(R.id.et_input_ad_detail)

        dropdownUnit = findViewById(R.id.dropdown_unit)
        dropdownCategory = findViewById(R.id.dropdown_category)
        dropdownSubCategory = findViewById(R.id.dropdown_subcategory)
        dropdownCity = findViewById(R.id.dropdown_city)
        dropdownDistrict = findViewById(R.id.dropdown_district)

        btnNext = findViewById(R.id.btn_make_ad_detail_next)
        btnNext.setOnClickListener(this)

        etDesiredShippingDate.isFocusable = false
        etDesiredShippingDate.isClickable = true
        etDesiredShippingDate.setOnClickListener { openDatePicker() }

        bindDropdownBehaviors()
        disableSubCategory()
        disableDistrict()
    }

    fun getOnInfoData(getInfoData: OnGetInfoData) {
        mGetInfoData = getInfoData
    }

    fun setOnCategorySelectedListener(listener: OnCategorySelectedListener) {
        categorySelectedListener = listener
    }

    fun setOnCitySelectedListener(listener: OnCitySelectedListener) {
        citySelectedListener = listener
    }

    // -------------------------
    // Public setters
    // -------------------------
    fun setUnitList(codeList: List<TxtListDataInfo>) {
        unitList = codeList

        bindAutoComplete(dropdownUnit, unitList) { selected ->
            unitCode = selected.getStrIdx()
            unitName = selected.getStrMsg()
        }

        applySelectionIfPending(dropdownUnit, unitList, pendingUnitCode) {
            unitCode = it.getStrIdx()
            unitName = it.getStrMsg()
        }
        pendingUnitCode = ""
    }

    fun setCategoryList(codeList: List<TxtListDataInfo>) {
        categoryList = codeList

        bindAutoComplete(dropdownCategory, categoryList) { selected ->
            categoryCode = selected.getStrIdx()
            categoryName = selected.getStrMsg()

            // 사용자가 바꾸면 sub 초기화
            resetSubCategory(clearPending = true)

            categorySelectedListener?.onCategorySelected(categoryCode)
        }

        // ✅ 수정 진입 자동 선택 + sub 로딩 트리거
        applySelectionIfPending(dropdownCategory, categoryList, pendingCategoryCode) {
            categoryCode = it.getStrIdx()
            categoryName = it.getStrMsg()
            categorySelectedListener?.onCategorySelected(categoryCode) // ✅ 중요
        }
        pendingCategoryCode = ""
    }

    fun setSubCategoryList(subCodeList: List<TxtListDataInfo>) {
        subCategoryList = subCodeList

        bindAutoComplete(dropdownSubCategory, subCategoryList) { selected ->
            subCategoryCode = selected.getStrIdx()
            subCategoryName = selected.getStrMsg()
        }
        enableSubCategory()

        applySelectionIfPending(dropdownSubCategory, subCategoryList, pendingSubCategoryCode) {
            subCategoryCode = it.getStrIdx()
            subCategoryName = it.getStrMsg()
        }
        pendingSubCategoryCode = ""
    }

    fun setCityList(codeList: List<TxtListDataInfo>) {
        cityList = codeList

        bindAutoComplete(dropdownCity, cityList) { selected ->
            cityCode = selected.getStrIdx()
            cityName = selected.getStrMsg()

            // 사용자가 바꾸면 district 초기화
            resetDistrict(clearPending = true)

            citySelectedListener?.onCitySelected(cityCode)
        }

        // ✅ 수정 진입 자동 선택 + district 로딩 트리거
        applySelectionIfPending(dropdownCity, cityList, pendingCityCode) {
            cityCode = it.getStrIdx()
            cityName = it.getStrMsg()
            citySelectedListener?.onCitySelected(cityCode) // ✅ 중요
        }
        pendingCityCode = ""
    }

    fun setDistrictList(list: List<TxtListDataInfo>) {
        districtList = list

        bindAutoComplete(dropdownDistrict, districtList) { selected ->
            districtCode = selected.getStrIdx()
            districtName = selected.getStrMsg()
        }
        enableDistrict()

        applySelectionIfPending(dropdownDistrict, districtList, pendingDistrictCode) {
            districtCode = it.getStrIdx()
            districtName = it.getStrMsg()
        }
        pendingDistrictCode = ""
    }

    /**
     * ✅ 수정 모드 데이터 세팅
     * - 여기서 category/city listener를 “원본처럼” 강제로 호출해서
     *   sub/district 리스트를 다시 받아오게 한다.
     */
    fun modifyData(data: KtModifyADInfo) {
        etName.setText(data.title.orEmpty())
        etQuantity.setText(data.quantity.orEmpty())
        etAmount.setText(data.price.orEmpty())
        etDesiredShippingDate.setText(data.desiredShippingDate.orEmpty())
        etDetail.setText(data.description.orEmpty())

        pendingCategoryCode = data.categoryMid.orEmpty()
        pendingSubCategoryCode = data.categoryScls.orEmpty()
        pendingCityCode = data.areaMid.orEmpty()
        pendingDistrictCode = data.areaScls.orEmpty()
        pendingUnitCode = data.unitCode.orEmpty()

        // unit
        if (unitList.isNotEmpty()) {
            applySelectionIfPending(dropdownUnit, unitList, pendingUnitCode) {
                unitCode = it.getStrIdx()
                unitName = it.getStrMsg()
            }
            pendingUnitCode = ""
        }

        // category (리스트가 있으면 선택, 없으면 pending만 유지)
        if (categoryList.isNotEmpty()) {
            applySelectionIfPending(dropdownCategory, categoryList, pendingCategoryCode) {
                categoryCode = it.getStrIdx()
                categoryName = it.getStrMsg()
            }
            pendingCategoryCode = ""
        } else {
            // 리스트가 아직이면 코드만 잡아두고
            categoryCode = pendingCategoryCode
        }

        // city
        if (cityList.isNotEmpty()) {
            applySelectionIfPending(dropdownCity, cityList, pendingCityCode) {
                cityCode = it.getStrIdx()
                cityName = it.getStrMsg()
            }
            pendingCityCode = ""
        } else {
            cityCode = pendingCityCode
        }

        // ✅✅✅ 핵심: 원본처럼 “서브 리스트 로딩” 트리거
        if (categoryCode.isNotBlank()) {
            categorySelectedListener?.onCategorySelected(categoryCode)
        }
        if (cityCode.isNotBlank()) {
            citySelectedListener?.onCitySelected(cityCode)
        }
        // sub/district는 리스트가 내려오면 setSubCategoryList/setDistrictList에서 pending으로 자동선택됨
    }

    // -------------------------
    // Click
    // -------------------------
    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_make_ad_detail_next) {
            if (!setInfoData()) return

            val arr = ArrayList<String>(15).apply {
                add(etName.text?.toString()?.trim().orEmpty())                 // 0
                add(etQuantity.text?.toString()?.trim().orEmpty())             // 1
                add(unitCode)                                                 // 2
                add(etAmount.text?.toString()?.trim().orEmpty())               // 3
                add(etDesiredShippingDate.text?.toString()?.trim().orEmpty())  // 4
                add(etDetail.text?.toString()?.trim().orEmpty())               // 5
                add(categoryCode)                                             // 6
                add(subCategoryCode)                                          // 7
                add(cityCode)                                                 // 8
                add(districtCode)                                             // 9
                add(unitName)                                                 // 10
                add(categoryName)                                             // 11
                add(subCategoryName)                                          // 12
                add(cityName)                                                 // 13
                add(districtName)                                             // 14
            }
            mGetInfoData?.onGetInfoData(arr, categoryCode)
        }
    }

    private fun setInfoData(): Boolean {
        val strInputAmount = etAmount.text?.toString().orEmpty()
        val strInputQuantity = etQuantity.text?.toString().orEmpty()

        val msg = when {
            etName.text?.toString().orEmpty().isBlank() ->
                resources.getString(R.string.str_make_ad_name_err)

            strInputQuantity.trim().isEmpty() || strInputQuantity == "0" ->
                resources.getString(R.string.str_make_ad_quantity_err)

            unitCode.isBlank() ->
                resources.getString(R.string.str_make_ad_unit_err)

            strInputAmount.trim().isEmpty() || strInputAmount == "0" ->
                String.format(resources.getString(R.string.str_make_ad_amount_reinput), strInputAmount)

            etDesiredShippingDate.text?.toString().orEmpty().isBlank() ->
                resources.getString(R.string.str_make_ad_desired_shipping_date_err)

            etDetail.text?.toString().orEmpty().isBlank() ->
                resources.getString(R.string.str_make_ad_detail_err)

            categoryCode.isBlank() ->
                resources.getString(R.string.str_make_ad_category_err)

            subCategoryCode.isBlank() ->
                resources.getString(R.string.str_make_ad_subcategory_err)

            cityCode.isBlank() ->
                resources.getString(R.string.str_make_ad_cityt_err)

            districtCode.isBlank() ->
                resources.getString(R.string.str_make_ad_district_err)

            else -> ""
        }

        if (msg.isNotBlank()) {
            context.startActivity(Intent(context, DlgBtnActivity::class.java).apply {
                putExtra("BtnDlgMsg", msg)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
            return false
        }
        return true
    }

    // -------------------------
    // Internals
    // -------------------------
    private fun openDatePicker() {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            { _, year, month, day ->
                cal.set(year, month, day)
                etDesiredShippingDate.setText(df.format(cal.time))
            },
            y, m, d
        ).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun bindDropdownBehaviors() {
        listOf(dropdownUnit, dropdownCategory, dropdownSubCategory, dropdownCity, dropdownDistrict).forEach { v ->
            v.setOnClickListener { v.showDropDown() }
            v.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) v.showDropDown() }
            v.threshold = 0
        }
    }

    private fun bindAutoComplete(
        view: AutoCompleteTextView,
        list: List<TxtListDataInfo>,
        onUserSelected: (TxtListDataInfo) -> Unit
    ) {
        val names = list.map { it.getStrMsg() }
        val adapter = ArrayAdapter(context, R.layout.list_txt_item, names)
        view.setAdapter(adapter)

        view.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selected = list.getOrNull(position) ?: return@OnItemClickListener
            view.setText(selected.getStrMsg(), false)
            onUserSelected(selected)
        }
    }

    private fun applySelectionIfPending(
        dropdown: AutoCompleteTextView,
        list: List<TxtListDataInfo>,
        pendingCode: String?,
        onSelected: (TxtListDataInfo) -> Unit
    ) {
        if (pendingCode.isNullOrBlank()) return
        val matched = list.firstOrNull { it.getStrIdx() == pendingCode } ?: return
        dropdown.setText(matched.getStrMsg(), false)
        onSelected(matched)
    }

    private fun resetSubCategory(clearPending: Boolean) {
        subCategoryList = emptyList()
        subCategoryCode = ""
        subCategoryName = ""
        if (clearPending) pendingSubCategoryCode = ""

        dropdownSubCategory.setText("", false)
        dropdownSubCategory.setAdapter(null)
        disableSubCategory()
    }

    private fun resetDistrict(clearPending: Boolean) {
        districtList = emptyList()
        districtCode = ""
        districtName = ""
        if (clearPending) pendingDistrictCode = ""

        dropdownDistrict.setText("", false)
        dropdownDistrict.setAdapter(null)
        disableDistrict()
    }

    private fun disableSubCategory() { dropdownSubCategory.isEnabled = false }
    private fun enableSubCategory() { dropdownSubCategory.isEnabled = true }
    private fun disableDistrict() { dropdownDistrict.isEnabled = false }
    private fun enableDistrict() { dropdownDistrict.isEnabled = true }
}
