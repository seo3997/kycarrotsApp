package com.whomade.kycarrots

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.common.TokenUtil
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.common.Constants
class OrderMgtDetailActivity : AppCompatActivity() {

    private val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
    private val repository = RemoteRepository(adApi)
    private val appService = AppService(repository)

    private lateinit var tvOrderStatus: TextView
    private lateinit var tvOrderNo: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvBuyerInfo: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvMemo: TextView
    private lateinit var tvCarrierInfo: TextView
    private lateinit var rvItems: RecyclerView
    private lateinit var btnConfirmDeposit: Button
    private lateinit var btnBranchConfirmDeposit: Button
    private lateinit var btnConfirmOrder: Button
    private lateinit var llShippingInput: View
    private lateinit var spCarrier: Spinner
    private lateinit var etTrackingNo: EditText
    private lateinit var btnUpdateShipping: Button

    private var orderNo: String? = null
    private var carrierList: List<Map<String, Any>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_mgt_detail)

        orderNo = intent.getStringExtra("orderNo")
        if (orderNo.isNullOrEmpty()) {
            Toast.makeText(this, "주문 번호가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initToolbar()
        initViews()
        loadData()
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "주문 상세 관리"
        }
    }

    private fun initViews() {
        tvOrderStatus = findViewById(R.id.tv_order_status)
        tvOrderNo = findViewById(R.id.tv_order_no)
        tvOrderDate = findViewById(R.id.tv_order_date)
        tvBuyerInfo = findViewById(R.id.tv_buyer_info)
        tvAddress = findViewById(R.id.tv_address)
        tvMemo = findViewById(R.id.tv_memo)
        tvCarrierInfo = findViewById(R.id.tv_carrier_info)
        rvItems = findViewById(R.id.rv_items)
        btnConfirmDeposit = findViewById(R.id.btn_confirm_deposit)
        btnBranchConfirmDeposit = findViewById(R.id.btn_branch_confirm_deposit)
        btnConfirmOrder = findViewById(R.id.btn_confirm_order)
        llShippingInput = findViewById(R.id.ll_shipping_input)
        spCarrier = findViewById(R.id.sp_carrier)
        etTrackingNo = findViewById(R.id.et_tracking_no)
        btnUpdateShipping = findViewById(R.id.btn_update_shipping)

        btnConfirmDeposit.setOnClickListener { confirmAction("DEPOSIT") }
        btnConfirmOrder.setOnClickListener { confirmAction("ORDER") }
        btnUpdateShipping.setOnClickListener { confirmAction("SHIPPING") }
    }

    private fun loadData() {
        val token = TokenUtil.getToken(this)
        if (token.isEmpty()) return

        showProgressBar()
        lifecycleScope.launch {
            try {
                val result = appService.getOrderMgtDetail(orderNo!!, token)
                if (result != null) {
                    updateUI(result)
                } else {
                    Toast.makeText(this@OrderMgtDetailActivity, "주문 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                hideProgressBar()
            }
        }
    }

    private fun updateUI(data: Map<String, Any>) {
        val order = data["resultVo"] as? Map<*, *> ?: return
        val items = data["orderItemList"] as? List<Map<String, Any>> ?: emptyList()
        carrierList = data["deliveryCompanyList"] as? List<Map<String, Any>> ?: emptyList()

        tvOrderNo.text = "주문번호: ${order["orderNo"] ?: order["ORDER_NO"]}"
        tvOrderDate.text = "주문일시: ${order["orderedAt"] ?: order["ORDERED_AT"] ?: order["ORDER_DATE"] ?: ""}"
        
        val status = (order["orderStatus"] ?: order["ORDER_STATUS"])?.toString() ?: ""
        val statusNm = (order["orderStatusNm"] ?: order["ORDER_STATUS_NM"])?.toString()
        tvOrderStatus.text = statusNm ?: getOrderStatusNameFallback(status)

        // Customer Info: Prefer receiverName/Phone if available, else user/branch info
        val buyerName = order["receiverName"] ?: order["USER_NM"] ?: order["BRANCH_NAME"] ?: ""
        val buyerPhone = order["receiverPhone"] ?: order["TEL_NO"] ?: order["PHONE"] ?: ""
        tvBuyerInfo.text = "주문자: $buyerName ($buyerPhone)"
        
        val zip = order["zipCode"] ?: order["ZIP_CODE"] ?: ""
        val addr1 = order["address1"] ?: order["ADDRESS_MAIN"] ?: ""
        val addr2 = order["address2"] ?: order["ADDRESS_DETAIL"] ?: ""
        tvAddress.text = "주소: ($zip) $addr1 $addr2"
        
        val memo = order["orderMemo"] ?: order["ORDER_MEMO"] ?: "없음"
        tvMemo.text = "배송메모: $memo"

        val carrierNm = order["deliveryCompanyNm"] ?: order["DELIVERY_COMPANY_NM"] ?: ""
        val trackingNo = order["trackingNo"] ?: order["TRACKING_NO"] ?: ""
        if (carrierNm.toString().isNotEmpty() || trackingNo.toString().isNotEmpty()) {
            tvCarrierInfo.text = "현재배송: $carrierNm (${trackingNo})"
            tvCarrierInfo.visibility = View.VISIBLE
        } else {
            tvCarrierInfo.visibility = View.GONE
        }

        val role = LoginInfoUtil.getMemberCode(this)
        
        // Action visibility based on status
        btnConfirmDeposit.visibility = if (status == "10") View.VISIBLE else View.GONE
        btnBranchConfirmDeposit.visibility = if (role == Constants.ROLE_PROJ && status == "50") View.VISIBLE else View.GONE
        llShippingInput.visibility = if (status == "30") View.VISIBLE else View.GONE
        btnConfirmOrder.visibility = if (status == "70") View.VISIBLE else View.GONE

        // Setup Carrier Spinner
        val carrierNames = carrierList.map { it["CODE_NM"] ?: it["codeNm"] ?: "" }.map { it.toString() }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, carrierNames)
        spCarrier.adapter = spinnerAdapter
        
        // Select current carrier if exists in spinner
        val currentCarrierCode = (order["deliveryCompanyCode"] ?: order["DELIVERY_COMPANY_CODE"])?.toString() ?: ""
        if (currentCarrierCode.isNotEmpty()) {
            val idx = carrierList.indexOfFirst { (it["CODE"] ?: it["code"])?.toString() == currentCarrierCode }
            if (idx >= 0) spCarrier.setSelection(idx)
        }

        // If shipping already exists, fill it
        etTrackingNo.setText(trackingNo.toString())

        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = OrderItemsAdapter(items)
    }

    private fun confirmAction(type: String) {
        val token = TokenUtil.getToken(this)
        if (token.isEmpty()) return

        lifecycleScope.launch {
            showProgressBar()
            try {
                val success = when (type) {
                    "DEPOSIT" -> appService.confirmDeposit(token, orderNo!!)
                    "ORDER" -> appService.confirmOrderMgt(token, orderNo!!)
                    "SHIPPING" -> {
                        val carrier = carrierList[spCarrier.selectedItemPosition]["CODE"]?.toString() ?: ""
                        val tracking = etTrackingNo.text.toString()
                        if (tracking.isEmpty()) {
                            Toast.makeText(this@OrderMgtDetailActivity, "운송장 번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                            hideProgressBar()
                            return@launch
                        }
                        appService.updateShipping(token, orderNo!!, carrier, tracking)
                    }
                    else -> false
                }

                if (success) {
                    Toast.makeText(this@OrderMgtDetailActivity, "처리가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    loadData() // Refresh
                } else {
                    Toast.makeText(this@OrderMgtDetailActivity, "처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                hideProgressBar()
            }
        }
    }

    private fun getOrderStatusNameFallback(status: String): String {
        return when (status) {
            "10" -> "결제대기"
            "30" -> "결제완료"
            "40" -> "주문취소"
            "50" -> "배송준비"
            "60" -> "배송중"
            "70" -> "배송완료"
            "80" -> "반품요청"
            "89" -> "반품완료"
            "99" -> "주문확정"
            else -> status
        }
    }

    private fun showProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        findViewById<View>(R.id.ll_progress_circle)?.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class OrderItemsAdapter(private val items: List<Map<String, Any>>) : RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(android.R.id.text1)
            val sub: TextView = view.findViewById(android.R.id.text2)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }
        override fun getItemCount(): Int = items.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val prodName = item["productName"] ?: item["PRODUCT_NAME"] ?: item["TITLE"] ?: ""
            //val optName = item["optionName"] ?: item["OPTION_NAME"] ?: "기본"
            holder.name.text = "$prodName"
            
            val qty = (item["quantity"] ?: item["QUANTITY"] ?: 0).toString().toDoubleOrNull()?.toInt() ?: 0
            val price = (item["unitPrice"] ?: item["UNIT_PRICE"] ?: 0).toString().toDoubleOrNull()?.toInt() ?: 0
            holder.sub.text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(qty)}개 / " +
                    NumberFormat.getCurrencyInstance(Locale.KOREA).format(price * qty)
        }
    }
}
