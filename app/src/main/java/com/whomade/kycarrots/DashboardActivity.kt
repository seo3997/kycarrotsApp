package com.whomade.kycarrots

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.NotificationBadgeHelper
import com.whomade.kycarrots.ui.common.TokenUtil
import com.whomade.kycarrots.ui.Noti.NotificationListActivity
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : BaseDrawerActivity() {
    private val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
    private val repository = RemoteRepository(adApi)
    private val appService = AppService(repository)
    private var badge: BadgeDrawable? = null

    private val notiPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
            android.util.Log.d("NOTI", "POST_NOTIFICATIONS granted=$granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initToolbar()
        initViews()
        loadDashboardData()

        if (com.whomade.kycarrots.ui.common.NotificationPermissionUtil.shouldRequest(this)) {
            notiPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            title = "대시보드"
        }
    }

    private fun initViews() {
        findViewById<TextView>(R.id.tv_more).setOnClickListener {
            // 주문 관리 화면으로 이동
            startActivity(Intent(this, OrderMgtActivity::class.java))
        }

        val role = LoginInfoUtil.getMemberCode(this)
        val branchName = LoginInfoUtil.getBranchName(this)
        
        val titleText = when (role) {
            Constants.ROLE_ADMIN -> "시스템 관리자 모드"
            Constants.ROLE_SELL -> "본사 통합 관리 시스템"
            Constants.ROLE_PROJ -> "지점 판매 어드민 [${branchName ?: ""}]"
            else -> "대시보드"
        }
        findViewById<TextView>(R.id.tv_dashboard_title).text = titleText

        if (role == Constants.ROLE_PROJ) {
            findViewById<View>(R.id.card_notice).visibility = View.VISIBLE
        }
    }

    private fun loadDashboardData() {
        val token = TokenUtil.getToken(this)
        if (token.isEmpty()) return

        showProgressBar()
        lifecycleScope.launch {
            try {
                val result = appService.getDashboardMgtData(token)
                if (result != null) {
                    updateUI(result)
                } else {
                    Toast.makeText(this@DashboardActivity, "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                hideProgressBar()
            }
        }
    }

    private fun updateUI(data: Map<String, Any>) {
        val role = LoginInfoUtil.getMemberCode(this)
        val stats = data["dashboardStats"] as? Map<*, *>
        
        if (stats != null) {
            when (role) {
                Constants.ROLE_ADMIN -> {
                    setStat(1, "총 사용자 수", stats["totalUsers"], "#1E293B")
                    setStat(2, "지점 수", stats["totalBranches"], "#6366F1")
                    setStat(3, "누적 주문", stats["totalOrders"], "#EF4444")
                    setStat(4, "누적 매출", stats["totalRevenue"], "#10B981", isCurrency = true)
                }
                Constants.ROLE_SELL -> {
                    setStat(1, "미처리 주문", stats["unprocessedOrders"], "#1E293B")
                    setStat(2, "지점 미입금액", stats["branchPendingAmount"], "#EF4444", isCurrency = true)
                    setStat(3, "출고 대기", stats["shipmentPending"], "#10B981")
                    setStat(4, "배송 중", stats["inTransit"], "#3B82F6")
                }
                Constants.ROLE_PROJ -> {
                    setStat(1, "오늘의 매출", stats["todayTotalSales"], "#1E293B", isCurrency = true)
                    setStat(2, "예상 순이익", stats["estimatedProfit"], "#6366F1", isCurrency = true)
                    setStat(3, "본사 송금 대기", stats["remittancePending"], "#EF4444")
                    setStat(4, "이달의 주문", stats["completedOrders"], "#10B981")
                    
                    val hq = data["headQuarterBranch"] as? Map<*, *>
                    if (hq != null) {
                        val bank = hq["BANK_NM"] ?: ""
                        val acc = hq["ACCOUNT_NO"] ?: ""
                        val holder = hq["ACCOUNT_HOLDER"] ?: ""
                        findViewById<TextView>(R.id.tv_notice_text).text = "본사 입금 안내: ${bank} ${acc} (예금주: ${holder})로 입금하셔야 배송이 시작됩니다."
                    }
                }
            }
        }

        val ordersList = data["dashboardOrderList"] as? List<Map<String, Any>>
        val rv: RecyclerView = findViewById(R.id.rv_recent_orders)
        val noData: View = findViewById(R.id.card_no_data)

        if (ordersList.isNullOrEmpty()) {
            rv.visibility = View.GONE
            noData.visibility = View.VISIBLE
        } else {
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = MgtOrderDashboardAdapter(ordersList)
            rv.visibility = View.VISIBLE
            noData.visibility = View.GONE
        }
    }

    private fun setStat(index: Int, label: String, value: Any?, color: String, isCurrency: Boolean = false) {
        val labelId = resources.getIdentifier("tv_stat${index}_label", "id", packageName)
        val valueId = resources.getIdentifier("tv_stat${index}_value", "id", packageName)
        
        findViewById<TextView>(labelId)?.text = label
        
        val valStr = when {
            value == null -> "0"
            isCurrency -> formatCurrency(value.toString().toDoubleOrNull()?.toInt() ?: 0)
            else -> formatNumber(value.toString().toDoubleOrNull()?.toInt() ?: 0)
        }
        
        findViewById<TextView>(valueId)?.apply {
            text = valStr
            kotlin.runCatching { setTextColor(android.graphics.Color.parseColor(color)) }
        }
    }

    private fun formatCurrency(amount: Int): String {
        return NumberFormat.getCurrencyInstance(Locale.KOREA).format(amount)
    }

    private fun formatNumber(count: Int): String {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(count) + "건"
    }

    private fun showProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
    }

    inner class MgtOrderDashboardAdapter(
        private val items: List<Map<String, Any>>
    ) : RecyclerView.Adapter<MgtOrderDashboardAdapter.ViewHolder>() {

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
            holder.branchName.text = (order["BRANCH_NAME"] ?: order["branchName"] ?: "").toString()
            holder.orderDate.text = (order["ORDERED_AT"] ?: order["orderedAt"] ?: order["ORDER_DATE"] ?: "").toString()
            holder.orderNo.text = (order["ORDER_NO"] ?: order["orderNo"] ?: "").toString()
            
            val amtValue = (order["TOTAL_PAY_AMOUNT"] ?: order["totalPayAmount"] ?: order["SUPPLY_PRICE_SUM"] ?: order["supplyPriceSum"])?.toString()?.toDoubleOrNull()?.toInt() ?: 0
            holder.orderAmount.text = formatCurrency(amtValue)
            
            val statusNm = (order["ORDER_STATUS_NM"] ?: order["orderStatusNm"])?.toString()
            holder.orderStatus.text = statusNm ?: (order["ORDER_STATUS"] ?: order["orderStatus"] ?: "").toString()

            holder.itemView.setOnClickListener {
                val intent = Intent(it.context, OrderMgtDetailActivity::class.java).apply {
                    val rawId = order["ORDER_ID"] ?: order["orderId"]
                    val oid = rawId?.toString()?.toDoubleOrNull()?.toLong()?.toString()
                    putExtra("orderId", oid)
                }
                it.context.startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        badge = NotificationBadgeHelper.attach(
            activity = this,
            menu = menu,
            toolbar = toolbar,
            menuItemId = R.id.action_notifications
        )
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_notifications -> {
                startActivity(Intent(this, NotificationListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
