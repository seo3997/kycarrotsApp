package com.whomade.kycarrots.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.model.OrderDetailItem
import java.text.DecimalFormat

class OrderItemAdapter : RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

    private var items = listOf<OrderDetailItem>()
    private val decimalFormat = DecimalFormat("#,###")

    fun submitList(newItems: List<OrderDetailItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.productName.text = item.productName
        holder.optionName.text = if (item.optionName.isNullOrEmpty()) "" else "옵션: ${item.optionName}"
        holder.priceQuantity.text = "${decimalFormat.format(item.unitPrice)}원 / ${item.quantity}개"

        Glide.with(holder.productImage.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_placeholder_default)
            .error(R.drawable.ic_placeholder_default)
            .into(holder.productImage)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.iv_product)
        val productName: TextView = view.findViewById(R.id.tv_product_name)
        val optionName: TextView = view.findViewById(R.id.tv_option_name)
        val priceQuantity: TextView = view.findViewById(R.id.tv_price_quantity)
    }
}
