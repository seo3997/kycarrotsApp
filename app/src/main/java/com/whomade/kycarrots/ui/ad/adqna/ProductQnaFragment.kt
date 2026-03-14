package com.whomade.kycarrots.ui.ad.adqna

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
import com.whomade.kycarrots.ui.common.TokenUtil
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ProductQnaFragment : Fragment() {

    private val viewModel: AdDetailViewModel by activityViewModels()
    private lateinit var adapter: AdQnaAdapter
    private lateinit var rvQna: RecyclerView
    private lateinit var tvEmptyQna: TextView
    private lateinit var fabAddQna: TextView

    private val qnaWriteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            viewModel.productDetail.value?.product?.productId?.toString()?.toLongOrNull()?.let { pid ->
                viewModel.loadQnas(pid)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_qna, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        rvQna = view.findViewById(R.id.rv_qna)
        tvEmptyQna = view.findViewById(R.id.tv_empty_qna)
        fabAddQna = view.findViewById(R.id.fab_add_qna)

        val memberCode = LoginInfoUtil.getMemberCode(requireContext())
        val isBuyer = (memberCode == Constants.ROLE_PUB)
        val currentUserId = LoginInfoUtil.getUserNo(requireContext())

        fabAddQna.visibility = if (isBuyer) View.VISIBLE else View.GONE
        fabAddQna.setOnClickListener {
            viewModel.productDetail.value?.product?.productId?.toLongOrNull()?.let { pid ->
                val intent = Intent(requireContext(), AdQnaWriteActivity::class.java).apply {
                    putExtra("productId", pid)
                }
                qnaWriteLauncher.launch(intent)
            } ?: run {
                Toast.makeText(requireContext(), "상품 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        adapter = AdQnaAdapter(emptyList(), currentUserId, 
            onDeleteClick = { qnaId ->
                val token = TokenUtil.getToken(requireContext())
                viewModel.deleteQna(qnaId, viewModel.productDetail.value?.product?.productId?.toString()?.toLongOrNull() ?: 0L, token)
            },
            onEditClick = { qna ->
                val pid = viewModel.productDetail.value?.product?.productId?.toString()?.toLongOrNull() ?: 0L
                val intent = Intent(requireContext(), AdQnaWriteActivity::class.java).apply {
                    putExtra("productId", pid)
                    putExtra("qnaId", (qna["QNA_ID"] ?: qna["qnaNo"])?.toString())
                    putExtra("title", (qna["TITLE"] ?: qna["title"])?.toString())
                    putExtra("contents", (qna["CONTENTS"] ?: qna["contents"])?.toString())
                    putExtra("secretYn", (qna["SECRET_YN"] ?: qna["secretYn"])?.toString())
                }
                qnaWriteLauncher.launch(intent)
            }
        )
        rvQna.adapter = adapter

        viewModel.qnaList.observe(viewLifecycleOwner) { qnas ->
            if (qnas.isEmpty()) {
                tvEmptyQna.visibility = View.VISIBLE
                rvQna.visibility = View.GONE
            } else {
                tvEmptyQna.visibility = View.GONE
                rvQna.visibility = View.VISIBLE
                adapter.updateData(qnas)
            }
        }

        viewModel.productDetail.observe(viewLifecycleOwner) { detail ->
            detail?.product?.productId?.toString()?.toLongOrNull()?.let { pid ->
                viewModel.loadQnas(pid)
            }
        }
    }

}
