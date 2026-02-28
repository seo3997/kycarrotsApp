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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.whomade.kycarrots.data.model.OrderCancelRequest
import com.whomade.kycarrots.data.model.PaymentCancelRequest
import androidx.appcompat.app.AlertDialog

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var appService: AppService
    private val decimalFormat = DecimalFormat("#,###")
    private var currentOrder: com.whomade.kycarrots.data.model.OrderInfo? = null

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

        binding.btnCancelOrder.setOnClickListener {
            showCancelConfirmDialog()
        }
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
        currentOrder = order
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

        checkCancelEligibility(order)
    }

    private fun checkCancelEligibility(order: com.whomade.kycarrots.data.model.OrderInfo) {
        if (order.orderStatus == "PAID") {
            val dateToCheck = order.paidAt ?: order.orderedAt
            if (dateToCheck != null) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val date = sdf.parse(dateToCheck)
                    if (date != null) {
                        val oneWeekAgo = Calendar.getInstance()
                        oneWeekAgo.add(Calendar.DAY_OF_YEAR, -7)

                        if (date.after(oneWeekAgo.time)) {
                            binding.btnCancelOrder.visibility = View.VISIBLE
                        } else {
                            binding.btnCancelOrder.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    binding.btnCancelOrder.visibility = View.GONE
                }
            } else {
                binding.btnCancelOrder.visibility = View.GONE
            }
        } else {
            binding.btnCancelOrder.visibility = View.GONE
        }
    }

    private fun getOrderStatusText(status: String): String {
        return when (status) {
            "READY" -> "결제대기"
            "FAILED" -> "결제실패"
            "PAID" -> "결제완료"
            "CANCEL" -> "주문취소"
            "PREPARING" -> "배송준비중"
            "SHIPPING" -> "배송중"
            "DELIVERED" -> "배송완료"
            "RETURN_REQUESTED" -> "반품요청"
            "EXCHANGED" -> "교환완료"
            else -> status
        }
    }

    private fun showCancelConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("주문 취소")
            .setMessage("정말로 주문을 취소하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                cancelOrder()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun cancelOrder() {
        val order = currentOrder ?: return
        val paymentKey = order.paymentKey ?: ""
        
        if (paymentKey.isEmpty()) {
            Toast.makeText(this, "주문 정보가 부족하여 취소 처리를 진행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = PaymentCancelRequest(
            paymentKey = paymentKey,
            orderId = order.orderNo,
            amount = order.totalPayAmount
        )

        binding.progressBarLayout.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val success = appService.cancelPayment(request)
                if (success) {
                    Toast.makeText(this@OrderDetailActivity, "주문이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    loadOrderDetail(order.orderNo) // Refresh
                } else {
                    Toast.makeText(this@OrderDetailActivity, "주문 취소에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OrderDetailActivity, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBarLayout.visibility = View.GONE
            }
        }
    }
}
