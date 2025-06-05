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
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.whomade.kycarrots.chatting.ChatActivity

class AdDetailActivity : AppCompatActivity() {
    private lateinit var productIdStr: String

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

        val fab: View = findViewById(R.id.fab_edit)
        fab.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
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

        val descriptionTextView: TextView = findViewById(R.id.product_description)
        // 설명 텍스트 표시
        descriptionTextView.text = detail.product.description ?: "설명이 없습니다"

        val priceTextView: TextView = findViewById(R.id.product_price)
        val priceLong = detail.product.price?.toDoubleOrNull()?.toLong() ?: 0L
        val formattedPrice = String.format("%,d원", priceLong)
        priceTextView.text = "가격: $formattedPrice"

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
}