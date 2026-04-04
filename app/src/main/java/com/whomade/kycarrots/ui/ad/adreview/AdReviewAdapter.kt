package com.whomade.kycarrots.ui.ad.adreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.whomade.kycarrots.R
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class AdReviewAdapter(
    private var reviews: List<Map<String, Any>>,
    private val currentUserId: String?,
    private val onDeleteClick: (String) -> Unit,
    private val onEditClick: (Map<String, Any>) -> Unit,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<AdReviewAdapter.ViewHolder>() {

    fun updateData(newReviews: List<Map<String, Any>>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val review = reviews[position]
    val reviewId = review["REVIEW_ID"]?.toString() ?: review["reviewNo"]?.toString() ?: ""
    val rating = (review["RATING"]?.toString() ?: review["rating"]?.toString())?.toDoubleOrNull()?.toFloat() ?: 0f
    val contents = review["CONTENTS"]?.toString() ?: review["contents"]?.toString() ?: ""
    val userNm = review["USER_NM"]?.toString() ?: review["userNm"]?.toString() ?: "사용자"
    val createDt = review["REGIST_DT"]?.toString() ?: review["createDt"]?.toString() ?: ""
    val filePaths = review["FILE_PATHS"]?.toString() ?: review["imageUrl"]?.toString() ?: ""
    val writerIdRaw = review["USER_NO"] ?: review["userNo"] ?: review["userId"]
    val writerId = writerIdRaw?.toString()?.split(".")?.get(0)
    val normalizedCurrentUserId = currentUserId?.split(".")?.get(0)

    holder.ratingBar.rating = rating
    holder.tvUserMeta.text = "$userNm | $createDt"
    holder.tvContents.text = contents

    holder.llReviewImages.removeAllViews()
    if (!filePaths.isBlank()) {
        holder.hsvReviewImages.visibility = View.VISIBLE
        val paths = filePaths.split(",")
        paths.forEach { path ->
            val context = holder.itemView.context
            val resources = context.resources
            val imageView = ImageView(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    (80 * resources.displayMetrics.density).toInt(),
                    (80 * resources.displayMetrics.density).toInt()
                ).apply {
                    marginEnd = (8 * resources.displayMetrics.density).toInt()
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundResource(R.drawable.bg_rounded_image)
                clipToOutline = true
                setOnClickListener { onImageClick(path.trim()) }
            }
            val trimmedPath = path.trim()
            val requestListener = object : RequestListener<Drawable> {
                var retryCount = 0
                val maxRetries = 3

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    if (retryCount < maxRetries) {
                        retryCount++
                        imageView.postDelayed({
                            Glide.with(context)
                                .load(trimmedPath)
                                .listener(this)
                                .into(imageView)
                        }, 1000) // 1초 후 재시도
                        return true
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            }

            Glide.with(context)
                .load(trimmedPath)
                .placeholder(R.drawable.bg_rounded_image)
                .listener(requestListener)
                .into(imageView)
            holder.llReviewImages.addView(imageView)
        }
    } else {
        holder.hsvReviewImages.visibility = View.GONE
    }

    // Show delete/edit buttons strictly for the author
    if (normalizedCurrentUserId != null && normalizedCurrentUserId == writerId) {
        holder.tvDelete.visibility = View.VISIBLE
        holder.tvDelete.setOnClickListener { onDeleteClick(reviewId) }
        
        holder.tvEdit.visibility = View.VISIBLE
        holder.tvEdit.setOnClickListener { onEditClick(review) }
    } else {
        holder.tvDelete.visibility = View.GONE
        holder.tvEdit.visibility = View.GONE
    }
}
    override fun getItemCount(): Int = reviews.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ratingBar: RatingBar = view.findViewById(R.id.rating_bar)
        val tvUserMeta: TextView = view.findViewById(R.id.tv_user_meta)
        val tvContents: TextView = view.findViewById(R.id.tv_contents)
        val hsvReviewImages: View = view.findViewById(R.id.hsv_review_images)
        val llReviewImages: LinearLayout = view.findViewById(R.id.ll_review_images)
        val tvEdit: TextView = view.findViewById(R.id.tv_edit)
        val tvDelete: TextView = view.findViewById(R.id.tv_delete)
    }
}
