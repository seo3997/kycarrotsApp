package com.whomade.kycarrots.ui.ad

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.R
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.adapter.AdAdapter
import kotlinx.coroutines.launch

class AdListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdAdapter
    private lateinit var appService: AppService
    private lateinit var progressBarLayout: View

    private var pageNo = 1
    private var isLoading = false
    private var isLastPage = false

    companion object {
        fun newInstance(tabCd: String): AdListFragment {
            val fragment = AdListFragment()
            val args = Bundle()
            args.putString("TAB_CD", tabCd)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ad_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AdAdapter(this)
        recyclerView.adapter = adapter
        progressBarLayout = view.findViewById(R.id.ll_progress_circle)

        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        appService = AppService(repository)

        fetchAdvertiseList(isRefresh = true)

        parentFragmentManager.setFragmentResultListener("register_result_key", viewLifecycleOwner) { _, bundle ->
            val isSuccess = bundle.getBoolean("register_result", false)
            if (isSuccess) {
                fetchAdvertiseList(isRefresh = true)
            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && !isLastPage && lastVisibleItem >= totalItemCount - 5) {
                    fetchAdvertiseList()
                }
            }
        })
    }

    fun fetchAdvertiseList(isRefresh: Boolean = false) {
        if (isLoading || isLastPage) return
        isLoading = true
        showProgressBar()

        val prefs = requireActivity().getSharedPreferences("TokenInfo", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        if (isRefresh) {
            pageNo = 1
            isLastPage = false
            adapter.clearList()
        }

        lifecycleScope.launch {
            try {
                val ads: List<AdItem> = appService.getAdvertiseList(token, adCode = 1, pageNo = pageNo)
                if (ads.isEmpty()) {
                    isLastPage = true
                } else {
                    if (pageNo == 1) adapter.updateList(ads)
                    else adapter.addList(ads)
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
    fun showProgressBar() {
        progressBarLayout.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBarLayout.visibility = View.GONE
    }
}
