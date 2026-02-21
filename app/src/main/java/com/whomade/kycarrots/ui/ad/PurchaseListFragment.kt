package com.whomade.kycarrots.ui.ad

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.R
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.data.model.OrderCancelRequest
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.adapter.AdAdapter
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.TokenUtil
import kotlinx.coroutines.launch

class PurchaseListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdAdapter
    private lateinit var appService: AppService
    private lateinit var progressBarLayout: View
    private lateinit var emptyTextView: TextView

    private var pageNo = 0 // Spring Page starts from 0
    private var isLoading = false
    private var isLastPage = false

    companion object {
        fun newInstance(): PurchaseListFragment = PurchaseListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_ad_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyTextView = view.findViewById(R.id.tv_empty)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AdAdapter(this)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            // 필요 시 상세 화면 이동 등 처리
        }
        
        progressBarLayout = view.findViewById(R.id.ll_progress_circle)

        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        appService = AppService(repository)

        fetchPurchaseList(isRefresh = true)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val lm = rv.layoutManager as LinearLayoutManager
                val total = lm.itemCount
                val last = lm.findLastVisibleItemPosition()
                if (!isLoading && !isLastPage && last >= total - 5) {
                    fetchPurchaseList()
                }
            }
        })
    }

    private fun fetchPurchaseList(isRefresh: Boolean = false) {
        if (isLoading || (isLastPage && !isRefresh)) return
        isLoading = true
        showProgressBar()

        if (isRefresh) {
            pageNo = 0
            isLastPage = false
            adapter.clearList()
        }

        // 1. String을 가져와서 Long으로 변환 (실패 시 null)
        val userNoStr = LoginInfoUtil.getUserNo(requireContext())
        val userNo: Long = userNoStr?.toLongOrNull() ?: 0L // null이거나 숫자가 아니면 0으로 취급

        // 2. 0 이하인 경우 처리
        if (userNo <= 0L) {
            isLoading = false
            hideProgressBar()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val orders: List<AdItem> = appService.getOrderHistory(userNo, pageNo, 20)
                if (orders.isEmpty()) {
                    if (pageNo == 0) {
                        emptyTextView.visibility = View.VISIBLE
                    }
                    isLastPage = true
                } else {
                    emptyTextView.visibility = View.GONE
                    if (pageNo == 0) adapter.updateList(orders) else adapter.addList(orders)
                    pageNo++
                }
            } catch (e: Exception) {
                Log.e("PurchaseListFragment", "API 호출 실패: ${e.message}")
            } finally {
                isLoading = false
                hideProgressBar()
            }
        }
    }

    private fun showProgressBar() { progressBarLayout.visibility = View.VISIBLE }
    private fun hideProgressBar() { progressBarLayout.visibility = View.GONE }
}

