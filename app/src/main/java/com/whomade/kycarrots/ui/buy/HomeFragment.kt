package com.whomade.kycarrots.ui.buy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.slider.RangeSlider
import com.whomade.kycarrots.R
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.Noti.NotificationListActivity
import com.whomade.kycarrots.ui.common.TokenUtil
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    // ViewModel (토큰은 fragment context 사용)
    private val viewModel: AdListViewModel by viewModels {
        AdListViewModelFactory(TokenUtil.getToken(requireContext()))
    }

    // Service
    private val appService by lazy { AppServiceProvider.getService() }

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdActivityAdapter
    private lateinit var llProgress: View
    private lateinit var btnInquiry: Button
    private lateinit var sliderPrice: RangeSlider
    private lateinit var tvSelectedRange: TextView
    private lateinit var emptyTextView: TextView
    private lateinit var checkboxSaleOnly: CheckBox

    private var currentAdCode = 1
    private var currentPage   = 1

    private var badge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // 툴바 메뉴 사용(알림 배지)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 뷰 바인딩
        recyclerView        = view.findViewById(R.id.recyclerView)
        btnInquiry          = view.findViewById(R.id.btn_inquiry)
        sliderPrice         = view.findViewById(R.id.slider_price)
        tvSelectedRange     = view.findViewById(R.id.selected_range)
        llProgress          = view.findViewById(R.id.ll_progress_circle)
        llProgress.visibility = View.GONE
        emptyTextView       = view.findViewById(R.id.emptyTextView)
        checkboxSaleOnly    = view.findViewById(R.id.checkbox_sale_only)

        // 2) 리스트/어댑터 (어댑터가 Activity 타입을 요구 → requireActivity())
        adapter = AdActivityAdapter(requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 3) 슬라이더
        sliderPrice.addOnChangeListener { slider, _, _ ->
            val (min, max) = slider.values
            viewModel.setPriceRange(min, max)
        }

        // 4) 희망 단가 토글
        val checkBox = view.findViewById<CheckBox>(R.id.checkbox_price)
        val priceLayout = view.findViewById<View>(R.id.layout_price_range)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            priceLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            sliderPrice.setValues(sliderPrice.valueFrom, sliderPrice.valueTo)
        }

        viewModel.setSaleOnly(if (checkboxSaleOnly.isChecked) "1" else "0")
        checkboxSaleOnly.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSaleOnly(if (isChecked) "1" else "0")
            applyFiltersAndReload()    // ✅ 요청하신 대로 이 함수 호출
        }
        // 5) 조회하기 버튼
        btnInquiry.setOnClickListener {
            viewModel.resetPaging()
            viewModel.loadNextPage()
        }

        // 6) LiveData
        viewModel.priceRange.observe(viewLifecycleOwner) { (min, max) ->
            val fmtMin = "%,d".format(min.toInt())
            val fmtMax = "%,d".format(max.toInt())
            tvSelectedRange.text = "$fmtMin ~ $fmtMax 원"
        }
        viewModel.items.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
            emptyTextView.visibility = if (list.isNullOrEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility  = if (list.isNullOrEmpty()) View.GONE   else View.VISIBLE
        }
        viewModel.isProgressLoading.observe(viewLifecycleOwner) { isLoading ->
            llProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 7) 무한 스크롤
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm    = rv.layoutManager as LinearLayoutManager
                val total = lm.itemCount
                val last  = lm.findLastVisibleItemPosition()
                Log.d("Paging", "last=$last, total=$total, isLoading=${viewModel.isLoading}")
                if (last == total - 1 && !viewModel.isLoading && !viewModel.endReached) {
                    llProgress.visibility = View.VISIBLE
                    viewModel.loadNextPage()
                }
            }
        })

        // 8) 드롭다운 초기화 + 리스너 제거됨

        // 9) 최초 로드 (onResume에서 처리하도록 변경)
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetAndLoad(currentAdCode)
    }

    // === Data loads ===
    private fun applyFiltersAndReload() {
        viewModel.loadItems(adCode = currentAdCode, pageNo = currentPage)
    }
}
