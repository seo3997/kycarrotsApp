package com.whomade.kycarrots.ui.ad.adreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.whomade.kycarrots.R

class AdReviewAdapter(
    private var reviews: List<Map<String, Any>>,
    private val currentUserId: String?,
    private val onDeleteClick: (String) -> Unit,
    private val onEditClick: (Map<String, Any>) -> Unit
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
    val imageUrl = review["FILE_RLTV_PATH"]?.toString() ?: review["imageUrl"]?.toString()
    val writerIdRaw = review["USER_NO"] ?: review["userNo"] ?: review["userId"]
    val writerId = writerIdRaw?.toString()?.split(".")?.get(0)
    val normalizedCurrentUserId = currentUserId?.split(".")?.get(0)

    holder.ratingBar.rating = rating
    holder.tvUserMeta.text = "$userNm | $createDt"
    holder.tvContents.text = contents

    if (!imageUrl.isNullOrBlank()) {
        holder.ivReviewImage.visibility = View.VISIBLE
        Glide.with(holder.itemView.context).load(imageUrl).into(holder.ivReviewImage)
    } else {
        holder.ivReviewImage.visibility = View.GONE
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
        val ivReviewImage: ImageView = view.findViewById(R.id.iv_review_image)
        val tvEdit: TextView = view.findViewById(R.id.tv_edit)
        val tvDelete: TextView = view.findViewById(R.id.tv_delete)
    }
}
