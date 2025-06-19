package com.whomade.kycarrots

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.common.TokenUtil
import kotlinx.coroutines.launch

class DashboardActivity : BaseDrawerActivity() {
    private val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
    private val repository = RemoteRepository(adApi)
    private val appService = AppService(repository)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initToolbar()
        initMoreButton()

        val token = TokenUtil.getToken(this)
        initDashboard(token)
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

    private fun initMoreButton() {
        findViewById<TextView>(R.id.tv_more).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun initDashboard(token: String) {
        loadDashboardStats(token)
        loadRecentProducts(token)
    }

    private fun loadDashboardStats(token: String) {
        lifecycleScope.launch {
            try {
                val stats = appService.getProductDashboard(token)
                updateDashboardUI(stats)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateDashboardUI(stats: Map<String, Int>) {
        findViewById<TextView>(R.id.tv_total_products).text = "총 등록 매물: ${stats["totalCount"] ?: 0}건"
        findViewById<TextView>(R.id.tv_stats).text = "처리 중: ${stats["processingCount"] ?: 0}건 / 완료: ${stats["completedCount"] ?: 0}건"
    }

    private fun loadRecentProducts(token: String) {
        val rv: RecyclerView = findViewById(R.id.rv_recent_products)
        val cardNoData: View = findViewById(R.id.card_no_data)

        lifecycleScope.launch {
            try {
                val recentList = appService.getRecentProducts(token)

                if (recentList.isEmpty()) {
                    rv.visibility = View.GONE
                    cardNoData.visibility = View.VISIBLE
                } else {
                    rv.layoutManager = LinearLayoutManager(this@DashboardActivity)
                    rv.adapter = RecentProductAdapter(recentList)
                    rv.visibility = View.VISIBLE
                    cardNoData.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                rv.visibility = View.GONE
                cardNoData.visibility = View.VISIBLE
            }
        }
    }

    class RecentProductAdapter(
        private val items: List<ProductVo>
    ) : RecyclerView.Adapter<RecentProductAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val quantity: TextView = view.findViewById(R.id.tv_quantity)
            val unit: TextView = view.findViewById(R.id.tv_unit)
            val btnProcess: TextView = view.findViewById(R.id.btn_process)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_product, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = items[position]
            val formattedQty = String.format("%,d", product.quantity?.toIntOrNull() ?: 0)
            holder.quantity.text = "${product.title} ${formattedQty} ${product.unitCodeNm ?: "-"}"
            holder.unit.text = "${product.areaMidNm} ${product.areaSclsNm} / ${product.desiredShippingDate}"
            holder.btnProcess.text = product.saleStatusNm ?: ""

            holder.itemView.setOnClickListener {
                val context = it.context
                val intent = Intent(context, AdDetailActivity::class.java).apply {
                    putExtra("imageUrl", product.imageUrl)
                    putExtra(AdDetailActivity.EXTRA_PRODUCT_ID, product.productId)
                    putExtra(AdDetailActivity.EXTRA_USER_ID, product.userId)
                }
                context.startActivity(intent)
            }
        }
    }
}
