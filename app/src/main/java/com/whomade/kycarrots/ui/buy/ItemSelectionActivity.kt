package com.whomade.kycarrots.ui.buy

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.RangeSlider
import com.whomade.kycarrots.R
import com.whomade.kycarrots.ui.common.TokenUtil
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.launch

class ItemSelectionActivity : AppCompatActivity() {

    // ViewModel 은 목록만 담당, 카테고리/세부항목은 Activity 에서 직접 AppService 호출
    private val viewModel: AdListViewModel by viewModels {
        AdListViewModelFactory(TokenUtil.getToken(this))
    }

    // AppService를 전역으로 한 번만 생성
    private val appService by lazy { AppServiceProvider.getService() }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdActivityAdapter
    private lateinit var llProgress: View

    private lateinit var btnRefresh: ImageButton
    private lateinit var btnInquiry: Button

    private lateinit var sliderPrice: RangeSlider
    private lateinit var tvSelectedRange: TextView

    private lateinit var dropdownCategory: AutoCompleteTextView
    private lateinit var dropdownSubcategory: AutoCompleteTextView
    private lateinit var dropdownCity: AutoCompleteTextView
    private lateinit var dropdownDistrict: AutoCompleteTextView

    private var categoryList    = listOf<TxtListDataInfo>()
    private var subcategoryList = listOf<TxtListDataInfo>()
    private var cityList = listOf<TxtListDataInfo>()
    private var districtList = listOf<TxtListDataInfo>()

    // 현재 선택된 adCode/pageNo 저장
    private var currentAdCode = 1
    private var currentPage   = 1

    var selectedCategoryGroup = "R010610"
    var selectedCategoryMid   = "ALL"
    var selectedCategoryScls  = "ALL"

