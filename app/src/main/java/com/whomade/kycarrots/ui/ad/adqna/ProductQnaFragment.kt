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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class ProductQnaFragment : Fragment() {

    private val viewModel: AdDetailViewModel by activityViewModels()
    private lateinit var adapter: AdQnaAdapter
    private lateinit var rvQna: RecyclerView
    private lateinit var tvEmptyQna: TextView
    private lateinit var fabAddQna: TextView

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
        val currentUserId = LoginInfoUtil.getUserId(requireContext())
        val isAdminOrSeller = (memberCode == Constants.ROLE_ADMIN || memberCode == Constants.ROLE_SELL)

        fabAddQna.visibility = if (isBuyer) View.VISIBLE else View.GONE
        fabAddQna.setOnClickListener {
            val intent = Intent(requireContext(), AdQnaWriteActivity::class.java).apply {
                putExtra("productId", viewModel.productDetail.value?.product?.productId?.toString())
            }
            startActivity(intent)
        }

        adapter = AdQnaAdapter(emptyList(), currentUserId, isAdminOrSeller, 
            onDeleteClick = { qnaId ->
                viewModel.deleteQna(qnaId, viewModel.productDetail.value?.product?.productId?.toString()?.toLongOrNull() ?: 0L)
            },
            onAnswerClick = { qnaId ->
                showAnswerDialog(qnaId)
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

    private fun showAnswerDialog(qnaId: String) {
        val editText = EditText(requireContext())
        editText.hint = "답변 내용을 입력하세요"

        AlertDialog.Builder(requireContext())
            .setTitle("답변 등록")
            .setView(editText)
            .setPositiveButton("등록") { _, _ ->
                val answer = editText.text.toString().trim()
                if (answer.isNotEmpty()) {
                    viewModel.answerQna(qnaId, answer, viewModel.productDetail.value?.product?.productId?.toString()?.toLongOrNull() ?: 0L)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
