package com.whomade.kycarrots.ui.buy


import android.util.Log
import androidx.lifecycle.*
import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.launch

class AdListViewModel(
    private val appService: AppService, // 싱글톤 주입
    private val initialToken: String
) : ViewModel() {
    internal var isLoading = false
        private set
    internal var endReached = false
        private set

    private val _items = MutableLiveData<List<AdItem>>(emptyList())
    val items: LiveData<List<AdItem>> = _items
    private val _priceRange = MutableLiveData(Pair(0f, 9990000f))
    val priceRange: LiveData<Pair<Float, Float>> = _priceRange
    val isProgressLoading = MutableLiveData(false)

    // **새로 추가**: 현재 페이지와 로딩 상태
    private var currentPage = 1

    // 추가: 필터 상태를 저장
    private var categoryGroup: String? = null
    private var categoryMid:   String? = null
    private var categoryScls:  String? = null
    private var areaGroup:     String? = null
    private var areaMid:       String? = null
    private var areaScls:      String? = null

    /**
     * 광고 목록을 불러올 때마다 필터를 함께 넘깁니다.
     *
     * @param adCode 광고 코드 (e.g. 품목)
     * @param pageNo 페이지 번호
     * @param categoryGroup 대분류 코드 or "ALL" or null
     * @param categoryMid   중분류 코드 or "ALL" or null
     * @param categoryScls  소분류 코드 or "ALL" or null
     * @param areaGroup     도시 대분류 코드 or "ALL" or null
     * @param areaMid       시/구 코드 or "ALL" or null
     * @param areaScls      동/읍/면 코드 or "ALL" or null
     */
    fun loadItems(
        adCode: Int   = 1,
        pageNo: Int   = 1,
        categoryGroup: String? = this.categoryGroup,
        categoryMid:   String? = this.categoryMid,
        categoryScls:  String? = this.categoryScls,
        areaGroup:     String? = this.areaGroup,
        areaMid:       String? = this.areaMid,
        areaScls:      String? = this.areaScls
    ) {
        isProgressLoading.value = true
        viewModelScope.launch {
            // priceRange LiveData 에서 값을 꺼내 Int? 으로 변환
            val (minF, maxF) = priceRange.value ?: Pair(0f, 0f)
            val minPrice = if (minF <= 0f) null else minF.toInt()
            val maxPrice = if (maxF <= 0f) null else maxF.toInt()

            val list = appService.getAdvertiseList(
                token          = initialToken,
                adCode         = adCode,
                pageNo         = pageNo,
                categoryGroup  = categoryGroup,
                categoryMid    = categoryMid,
                categoryScls   = categoryScls,
                areaGroup      = areaGroup,
                areaMid        = areaMid,
                areaScls       = areaScls,
                minPrice       = minPrice,
                maxPrice       = maxPrice
            )
            _items.value = list
            isProgressLoading.value = false
        }
    }

    /** 가격 범위 변경 */
    fun setPriceRange(min: Float, max: Float) {
        _priceRange.value = Pair(min, max)
    }

    /** 카테고리 필터 세팅 */
    fun setCategoryFilter(group: String?, mid: String?, scls: String?) {
        this.categoryGroup = group
        this.categoryMid   = mid
        this.categoryScls  = scls
    }

    /** 지역 필터 세팅 */
    fun setAreaFilter(group: String?, mid: String?, scls: String?) {
        this.areaGroup = group
        this.areaMid   = mid
        this.areaScls  = scls
    }

    /** 화면 재진입 등으로 새로고침할 때 호출 */
    fun resetAndLoad(adCode: Int = 1) {
        currentPage = 1
        endReached = false
        _items.value = emptyList()
        loadNextPage(adCode)
    }

    fun resetPaging() {
        currentPage = 1
        endReached = false
        _items.value = emptyList()
    }

    fun loadNextPage(adCode: Int = 1) {
        if (isProgressLoading.value == true) return
        isProgressLoading.value = true
        if (isLoading || endReached) {
            Log.d("Paging", "스킵: isLoading=$isLoading, endReached=$endReached")
            return
        }
        isLoading = true

        viewModelScope.launch {
            try {
                val newItems = appService.getAdvertiseList(initialToken,
                    adCode,
                    currentPage
                )
                if (newItems.isEmpty()) {
                    endReached = true
                    Log.d("Paging", "endReached true")
                } else {
                    val updated = _items.value.orEmpty() + newItems
                    _items.value = updated
                    currentPage++
                }
            } catch (e: Exception) {
                Log.e("Paging", "로딩 에러", e)
                // 에러 핸들링
            } finally {
                isLoading = false
                isProgressLoading.value = false
            }
        }
    }
}
