package com.whomade.kycarrots.ui.ad

import android.content.Intent
import com.whomade.kycarrots.OrderDetailActivity
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
        adapter = AdAdapter(this, R.layout.item_purchase)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            val oid = item.orderId ?: item.orderNo
            if (!oid.isNullOrEmpty()) {
                val intent = Intent(requireContext(), OrderDetailActivity::class.java).apply {
                    putExtra("orderId", oid)
                }
                startActivity(intent)
            }
        }

        adapter.setOnCancelClickListener { item ->
            AlertDialog.Builder(requireContext())
                .setTitle("주문 취소")
                .setMessage("정말로 주문을 취소하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    cancelOrderInList(item)
                }
                .setNegativeButton("취소", null)
                .show()
        }

        adapter.setOnReturnClickListener { item ->
            AlertDialog.Builder(requireContext())
                .setTitle("반품 요청")
                .setMessage("반품을 요청하시겠습니까? (배송비가 발생할 수 있습니다.)")
                .setPositiveButton("확인") { _, _ ->
                    returnOrderInList(item)
                }
                .setNegativeButton("취소", null)
                .show()
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

    private fun cancelOrderInList(item: AdItem) {
        // 7-day check
        val orderedAt = item.orderedAt
        if (!orderedAt.isNullOrEmpty()) {
            try {
                // Handling formats like "2024.03.04 12:00" or "2024-03-04 12:00:00"
                val cleanDate = orderedAt.replace(".", "-")
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val orderDate = sdf.parse(cleanDate)
                if (orderDate != null) {
                    val diff = System.currentTimeMillis() - orderDate.time
                    val diffDays = diff / (1000 * 60 * 60 * 24)
                    if (diffDays > 7) {
                        Toast.makeText(requireContext(), "결제 후 7일이 경과하여 직접 취소가 불가능합니다. 고객센터로 문의해주세요.", Toast.LENGTH_LONG).show()
                        return
                    }
                }
            } catch (e: Exception) {
                Log.e("PurchaseListFragment", "Cancel date parse error: $orderedAt")
            }
        }

        val userNoStr = LoginInfoUtil.getUserNo(requireContext())
        val userNo = userNoStr?.toLongOrNull() ?: 0L
        if (item.orderNo == null || userNo <= 0L) return

        val request = OrderCancelRequest(
            orderNo = item.orderNo,
            cancelReason = "고객 변심",
            userNo = userNo
        )

        showProgressBar()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val success = appService.cancelPayment(request)
                if (success) {
                    Toast.makeText(requireContext(), "주문이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    fetchPurchaseList(isRefresh = true)
                } else {
                    Toast.makeText(requireContext(), "취소 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "오류 발생", Toast.LENGTH_SHORT).show()
            } finally {
                hideProgressBar()
            }
        }
    }

    private fun returnOrderInList(item: AdItem) {
        // 7-day check
        val deliveredAt = item.deliveredAt
        if (!deliveredAt.isNullOrEmpty()) {
            try {
                val cleanDate = deliveredAt.replace("T", " ")
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val delDate = sdf.parse(cleanDate)
                if (delDate != null) {
                    val diff = System.currentTimeMillis() - delDate.time
                    val diffDays = diff / (1000 * 60 * 60 * 24)
                    if (diffDays > 7) {
                        Toast.makeText(requireContext(), "배송 완료 후 7일이 경과하여 반품 요청이 불가능합니다. 고객센터로 문의해주세요.", Toast.LENGTH_LONG).show()
                        return
                    }
                }
            } catch (e: Exception) {
                Log.e("PurchaseListFragment", "Return date parse error: $deliveredAt")
            }
        }

        val userNoStr = LoginInfoUtil.getUserNo(requireContext())
        val userNo = userNoStr?.toLongOrNull() ?: 0L
        if (item.orderNo == null || userNo <= 0L) return

        val req = mapOf(
            "orderNo" to item.orderNo,
            "returnReason" to "단순 변심",
            "userNo" to userNo
        )

        showProgressBar()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val success = appService.requestReturn(req)
                if (success) {
                    Toast.makeText(requireContext(), "반품 요청이 접수되었습니다.", Toast.LENGTH_SHORT).show()
                    fetchPurchaseList(isRefresh = true)
                } else {
                    Toast.makeText(requireContext(), "반품 요청 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "오류 발생", Toast.LENGTH_SHORT).show()
            } finally {
                hideProgressBar()
            }
        }
    }

    private fun showProgressBar() { progressBarLayout.visibility = View.VISIBLE }
    private fun hideProgressBar() { progressBarLayout.visibility = View.GONE }
}

