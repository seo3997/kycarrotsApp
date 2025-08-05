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
import com.whomade.kycarrots.common.AppServiceProvider.instance
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.ChatRoomResponse
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.ad.makead.MakeADDetail1
import com.whomade.kycarrots.ui.ad.makead.MakeADMainActivity
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import kotlinx.coroutines.launch

class AdDetailActivity : AppCompatActivity() {
    private lateinit var productIdStr: String
    private lateinit var productUserId: String
    private lateinit var wholesalerId  : String
    private lateinit var spinner: AppCompatSpinner
    private var currentStatus: String? = null
    private lateinit var filteredList: List<TxtListDataInfo>  // ← 전역 선언 필요
    private lateinit var statusTextView: TextView // ← 추가

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

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

        val btn_edit_product: View = findViewById(R.id.btn_edit_product)
        btn_edit_product.setOnClickListener {
            val intent = Intent(this, MakeADMainActivity::class.java)
            intent.putExtra(MakeADDetail1.STR_PUT_AD_IDX, productIdStr) // 현재 ID 전달
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }


    }


    private fun showProductDetail(detail: ProductDetailResponse) {
        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        collapsingToolbar.title = detail.product.title

        wholesalerId = detail.product.wholesalerId                                                  //도매상ID

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
                    val apiList = instance.getCodeList("R010630")
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
                val apiList = instance.getCodeList("R010630")

                filteredList = apiList.filter {
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
        val message = if (rejectReason != null) {
            "상태를 \"$label\"(으)로 변경하고 아래 사유를 저장하시겠습니까?\n\n사유: $rejectReason"
        } else {
            "상태를 \"$label\"(으)로 변경하시겠습니까?"
        }

        AlertDialog.Builder(this)
            .setTitle("상태 변경 확인")
            .setMessage(message)
            .setPositiveButton("확인") { _, _ ->
                //updateProductStatus(code, rejectReason)
            }
            .setNegativeButton("취소") { _, _ ->
                restoreSpinnerSelection()
            }
            .show()
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
        menuInflater.inflate(R.menu.sample_actions, menu)
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

        lifecycleScope.launch {
            try {
                val detail = appService.getProductDetail(productId)
                if (detail != null) {
                    showProductDetail(detail)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                supportFinishAfterTransition()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
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
        val buyerId = if (isBuyer) sUID else ""     //구매자
        val sellerId = productUserId                 //판매자
        val productId = productIdStr                  //상품ID

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
                val sellerId = wholesalerId
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


}