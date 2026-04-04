/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.whomade.kycarrots

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.whomade.kycarrots.chatting.ChatActivity
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.ChatBuyerDto
import com.whomade.kycarrots.data.model.ChatRoomResponse
import com.whomade.kycarrots.data.model.InterestRequest
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductItem
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.ad.ImageViewerActivity
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.TokenUtil
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import com.whomade.kycarrots.ui.dialog.SelectOption
import com.whomade.kycarrots.ui.dialog.SelectOptionDialogFragment
import kotlinx.coroutines.launch

class AdDetailActivity : AppCompatActivity() {
    private lateinit var productIdStr: String
    private lateinit var branchId: String
    private lateinit var spinner: AppCompatSpinner
    private var currentStatus: String? = null
    private lateinit var filteredList: List<TxtListDataInfo>
    private lateinit var statusList: List<TxtListDataInfo>
    private lateinit var statusTextView: TextView
    private var memberCode: String? = null
    private var isFav: Boolean = false

    private var statusChanged = false
    private var newStatus: String? = null
    private var selectedBuyerForCompletion: ChatBuyerDto? = null
    private var currentProductDetail: ProductDetailResponse? = null

    val viewModel: AdDetailViewModel by viewModels()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 1. ViewModel의 로딩 상태 관찰 (Fragment 등에서 발생하는 로딩까지 처리)
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        memberCode = LoginInfoUtil.getMemberCode(this)

        val intent = intent
        productIdStr = intent.getStringExtra(EXTRA_PRODUCT_ID) ?: "0"
        val productId = productIdStr.toLongOrNull()
        if (productId == null || productId <= 0) {
            finish()
            return
        }

