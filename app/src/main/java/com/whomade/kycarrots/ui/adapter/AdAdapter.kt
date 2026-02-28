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

class AdAdapter(private val fragment: Fragment) :
    RecyclerView.Adapter<AdAdapter.ViewHolder>() {

    private val items: MutableList<AdItem> = mutableListOf()
    private val decimalFormat = DecimalFormat("#,###원")
    private var onItemClick: ((AdItem, View) -> Unit)? = null   // ⬅ 공유뷰까지 전달

    // 새로고침 또는 첫 로딩 시 사용
    fun updateList(newList: List<AdItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    // 무한스크롤 추가 로딩 시 사용
    fun addList(moreItems: List<AdItem>) {
        val start = items.size
        items.addAll(moreItems)
        notifyItemRangeInserted(start, moreItems.size)
    }

    // 리스트 전체 비움 (필요할 때)
    fun clearList() {
        items.clear()
        notifyDataSetChanged()
    }
    fun setOnItemClickListener(listener: (AdItem, View) -> Unit) { onItemClick = listener }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleText)
        val brief: TextView = view.findViewById(R.id.briefText)
        val price: TextView = view.findViewById(R.id.priceText)
        val image: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ad, parent, false)
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
    }

    override fun getItemCount(): Int = items.size

    fun removeByProductId(productId: String) {
        val idx = items.indexOfFirst { it.productId == productId }
        if (idx != -1) {
            val mutable = items.toMutableList()
            mutable.removeAt(idx)
            updateList(mutable) // or submitList if ListAdapter 사용
        }
    }
}
