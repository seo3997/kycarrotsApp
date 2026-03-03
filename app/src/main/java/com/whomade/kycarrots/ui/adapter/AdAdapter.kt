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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleText)
        val price: TextView = view.findViewById(R.id.priceText)
        val image: ImageView = view.findViewById(R.id.imageView)
        val status: TextView? = view.findViewById(R.id.statusText)
        val overlay: View? = view.findViewById(R.id.soldOutOverlay)
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

        // saleStatusNm 처리 (판매중이 아니면 표시)
        val isNotOnSale = !item.saleStatusNm.isNullOrEmpty() && item.saleStatusNm != "판매중"
        
        if (isNotOnSale) {
            holder.status?.apply {
                visibility = View.VISIBLE
                text = item.saleStatusNm
                // 판매중이 아닐 때의 스타일
                setBackgroundResource(R.drawable.bg_status_badge_inactive)
                setTextColor(android.graphics.Color.parseColor("#757575"))
            }
            holder.overlay?.visibility = View.VISIBLE
        } else {
            // "판매중" 이거나 null인 경우 paymentStatus 확인 (주문 목록 등에서)
            if (!item.paymentStatus.isNullOrEmpty()) {
                holder.status?.apply {
                    visibility = View.VISIBLE
                    text = getOrderStatusText(item.paymentStatus)
                    setBackgroundResource(R.drawable.bg_status_badge)
                    setTextColor(android.graphics.Color.parseColor("#1976D2"))
                }
                holder.overlay?.visibility = View.GONE
            } else {
                holder.status?.visibility = View.GONE
                holder.overlay?.visibility = View.GONE
            }
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

    override fun getItemCount(): Int = items.size

    fun removeByProductId(productId: String) {
        val idx = items.indexOfFirst { it.productId == productId }
        if (idx != -1) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}
