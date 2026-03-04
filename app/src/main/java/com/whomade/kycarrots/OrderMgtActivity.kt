package com.whomade.kycarrots

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.common.TokenUtil
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class OrderMgtActivity : AppCompatActivity() {

    private val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
    private val repository = RemoteRepository(adApi)
    private val appService = AppService(repository)

    private lateinit var rvOrders: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var spStatus: Spinner
    private lateinit var tvDateRange: TextView
    private lateinit var btnSearch: ImageButton

    private var startDate: String? = null
    private var endDate: String? = null
    private var currentStatus: String? = null
    private var orderList: MutableList<Map<String, Any>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_mgt_list)

        initToolbar()
        initViews()
        loadData()
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "주문 통합 관리"
        }
    }

    private fun initViews() {
        rvOrders = findViewById(R.id.rv_orders)
        etSearch = findViewById(R.id.et_search)
        spStatus = findViewById(R.id.sp_status)
        tvDateRange = findViewById(R.id.tv_date_range)
        btnSearch = findViewById(R.id.btn_search)

        // Status Spinner
        val statusList = listOf("전체 상태", "결제대기(10)", "결제완료(30)", "주문취소(40)", "배송준비(50)", "배송중(60)", "배송완료(70)", "반품요청(80)", "반품완료(89)", "주문확정(99)")
        val statusCodes = listOf(null, "10", "30", "40", "50", "60", "70", "80", "89", "99")
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusList)
        spStatus.adapter = adapter
        spStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStatus = statusCodes[position]
                loadData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Date Picker
        tvDateRange.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("조회 기간 선택")
                .setSelection(Pair(MaterialDatePicker.todayInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds()))
                .build()

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                startDate = sdf.format(Date(selection.first))
                endDate = sdf.format(Date(selection.second))
                tvDateRange.text = "$startDate ~ $endDate"
                loadData()
            }
            dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
        }

        btnSearch.setOnClickListener {
            loadData()
        }
    }

    private fun loadData() {
        val token = TokenUtil.getToken(this)
        if (token.isEmpty()) return

        showProgressBar()
        lifecycleScope.launch {
            try {
                val keyword = etSearch.text?.toString()
                val result = appService.getOrderMgtList(token, currentStatus, startDate, endDate, keyword)
                if (result != null) {
                    val list = result["resultList"] as? List<Map<String, Any>>
                    if (list != null) {
                        orderList.clear()
                        orderList.addAll(list)
                        updateUI()
                    }
                } else {
                    Toast.makeText(this@OrderMgtActivity, "주문 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                hideProgressBar()
            }
        }
    }

    private fun updateUI() {
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = OrderMgtAdapter(orderList)
    }

    private fun showProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class OrderMgtAdapter(private val items: List<Map<String, Any>>) : RecyclerView.Adapter<OrderMgtAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val branchName: TextView = view.findViewById(R.id.tv_branch_name)
            val orderDate: TextView = view.findViewById(R.id.tv_order_date)
            val orderNo: TextView = view.findViewById(R.id.tv_order_no)
            val orderAmount: TextView = view.findViewById(R.id.tv_order_amount)
            val orderStatus: TextView = view.findViewById(R.id.tv_order_status)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mgt_order_dashboard, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = items[position]
            holder.branchName.text = (order["BRANCH_NAME"] ?: order["USER_NM"] ?: "").toString()
            holder.orderDate.text = (order["ORDERED_AT"] ?: order["ORDER_DATE"] ?: "").toString()
            holder.orderNo.text = (order["ORDER_NO"] ?: "").toString()
            
            val amtValue = (order["TOTAL_PAY_AMOUNT"] ?: order["SUPPLY_PRICE_SUM"])?.toString()?.toDoubleOrNull()?.toInt() ?: 0
            holder.orderAmount.text = NumberFormat.getCurrencyInstance(Locale.KOREA).format(amtValue)
            
            val status = (order["ORDER_STATUS"] ?: "").toString()
            holder.orderStatus.text = getOrderStatusName(status)

            holder.itemView.setOnClickListener {
                val intent = Intent(it.context, OrderMgtDetailActivity::class.java).apply {
                    putExtra("orderNo", (order["ORDER_NO"] ?: "").toString())
                }
                it.context.startActivity(intent)
            }
        }

        private fun getOrderStatusName(status: String): String {
            return when (status) {
                "10" -> "결제대기"
                "30" -> "결제완료"
                "40" -> "주문취소"
                "50" -> "배송준비"
                "60" -> "배송중"
                "70" -> "배송완료"
                "80" -> "반품요청"
                "89" -> "반품완료"
                "99" -> "주문확정"
                else -> status
            }
        }
    }
}
