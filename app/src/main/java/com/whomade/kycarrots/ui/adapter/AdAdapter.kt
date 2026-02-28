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
        val brief: TextView = view.findViewById(R.id.briefText)
        val price: TextView = view.findViewById(R.id.priceText)
        val image: ImageView = view.findViewById(R.id.imageView)
        val status: TextView? = view.findViewById(R.id.statusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutResId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.brief.text = item.description

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

        // statusText가 레이아웃에 있는 경우에만 처리
        holder.status?.let { statusView ->
            if (!item.paymentStatus.isNullOrEmpty()) {
                statusView.visibility = View.VISIBLE
                statusView.text = getOrderStatusText(item.paymentStatus)
            } else {
                statusView.visibility = View.GONE
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
