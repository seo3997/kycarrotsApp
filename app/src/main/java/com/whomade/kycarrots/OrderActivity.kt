package com.whomade.kycarrots

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.OrderCreateRequest
import com.whomade.kycarrots.data.model.OrderItemRequest
import com.whomade.kycarrots.data.model.PaymentConfirmRequest
import com.whomade.kycarrots.databinding.ActivityOrderBinding
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.webview.WebViewActivity
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding

    private var productId: Long = 0
    private var productName: String = ""
    private var unitPrice: Int = 0
    private var selectedOption: String = ""
    private var quantity: Int = 1


    private val addressSearchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val address = result.data?.getStringExtra("address") ?: ""
            val zonecode = result.data?.getStringExtra("zonecode") ?: ""
            binding.etAddress1.setText(address)
            binding.etZipCode.setText(zonecode)
        }
    }

    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val status = result.data?.getStringExtra("status")
            if (status == "SUCCESS") {
                val paymentKey = result.data?.getStringExtra("paymentKey") ?: ""
                val orderId = result.data?.getStringExtra("orderId") ?: ""
                val amount = result.data?.getIntExtra("amount", 0) ?: 0
                confirmPayment(paymentKey, orderId, amount)
            } else {
                val message = result.data?.getStringExtra("message") ?: "결제에 실패했습니다."
                showToast(message)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initData()
        initViews()
    }

    private fun initData() {
        productId = intent.getLongExtra("productId", 0)
        productName = intent.getStringExtra("productName") ?: ""
        unitPrice = intent.getIntExtra("unitPrice", 0)
        selectedOption = intent.getStringExtra("selectedOption") ?: ""
        quantity = intent.getIntExtra("quantity", 1)

        binding.tvProductName.text = productName
        binding.tvProductOption.text = "옵션: $selectedOption / 수량: ${quantity}개"

        val totalItemAmount = unitPrice * quantity
        val deliveryFee = 3000 // 기본 배송비 예시
        val totalPayAmount = totalItemAmount + deliveryFee

        binding.tvTotalItemAmount.text = formatCurrency(totalItemAmount)
        binding.tvDeliveryFee.text = formatCurrency(deliveryFee)
        binding.tvTotalPayAmount.text = formatCurrency(totalPayAmount)
    }

    private fun initViews() {
        binding.btnSearchAddress.setOnClickListener {
            // 카카오 주소 검색 웹뷰 호출 (예시)
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("url", "https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js") // 실제 구현 시 주소검색 HTML 필요
                putExtra("title", "주소 검색")
                putExtra("isPostcode", true)
            }
            addressSearchLauncher.launch(intent)
        }

        binding.btnPay.setOnClickListener {
            if (validateInputs()) {
                createOrder()
            }
        }
    }


    private fun validateInputs(): Boolean {
        if (binding.etReceiverName.text.isNullOrBlank()) {
            showToast("수령인 이름을 입력해주세요.")
            return false
        }
        if (binding.etReceiverPhone.text.isNullOrBlank()) {
            showToast("연락처를 입력해주세요.")
            return false
        }
        if (binding.etAddress1.text.isNullOrBlank()) {
            showToast("주소를 검색해주세요.")
            return false
        }
        return true
    }

    private fun createOrder() {
        val userNo = LoginInfoUtil.getUserNo(this).toLongOrNull() ?: 0L
        val totalItemAmount = unitPrice * quantity
        val deliveryFee = 3000
        val totalPayAmount = totalItemAmount + deliveryFee

        val request = OrderCreateRequest(
            userNo = userNo,
            totalItemAmount = totalItemAmount,
            deliveryFee = deliveryFee,
            discountAmount = 0,
            totalPayAmount = totalPayAmount,
            receiverName = binding.etReceiverName.text.toString(),
            receiverPhone = binding.etReceiverPhone.text.toString(),
            zipCode = binding.etZipCode.text.toString(),
            address1 = binding.etAddress1.text.toString(),
            address2 = binding.etAddress2.text.toString(),
            orderMemo = binding.etOrderMemo.text.toString(),
            items = listOf(
                OrderItemRequest(
                    productId = productId,
                    quantity = quantity,
                    optionName = selectedOption
                )
            )
        )

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val api = RetrofitProvider.retrofit.create(AdApi::class.java)
                val response = api.createOrder(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val orderNo = response.body()?.orderNo ?: ""
                    requestPayment(orderNo, productName)
                } else {
                    showToast("주문 생성 실패: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                showToast("오류 발생: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun requestPayment(orderNo: String, orderName: String) {
        val amount = (unitPrice * quantity) + 3000
        val intent = Intent(this, PaymentWebViewActivity::class.java).apply {
            putExtra("orderNo", orderNo)
            putExtra("amount", amount)
            putExtra("productName", orderName)
        }
        paymentLauncher.launch(intent)
    }

    private fun confirmPayment(paymentKey: String, orderId: String, amount: Int) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val api = RetrofitProvider.retrofit.create(AdApi::class.java)
                val request = PaymentConfirmRequest(
                    paymentKey = paymentKey,
                    orderId = orderId,
                    amount = amount
                )
                val response = api.confirmPayment(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    showToast("결제가 완료되었습니다.")
                    finish()
                } else {
                    showToast("결제 승인 실패: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                showToast("결제 승인 중 오류: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun formatCurrency(amount: Int): String {
        return NumberFormat.getCurrencyInstance(Locale.KOREA).format(amount)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
