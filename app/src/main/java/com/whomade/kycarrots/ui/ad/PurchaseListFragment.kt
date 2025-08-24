// src/main/java/com/whomade/kycarrots/ui/ad/PurchaseListFragment.kt
package com.whomade.kycarrots.ui.ad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.AdDetailActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.adapter.AdAdapter
import com.whomade.kycarrots.ui.common.TokenUtil
import kotlinx.coroutines.launch

class PurchaseListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdAdapter
    private lateinit var appService: AppService
    private lateinit var progressBarLayout: View
    private lateinit var emptyTextView: TextView

    private var pageNo = 1
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

        adapter.setOnItemClickListener { item, sharedView ->
            val intent = Intent(requireContext(), AdDetailActivity::class.java).apply {
                putExtra("imageUrl", item.imageUrl)
                putExtra(AdDetailActivity.EXTRA_PRODUCT_ID, item.productId) // String
                putExtra(AdDetailActivity.EXTRA_USER_ID, item.userId)
            }

            // 공유 요소 전환 (imageView와 "shared_image"는 item_ad.xml과 상세 레이아웃에서 동일해야 함)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), sharedView, "shared_image"
            )
            startActivity(intent, options.toBundle())
            // 만약 상세에서 결과를 받아 목록을 갱신하고 싶다면:
            // detailLauncher.launch(intent, options)
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
        if (isLoading || isLastPage) return
        isLoading = true
        showProgressBar()

        val token = TokenUtil.getToken(requireContext())

        if (isRefresh) {
            pageNo = 1
            isLastPage = false
            adapter.clearList()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ads: List<AdItem> = appService.getPurchaseList(token, pageNo)
                if (ads.isEmpty()) {
                    if (pageNo == 1) {
                        // 첫 페이지부터 데이터 없음
                        emptyTextView.visibility = View.VISIBLE
                    }
                    isLastPage = true
                } else {
                    emptyTextView.visibility = View.GONE
                    if (pageNo == 1) adapter.updateList(ads) else adapter.addList(ads)
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
