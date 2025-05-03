package com.whomade.kycarrots.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.whomade.kycarrots.AdDetailActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.model.AdItem

class AdAdapter(private val items: List<AdItem>) :
    RecyclerView.Adapter<AdAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleText)
        val brief: TextView = view.findViewById(R.id.briefText)
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

        holder.itemView.setOnClickListener { v ->
            val context = v.context
            val intent = Intent(context, AdDetailActivity::class.java)
            intent.putExtra(AdDetailActivity.EXTRA_NAME, holder.title.text)
            context.startActivity(intent)
        }


        Glide.with(holder.image.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.cheese_1) // 로딩 중
            .error(R.drawable.cheese_1)       // 실패 시
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.image)
    }

    override fun getItemCount(): Int = items.size
}
