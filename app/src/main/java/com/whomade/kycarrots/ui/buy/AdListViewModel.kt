package com.whomade.kycarrots.ui.buy


import android.util.Log
import androidx.lifecycle.*
import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.data.model.AdListRequest
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.launch

class AdListViewModel(
    private val appService: AppService,
    private val initialToken: String
) : ViewModel() {

    private val _items = MutableLiveData<List<AdItem>>(emptyList())
    val items: LiveData<List<AdItem>> = _items

    private val _priceRange = MutableLiveData(Pair(0f, 9990000f))
    val priceRange: LiveData<Pair<Float, Float>> = _priceRange
    val isProgressLoading = MutableLiveData(false)

    private var currentPage = 1
    internal var isLoading = false
        private set
    internal var endReached = false
        private set

    // 필터 상태
    private var adCode: Int = 1
    private var categoryGroup: String? = null
    private var categoryMid:   String? = null
    private var categoryScls:  String? = null
    private var areaGroup:     String? = null
    private var areaMid:       String? = null
    private var areaScls:      String? = null
    private var saleStatus:    String = "1"

    /** 필터, 가격 등 모든 조건을 ViewModel에 저장하고, pageNo 만 전달 */
    private fun getQueryParam(pageNo: Int): QueryParam {
        val (minF, maxF) = priceRange.value ?: Pair(0f, 0f)
        val minPrice = if (minF > 0f) minF.toInt() else null
        val maxPrice = if (maxF > 0f) maxF.toInt() else null
        return QueryParam(
            token = initialToken,
            adCode = adCode,
            pageNo = pageNo,
            categoryGroup = categoryGroup,
            categoryMid = categoryMid,
            categoryScls = categoryScls,
            areaGroup = areaGroup,
            areaMid = areaMid,
            areaScls = areaScls,
            minPrice = minPrice,
            maxPrice = maxPrice,
            saleStatus = saleStatus
        )
    }

    // 1. 전체 새로고침(조회)
    fun loadItems(
        adCode: Int   = this.adCode,
        pageNo: Int   = 1,
        categoryGroup: String? = this.categoryGroup,
        categoryMid:   String? = this.categoryMid,
        categoryScls:  String? = this.categoryScls,
        areaGroup:     String? = this.areaGroup,
        areaMid:       String? = this.areaMid,
        areaScls:      String? = this.areaScls
    ) {
        this.adCode = adCode
        this.categoryGroup = categoryGroup
        this.categoryMid = categoryMid
        this.categoryScls = categoryScls
        this.areaGroup = areaGroup
        this.areaMid = areaMid
        this.areaScls = areaScls
        this.currentPage = pageNo
        endReached = false

        isProgressLoading.value = true
        isLoading = true

        viewModelScope.launch {
            try {
                val param = getQueryParam(pageNo)
                val req = AdListRequest(
                    token        = param.token,
                    adCode       = param.adCode,
                    pageno       = param.pageNo,
                    categoryGroup= param.categoryGroup ?: "R010610",
                    categoryMid  = param.categoryMid,
                    categoryScls = param.categoryScls,
                    areaGroup    = param.areaGroup ?: "R010070",
                    areaMid      = param.areaMid,
                    areaScls     = param.areaScls,
                    minPrice     = param.minPrice,
                    maxPrice     = param.maxPrice,
                    saleStatus   = param.saleStatus
                )
                val list = appService.getBuyAdvertiseList(req)
                _items.value = list
                endReached = list.isEmpty()
            } catch (e: Exception) {
                // 에러 처리
                endReached = true
            } finally {
                isProgressLoading.value = false
                isLoading = false
            }
        }
    }

    // 2. 다음 페이지 로딩(필터는 그대로, pageNo만 +1)
    fun loadNextPage() {
        if (isProgressLoading.value == true || isLoading || endReached) return
        isProgressLoading.value = true
        isLoading = true

        val nextPage = currentPage + 1

        viewModelScope.launch {
            try {
                val param = getQueryParam(nextPage)
                val req = AdListRequest(
                    token        = param.token,
                    adCode       = param.adCode,
                    pageno       = param.pageNo,
                    categoryGroup= param.categoryGroup ?: "R010610",
                    categoryMid  = param.categoryMid,
                    categoryScls = param.categoryScls,
                    areaGroup    = param.areaGroup ?: "R010070",
                    areaMid      = param.areaMid,
                    areaScls     = param.areaScls,
                    minPrice     = param.minPrice,
                    maxPrice     = param.maxPrice,
                    saleStatus   = param.saleStatus
                )

                val newItems = appService.getBuyAdvertiseList(req)
                if (newItems.isEmpty()) {
                    endReached = true
                } else {
                    val updated = _items.value.orEmpty() + newItems
                    _items.value = updated
                    currentPage = nextPage
                }
            } catch (e: Exception) {
                // 에러 핸들링
            } finally {
                isLoading = false
                isProgressLoading.value = false
            }
        }
    }

    fun setPriceRange(min: Float, max: Float) {
        _priceRange.value = Pair(min, max)
    }
    fun setCategoryFilter(group: String?, mid: String?, scls: String?) {
        this.categoryGroup = group
        this.categoryMid   = mid
        this.categoryScls  = scls
    }
    fun setAreaFilter(group: String?, mid: String?, scls: String?) {
        this.areaGroup = group
        this.areaMid   = mid
        this.areaScls  = scls
    }

    fun setSaleOnly(value: String) { saleStatus = value }

    fun resetPaging() {
        currentPage = 0
        endReached = false
        _items.value = emptyList()
    }

    fun resetAndLoad(adCode: Int = 1) {
        currentPage = 1
        endReached = false
        this.adCode = adCode
        _items.value = emptyList()
        loadItems(adCode, 1)
    }

    // QueryParam 데이터 클래스 정의
    data class QueryParam(
        val token: String,
        val adCode: Int,
        val pageNo: Int,
        val categoryGroup: String?,
        val categoryMid: String?,
        val categoryScls: String?,
        val areaGroup: String?,
        val areaMid: String?,
        val areaScls: String?,
        val minPrice: Int?,
        val maxPrice: Int?,
        var saleStatus: String? = "1"
    )
}
