package com.whomade.kycarrots.ui.buy

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.whomade.kycarrots.AdDetailActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.model.AdItem

class AdActivityAdapter(
    private val activity: Activity
) : RecyclerView.Adapter<AdActivityAdapter.ViewHolder>() {

    private val items: MutableList<AdItem> = mutableListOf()

    fun updateList(newList: List<AdItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView  = view.findViewById(R.id.titleText)
        val brief: TextView  = view.findViewById(R.id.briefText)
        val image: ImageView = view.findViewById(R.id.imageView)

        init {
            view.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val item = items[pos]
                val intent = Intent(activity, AdDetailActivity::class.java).apply {
                    putExtra(AdDetailActivity.EXTRA_PRODUCT_ID, item.productId)
                    putExtra(AdDetailActivity.EXTRA_USER_ID,    item.userId)
                    putExtra("imageUrl", item.imageUrl)
                }
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    image,
                    "shared_image"
                )
                activity.startActivity(intent, options.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity)
            .inflate(R.layout.item_ad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.brief.text = item.description

        Glide.with(activity)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_placeholder_default)
            .error(R.drawable.ic_placeholder_default)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.image)
    }

    override fun getItemCount(): Int = items.size
}
