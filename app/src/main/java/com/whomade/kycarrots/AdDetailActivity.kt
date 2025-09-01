/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.card.MaterialCardView
import com.whomade.kycarrots.Cheeses.randomCheeseDrawable
import com.whomade.kycarrots.chatting.ChatActivity
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.ChatRoomResponse
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductItem
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.ad.makead.MakeADDetail1
import com.whomade.kycarrots.ui.ad.makead.MakeADMainActivity
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.TokenUtil
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import kotlinx.coroutines.launch
import androidx.core.view.WindowInsetsCompat
import com.whomade.kycarrots.data.model.ChatBuyerDto
import com.whomade.kycarrots.data.model.InterestRequest
import com.whomade.kycarrots.ui.dialog.SelectOption
import com.whomade.kycarrots.ui.dialog.SelectOptionDialogFragment

class AdDetailActivity : AppCompatActivity() {
    private lateinit var productIdStr: String
    private lateinit var productUserId: String
    private lateinit var wholesalerId  : String
    private lateinit var spinner: AppCompatSpinner
    private var currentStatus: String? = null
    private lateinit var filteredList: List<TxtListDataInfo>  // ← 전역 선언 필요
    private lateinit var statusList: List<TxtListDataInfo>  // ← 전역 선언 필요
    private lateinit var statusTextView: TextView // ← 추가
    private var memberCode: String? = null   // ← 현재 사용자 권한 저장
    private var isFav: Boolean = false
    private lateinit var btnEditProduct: View
    private var statusChanged = false
    private var newStatus: String? = null
    private var selectedBuyerForCompletion: ChatBuyerDto? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        memberCode = LoginInfoUtil.getMemberCode(this)
        btnEditProduct = findViewById(R.id.btn_edit_product)


        val intent = intent
        productIdStr = intent.getStringExtra(EXTRA_PRODUCT_ID) ?: "0"
        val productId = productIdStr.toLongOrNull()
        if (productId == null || productId <= 0) {
            finish() // 유효하지 않은 ID는 종료
            return
        }
        productUserId = intent.getStringExtra(EXTRA_USER_ID) ?: "0"


        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        val appService = AppService(repository)

        loadProductDetail(productId)

        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        //collapsingToolbar.title = cheeseName
        //loadBackdrop()

        val fab: View = findViewById(R.id.fab_send)
        fab.setOnClickListener {
            when (Constants.SYSTEM_TYPE) {
                1 -> handleFabClickForSystemType1()
                2 -> handleFabClickForSystemType2()
                else -> showToast("지원하지 않는 시스템 유형입니다.")
            }

        }

        val isSeller = (memberCode == "ROLE_SELL")
        btnEditProduct.visibility = if (isSeller) View.VISIBLE else View.GONE
        btnEditProduct.setOnClickListener {
            val intent = Intent(this, MakeADMainActivity::class.java)
            intent.putExtra(MakeADDetail1.STR_PUT_AD_IDX, productIdStr) // 현재 ID 전달
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }


