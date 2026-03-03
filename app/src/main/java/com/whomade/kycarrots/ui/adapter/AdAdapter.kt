package com.whomade.kycarrots.ui.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.whomade.kycarrots.AdDetailActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.model.AdItem
import java.text.DecimalFormat

class AdAdapter(
    private val fragment: Fragment,
    private val layoutResId: Int = R.layout.item_ad
) : RecyclerView.Adapter<AdAdapter.ViewHolder>() {

    private val items: MutableList<AdItem> = mutableListOf()
    private val decimalFormat = DecimalFormat("#,###원")
    private var onItemClick: ((AdItem, View) -> Unit)? = null
    private var onCancelClick: ((AdItem) -> Unit)? = null
    private var onReturnClick: ((AdItem) -> Unit)? = null

    fun updateList(newList: List<AdItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun addList(moreItems: List<AdItem>) {
        val start = items.size
        items.addAll(moreItems)
        notifyItemRangeInserted(start, moreItems.size)
    }

    fun clearList() {
        items.clear()
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (AdItem, View) -> Unit) {
        onItemClick = listener
    }

    fun setOnCancelClickListener(listener: (AdItem) -> Unit) {
        onCancelClick = listener
    }

    fun setOnReturnClickListener(listener: (AdItem) -> Unit) {
        onReturnClick = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleText)
        val price: TextView = view.findViewById(R.id.priceText)
        val image: ImageView = view.findViewById(R.id.imageView)
        val status: TextView? = view.findViewById(R.id.statusText)
        val overlay: View? = view.findViewById(R.id.soldOutOverlay)
        val btnCancel: View? = view.findViewById(R.id.btn_cancel)
        val btnReturn: View? = view.findViewById(R.id.btn_return)
        val orderNo: TextView? = view.findViewById(R.id.orderNoText)
        val deliveryInfo: TextView? = view.findViewById(R.id.deliveryInfoText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutResId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title

        try {
            val priceVal = item.price.toDouble().toLong()
            holder.price.text = decimalFormat.format(priceVal)
        } catch (e: Exception) {
            holder.price.text = item.price
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item, holder.image)
        }

        Glide.with(holder.image.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_placeholder_default)
            .error(R.drawable.ic_placeholder_default)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.image)

        // 주문 목록(구매 내역)인 경우와 일반 상품 목록인 경우를 구분하여 처리
        val isOrder = !item.orderNo.isNullOrEmpty()
        val isNotOnSale = !item.saleStatusNm.isNullOrEmpty() && item.saleStatusNm != "판매중"
        
        if (isOrder) {
            // 주문 내역 로직
            if (!item.paymentStatus.isNullOrEmpty()) {
                holder.status?.apply {
                    visibility = View.VISIBLE
                    text = item.orderStatusNm ?: getOrderStatusText(item.paymentStatus)
                    if (item.paymentStatus == "40") {
                        setBackgroundResource(R.drawable.bg_status_badge_inactive)
                        setTextColor(android.graphics.Color.parseColor("#64748b"))
                    } else {
                        setBackgroundResource(R.drawable.bg_status_badge)
                        setTextColor(android.graphics.Color.parseColor("#1976D2"))
                    }
                }
                holder.overlay?.visibility = View.GONE
                
                holder.orderNo?.apply {
                    visibility = View.VISIBLE
                    text = "주문번호: ${item.orderNo}"
                }

                if (item.paymentStatus == "60") {
                    holder.deliveryInfo?.apply {
                        visibility = View.VISIBLE
                        text = "택배사: ${item.deliveryCompanyNm ?: "-"} | 송장번호: ${item.trackingNo ?: "-"}"
                    }
                } else {
                    holder.deliveryInfo?.visibility = View.GONE
                }

                holder.btnCancel?.visibility = if (item.paymentStatus == "50") View.VISIBLE else View.GONE
                
                var showReturn = false
                if (item.paymentStatus == "70") {
                    item.deliveredAt?.let { rawDate ->
                        try {
                            val cleanDate = rawDate.replace("T", " ")
                            val formats = arrayOf("yyyy-MM-dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy-MM-dd")
                            var date: java.util.Date? = null
                            for (fmt in formats) {
                                try {
                                    date = java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault()).parse(cleanDate)
                                    if (date != null) break
                                } catch (e: Exception) {}
                            }
                            if (date != null) {
                                val cal = java.util.Calendar.getInstance()
                                cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
                                if (date.after(cal.time)) showReturn = true
                            }
                        } catch (e: Exception) {
                            Log.e("AdAdapter", "Date parse error: $rawDate")
                        }
                    }
                }
                holder.btnReturn?.visibility = if (showReturn) View.VISIBLE else View.GONE
                holder.btnCancel?.setOnClickListener { onCancelClick?.invoke(item) }
                holder.btnReturn?.setOnClickListener { onReturnClick?.invoke(item) }
            } else {
                holder.status?.visibility = View.GONE
                holder.overlay?.visibility = View.GONE
                holder.orderNo?.visibility = View.GONE
                holder.deliveryInfo?.visibility = View.GONE
                holder.btnCancel?.visibility = View.GONE
                holder.btnReturn?.visibility = View.GONE
            }
        } else if (isNotOnSale) {
            // 일반 목록: 판매 중지/품절 로직
            holder.status?.apply {
                visibility = View.VISIBLE
                text = item.saleStatusNm
                setBackgroundResource(R.drawable.bg_status_badge_inactive)
                setTextColor(android.graphics.Color.parseColor("#757575"))
            }
            holder.overlay?.visibility = View.VISIBLE
            holder.orderNo?.visibility = View.GONE
            holder.deliveryInfo?.visibility = View.GONE
            holder.btnCancel?.visibility = View.GONE
            holder.btnReturn?.visibility = View.GONE
        } else {
            // 일반 목록: 판매 중 로직
            holder.status?.visibility = View.GONE
            holder.overlay?.visibility = View.GONE
            holder.orderNo?.visibility = View.GONE
            holder.deliveryInfo?.visibility = View.GONE
            holder.btnCancel?.visibility = View.GONE
            holder.btnReturn?.visibility = View.GONE
        }
    }

    private fun getOrderStatusText(status: String): String {
        return when (status) {
            "READY", "10" -> "결제대기"
            "FAILED", "20" -> "결제실패"
            "PAID", "30" -> "결제완료"
            "CANCEL", "40" -> "주문취소"
            "PREPARING", "50" -> "배송준비중"
            "SHIPPING", "60" -> "배송중"
            "DELIVERED", "70" -> "배송완료"
            "RETURN_REQUESTED", "80" -> "반품요청"
            "RETURN_COMPLETED", "89" -> "반품완료"
            "CONFIRM", "99" -> "주문확정"
            "EXCHANGED", "90" -> "교환완료"
            else -> status
        }
    }

    override fun getItemCount(): Int = items.size

    fun removeByProductId(productId: String) {
        val idx = items.indexOfFirst { it.productId == productId }
        if (idx != -1) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}