        // 초기 데이터 로드
        loadProductDetail(productId)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fab: View = findViewById(R.id.fab_send)
        fab.setOnClickListener {
            handleFabClickForSystemType2()
        }

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val pagerAdapter = AdDetailPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "상품상세"
                1 -> "상품리뷰"
                else -> "상품문의"
            }
        }.attach()

        // 구매자 선택 다이얼로그 결과 리스너
        supportFragmentManager.setFragmentResultListener(
            SelectOptionDialogFragment.RESULT_KEY,
            this
        ) { _, bundle ->
            when {
                bundle.getBoolean(SelectOptionDialogFragment.RESULT_NONE, false) -> {
                    selectedBuyerForCompletion = null
                    showStatusChangeConfirmDialog("구매확정", "99")
                }
                bundle.getBoolean(SelectOptionDialogFragment.RESULT_CANCELED, false) -> {
                    restoreSpinnerSelection()
                }
                else -> {
                    val selected = bundle.getParcelable<SelectOption>(SelectOptionDialogFragment.RESULT_ITEM)
                    if (selected != null) {
                        selectedBuyerForCompletion = ChatBuyerDto(
                            roomId     = selected.code3,
                            productId  = selected.code6.toLongOrNull() ?: 0L,
                            branchId   = selected.code4,
                            buyerId    = selected.code1,
                            buyerNo    = selected.code2.toLongOrNull() ?: 0L,
                            buyerNm    = selected.name,
                            sellerNo   = selected.code5.toLongOrNull() ?: 0L,
                            sellerNm   = ""
                        )
                        showStatusChangeConfirmDialog("구매확정", "99")
                    }
                }
            }
        }
    }

    private fun loadProductDetail(productId: Long) {
        val userNo = LoginInfoUtil.getUserNo(this).toLongOrNull() ?: return
        showLoading(true)
        lifecycleScope.launch {
            try {
                val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
                val repository = RemoteRepository(adApi)
                val appService = AppService(repository)

                val detail = appService.getProductDetail(productId, userNo)
                if (detail != null) {
                    viewModel.setProductDetail(detail)
                    showProductDetail(detail)
                }
            } catch (e: Exception) {
                Log.e("AdDetail", "Error loading detail", e)
                Toast.makeText(this@AdDetailActivity, "데이터 로딩 실패", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showProductDetail(detail: ProductDetailResponse) {
        currentProductDetail = detail
        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        collapsingToolbar.title = detail.product.title

        branchId = detail.product.branchId
        isFav = detail.product.fav == "1"
        currentStatus = detail.product.saleStatus

        val imageView: ImageView = findViewById(R.id.backdrop)
        val mainImageUrl = detail.imageMetas.firstOrNull { it.represent == "1" }?.imageUrl

        imageView.setOnClickListener {
            mainImageUrl?.let { openImageViewer(it) }
        }

        if (mainImageUrl.isNullOrBlank()) {
            imageView.setImageResource(R.color.colorRPrimary)
        } else {
            postponeEnterTransition()
            Glide.with(this)
                .load(mainImageUrl)
                .apply(RequestOptions.centerCropTransform().placeholder(R.color.colorRPrimary))
                .listener(object : RequestListener<Drawable> {
                    var retryCount = 0
                    val maxRetries = 3

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        if (retryCount < maxRetries) {
                            retryCount++
                            imageView.postDelayed({
                                Glide.with(this@AdDetailActivity)
                                    .load(mainImageUrl)
                                    .apply(RequestOptions.centerCropTransform().placeholder(R.color.colorRPrimary))
                                    .listener(this)
                                    .into(imageView)
                            }, 1000)
                            return true
                        }
                        startPostponedEnterTransition()
                        return false
                    }
                })
                .into(imageView)
        }
        invalidateOptionsMenu()
    }

    fun restoreSpinnerSelection() {
        val detail = viewModel.productDetail.value ?: return
        viewModel.setProductDetail(detail)
    }

    fun handleStatusChange(label: String, code: String) {
        val memberCode = LoginInfoUtil.getMemberCode(this)
        val canChange = when {
            memberCode == Constants.ROLE_SELL -> code in listOf("0", "1", "10", "20", "30", "99")
            else -> false
        }

        if (!canChange) {
            Toast.makeText(this, "이 상태에서는 변경할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (code == "99") {
            maybePickBuyerThenConfirm(label, code)

        } else {
            showStatusChangeConfirmDialog(label, code)
        }
    }

    private fun maybePickBuyerThenConfirm(label: String, code: String) {
        val pid = productIdStr.toLongOrNull() ?: return
        val branchId = LoginInfoUtil.getBranchId(this)

        lifecycleScope.launch {
            showLoading(true)
            try {
                val buyers = AppServiceProvider.getService().getChatBuyers(pid, branchId)
                if (buyers.isEmpty()) {
                    selectedBuyerForCompletion = null
                    showStatusChangeConfirmDialog(label, code)
                } else {
                    val options = ArrayList(buyers.map { b ->
                        SelectOption(
                            code1 = b.buyerId, code2 = b.buyerNo.toString(),
                            code3 = b.roomId, code4 = b.branchId,
                            code5 = b.sellerNo.toString(), code6 = b.productId.toString(),
                            name = "${b.buyerId}/${b.buyerNm}"
                        )
                    })
                    SelectOptionDialogFragment.newInstance("판매완료 처리 — 구매자 선택", options, true, true)
                        .show(supportFragmentManager, "SelectOptionDialog")
                }
            } catch (e: Exception) {
                showStatusChangeConfirmDialog(label, code)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showStatusChangeConfirmDialog(label: String, code: String) {
        val buyer = if (code == "99") selectedBuyerForCompletion else null
        val message = "상태를 \"$label\"(으)로 변경하시겠습니까?" + (buyer?.let { "\n구매자: ${it.buyerNm}" } ?: "")

        AlertDialog.Builder(this)
            .setTitle("상태 변경 확인")
            .setMessage(message)
            .setPositiveButton("확인") { _, _ ->
                lifecycleScope.launch {
                    val (ok, msg) = createPurchaseIfNeeded(code, buyer)
                    if (!ok && !msg.isNullOrBlank()) Toast.makeText(this@AdDetailActivity, msg, Toast.LENGTH_SHORT).show()
                    updateProductStatus(code)
                }
            }
            .setNegativeButton("취소") { _, _ -> restoreSpinnerSelection() }
            .show()
    }

    private suspend fun createPurchaseIfNeeded(code: String, buyer: ChatBuyerDto?): Pair<Boolean, String?> {
        if (code != "99" || buyer == null) return true to null
        val pid = productIdStr.toLongOrNull() ?: return false to "ID 오류"
        return try {
            AppServiceProvider.getService().createPurchase(pid, buyer.buyerNo, buyer.roomId, buyer.sellerNo)
        } catch (e: Exception) {
            false to (e.message ?: "구매이력 생성 오류")
        }
    }

    private fun updateProductStatus(code: String) {
        val token = TokenUtil.getToken(this)
        showLoading(true)
        lifecycleScope.launch {
            try {
                val productItem = ProductItem(productId = productIdStr, saleStatus = code, updusrNo = 0)
                val success = AppServiceProvider.getService().updateProductStatus(token, productItem)
                if (success) {
                    Toast.makeText(this@AdDetailActivity, "상태가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    currentStatus = code
                    newStatus = code
                    statusChanged = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdDetailActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun toggleFavorite(menuItem: MenuItem) {
        val userNo = LoginInfoUtil.getUserNo(this).toLongOrNull() ?: return
        val productId = productIdStr.toLongOrNull() ?: return
        showLoading(true)
        lifecycleScope.launch {
            try {
                val req = InterestRequest(userNo = userNo, productId = productId)
                if (AppServiceProvider.getService().toggleInterest(req)) {
                    isFav = !isFav
                    menuItem.setIcon(if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_border)
                    setResult(Activity.RESULT_OK, Intent().apply { putExtra("productId", productIdStr); putExtra("isInterested", isFav) })
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_ad_detail, menu)
        val favItem = menu.findItem(R.id.action_favorite)
        val isBuyer = (memberCode == Constants.ROLE_PUB)
        favItem.isVisible = isBuyer
        if (isBuyer) favItem.setIcon(if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_border)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { maybeSetResultAndFinish(); true }
            R.id.action_favorite -> { if (memberCode == Constants.ROLE_PUB) toggleFavorite(item) else Toast.makeText(this, "구매자전용", Toast.LENGTH_SHORT).show(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun maybeSetResultAndFinish() {
        if (statusChanged) {
            setResult(Activity.RESULT_OK, Intent().putExtra("status_changed", true).putExtra("new_status", newStatus))
        }
        supportFinishAfterTransition()
    }

    override fun onBackPressed() { maybeSetResultAndFinish() }

    private fun handleFabClickForSystemType2() {
        val myId = LoginInfoUtil.getUserId(this)
        val myRole = LoginInfoUtil.getMemberCode(this)
        val mybranchId = LoginInfoUtil.getBranchId(this)
        val centerBranchId = Constants.CENTER_BRANCH_ID

        when (myRole) {
            "ROLE_PUB" -> createOrGetRoomFromServer(productIdStr, myId, mybranchId)
            "ROLE_SELL" -> fetchRoomListForSeller(productIdStr, centerBranchId)
            "ROLE_PROJ" -> {
                val options = arrayOf("구매자에게 채팅", "본사와 채팅")
                AlertDialog.Builder(this).setItems(options) { _, which ->
                    if (which == 0) fetchRoomListForSeller(productIdStr, mybranchId)
                    else createOrGetRoomFromServer(productIdStr, mybranchId, centerBranchId)
                }.show()
            }
        }
    }

    private fun createOrGetRoomFromServer(productId: String, buyerId: String, branchId: String) {
        lifecycleScope.launch {
            try {
                val chatRoom = AppServiceProvider.getService().createOrGetChatRoom(productId, buyerId, branchId)
                chatRoom?.let { openChatActivity(it.roomId, buyerId, branchId, productId) }
            } catch (e: Exception) { Toast.makeText(this@AdDetailActivity, "채팅방 오류", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun fetchRoomListForSeller(productId: String, branchId: String) {
        lifecycleScope.launch {
            try {
                val rooms = AppServiceProvider.getService().getUserChatRooms(productId, branchId)
                if (rooms.isEmpty()) Toast.makeText(this@AdDetailActivity, "요청 없음", Toast.LENGTH_SHORT).show()
                else if (rooms.size == 1) openChatActivity(rooms[0].roomId, rooms[0].buyerId, rooms[0].branchId, rooms[0].productId)
                else showBuyerSelectionDialog(rooms)
            } catch (e: Exception) { Toast.makeText(this@AdDetailActivity, "조회 오류", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun showBuyerSelectionDialog(chatRooms: List<ChatRoomResponse>) {
        val labels = chatRooms.map { "구매자: ${it.buyerId}" }.toTypedArray()
        AlertDialog.Builder(this).setTitle("구매자 선택").setItems(labels) { _, which ->
            val r = chatRooms[which]
            openChatActivity(r.roomId, r.buyerId, r.branchId, r.productId)
        }.show()
    }

    private fun openChatActivity(roomId: String, buyerId: String, branchId: String, productId: String) {
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra("roomId", roomId); putExtra("buyerId", buyerId)
            putExtra("branchId", branchId); putExtra("productId", productId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
    }

    private fun showLoading(show: Boolean) {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun getStatusName(code: String): String = statusList.find { it.strIdx == code }?.strMsg ?: code
    private fun openImageViewer(url: String) = startActivity(Intent(this, ImageViewerActivity::class.java).putExtra("url", url))


    companion object { const val EXTRA_PRODUCT_ID = "product_id" }
}