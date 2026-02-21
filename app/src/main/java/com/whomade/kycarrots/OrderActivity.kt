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
    private var expectedAmount: Int = 0
    private var deliveryFee: Int = 0 // Changed from hardcoded value to member variable


    private val addressSearchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val address = result.data?.getStringExtra("address") ?: ""
            val zipCode = result.data?.getStringExtra("zipCode") ?: ""
            binding.etAddress1.setText(address)
            binding.etZipCode.setText(zipCode)
        }
    }

    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val status = result.data?.getStringExtra("status")
            if (status == "SUCCESS") {
                val paymentKey = result.data?.getStringExtra("paymentKey") ?: ""
                val orderId = result.data?.getStringExtra("orderId") ?: ""
                val amount = result.data?.getIntExtra("amount", 0) ?: 0
                
                if (amount != expectedAmount) {
                    showToast("결제 금액이 일치하지 않습니다. ($expectedAmount vs $amount)")
                    return@registerForActivityResult
                }
                
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
        // TODO: Later update this to fetch from product detail or server
        deliveryFee = 0 
        val totalPayAmount = totalItemAmount + deliveryFee

        binding.tvTotalItemAmount.text = formatCurrency(totalItemAmount)
        binding.tvDeliveryFee.text = formatCurrency(deliveryFee)
        binding.tvTotalPayAmount.text = formatCurrency(totalPayAmount)
    }

    private fun initViews() {
        binding.btnSearchAddress.setOnClickListener {
            val intent = Intent(this, AddressSearchActivity::class.java)
            addressSearchLauncher.launch(intent)
        }

        binding.btnPay.setOnClickListener {
            if (validateInputs()) {
                showPaymentConfirmDialog()
            }
        }

        setupPhoneNumberFormatting()
    }

    private fun setupPhoneNumberFormatting() {
        binding.etReceiverPhone.addTextChangedListener(object : android.text.TextWatcher {
            private var isFormatting: Boolean = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true

                val input = s.toString().replace("-", "")
                val formatted = formatPhoneNumber(input)

                s.replace(0, s.length, formatted)
                isFormatting = false
            }

            private fun formatPhoneNumber(input: String): String {
                val len = input.length
                return when {
                    len <= 3 -> input
                    len <= 7 -> {
                        if (input.startsWith("02") && len > 2) {
                            "${input.substring(0, 2)}-${input.substring(2)}"
                        } else if (len > 3) {
                            "${input.substring(0, 3)}-${input.substring(3)}"
                        } else {
                            input
                        }
                    }
                    len <= 10 -> {
                        if (input.startsWith("02")) {
                            val mid = if (len == 10) 6 else 5
                            "${input.substring(0, 2)}-${input.substring(2, mid)}-${input.substring(mid)}"
                        } else {
                            "${input.substring(0, 3)}-${input.substring(3, 6)}-${input.substring(6)}"
                        }
                    }
                    else -> {
                        val end = if (len > 11) 11 else len
                        "${input.substring(0, 3)}-${input.substring(3, 7)}-${input.substring(7, end)}"
                    }
                }
            }
        })
    }

    private fun showPaymentConfirmDialog() {
        val totalAmount = (unitPrice * quantity) + deliveryFee
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("결제 확인")
            .setMessage("${productName}을(를) ${formatCurrency(totalAmount)}에 결제하시겠습니까?")
            .setPositiveButton("결제하기") { _, _ ->
                createOrder()
            }
            .setNegativeButton("취소", null)
            .show()
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
                    val body = response.body()!!
                    requestPayment(body.orderNo, body.orderName, body.amount)
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

    private fun requestPayment(orderNo: String, orderName: String, amount: Int) {
        this.expectedAmount = amount
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
