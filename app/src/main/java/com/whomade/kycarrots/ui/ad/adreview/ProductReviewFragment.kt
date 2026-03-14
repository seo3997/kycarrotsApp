package com.whomade.kycarrots.ui.ad.adreview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.AdDetailViewModel
import com.whomade.kycarrots.R
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.ui.common.LoginInfoUtil

class ProductReviewFragment : Fragment() {

    private val viewModel: AdDetailViewModel by activityViewModels()
    private lateinit var adapter: AdReviewAdapter
    private lateinit var rvReviews: RecyclerView
    private lateinit var tvEmptyReview: TextView
    private lateinit var fabAddReview: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        rvReviews = view.findViewById(R.id.rv_reviews)
        tvEmptyReview = view.findViewById(R.id.tv_empty_review)
        fabAddReview = view.findViewById(R.id.fab_add_review)

        val memberCode = LoginInfoUtil.getMemberCode(requireContext())
        val isBuyer = (memberCode == Constants.ROLE_PUB)
        val currentUserId = LoginInfoUtil.getUserId(requireContext())
        val isAdmin = (memberCode == Constants.ROLE_ADMIN)

        fabAddReview.visibility = if (isBuyer) View.VISIBLE else View.GONE
        fabAddReview.setOnClickListener {
            val intent = Intent(requireContext(), AdReviewWriteActivity::class.java).apply {
                putExtra("productId", viewModel.productDetail.value?.product?.productId?.toString())
            }
            startActivity(intent)
        }

        adapter = AdReviewAdapter(emptyList(), currentUserId, isAdmin) { reviewId ->
            viewModel.deleteReview(reviewId, viewModel.productDetail.value?.product?.productId?.toLongOrNull() ?: 0L)
        }
        rvReviews.adapter = adapter

        viewModel.reviewList.observe(viewLifecycleOwner) { reviews ->
            if (reviews.isEmpty()) {
                tvEmptyReview.visibility = View.VISIBLE
                rvReviews.visibility = View.GONE
            } else {
                tvEmptyReview.visibility = View.GONE
                rvReviews.visibility = View.VISIBLE
                adapter.updateData(reviews)
            }
        }

        viewModel.productDetail.observe(viewLifecycleOwner) { detail ->
            detail?.product?.productId?.toString()?.toLongOrNull()?.let { pid ->
                viewModel.loadReviews(pid)
            }
        }
    }
}
