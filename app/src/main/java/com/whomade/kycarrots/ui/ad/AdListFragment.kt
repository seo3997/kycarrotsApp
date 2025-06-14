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

        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        appService = AppService(repository)

        fetchAdvertiseList()

        parentFragmentManager.setFragmentResultListener("register_result_key", viewLifecycleOwner) { _, bundle ->
            val isSuccess = bundle.getBoolean("register_result", false)
            if (isSuccess) {
                fetchAdvertiseList()
            }
        }
    }

    private fun fetchAdvertiseList() {
        val prefs = requireActivity().getSharedPreferences("TokenInfo", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        lifecycleScope.launch {
            try {
                val ads: List<AdItem> = appService.getAdvertiseList(token, adCode = 1, pageNo = 1)
                Log.d("AdListFragment", "updateList 호출됨: ${ads.size}개 아이템")
                adapter.updateList(ads)
            } catch (e: Exception) {
                Log.e("AdListFragment", "API 호출 실패: ${e.message}")
            }
        }
    }
}
