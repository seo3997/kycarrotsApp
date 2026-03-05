package com.whomade.kycarrots.ui.ad

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

/**
 * 관심상품 목록 전용 Fragment
 * - 레이아웃: fragment_ad_list 재사용
 * - 페이징/프로그레스/어댑터 구조 동일
 * - 데이터 소스만 getInterestList()로 변경
 */
class InterestListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdAdapter
    private lateinit var appService: AppService
    private lateinit var progressBarLayout: View
    private lateinit var emptyTextView: TextView

    private var pageNo = 1
    private var isLoading = false
    private var isLastPage = false

    private lateinit var detailLauncher: ActivityResultLauncher<Intent>

    companion object {
        fun newInstance(): InterestListFragment = InterestListFragment()
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
        progressBarLayout = view.findViewById(R.id.ll_progress_circle)

        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        appService = AppService(repository)

        fetchInterestList(isRefresh = true)


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val lm = rv.layoutManager as LinearLayoutManager
                val total = lm.itemCount
                val last = lm.findLastVisibleItemPosition()
                if (!isLoading && !isLastPage && last >= total - 5) {
                    fetchInterestList()
                }
            }
        })

        adapter.setOnItemClickListener { item, sharedView ->
            val intent = Intent(requireContext(), AdDetailActivity::class.java).apply {
                putExtra("imageUrl", item.imageUrl)
                putExtra(AdDetailActivity.EXTRA_PRODUCT_ID, item.productId) // String
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), sharedView, "shared_image"
            )
            detailLauncher.launch(intent, options)  // ✅ 결과를 받으려면 반드시 launch 사용
        }

        detailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val productId = data?.getStringExtra("productId") ?: ""
                val isInterested = data?.getBooleanExtra("isInterested", true) ?: true

                if (productId.isNotEmpty()) {
                    if (!isInterested) {
                        adapter.removeByProductId(productId)
                        if (adapter.itemCount == 0) {
                            emptyTextView.visibility = View.VISIBLE
                            isLastPage = true
                        }
                    }
                }
            }
        }
    }

    private fun fetchInterestList(isRefresh: Boolean = false) {
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
                val ads: List<AdItem> = appService.getInterestList(token, pageNo)

                if (ads.isEmpty()) {
                    if (pageNo == 1) {
                        // 첫 페이지부터 데이터 없음 → 안내 문구 노출
                        emptyTextView.visibility = View.VISIBLE
                    }
                    isLastPage = true
                } else {
                    // 데이터 있으면 리스트 갱신
                    emptyTextView.visibility = View.GONE
                    if (pageNo == 1) adapter.updateList(ads) else adapter.addList(ads)
                    pageNo++
                }
            } catch (e: Exception) {
                Log.e("InterestListFragment", "API 호출 실패: ${e.message}")
            } finally {
                isLoading = false
                hideProgressBar()
            }
        }
    }
    private fun showProgressBar() { progressBarLayout.visibility = View.VISIBLE }
    private fun hideProgressBar() { progressBarLayout.visibility = View.GONE }
}
