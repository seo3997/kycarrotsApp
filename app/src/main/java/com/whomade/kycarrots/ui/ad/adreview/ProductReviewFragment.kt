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
import android.widget.Toast
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.TokenUtil
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.whomade.kycarrots.ui.ad.ImageViewerActivity

class ProductReviewFragment : Fragment() {

    private val viewModel: AdDetailViewModel by activityViewModels()
    private lateinit var adapter: AdReviewAdapter
    private lateinit var rvReviews: RecyclerView
    private lateinit var tvEmptyReview: TextView
    private lateinit var fabAddReview: TextView

    private val reviewWriteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            viewModel.productDetail.value?.product?.productId?.toLongOrNull()?.let { pid ->
                viewModel.loadReviews(pid)
            }
        }
    }

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
        val currentUserId = LoginInfoUtil.getUserNo(requireContext())

        fabAddReview.visibility = if (isBuyer) View.VISIBLE else View.GONE
        fabAddReview.setOnClickListener {
            viewModel.productDetail.value?.product?.productId?.toLongOrNull()?.let { pid ->
                val intent = Intent(requireContext(), AdReviewWriteActivity::class.java).apply {
                    putExtra("productId", pid)
                }
                reviewWriteLauncher.launch(intent)
            } ?: run {
                Toast.makeText(requireContext(), "상품 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        adapter = AdReviewAdapter(emptyList(), currentUserId, 
            onDeleteClick = { reviewId ->
                val token = TokenUtil.getToken(requireContext())
                viewModel.deleteReview(reviewId, viewModel.productDetail.value?.product?.productId?.toLongOrNull() ?: 0L, token)
            },
            onEditClick = { review ->
                val pid = viewModel.productDetail.value?.product?.productId?.toLongOrNull() ?: 0L
                val filePaths = review["FILE_PATHS"]?.toString() ?: review["imageUrl"]?.toString() ?: ""
                val intent = Intent(requireContext(), AdReviewWriteActivity::class.java).apply {
                    putExtra("productId", pid)
                    putExtra("reviewId", (review["REVIEW_ID"] ?: review["reviewNo"])?.toString())
                    putExtra("rating", (review["RATING"] ?: review["rating"])?.toString()?.toFloatOrNull() ?: 0f)
                    putExtra("contents", (review["CONTENTS"] ?: review["contents"])?.toString())
                    putExtra("filePaths", filePaths)
                }
                reviewWriteLauncher.launch(intent)
            },
            onImageClick = { imageUrl ->
                val intent = Intent(requireContext(), ImageViewerActivity::class.java).apply {
                    putExtra("url", imageUrl)
                }
                startActivity(intent)
            }
        )
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