        supportFragmentManager.setFragmentResultListener(
            SelectOptionDialogFragment.RESULT_KEY,
            this
        ) { _, bundle ->
            when {
                bundle.getBoolean(SelectOptionDialogFragment.RESULT_NONE, false) -> {
                    // "선택 안함" 눌렀을 때 처리
                    selectedBuyerForCompletion = null
                    showStatusChangeConfirmDialog("구매확정", "99", rejectReason = null)
                }
                bundle.getBoolean(SelectOptionDialogFragment.RESULT_CANCELED, false) -> {
                    restoreSpinnerSelection()
                }
                else -> {
                    val selected = bundle.getParcelable<SelectOption>(
                        SelectOptionDialogFragment.RESULT_ITEM
                    )
                    if (selected != null) {
                        // ✅ 여기서 selected.name == labels 의 역할
                        //    selected.code == buyerId
                        selectedBuyerForCompletion = ChatBuyerDto(
                            roomId     = selected.code3,           // 예: roomId
                            productId  = selected.code6.toLongOrNull() ?: 0L, // 예: productId
                            sellerId   = selected.code4,           // 예: sellerId
                            buyerId    = selected.code1,           // 예: buyerId
                            buyerNo    = selected.code1.toLongOrNull() ?: 0L,   // 필요 없으면 0L
                            buyerNm    = selected.name,            // 표시용 이름
                            sellerNo   = selected.code5.toLongOrNull() ?: 0L,
                            sellerNm   = ""                        // 필요시 추가
                        )
                        showStatusChangeConfirmDialog("구매확정", "99", rejectReason = null)
                    }
                }
            }
        }
    }


    private fun showProductDetail(detail: ProductDetailResponse) {
        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        collapsingToolbar.title = detail.product.title

        wholesalerId = detail.product.wholesalerId                                                  //도매상ID
        isFav = detail.product.fav == "1"


        val descriptionTextView: TextView = findViewById(R.id.product_description)
        // 설명 텍스트 표시
        descriptionTextView.text = detail.product.description ?: "설명이 없습니다"

        val priceTextView: TextView = findViewById(R.id.product_price)
        val priceLong = detail.product.price?.toDoubleOrNull()?.toLong() ?: 0L
        val formattedPrice = String.format("%,d원", priceLong)
        priceTextView.text = "가격: $formattedPrice"


        val shippingDateTextView: TextView = findViewById(R.id.product_shipping_date)
        shippingDateTextView.text = "희망출하일: ${detail.product.desiredShippingDate}"

        val quantityTextView: TextView = findViewById(R.id.product_quantity_unit)

        val rawQty = detail.product.quantity.toLongOrNull() ?: 0L
        val formattedQty = String.format("%,d", rawQty)
        quantityTextView.text = "수량: ${formattedQty} ${detail.product.unitCodeNm}"

        val categoryTextView: TextView = findViewById(R.id.product_category_name)
        categoryTextView.text =
            "카테고리: ${detail.product.categoryMidNm} > ${detail.product.categorySclsNm}"

        val regionTextView: TextView = findViewById(R.id.product_region_name)
        regionTextView.text = "지역: ${detail.product.areaMidNm}  ${detail.product.areaSclsNm}"


        // 대표 이미지 (represent == 1)
        /*
        val imageView: ImageView = findViewById(R.id.backdrop)
        val mainImageUrl = detail.imageMetas.firstOrNull { it.represent == "1" }?.imageUrl
        if (mainImageUrl != null) {
            Glide.with(this)
                .load(mainImageUrl)
                .apply(RequestOptions.centerCropTransform())
                .into(imageView)
        }
         */

        val imageView: ImageView = findViewById(R.id.backdrop)
        val mainImageUrl = detail.imageMetas.firstOrNull { it.represent == "1" }?.imageUrl

        // 공유 요소 전환을 위해 transition 일시 지연
        postponeEnterTransition()

        Glide.with(this)
            .load(mainImageUrl)
            .apply(
                RequestOptions.centerCropTransform()
                    .placeholder(R.color.colorRPrimary)  // 여기
            )
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }
            })
            .into(imageView)
        // 서브 이미지 (represent == 0)
        val imageCardView = findViewById<MaterialCardView>(R.id.image_card_view)
        val subImages = detail.imageMetas.filter { it.represent == "0" }.take(3)

        if (subImages.isEmpty()) {
            imageCardView.visibility = View.GONE
        } else {
            imageCardView.visibility = View.VISIBLE
            val imageViews = listOf(
                findViewById<ImageView>(R.id.image_sub_1),
                findViewById<ImageView>(R.id.image_sub_2),
                findViewById<ImageView>(R.id.image_sub_3)
            )
            for (i in imageViews.indices) {
                if (i < subImages.size) {
                    val imageUrl = subImages[i].imageUrl
                    Glide.with(this)
                        .load(imageUrl)
                        .apply(RequestOptions.centerCropTransform())
                        .into(imageViews[i])
                    imageViews[i].visibility = View.VISIBLE
                } else {
                    imageViews[i].visibility = View.GONE
                }
            }
        }
        // TODO: 나머지 detail.product.description, price 등도 TextView에 연결 가능
        /*
        val spinner = findViewById<AppCompatSpinner>(R.id.spinner_product_status)
        val statusList = listOf("승인요청","판매중", "수정요청")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStatus = statusList[position]
                Log.d("ProductDetail", "선택된 상태: $selectedStatus")
                // 서버에 상태 업데이트 로직 연결 예정
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
         */
        currentStatus = detail.product.saleStatus
        loadProductStatusOptions( Constants.SYSTEM_TYPE, currentStatus)
        invalidateOptionsMenu()
    }

    private fun loadProductStatusOptions(systemType: Int, currentStatus: String?) {
        spinner = findViewById(R.id.spinner_product_status)
        statusTextView = findViewById(R.id.text_product_status)

        val memberCode = LoginInfoUtil.getMemberCode(this)

        val isReadonly =
            (memberCode == "ROLE_PUB") || // 구매자
                    (systemType == 2 && memberCode == "ROLE_SELL" && currentStatus != "98") // 도매상 시스템의 판매자이고 반려상태가 아님

        if (isReadonly) {
            // 상태만 보여주기
            spinner.visibility = View.GONE
            statusTextView.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val apiList = AppServiceProvider.getService().getCodeList("R010630")
                    val label = apiList.find { it.strIdx == currentStatus }?.strMsg ?: "알 수 없음"
                    statusTextView.text = "현재 상태: $label"
                } catch (e: Exception) {
                    statusTextView.text = "현재 상태: 알 수 없음"
                }
            }
            return
        }

        // 이 아래는 Spinner 표시 및 상태 변경 가능한 경우
        spinner.visibility = View.VISIBLE
        statusTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                statusList = AppServiceProvider.getService().getCodeList("R010630")

                filteredList = statusList.filter {
                    when {
                        systemType == 1 && memberCode == "ROLE_SELL" ->
                            it.strIdx in listOf("1", "10", "99") || it.strIdx == currentStatus
                        systemType == 2 && memberCode == "ROLE_PROJ" ->
                            it.strIdx in listOf("0", "1", "10", "98", "99") || it.strIdx == currentStatus
                        systemType == 2 && memberCode == "ROLE_SELL" ->
                            // 반려 상태인 경우 승인요청만 허용
                            it.strIdx in listOf("0", "98", "1", "10", "99")
                        else -> false
                    }
                }.distinctBy { it.strIdx }

                val names = filteredList.map { it.strMsg }

                val adapter = ArrayAdapter(
                    this@AdDetailActivity,
                    android.R.layout.simple_spinner_item,
                    names
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                currentStatus?.let {
                    val index = filteredList.indexOfFirst { code -> code.strIdx == it }
                    if (index != -1) spinner.setSelection(index)
                }

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    var initialized = false
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        if (!initialized) {
                            initialized = true
                            return
                        }
                        val selectedLabel = names[position]
                        val selectedCode = filteredList[position].strIdx

                        if (selectedCode == currentStatus) return
                        handleStatusChange(selectedLabel, selectedCode)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            } catch (e: Exception) {
                Toast.makeText(this@AdDetailActivity, "상품 상태 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleStatusChange(label: String, code: String) {
        val memberCode = LoginInfoUtil.getMemberCode(this)
        val systemType  = Constants.SYSTEM_TYPE
        val canChange = when {
            systemType == 1 && memberCode == "ROLE_SELL" -> code in listOf("1", "10", "99")
            systemType == 2 && memberCode == "ROLE_PROJ" -> code in listOf("1", "10", "98", "99") // 예: 승인요청, 반려
            systemType == 2 && memberCode == "ROLE_SELL" ->
                currentStatus == "98" && code == "0" // 반려 → 승인요청만 허용
            else -> false
        }

        if (!canChange) {
            Toast.makeText(this, "이 상태에서는 변경할 수 없습니다.", Toast.LENGTH_SHORT).show()
            restoreSpinnerSelection()
            return
        }

        if (code == "99") {
            maybePickBuyerThenConfirm(label, code)
            return
        }

        if (currentStatus == "0" && code == "98") {
            // 승인요청 → 반려
            showRejectReasonDialog { reason ->
                showStatusChangeConfirmDialog(label, code, reason)
            }
        } else {
            // 그 외 상태 변경
            showStatusChangeConfirmDialog(label, code, null)
        }
    }

    private fun maybePickBuyerThenConfirm(label: String, code: String) {
        val pid = productIdStr.toLongOrNull()
        val sellerId = resolveSellerId() // 로그인한 내 ID

        if (pid == null) {
            Toast.makeText(this, "상품 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
            restoreSpinnerSelection()
            return
        }
        if (sellerId.isBlank()) {
            Toast.makeText(this, "로그인 정보를 확인해주세요.", Toast.LENGTH_SHORT).show()
            restoreSpinnerSelection()
            return
        }

        lifecycleScope.launch {
            showLoading(true)
            try {
                // ✅ 서버에서 구매자 목록 가져오기
                val buyers = AppServiceProvider.getService().getChatBuyers(pid, sellerId)

                if (buyers.isEmpty()) {
                    // 구매자 없음 → 바로 확인 다이얼로그(= 상태만 변경)
                    selectedBuyerForCompletion = null
                    showStatusChangeConfirmDialog(label, code, rejectReason = null)
                } else {
                    // 구매자 있음 → 목록에서 선택해야 진행
                    /*
                    val labels = buyers.mapIndexed { i, b -> "${i+1}. ${b.buyerNm} (${b.buyerId})" }.toTypedArray()
                    AlertDialog.Builder(this@AdDetailActivity)
                        .setTitle("판매완료 처리 — 구매자 선택")
                        .setItems(labels) { _, which ->
                            selectedBuyerForCompletion = buyers[which]
                            showStatusChangeConfirmDialog(label, code, rejectReason = null)
                        }
                        .setNegativeButton("취소") { _, _ ->
                            restoreSpinnerSelection()
                        }
                        .show()
                     */
                    val options = ArrayList(
                        buyers.map { b ->
                            SelectOption(
                                code1 = b.buyerId,   // 내부적으로 사용할 코드
                                code2 = b.buyerNo.toString(),
                                code3 = b.roomId,
                                code4 = b.sellerId,
                                code5 = b.sellerNo.toString(),
                                code6 = b.productId.toString(),
                                name =  b.buyerId+"/"+b.buyerNm    // 다이얼로그에 표시될 라벨
                            )
                        }
                    )
                    SelectOptionDialogFragment
                        .newInstance(
                            title = "판매완료 처리 — 구매자 선택",
                            options = options,   // name이 곧 labels
                            numbered = true,      // 번호 붙일지 여부
                            showNone = true
                        )
                        .show(supportFragmentManager, "SelectOptionDialog")
                }
            } catch (e: Exception) {
                // 에러 시에도 구매자 없이 진행(상태만 변경)
                selectedBuyerForCompletion = null
                showStatusChangeConfirmDialog(label, code, rejectReason = null)
            } finally {
                showLoading(false)
            }
        }
    }
    private fun showRejectReasonDialog(onReasonEntered: (String) -> Unit) {
        val editText = EditText(this).apply {
            hint = "반려 사유를 입력하세요"
        }

        AlertDialog.Builder(this)
            .setTitle("반려 사유 입력")
            .setView(editText)
            .setPositiveButton("확인") { _, _ ->
                val reason = editText.text.toString().trim()
                if (reason.isNotEmpty()) {
                    onReasonEntered(reason)
                } else {
                    Toast.makeText(this, "반려 사유를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showStatusChangeConfirmDialog(label: String, code: String, rejectReason: String?) {
        val buyer = if (code == "99") selectedBuyerForCompletion else null
        val buyerLine = buyer?.let { "\n\n선택한 구매자: ${it.buyerNm}" } ?: ""

        val message = if (rejectReason != null) {
            "상태를 \"$label\"(으)로 변경하고 아래 사유를 저장하시겠습니까?\n\n사유: $rejectReason$buyerLine"
        } else {
            "상태를 \"$label\"(으)로 변경하시겠습니까?$buyerLine"
        }

        AlertDialog.Builder(this)
            .setTitle("상태 변경 확인")
            .setMessage(message)
            .setPositiveButton("확인") { _, _ ->
                lifecycleScope.launch {
                    val (ok, msg) = createPurchaseIfNeeded(code, buyer)
                    if (!ok && !msg.isNullOrBlank()) {
                        Toast.makeText(this@AdDetailActivity, msg, Toast.LENGTH_SHORT).show()
                    }                    // 🔸 최종 상태 변경
                    updateProductStatus(code, rejectReason)
                }
            }
            .setNegativeButton("취소") { _, _ ->
                restoreSpinnerSelection()
            }
            .show()
    }

    private suspend fun createPurchaseIfNeeded(
        code: String,
        buyer: ChatBuyerDto?
    ): Pair<Boolean, String?> {
        if (code != "99" || buyer == null) return true to null

        val pid = productIdStr.toLongOrNull()
            ?: return false to "상품 ID가 유효하지 않습니다."
        val sellerNo = LoginInfoUtil.getUserNo(this).toLongOrNull()

        return try {
            AppServiceProvider.getService().createPurchase(
                productId = pid,
                buyerNo   = buyer.buyerNo,
                roomId    = buyer.roomId,
                sellerNo  = buyer.sellerNo
            )
        } catch (e: Exception) {
            false to (e.message ?: "구매이력 생성 중 오류")
        }
    }
    private fun updateProductStatus(code: String, rejectReason: String?) {
        val token = TokenUtil.getToken(this)
        val productId = productIdStr
        showLoading(true)
        lifecycleScope.launch {
            try {

                val productItem = ProductItem(
                    productId = productId,
                    saleStatus = code,
                    updusrNo = 0,
                    rejectReason = rejectReason
                )

                val success = AppServiceProvider.getService().updateProductStatus(token,productItem)
                val statusName = getStatusName(code)
                if (success) {
                    Toast.makeText(this@AdDetailActivity, "상태가 \"$statusName\"(으)로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AdDetailActivity, "상태 변경 실패", Toast.LENGTH_SHORT).show()
                    restoreSpinnerSelection()
                }
                newStatus    = code
                statusChanged = true

            } catch (e: Exception) {
                Toast.makeText(this@AdDetailActivity, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                restoreSpinnerSelection()
            } finally {
                showLoading(false)
            }
        }
    }
    private fun maybeSetResultAndFinish() {
        if (statusChanged && newStatus in listOf("1", "10", "99")) {
            setResult(
                Activity.RESULT_OK,
                Intent()
                    .putExtra("status_changed", true)
                    .putExtra("new_status", newStatus)
            )
        }
        supportFinishAfterTransition()
    }

    private fun restoreSpinnerSelection() {
        currentStatus?.let { status ->
            val index = filteredList.indexOfFirst { it.strIdx == status }
            if (index != -1) {
                spinner.setSelection(index)
            }
        }
    }
    private fun loadBackdrop() {
        val imageView: ImageView = findViewById(R.id.backdrop)
        Glide.with(imageView)
            .load(randomCheeseDrawable)
            .apply(RequestOptions.centerCropTransform())
            .into(imageView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //menuInflater.inflate(R.menu.sample_actions, menu)
        menuInflater.inflate(R.menu.menu_ad_detail, menu)
        val favItem = menu.findItem(R.id.action_favorite)
        val isBuyer = (memberCode == "ROLE_PUB")

        // 구매자만 보이도록
        favItem.isVisible = isBuyer

        // 보일 때만 아이콘 상태 반영
        if (isBuyer) {
            favItem.setIcon(if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_border)
            // 필요 시 틴트 강제
            // favItem.icon?.mutate()?.setTint(ContextCompat.getColor(this, android.R.color.white))
        }
        return true
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
        const val EXTRA_USER_ID = "user_id"
    }

    private fun loadProductDetail(productId: Long) {
        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        val appService = AppService(repository)
        val userNo = LoginInfoUtil.getUserNo(this).toLongOrNull()?: return
        showLoading(true)
        lifecycleScope.launch {
            try {
                val detail = appService.getProductDetail(productId,userNo)
                if (detail != null) {
                    showProductDetail(detail)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                showLoading(false)
            }        }
    }

    override fun onResume() {
        super.onResume()

        val productId = productIdStr.toLongOrNull()
        if (productId != null && productId > 0) {
            loadProductDetail(productId)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                maybeSetResultAndFinish()
                true
            }
            R.id.action_favorite -> {
                if (memberCode == "ROLE_PUB") {
                    toggleFavorite(item)
                } else {
                    Toast.makeText(this, "구매자만 찜하기가 가능합니다", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun toggleFavorite(menuItem: MenuItem) {
        val userNo = LoginInfoUtil.getUserNo(this).toLongOrNull()?: return
        val productId = productIdStr.toLongOrNull() ?: return

        lifecycleScope.launch {
            showLoading(true)
            try {
                val req = InterestRequest(userNo = userNo, productId = productId)
                val resp = AppServiceProvider.getService().toggleInterest(req)

                if (resp) {
                    isFav =!isFav
                    menuItem.setIcon(if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_border)

                    setResult(
                        Activity.RESULT_OK,
                        Intent().apply {
                            putExtra("productId", productIdStr)    // String
                            putExtra("isInterested", isFav)         // Boolean
                        }
                    )
                    Toast.makeText(
                        this@AdDetailActivity,
                        if (isFav) "관심상품에 추가되었습니다" else "관심상품에서 제거되었습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this@AdDetailActivity, "서버 오류로 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdDetailActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    override fun onBackPressed() {
        maybeSetResultAndFinish()
    }

    private fun handleFabClickForSystemType1() {
        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val sUID = prefs.getString("LogIn_ID", "") ?: ""
        val sMemberCode = prefs.getString("LogIn_MEMBERCODE", "") ?: ""
        val isBuyer = sMemberCode == "ROLE_PUB"
        val systemType = Constants.SYSTEM_TYPE

        // SYSTEM_TYPE 체크
        if (systemType != 1) {
            showToast("SYSTEM_TYPE 1 전용 로직입니다.")
            return
        }

        // 값 설정
        val buyerId = if (isBuyer) sUID else ""         //구매자
        val sellerId  = resolveSellerId()               //판매자
        val productId = productIdStr                    //상품ID

        if (isBuyer) {
            // 구매자 → 채팅방 생성 요청
            createOrGetRoomFromServer(productId, buyerId, sellerId)
        } else {
            // 판매자 → 채팅방 목록 확인
            fetchRoomListForSeller(productId, sellerId)
        }
    }


    private fun handleFabClickForSystemType2() {
        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val myId = prefs.getString("LogIn_ID", "") ?: ""
        val myRole = prefs.getString("LogIn_MEMBERCODE", "") ?: ""
        val productId = productIdStr

        when (myRole) {
            "ROLE_PUB" -> {
                // 구매자 → 도매상과 채팅 생성
                val buyerId = myId
                val sellerId = resolveSellerId()
                createOrGetRoomFromServer(productId, buyerId, sellerId)
            }

            "ROLE_SELL" -> {
                // 판매자 → 도매상과 채팅방 입장 (생성 불가)
                val sellerId = myId
                fetchRoomListForSeller(productId, sellerId)
            }

            "ROLE_PROJ" -> {
                // 도매상 → 구매자 or 판매자 판별
                val options = arrayOf("판매자에게 채팅", "구매자에게 채팅")
                AlertDialog.Builder(this)
                    .setTitle("채팅 대상 선택")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                // 도매상 → 판매자: 채팅방 생성 가능
                                val targetBuyerId = myId
                                val targetSellerId = productUserId
                                createOrGetRoomFromServer(productId, targetBuyerId, targetSellerId)
                            }
                            1 -> {
                                // 도매상 본인이 sellerId인 채팅 목록 조회
                                fetchRoomListForSeller(productId, myId)
                            }
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }

            else -> {
                Toast.makeText(this, "알 수 없는 사용자 역할입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this@AdDetailActivity, message, Toast.LENGTH_SHORT).show()
    }


    private fun createOrGetRoomFromServer(productId: String, buyerId: String, sellerId: String) {
        val appService = AppServiceProvider.getService()

        lifecycleScope.launch {
            try {
                val chatRoom = appService.createOrGetChatRoom(productId, buyerId, sellerId)
                if (chatRoom != null) {
                    openChatActivity(chatRoom.roomId,buyerId,sellerId,productId)
                } else {
                    Toast.makeText(this@AdDetailActivity, "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("FAB", "채팅방 생성 중 오류", e)
                Toast.makeText(this@AdDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchRoomListForSeller(productId: String, sellerId: String) {
        val appService = AppServiceProvider.getService()

        lifecycleScope.launch {
            try {
                val chatRooms = appService.getUserChatRooms(productId,sellerId)
                //val chatRooms = allChatRooms.filter { it.productId == productId }

                when {
                    chatRooms.isEmpty() -> {
                        Toast.makeText(
                            this@AdDetailActivity,
                            "이 상품에 대한 채팅 요청이 없습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    chatRooms.size == 1 -> {
                        val selectedRoom = chatRooms[0]
                        openChatActivity(
                            selectedRoom.roomId,
                            selectedRoom.buyerId,
                            selectedRoom.sellerId,
                            selectedRoom.productId
                        )
                    }

                    else -> {
                        // 여러 명의 구매자 중 선택해야 하는 경우 처리
                        showBuyerSelectionDialog(chatRooms)
                    }
                }
            } catch (e: Exception) {
                Log.e("FAB", "채팅방 조회 중 오류", e)
                Toast.makeText(this@AdDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBuyerSelectionDialog(chatRooms: List<ChatRoomResponse>) {
        val buyerLabels = chatRooms.mapIndexed { index, room ->
            "구매자 ${index + 1}: ${room.buyerId}"
        }

        AlertDialog.Builder(this)
            .setTitle("구매자를 선택하세요")
            .setItems(buyerLabels.toTypedArray()) { _, which ->
                val selectedRoom = chatRooms[which]
                openChatActivity(
                    selectedRoom.roomId,
                    selectedRoom.buyerId,
                    selectedRoom.sellerId,
                    selectedRoom.productId
                )
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun openChatActivity(
        roomId: String,
        buyerId: String,
        sellerId: String,
        productId: String
    ) {
        val intent = Intent(this@AdDetailActivity, ChatActivity::class.java).apply {
            putExtra("roomId", roomId)
            putExtra("buyerId", buyerId)
            putExtra("sellerId", sellerId)
            putExtra("productId", productId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        findViewById<View>(R.id.ll_progress_circle).visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun resolveSellerId(): String {
        return when (Constants.SYSTEM_TYPE) {
            1 -> productUserId
            2 -> wholesalerId
            else -> productUserId // 안전 기본값
        }
    }

    private fun getStatusName(code: String): String {
        return statusList.find { it.strIdx == code }?.strMsg ?: code
    }

}