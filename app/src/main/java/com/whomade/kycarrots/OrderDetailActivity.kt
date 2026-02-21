package com.whomade.kycarrots

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.databinding.ActivityOrderDetailBinding
import com.whomade.kycarrots.data.model.OrderDetailResponse
import com.whomade.kycarrots.domain.service.AppService
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var appService: AppService
    private val decimalFormat = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_detail)

        val orderNo = intent.getStringExtra("orderNo") ?: ""
        if (orderNo.isEmpty()) {
            Toast.makeText(this, "주문 번호가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupService()
        loadOrderDetail(orderNo)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupService() {
        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        appService = AppService(repository)
    }

    private fun loadOrderDetail(orderNo: String) {
        binding.progressBarLayout.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = appService.getOrderDetail(orderNo)
                if (response != null) {
                    displayOrderDetail(response)
                } else {
                    Toast.makeText(this@OrderDetailActivity, "주문 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OrderDetailActivity, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBarLayout.visibility = View.GONE
            }
        }
    }

    private fun displayOrderDetail(data: OrderDetailResponse) {
        val order = data.order
        binding.tvOrderStatus.text = getOrderStatusText(order.orderStatus)
        binding.tvOrderNo.text = "주문번호: ${order.orderNo}"
        binding.tvOrderDate.text = "주문일시: ${order.orderedAt}"

        // Shipping Info
        binding.tvReceiverName.text = order.receiverName
        binding.tvReceiverPhone.text = order.receiverPhone
        binding.tvAddress.text = "(${order.zipCode}) ${order.address1} ${order.address2 ?: ""}"
        binding.tvOrderMemo.text = "배송 메모: ${order.orderMemo ?: "없음"}"

        // Price Info
        binding.tvTotalItemAmount.text = "${decimalFormat.format(order.totalItemAmount)}원"
        binding.tvDeliveryFee.text = "${decimalFormat.format(order.deliveryFee)}원"
        binding.tvDiscountAmount.text = "-${decimalFormat.format(order.discountAmount)}원"
        binding.tvTotalPayAmount.text = "${decimalFormat.format(order.totalPayAmount)}원"

        // Item
        if (data.items.isNotEmpty()) {
            val item = data.items[0]
            binding.tvProductName.text = item.productName
            binding.tvOptionName.text = if (item.optionName.isNullOrEmpty()) "" else "옵션: ${item.optionName}"
            binding.tvPriceQuantity.text = "${decimalFormat.format(item.unitPrice)}원 / ${item.quantity}개"

            Glide.with(this)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_placeholder_default)
                .error(R.drawable.ic_placeholder_default)
                .into(binding.ivProduct)
        }
    }

    private fun getOrderStatusText(status: String): String {
        return when (status) {
            "PAID" -> "결제완료"
            "CANCELLED" -> "주문취소"
            "SHIPPING" -> "배송중"
            "DELIVERED" -> "배송완료"
            else -> status
        }
    }
}
