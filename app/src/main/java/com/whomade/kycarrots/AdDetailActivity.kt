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
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.card.MaterialCardView
import com.whomade.kycarrots.Cheeses.randomCheeseDrawable
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.ad.makead.MakeADDetail1
import com.whomade.kycarrots.ui.ad.makead.MakeADMainActivity
import kotlinx.coroutines.launch
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.whomade.kycarrots.chatting.ChatActivity
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.data.model.ChatRoomResponse
import com.whomade.kycarrots.domain.service.AppServiceProvider

class AdDetailActivity : AppCompatActivity() {
    private lateinit var productIdStr: String
    private lateinit var productUserId: String

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
            handleFabClickForSystemType1()

        }

        val btn_edit_product: View = findViewById(R.id.btn_edit_product)
        btn_edit_product.setOnClickListener {
            val intent = Intent(this, MakeADMainActivity::class.java)
            intent.putExtra(MakeADDetail1.STR_PUT_AD_IDX, productIdStr) // 현재 ID 전달
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }


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

    private fun showToast(message: String) {
        Toast.makeText(this@AdDetailActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun showProductDetail(detail: ProductDetailResponse) {
        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        collapsingToolbar.title = detail.product.title

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
                val allChatRooms = appService.getUserChatRooms(sellerId)
                val chatRooms = allChatRooms.filter { it.productId == productId }

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