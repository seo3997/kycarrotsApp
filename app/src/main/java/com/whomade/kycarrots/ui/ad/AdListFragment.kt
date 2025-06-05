package com.whomade.kycarrots.ui.ad

import android.os.Bundle
import android.os.Parcelable
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

    private var listState: Parcelable? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_ad_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        appService = AppService(repository)
        fetchAdvertiseList();
    }

    private fun fetchAdvertiseList() {
        val token = "%2FV%2F26xyieYwgQKUf6wFvdeMy3O%2Fw%2Fc6g0sAskcxhDZq1I3kiw2GIHmlt3Mm5SSL0ymBfgmxLIjjymbiDmbC%2B3JpMkWY9xcCFUXUJj2RWO5XKMqJ5YxuYevAntPPC1hbIRy6tcFp2lVZwIiGu%2Be6hsL6B2yufbYBF5rxE78nRNuB2JGGFm6RPyuhdQGtnaPCWwsmMOYd1q3eG0uXH91j4hHtyU7HiGqVfQ0nGiqRXY3PC50wIRtH5zalLw6Lq4Bqrwlq79dfIhOTkcNP3RCItWyZd5lgqlFPh5Z4q98Rvfw2v6hT4JnlIWEs7fMJ2M0RYaKfvpuxsFsgQPQ1Qvxs22vwY7iypqbii5GAa413Dk0A%3D"

        lifecycleScope.launch {
            try {
                val ads: List<AdItem> = appService.getAdvertiseList(token, adCode = 1, pageNo = 1)
                adapter = AdAdapter(ads,this@AdListFragment)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                Log.e("AdListFragment", "API 호출 실패: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //fetchAdvertiseList()
    }

}
