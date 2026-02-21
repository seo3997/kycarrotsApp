package com.whomade.kycarrots

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.whomade.kycarrots.databinding.ActivityOrderSuccessBinding
import java.text.NumberFormat
import java.util.Locale

class OrderSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_success)

        val orderNo = intent.getStringExtra("orderNo") ?: ""
        val amount = intent.getIntExtra("amount", 0)

        binding.tvOrderNo.text = "주문번호: $orderNo"
        binding.tvPayAmount.text = "결제금액: ${formatCurrency(amount)}"

        setSupportActionBar(binding.toolbar)

        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun formatCurrency(amount: Int): String {
        return NumberFormat.getCurrencyInstance(Locale.KOREA).format(amount)
    }

    override fun onBackPressed() {
        // Prevent going back to payment screens
        binding.btnGoHome.performClick()
    }
}
