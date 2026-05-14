package com.whomade.kycarrots

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductItem
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.TokenUtil
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import kotlinx.coroutines.launch

class ProductDescriptionFragment : Fragment() {

    private val viewModel: AdDetailViewModel by activityViewModels()

    private lateinit var spinner: AppCompatSpinner
    private lateinit var statusTextView: TextView
    private lateinit var btnBuy: View
    private lateinit var tvQuantity: TextView
    private lateinit var tvTotalPrice: TextView
    
    private var orderQuantity: Int = 1
    private var maxQuantity: Int = 1
    private var currentStatus: String? = null
    private var filteredList: List<TxtListDataInfo> = emptyList()
    private var statusList: List<TxtListDataInfo> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        observeViewModel()
    }

    private fun initViews(view: View) {
        spinner = view.findViewById(R.id.spinner_product_status)
        statusTextView = view.findViewById(R.id.text_product_status)
        btnBuy = view.findViewById(R.id.btn_buy)
        tvQuantity = view.findViewById(R.id.tv_quantity)
        tvTotalPrice = view.findViewById(R.id.tv_total_price)

        val btnMinus: View = view.findViewById(R.id.btn_minus)
        val btnPlus: View = view.findViewById(R.id.btn_plus)

        val memberCode = LoginInfoUtil.getMemberCode(requireContext())
        val isBuyer = (memberCode == Constants.ROLE_PUB)
        btnBuy.visibility = if (isBuyer) View.VISIBLE else View.GONE

        btnBuy.setOnClickListener {
            val detail = viewModel.productDetail.value ?: return@setOnClickListener
            if (detail.product.saleStatus != "1") {
                Toast.makeText(context, "판매 중인 상품만 구매 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val mainImageUrl = detail.imageMetas.firstOrNull { it.represent == "1" }?.imageUrl
            val intent = Intent(requireContext(), OrderActivity::class.java).apply {
                putExtra("productId", detail.product.productId!!.toLong())
                putExtra("productName", detail.product.title)
                putExtra("unitPrice", detail.product.price?.toDoubleOrNull()?.toInt() ?: 0)
                putExtra("selectedOption", detail.product.unitCodeNm)
                putExtra("quantity", orderQuantity)
                putExtra("productImage", mainImageUrl)
            }
            startActivity(intent)
        }

        btnMinus.setOnClickListener {
            if (currentStatus != "1") return@setOnClickListener
            if (orderQuantity > 1) {
                orderQuantity--
                tvQuantity.text = orderQuantity.toString()
                updateTotalAmount()
            }
        }

        btnPlus.setOnClickListener {
            if (currentStatus != "1") return@setOnClickListener
            if (orderQuantity < maxQuantity) {
                orderQuantity++
                tvQuantity.text = orderQuantity.toString()
                updateTotalAmount()
            } else {
                Toast.makeText(context, "최대 구매 가능 수량입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.productDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let { showProductDetail(it) }
        }
    }

    private fun showProductDetail(detail: ProductDetailResponse) {
        val view = requireView()
        view.findViewById<TextView>(R.id.product_title).text = detail.product.title
        currentStatus = detail.product.saleStatus

        val descriptionTextView: TextView = view.findViewById(R.id.product_description)
        val descriptionWebView: WebView = view.findViewById(R.id.product_description_webview)

        val editorMode = detail.product.editorMode
        if (editorMode == "1" || editorMode == "2") {
            descriptionTextView.visibility = View.GONE
            descriptionWebView.visibility = View.VISIBLE
            descriptionWebView.settings.javaScriptEnabled = true
            descriptionWebView.settings.defaultTextEncodingName = "UTF-8"
            descriptionWebView.settings.loadWithOverviewMode = true
            descriptionWebView.settings.useWideViewPort = true
            descriptionWebView.settings.domStorageEnabled = true
            descriptionWebView.settings.setSupportZoom(true)
            descriptionWebView.settings.builtInZoomControls = true
            descriptionWebView.settings.displayZoomControls = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                descriptionWebView.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            // NestedScrollView 안에서 줌이 잘 동작하도록 터치 이벤트 처리
            descriptionWebView.setOnTouchListener { v, event ->
                if (event.pointerCount > 1) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                false
            }
            
            var description = detail.product.description ?: "설명이 없습니다"
            if (description.contains("&lt;") || description.contains("&gt;")) {
               description = android.text.Html.fromHtml(description, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
            }

            val htmlContent = """
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=0.5, maximum-scale=5.0, user-scalable=yes">
                    <style>
                        * { box-sizing: border-box; }
                        html, body { margin: 0; padding: 0; width: 100%; overflow-x: hidden; }
                        img { max-width: 100% !important; height: auto !important; display: block; margin: 8px 0; }
                        table { width: 100% !important; border-collapse: collapse; table-layout: fixed; }
                        td, th { word-wrap: break-word; overflow-wrap: break-word; }
                        video, iframe { max-width: 100% !important; height: auto !important; }
                        body { 
                            word-wrap: break-word; 
                            padding: 16px;
                            font-size: 16px;
                            line-height: 1.6;
                            color: #333333;
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        }
                        /* Remove fixed widths from inline styles */
                        [style*="width"] { max-width: 100% !important; }
                    </style>
                </head>
                <body>$description</body>
                </html>
            """.trimIndent()
            descriptionWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } else {
            descriptionTextView.visibility = View.VISIBLE
            descriptionWebView.visibility = View.GONE
            descriptionTextView.text = detail.product.description ?: "설명이 없습니다"
        }

        val priceLong = detail.product.price?.toDoubleOrNull()?.toLong() ?: 0L
        view.findViewById<TextView>(R.id.product_price).text = String.format("%,d원", priceLong)

        maxQuantity = detail.product.availableQuantity.toIntOrNull() ?: 0
        view.findViewById<TextView>(R.id.tv_available_quantity).text = "구매 가능 수량: ${String.format("%,d", maxQuantity)} 개"

        val baseShippingFee = LoginInfoUtil.getBaseShippingFee(requireContext())
        val freeThreshold = LoginInfoUtil.getFreeShippingThreshold(requireContext())
        view.findViewById<TextView>(R.id.tv_delivery_fee).text = "배송비: ${String.format("%,d", baseShippingFee)}원"
        view.findViewById<TextView>(R.id.tv_free_shipping_threshold).text = "(${String.format("%,d", freeThreshold)}원 이상 구매 시 무료)"

        updateTotalAmount()
        renderSubImages(detail)
        loadProductStatusOptions(currentStatus)
        updatePurchaseUi()
    }

    private fun renderSubImages(detail: ProductDetailResponse) {
        val imageCardView = requireView().findViewById<MaterialCardView>(R.id.image_card_view)
        val subImages = detail.imageMetas.filter { it.represent == "0" }.take(3)

        if (subImages.isEmpty()) {
            imageCardView.visibility = View.GONE
        } else {
            imageCardView.visibility = View.VISIBLE
            val imageViews = listOf(
                requireView().findViewById<ImageView>(R.id.image_sub_1),
                requireView().findViewById<ImageView>(R.id.image_sub_2),
                requireView().findViewById<ImageView>(R.id.image_sub_3)
            )
            for (i in imageViews.indices) {
                if (i < subImages.size) {
                    Glide.with(this).load(subImages[i].imageUrl).apply(RequestOptions.centerCropTransform()).into(imageViews[i])
                    imageViews[i].visibility = View.VISIBLE
                } else {
                    imageViews[i].visibility = View.GONE
                }
            }
        }
    }

    private fun updateTotalAmount() {
        val detail = viewModel.productDetail.value ?: return
        val price = detail.product.price?.toDoubleOrNull() ?: 0.0
        val totalAmount = price * orderQuantity
        tvTotalPrice.text = String.format("%,d원", totalAmount.toLong())
    }

    private fun updatePurchaseUi() {
        val isSale = (currentStatus == "1")
        val alpha = if (isSale) 1.0f else 0.5f
        btnBuy.alpha = alpha
        requireView().findViewById<View>(R.id.btn_minus).alpha = alpha
        requireView().findViewById<View>(R.id.btn_plus).alpha = alpha
    }

    private fun loadProductStatusOptions(currentStatus: String?) {
        val memberCode = LoginInfoUtil.getMemberCode(requireContext())
        val isReadonly = (memberCode != Constants.ROLE_SELL)

        if (isReadonly) {
            spinner.visibility = View.GONE
            statusTextView.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val apiList = AppServiceProvider.getService().getCodeList("R010630")
                    val label = apiList.find { it.strIdx == currentStatus }?.strMsg ?: "알 수 없음"
                    statusTextView.text = label
                } catch (_: Exception) {}
            }
            return
        }

        spinner.visibility = View.VISIBLE
        statusTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                statusList = AppServiceProvider.getService().getCodeList("R010630")
                filteredList = statusList.filter {
                    it.strIdx in listOf("0", "1", "20", "30", "99") || it.strIdx == currentStatus
                }.distinctBy { it.strIdx }

                val names = filteredList.map { it.strMsg }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                currentStatus?.let {
                    val index = filteredList.indexOfFirst { code -> code.strIdx == it }
                    if (index != -1) spinner.setSelection(index)
                }

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    var initialized = false
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        if (!initialized) { initialized = true; return }
                        val selectedCode = filteredList[position].strIdx
                        if (selectedCode == currentStatus) return
                        // status change logic should probably stay in Activity or through ViewModel
                        (activity as? AdDetailActivity)?.handleStatusChange(names[position], selectedCode)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            } catch (_: Exception) {}
        }
    }
}
