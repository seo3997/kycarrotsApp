package com.whomade.kycarrots.ui.buy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.slider.RangeSlider
import com.whomade.kycarrots.R
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.Noti.NotificationListActivity
import com.whomade.kycarrots.ui.common.TokenUtil
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    // ViewModel (토큰은 fragment context 사용)
    private val viewModel: AdListViewModel by viewModels {
        AdListViewModelFactory(TokenUtil.getToken(requireContext()))
    }

    // Service
    private val appService by lazy { AppServiceProvider.getService() }

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdActivityAdapter
    private lateinit var llProgress: View
    private lateinit var btnInquiry: Button
    private lateinit var sliderPrice: RangeSlider
    private lateinit var tvSelectedRange: TextView
    private lateinit var dropdownCategory: AutoCompleteTextView
    private lateinit var dropdownSubcategory: AutoCompleteTextView
    private lateinit var dropdownCity: AutoCompleteTextView
    private lateinit var dropdownDistrict: AutoCompleteTextView
    private lateinit var emptyTextView: TextView
    private lateinit var checkboxSaleOnly: CheckBox

    // Data
    private var categoryList    = listOf<TxtListDataInfo>()
    private var subcategoryList = listOf<TxtListDataInfo>()
    private var cityList        = listOf<TxtListDataInfo>()
    private var districtList    = listOf<TxtListDataInfo>()

    private var currentAdCode = 1
    private var currentPage   = 1

    var selectedCategoryGroup = "R010610"
    var selectedCategoryMid   = "ALL"
    var selectedCategoryScls  = "ALL"

    var selectedAreaGroup = "R010070"
    var selectedAreaMid   = "ALL"
    var selectedAreaScls  = "ALL"

    private var badge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // 툴바 메뉴 사용(알림 배지)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 뷰 바인딩
        recyclerView        = view.findViewById(R.id.recyclerView)
        btnInquiry          = view.findViewById(R.id.btn_inquiry)
        sliderPrice         = view.findViewById(R.id.slider_price)
        tvSelectedRange     = view.findViewById(R.id.selected_range)
        dropdownCategory    = view.findViewById(R.id.dropdown_category)
        dropdownSubcategory = view.findViewById(R.id.dropdown_subcategory)
        dropdownCity        = view.findViewById(R.id.dropdown_city)
        dropdownDistrict    = view.findViewById(R.id.dropdown_district)
        llProgress          = view.findViewById(R.id.ll_progress_circle)
        llProgress.visibility = View.GONE
        emptyTextView       = view.findViewById(R.id.emptyTextView)
        checkboxSaleOnly    = view.findViewById(R.id.checkbox_sale_only)

        // 2) 리스트/어댑터 (어댑터가 Activity 타입을 요구 → requireActivity())
        adapter = AdActivityAdapter(requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 3) 슬라이더
        sliderPrice.addOnChangeListener { slider, _, _ ->
            val (min, max) = slider.values
            viewModel.setPriceRange(min, max)
        }

        // 4) 희망 단가 토글
        val checkBox = view.findViewById<CheckBox>(R.id.checkbox_price)
        val priceLayout = view.findViewById<View>(R.id.layout_price_range)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            priceLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            sliderPrice.setValues(sliderPrice.valueFrom, sliderPrice.valueTo)
        }

        viewModel.setSaleOnly(if (checkboxSaleOnly.isChecked) "1" else "0")
        checkboxSaleOnly.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSaleOnly(if (isChecked) "1" else "0")
            applyFiltersAndReload()    // ✅ 요청하신 대로 이 함수 호출
        }
        // 5) 조회하기 버튼
        btnInquiry.setOnClickListener {
            viewModel.setCategoryFilter(selectedCategoryGroup, selectedCategoryMid, selectedCategoryScls)
            viewModel.setAreaFilter(selectedAreaGroup, selectedAreaMid, selectedAreaScls)
            viewModel.resetPaging()
            viewModel.loadNextPage()
        }

        // 6) LiveData
        viewModel.priceRange.observe(viewLifecycleOwner) { (min, max) ->
            val fmtMin = "%,d".format(min.toInt())
            val fmtMax = "%,d".format(max.toInt())
            tvSelectedRange.text = "$fmtMin ~ $fmtMax 원"
        }
        viewModel.items.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
            emptyTextView.visibility = if (list.isNullOrEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility  = if (list.isNullOrEmpty()) View.GONE   else View.VISIBLE
        }
        viewModel.isProgressLoading.observe(viewLifecycleOwner) { isLoading ->
            llProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 7) 무한 스크롤
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm    = rv.layoutManager as LinearLayoutManager
                val total = lm.itemCount
                val last  = lm.findLastVisibleItemPosition()
                Log.d("Paging", "last=$last, total=$total, isLoading=${viewModel.isLoading}")
                if (last == total - 1 && !viewModel.isLoading && !viewModel.endReached) {
                    llProgress.visibility = View.VISIBLE
                    viewModel.loadNextPage()
                }
            }
        })

        // 8) 드롭다운 초기화 + 리스너
        loadCategories()
        loadCities()

        dropdownCategory.setOnItemClickListener { _, _, pos, _ ->
            val code = categoryList[pos].strIdx
            selectedCategoryMid = code
            if (code == "ALL") {
                val allEntry = TxtListDataInfo().apply { strIdx = "ALL"; strMsg = "전체" }
                subcategoryList = listOf(allEntry)
                dropdownSubcategory.setAdapter(
                    ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf(allEntry.strMsg))
                )
                dropdownSubcategory.setText(allEntry.strMsg, false)
            } else {
                loadSubcategories(code)
            }
            applyFiltersAndReload()
        }

        dropdownSubcategory.setOnItemClickListener { _, _, pos, _ ->
            selectedCategoryScls = subcategoryList[pos].strIdx
            Toast.makeText(requireContext(), "선택: ${subcategoryList[pos].strMsg}", Toast.LENGTH_SHORT).show()
            applyFiltersAndReload()
        }

        dropdownCity.setOnItemClickListener { _, _, pos, _ ->
            selectedAreaMid = cityList[pos].strIdx
            loadDistricts(selectedAreaMid)
            applyFiltersAndReload()
        }

        dropdownDistrict.setOnItemClickListener { _, _, pos, _ ->
            selectedAreaScls = districtList[pos].strIdx
            applyFiltersAndReload()
        }

        // 9) 최초 로드
        viewModel.resetAndLoad(currentAdCode)
    }


    // === Data loads ===
    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiList = appService.getCodeList("R010610")
                val allEntry = TxtListDataInfo().apply { strIdx = "ALL"; strMsg = "전체" }
                categoryList = listOf(allEntry) + apiList

                dropdownCategory.setAdapter(
                    ArrayAdapter(requireContext(), R.layout.list_txt_item, categoryList.map { it.strMsg })
                )
                dropdownCategory.setText(allEntry.strMsg, false)

                loadSubcategories("ALL")
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "카테고리 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSubcategories(midCode: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val allEntry = TxtListDataInfo().apply { strIdx = "ALL"; strMsg = "전체" }
                subcategoryList = if (midCode == "ALL") {
                    listOf(allEntry)
                } else {
                    val apiSubList = appService.getSCodeList("R010610", midCode)
                    listOf(allEntry) + apiSubList
                }
                dropdownSubcategory.setAdapter(
                    ArrayAdapter(requireContext(), R.layout.list_txt_item, subcategoryList.map { it.strMsg })
                )
                dropdownSubcategory.setText(allEntry.strMsg, false)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "세부항목 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCities() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiList = appService.getCodeList("R010070")
                val allEntry = TxtListDataInfo().apply { strIdx = "ALL"; strMsg = "전체" }
                cityList = listOf(allEntry) + apiList

                dropdownCity.setAdapter(
                    ArrayAdapter(requireContext(), R.layout.list_txt_item, cityList.map { it.strMsg })
                )
                dropdownCity.setText(allEntry.strMsg, false)

                loadDistricts("ALL")
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "도시 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDistricts(cityCode: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val allEntry = TxtListDataInfo().apply { strIdx = "ALL"; strMsg = "전체" }
                districtList = if (cityCode == "ALL") {
                    listOf(allEntry)
                } else {
                    val apiSubList = appService.getSCodeList("R010070", cityCode)
                    listOf(allEntry) + apiSubList
                }
                dropdownDistrict.setAdapter(
                    ArrayAdapter(requireContext(), R.layout.list_txt_item, districtList.map { it.strMsg })
                )
                dropdownDistrict.setText(allEntry.strMsg, false)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "시·구 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFiltersAndReload() {
        viewModel.setCategoryFilter(selectedCategoryGroup, selectedCategoryMid, selectedCategoryScls)
        viewModel.setAreaFilter(selectedAreaGroup, selectedAreaMid, selectedAreaScls)
        viewModel.loadItems(adCode = currentAdCode, pageNo = currentPage)
    }
}
