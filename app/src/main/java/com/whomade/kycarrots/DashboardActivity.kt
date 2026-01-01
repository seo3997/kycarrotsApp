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
import com.whomade.kycarrots.data.model.OpUserVO
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.Noti.NotificationListActivity
import com.whomade.kycarrots.ui.ad.makead.KtMakeADDetailView
import com.whomade.kycarrots.ui.ad.makead.KtMakeADMainActivity
import com.whomade.kycarrots.ui.ad.makead.MakeADDetail1
import com.whomade.kycarrots.ui.ad.makead.MakeADMainActivity
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.NotificationBadgeHelper
import com.whomade.kycarrots.ui.common.TokenUtil
import com.whomade.kycarrots.ui.dialog.BottomDto
import com.whomade.kycarrots.ui.dialog.BottomDtoPickerSheet
import kotlinx.coroutines.launch

class DashboardActivity : BaseDrawerActivity() {
    private val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
    private val repository = RemoteRepository(adApi)
    private val appService = AppService(repository)
    private var badge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initToolbar()
        initMoreButton()


        initDashboard()

        val btnAddProduct: View = findViewById(R.id.btn_add_product)
        btnAddProduct.setOnClickListener {
            if (Constants.SYSTEM_TYPE == 1) {
                // 바로 상품등록 화면으로
                moveToMakeAD()
            } else if (Constants.SYSTEM_TYPE == 2) {
                // 기본 중간센터 확인 후 상품등록
                handleAddProductClick()
            }
        }

        val btnApprovalProduct: View = findViewById(R.id.btn_approval_product)
        btnApprovalProduct.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }

        if (LoginInfoUtil.getMemberCode(this) == Constants.ROLE_SELL) {
            btnAddProduct.visibility = View.VISIBLE
            btnApprovalProduct.visibility = View.GONE
        } else if (Constants.SYSTEM_TYPE == 2) {
            btnAddProduct.visibility = View.GONE
            btnApprovalProduct.visibility = View.VISIBLE
        }


    }

    private fun handleAddProductClick() {
        val userId = LoginInfoUtil.getUserId(this)
        showProgressBar()

        lifecycleScope.launch {
            try {

                // 1) 기본 중간센터 조회 (Long? 반환)
                val defaultWholesalerNo = appService.getDefaultWholesaler(userId)
                if (defaultWholesalerNo != null) {
                    // 이미 지정됨 → 바로 이동
                    moveToMakeAD()
                    return@launch
                }

                // 2) 기본센터 없음 → 도매상(중간센터) 목록 불러오기
                val wholesalers = appService.getWholesalers(Constants.ROLE_PROJ)
                val centers = wholesalers.map { it.toCenterDto() }

                if (centers.isEmpty()) {
                    Toast.makeText(this@DashboardActivity, "선택 가능한 중간센터가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 3) 센터 선택 다이얼로그
                BottomDtoPickerSheet.new(
                    centers = centers,
                    title = "센터/도매상 선택",
                    onPicked = { picked ->
                        lifecycleScope.launch {
                            showProgressBar()
                            try {
                                val ok = appService.setDefaultWholesaler(userId, picked.code)
                                if (ok) {
                                    Toast.makeText(this@DashboardActivity,"기본 중간센터 지정 완료",Toast.LENGTH_SHORT).show()
                                    moveToMakeAD()
                                } else {
                                    Toast.makeText(this@DashboardActivity,"센터 지정 실패",Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(this@DashboardActivity,"센터 지정 중 오류가 발생했습니다.",Toast.LENGTH_SHORT).show()
                            } finally {
                                hideProgressBar()
                            }
                        }
                    }
                ).show(supportFragmentManager, "center_picker")

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DashboardActivity, "처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                hideProgressBar()
            }
        }
    }
    private fun OpUserVO.toCenterDto(): BottomDto =
        BottomDto(
            code = this.userNo.toString() ?: "",
            name = this.userNm +"("+ this.userNo +")",
            text1 = null,
            text2 = null,
            text3 = null,
            text4 = null,
        )

    private fun moveToMakeAD() {
        val intent = Intent(this, KtMakeADMainActivity::class.java).apply {
            putExtra(KtMakeADDetailView.STR_PUT_AD_IDX, "")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
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

    private fun initDashboard() {
        val token = TokenUtil.getToken(this)
        loadDashboardStats(token)
        loadRecentProducts(token)
    }

    private fun loadDashboardStats(token: String) {
        showProgressBar()
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
        findViewById<TextView>(R.id.tv_stats).text = "승인반려:${stats["reguestCount"] ?: 0}건 / 처리 중: ${stats["processingCount"] ?: 0}건 / 완료: ${stats["completedCount"] ?: 0}건"
    }

    private fun loadRecentProducts(token: String) {
        showProgressBar()
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
            } finally {
                hideProgressBar()
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
    private fun showProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        badge = NotificationBadgeHelper.attach(
            activity = this,
            menu = menu,
            toolbar = toolbar,              // ✅ Toolbar 전달
            menuItemId = R.id.action_notifications
        )
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
        return true
    }

    override fun onResume() {
        super.onResume()
        initDashboard()
        // 돌아왔을 때 뱃지 다시 갱신
        NotificationBadgeHelper.refresh(this, lifecycleScope, badge)
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
