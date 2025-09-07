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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.AdDetailActivity
import com.whomade.kycarrots.MainActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.data.model.AdListRequest
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.adapter.AdAdapter
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import kotlinx.coroutines.launch

class AdListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdAdapter
    private lateinit var appService: AppService
    private var progressBarLayout: View? = null

    private var pageNo = 1
    private var isLoading = false
    private var isLastPage = false

    private var saleStatus: String = "1"
    private lateinit var emptyTextView: TextView
    // TAB_CD -> saleStatus 매핑: 1->"1"(판매중), 2->"10"(예약중), 3->"99"(판매완료)
    private fun mapSaleStatusFromTab(tabCd: String?): String = when (tabCd) {
        "0" -> "0"
        "1" -> "1"
        "2" -> "10"
        "3" -> "99"
        else -> "1"
    }
    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val changed = result.data?.getBooleanExtra("status_changed", false) ?: false
            if (changed) {
                // 1) 현재 탭 재조회
                fetchAdvertiseList(isRefresh = true)

                // 2) 필요 시, 변경된 상태의 탭도 재조회 (선택)
                val newStatus = result.data?.getStringExtra("new_status")
                (activity as? MainActivity)?.refreshTabBySaleStatus(newStatus)
            }
        }
    }

    companion object {
        fun newInstance(tabCd: String): AdListFragment =
            AdListFragment().apply {
                arguments = Bundle().apply { putString("TAB_CD", tabCd) }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saleStatus = mapSaleStatusFromTab(arguments?.getString("TAB_CD"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_ad_list, container, false)

    private val pagingScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)
            val lm = rv.layoutManager as LinearLayoutManager
            val total = lm.itemCount
            val last = lm.findLastVisibleItemPosition()
            if (!isLoading && !isLastPage && last >= total - 5) {
                fetchAdvertiseList()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyTextView = view.findViewById(R.id.tv_empty)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AdAdapter(this) // 클릭 리스너 사용하니 this 전달 유지
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(pagingScrollListener)

        adapter.setOnItemClickListener { item, sharedView ->
            val intent = Intent(requireContext(), AdDetailActivity::class.java).apply {
                putExtra("imageUrl", item.imageUrl)
                putExtra(AdDetailActivity.EXTRA_PRODUCT_ID, item.productId)
                putExtra(AdDetailActivity.EXTRA_USER_ID, item.userId)
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), sharedView, "shared_image"
            )
            detailLauncher.launch(intent, options)
        }

        progressBarLayout = view.findViewById(R.id.ll_progress_circle)

        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        appService = AppService(repository)

        if (adapter.itemCount == 0) {
            fetchAdvertiseList(isRefresh = true)
        }

        // 등록/수정 성공 시 재조회
        parentFragmentManager.setFragmentResultListener(
            "register_result_key",
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean("register_result", false)) {
                fetchAdvertiseList(isRefresh = true)
            }
        }
    }

    override fun onDestroyView() {
        recyclerView.removeOnScrollListener(pagingScrollListener)
        progressBarLayout = null
        super.onDestroyView()
    }

    fun fetchAdvertiseList(isRefresh: Boolean = false) {
        if (isLoading || (!isRefresh && isLastPage)) return
        isLoading = true
        showProgressBar()

        val prefs = requireActivity().getSharedPreferences("TokenInfo", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val memberCode = LoginInfoUtil.getMemberCode(requireActivity())
        if (isRefresh) {
            pageNo = 1
            isLastPage = false
            adapter.clearList()
            recyclerView.scrollToPosition(0)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val req = AdListRequest(
                    token = token,
                    adCode = 1,
                    pageno = pageNo,
                    saleStatus = saleStatus,
                    memberCode = memberCode
                )
                val ads: List<AdItem> = appService.getAdvertiseList(req)

                if (ads.isEmpty()) {
                    isLastPage = true
                    emptyTextView.visibility = View.VISIBLE
                } else {
                    if (pageNo == 1) adapter.updateList(ads) else adapter.addList(ads)
                    pageNo++
                }
            } catch (e: Exception) {
                Log.e("AdListFragment", "API 호출 실패: ${e.message}")
            } finally {
                isLoading = false
                hideProgressBar()
            }
        }
    }

    private fun showProgressBar() { progressBarLayout?.visibility = View.VISIBLE }
    private fun hideProgressBar() { progressBarLayout?.visibility = View.GONE }
}
