package com.whomade.kycarrots

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.launch

class AdDetailViewModel : ViewModel() {
    private val appService = AppServiceProvider.getService()

    // 로딩 상태를 관찰할 수 있도록 추가 (Activity에서 Observe 하세요)
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _productDetail = MutableLiveData<ProductDetailResponse?>()
    val productDetail: LiveData<ProductDetailResponse?> = _productDetail

    private val _reviewList = MutableLiveData<List<Map<String, Any>>>()
    val reviewList: LiveData<List<Map<String, Any>>> = _reviewList

    private val _qnaList = MutableLiveData<List<Map<String, Any>>>()
    val qnaList: LiveData<List<Map<String, Any>>> = _qnaList

    // 원본 유지: 단순 데이터 set만 수행 (자동 호출 X)
    fun setProductDetail(detail: ProductDetailResponse) {
        _productDetail.value = detail
    }

    // 탭 클릭 시 Fragment 등에서 호출될 함수들
    fun loadReviews(productId: Long) {
        if (productId <= 0L) return
        viewModelScope.launch {
            _isLoading.value = true // 로딩 시작
            try {
                val list = appService.getReviewList(productId)
                _reviewList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false // 로딩 종료 (성공/실패 무관)
            }
        }
    }

    fun loadQnas(productId: Long) {
        if (productId <= 0L) return
        viewModelScope.launch {
            _isLoading.value = true // 로딩 시작
            try {
                val list = appService.getQnaList(productId)
                _qnaList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false // 로딩 종료
            }
        }
    }

    fun deleteReview(reviewId: String, productId: Long, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = appService.deleteReview(reviewId, token)
                if (success) {
                    loadReviews(productId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateReview(reviewId: String, rating: Int, contents: String, productId: Long, token: String, branchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = appService.updateReview(reviewId, rating, contents, token, branchId)
                if (success) {
                    loadReviews(productId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteQna(qnaId: String, productId: Long, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = appService.deleteQna(qnaId, token)
                if (success) {
                    loadQnas(productId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateQna(qnaId: String, title: String, contents: String, secretYn: String, productId: Long, token: String, branchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = appService.updateQna(qnaId, title, contents, secretYn, token, branchId)
                if (success) {
                    loadQnas(productId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun answerQna(qnaId: String, answerContents: String, productId: Long, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = appService.answerQna(qnaId, answerContents, token)
                if (success) {
                    loadQnas(productId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}