    var selectedAreaGroup = "R010070"
    var selectedAreaMid   = "ALL"
    var selectedAreaScls  = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_selection)

        // 1. 뷰 바인딩
        btnRefresh          = findViewById(R.id.btn_refresh)
        btnInquiry          = findViewById(R.id.btn_inquiry)
        sliderPrice         = findViewById(R.id.slider_price)
        tvSelectedRange     = findViewById(R.id.selected_range)
        recyclerView        = findViewById(R.id.recyclerView)
        dropdownCategory    = findViewById(R.id.dropdown_category)
        dropdownSubcategory = findViewById(R.id.dropdown_subcategory)
        dropdownCity        = findViewById(R.id.dropdown_city)
        dropdownDistrict    = findViewById(R.id.dropdown_district)
        llProgress          = findViewById(R.id.ll_progress_circle)

        llProgress.visibility = View.GONE

        val layoutManager = LinearLayoutManager(this)
        adapter = AdActivityAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 2. 툴바 새로고침
        btnRefresh.setOnClickListener {
            viewModel.loadItems(currentAdCode, currentPage)
        }

        // 4. 가격 슬라이더
        sliderPrice.addOnChangeListener { slider, _, _ ->
            val (min, max) = slider.values
            viewModel.setPriceRange(min, max)
        }

        // 5. 문의하기 버튼
        btnInquiry.setOnClickListener {
            Toast.makeText(this, "문의하기 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }

        // 6. LiveData 관찰
        viewModel.priceRange.observe(this) { (min, max) ->
            val fmtMin = "%,d".format(min.toInt())
            val fmtMax = "%,d".format(max.toInt())
            tvSelectedRange.text = "$fmtMin ~ $fmtMax 원"
        }
        viewModel.items.observe(this) { list ->
            adapter.updateList(list)
        }
        viewModel.isProgressLoading.observe(this) { isLoading ->
            llProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        // 무한 스크롤 리스너
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return  // 위로 스크롤/정지일 땐 무시

                val lm    = rv.layoutManager as LinearLayoutManager
                val total = lm.itemCount
                val last  = lm.findLastVisibleItemPosition()
                Log.d("Paging", "last=$last, total=$total, isLoading=${viewModel.isLoading}")
                // 맨 끝 아이템 보일 때, 로딩 중 아니고 아직 끝에 도달하지 않았다면
                if (last == total - 1 && !viewModel.isLoading && !viewModel.endReached) {
                    llProgress.visibility = View.VISIBLE
                    viewModel.loadNextPage(currentAdCode)
                }
            }
        })

        // 7. 카테고리/세부항목 드롭다운 초기화
        loadCategories()
        loadCities()

        // 8. 드롭다운 선택 리스너
        dropdownCategory.setOnItemClickListener { _, _, pos, _ ->
            val selectedCode = categoryList[pos].strIdx
            selectedCategoryMid = selectedCode
            if (selectedCode == "ALL") {
                // "전체" 만 남기기
                val allEntry = TxtListDataInfo().apply {
                    strIdx = "ALL"
                    strMsg = "전체"
                }
                subcategoryList = listOf(allEntry)

                // 어댑터에도 "전체" 하나만 세팅
                dropdownSubcategory.setAdapter(
                    ArrayAdapter(
                        this@ItemSelectionActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        listOf(allEntry.strMsg)
                    )
                )
                // 텍스트에 바로 보여주기
                dropdownSubcategory.setText(allEntry.strMsg, false)

            } else {
                // 일반적인 소분류 로딩
                loadSubcategories(selectedCode)
            }
            applyFiltersAndReload()
        }


        dropdownSubcategory.setOnItemClickListener { _, _, pos, _ ->
            // 선택된 세부항목이 있으면 필터 재적용
            // 예: viewModel.loadItems(...) 혹은 다른 로직
            Toast.makeText(
                this,
                "선택: ${subcategoryList[pos].strMsg}",
                Toast.LENGTH_SHORT
            ).show()
            val code = subcategoryList[pos].strIdx
            selectedCategoryScls = code
            applyFiltersAndReload()
        }

        // 도시 선택 시 시·구 재로딩
        dropdownCity.setOnItemClickListener { _, _, pos, _ ->
            val code = cityList[pos].strIdx
            selectedAreaMid = code
            loadDistricts(code)
            applyFiltersAndReload()
        }

        dropdownDistrict.setOnItemClickListener { _, _, pos, _ ->
            val code = districtList[pos].strIdx
            selectedAreaScls = code
            applyFiltersAndReload()
        }

        // (4) 조회하기 버튼 클릭 → 필터 적용 후 API 재호출
        btnInquiry.setOnClickListener {
            // 1) ViewModel에 필터 세팅
            viewModel.setCategoryFilter(
                selectedCategoryGroup,
                selectedCategoryMid,
                selectedCategoryScls
            )
            viewModel.setAreaFilter(
                selectedAreaGroup,
                selectedAreaMid,
                selectedAreaScls
            )
            onInquiryClicked()
        }
        // 9. 최초 데이터 로드
        // 최초 데이터 로드 (1페이지만)
        viewModel.resetAndLoad(currentAdCode)

    }

    /** AppServiceProvider 로부터 AppService 를 얻어 대분류 리스트를 suspend 호출 */
    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                // 실제 API 호출로 받아온 리스트
                val apiList = appService.getCodeList("R010610")
                // 맨 앞에 "전체" 추가
                val allEntry = TxtListDataInfo().apply {
                    strIdx = "ALL"
                    strMsg = "전체"
                }
                categoryList = listOf(allEntry) + apiList

                val names = categoryList.map { it.strMsg }
                dropdownCategory.setAdapter(
                    ArrayAdapter(
                        this@ItemSelectionActivity,
                        R.layout.list_txt_item,
                        names
                    )
                )
                // 5) 디폴트로 "전체" 표시
                dropdownCategory.setText(allEntry.strMsg, false)

                // 6) 세부항목도 초기화
                loadSubcategories("ALL")
            } catch (e: Exception) {
                Toast.makeText(this@ItemSelectionActivity,
                    "카테고리 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSubcategories(midCode: String) {
        lifecycleScope.launch {
            try {
                val allEntry = TxtListDataInfo().apply {
                    strIdx = "ALL"
                    strMsg = "전체"
                }

                // midCode == "ALL" 이면, API 호출 없이 "전체" 하나만
                if (midCode == "ALL") {
                    subcategoryList = listOf(allEntry)
                } else {
                    // 실제 API 호출
                    val apiSubList = appService.getSCodeList("R010610", midCode)
                    subcategoryList = listOf(allEntry) + apiSubList
                }

                // 어댑터 세팅
                val names = subcategoryList.map { it.strMsg }
                dropdownSubcategory.setAdapter(
                    ArrayAdapter(
                        this@ItemSelectionActivity,
                        R.layout.list_txt_item,
                        names
                    )
                )

                // 기본값 "전체" 표시
                dropdownSubcategory.setText(allEntry.strMsg, false)

            } catch (e: Exception) {
                Toast.makeText(
                    this@ItemSelectionActivity,
                    "세부항목 로드 실패",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /** AppServiceProvider 로부터 AppService 를 얻어 도시(대분류) 리스트를 suspend 호출 */
    private fun loadCities() {
        lifecycleScope.launch {
            try {
                // 실제 API 호출로 받아온 도시 리스트 (R010070)
                val apiList = appService.getCodeList("R010070")
                // 맨 앞에 "전체" 추가
                val allEntry = TxtListDataInfo().apply {
                    strIdx = "ALL"
                    strMsg = "전체"
                }
                cityList = listOf(allEntry) + apiList

                val names = cityList.map { it.strMsg }
                dropdownCity.setAdapter(
                    ArrayAdapter(
                        this@ItemSelectionActivity,
                        R.layout.list_txt_item,
                        names
                    )
                )
                // 디폴트로 "전체" 표시
                dropdownCity.setText(allEntry.strMsg, false)

                // 시·구도 초기화
                loadDistricts("ALL")
            } catch (e: Exception) {
                Toast.makeText(
                    this@ItemSelectionActivity,
                    "도시 로드 실패",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadDistricts(cityCode: String) {
        lifecycleScope.launch {
            try {
                // "전체" 항목
                val allEntry = TxtListDataInfo().apply {
                    strIdx = "ALL"
                    strMsg = "전체"
                }

                districtList = if (cityCode == "ALL") {
                    // API 호출 없이 "전체" 만
                    listOf(allEntry)
                } else {
                    // 실제 API 호출 (R010070 의 하위코드)
                    val apiSubList = appService.getSCodeList("R010070", cityCode)
                    listOf(allEntry) + apiSubList
                }

                // 어댑터 세팅
                val names = districtList.map { it.strMsg }
                dropdownDistrict.setAdapter(
                    ArrayAdapter(
                        this@ItemSelectionActivity,
                        R.layout.list_txt_item,
                        names
                    )
                )
                // 기본값 "전체" 표시
                dropdownDistrict.setText(allEntry.strMsg, false)

            } catch (e: Exception) {
                Toast.makeText(
                    this@ItemSelectionActivity,
                    "시·구 로드 실패",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun applyFiltersAndReload() {
        // ViewModel 쪽에 필터 상태를 모두 보관하게 세팅
        viewModel.setCategoryFilter(
            selectedCategoryGroup,
            selectedCategoryMid,
            selectedCategoryScls
        )
        viewModel.setAreaFilter(
            selectedAreaGroup,
            selectedAreaMid,
            selectedAreaScls
        )
        // 마지막으로 리스트 다시 불러오기
        viewModel.loadItems(
            adCode  = currentAdCode,
            pageNo  = currentPage
        )
    }

    // 조회하기 버튼 클릭 시: 페이징도 리셋
    private fun onInquiryClicked() {
        viewModel.resetPaging()
        viewModel.loadNextPage(currentAdCode)
    }
}
