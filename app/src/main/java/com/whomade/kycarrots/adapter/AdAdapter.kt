package com.whomade.kycarrots.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.model.AdItem
import java.text.DecimalFormat

class AdAdapter(private val items: List<AdItem>) :
    RecyclerView.Adapter<AdAdapter.AdViewHolder>() {

    private val decimalFormat = DecimalFormat("#,###원")

    inner class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleText)
        val brief: TextView = view.findViewById(R.id.briefText)
        val price: TextView = view.findViewById(R.id.priceText)
        val image: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ad, parent, false)
        return AdViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.brief.text = item.description
        
        try {
            val priceVal = item.price.toDouble().toLong()
            holder.price.text = decimalFormat.format(priceVal)
        } catch (e: Exception) {
            holder.price.text = item.price
        }

        Glide.with(holder.image.context).load(item.imageUrl).into(holder.image)
    }

    override fun getItemCount(): Int = items.size
}
