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
import com.whomade.kycarrots.dialog.DlgBtnActivity
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * XML(advertiser_make_ad_detail.xml) 기준으로 구현한 Custom View
 * - dropdown_* 는 모두 AutoCompleteTextView (Spinner 아님)
 */
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

    // Views (XML 그대로)
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

    // Data lists
    private var unitList: List<TxtListDataInfo> = emptyList()
    private var categoryList: List<TxtListDataInfo> = emptyList()
    private var subCategoryList: List<TxtListDataInfo> = emptyList()
    private var cityList: List<TxtListDataInfo> = emptyList()
    private var districtList: List<TxtListDataInfo> = emptyList()

    // Selected (code + name)
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

    // modify mode pending codes (리스트 로드 전에 들어올 수 있음)
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

        // date picker
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
    // Public setters (MainActivity에서 내려줌)
    // -------------------------
    fun setUnitList(codeList: List<TxtListDataInfo>) {
        unitList = codeList
        bindAutoComplete(dropdownUnit, unitList) { selected ->
            unitCode = selected.getStrIdx()
            unitName = selected.getStrMsg()
        }
        // modify pending apply
        applySelectionIfPending(dropdownUnit, unitList, pendingUnitCode) {
            unitCode = it.getStrIdx()
            unitName = it.getStrMsg()
        }
    }

    fun setCategoryList(codeList: List<TxtListDataInfo>) {
        categoryList = codeList
        bindAutoComplete(dropdownCategory, categoryList) { selected ->
            categoryCode = selected.getStrIdx()
            categoryName = selected.getStrMsg()

            // 카테고리 바뀌면 세부항목 초기화
            resetSubCategory()

            categorySelectedListener?.onCategorySelected(categoryCode)
        }
        applySelectionIfPending(dropdownCategory, categoryList, pendingCategoryCode) {
            categoryCode = it.getStrIdx()
            categoryName = it.getStrMsg()
            categorySelectedListener?.onCategorySelected(categoryCode)
        }
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
    }

    fun setCityList(codeList: List<TxtListDataInfo>) {
        cityList = codeList
        bindAutoComplete(dropdownCity, cityList) { selected ->
            cityCode = selected.getStrIdx()
            cityName = selected.getStrMsg()

            // 도시 바뀌면 시/구 초기화
            resetDistrict()

            citySelectedListener?.onCitySelected(cityCode)
        }
        applySelectionIfPending(dropdownCity, cityList, pendingCityCode) {
            cityCode = it.getStrIdx()
            cityName = it.getStrMsg()
            citySelectedListener?.onCitySelected(cityCode)
        }
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
    }

    /**
     * 수정 모드 데이터 세팅 (리스트 로드 전/후 모두 안전)
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

        // 이미 리스트가 로드돼있으면 즉시 반영
        if (categoryList.isNotEmpty()) {
            applySelectionIfPending(dropdownCategory, categoryList, pendingCategoryCode) {
                categoryCode = it.getStrIdx()
                categoryName = it.getStrMsg()
                categorySelectedListener?.onCategorySelected(categoryCode)
            }
        }
        if (cityList.isNotEmpty()) {
            applySelectionIfPending(dropdownCity, cityList, pendingCityCode) {
                cityCode = it.getStrIdx()
                cityName = it.getStrMsg()
                citySelectedListener?.onCitySelected(cityCode)
            }
        }
        if (unitList.isNotEmpty()) {
            applySelectionIfPending(dropdownUnit, unitList, pendingUnitCode) {
                unitCode = it.getStrIdx()
                unitName = it.getStrMsg()
            }
        }
        // subCategory/district 는 보통 리스너 호출 후(MainActivity에서) 리스트를 내려주므로
        // setSubCategoryList / setDistrictList 에서 pending 값 반영됨
    }

    // -------------------------
    // Click
    // -------------------------
    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_make_ad_detail_next) {
            if (!setInfoData()) return

            val arr = ArrayList<String>(15).apply {
                add(etName.text?.toString()?.trim().orEmpty())                 // 0 광고명
                add(etQuantity.text?.toString()?.trim().orEmpty())             // 1 수량
                add(unitCode)                                                 // 2 단위 코드
                add(etAmount.text?.toString()?.trim().orEmpty())               // 3 금액
                add(etDesiredShippingDate.text?.toString()?.trim().orEmpty())  // 4 출하일
                add(etDetail.text?.toString()?.trim().orEmpty())               // 5 상세
                add(categoryCode)                                             // 6 카테고리(mid)
                add(subCategoryCode)                                          // 7 카테고리(scls)
                add(cityCode)                                                 // 8 지역(mid)
                add(districtCode)                                             // 9 지역(scls)
                add(unitName)                                                 // 10 단위명
                add(categoryName)                                             // 11 카테고리명(mid)
                add(subCategoryName)                                          // 12 카테고리명(scls)
                add(cityName)                                                 // 13 지역명(mid)
                add(districtName)                                             // 14 지역명(scls)
            }
            mGetInfoData?.onGetInfoData(arr, categoryCode)
        }
    }
    private fun setInfoData(): Boolean {
        var strMsg = ""

        val strInputAmount = etAmount.text?.toString().orEmpty()
        val strInputQuantity = etQuantity.text?.toString().orEmpty()

        strMsg = when {
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

        if (strMsg.isNotBlank()) {
            context.startActivity(Intent(context, DlgBtnActivity::class.java).apply {
                putExtra("BtnDlgMsg", strMsg)
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
        // 클릭하면 드롭다운 열리게 (Material dropdown 느낌)
        listOf(dropdownUnit, dropdownCategory, dropdownSubCategory, dropdownCity, dropdownDistrict).forEach { v ->
            v.setOnClickListener { v.showDropDown() }
            v.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) v.showDropDown() }
            v.threshold = 0
        }
    }

    private fun bindAutoComplete(
        view: AutoCompleteTextView,
        list: List<TxtListDataInfo>,
        onSelected: (TxtListDataInfo) -> Unit
    ) {
        val names = list.map { it.getStrMsg() }
        val adapter = ArrayAdapter(context, R.layout.list_txt_item, names)
        view.setAdapter(adapter)

        view.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selected = list.getOrNull(position) ?: return@OnItemClickListener
            // 표시 텍스트는 선택된 값으로 고정
            view.setText(selected.getStrMsg(), false)
            onSelected(selected)
        }
    }

    private fun applySelectionIfPending(
        view: AutoCompleteTextView,
        list: List<TxtListDataInfo>,
        pendingCode: String,
        onSelected: (TxtListDataInfo) -> Unit
    ) {
        if (pendingCode.isBlank() || list.isEmpty()) return
        val found = list.firstOrNull { it.getStrIdx() == pendingCode } ?: return
        view.setText(found.getStrMsg(), false)
        onSelected(found)
    }

    private fun resetSubCategory() {
        subCategoryList = emptyList()
        subCategoryCode = ""
        subCategoryName = ""
        pendingSubCategoryCode = ""

        dropdownSubCategory.setText("", false)
        dropdownSubCategory.setAdapter(null)
        disableSubCategory()
    }

    private fun resetDistrict() {
        districtList = emptyList()
        districtCode = ""
        districtName = ""
        pendingDistrictCode = ""

        dropdownDistrict.setText("", false)
        dropdownDistrict.setAdapter(null)
        disableDistrict()
    }

    private fun disableSubCategory() {
        dropdownSubCategory.isEnabled = false
    }

    private fun enableSubCategory() {
        dropdownSubCategory.isEnabled = true
    }

    private fun disableDistrict() {
        dropdownDistrict.isEnabled = false
    }

    private fun enableDistrict() {
        dropdownDistrict.isEnabled = true
    }
}
